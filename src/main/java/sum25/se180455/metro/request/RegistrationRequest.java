package sum25.se180455.metro.request;

import jakarta.validation.constraints.*;
import sum25.se180455.metro.pojo.Member;
import java.nio.charset.StandardCharsets;

public class RegistrationRequest {
    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự.")
    private String name;
    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không hợp lệ.")
    @Size(max = 254, message = "Email quá dài.")
    private String email;
    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    @Size(min = 12, max = 72, message = "Mật khẩu từ 12 đến 72 ký tự (tối đa 72 byte UTF-8).")
    private String password;
    @NotBlank(message = "Vui lòng xác nhận mật khẩu.")
    private String confirmPassword;
    @Size(max = 20, message = "Số điện thoại quá dài.")
    @Pattern(regexp = "[+0-9 ()-]*", message = "Số điện thoại không hợp lệ.")
    private String phone;
    @Size(max = 100, message = "Mã sinh viên quá dài.")
    private String studentCard;
    @Size(max = 200, message = "Tên trường quá dài.")
    private String schoolName;

    @AssertTrue(message = "Xác nhận mật khẩu không khớp.")
    public boolean isPasswordMatching() { return password != null && password.equals(confirmPassword); }
    @AssertTrue(message = "Mật khẩu phải có chữ và số, tối đa 72 byte UTF-8.")
    public boolean isPasswordPolicyValid() {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72
                && password.codePoints().anyMatch(Character::isLetter)
                && password.codePoints().anyMatch(Character::isDigit);
    }
    public Member toMember() {
        Member member = new Member();
        member.setName(name); member.setEmail(email); member.setPassword(password);
        member.setPhone(phone); member.setStudentCard(studentCard); member.setSchoolName(schoolName);
        return member;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? null : name.strip(); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.strip(); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStudentCard() { return studentCard; }
    public void setStudentCard(String studentCard) { this.studentCard = studentCard; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
}
