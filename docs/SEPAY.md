# Thanh toán SePay

Ứng dụng tạo VietQR theo từng vé, chờ webhook tiền vào từ SePay, chống xử lý
trùng giao dịch và chỉ kích hoạt/cấp mã quét cho vé sau khi nhận đủ tiền.

## 1. Cập nhật database

Với database `metro` hiện có, chạy:

```text
docs/database/V003__sepay_payment.sql
```

Database mới tạo bằng `CreateMetroDatabase.sql` đã có sẵn các cột SePay.

## 2. Cấu hình `.env`

```properties
SEPAY_ENABLED=true
SEPAY_BANK=Vietcombank
SEPAY_ACCOUNT_NUMBER=so-tai-khoan-nhan-tien
SEPAY_ACCOUNT_HOLDER=TEN CHU TAI KHOAN KHONG DAU
SEPAY_PAYMENT_PREFIX=METRO
SEPAY_TRANSFER_PREFIX=
SEPAY_WEBHOOK_AUTH_MODE=HMAC
SEPAY_WEBHOOK_SECRET=mot-chuoi-bi-mat-dai-va-ngau-nhien
SEPAY_WEBHOOK_MAX_AGE_SECONDS=300
```

`SEPAY_BANK` phải là short name/alias/bin được VietQR hỗ trợ. Nếu dùng
VietinBank cá nhân/hộ kinh doanh, đặt `SEPAY_TRANSFER_PREFIX=SEVQR`. Nếu dùng
VA dạng nội dung, đặt giá trị `TKP` kèm mã VA theo cấu hình ngân hàng.

Có thể dùng API Key để thử nhanh bằng cách đổi:

```properties
SEPAY_WEBHOOK_AUTH_MODE=API_KEY
SEPAY_API_KEY=api-key-da-cau-hinh-tren-sepay
```

HMAC được khuyến nghị cho production vì xác minh cả nguồn gửi và nội dung
payload. Không commit `.env`, secret hoặc thông tin đăng nhập SePay.

## 3. Tạo webhook trên SePay

Trong `my.sepay.vn` mở **Tích hợp → Webhooks → Thêm webhook** và cấu hình:

- URL: `https://ten-mien-cua-ban/api/webhooks/sepay`
- Sự kiện: **Có tiền vào**
- Định dạng: `application/json`
- Tài khoản: tài khoản nhận tiền đã cấu hình ở `.env`
- Tiền tố mã thanh toán: `METRO`
- Bỏ qua giao dịch không có mã thanh toán: bật
- Xác thực: **HMAC-SHA256**, secret phải giống `SEPAY_WEBHOOK_SECRET`
- Tự động gửi lại khi server trả lỗi: bật

Webhook production cần URL HTTPS công khai. Khi chạy local, dùng tunnel HTTPS
như ngrok hoặc Cloudflare Tunnel rồi nhập URL tunnel vào SePay.

Với ứng dụng chạy ở cổng mặc định `8080`, mở một terminal khác và chạy:

```text
ngrok http 8080
```

Sau đó lấy địa chỉ HTTPS mà ngrok hiển thị và nối đúng đường dẫn, ví dụ:

```text
https://abc123.ngrok-free.app/api/webhooks/sepay
```

Không dùng `/payment`, `/payment/success`, URL HTTP hoặc URL có bước chuyển
hướng đăng nhập làm webhook.

Ứng dụng trả HTTP `201` và body `{"success":true}` cho webhook hợp lệ. Transaction
ID của SePay được lưu bằng unique index nên retry/replay không thể kích hoạt vé hai
lần. Webhook quá 5 phút hoặc sai chữ ký bị từ chối.

## 4. Kiểm thử

1. Đăng nhập và mua một vé.
2. Trang thanh toán hiển thị QR, số tiền và nội dung `METRO...`.
3. Dùng tính năng **Gửi thử** trong SePay hoặc chuyển khoản thật đúng số tiền.
4. Sau khi webhook đến, trang tự chuyển sang **Thanh toán thành công** và mã vé
   quét được cấp trong **Vé của tôi**.

Nút **Gửi thử** có thể dùng dữ liệu mẫu không khớp đơn hàng thật. Khi đó server
vẫn ACK `201` để SePay không gửi lại vô hạn, nhưng không đổi payment sang `PAID`.

Tài liệu chính thức:

- https://developer.sepay.vn/vi/sepay-webhooks/tich-hop-webhook
- https://developer.sepay.vn/vi/sepay-webhooks/xac-thuc
- https://developer.sepay.vn/en/tien-ich-khac/tao-qr-code
