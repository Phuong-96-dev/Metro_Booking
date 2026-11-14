# Xác thực Metro

## Kiến trúc và phạm vi

Dự án giữ Spring Boot **3.5.3**, Java target **17**, Spring MVC + Thymeleaf, Spring Data JPA và SQL Server. Không thêm SPA, JWT, UI framework hay đổi database chạy thật. `Member` tiếp tục là người dùng, giữ `member_id`, các quan hệ vé/thanh toán/blog/tàu, role `ADMIN` và `MEMBER`.

Trước thay đổi, đăng nhập dùng `findByEmailAndPassword`, đăng ký lưu mật khẩu trực tiếp và ghi mật khẩu ra console. Session chỉ chứa `member`, chưa có Spring Security, CSRF, OAuth, rate limiting hay cấu hình CORS riêng. Một số API quản trị chưa kiểm tra quyền, sửa profile nhận ID từ client, sử dụng vé chưa kiểm tra chủ sở hữu. Component/entity scanning có tên package không đồng nhất.

Thay đổi tái sử dụng controller nghiệp vụ, service/repository, template và session hiện tại. Bổ sung các starter Security, OAuth2 Client và Validation theo BOM hiện có; `spring-security-test` và H2 chỉ ở test scope. Không tự chạy migration, không tạo tài khoản Admin hoặc dữ liệu production, không push Git.

## Luồng xác thực

1. Đăng ký: DTO chỉ nhận thông tin cá nhân, validate họ tên/email/mật khẩu/xác nhận; mật khẩu tối thiểu 12 ký tự có chữ và số, tối đa 72 byte UTF-8. Email được trim/lowercase, kiểm tra trùng ở service và unique index trong DB. BCrypt cost 12; role luôn `MEMBER`, trạng thái `ACTIVE`. Thành công chuyển tới login.
2. Đăng nhập: Spring Security xử lý POST login, kiểm tra BCrypt và trạng thái active. Email không có, mật khẩu sai và tài khoản khóa dùng cùng lỗi. Đổi session ID sau xác thực, chỉ lưu principal không có hash và bản sao `session.member` không có password để giữ tương thích controller/template cũ.
3. Mỗi request của phiên đã đăng nhập đọc lại người dùng từ DB; cập nhật quyền/tên/thông tin hoặc hủy session nếu tài khoản bị khóa/xóa. Admin được chuyển đến dashboard, thành viên về trang chủ. Không có token trong localStorage hoặc URL redirect về trang ứng dụng.
4. Google: Authorization Code + OIDC với `openid profile email`, state, nonce và PKCE S256. Spring Security đổi code ở backend và kiểm tra chữ ký/JWK, issuer, audience, hạn token và nonce trước khi gọi account service. Chỉ nhận email đã được nguồn OIDC xác minh. Tra liên kết bằng `(google, sub)`, không dùng email làm định danh Google.
5. Google lần đầu với email mới: tạo `Member` role MEMBER/ACTIVE và `OAuthAccount` trong cùng transaction. Password được đặt thành BCrypt của chuỗi ngẫu nhiên không cung cấp cho người dùng. Lần sau dùng liên kết `sub`, kể cả khi email Google thay đổi.
6. Google trùng email local: từ chối tự liên kết, hiển thị hướng dẫn đăng nhập bằng mật khẩu. Tại profile, người dùng xác nhận mật khẩu Metro qua POST có CSRF rồi chọn Google. Xác nhận liên kết hết hạn sau 5 phút, dùng một lần; callback `google-link` phải còn đúng phiên đã xác nhận. Một Google identity không được chuyển sang thành viên khác; một thành viên chỉ có một liên kết Google. Không đổi email Metro khi liên kết.
7. Logout: POST có CSRF, hủy session và xóa JSESSIONID. Chỉ đăng xuất khỏi Metro; tài khoản Google trong trình duyệt vẫn có thể đăng nhập Google.

Session hết hạn sau 30 phút không hoạt động, cookie HttpOnly + SameSite=Lax, cookie-only tracking. Đặt `SESSION_COOKIE_SECURE=true` khi chạy HTTPS. Không bật CORS cho origin ngoài vì frontend cùng origin. Thymeleaf tự chèn CSRF vào các form POST có `th:action`.

Rate limiter của mỗi process giới hạn 30 lần POST login/register/link trên mỗi IP và 10 lần trên mỗi email trong 15 phút; trả HTTP 429 + Retry-After. Bộ nhớ có giới hạn 10.000 key, không tin `X-Forwarded-For`. Khi triển khai nhiều instance/proxy, cấu hình rate limit dùng chung ở gateway/Redis và giới hạn truy cập trực tiếp vào backend; session cũng cần shared store hoặc sticky session.

## Endpoints

Các endpoint là MVC form/redirect, không phải JSON API.

