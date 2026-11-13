# Metro AI Assistant

## Kiến trúc đã kiểm tra

Spring Boot 3.5.3, Java target 17, Maven Wrapper, Spring MVC + Thymeleaf, Spring Data JPA/JDBC và SQL Server `metro`. Cấu trúc hiện tại gồm `controller`, `service`, `repository`, `pojo`, `request`, `security`; main class chủ động scan package gốc. Các controller/service/entity hiện có bao gồm thành viên, OAuth, tàu, ga, lịch tàu, xe buýt/điểm dừng, loại vé, vé, thanh toán, blog và đánh giá. Không đổi kiến trúc, thêm dependency, bảng, migration hoặc dữ liệu production.

Đã xác nhận luồng thật: `/register`, `/login`, tìm Ga đi/Ga đến ở `/` và `/search-schedules`, xem `/train-stations`, `/trains`, `/train-schedules`, `/bus-routes`, `/ticket`, lọc vé `/ticket/search`, mua vé POST `/ticket/buy` rồi đến `/payment`, `/myTicket`, `/profile`, `/blog`, `/feedback/all`. Chatbot chỉ nhận mô tả chức năng đã kiểm tra từ code; không gửi entity, session.member, ID tài khoản, vé, hồ sơ, mật khẩu hay dữ liệu database cho Gemini. Không khẳng định đã thu tiền qua ngân hàng vì code hiện tại chưa xác nhận tích hợp payment gateway.

## Gemini và cấu hình

Dùng **REST API chính thức `v1`** qua Spring RestClient có sẵn và Java HttpClient. Không cần SDK/dependency ngoài, không dùng SDK cũ. Default model **`gemini-3.1-flash-lite`**, stable, thiên về tác vụ nhẹ/độ trễ thấp theo tài liệu Google kiểm tra ngày 13/11/2026:

