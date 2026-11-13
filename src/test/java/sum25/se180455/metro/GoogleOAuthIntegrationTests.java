package sum25.se180455.metro;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.web.util.UriComponentsBuilder;
import sum25.se180455.metro.main.MetroApplication;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real authorization-code callback and JWT verification against an isolated local OAuth provider. */
@SpringBootTest(classes = {MetroApplication.class, GoogleOAuthIntegrationTests.ProviderConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleOAuthIntegrationTests {
    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();
    private static final RSAKey KEY;
    private static final HttpServer SERVER;
    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            KEY = new RSAKey.Builder((RSAPublicKey) pair.getPublic()).privateKey((RSAPrivateKey) pair.getPrivate()).keyID("test-key").build();
            SERVER = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            SERVER.createContext("/jwks", exchange -> {
                byte[] body = new JWKSet(KEY.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length); exchange.getResponseBody().write(body); exchange.close();
            });
            SERVER.createContext("/token", exchange -> {
                String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = new HashMap<>();
                for (String part : request.split("&")) {
                    String[] keyValue = part.split("=", 2);
                    params.put(keyValue[0], URLDecoder.decode(keyValue.length == 2 ? keyValue[1] : "", StandardCharsets.UTF_8));
                }
                String token = params.containsKey("code_verifier") ? TOKENS.remove(params.get("code")) : null;
                String json = token == null ? "{\"error\":\"invalid_grant\"}"
                        : "{\"access_token\":\"test-access-token\",\"token_type\":\"Bearer\",\"expires_in\":300,\"scope\":\"openid\",\"id_token\":\"" + token + "\"}";
                byte[] body = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(token == null ? 400 : 200, body.length);
                exchange.getResponseBody().write(body); exchange.close();
            });
            SERVER.start();
        } catch (Exception exception) { throw new ExceptionInInitializerError(exception); }
    }
    @TestConfiguration(proxyBeanMethods = false)
    static class ProviderConfig {
        @Bean ClientRegistrationRepository clientRegistrationRepository() {
            String base = "http://127.0.0.1:" + SERVER.getAddress().getPort();
            ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google").clientId("test-client")
                    .clientSecret("test-secret").redirectUri("http://localhost/login/oauth2/code/google")
                    .tokenUri(base + "/token").jwkSetUri(base + "/jwks").scope("openid", "email", "profile").build();
            ClientRegistration link = ClientRegistration.withClientRegistration(google).registrationId("google-link")
                    .redirectUri("http://localhost/login/oauth2/code/google-link").build();
            return new InMemoryClientRegistrationRepository(google, link);
        }
    }
    @AfterAll static void stopServer() { SERVER.stop(0); }
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired OAuthAccountRepository accounts;
    @Autowired PasswordEncoder encoder;
    private record Flow(MockHttpSession session, String state, String nonce, String registration) { }
    private Flow begin(String registration, MockHttpSession session) throws Exception {
        var request = get("/oauth2/authorization/" + registration);
        if (session != null) request.session(session);
        MvcResult result = mvc.perform(request).andExpect(status().is3xxRedirection()).andReturn();
        String location = result.getResponse().getRedirectedUrl();
        var params = UriComponentsBuilder.fromUriString(location).build().getQueryParams();
        assertThat(params.getFirst("response_type")).isEqualTo("code");
        assertThat(params.getFirst("state")).isNotBlank(); assertThat(params.getFirst("nonce")).isNotBlank();
        assertThat(params.getFirst("code_challenge_method")).isEqualTo("S256"); assertThat(params.getFirst("code_challenge")).isNotBlank();
        return new Flow((MockHttpSession) result.getRequest().getSession(),
                URLDecoder.decode(params.getFirst("state"), StandardCharsets.UTF_8),
                URLDecoder.decode(params.getFirst("nonce"), StandardCharsets.UTF_8), registration);
    }
    private String token(Flow flow, String subject, String email, boolean verified, String fault) throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer("issuer".equals(fault) ? "https://wrong.example" : "https://accounts.google.com")
                .audience("audience".equals(fault) ? "wrong-client" : "test-client").subject(subject)
                .issueTime(Date.from(now.minusSeconds("expired".equals(fault) ? 1000 : 5)))
                .expirationTime(Date.from(now.plusSeconds("expired".equals(fault) ? -600 : 300)))
                .claim("nonce", "nonce".equals(fault) ? "wrong-nonce" : flow.nonce()).claim("email", email)
                .claim("email_verified", verified).claim("name", "Người dùng Google").build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY.getKeyID()).build(), claims);
        if ("signature".equals(fault)) {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048);
            jwt.sign(new RSASSASigner((RSAPrivateKey) generator.generateKeyPair().getPrivate()));
        } else {
            jwt.sign(new RSASSASigner(KEY));
        }
        return jwt.serialize();
    }
    private ResultActions callback(Flow flow, String subject, String email, boolean verified, String fault) throws Exception {
        String code = UUID.randomUUID().toString(); TOKENS.put(code, token(flow, subject, email, verified, fault));
        return mvc.perform(get("/login/oauth2/code/" + flow.registration()).session(flow.session()).param("state", flow.state()).param("code", code));
    }
    private Member local() {
        Member member = new Member(); member.setName("Local user"); member.setEmail(UUID.randomUUID() + "@example.com");
        member.setPassword(encoder.encode("MetroSecure12345")); member.setRole("MEMBER"); member.setStatus("ACTIVE");
        return members.saveAndFlush(member);
    }
    @Test void firstAndReturningGoogleLoginUseSubjectAndSharedSession() throws Exception {
        String sub = UUID.randomUUID().toString(); String email = UUID.randomUUID() + "@example.com";
        Flow first = begin("google", null);
        callback(first, sub, email, true, "").andExpect(redirectedUrl("/"));
        Member member = members.findByEmail(email); assertThat(member.getRole()).isEqualTo("MEMBER");
        assertThat(accounts.findByProviderAndProviderUserId("google", sub)).isPresent();
        mvc.perform(get("/profile").session(first.session())).andExpect(status().isOk());
        assertThat(((Member) first.session().getAttribute("member")).getMemberId()).isEqualTo(member.getMemberId());
        Flow again = begin("google", null);
        callback(again, sub, "changed@example.com", true, "").andExpect(redirectedUrl("/"));
        mvc.perform(get("/profile").session(again.session())).andExpect(status().isOk());
        assertThat(((Member) again.session().getAttribute("member")).getMemberId()).isEqualTo(member.getMemberId());
    }
    @Test void existingEmailCannotBeAutomaticallyLinked() throws Exception {
        Member member = local(); String sub = UUID.randomUUID().toString();
        callback(begin("google", null), sub, member.getEmail(), true, "").andExpect(redirectedUrl("/login?google=link_required"));
        assertThat(accounts.findByProviderAndProviderUserId("google", sub)).isEmpty();
    }
    @Test void linkingRequiresPasswordConfirmationAndPreservesLocalIdentity() throws Exception {
        Member member = local();
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login").with(csrf()).param("email", member.getEmail())
                .param("password", "MetroSecure12345")).andReturn().getRequest().getSession();
        mvc.perform(post("/account/google/link").session(session).with(csrf()).param("password", "wrong"))
                .andExpect(redirectedUrl("/profile"));
        mvc.perform(post("/account/google/link").session(session).with(csrf()).param("password", "MetroSecure12345"))
                .andExpect(redirectedUrl("/oauth2/authorization/google-link"));
        String sub = UUID.randomUUID().toString();
        callback(begin("google-link", session), sub, "different@example.com", true, "").andExpect(redirectedUrl("/"));
        assertThat(accounts.findByProviderAndProviderUserId("google", sub).orElseThrow().getMember().getMemberId()).isEqualTo(member.getMemberId());
        mvc.perform(get("/profile").session(session)).andExpect(status().isOk());
        assertThat(((Member) session.getAttribute("member")).getEmail()).isEqualTo(member.getEmail());
        callback(begin("google-link", session), UUID.randomUUID().toString(), "other@example.com", true, "")
                .andExpect(redirectedUrl("/login?google=link_required"));
    }
    @Test void invalidIssuerAudienceExpiryNonceAndUnverifiedEmailAreRejected() throws Exception {
        for (String fault : new String[]{"issuer", "audience", "expired", "nonce", "unverified", "signature"}) {
            String sub = UUID.randomUUID().toString();
            callback(begin("google", null), sub, UUID.randomUUID() + "@example.com", !fault.equals("unverified"), fault)
                    .andExpect(redirectedUrl("/login?google=failed"));
            assertThat(accounts.findByProviderAndProviderUserId("google", sub)).isEmpty();
        }
    }
    @Test void deniedConsentInvalidStateAndTokenExchangeFailureAreHandled() throws Exception {
        Flow denied = begin("google", null);
        mvc.perform(get("/login/oauth2/code/google").session(denied.session()).param("state", denied.state()).param("error", "access_denied"))
                .andExpect(redirectedUrl("/login?google=failed"));
        Flow tampered = begin("google", null);
        mvc.perform(get("/login/oauth2/code/google").session(tampered.session()).param("state", "wrong-state").param("code", "unknown"))
                .andExpect(redirectedUrl("/login?google=failed"));
        Flow invalidCode = begin("google", null);
        mvc.perform(get("/login/oauth2/code/google").session(invalidCode.session()).param("state", invalidCode.state()).param("code", "unknown"))
                .andExpect(redirectedUrl("/login?google=failed"));
    }
    @Test void disabledLinkedAccountCannotLogin() throws Exception {
        String sub = UUID.randomUUID().toString(); String email = UUID.randomUUID() + "@example.com";
        callback(begin("google", null), sub, email, true, "").andExpect(redirectedUrl("/"));
        Member member = members.findByEmail(email); member.setStatus("INACTIVE"); members.saveAndFlush(member);
        callback(begin("google", null), sub, email, true, "").andExpect(redirectedUrl("/login?google=failed"));
    }
    @Test void expiredLinkConfirmationIsRejectedAndGoogleIdentityCannotMoveBetweenMembers() throws Exception {
        String sub = UUID.randomUUID().toString(); String email = UUID.randomUUID() + "@example.com";
        callback(begin("google", null), sub, email, true, "").andExpect(redirectedUrl("/"));
        Long originalId = members.findByEmail(email).getMemberId();
        Member member = local();
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login").with(csrf()).param("email", member.getEmail())
                .param("password", "MetroSecure12345")).andReturn().getRequest().getSession();
        mvc.perform(post("/account/google/link").session(session).with(csrf()).param("password", "MetroSecure12345"))
                .andExpect(redirectedUrl("/oauth2/authorization/google-link"));
        callback(begin("google-link", session), sub, email, true, "").andExpect(redirectedUrl("/login?google=failed"));
        assertThat(accounts.findByProviderAndProviderUserId("google", sub).orElseThrow().getMember().getMemberId()).isEqualTo(originalId);
        mvc.perform(post("/account/google/link").session(session).with(csrf()).param("password", "MetroSecure12345"))
                .andExpect(redirectedUrl("/oauth2/authorization/google-link"));
        session.setAttribute(sum25.se180455.metro.security.GoogleOidcUserService.LINK_EXPIRES, 0L);
        String newSub = UUID.randomUUID().toString();
        callback(begin("google-link", session), newSub, member.getEmail(), true, "").andExpect(redirectedUrl("/login?google=link_required"));
        assertThat(accounts.findByProviderAndProviderUserId("google", newSub)).isEmpty();
    }
}
