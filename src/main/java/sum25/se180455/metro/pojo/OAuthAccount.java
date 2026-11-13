package sum25.se180455.metro.pojo;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "oauth_account", uniqueConstraints = {
        @UniqueConstraint(name = "uq_oauth_identity", columnNames = {"provider", "provider_user_id"}),
        @UniqueConstraint(name = "uq_oauth_member_provider", columnNames = {"member_id", "provider"})})
public class OAuthAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Column(nullable = false, length = 20)
    private String provider;
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;
    @Column(nullable = false)
    private Instant createdAt;

    protected OAuthAccount() { }
    public OAuthAccount(Member member, String subject) {
        this.member = member; this.provider = "google"; this.providerUserId = subject;
        this.createdAt = Instant.now();
    }
    public Member getMember() { return member; }
    public String getProviderUserId() { return providerUserId; }
}
