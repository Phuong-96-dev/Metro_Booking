package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String name;
    private String phone;
    private String email;
    private String password;
    private String role;

    @OneToMany(mappedBy = "admin")
    private List<Train> trains;

    @OneToMany(mappedBy = "approvedBy")
    private List<Blog> approvedBlogs;
}

