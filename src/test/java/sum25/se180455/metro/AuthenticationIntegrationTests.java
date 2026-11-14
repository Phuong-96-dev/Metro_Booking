package sum25.se180455.metro;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import sum25.se180455.metro.main.MetroApplication;
import sum25.se180455.metro.pojo.*;
import sum25.se180455.metro.repository.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MetroApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired PasswordEncoder encoder;
    @Autowired TicketRepository tickets;
    @Autowired TicketTypeRepository ticketTypes;
    @Autowired PaymentRepository payments;
    @Autowired FeedbackRepository feedbacks;
    private final String password = "MetroSecure12345";
    private String ip() { return UUID.randomUUID().toString(); }
    private org.springframework.test.web.servlet.request.RequestPostProcessor address(String address) {
        return request -> { request.setRemoteAddr(address); return request; };
    }
    private MockHttpServletRequestBuilder signedWebhook(String body, long timestamp) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("test-sepay-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
        String signature = "sha256=" + HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        return post("/api/webhooks/sepay")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-SePay-Timestamp", Long.toString(timestamp))
                .header("X-SePay-Signature", signature)
                .content(body);
    }
    private String webhook(long id, String account, String code, long amount) {
        return """
                {"id":%d,"gateway":"Vietcombank","transactionDate":"2026-11-14 12:00:00",
                 "accountNumber":"%s","subAccount":"","code":"%s",
                 "content":"%s thanh toan ve","transferType":"in","description":"test",
                 "transferAmount":%d,"accumulated":1000000,"referenceCode":"FTTEST%d"}
                """.formatted(id, account, code, code, amount, id);
    }
    private Member member(String role) {
        Member member = new Member(); member.setName("Nguyễn An");
        member.setEmail(UUID.randomUUID() + "@example.com"); member.setPassword(encoder.encode(password));
        member.setRole(role); member.setStatus("ACTIVE"); return members.saveAndFlush(member);
    }
    private MockHttpSession login(Member member) throws Exception {
        return (MockHttpSession) mvc.perform(post("/login").with(csrf()).with(address(ip()))
                .param("email", member.getEmail()).param("password", password))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession(false);
    }
    @Test void registrationHashesPasswordAndIgnoresPrivilegeFields() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        mvc.perform(post("/register").with(csrf()).with(address(ip())).param("name", " \u2003Nguyễn An\u2003 ")
                .param("email", " " + email.toUpperCase() + " ").param("password", password).param("confirmPassword", password)
                .param("role", "ADMIN").param("status", "BLOCKED").param("memberId", "1"))
                .andExpect(redirectedUrl("/login?registered"));
        Member saved = members.findByEmail(email);
        assertThat(saved.getName()).isEqualTo("Nguyễn An");
        assertThat(saved.getRole()).isEqualTo("MEMBER"); assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getPassword()).isNotEqualTo(password); assertThat(encoder.matches(password, saved.getPassword())).isTrue();
    }
    @Test void duplicateEmailAndInvalidRegistrationAreRejectedWithoutRenderingPasswords() throws Exception {
        Member existing = member("MEMBER");
        long count = members.count();
        mvc.perform(post("/register").with(csrf()).with(address(ip())).param("name", "An")
                .param("email", " " + existing.getEmail().toUpperCase() + " ").param("password", password).param("confirmPassword", password))
                .andExpect(view().name("register")).andExpect(model().attributeHasFieldErrors("newMember", "email"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(password))));
        assertThat(members.count()).isEqualTo(count);
        Member unchanged = members.findById(existing.getMemberId()).orElseThrow();
        assertThat(unchanged.getName()).isEqualTo(existing.getName());
        assertThat(unchanged.getPassword()).isEqualTo(existing.getPassword());
        mvc.perform(post("/register").with(csrf()).with(address(ip())).param("name", " ").param("email", "invalid")
                .param("password", "weak").param("confirmPassword", "different"))
                .andExpect(view().name("register")).andExpect(model().hasErrors());
        assertThat(members.count()).isEqualTo(count);
    }
    static Stream<Arguments> invalidRegistrations() {
        return Stream.of(
                Arguments.of("name", null, "name"),
                Arguments.of("name", "", "name"),
                Arguments.of("name", " \t ", "name"),
                Arguments.of("name", "\u2003", "name"),
                Arguments.of("name", "A".repeat(101), "name"),
                Arguments.of("email", null, "email"),
                Arguments.of("email", "", "email"),
                Arguments.of("email", "   ", "email"),
                Arguments.of("email", "invalid", "email"),
                Arguments.of("email", "a".repeat(243) + "@example.com", "email"),
                Arguments.of("password", null, "password"),
                Arguments.of("password", "", "password"),
                Arguments.of("password", "weak", "password"),
                Arguments.of("password", "A1" + "a".repeat(71), "password"),
                Arguments.of("password", "lettersletters", "passwordPolicyValid"),
                Arguments.of("password", "123456789012", "passwordPolicyValid"),
                Arguments.of("password", "é".repeat(36) + "1", "passwordPolicyValid"),
                Arguments.of("confirmPassword", null, "confirmPassword"),
                Arguments.of("confirmPassword", "", "confirmPassword"),
                Arguments.of("confirmPassword", "different", "passwordMatching"),
                Arguments.of("phone", "invalid", "phone"),
                Arguments.of("phone", "1".repeat(21), "phone"),
                Arguments.of("studentCard", "S".repeat(101), "studentCard"),
                Arguments.of("schoolName", "S".repeat(201), "schoolName"));
    }
    @ParameterizedTest(name = "[{index}] invalid {0}: error on {2}")
    @MethodSource("invalidRegistrations")
    void invalidRegistrationDoesNotRedirectOrPersist(String field, String value, String errorField) throws Exception {
        long count = members.count();
        String email = UUID.randomUUID() + "@example.com";
        var params = new LinkedMultiValueMap<String, String>();
        params.add("name", "Nguyễn An"); params.add("email", email);
        params.add("password", password); params.add("confirmPassword", password);
        if (value == null) params.remove(field);
        else params.set(field, value);
        if ("password".equals(field) && value != null) params.set("confirmPassword", value);
        mvc.perform(post("/register").with(csrf()).with(address(ip())).params(params))
                .andExpect(status().isOk()).andExpect(view().name("register"))
                .andExpect(header().doesNotExist("Location"))
                .andExpect(model().attributeHasFieldErrors("newMember", errorField))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("value=\"" + password + "\""))));
        assertThat(members.count()).isEqualTo(count);
        assertThat(members.findByEmail(email)).isNull();
    }
    @Test void successfulLoginRotatesSessionAndLogoutInvalidatesIt() throws Exception {
        Member member = member("MEMBER");
        MockHttpSession old = new MockHttpSession(); String oldId = old.getId();
        MvcResult result = mvc.perform(post("/login").session(old).with(csrf()).with(address(ip()))
                .param("email", member.getEmail()).param("password", password)).andExpect(redirectedUrl("/")).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session.getId()).isNotEqualTo(oldId);
        mvc.perform(get("/profile").session(session)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Nguyễn An")));
        assertThat(((Member) session.getAttribute("member")).getPassword()).isNull();
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout"));
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/profile")).andExpect(redirectedUrl("http://localhost/login"));
    }
    @Test void wrongUnknownDisabledAndLegacyPasswordsHaveSameLoginError() throws Exception {
        Member member = member("MEMBER");
        for (String email : new String[]{member.getEmail(), UUID.randomUUID() + "@example.com"}) {
            mvc.perform(post("/login").with(csrf()).with(address(ip())).param("email", email).param("password", "wrong"))
                    .andExpect(redirectedUrl("/login?error"));
        }
        member.setStatus("INACTIVE"); members.saveAndFlush(member);
        mvc.perform(post("/login").with(csrf()).with(address(ip())).param("email", member.getEmail()).param("password", password))
                .andExpect(redirectedUrl("/login?error"));
        member.setStatus("ACTIVE"); member.setPassword(password); members.saveAndFlush(member);
        mvc.perform(post("/login").with(csrf()).with(address(ip())).param("email", member.getEmail()).param("password", password))
                .andExpect(redirectedUrl("/login?error"));
    }
    @Test void protectedRoutesRequireLoginAndMemberCannotManageSystem() throws Exception {
        mvc.perform(get("/myTicket")).andExpect(redirectedUrl("http://localhost/login"));
        MockHttpSession session = login(member("MEMBER"));
        mvc.perform(get("/admindashboard").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/adminManage/updateMemberRole").session(session).with(csrf()).param("memberId", "1").param("role", "ADMIN"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/train-schedules").session(session).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/bus-routes/1/destinations/create").session(session).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get("/train-schedules/add").session(session)).andExpect(status().isForbidden());
    }
    @Test void adminCanManageAndRoleOrStatusChangesApplyToExistingSessions() throws Exception {
        Member admin = member("ADMIN"); MockHttpSession session = login(admin);
        mvc.perform(get("/adminManage/manageBlog").session(session)).andExpect(status().isOk());
        admin.setRole("MEMBER"); members.saveAndFlush(admin);
        mvc.perform(get("/adminManage/manageBlog").session(session)).andExpect(status().isForbidden());
        admin.setStatus("INACTIVE"); members.saveAndFlush(admin);
        mvc.perform(get("/profile").session(session)).andExpect(redirectedUrl("/login?error"));
        assertThat(session.isInvalid()).isTrue();
    }
    @Test void csrfIsRequiredAndGetCannotLogoutOrDelete() throws Exception {
        mvc.perform(post("/login").param("email", "x@example.com").param("password", password)).andExpect(status().isForbidden());
        MockHttpSession session = login(member("ADMIN"));
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/adminManage/deleteBlog/1").session(session)).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/train-schedules/delete/1").session(session)).andExpect(status().isMethodNotAllowed());
    }
    @Test void profileCannotChangeAnotherMemberOrPromoteItself() throws Exception {
        Member owner = member("MEMBER"); Member victim = member("MEMBER");
        mvc.perform(post("/update-profile").session(login(owner)).with(csrf()).param("memberId", victim.getMemberId().toString())
                .param("name", "Tên mới").param("email", "attacker@example.com").param("role", "ADMIN").param("password", "changed"))
                .andExpect(status().is3xxRedirection());
        assertThat(members.findById(victim.getMemberId()).orElseThrow().getName()).isEqualTo("Nguyễn An");
        Member updated = members.findById(owner.getMemberId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Tên mới"); assertThat(updated.getRole()).isEqualTo("MEMBER");
        assertThat(updated.getEmail()).isEqualTo(owner.getEmail()); assertThat(encoder.matches(password, updated.getPassword())).isTrue();
    }
    @Test void memberCannotUseSomeoneElsesTicket() throws Exception {
        Member owner = member("MEMBER"); Ticket ticket = new Ticket(); ticket.setMember(owner); ticket.setStatus("Chưa sử dụng");
        ticket = tickets.saveAndFlush(ticket);
        mvc.perform(post("/ticket/use").session(login(member("MEMBER"))).with(csrf()).param("ticketId", ticket.getTicketId().toString()))
                .andExpect(status().isForbidden());
        assertThat(tickets.findById(ticket.getTicketId()).orElseThrow().getStatus()).isEqualTo("Chưa sử dụng");
    }
    @Test void publicPagesAndSearchStillWorkAndFormsHaveCsrf() throws Exception {
        for (String path : new String[]{"/", "/login", "/register", "/blog", "/ticket", "/trains", "/train-stations", "/bus-routes", "/train-schedules", "/search-schedules"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
        mvc.perform(get("/login")).andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
        mvc.perform(post("/bus-routes/search").with(csrf()).param("stationId", "1")).andExpect(status().isOk());
    }
    @Test void bruteForceIsRateLimitedEvenWhenForwardedHeaderChanges() throws Exception {
        String ip = ip();
        for (int i = 0; i < 30; i++) {
            mvc.perform(post("/register").with(csrf()).with(address(ip)).header("X-Forwarded-For", "192.0.2." + i))
                    .andExpect(status().isOk());
        }
        mvc.perform(post("/register").with(csrf()).with(address(ip))).andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After", "900"));
    }
    @Test void existingPurchasePaymentTicketAndFeedbackFlowStillWorks() throws Exception {
        Member member = member("MEMBER"); MockHttpSession session = login(member);
        TicketType type = new TicketType(); type.setName("Vé tháng"); type.setDurationDays(30); type.setPrice(300000.0);
        type = ticketTypes.saveAndFlush(type);
        mvc.perform(post("/ticket/buy").session(session).with(csrf()).param("ticketTypeId", type.getTicketTypeId().toString()))
                .andExpect(status().is3xxRedirection());
        Ticket ticket = tickets.findByMember_MemberId(member.getMemberId()).get(0);
        assertThat(ticket.getStatus()).isEqualTo("Chưa thanh toán");
        mvc.perform(get("/payment").session(session).param("ticketId", ticket.getTicketId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mã QR thanh toán SePay")));
        Payment pending = payments.findByTicket_TicketId(ticket.getTicketId()).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo("PENDING");
        assertThat(pending.getTransactionCode()).startsWith("METRO");
        mvc.perform(get("/payment/success").session(session).param("ticketId", ticket.getTicketId().toString()))
                .andExpect(redirectedUrl("/payment?ticketId=" + ticket.getTicketId()));

        String webhook = webhook(92704, "0123456789", pending.getTransactionCode(), 300000);
        mvc.perform(post("/api/webhooks/sepay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhook))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(post("/api/webhooks/sepay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-SePay-Timestamp", Long.toString(Instant.now().getEpochSecond()))
                        .header("X-SePay-Signature", "sha256=wrong")
                        .content(webhook))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));
        long expired = Instant.now().minusSeconds(301).getEpochSecond();
        mvc.perform(signedWebhook(webhook, expired))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));

        long now = Instant.now().getEpochSecond();
        mvc.perform(signedWebhook(webhook(92701, "0123456789", "METROUNKNOWN1", 300000), now))
                .andExpect(status().isCreated());
        mvc.perform(signedWebhook(webhook(92702, "9999999999", pending.getTransactionCode(), 300000), now))
                .andExpect(status().isCreated());
        mvc.perform(signedWebhook(webhook(92703, "0123456789", pending.getTransactionCode(), 299999), now))
                .andExpect(status().isCreated());
        assertThat(payments.findById(pending.getPaymentId()).orElseThrow().getStatus()).isEqualTo("PENDING");

        mvc.perform(signedWebhook(webhook, now))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.success").value(true));
        mvc.perform(signedWebhook(webhook, now))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        Payment paid = payments.findById(pending.getPaymentId()).orElseThrow();
        assertThat(paid.getStatus()).isEqualTo("PAID");
        assertThat(paid.getSepayTransactionId()).isEqualTo(92704L);
        assertThat(tickets.findById(ticket.getTicketId()).orElseThrow().getStatus()).isEqualTo("Chưa sử dụng");
        mvc.perform(get("/api/payments/{ticketId}/status", ticket.getTicketId()).session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.paid").value(true));
        mvc.perform(get("/myTicket").session(session)).andExpect(status().isOk());
        mvc.perform(get("/payment/success").session(session).param("ticketId", ticket.getTicketId().toString())).andExpect(status().isOk());
        mvc.perform(post("/ticket/use").session(session).with(csrf()).param("ticketId", ticket.getTicketId().toString())).andExpect(redirectedUrl("/feedback"));
        assertThat(tickets.findById(ticket.getTicketId()).orElseThrow().getStatus()).isEqualTo("Đang sử dụng");
        mvc.perform(post("/feedback/send").session(session).with(csrf()).param("title", "Chuyến đi tốt").param("content", "Dịch vụ tốt").param("rating", "5"))
                .andExpect(redirectedUrl("/myTicket?success"));
        assertThat(feedbacks.findAll()).anyMatch(feedback -> feedback.getMember().getMemberId().equals(member.getMemberId()));
    }
}
