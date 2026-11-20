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
- Mục tiêu phiên bản: Minecraft 26.3 (Java 26).
- Kiến trúc đa mô-đun (Multi-Loader):
  + common: Chứa 90% logic nghiệp vụ cốt lõi, hệ thống tính toán phẩm chất, đăng ký Data Component, xử lý tệp dữ liệu JSON.
  + fabric: Cầu nối riêng cho Fabric Loader (ModInitializer, Fabric API hooks).
  + neoforge: Cầu nối riêng cho NeoForge Loader (@Mod, Event Bus).
- Sử dụng Data Components: Sử dụng hệ thống Data Components của Minecraft hiện đại để lưu trữ và đồng bộ phẩm chất vật phẩm, không sử dụng NBT cũ.
- Tối ưu hóa Signal-to-Noise: Mã nguồn tinh gọn, trực diện, không tạo boilerplate dư thừa.
- An toàn tài nguyên: Luôn xử lý ngoại lệ an toàn, kiểm tra null, không để xảy ra crash game client hoặc máy chủ.
