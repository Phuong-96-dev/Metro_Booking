package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sum25.se180455.metro.pojo.OAuthAccount;
import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
    boolean existsByMember_MemberIdAndProvider(Long memberId, String provider);
}
