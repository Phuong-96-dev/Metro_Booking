package sum25.se180455.metro.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.StudentVerification;
import sum25.se180455.metro.service.StudentCardOcrService;
import sum25.se180455.metro.service.StudentVerificationService;
import sum25.se180455.metro.service.MemberService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

@Controller
public class StudentVerificationController {
    private static final Set<String> FILTER_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED");

    private final StudentVerificationService verificationService;
    private final StudentCardOcrService ocrService;
    private final MemberService memberService;

    public StudentVerificationController(
            StudentVerificationService verificationService,
            StudentCardOcrService ocrService,
            MemberService memberService) {
        this.verificationService = verificationService;
        this.ocrService = ocrService;
        this.memberService = memberService;
    }

    @GetMapping("/student-verification")
    public String status(HttpSession session, Model model) {
        Member member = sessionMember(session);
        if (member == null) {
            return "redirect:/login";
        }
        addLatest(member, model);
        return "member/studentVerification";
    }

    @PostMapping("/student-verification/ocr")
    public String processOcr(
            @RequestParam("cardImage") org.springframework.web.multipart.MultipartFile cardImage,
            HttpSession session,
            Model model) {
        Member member = sessionMember(session);
        if (member == null) {
            return "redirect:/login";
        }
        try {
            StudentVerification draft = verificationService.createOcrDraft(member.getMemberId(), cardImage);
            model.addAttribute("draft", draft);
            return "member/studentVerificationConfirm";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            addLatest(member, model);
            model.addAttribute("error", exception.getMessage());
            return "member/studentVerification";
        }
    }

    @GetMapping("/student-verification/{id}/confirm")
    public String resumeConfirmation(@PathVariable Long id, HttpSession session, Model model) {
        Member member = sessionMember(session);
        if (member == null) {
            return "redirect:/login";
        }
        try {
            StudentVerification draft = verificationService.findOwned(member.getMemberId(), id);
            if (!StudentVerificationService.DRAFT.equals(draft.getStatus())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            model.addAttribute("draft", draft);
            return "member/studentVerificationConfirm";
        } catch (EntityNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/student-verification/{id}/submit")
    public String submit(
            @PathVariable Long id,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issuedAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
            HttpSession session,
            Model model) {
        Member member = sessionMember(session);
        if (member == null) {
            return "redirect:/login";
        }
        try {
            verificationService.submit(member.getMemberId(), id, fullName, studentCode,
                    schoolName, dateOfBirth, issuedAt, expiresAt);
            return "redirect:/student-verification?submitted";
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException
                 | DataIntegrityViolationException exception) {
            String message = exception instanceof DataIntegrityViolationException
                    ? "Yêu cầu bị trùng với hồ sơ khác. Vui lòng kiểm tra mã sinh viên."
                    : exception.getMessage();
            model.addAttribute("error", message);
            try {
                model.addAttribute("draft", verificationService.findOwned(member.getMemberId(), id));
            } catch (EntityNotFoundException notFound) {
                addLatest(member, model);
                return "member/studentVerification";
            }
            return "member/studentVerificationConfirm";
        }
    }

    @GetMapping("/student-verification/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id, HttpSession session) throws IOException {
        Member member = sessionMember(session);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        StudentVerification verification;
        try {
            verification = verificationService.findOwned(member.getMemberId(), id);
        } catch (EntityNotFoundException exception) {
            if (!"ADMIN".equalsIgnoreCase(member.getRole())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            verification = verificationService.findForAdmin(id);
        }
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(verification.getImageContentType()))
                    .cacheControl(CacheControl.noStore())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(ocrService.readImage(verification.getImagePath()));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy ảnh hồ sơ.", exception);
        }
    }

    @GetMapping("/admin/student-verifications")
    public String adminList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            HttpSession session,
            Model model) {
        Member admin = requireAdmin(session);
        if (status != null && !status.isBlank()
                && !FILTER_STATUSES.contains(status.toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái lọc không hợp lệ.");
        }
        String normalizedStatus = status == null || status.isBlank() ? null : status.toUpperCase(Locale.ROOT);
        model.addAttribute("verifications", verificationService.search(normalizedStatus, q));
        model.addAttribute("selectedStatus", normalizedStatus);
        model.addAttribute("searchTerm", q);
        model.addAttribute("admin", admin);
        return "adminManage/studentVerifications";
    }

    @GetMapping("/admin/student-verifications/{id}")
    public String adminDetail(@PathVariable Long id, HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("verification", verificationService.findForAdmin(id));
        return "adminManage/studentVerificationDetail";
    }

    @PostMapping("/admin/student-verifications/{id}/approve")
    public String approve(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Member admin = requireAdmin(session);
        try {
            verificationService.approve(id, admin.getMemberId());
            redirectAttributes.addFlashAttribute("message", "Đã duyệt hồ sơ xác minh sinh viên.");
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/student-verifications/" + id;
    }

    @PostMapping("/admin/student-verifications/{id}/reject")
    public String reject(
            @PathVariable Long id,
            @RequestParam String rejectionReason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Member admin = requireAdmin(session);
        try {
            verificationService.reject(id, admin.getMemberId(), rejectionReason);
            redirectAttributes.addFlashAttribute("message", "Đã từ chối hồ sơ xác minh sinh viên.");
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/student-verifications/" + id;
    }

    private Member sessionMember(HttpSession session) {
        Member sessionMember = (Member) session.getAttribute("member");
        return sessionMember == null ? null : memberService.findById(sessionMember.getMemberId());
    }

    private Member requireAdmin(HttpSession session) {
        Member member = sessionMember(session);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!"ADMIN".equalsIgnoreCase(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return member;
    }

    private void addLatest(Member member, Model model) {
        StudentVerification latest = verificationService.latestForMember(member.getMemberId()).orElse(null);
        model.addAttribute("verification", latest);
        boolean canStart = latest == null
                || StudentVerificationService.REJECTED.equals(latest.getStatus());
        if (latest != null && StudentVerificationService.APPROVED.equals(latest.getStatus())) {
            canStart = latest.getExpiresAt() != null
                    && latest.getExpiresAt().isBefore(LocalDate.now());
        }
        if (latest != null && StudentVerificationService.DRAFT.equals(latest.getStatus())) {
            canStart = false;
        }
        model.addAttribute("canStartVerification", canStart);
    }
}
