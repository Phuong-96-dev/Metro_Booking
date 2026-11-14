# Quét mã vé Metro

Chức năng tạo QR/Code 128 được nối với luồng thanh toán hiện tại. Khi thanh toán thành công, hệ thống tạo một mã quét duy nhất cho vé. Thành viên xem mã tại **Vé của tôi**; tài khoản `ADMIN` mở **Quét mã vé** trong thanh bên để quét bằng camera, nhập mã hoặc tải ảnh.

## Chuẩn bị database

- Database đang sử dụng: sao lưu rồi chạy `docs/database/V002__ticket_scan.sql` trên database `metro`.
- Database mới, trống: `docs/database/V000__initial_schema.sql` đã gồm các bảng quét mã; sau đó tiếp tục các migration khác như hướng dẫn xác thực và xác minh sinh viên.
- Ứng dụng giữ `spring.jpa.hibernate.ddl-auto=validate`, vì vậy migration phải hoàn tất trước khi khởi động.

## Cách dùng

1. Thành viên mua và thanh toán vé. Mã QR/barcode được tạo trong cùng giao dịch thanh toán.
2. Thành viên vào `/myTicket` và chọn **Xem mã vé**.
3. Admin vào `/scanner`. Camera trình duyệt chỉ hoạt động trên `localhost` hoặc HTTPS; nhập mã và tải ảnh vẫn dùng được khi không có camera.
4. Lần quét hợp lệ đầu tiên trả về `SUCCESS`; quét lại trả về `ALREADY_USED`. Vé quá hạn, bị hủy, sai định dạng và không tồn tại đều được ghi vào lịch sử quét.

Các endpoint quét chỉ dành cho `ADMIN`. API xem ảnh mã yêu cầu đăng nhập và backend kiểm tra đúng chủ vé (admin được phép hỗ trợ xem).
