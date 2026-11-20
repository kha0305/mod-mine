# Mod MineQuality (mod-mine)

Bản mod QoL bổ sung cơ chế Phẩm chất ngẫu nhiên khi chế tạo cho Minecraft 26.3, hỗ trợ song song cả Fabric và NeoForge.

## 1. Giới Thiệu
Mod MineQuality lấy cảm hứng từ những tinh hoa của Quality Tools, Tiered và Apotheosis:
- Tự động gán phẩm chất ngẫu nhiên khi chế tạo vũ khí, công cụ và giáp.
- Phẩm chất 2 chiều: Bao gồm cả phẩm chất tích cực (tăng tốc độ đào, tăng sát thương, tăng độ bền) và phẩm chất tiêu cực (rỉ sét, mòn mẻ, giảm độ bền).
- Hiển thị trực quan qua màu sắc tên và bảng chỉ số thuộc tính (Attribute Modifiers Tooltip).
- Hỗ trợ cơ chế đúc lại phẩm chất (Reforging) bằng vật liệu tương ứng.

## 2. Kiến Trúc Kỹ Thuật
Dự án được cấu trúc theo mô hình Đa Loader (Multi-Loader Project):
- `common`: Chứa mã nguồn logic nghiệp vụ dùng chung, hệ thống dữ liệu phẩm chất và đăng ký Data Component.
- `fabric`: Mô-đun chuyển tiếp cho Fabric Loader.
- `neoforge`: Mô-đun chuyển tiếp cho NeoForge Loader.

## 3. Môi Trường Yêu Cầu
- Phiên bản Minecraft: 26.3
- Java: Java 26
- Gradle: 9.x+
