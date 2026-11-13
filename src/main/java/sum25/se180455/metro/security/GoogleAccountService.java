package sum25.se180455.metro.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sum25.se180455.metro.pojo.*;
import sum25.se180455.metro.repository.*;
import java.util.Locale;
import java.util.UUID;

@Service
public class GoogleAccountService {
    private final MemberRepository members;
    private final OAuthAccountRepository accounts;
    private final PasswordEncoder encoder;
    public GoogleAccountService(MemberRepository members, OAuthAccountRepository accounts, PasswordEncoder encoder) {
        this.members = members; this.accounts = accounts; this.encoder = encoder;
    }
    @Transactional
    public MemberPrincipal resolve(OidcUser verifiedUser, Long linkMemberId) {
        // Called only after Spring's OIDC decoder has verified signature, issuer, audience, expiry and nonce.
        String subject = verifiedUser.getSubject();
        if (subject == null || subject.isBlank() || subject.length() > 255 || !Boolean.TRUE.equals(verifiedUser.getEmailVerified())) {
            throw error("unverified_email");
        }
        OAuthAccount account = accounts.findByProviderAndProviderUserId("google", subject).orElse(null);
        if (account != null) {
            if (linkMemberId != null && !account.getMember().getMemberId().equals(linkMemberId)) throw error("link_conflict");
            return active(account.getMember(), verifiedUser);
        }
        Member member;
        if (linkMemberId != null) {
            member = members.findById(linkMemberId).orElseThrow(() -> error("link_required"));
            if (accounts.existsByMember_MemberIdAndProvider(linkMemberId, "google")) throw error("link_conflict");
        } else {
            String email = verifiedUser.getEmail();
            if (email == null || email.length() > 254) throw error("unverified_email");
            email = email.strip().toLowerCase(Locale.ROOT);
            if (members.findByEmail(email) != null) throw error("link_required");
            member = new Member();
            member.setEmail(email);
            String name = verifiedUser.getFullName();
            member.setName(name == null || name.isBlank() ? "Thành viên Metro" : name.substring(0, Math.min(name.length(), 100)));
            // Unknowable random password supports existing non-null password columns.
            member.setPassword(encoder.encode(UUID.randomUUID().toString()));
            member.setRole("MEMBER"); member.setStatus("ACTIVE");
            member = members.saveAndFlush(member);
        }
        MemberPrincipal principal = active(member, verifiedUser);
        accounts.saveAndFlush(new OAuthAccount(member, subject));
        return principal;
    }
    private MemberPrincipal active(Member member, OidcUser user) {
        if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) throw error("account_unavailable");
        return new MemberPrincipal(member, user);
    }
    private OAuth2AuthenticationException error(String code) { return new OAuth2AuthenticationException(new OAuth2Error(code)); }
}
