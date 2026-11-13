# Báo cáo sửa lỗi đăng ký và validation

Ngày kiểm tra: 13/11/2026 (Asia/Saigon).

## Nguyên nhân và xử lý

1. **NPE tại `RegistrationRequest.toMember()`**: gọi `name.strip()` trực tiếp không an toàn khi `name == null`. Chuyển chuẩn hóa tên vào `setName()` với kiểm tra null; `toMember()` chỉ sao chép tên đã chuẩn hóa.
2. **Controller chuyển đổi dữ liệu chưa hợp lệ**: mã hiện tại đã đặt `BindingResult` ngay sau `@Valid @ModelAttribute` và trả form trước `toMember()` nếu có lỗi. Giữ nguyên luồng này. Khi không có validation provider, annotation không đủ để bảo đảm kiểm tra dữ liệu. Tuy nhiên lỗi thiếu provider không tái hiện trong bản Maven hiện tại; không khẳng định được classpath của lần chạy lỗi trước.
3. **`NoProviderFoundException`**: exception này cho biết runtime không tìm thấy implementation Bean Validation. POM hiện có sẵn `spring-boot-starter-validation` ở compile scope, được quản lý phiên bản bởi Spring Boot 3.5.3. Không thêm dependency trùng hoặc cấu hình validator thủ công. Xác minh JAR mới có `hibernate-validator-8.0.2.Final.jar`, `jakarta.validation-api-3.0.2.jar` và `tomcat-embed-el-10.1.42.jar`. Thêm test inject `jakarta.validation.Validator` và kiểm tra các constraint trên form rỗng. Nếu IDE vẫn báo lỗi này, cần reload Maven và kiểm tra runtime classpath của cấu hình chạy đó.
4. **Đăng ký lỗi vẫn redirect thành công**: tái hiện được với tên chỉ chứa U+2003. Trước sửa, `@NotBlank` chấp nhận chuỗi này nhưng `strip()` sau validation biến tên thành chuỗi rỗng rồi lưu. Test hồi quy trước sửa: 24 ca, 1 FAIL vì nhận HTTP 302 thay vì 200. Sau sửa, setter chuẩn hóa trước validation nên tên rỗng bị từ chối. Thêm 24 ca kiểm tra trường thiếu/rỗng, định dạng, độ dài, mật khẩu UTF-8 quá 72 byte và xác nhận mật khẩu; mỗi ca phải trả form có lỗi, không có header Location, không tăng số bản ghi Member.
5. **Thymeleaf deprecated**: 8 template sidebar dùng `common/sidebar :: sidebar` chưa được bọc trong biểu thức fragment. Đổi thành `~{common/sidebar :: sidebar}`. Quét lại template và log cuối không còn cảnh báo này.
6. **GET không được hỗ trợ**: hai cảnh báo hiện tại xuất phát từ `csrfIsRequiredAndGetCannotLogoutOrDelete`, cố tình GET `/adminManage/deleteBlog/1` và `/train-schedules/delete/1`, yêu cầu HTTP 405. Đây là hành vi mong đợi. Form xóa/logout hiện dùng POST và CSRF; giữ nguyên các mapping và test bảo vệ này.

## Email trùng và phạm vi thay đổi

- Giữ cơ chế chuẩn hóa email bằng strip/lowercase, truy vấn email không phân biệt hoa thường, kiểm tra trùng trong service và bắt lỗi ràng buộc dữ liệu ở controller.
- Tăng cường test email trùng có khoảng trắng/khác chữ hoa: phải báo lỗi email, không thêm Member, không đổi tên/mật khẩu tài khoản cũ và không render mật khẩu.
- Giữ nguyên controller, service, repository, cấu hình security/OAuth, POM và cấu hình database đang có. Thay đổi mã chạy chỉ gồm chuẩn hóa tên và cú pháp 8 fragment; còn lại là test và báo cáo này.
- Không chạy ứng dụng bằng cấu hình production, không migration, không sửa/xóa bảng hay dữ liệu SQL Server. Tất cả integration test dùng profile `test`, `jdbc:h2:mem:metro-auth`; `create-drop` chỉ áp dụng database H2 trong RAM theo cấu hình test sẵn có.

## Kết quả cuối

| Bộ test | PASS | FAIL | ERROR | SKIP |
|---|---:|---:|---:|---:|
| AuthenticationIntegrationTests | 36 | 0 | 0 | 0 |
| GoogleOAuthIntegrationTests | 7 | 0 | 0 | 0 |
| LegacyPasswordTests | 1 | 0 | 0 | 0 |
| MetroApplicationTests | 2 | 0 | 0 | 0 |
| Tổng | 46 | 0 | 0 | 0 |

Maven `package`: **BUILD SUCCESS**, bao gồm toàn bộ test, không có bộ lọc test hoặc skip. JAR: `target/metro-0.0.1-SNAPSHOT.jar`. `git diff --check` sạch.

Môi trường: Maven 3.9.10, JDK 22, biên dịch theo `java.version=17` trong POM. Lệnh thực tế (PowerShell):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-22'
& 'C:\Users\LENOVO\.m2\wrapper\dists\apache-maven-3.9.10-bin\53h08a94dg6djh6umvruv7q564\apache-maven-3.9.10\bin\mvn.cmd' -o -B '-Dmaven.repo.local=C:\metro\.maven-repository' '-DargLine=-javaagent:C:\metro\.maven-repository\org\mockito\mockito-core\5.17.0\mockito-core-5.17.0.jar' package
```

Mockito được nạp bằng Java agent để tránh attach pipe bị sandbox chặn. Lần chạy toàn bộ đầu tiên trong sandbox không khởi tạo được provider OAuth kiểm thử do `Permission denied: connect` trên loopback; không sửa/vô hiệu hóa test. Chạy lại cùng lệnh với quyền mở loopback đã PASS cả 7 test OAuth/OIDC. Các test này thực hiện authorization-code callback, trao đổi token và xác minh JWT bằng provider local; không kiểm tra màn hình consent của Google bằng tài khoản thật.

Log cuối: `target/metro-verification.log`; báo cáo chi tiết từng test: `target/surefire-reports/TEST-*.xml`. Các cảnh báo H2Dialect, open-in-view và JVM CDS còn lại không làm test thất bại và nằm ngoài phạm vi sửa này.
