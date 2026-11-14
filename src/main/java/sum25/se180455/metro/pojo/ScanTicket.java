package sum25.se180455.metro.pojo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_tickets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_scan_ticket_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_scan_ticket_order_ref", columnNames = "order_ref")
        },
        indexes = @Index(name = "idx_scan_ticket_user", columnList = "user_id"))
public class ScanTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32, updatable = false)
    private String code;

    @Column(name = "order_ref", nullable = false, length = 64, updatable = false)
    private String orderRef;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_name", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used_by", length = 100)
    private String usedBy;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getOrderRef() { return orderRef; }
    public void setOrderRef(String orderRef) { this.orderRef = orderRef; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public String getUsedBy() { return usedBy; }
}
