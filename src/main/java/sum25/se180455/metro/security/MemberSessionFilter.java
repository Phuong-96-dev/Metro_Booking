package sum25.se180455.metro.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.MemberRepository;
import java.io.IOException;

/** Bridges the existing session.member UI and controllers to the verified security identity. */
public class MemberSessionFilter extends OncePerRequestFilter {
    private final MemberRepository members;
    public MemberSessionFilter(MemberRepository members) { this.members = members; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                               FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MemberPrincipal principal) {
            Member member = members.findById(principal.getMemberId()).orElse(null);
            if (member == null || !"ACTIVE".equalsIgnoreCase(member.getStatus())) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                response.sendRedirect(request.getContextPath() + "/login?error");
                return;
            }
            MemberPrincipal refreshed = principal.refresh(member);
            Authentication updated = auth instanceof OAuth2AuthenticationToken oauth
                    ? new OAuth2AuthenticationToken(refreshed, refreshed.getAuthorities(), oauth.getAuthorizedClientRegistrationId())
                    : UsernamePasswordAuthenticationToken.authenticated(refreshed, null, refreshed.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(updated);
            Member view = new Member();
            view.setMemberId(member.getMemberId()); view.setName(member.getName()); view.setEmail(member.getEmail());
            view.setRole(member.getRole()); view.setStatus(member.getStatus()); view.setPhone(member.getPhone());
            view.setStudentCard(member.getStudentCard()); view.setSchoolName(member.getSchoolName());
            request.getSession().setAttribute("member", view);
        } else if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute("member");
        }
        chain.doFilter(request, response);
    }
}