| Method | URL | Quyền / hành vi |
| --- | --- | --- |
| GET | `/register` | Public, form đăng ký |
| POST | `/register` | Public + CSRF, validate và tạo MEMBER |
| GET | `/login` | Public, form và thông báo |
| POST | `/login` | Public + CSRF, xác thực email/password |
| POST | `/logout` | CSRF, hủy phiên |
| GET | `/oauth2/authorization/google` | Khởi động Google login khi có credentials |
| GET | `/login/oauth2/code/google` | Callback đăng nhập, Spring Security xử lý |
| POST | `/account/google/link` | Đã đăng nhập + CSRF + mật khẩu hiện tại |
| GET | `/oauth2/authorization/google-link` | Khởi động liên kết sau xác nhận |
| GET | `/login/oauth2/code/google-link` | Callback liên kết, cần xác nhận còn hiệu lực |
| GET / POST | `/profile`, `/update-profile` | Người dùng hiện tại; bỏ qua ID/role/password do client gửi |

`/admindashboard`, `/adminManage/**`, thêm/sửa/xóa lịch tàu và POST sửa điểm dừng bus yêu cầu ADMIN. `/myTicket`, `/payment/**`, mua/dùng vé, gửi feedback và `/account/**` yêu cầu đăng nhập. Dùng vé kiểm tra đúng chủ sở hữu. Các trang công khai, danh sách tàu/ga/bus/lịch tàu, tin tức, tìm lịch và xem loại vé vẫn public. Route không khai báo bị từ chối mặc định.

Các thao tác xóa blog/lịch tàu và logout chuyển từ GET sang POST có CSRF; template đã cập nhật tương ứng. Sửa email qua profile bị khóa vì email là định danh đăng nhập; đổi email cần một quy trình xác minh riêng. Các trường tên/điện thoại/sinh viên/trường vẫn sửa được.

## Database và triển khai tài khoản cũ

Ở lượt triển khai đầu chưa xác nhận được kết nối SQL Server. Lượt chẩn đoán sau xác nhận server local hoạt động nhưng chưa có database `metro`; database local mới đã được tạo theo yêu cầu và kiểm tra ở mục “Database local mới” bên dưới. Với database có dữ liệu từ môi trường khác, cần đối chiếu mapping JPA (`dbo.member`, `member_id`) trước khi chạy script.

1. Sao lưu DB, xem script `docs/database/V001__authentication.sql` và chạy trên bản sao/staging trước.
2. Script dừng nếu thiếu bảng Member, có email trùng sau trim/lowercase, hoặc cột password quá ngắn. Không tự gộp/xóa tài khoản trùng. Các email cũ không bị sửa.
3. Thêm cột tính toán persisted `member.email_normalized` và unique filtered index cho email khác NULL. Thêm `oauth_account` gồm ID, member_id FK, provider, provider_user_id (sub), created_at; unique provider/sub và member/provider. Định danh OAuth dùng binary collation để giữ phân biệt hoa/thường.
4. Cấu hình mặc định `ddl-auto=validate`, thay cho `update`, nhằm yêu cầu schema sẵn sàng trước khi chạy. Không có Flyway/startup runner tự chạy script. Cần database hiện hữu; script này không tạo lại toàn bộ schema Metro.
5. `V001__authentication_rollback.sql` chỉ gỡ index email của migration nếu cần; giữ bảng/cột/link để không mất dữ liệu. Bản app cũ bỏ qua các bổ sung này. BCrypt là một chiều: rollback sang app so sánh plaintext không thể đăng nhập các mật khẩu đã chuyển; ưu tiên bản app hỗ trợ BCrypt hoặc rolling forward. Không có rollback biến hash thành plaintext.

Mật khẩu plaintext cũ **bị từ chối mặc định**. Để chuyển tiếp tài khoản cũ mà không reset toàn bộ, chủ hệ thống có thể tạm đặt `AUTH_ALLOW_LEGACY_PASSWORDS=true`: đăng nhập đúng bằng mật khẩu cũ sẽ thay ngay bằng BCrypt trong transaction. Không đổi mật khẩu không đăng nhập, không ghi plaintext mới. Chỉ bật trong giai đoạn chuyển đổi ngắn, sau đó tắt và xử lý tài khoản còn lại qua quy trình reset có xác minh. Mật khẩu cũ dài hơn 72 byte cần reset; không cắt ngắn âm thầm. Không thể đảo ngược các hash đã chuyển.

## Biến môi trường và cách chạy

Xem `.env.example`; dự án đã cấu hình `spring.config.import=optional:file:./.env[.properties]` để đọc `.env` tại thư mục chạy. Có thể copy `.env.example` thành `.env`, điền giá trị dạng Java properties (không bọc bằng dấu nháy shell), rồi chạy với working directory là thư mục dự án. Biến môi trường process/IDE/deployment vẫn được ưu tiên. Xem [cấu hình external của Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html).