- [Model và trạng thái stable](https://ai.google.dev/gemini-api/docs/models/gemini-3.1-flash-lite)
- [API v1 và v1beta](https://ai.google.dev/gemini-api/docs/api-versions)
- [generateContent](https://ai.google.dev/api/generate-content)
- [Model metadata và supportedGenerationMethods](https://ai.google.dev/api/models)

Quyền sử dụng model/quota phụ thuộc API key/project. Môi trường triển khai ban đầu chưa có key; chưa xác minh được quyền truy cập model bằng key thực tế. Không tự đổi model hoặc thử lại nhiều lần khi Google từ chối, để tránh phát sinh chi phí ngoài ý muốn.

PowerShell tại thư mục dự án:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-17-or-newer'
$env:GEMINI_API_KEY = '<API key của bạn>'
$env:GEMINI_MODEL = 'gemini-3.1-flash-lite'
.\mvnw.cmd spring-boot:run
```

Điền API key trong môi trường process/IDE/deployment. Dự án cũng đã hỗ trợ `.env` dưới dạng Java properties; có thể thêm các biến Gemini từ `.env.example` vào `.env` đang có, không ghi đè cấu hình database/OAuth cũ. `.env` đã được `.gitignore` loại trừ. Không đưa key vào template, JavaScript, URL hoặc Git. Khởi động lại ứng dụng sau khi đổi biến.

| Biến | Mặc định | Ý nghĩa |
| --- | --- | --- |
| `GEMINI_API_KEY` | rỗng | Chỉ backend; thiếu key trả 503 `NOT_CONFIGURED`, các chức năng cũ tiếp tục chạy |
| `GEMINI_MODEL` | `gemini-3.1-flash-lite` | Tên model Gemini, không gồm `models/` |
| `GEMINI_TIMEOUT_SECONDS` | `25` | Read timeout 1–60 giây; connect timeout cố định 5 giây |

Cần database Metro và các cấu hình DB hiện có như hướng dẫn [xác thực](AUTHENTICATION.md). Không chạy script tạo lại DB để thử chatbot. Java trên PATH của máy có thể là Java 8; hãy đặt `JAVA_HOME` đúng trước khi dùng Maven.

Kiểm tra model bằng key hiện tại trước khi triển khai (không in key hay lỗi thô từ Google):

```powershell
if ([string]::IsNullOrWhiteSpace($env:GEMINI_API_KEY)) { throw 'Chưa đặt GEMINI_API_KEY.' }
$chatModel = if ($env:GEMINI_MODEL) { $env:GEMINI_MODEL } else { 'gemini-3.1-flash-lite' }
try {
    $chatMetadata = Invoke-RestMethod -Method Get -Uri ('https://generativelanguage.googleapis.com/v1/models/' + [Uri]::EscapeDataString($chatModel)) -Headers @{ 'x-goog-api-key' = $env:GEMINI_API_KEY } -TimeoutSec 15
    $chatMetadata | Select-Object name, supportedGenerationMethods
    if ('generateContent' -notin $chatMetadata.supportedGenerationMethods) { throw 'Model không hỗ trợ generateContent.' }
} catch { throw 'Không xác nhận được model. Kiểm tra tên model, API key, quyền project và kết nối.' }
```

## API và bảo mật

`POST /api/chat/message`, Content-Type application/json:

```json
{"message":"Làm sao để mua vé tàu?"}
```

Thành công 200:

```json
{"reply":"Bạn hãy mở mục Vé tàu..."}
```

`POST /api/chat/reset` xóa ngữ cảnh phiên và trả 204. Cả hai public để dùng trên trang login nhưng **vẫn bắt buộc CSRF**. Thymeleaf render token XOR và tên header trong fragment; JS gửi header đó cùng cookie phiên. Không sửa rule CSRF, CORS hoặc quyền các route cũ. Thiếu/sai CSRF trả 403; frontend yêu cầu tải lại trang. Không làm public `/api/**` chung; chỉ hai POST chat và GET asset `/chat/**`.

Tin nhắn tối đa 2.000 ký tự, loại nội dung trống; DTO không nhận history/tools/role. Server chỉ gửi tối đa 12 tin hoàn tất trước đó (6 cặp user/model) và tin mới. Chỉ lưu thành công, giới hạn trả lời 2.048 token và tối đa 8.000 ký tự; bỏ thought và phản hồi bị chặn/chưa hoàn tất. System instruction cấm bịa dữ liệu thực tế và cấm thao tác thay đổi dữ liệu. Đây là ràng buộc hành vi của model, không phải cam kết model không bao giờ sai; backend không cấp bất kỳ tool/quyền DB nào nên model không thể thực hiện giao dịch.

Rate limiting mỗi process: 10 tin/phút/phiên, 20 tin/phút/IP, tổng 120 tin/phút, tối đa 8 lệnh gọi Gemini đồng thời; bộ nhớ limiter giới hạn khoảng 10.000 key. IP lấy `remoteAddr`, không tin `X-Forwarded-For` từ client. 429 có Retry-After 60. Khi chạy nhiều instance hoặc reverse proxy, cần rate limiting dùng chung ở gateway/Redis và session store chung/sticky session; cấu hình proxy tin cậy riêng tại hạ tầng. Reset không gọi Gemini; không reset rate limit.

Ngữ cảnh server trong HttpSession, được tách khi identity thay đổi (kể cả login dùng session fixation protection), tự mất khi logout/hết phiên. UI lưu tối đa 40 tin ở `sessionStorage`, theo UUID ngẫu nhiên của phiên/identity, xóa history identity cũ khi render trang mới. Không dùng localStorage. New Chat xóa cả hai phía; không hủy/reset khi yêu cầu trước đang chạy. Back/forward cache sẽ tải lại trang để nhận identity/CSRF mới. Tab cũ sau khi đổi đăng nhập cần tải lại; token cũ bị từ chối.

CSS chỉ chọn dưới `#metro-ai`; một fragment được thêm trước `</body>` của mỗi trang, không đổi nội dung navbar/footer. Các trang chưa có viewport được bổ sung meta viewport để chatbox responsive. Hiển thị text bằng `textContent`, không render HTML/Markdown từ AI. Header/footer không bị chatbot sửa DOM.

Lỗi JSON chỉ gồm `code`, `error`, không trả stack trace/provider body/API key. Có thông báo cho thiếu key, cấu hình bị từ chối, model không khả dụng, quota/429, timeout/mất kết nối, phản hồi rỗng/bị chặn và lỗi backend. Không log prompt, reply hoặc raw lỗi Gemini. Không tự retry giao dịch tính phí.

## Kiểm thử và chạy thử

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

Các test dùng H2 **in-memory** trong profile test, không chạm SQL Server thật. Gemini service test dùng MockRestServiceServer; integration test mock provider để kiểm tra security/controller/session/render template. Bộ test cũ tiếp tục kiểm tra đăng ký, đăng nhập, quyền, OAuth và quyền sở hữu vé.

Kiểm tra thủ công:

1. Mở `http://localhost:8080/login` và trang chủ; nút xanh Metro AI nằm góc phải dưới. Mở, thu nhỏ, đóng, mở lại.
2. Gửi “Làm sao để mua vé tàu?” bằng Enter; Shift+Enter tạo dòng mới. Kiểm tra trạng thái chờ, bubble hai phía và responsive 390px/320px.
3. Khi chưa có key, có lỗi thân thiện; trang và chức năng đăng nhập vẫn hoạt động. Sau khi có key hợp lệ, kiểm tra câu trả lời thật.
4. Tải lại/chuyển trang cùng phiên: xem history; New Chat xóa UI và ngữ cảnh. Đăng nhập/đăng xuất/đổi tài khoản: history mới. Cửa sổ riêng tư có phiên riêng.
5. DevTools Network: body chỉ `message`, không có key; header CSRF và cookie được gửi. Xóa/sửa CSRF: 403. Gửi quá giới hạn: 429.
6. Hỏi giờ tàu, giá vé, số ghế hoặc đơn hàng: phải nêu chưa thể xác nhận và chỉ tới trang phù hợp. Không đặt vé/thanh toán/đổi profile qua chat.
7. Thử mất mạng và model không hợp lệ; không có stack trace/provider body. Kiểm tra lại navbar, footer, form đăng nhập và tìm chuyến.

## Phần chưa tích hợp và API đọc đề xuất

Chưa nối SQL Server vào chatbot, chưa lưu DB, stream, tìm kiếm/RAG, thao tác đặt vé/thanh toán/sửa tài khoản. Không bảo đảm thông tin giá/tuyến/giờ thực tế khi chưa cấp dữ liệu.

Nếu mở rộng giai đoạn sau, đề xuất API đọc công khai với projection whitelist và giới hạn kết quả: GET `/api/public/stations` (id/tên/vị trí), GET `/api/public/schedules?start=...&end=...` (ga đi/đến, giờ từ lịch lưu DB), GET `/api/public/ticket-types?start=...&end=...` (loại vé/giá/thời hạn), GET `/api/public/bus-routes?station=...` (tuyến và điểm dừng). Cần phân biệt lịch lưu trong DB với dữ liệu thời gian thực; không trả quan hệ Member/Ticket/Payment. Những API này **chỉ là đề xuất, chưa được tạo**.

Danh sách file và kết quả kiểm thử của lượt triển khai: [GEMINI_CHAT_FILES.md](GEMINI_CHAT_FILES.md).
