package sum25.se180455.metro.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import sum25.se180455.metro.pojo.Member;
import java.util.*;

/** Session identity only; never retain a Member entity or password hash in the principal. */
public class MemberPrincipal implements UserDetails, OidcUser {
    private final Long memberId;
    private final String role;
    private final OidcUser oidc;
    public MemberPrincipal(Member member) { this(member, null); }
    public MemberPrincipal(Member member, OidcUser oidc) {
        this.memberId = member.getMemberId();
        this.role = "ADMIN".equalsIgnoreCase(member.getRole()) ? "ADMIN" : "MEMBER";
        this.oidc = oidc;
    }
    public Long getMemberId() { return memberId; }
    public MemberPrincipal refresh(Member member) { return new MemberPrincipal(member, oidc); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return memberId.toString(); }
    @Override public String getName() { return getUsername(); }
    @Override public Map<String, Object> getAttributes() { return getClaims(); }
    @Override public Map<String, Object> getClaims() { return oidc == null ? Map.of() : oidc.getClaims(); }
    @Override public OidcIdToken getIdToken() { return oidc == null ? null : oidc.getIdToken(); }
    @Override public OidcUserInfo getUserInfo() { return oidc == null ? null : oidc.getUserInfo(); }
}
