package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    private String name;
    private String studentCard;
    private String email;
    private String phone;
    private String schoolName;
    private String password;
    private String role;

    @OneToMany(mappedBy = "member")
    private List<Payment> payments;

    @OneToMany(mappedBy = "member")
    private List<Ticket> tickets;

    @OneToMany(mappedBy = "member")
    private List<Blog> blogs;
}
