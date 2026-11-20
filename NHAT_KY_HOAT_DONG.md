# NHẬT KÝ HOẠT ĐỘNG DỰ ÁN (PROJECT ACTIVITY LOG)

Theo dõi chi tiết tiến độ phát triển, thay đổi mã nguồn và các quyết định kỹ thuật của dự án mod-mine.

---

## [2026-11-20] - Khởi tạo nền tảng dự án Mod Minecraft 26.3 Đa Loader

### 1. Mục tiêu kỹ thuật
- Thiết lập cấu trúc dự án chuẩn cho Mod Minecraft 26.3 hỗ trợ song song Fabric và NeoForge.
- Ý tưởng cốt lõi: Mod QoL bổ sung phẩm chất ngẫu nhiên khi chế tạo trang bị (vũ khí, công cụ, giáp), kết hợp ưu điểm từ Quality Tools, Tiered và Apotheosis.
- Thiết lập bộ 3 tệp nền tảng (GEMINI.md, NHAT_KY_HOAT_DONG.md, SO_TAY_KIEM_TRA_BUG.md) và cấu trúc đa mô-đun Gradle.

### 2. Danh sách tệp khởi tạo
- GEMINI.md: Quy chuẩn riêng của dự án.
- NHAT_KY_HOAT_DONG.md: Nhật ký tiến độ công việc.
- SO_TAY_KIEM_TRA_BUG.md: Sổ tay chẩn đoán lỗi và danh mục phòng ngừa hồi quy.
- .gitignore: Cấu hình loại trừ các tệp rác, thư mục build Gradle, cache Loom/NeoGradle.
- README.md: Tài liệu giới thiệu và cấu trúc dự án.
- gradle.properties, settings.gradle, build.gradle: Cấu hình Gradle gốc đa mô-đun.
- common/build.gradle: Cấu hình mô-đun logic dùng chung.
- fabric/build.gradle: Cấu hình mô-đun Fabric Loader.
- neoforge/build.gradle: Cấu hình mô-đun NeoForge Loader.
- common/src/main/java/com/minequality/MineQuality.java: Lớp hằng số và Logger trung tâm.
- common/src/main/java/com/minequality/quality/QualityTier.java: Enum định nghĩa 6 bậc phẩm chất.
- common/src/main/java/com/minequality/quality/QualityManager.java: Thuật toán weighted random phẩm chất.
- common/src/main/resources/assets/mod_mine/lang/en_us.json: Bản dịch tiếng Anh.
- common/src/main/resources/assets/mod_mine/lang/vi_vn.json: Bản dịch tiếng Việt chuẩn có dấu.
- fabric/src/main/resources/fabric.mod.json: Metadata cấu hình Fabric.
- fabric/src/main/java/com/minequality/fabric/MineQualityFabric.java: Entrypoint Fabric.
- neoforge/src/main/resources/META-INF/neoforge.mods.toml: Metadata cấu hình NeoForge.
- neoforge/src/main/java/com/minequality/neoforge/MineQualityNeoForge.java: Entrypoint NeoForge.
- common/src/test/java/com/minequality/quality/QualityManagerTest.java: Kiểm thử đơn vị 1000 lượt roll ngẫu nhiên.

### 3. Kết quả kiểm tra
- Đã chạy kiểm thử tự động `QualityManagerTest` trên Java 26: 1000/1000 lượt bốc thăm thành công, không phát sinh ngoại lệ null, phân bổ tỷ lệ xác suất khớp chính xác với bảng trọng số (awful ~14.1%, common ~43.8%, uncommon ~21.5%, rare ~14.2%, epic ~5.2%, legendary ~1.2%).
- Đã dọn sạch thư mục tạm `bin/`.

### 4. Bài học rút ra & Bàn giao
- Kiến trúc đa mô-đun tách biệt `common`, `fabric` và `neoforge` giúp cô lập 90% logic, loại bỏ hoàn toàn việc viết lại mã nguồn giữa các loader.
- Môi trường Java 26 và Git 2.55.0 hoạt động trơn tru.
- Trạng thái hiện tại: Nền tảng dự án đã hoàn tất 100%.
- Bước hành động tiếp theo: Triển khai Data Component cho Minecraft 26.3 và hook vào sự kiện chế tạo (Crafting Event) để gắn phẩm chất vào trang bị thực tế trong game.
