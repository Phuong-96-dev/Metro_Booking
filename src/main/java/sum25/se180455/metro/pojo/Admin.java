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

    public Admin() {
    }

    public Admin(String name, String phone, String email, String password, String role, List<Train> trains, List<Blog> approvedBlogs) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role;
        this.trains = trains;
        this.approvedBlogs = approvedBlogs;
    }

    public Admin(Long adminId, String name, String phone, String email, String password, String role, List<Train> trains, List<Blog> approvedBlogs) {
        this.adminId = adminId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role;
        this.trains = trains;
        this.approvedBlogs = approvedBlogs;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Train> getTrains() {
        return trains;
    }

    public void setTrains(List<Train> trains) {
        this.trains = trains;
    }

    public List<Blog> getApprovedBlogs() {
        return approvedBlogs;
    }

    public void setApprovedBlogs(List<Blog> approvedBlogs) {
        this.approvedBlogs = approvedBlogs;
    }
}

