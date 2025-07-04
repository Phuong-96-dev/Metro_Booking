package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.service.MemberService;

@Controller
public class UserController {
    @Autowired
    private MemberService memberService;

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

}