| Biến | Giá trị / mục đích |
| --- | --- |
| `DB_URL` | Mặc định SQL Server localhost:1433, database metro; production dùng TLS/certificate phù hợp |
| `DB_USERNAME` | Mặc định sa để tương thích local; production dùng tài khoản DB riêng |
| `DB_PASSWORD` | Bắt buộc, không có mật khẩu hardcode |
| `GOOGLE_CLIENT_ID` | Web OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Chỉ backend |
| `GOOGLE_REDIRECT_URI` | `http://localhost:8080/login/oauth2/code/google` |
| `GOOGLE_LINK_REDIRECT_URI` | `http://localhost:8080/login/oauth2/code/google-link` |
| `SESSION_COOKIE_SECURE` | false khi local HTTP; true khi HTTPS |
| `AUTH_ALLOW_LEGACY_PASSWORDS` | false mặc định; chỉ tạm bật để nâng cấp plaintext cũ |

Khi thiếu ID hoặc secret Google, app vẫn chạy email/password và UI thông báo Google chưa cấu hình. Không dùng placeholder giả để kích hoạt OAuth. `.env` và cache Maven local được ignore; `.env.example` chỉ chứa ví dụ, không chứa credentials thật.

PowerShell, sau khi đã chuẩn bị database và chạy migration:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-17-or-newer'
$env:DB_PASSWORD = '<database password>'
$env:GOOGLE_CLIENT_ID = '<client-id>'
$env:GOOGLE_CLIENT_SECRET = '<client-secret>'
# Local HTTP dùng mặc định các redirect URI và cookie secure=false.
.\mvnw.cmd spring-boot:run
```

Truy cập `http://localhost:8080/login`. Khi đổi host/port/context-path, cấu hình cả hai redirect URI chính xác và cập nhật allowlist trong Google. Khi chạy sau reverse proxy HTTPS, thiết lập origin/forwarded headers ở proxy tin cậy và dùng các redirect URI HTTPS cố định; không bật tin forwarded headers từ nguồn tùy ý.

## Tạo Google OAuth client

