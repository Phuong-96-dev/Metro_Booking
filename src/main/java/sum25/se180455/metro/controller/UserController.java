package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.service.BlogService;
import sum25.se180455.metro.service.MemberService;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private BlogService blogService;

    // profile
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    // update profile
    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute("member") Member member) {
        Member existingMember = memberService.findById(member.getMemberId());
        if (existingMember == null) {
            // Xử lý lỗi, không tìm thấy member
            return "redirect:/profile?error=Member not found";
        }

        // Chỉ update các trường cho phép
        existingMember.setName(member.getName());
        existingMember.setEmail(member.getEmail());
        existingMember.setPhone(member.getPhone());
        existingMember.setStudentCard(member.getStudentCard());
        existingMember.setSchoolName(member.getSchoolName());

        memberService.updateMember(existingMember);

        return "redirect:/profile?message=Profile updated successfully";
    }
    @GetMapping("/admindashboard")
    public String adminDashboard(Model model) {
        int memberCount = memberService.countMembersByRoleAndStatus("MEMBER", "ACTIVE");
        model.addAttribute("memberCount", memberCount);
        int blogCount = blogService.countBlogs();
        model.addAttribute("blogCount", blogCount);
        return "admindashboard";
    }
    // Admin dashboard
    @GetMapping("/adminManage/managerMember")
    public String adminManageMember(Model model) {
        List<Member> members = memberService.findAll();
        model.addAttribute("members", members);
        return "adminManage/manageMember";
    }

    // Update member status
    @PostMapping("/adminManage/updateMemberStatus")
    public String updateMemberStatus(@ModelAttribute("member") Member member) {
        Member existingMember = memberService.findById(member.getMemberId());
        if (existingMember == null) {
            // Xử lý lỗi, không tìm thấy member
            return "redirect:/adminManage/managerMember?error=Member not found";
        }

        existingMember.setStatus(member.getStatus());
        memberService.updateStatus(existingMember.getMemberId(), existingMember.getStatus());

        return "redirect:/adminManage/managerMember?message=Member status updated successfully";
    }
    // Update member role
    @PostMapping("/adminManage/updateMemberRole")
    public String updateMemberRole(@ModelAttribute("member") Member member) {
        Member existingMember = memberService.findById(member.getMemberId());
        if (existingMember == null) {
            // Xử lý lỗi, không tìm thấy member
            return "redirect:/adminManage/managerMember?error=Member not found";
        }

        existingMember.setRole(member.getRole());
        memberService.updateRole(existingMember.getMemberId(), existingMember.getRole());

        return "redirect:/adminManage/managerMember?message=Member role updated successfully";
    }
}
