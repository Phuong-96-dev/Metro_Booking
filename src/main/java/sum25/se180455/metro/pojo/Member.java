    package sum25.se180455.metro.pojo;

    import jakarta.persistence.*;
    import com.fasterxml.jackson.annotation.JsonIgnore;

    import java.util.List;

    @Entity
    public class Member {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long memberId;

        private String name;
        private String studentCard;
        @Column(unique = true)
        private String email;
        private String phone;
        private String schoolName;
        @JsonIgnore
        private String password;
        private String role;
        private String status; // Default status

        @OneToMany(mappedBy = "member")
        private List<Payment> payments;

        @OneToMany(mappedBy = "member")
        private List<Ticket> tickets;

        @OneToMany(mappedBy = "admin")
        private List<Blog> blogs;

        @OneToMany(mappedBy = "admin")
        private List<Train> trains;

        public Member(String name, String studentCard, String email, String phone, String schoolName, String password, String role, String status, List<Payment> payments, List<Ticket> tickets, List<Blog> blogs, List<Train> trains) {
            this.name = name;
            this.studentCard = studentCard;
            this.email = email;
            this.phone = phone;
            this.schoolName = schoolName;
            this.password = password;
            this.role = role;
            this.status = status;
            this.payments = payments;
            this.tickets = tickets;
            this.blogs = blogs;
            this.trains = trains;
        }

        public Member(Long memberId, String name, String studentCard, String email, String phone, String schoolName, String password, String role, String status, List<Payment> payments, List<Ticket> tickets, List<Blog> blogs, List<Train> trains) {
            this.memberId = memberId;
            this.name = name;
            this.studentCard = studentCard;
            this.email = email;
            this.phone = phone;
            this.schoolName = schoolName;
            this.password = password;
            this.role = role;
            this.status = status;
            this.payments = payments;
            this.tickets = tickets;
            this.blogs = blogs;
            this.trains = trains;
        }

        public Member() {
        }

        public Long getMemberId() {
            return memberId;
        }

        public void setMemberId(Long memberId) {
            this.memberId = memberId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStudentCard() {
            return studentCard;
        }

        public void setStudentCard(String studentCard) {
            this.studentCard = studentCard;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSchoolName() {
            return schoolName;
        }

        public void setSchoolName(String schoolName) {
            this.schoolName = schoolName;
        }

        public String getPassword() {
            return password;
        }

        public List<Train> getTrains() {
            return trains;
        }

        public void setTrains(List<Train> trains) {
            this.trains = trains;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<Payment> getPayments() {
            return payments;
        }

        public void setPayments(List<Payment> payments) {
            this.payments = payments;
        }

        public List<Ticket> getTickets() {
            return tickets;
        }

        public void setTickets(List<Ticket> tickets) {
            this.tickets = tickets;
        }

        public List<Blog> getBlogs() {
            return blogs;
        }

        public void setBlogs(List<Blog> blogs) {
            this.blogs = blogs;
        }

    }
