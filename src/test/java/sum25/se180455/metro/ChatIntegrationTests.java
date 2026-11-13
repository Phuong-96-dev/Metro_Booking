package sum25.se180455.metro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sum25.se180455.metro.main.MetroApplication;
import sum25.se180455.metro.service.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MetroApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ChatSessionManager sessions;
    @Autowired sum25.se180455.metro.repository.MemberRepository members;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder passwords;
    @MockitoBean GeminiChatService gemini;

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder message(MockHttpSession session, String text) throws Exception {
        return post("/api/chat/message").session(session).with(csrf()).with(request -> {
            request.setRemoteAddr(session.getId()); return request;
        }).contentType("application/json").content(json.writeValueAsString(Map.of("message", text)));
    }
    @BeforeEach void answer() { when(gemini.reply(anyString(), anyList())).thenReturn("Bạn hãy mở mục Vé tàu."); }

    @Test void publicChatRequiresCsrfAndAcceptsThymeleafHeaderToken() throws Exception {
        mvc.perform(post("/api/chat/message").contentType("application/json").content("{\"message\":\"Chào\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/chat/reset").with(csrf().useInvalidToken())).andExpect(status().isForbidden());
        verifyNoInteractions(gemini);
        var page = mvc.perform(get("/login")).andExpect(status().isOk()).andReturn();
        String html = page.getResponse().getContentAsString();
        var session = (MockHttpSession) page.getRequest().getSession(false);
        String token = attribute(html, "data-csrf-token");
        String header = attribute(html, "data-csrf-header");
        mvc.perform(post("/api/chat/message").session(session).contentType("application/json")
                        .header(header, token).content("{\"message\":\"Mua vé?\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reply").value("Bạn hãy mở mục Vé tàu."))
                .andExpect(header().string("Cache-Control", "no-store"));
    }
    @Test void acceptsAuthenticatedChatAndRetainsExistingAccessRules() throws Exception {
        mvc.perform(message(new MockHttpSession(), "Mua vé?").with(user("member@example.com").roles("MEMBER")))
                .andExpect(status().isOk());
        mvc.perform(get("/profile")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admindashboard").with(user("member@example.com").roles("MEMBER"))).andExpect(status().isForbidden());
        mvc.perform(post("/ticket/buy").param("ticketTypeId", "1")).andExpect(status().isForbidden());
    }
    @Test void realLoginReplacesAnonymousHistoryAndChatWorksWithMemberPrincipal() throws Exception {
        var member = new sum25.se180455.metro.pojo.Member();
        member.setName("Chat Test"); member.setEmail(UUID.randomUUID() + "@example.invalid");
        member.setPassword(passwords.encode("ChatTestPassword123")); member.setRole("MEMBER"); member.setStatus("ACTIVE");
        members.saveAndFlush(member);
        var session = new MockHttpSession();
        var anonymous = sessions.state(session, null);
        anonymous.append("Anonymous context", "Reply");
        mvc.perform(post("/login").session(session).with(csrf()).with(request -> {
            request.setRemoteAddr(UUID.randomUUID().toString()); return request;
        }).param("email", member.getEmail()).param("password", "ChatTestPassword123"))
                .andExpect(redirectedUrl("/"));
        String html = mvc.perform(get("/").session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Chat Test");
        assertThat(attribute(html, "data-session-key")).isNotEqualTo(anonymous.key());
        mvc.perform(message(session, "Sau đăng nhập")).andExpect(status().isOk());
        verify(gemini).reply("Sau đăng nhập", List.of());
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout"));
        assertThat(session.isInvalid()).isTrue();
    }
    @Test void validatesBlankOversizeNullAndMalformedMessages() throws Exception {
        for (String text : List.of("", "  ", "\u2003", "a".repeat(2001))) {
            mvc.perform(message(new MockHttpSession(), text)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
        }
        for (String body : List.of("{}", "{\"message\":null}", "not json")) {
            mvc.perform(post("/api/chat/message").with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(gemini);
    }
    @Test void isolatesHistoryAndResetsServerContext() throws Exception {
        var first = new MockHttpSession(); var second = new MockHttpSession();
        mvc.perform(message(first, "Tin một")).andExpect(status().isOk());
        mvc.perform(message(first, "Tin hai")).andExpect(status().isOk());
        verify(gemini).reply(eq("Tin hai"), argThat(history -> history.size() == 2 && history.get(0).text().equals("Tin một")));
        mvc.perform(message(second, "Phiên khác")).andExpect(status().isOk());
        verify(gemini).reply("Phiên khác", List.of());
        mvc.perform(post("/api/chat/reset").session(first).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(message(first, "Mới")).andExpect(status().isOk());
        verify(gemini).reply("Mới", List.of());
    }
    @Test void discardsHistoryAcrossIdentityChangesAndBoundsContext() {
        var session = new MockHttpSession();
        var anonymous = sessions.state(session, null);
        for (int i = 0; i < 20; i++) anonymous.append("question " + i, "answer " + i);
        assertThat(anonymous.history()).hasSize(12);
        assertThat(anonymous.history().get(0).text()).isEqualTo("question 14");
        var auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated("alice", null, List.of());
        var alice = sessions.state(session, auth);
        assertThat(alice.history()).isEmpty(); assertThat(alice.key()).isNotEqualTo(anonymous.key());
        alice.append("Private", "Reply");
        assertThat(sessions.state(session, null).history()).isEmpty();
    }
    @Test void limitsSpamAndDoesNotTrustForwardingHeaders() throws Exception {
        var session = new MockHttpSession();
        for (int i = 0; i < 10; i++) mvc.perform(message(session, "Chào")).andExpect(status().isOk());
        mvc.perform(message(session, "Spam").header("X-Forwarded-For", UUID.randomUUID().toString()))
                .andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After", "60"));
        verify(gemini, times(10)).reply(anyString(), anyList());
    }
    @Test void hidesUnexpectedBackendErrorsAndDoesNotRetainFailedTurns() throws Exception {
        when(gemini.reply(anyString(), anyList())).thenThrow(new IllegalStateException("secret password api key"));
        var session = new MockHttpSession();
        mvc.perform(message(session, "Chào")).andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));
        assertThat(sessions.state(session, null).history()).isEmpty();
    }
    @Test void rendersOneWidgetAndStaticAssetsOnHomeLoginAndAuthenticatedHome() throws Exception {
        for (String url : List.of("/", "/login", "/register", "/ticket", "/train-stations")) {
            String html = mvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(html.split("id=\"metro-ai\"", -1)).hasSize(2);
            assertThat(html).contains("data-csrf-token=", "Metro AI Assistant").doesNotContain("test-only-secret");
        }
        mvc.perform(get("/").with(user("member@example.com"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Metro AI Assistant")));
        mvc.perform(get("/chat/chatbox.css")).andExpect(status().isOk());
        mvc.perform(get("/chat/chatbox.js")).andExpect(status().isOk());
    }
    private String attribute(String html, String name) {
        var matcher = java.util.regex.Pattern.compile(name + "=\"([^\"]+)\"").matcher(html);
        assertThat(matcher.find()).as(name).isTrue(); return matcher.group(1);
    }
}
