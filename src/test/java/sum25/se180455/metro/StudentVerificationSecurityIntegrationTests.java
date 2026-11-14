package sum25.se180455.metro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sum25.se180455.metro.main.MetroApplication;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.StudentVerification;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.repository.MemberRepository;
import sum25.se180455.metro.repository.StudentVerificationRepository;
import sum25.se180455.metro.repository.TicketRepository;
import sum25.se180455.metro.repository.TicketTypeRepository;
import sum25.se180455.metro.request.StudentCardOcrResult;
import sum25.se180455.metro.service.StudentCardOcrService;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MetroApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentVerificationSecurityIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired StudentVerificationRepository verifications;
    @Autowired TicketTypeRepository ticketTypes;
    @Autowired TicketRepository tickets;
    @Autowired PasswordEncoder encoder;
    @MockitoBean StudentCardOcrService ocrService;

    private static final String PASSWORD = "MetroSecure12345";

    private Member member(String role) {
        Member member = new Member();
        member.setName("Test member");
        member.setEmail(UUID.randomUUID() + "@example.com");
        member.setPassword(encoder.encode(PASSWORD));
        member.setRole(role);
        member.setStatus("ACTIVE");
        return members.saveAndFlush(member);
    }

    private MockHttpSession login(Member member) throws Exception {
        return (MockHttpSession) mvc.perform(post("/login").with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(UUID.randomUUID().toString());
                            return request;
                        })
                        .param("email", member.getEmail())
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn().getRequest().getSession(false);
    }

    @Test
    void guestAndOtherMembersCannotAccessPrivateVerificationRoutes() throws Exception {
        mvc.perform(get("/student-verification"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/admin/student-verifications"))
                .andExpect(status().is3xxRedirection());

        MockHttpSession other = login(member("MEMBER"));
        mvc.perform(get("/admin/student-verifications").session(other))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/student-verifications/1/approve").session(other).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanSubmitAndAdminCanApproveBeforeTicketPurchase() throws Exception {
        Member owner = member("MEMBER");
        MockHttpSession memberSession = login(owner);
        MockHttpSession otherSession = login(member("MEMBER"));
        MockHttpSession adminSession = login(member("ADMIN"));

        TicketType type = new TicketType();
        type.setName("Student ticket");
        type.setPrice(100000.0);
        type.setDurationDays(30);
        type.setStudentTicket(true);
        ticketTypes.saveAndFlush(type);

        mvc.perform(get("/student-verification").session(memberSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Xác minh tư cách sinh viên")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/ticket/buy").session(memberSession).with(csrf())
                        .param("ticketTypeId", type.getTicketTypeId().toString()))
                .andExpect(redirectedUrl("/student-verification?required"));

        String code = "SV" + UUID.randomUUID().toString().replace("-", "");
        when(ocrService.process(any())).thenReturn(new StudentCardOcrResult(
                "test-card.jpg", "image/jpeg", "Student card", "Test member", code,
                "Test school", null, null, LocalDate.now().plusDays(30)));
        when(ocrService.readImage(anyString())).thenReturn(new byte[]{1, 2, 3});
        MockMultipartFile image = new MockMultipartFile("cardImage", "card.jpg", "image/jpeg", new byte[]{1});

        mvc.perform(multipart("/student-verification/ocr").file(image).session(memberSession))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/student-verification/ocr").file(image).session(memberSession).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/studentVerificationConfirm"))
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        StudentVerification draft = verifications.findTopByMember_MemberIdOrderByCreatedAtDesc(owner.getMemberId()).orElseThrow();
        assertThat(draft.getStatus()).isEqualTo("DRAFT");
        mvc.perform(get("/student-verification/{id}/image", draft.getId()).session(otherSession))
                .andExpect(status().isNotFound());
        mvc.perform(get("/student-verification/{id}/image", draft.getId()).session(memberSession))
                .andExpect(status().isOk());
        mvc.perform(get("/student-verification/{id}/confirm", draft.getId()).session(memberSession))
                .andExpect(status().isOk());

        mvc.perform(post("/student-verification/{id}/submit", draft.getId()).session(memberSession).with(csrf())
                        .param("fullName", "Test member")
                        .param("studentCode", code)
                        .param("schoolName", "Test school")
                        .param("expiresAt", LocalDate.now().plusDays(30).toString()))
                .andExpect(redirectedUrl("/student-verification?submitted"));
        assertThat(verifications.findById(draft.getId()).orElseThrow().getStatus()).isEqualTo("PENDING");
        mvc.perform(get("/admin/student-verifications").session(adminSession)).andExpect(status().isOk());
        mvc.perform(get("/admin/student-verifications/{id}", draft.getId()).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/student-verification/{id}/image", draft.getId()).session(adminSession))
                .andExpect(status().isOk());
        mvc.perform(post("/admin/student-verifications/{id}/approve", draft.getId()).session(adminSession))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/student-verifications/{id}/approve", draft.getId()).session(adminSession).with(csrf()))
                .andExpect(redirectedUrl("/admin/student-verifications/" + draft.getId()));
        assertThat(verifications.findById(draft.getId()).orElseThrow().getStatus()).isEqualTo("APPROVED");

        long before = tickets.count();
        mvc.perform(post("/ticket/buy").session(memberSession).with(csrf())
                        .param("ticketTypeId", type.getTicketTypeId().toString()))
                .andExpect(redirectedUrlPattern("/payment?ticketId=*"));
        assertThat(tickets.count()).isEqualTo(before + 1);
    }
}
