package sum25.se180455.metro.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.StudentVerification;
import sum25.se180455.metro.repository.MemberRepository;
import sum25.se180455.metro.repository.StudentVerificationRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentVerificationServiceTest {
    private final StudentVerificationRepository verificationRepository = mock(StudentVerificationRepository.class);
    private final Map<Long, Member> lockedMembers = new HashMap<>();
    private final MemberRepository memberRepository = mock(MemberRepository.class, invocation -> {
        if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
            Long memberId = invocation.getArgument(0, Long.class);
            return Optional.ofNullable(lockedMembers.get(memberId));
        }
        return RETURNS_DEFAULTS.answer(invocation);
    });
    private final StudentCardOcrService ocrService = mock(StudentCardOcrService.class);
    private final StudentVerificationService service =
            new StudentVerificationService(verificationRepository, memberRepository, ocrService);

    @Test
    void submitSavesConfirmedValuesAsPending() {
        Member member = member(5L);
        StudentVerification draft = draft(10L, member);
        lockedMembers.put(5L, member);
        when(verificationRepository.findById(10L)).thenReturn(Optional.of(draft));
        when(verificationRepository.existsByMember_MemberIdAndStatus(5L, StudentVerificationService.PENDING))
                .thenReturn(false);
        when(verificationRepository.existsByStudentCodeIgnoreCaseAndStatusInAndMember_MemberIdNot(
                "SE180455", List.of("PENDING", "APPROVED"), 5L)).thenReturn(false);
        when(verificationRepository.save(any(StudentVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.submit(5L, 10L, "Nguyễn Văn An", "SE180455", "Đại học FPT",
                LocalDate.of(2004, 5, 4), null, LocalDate.now().plusDays(1));

        assertEquals(StudentVerificationService.PENDING, draft.getStatus());
        assertEquals("Nguyễn Văn An", draft.getFullName());
        assertEquals("SE180455", draft.getStudentCode());
        assertEquals("Đại học FPT", draft.getSchoolName());
        verify(verificationRepository).save(draft);
    }

    @Test
    void submitRejectsASecondPendingRequest() {
        Member member = member(5L);
        lockedMembers.put(5L, member);
        when(verificationRepository.findById(10L)).thenReturn(Optional.of(draft(10L, member)));
        when(verificationRepository.existsByMember_MemberIdAndStatus(5L, StudentVerificationService.PENDING))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.submit(
                5L, 10L, "Nguyễn Văn An", "SE180455", "Đại học FPT", null, null, null));
        assertEquals("Bạn đã có yêu cầu xác minh đang chờ xử lý.", exception.getMessage());
        verify(verificationRepository, never()).save(any(StudentVerification.class));
    }

    @Test
    void memberCannotSubmitAnotherMembersDraft() {
        Member owner = member(5L);
        lockedMembers.put(7L, member(7L));
        when(verificationRepository.findById(10L)).thenReturn(Optional.of(draft(10L, owner)));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.submit(
                7L, 10L, "Name", "CODE", "School", null, null, null));
        assertEquals("Không tìm thấy hồ sơ.", exception.getMessage());
        verify(verificationRepository, never()).save(any(StudentVerification.class));
    }

    private Member member(Long id) {
        Member member = new Member();
        member.setMemberId(id);
        return member;
    }

    private StudentVerification draft(Long id, Member member) {
        StudentVerification draft = new StudentVerification();
        draft.setId(id);
        draft.setMember(member);
        draft.setStatus(StudentVerificationService.DRAFT);
        draft.setFullName("");
        draft.setStudentCode("");
        draft.setSchoolName("");
        return draft;
    }
}