Theo [hướng dẫn OpenID Connect của Google](https://developers.google.com/identity/openid-connect/openid-connect):

1. Mở Google Cloud Console, chọn/tạo project; trong Google Auth Platform cấu hình Branding, Audience và Data Access (openid, email, profile). Với ứng dụng External đang Testing, thêm tài khoản test trong Audience.
2. Tại Clients, tạo OAuth client loại **Web application**. Copy Client ID/Client Secret vào biến môi trường backend. Không đặt secret vào template/JS/Git.
3. Thêm **cả hai Authorized redirect URIs** dưới đây, đúng ký tự và không thêm dấu `/` cuối:
   - `http://localhost:8080/login/oauth2/code/google`
   - `http://localhost:8080/login/oauth2/code/google-link`
4. Khi production, thêm hai URI HTTPS trên domain thật và đặt `GOOGLE_REDIRECT_URI`, `GOOGLE_LINK_REDIRECT_URI`, `SESSION_COOKIE_SECURE=true`.
5. Chạy lại backend sau khi thay biến môi trường. Mở login → Tiếp tục với Google; thử tài khoản mới, tài khoản đã liên kết và trường hợp trùng email local. Đường dẫn của Console có thể thay đổi; dùng tài liệu Google làm nguồn chuẩn.

Backend dùng cơ chế [OAuth2 Login/OIDC của Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/), không tự viết bộ xác minh token.

## Kiểm thử

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Test dùng H2 trong RAM với profile `test`, không kết nối SQL Server. Test OAuth mở provider ở loopback và ký JWT RSA; chạy qua redirect + authorization code callback thật của Spring Security, token exchange và JWK verification, không dùng Google credentials thật. Môi trường sandbox cần cho phép mở loopback.

Phủ các ca: đăng ký/hash/role mặc định, email trùng, dữ liệu sai, login đúng/sai/khóa/không tồn tại, nâng cấp legacy khi opt-in, logout/session rotation, CSRF, guest/member/admin, thu hồi quyền/khóa phiên đang mở, ID giả khi sửa profile, quyền sở hữu vé, giới hạn thử đăng nhập, các trang public/tìm bus, Google lần đầu/lần sau/sub, không tự link email, link có xác nhận password, callback lỗi/state sai/consent bị từ chối/token exchange lỗi, token sai issuer/audience/expiration/nonce/signature và email chưa xác minh.

Đã bổ sung kiểm tra khởi động và các trang public trên SQL Server local mới. Chưa kiểm thử migration trên dữ liệu thật, hoàn tất Google consent bằng tài khoản thật, cookie Secure qua HTTPS/proxy và quan sát responsive trực tiếp trong trình duyệt. Các truy vấn thống kê doanh thu riêng SQL Server cần smoke test lại ở staging. Không chạy production schema migration tự động.

Kết quả thực tế ngày **12/11/2026**: biên dịch thành công; Maven `package` thành công với **21 tests, 0 failures, 0 errors, 0 skipped** (12 test tích hợp MVC/auth, 7 test Google OAuth/OIDC, 1 test nâng cấp legacy, 1 context test). Luồng mua vé → thanh toán → dùng vé → gửi phản hồi đã qua test. `git diff --check` sạch. JAR: `target/metro-0.0.1-SNAPSHOT.jar`.

Máy thực hiện dùng JDK 22 và Maven 3.9.9 có sẵn, biên dịch với `--release 17` theo POM; chưa chạy runtime trên JDK 17 riêng. Wrapper dự án vẫn giữ Maven 3.9.10. Lệnh build thực tế tương đương `mvn -B -Dmaven.repo.local=C:\metro\.maven-repository package`; cache riêng trong workspace để không sửa bộ nhớ đệm người dùng. Không còn lỗi test; compiler vẫn báo API deprecated trong `TrainStationServiceImpl` có từ trước và Thymeleaf còn một số fragment cũ ngoài phạm vi xác thực.

## Danh sách file

Danh sách đầy đủ file tạo/chỉnh sửa nằm trong `docs/AUTHENTICATION_FILES.md`.

## Database local mới

Theo yêu cầu tạo database mới, đã tạo `metro` trên SQL Server `localhost:1433` và cấu hình `DB_URL` trong `.env` local. Database không có dữ liệu mẫu hoặc tài khoản Admin tự tạo.

Schema cơ sở được xuất từ các entity hiện có bằng SQL Server dialect, lưu tại `docs/database/V000__initial_schema.sql`. Script V000 chỉ chạy khi database tên `metro` và chưa có bảng người dùng; từ chối database không rỗng. Với database mới, có thể chạy trực tiếp `docs/database/CreateMetroDatabase.sql` để tạo database, đủ 14 bảng, index và khóa ngoại. Script `V001__authentication.sql` vẫn dùng để bổ sung email chuẩn hóa cho database cũ. Các database khác không bị chỉnh sửa.

Ứng dụng tiếp tục sử dụng `ddl-auto=validate`; không để Hibernate tự tạo/cập nhật bảng mỗi lần khởi động. Khi chạy từ IntelliJ, đặt working directory `C:\metro` để đọc đúng `.env`, rồi chạy `sum25.se180455.metro.main.MetroApplication`. Khi chạy bằng JAR, mở PowerShell tại thư mục dự án và dùng `java -jar target/metro-0.0.1-SNAPSHOT.jar` với Java 17 trở lên. Các trang danh sách sẽ trống cho tới khi có dữ liệu nghiệp vụ được nhập.

Kết quả thực tế: khởi động bình thường với SQL Server và schema validation thành công; `/`, `/login`, `/register`, `/ticket`, `/blog` đều HTTP 200. Form login có CSRF. Endpoint Google trả redirect tới `accounts.google.com` với state, nonce và PKCE S256; không thực hiện bước chọn tài khoản/consent Google thật. Không đăng ký tài khoản thử hoặc thêm bản ghi nghiệp vụ. Tiến trình kiểm tra đã dừng để tránh xung đột khi chạy bằng IDE.

## Chẩn đoán lỗi khởi động khi cấu hình Google

Lượt kiểm tra local tiếp theo ngày 12/11/2026 xác nhận SQL Server tại `localhost:1433` hoạt động và tài khoản local đăng nhập được vào `master`, nhưng **chưa có database `metro`**. Lỗi thực tế là `Cannot open database "metro" requested by the login`, xảy ra ở bước khởi tạo JPA trước khi Google OAuth chạy. Không trỏ ứng dụng sang database nghiệp vụ khác để tránh dùng sai schema.

Nếu database Metro ở server/instance khác, đặt `DB_URL` trong `.env` theo vị trí thực tế, cùng `DB_USERNAME`/`DB_PASSWORD` phù hợp. Nếu chưa có database, cần khôi phục bản Metro hiện có hoặc chủ động chuẩn bị database local trống và schema ban đầu. Migration V001 chỉ mở rộng database hiện hữu, không tạo toàn bộ schema Metro. Giữ `ddl-auto=validate` khi dùng database có dữ liệu.

Google credentials hiện có đã được chuyển sang `.env` local bị Git ignore; `application.properties` chỉ giữ tham chiếu biến cấu hình. Sau khi bổ sung import `.env`, build `package` chạy lại thành công với 21 tests, 0 failures/errors/skipped. Chưa tạo database hoặc chạy migration trong lượt chẩn đoán này.
