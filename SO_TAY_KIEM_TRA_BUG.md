# SỔ TAY KIỂM TRA LỖI VÀ CHỐNG HỒI QUY (BUG JOURNAL & REGRESSION PREVENTION)

Sổ tay theo dõi nguyên nhân gốc rễ và biện pháp phòng ngừa các lỗi kỹ thuật phát sinh trong quá trình phát triển mod-mine.

---

## 1. Danh Mục Kiểm Tra Bắt Buộc Trước Khi Bàn Giao (Checklist)
- [ ] Không rò rỉ ngoại lệ null (NullPointerException) khi người chơi cầm vật phẩm không có phẩm chất hoặc không phải trang bị.
- [ ] Tính toán Attribute Modifiers chính xác, không cộng dồn vô hạn chỉ số mỗi khi chuyển đổi slot hoặc tải lại thế giới.
- [ ] Đồng bộ hóa dữ liệu hai chiều: Phẩm chất trên Server phải hiển thị chính xác trên Client Tooltip và ngược lại.
- [ ] Tương thích khi lưu thế giới: Không làm hỏng dữ liệu vật phẩm lưu trong thế giới (World Save / Level Data) khi nâng cấp phiên bản mod.
- [ ] Không gây xung đột sự kiện chế tạo (Crafting Event) với các mod khác hoặc các bàn chế tạo tùy biến.
- [ ] Hỗ trợ đa ngôn ngữ đầy đủ (chìa khóa dịch / translation keys) cho mọi cấp bậc và mô tả phẩm chất.
- [ ] Biên dịch Gradle thành công không có cảnh báo nghiêm trọng (Warnings/Errors) trên cả Fabric và NeoForge.

---

## 2. Nhật Ký Lỗi Đã Xử Lý (Bug Log)

*Hiện chưa phát sinh lỗi. Khi sửa bất kỳ lỗi nào, ghi chép đầy đủ theo mẫu chuẩn sau:*

### [Mã Bug] - Tên mô tả lỗi ngắn gọn
- Thời gian: YYYY-MM-DD
- Hiện tượng lỗi: Mô tả chính xác hoàn cảnh xuất hiện lỗi, hành động của người chơi hoặc log lỗi game (Crash log / Stack trace).
- Nguyên nhân gốc rễ (Root Cause): Phân tích nguyên nhân kỹ thuật cụ thể trong mã nguồn hoặc khác biệt giữa hai loader Fabric/NeoForge.
- Cách khắc phục: Chỉ rõ tệp và dòng mã được sửa đổi, phương án xử lý tối thiểu và triệt để.
- Cách phòng ngừa: Biện pháp hoặc quy tắc kiểm thử để không tái diễn lỗi.
