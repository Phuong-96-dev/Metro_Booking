package sum25.se180455.metro.pojo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_logs", indexes = @Index(name = "idx_scanlog_code", columnList = "ticket_code"))
public class ScanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", length = 100)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanResult result;

    @Column(name = "scanned_by", length = 100)
    private String scannedBy;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt = LocalDateTime.now();

    public ScanLog() {}

    public ScanLog(String ticketCode, ScanResult result, String scannedBy) {
        this.ticketCode = ticketCode;
        this.result = result;
        this.scannedBy = scannedBy;
    }

    public Long getId() { return id; }
    public String getTicketCode() { return ticketCode; }
    public ScanResult getResult() { return result; }
    public String getScannedBy() { return scannedBy; }
    public LocalDateTime getScannedAt() { return scannedAt; }
}
