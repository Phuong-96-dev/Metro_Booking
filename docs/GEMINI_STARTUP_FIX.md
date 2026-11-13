# Sửa lỗi thiếu Bean Validation provider khi khởi động Metro

## Nguyên nhân đã xác minh

`pom.xml` đã có `spring-boot-starter-validation:3.5.3` ở compile scope và không có exclusion Hibernate Validator. Dependency tree trước sửa xác nhận starter kéo `hibernate-validator:8.0.2.Final`, `jakarta.validation-api:3.0.2`, `jboss-logging:3.6.1.Final`, `classmate:1.7.0` và `tomcat-embed-el:10.1.42`. Không có xung đột phiên bản trong cây phụ thuộc được kiểm tra.

Tuy nhiên tại thời điểm kiểm tra, cache Maven mặc định dùng cho IDE `C:\Users\LENOVO\.m2\repository\org\hibernate\validator\hibernate-validator\8.0.2.Final` chỉ có POM và `hibernate-validator-8.0.2.Final.jar.lastUpdated`, **không có JAR**. `.lastUpdated` là dấu vết tải artifact thất bại, không phải thư viện có thể đưa vào classpath. Cache riêng `C:\metro\.maven-repository` có JAR hợp lệ, giải thích vì sao các lượt Maven trước dùng cache riêng chạy được trong khi runtime dùng cache mặc định thiếu provider.

`GeminiConfiguration.Properties` có `@Validated`, nên khi Spring bind `app.gemini` nó cần implementation Bean Validation. Thiếu provider khiến lỗi xuất hiện ở bước bind này, lan ngược qua `geminiRestClient`, `GeminiChatService`, `ChatController`. Đây không phải lỗi API key hoặc constructor record.

Phiên bản `8.0.2.Final` được xác nhận từ BOM Spring Boot 3.5.3 trong cache và [POM chính thức starter-validation 3.5.3](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-validation/3.5.3). Không dùng Hibernate Validator 9 hoặc namespace `javax.validation`.

## Sửa tối thiểu

1. Thêm dependency trực tiếp `org.hibernate.validator:hibernate-validator` vào `pom.xml`, compile scope mặc định, **không chỉ định version** để BOM Boot 3.5.3 quản lý. Giữ nguyên starter-validation.
2. Chạy Maven với `-U` và cache `.m2` mặc định để khôi phục artifact thiếu. Sau đó JAR 8.0.2.Final đã hiện diện, SHA1 khớp file checksum Maven và có `META-INF/services/jakarta.validation.spi.ValidationProvider`.
3. Thêm `GeminiConfigurationTests.java`: xác nhận provider runtime thực sự có mặt, constraint Gemini vẫn được thực thi, cấu hình key rỗng bind/khởi tạo thành công và chat trả `NOT_CONFIGURED`. Timeout không hợp lệ vẫn bị từ chối.

Không sửa `GeminiConfiguration.java`, `application.properties`, `.env`, code đăng nhập/đăng ký/Google OAuth, SecurityConfig, entity/repository hoặc dữ liệu SQL Server trong lượt sửa này. Không bỏ `@Validated` hoặc `@Valid`.

## Kiểm tra Gemini configuration

- `GEMINI_API_KEY` trong `.env` hiện có giá trị; chỉ kiểm tra có/rỗng, không in key. File vẫn bị Git ignore. Không hardcode hoặc thay key.
- `.env` không đặt `GEMINI_MODEL`; ứng dụng dùng mặc định `gemini-3.1-flash-lite` từ `application.properties`.
- Key rỗng là hợp lệ khi bind. Model vẫn có `@NotBlank` và pattern, timeout vẫn giới hạn 1–60 giây; mặc định 25 giây. Những validation này được giữ nguyên.
- Chỉ khi gửi tin nhắn mới cần Gemini key. Không có key: trả 503 `NOT_CONFIGURED`, không gọi Google, không làm website dừng khởi động.
- Lượt này chưa gọi Gemini live hoặc xác minh tính hợp lệ/quota của key Google.

## Kết quả chạy thực tế ngày 13/11/2026

- Maven `compile` bằng cache mặc định: **BUILD SUCCESS**.
- Maven `test`: **75 test, 0 failure, 0 error, 0 skipped; BUILD SUCCESS**. Gồm test binding/provider mới và toàn bộ test xác thực/OAuth/chat cũ. Test DB chỉ dùng H2 in-memory.
- Khởi động trực tiếp main class `sum25.se180455.metro.main.MetroApplication` bằng JDK 22, bytecode target 17, runtime classpath do Maven tạo, có Hibernate Validator 8.0.2.Final từ `.m2` mặc định.
- Log: `Tomcat started on port 8080 (http)` và `Started MetroApplication`; PID lúc chạy là 19564. Kết nối SQL Server thành công, dùng `spring.jpa.hibernate.ddl-auto=validate`, không chạy migration hoặc thao tác ghi dữ liệu.
- GET `/`, `/login`, `/register`, `/chat/chatbox.js` trên 8080: **200**.
- POST `/api/chat/message` với CSRF lấy từ HTML Thymeleaf và message rỗng: **400** (validation hoạt động, không gọi provider).
- POST chat thiếu CSRF thực tế: **302 về `/login`**, request không được vào controller. Đây là hành vi error dispatch/security hiện có, không sửa trong nhiệm vụ khởi động. MockMvc test CSRF vẫn trả 403 vì không chạy error dispatch của container.
- Chạy phiên tạm trên 18082 với `--app.gemini.api-key=`: Tomcat khởi động; trang chủ/login/register trả **200**; tin nhắn hợp lệ có CSRF trả **503**, JSON `code=NOT_CONFIGURED` và thông báo “Metro AI chưa được cấu hình. Vui lòng liên hệ quản trị viên hoặc thử lại sau.” Phiên này không gửi dữ liệu tới Google.

## Chạy lại

Trong IntelliJ dùng **Reload All Maven Projects** sau khi cập nhật POM, rồi chạy cấu hình MetroApplication hiện có. Không thêm JAR thủ công vào module. Nếu một phiên Metro đang giữ port 8080, dừng phiên đó trước khi Run lại trong IDE.

PowerShell tại thư mục dự án, với JDK 17 trở lên đã cấu hình:

```powershell
.\mvnw.cmd -U compile
.\mvnw.cmd spring-boot:run
```

Log/diagnostic trong `target/` (đã Git ignore): `validation-default-repo-tree.log`, `validation-fixed-tree.log`, `validation-compile.log`, `validation-tests-final.log`, `validation-startup-8080.log`, `validation-no-key-startup.log`, `validation-http-smoke.log`, `validation-runtime-classpath.txt`. Một số lần offline ban đầu không chạy được do cache plugin chưa đủ; kết quả cuối ở các log nêu trên đã thành công sau khi tải bổ sung bằng Maven.
