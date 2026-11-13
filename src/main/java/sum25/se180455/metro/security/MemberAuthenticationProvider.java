package sum25.se180455.metro.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class MemberAuthenticationProvider implements AuthenticationProvider {
    private final MemberRepository members;
    private final PasswordEncoder encoder;
    private final boolean allowLegacy;
    private final String dummyHash;
    public MemberAuthenticationProvider(MemberRepository members, PasswordEncoder encoder,
            @Value("${app.auth.allow-legacy-passwords:false}") boolean allowLegacy) {
        this.members = members; this.encoder = encoder; this.allowLegacy = allowLegacy;
        this.dummyHash = encoder.encode("dummy-password-for-timing-only");
    }
    @Override @Transactional
    public Authentication authenticate(Authentication authentication) {
        String password = String.valueOf(authentication.getCredentials());
        if (password.getBytes(StandardCharsets.UTF_8).length > 72 || authentication.getName().length() > 254) {
            throw new BadCredentialsException("Invalid credentials");
        }
        Member member = members.findByEmail(authentication.getName());
        String stored = member == null ? null : member.getPassword();
        boolean bcrypt = stored != null && stored.matches("\\$2[aby]\\$\\d{2}\\$.{53}");
        boolean matches = encoder.matches(password, bcrypt ? stored : dummyHash);
        if (!bcrypt && allowLegacy && stored != null && !stored.startsWith("$")) {
            matches = MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8), stored.getBytes(StandardCharsets.UTF_8));
        }
        if (!matches || member == null || !"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!bcrypt || encoder.upgradeEncoding(stored)) {
            member.setPassword(encoder.encode(password));
            members.saveAndFlush(member);
        }
        MemberPrincipal principal = new MemberPrincipal(member);
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }
    @Override public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
