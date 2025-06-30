package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private LocalDate paymentDate;
    private Double price;
    private String paymentMethod;
    private String status;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
