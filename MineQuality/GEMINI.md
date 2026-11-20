# QUY TẮC PHÁT TRIỂN DỰ ÁN MOD-MINE (MINECRAFT 26.3 MULTI-LOADER)

Dự án mod Minecraft QoL phẩm chất ngẫu nhiên khi chế tạo (lấy cảm hứng từ Quality Tools, Tiered, Apotheosis).
Áp dụng bắt buộc cho mọi phiên làm việc:

## 1. Nguyên Tắc Cốt Lõi
- Tuyệt đối không dùng Emoji trong toàn bộ mã nguồn, chuỗi ngôn ngữ, tệp cấu hình, nhật ký hay phản hồi trao đổi.
- 100% tiếng Việt chuẩn mực có dấu đầy đủ, đúng ngữ pháp, không viết tắt bừa bãi, không dịch ẩu.
- Tuân thủ nguyên tắc "Phân tích trước - Duyệt mới làm": Trình phương án kỹ thuật rõ ràng, người dùng duyệt mới sửa hoặc viết mã.
- Làm việc cuốn chiếu (Phased & Single-File Discipline): Trước khi tạo/sửa tệp, ghi rõ mục tiêu và kế hoạch hành động; làm xong kiểm tra đạt mới chuyển sang tệp tiếp theo.
- Sau mỗi đợt làm việc, cập nhật NHAT_KY_HOAT_DONG.md; sau mỗi lần sửa bug, cập nhật SO_TAY_KIEM_TRA_BUG.md.
- Luôn đối chiếu SO_TAY_KIEM_TRA_BUG.md trước khi công bố hoàn tất tính năng.

## 2. Tiêu Chuẩn Kỹ Thuật Dự Án
- Phiên bản mục tiêu và Runtime: Minecraft 26.3, hỗ trợ Java 21 đến Java 26. Biên dịch bằng cờ `--release 21` và khai báo `"java": ">=21"` trong metadata mod để đảm bảo tương thích rộng rãi với các máy ảo Java thực tế.
- Kiến trúc đa mô-đun (Multi-Loader):
  + common: Chứa 90% logic nghiệp vụ cốt lõi, hệ thống tính toán phẩm chất, đăng ký Data Component, xử lý tệp dữ liệu JSON.
  + fabric: Cầu nối riêng cho Fabric Loader (ModInitializer, Fabric API hooks, Mixins).
  + neoforge: Cầu nối riêng cho NeoForge Loader (@Mod, Event Bus).
- Sử dụng Data Components: Sử dụng hệ thống Data Components của Minecraft hiện đại (`DataComponents.CUSTOM_DATA`) để lưu trữ và đồng bộ phẩm chất vật phẩm, tuyệt đối không dùng hệ thống NBT cũ.
- Tối ưu hóa Signal-to-Noise: Mã nguồn tinh gọn, trực diện, không tạo boilerplate dư thừa.
- An toàn tài nguyên: Luôn xử lý ngoại lệ an toàn, kiểm tra null, không để xảy ra crash game client hoặc máy chủ.

## 3. Quy Chuẩn Kỹ Thuật Đúc Rút Từ Sửa Lỗi (Learned Engineering Rules)
- Thẩm quyền Server Tuyệt Đối (Server-Authoritative vs Client Read-Only):
  + Dữ liệu trạng thái trò chơi (sinh phẩm chất, tính toán nâng cấp, trừ tài nguyên) bắt buộc 100% do Server quản trị và lưu trữ bền vững.
  + Tầng Client hoàn toàn Read-Only: chỉ đọc dữ liệu để hiển thị giao diện và Tooltip, tuyệt đối không tự ý bốc thăm ngẫu nhiên hay sửa đổi dữ liệu ở Client nhằm triệt tiêu lỗi nhảy phẩm chất khi đồng bộ túi đồ.
  + Vật phẩm ở ô xem trước chế tạo (ResultSlot) phải luôn được ẩn phẩm chất bằng nhãn `Quality: ??? (Craft to reveal)`.
- Tương Thích Mixin Cho Container Đa Mod:
  + Khi can thiệp bằng Mixin vào các khối tương tác trong game có độ tùy biến cao từ modpack (như Đe, Bàn rèn, Bàn chế tạo, Lò nung), bắt buộc phải kiểm tra và can thiệp từ lớp cha trừu tượng của Container (`ItemCombinerMenu`, `AbstractContainerMenu`) kết hợp Accessor Mixin. Tuyệt đối không chỉ phụ thuộc vào lớp triển khai Vanilla (`AnvilMenu`) vì các mod bên thứ ba (như Easy Anvils) có thể ghi đè phương thức và không gọi super.
- Thiết Kế Rèn Lại Lũy Tiến & Bảo Hiểm Vận May (Progressive Pity & Guarantees):
  + Mọi cơ chế tiêu hao tài nguyên nâng cấp phải đi kèm xác suất lũy tiến theo số lần thất bại và có mốc bảo hiểm vận may (Pity Threshold) chắc chắn nâng cấp 100%, không dùng tỉ lệ ngẫu nhiên thuần túy gây ức chế cho người chơi.
  + Luôn phản hồi tiến trình tích lũy trực quan trên Tooltip (`Pity: X/Y`) với màu sắc phân cấp rõ ràng.
- Nguyên Tắc Sử Dụng @Shadow Trong Mixin:
  + Trong SpongePowered Mixin, chỉ dùng `@Shadow` cho các thành phần (phương thức hoặc trường) được khai báo trực tiếp trên lớp mục tiêu (Target Class).
  + Tuyệt đối không dùng `@Shadow` cho các thành viên thuộc về lớp cha của lớp mục tiêu. Bắt buộc phải tạo Mixin `@Accessor` hoặc `@Invoker` trên chính lớp cha đó để can thiệp an toàn, tránh triệt để lỗi crash runtime `InvalidMixinException`.
