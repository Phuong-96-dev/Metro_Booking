package sum25.se180455.metro.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.StudentVerification;
import sum25.se180455.metro.repository.MemberRepository;
import sum25.se180455.metro.repository.StudentVerificationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudentVerificationService {
    public static final String DRAFT = "DRAFT";
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private final StudentVerificationRepository verificationRepository;
    private final MemberRepository memberRepository;
    private final StudentCardOcrService ocrService;

    public StudentVerificationService(
            StudentVerificationRepository verificationRepository,
            MemberRepository memberRepository,
            StudentCardOcrService ocrService) {
        this.verificationRepository = verificationRepository;
        this.memberRepository = memberRepository;
        this.ocrService = ocrService;
    }

    @Transactional
    public StudentVerification createOcrDraft(Long memberId, MultipartFile image) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản."));
        if (verificationRepository.existsByMember_MemberIdAndStatus(memberId, PENDING)) {
            throw new IllegalStateException("Bạn đã có yêu cầu xác minh đang chờ xử lý.");
        }
        if (verificationRepository.existsByMember_MemberIdAndStatus(memberId, DRAFT)) {
            throw new IllegalStateException("Bạn đang có bản nháp OCR. Hãy tiếp tục xác nhận bản nháp hiện tại.");
        }
        if (hasCurrentApproval(memberId)) {
            throw new IllegalStateException("Tư cách sinh viên của bạn hiện đã được xác minh.");
        }
        var result = ocrService.process(image);
        try {
            StudentVerification verification = new StudentVerification();
            verification.setMember(member);
            verification.setStatus(DRAFT);
            verification.setImagePath(result.imagePath());
            verification.setImageContentType(result.imageContentType());
            verification.setOcrText(result.text());
            verification.setOcrFullName(result.fullName());
            verification.setOcrStudentCode(result.studentCode());
            verification.setOcrSchoolName(result.schoolName());
            verification.setOcrDateOfBirth(result.dateOfBirth());
            verification.setOcrIssuedAt(result.issuedAt());
            verification.setOcrExpiresAt(result.expiresAt());
            verification.setFullName(orEmpty(result.fullName()));
            verification.setStudentCode(orEmpty(result.studentCode()));
            verification.setSchoolName(orEmpty(result.schoolName()));
            verification.setDateOfBirth(result.dateOfBirth());
            verification.setIssuedAt(result.issuedAt());
            verification.setExpiresAt(result.expiresAt());
            verification.setCreatedAt(LocalDateTime.now());
            return verificationRepository.saveAndFlush(verification);
        } catch (RuntimeException exception) {
            ocrService.deleteImage(result.imagePath());
            throw exception;
        }
    }

    @Transactional
    public StudentVerification submit(
            Long memberId,
            Long verificationId,
            String fullName,
            String studentCode,
            String schoolName,
            LocalDate dateOfBirth,
            LocalDate issuedAt,
            LocalDate expiresAt) {
        memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản."));
        StudentVerification verification = findOwned(memberId, verificationId);
        if (!DRAFT.equals(verification.getStatus())) {
            throw new IllegalStateException("Hồ sơ này không còn ở trạng thái có thể gửi.");
        }
        if (verificationRepository.existsByMember_MemberIdAndStatus(memberId, PENDING)) {
            throw new IllegalStateException("Bạn đã có yêu cầu xác minh đang chờ xử lý.");
        }

        String normalizedCode = required(studentCode, "Mã số sinh viên", 100);
        if (verificationRepository.existsByStudentCodeIgnoreCaseAndStatusInAndMember_MemberIdNot(
                normalizedCode, List.of(PENDING, APPROVED), memberId)) {
            throw new IllegalArgumentException("Mã sinh viên này đã được sử dụng trong một hồ sơ khác.");
        }
        if (expiresAt != null && expiresAt.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Thẻ sinh viên đã hết hạn.");
        }
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ.");
        }
        if (issuedAt != null && issuedAt.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày cấp thẻ không hợp lệ.");
        }
        if (issuedAt != null && expiresAt != null && expiresAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("Ngày hết hạn phải sau ngày cấp thẻ.");
        }

        verification.setFullName(required(fullName, "Họ và tên", 150));
        verification.setStudentCode(normalizedCode);
        verification.setSchoolName(required(schoolName, "Tên trường", 200));
        verification.setDateOfBirth(dateOfBirth);
        verification.setIssuedAt(issuedAt);
        verification.setExpiresAt(expiresAt);
        verification.setStatus(PENDING);
        verification.setSubmittedAt(LocalDateTime.now());
        verification.setRejectionReason(null);
        return verificationRepository.save(verification);
    }

    @Transactional(readOnly = true)
    public Optional<StudentVerification> latestForMember(Long memberId) {
        return verificationRepository.findTopByMember_MemberIdOrderByCreatedAtDesc(memberId);
    }

    @Transactional(readOnly = true)
    public boolean hasCurrentApproval(Long memberId) {
        return latestForMember(memberId)
                .filter(verification -> APPROVED.equals(verification.getStatus()))
                .filter(verification -> verification.getExpiresAt() == null
                        || !verification.getExpiresAt().isBefore(LocalDate.now()))
                .isPresent();
    }

    @Transactional(readOnly = true)
    public StudentVerification findOwned(Long memberId, Long verificationId) {
        StudentVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ."));
        if (!verification.getMember().getMemberId().equals(memberId)) {
            throw new EntityNotFoundException("Không tìm thấy hồ sơ.");
        }
        return verification;
    }

    @Transactional(readOnly = true)
    public StudentVerification findForAdmin(Long verificationId) {
        return verificationRepository.findById(verificationId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ."));
    }

    @Transactional(readOnly = true)
    public List<StudentVerification> search(String status, String term) {
        String query = term == null || term.isBlank() ? null : term.trim();
        return verificationRepository.search(status, query);
    }

    @Transactional
    public StudentVerification approve(Long verificationId, Long adminId) {
        StudentVerification verification = findForUpdate(verificationId);
        requirePending(verification);
        if (verification.getExpiresAt() != null
                && verification.getExpiresAt().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể duyệt thẻ đã hết hạn.");
        }
        if (verificationRepository.existsByStudentCodeIgnoreCaseAndStatusInAndMember_MemberIdNot(
                verification.getStudentCode(), List.of(APPROVED), verification.getMember().getMemberId())) {
            throw new IllegalArgumentException("Mã sinh viên đã thuộc về một tài khoản khác.");
        }

        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản ADMIN."));
        verification.setStatus(APPROVED);
        verification.setReviewedBy(admin);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setRejectionReason(null);

        Member member = verification.getMember();
        member.setStudentCard(verification.getStudentCode());
        member.setSchoolName(verification.getSchoolName());
        memberRepository.save(member);
        return verificationRepository.save(verification);
    }

    @Transactional
    public StudentVerification reject(Long verificationId, Long adminId, String reason) {
        StudentVerification verification = findForUpdate(verificationId);
        requirePending(verification);
        String rejectionReason = required(reason, "Lý do từ chối");
        if (rejectionReason.length() > 1000) {
            throw new IllegalArgumentException("Lý do từ chối không được vượt quá 1000 ký tự.");
        }
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản ADMIN."));
        verification.setStatus(REJECTED);
        verification.setReviewedBy(admin);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setRejectionReason(rejectionReason);
        return verificationRepository.save(verification);
    }

    private void requirePending(StudentVerification verification) {
        if (!PENDING.equals(verification.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xử lý hồ sơ đang chờ duyệt.");
        }
    }

    private StudentVerification findForUpdate(Long verificationId) {
        return verificationRepository.findByIdForUpdate(verificationId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ."));
    }

    private String required(String value, String fieldName) {
        return required(value, fieldName, 1000);
    }

    private String required(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " là bắt buộc.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " không được vượt quá " + maxLength + " ký tự.");
        }
        return trimmed;
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
