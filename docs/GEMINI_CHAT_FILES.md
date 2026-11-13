# File của lượt tích hợp Gemini

Danh sách này chỉ ghi phần chatbot được bổ sung trong lượt này. Checkout đã có thay đổi về xác thực, OAuth và giao diện trước khi bắt đầu; những thay đổi đó được giữ nguyên. Không dùng toàn bộ `git diff` hiện tại làm danh sách thay đổi chatbot.

## File mới (18)

Backend:

- `src/main/java/sum25/se180455/metro/config/GeminiConfiguration.java`
- `src/main/java/sum25/se180455/metro/controller/ChatController.java`
- `src/main/java/sum25/se180455/metro/controller/ChatExceptionHandler.java`
- `src/main/java/sum25/se180455/metro/controller/ChatWidgetAdvice.java`
- `src/main/java/sum25/se180455/metro/request/ChatRequestDTO.java`
- `src/main/java/sum25/se180455/metro/request/ChatResponseDTO.java`
- `src/main/java/sum25/se180455/metro/service/ChatException.java`
- `src/main/java/sum25/se180455/metro/service/ChatRateLimiter.java`
- `src/main/java/sum25/se180455/metro/service/ChatSessionManager.java`
- `src/main/java/sum25/se180455/metro/service/GeminiChatService.java`

Frontend:

- `src/main/resources/templates/common/chatbox.html`
- `src/main/resources/static/chat/chatbox.css`
- `src/main/resources/static/chat/chatbox.js`

Kiểm thử:

- `src/test/java/sum25/se180455/metro/ChatIntegrationTests.java`
- `src/test/java/sum25/se180455/metro/GeminiChatServiceTests.java`
- `src/test/java/sum25/se180455/metro/service/ChatRateLimiterTests.java`

Tài liệu:

- `docs/GEMINI_CHAT.md`
- `docs/GEMINI_CHAT_FILES.md`

## File chỉnh sửa (30)

- `README.md`: liên kết tài liệu chatbot.
- `.env.example`: thêm ba biến Gemini, không chứa key thật.
- `src/main/resources/application.properties`: map biến môi trường sang cấu hình Gemini.
- `src/main/java/sum25/se180455/metro/security/SecurityConfig.java`: chỉ cho phép hai POST chat và asset chat công khai; giữ CSRF và các rule cũ.

26 template dưới đây chỉ được thêm fragment chat trước `</body>` và meta viewport ở các trang chưa có; không sửa nội dung UI/nghiệp vụ:

- `src/main/resources/templates/admindashboard.html`
- `src/main/resources/templates/blog.html`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/more.html`
- `src/main/resources/templates/profile.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/ticket.html`
- `src/main/resources/templates/adminManage/manageBlog.html`
- `src/main/resources/templates/adminManage/manageMember.html`
- `src/main/resources/templates/adminManage/manageTicketType.html`
- `src/main/resources/templates/busroute/search.html`
- `src/main/resources/templates/member/feedback.html`
- `src/main/resources/templates/member/myTicket.html`
- `src/main/resources/templates/member/payment.html`
- `src/main/resources/templates/member/showFeedback.html`
- `src/main/resources/templates/member/success.html`
- `src/main/resources/templates/train/busroute-detail.html`
- `src/main/resources/templates/train/busroute-list.html`
- `src/main/resources/templates/train/schedule-search.html`
- `src/main/resources/templates/train/train-detail.html`
- `src/main/resources/templates/train/train-list.html`
- `src/main/resources/templates/train/train-schedule-form.html`
- `src/main/resources/templates/train/train-schedule-list.html`
- `src/main/resources/templates/train/train-station-detail.html`
- `src/main/resources/templates/train/train-station-list.html`

Không sửa `pom.xml`, `.gitignore`, entity, repository, controller/service nghiệp vụ hay file navbar/footer trong lượt chatbot này.

## Xác minh

- Maven `verify`: **72 test, 0 failure, 0 error, 0 skipped; BUILD SUCCESS**, bao gồm toàn bộ test cũ và 26 test chatbot mới.
- Compile Java target 17 và Maven package thành công bằng JDK 22 có sẵn trên máy.
- `node --check src/main/resources/static/chat/chatbox.js`: thành công.
- `git diff --check`: thành công.
- Security/controller/provider tests: JSON thành công, lỗi provider 400/401/403/404/429/500/503, timeout, phản hồi lỗi/rỗng/bị chặn, thiếu key, DTO validation, CSRF thật lấy từ HTML, session/history/reset, rate limits và quyền cũ.
- Kiểm tra login thật bằng MemberPrincipal, đổi identity và logout trong H2; không dùng tài khoản SQL Server thật để kiểm thử ghi.
- Trình duyệt: trang chủ và login; mở, đóng, thu nhỏ, gửi bằng nút/Enter, Shift+Enter xuống dòng, trạng thái chờ, lỗi thiếu key, New Chat và history khi chuyển trang. Kiểm tra bố cục ở viewport mặc định, 390px và 320px; khung chat nằm trong vùng nội dung kể cả khi có scrollbar.
- Lần chạy UI đầu dùng cấu hình database local hiện có với `ddl-auto=validate` và chỉ thực hiện GET/chat. Sau đó chạy lại với H2 `jdbc:h2:mem:metro-chat-ui` được chỉ định tường minh để xác minh độc lập. Không tạo/xóa/sửa dữ liệu SQL Server.
- Chưa có API key trong môi trường hoặc `.env`: chưa gọi Gemini live, chưa xác minh quyền model/quota của project và chất lượng trả lời thực tế. Có hướng dẫn kiểm tra metadata API trong `GEMINI_CHAT.md`.

Log và ảnh chỉ ở thư mục `target/` đã được Git ignore: `chat-verify-final.log`, `chat-ui-isolated.log`, `chat-login-preview.jpg`. Không commit/push trong lượt triển khai này.
