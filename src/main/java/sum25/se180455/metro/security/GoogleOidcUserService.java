package sum25.se180455.metro.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.*;

@Service
public class GoogleOidcUserService extends OidcUserService {
    public static final String LINK_MEMBER = "googleLinkMember";
    public static final String LINK_EXPIRES = "googleLinkExpires";
    private final GoogleAccountService accounts;
    public GoogleOidcUserService(GoogleAccountService accounts) { this.accounts = accounts; }
    @Override public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser verified = super.loadUser(request);
        Long linkMemberId = null;
        if ("google-link".equals(request.getClientRegistration().getRegistrationId())) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attributes.getRequest().getSession(false);
            Object pending = session == null ? null : session.getAttribute(LINK_MEMBER);
            Object expiry = session == null ? null : session.getAttribute(LINK_EXPIRES);
            if (session != null) { session.removeAttribute(LINK_MEMBER); session.removeAttribute(LINK_EXPIRES); }
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(pending instanceof Long id) || !(expiry instanceof Long until) || until < System.currentTimeMillis()
                    || authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)
                    || !principal.getMemberId().equals(id)) {
                throw new OAuth2AuthenticationException(new OAuth2Error("link_required"));
            }
            linkMemberId = id;
        }
        try {
            return accounts.resolve(verified, linkMemberId);
        } catch (DataIntegrityViolationException exception) {
            // A concurrent signup/link must never bypass uniqueness or merge identities.
            throw new OAuth2AuthenticationException(new OAuth2Error("link_conflict"));
        }
    }
}
