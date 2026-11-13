package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sum25.se180455.metro.request.RegistrationRequest;
import sum25.se180455.metro.security.*;
import sum25.se180455.metro.service.MemberService;
import sum25.se180455.metro.repository.MemberRepository;

@Controller
public class AuthController {
    private final MemberService members;
    private final MemberRepository repository;
    private final MemberAuthenticationProvider provider;
    private final boolean googleEnabled;
    public AuthController(MemberService members, MemberRepository repository, MemberAuthenticationProvider provider,
                          ObjectProvider<ClientRegistrationRepository> registrations) {
        this.members = members; this.repository = repository; this.provider = provider;
        this.googleEnabled = registrations.getIfAvailable() != null;
    }
    @GetMapping("/login") public String login(Model model) {
        model.addAttribute("googleEnabled", googleEnabled);
        return "login";
    }
    @GetMapping("/register") public String register(Model model) {
        model.addAttribute("newMember", new RegistrationRequest());
        return "register";
    }
    @PostMapping("/register") public String register(@Valid @ModelAttribute("newMember") RegistrationRequest form,
            BindingResult result) {
        if (result.hasErrors()) return "register";
        try { members.register(form.toMember()); }
        catch (IllegalArgumentException | DataIntegrityViolationException exception) {
            result.rejectValue("email", "duplicate", "Email đã được đăng ký.");
            return "register";
        }
        return "redirect:/login?registered";
    }
    @PostMapping("/account/google/link") public String linkGoogle(@RequestParam String password, Authentication authentication,
            HttpSession session, RedirectAttributes redirect) {
        if (!googleEnabled) {
            redirect.addFlashAttribute("authError", "Đăng nhập Google chưa được cấu hình.");
            return "redirect:/profile";
        }
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        var member = repository.findById(principal.getMemberId()).orElseThrow();
        try {
            provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(member.getEmail(), password));
        } catch (AuthenticationException exception) {
            redirect.addFlashAttribute("authError", "Không thể xác nhận tài khoản. Vui lòng kiểm tra mật khẩu.");
            return "redirect:/profile";
        }
        session.setAttribute(GoogleOidcUserService.LINK_MEMBER, principal.getMemberId());
        session.setAttribute(GoogleOidcUserService.LINK_EXPIRES, System.currentTimeMillis() + 5 * 60 * 1000L);
        return "redirect:/oauth2/authorization/google-link";
    }
}
