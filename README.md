# KHO LƯU TRỮ MOD MINECRAFT (MINECRAFT MODS MONOREPO)

Kho lưu trữ mã nguồn các bản mod chất lượng cao dành cho Minecraft hiện đại (Minecraft 26.3+), được xây dựng theo kiến trúc đa nền tảng (Multi-Loader) hỗ trợ đồng thời Fabric và NeoForge.

Tác giả: kha0305  
Địa chỉ kho lưu trữ: https://github.com/kha0305/mod-mine.git

---

## 1. Cấu Trúc Tổ Chức Kho Lưu Trữ

Toàn bộ các dự án mod được quản lý dưới dạng thư mục độc lập nhằm bảo đảm tính mô-đun hóa, dễ dàng mở rộng và bảo trì dài hạn:

```text
mod-mine/ (Thư mục gốc Repository)
├── .gitignore                    (Cấu hình loại trừ file rác, cache, build toàn repo)
├── README.md                     (Tài liệu tổng quan điều hướng kho lưu trữ)
│
└── MineQuality/                  (Bản mod chính: Hệ thống Phẩm chất & Độ gắn bó trang bị)
    ├── .gitignore
    ├── README.md                 (Tài liệu chi tiết riêng cho mod MineQuality)
    ├── GEMINI.md                 (Quy chuẩn kỹ thuật phát triển và điều hành mod)
    ├── NHAT_KY_HOAT_DONG.md      (Nhật ký tiến độ và lịch sử phiên làm việc)
    ├── SO_TAY_KIEM_TRA_BUG.md    (Sổ tay chẩn đoán lỗi và chống hồi quy)
    ├── THIET_KE_HE_THONG_PHAM_CHAT.md (Tài liệu đặc tả kiến trúc và cân bằng game)
    ├── build.gradle              (Cấu hình Gradle trung tâm)
    ├── settings.gradle           (Khai báo các mô-đun con)
    ├── gradle.properties         (Thông số phiên bản và môi trường Java)
    │
    ├── common/                   (90% Logic nghiệp vụ cốt lõi dùng chung)
    │   ├── src/main/java/        (Hệ thống tính toán phẩm chất, chỉ số, bảo hiểm)
    │   ├── src/main/resources/   (Tệp ngôn ngữ en_us.json, vi_vn.json)
    │   └── src/test/java/        (Bộ kiểm thử tự động chuyên sâu & Stress test)
    │
    ├── fabric/                   (Mô-đun cầu nối Fabric Loader)
    │   ├── src/main/java/        (ClientTooltip, ServerItemManager, Mixins)
    │   ├── src/main/resources/   (fabric.mod.json, mod_mine.mixins.json)
    │   └── src/test/java/        (Kiểm thử tích hợp Fabric và QA checks)
    │
    └── neoforge/                 (Mô-đun cầu nối NeoForge Loader)
        ├── src/main/java/        (NeoForge Events, Handlers)
        ├── src/main/resources/   (neoforge.mods.toml)
        └── src/test/java/        (Kiểm thử tích hợp NeoForge)
```

---

## 2. Giới Thiệu Dự Án Trọng Tâm: MineQuality

MineQuality là bản mod mở rộng trải nghiệm người chơi (Quality of Life - QoL) mang đến cơ chế Phẩm chất ngẫu nhiên (Quality Tiers), Hệ thống Điểm gắn bó (Item Affinity) và Cơ chế Rèn lại có bảo hiểm (Protected Reforging) cho toàn bộ trang bị trong Minecraft.

Bản mod được lấy cảm hứng từ các tinh hoa của Quality Tools, Tiered và Apotheosis, nhưng được tái thiết kế hoàn chỉnh để đạt tính cân bằng cao, tương thích multiplayer và an toàn tuyệt đối về mặt dữ liệu.

### Các Đặc Điểm Kỹ Thuật Nổi Bật:
1. **6 Bậc Phẩm Chất Chuẩn Mực:**
   - Phế phẩm (Junk - §8)
   - Thông thường (Common - §f)
   - Bền chắc (Uncommon - §a)
   - Hiếm có (Rare - §9)
   - Sử thi (Epic - §d)
   - Huyền thoại (Legendary - §6)
2. **Cơ Chế Thuộc Tính Hai Chiều (Bi-directional Modifiers):**
   - Chỉ số thưởng tích cực (Sát thương, Tốc độ đánh, Máu tối đa, Giáp, Độ kháng đẩy lùi, Tốc độ đào, Tốc độ di chuyển, Vận may).
   - Chỉ số phạt tiêu cực đối với Phế phẩm (Giảm sát thương, giảm độ bền, Debuff Suy yếu/Đào chậm khi cầm).
   - Chuẩn máu tối đa 40 HP (20 tim) cho trọn bộ Giáp Huyền thoại (Mũ +4 HP, Áo +6 HP, Quần +6 HP, Giày +4 HP).
3. **Thẩm Quyền Server Tuyệt Đối (Server-Authoritative):**
   - 100% dữ liệu phẩm chất do Server quản trị và lưu trữ bền vững trong `DataComponents.CUSTOM_DATA`, loại bỏ hoàn toàn hệ thống NBT cũ.
   - Tầng Client hoàn toàn Read-Only: chỉ đọc dữ liệu để kết xuất Tooltip màu sắc trực quan, triệt tiêu lỗi nhảy phẩm chất khi đồng bộ túi đồ.
   - Tự động ẩn phẩm chất ở ô xem trước chế tạo (`Quality: ??? (Craft to reveal)`).
4. **Cơ Chế Rèn Lại Lũy Tiến & Bảo Hiểm Vận May (Progressive Reforging & Pity):**
   - Vùng An Toàn (Junk, Common, Uncommon): 100% không bao giờ vỡ đồ.
   - Vùng Mạo Hiểm (Rare, Epic): Có rủi ro vỡ đồ, nhưng được giảm rủi ro theo điểm Affinity hoặc cứu sống hoàn toàn nhờ Vé Bảo Hiểm Ngọc Lục Bảo (Emerald Insurance).
   - Bảo hiểm chống tụt hạng (Keep-Highest Guarantee): Không bao giờ bị rớt phẩm chất thấp hơn phẩm chất hiện tại.
   - Tỉ lệ nâng cấp lũy tiến theo số lần đập xịt (+15% đến +25% mỗi lần) kèm mốc Pity chắc chắn 100% nâng cấp lên bậc cao hơn.
   - Hiển thị tiến trình trực quan trên Tooltip (`Pity: X/Y - Z% Upgrade Chance`).
5. **Khả Năng Tương Thích Đa Mod Vượt Trội:**
   - Sử dụng Mixin can thiệp từ lớp cha trừu tượng `ItemCombinerMenu`, bảo đảm tương thích hoàn hảo với các mod tùy biến Đe phổ biến như Easy Anvils.

---

## 3. Yêu Cầu Môi Trường & Công Nghệ

- **Minecraft:** Phiên bản mục tiêu 26.3
- **Java Virtual Machine:** Java 21 đến Java 26 (Biên dịch theo cờ `--release 21` để tương thích tối đa với OpenJDK thực tế)
- **Mod Loaders Hỗ Trợ:**
  - Fabric Loader: Phiên bản >= 0.20.1
  - NeoForge: Phiên bản tương ứng Minecraft 26.3
- **Kiến Trúc Quản Lý Dữ Liệu:** Minecraft Data Components API (`net.minecraft.core.component.DataComponents`)

---

## 4. Hướng Dẫn Biên Dịch & Kiểm Thử

### 4.1. Biên Dịch Mã Nguồn (Java CLI hoặc Gradle)
Tại thư mục `MineQuality`:

```powershell
# Biên dịch mô-đun common với bytecode Java 21
javac --release 21 -d common/build/classes (Get-ChildItem -Recurse -Filter *.java common/src/main/java | Select-Object -ExpandProperty FullName)

# Đóng gói thư viện dùng chung
jar -cvf common/build/libs/common-0.1.0.jar -C common/build/classes .

# Biên dịch mô-đun Fabric
javac --release 21 -cp "fabric/libs/*" -d fabric/build/classes (Get-ChildItem -Recurse -Filter *.java fabric/src/main/java | Select-Object -ExpandProperty FullName)
```

### 4.2. Chạy Bộ Kiểm Thử Tự Động (Test Suite)
Dự án được bảo vệ nghiêm ngặt bởi hơn 5.699 assertions kiểm thử bao phủ toàn bộ các trường hợp biên, chống tràn số nguyên, an toàn null và kiểm thử áp lực:

```powershell
# Chạy kiểm thử đơn vị logic common
java -cp "common/build/classes;common/build/test-classes" com.minequality.quality.QualityItemFilterTest
java -cp "common/build/classes;common/build/test-classes" com.minequality.quality.QualityTooltipBuilderTest
java -cp "common/build/classes;common/build/test-classes" com.minequality.quality.QualityStressBugTest

# Chạy kiểm thử tích hợp Fabric Loader
java -cp "fabric/libs/*;fabric/build/classes;fabric/build/test-classes" com.minequality.fabric.FabricIntegrationTest
java -cp "fabric/libs/*;fabric/build/classes;fabric/build/test-classes" com.minequality.fabric.FabricQACheckTest
```

---

## 5. Quy Chuẩn Kỹ Thuật Đúc Rút (Learned Engineering Rules)

Mọi đóng góp mã nguồn vào kho lưu trữ phải tuân thủ nghiêm ngặt các quy tắc đã được đúc kết từ thực tế tại [MineQuality/GEMINI.md](file:///D:/tool/mod-mine/MineQuality/GEMINI.md):
- **Tuyệt đối không dùng ký tự Emoji** trong mã nguồn, nhật ký thực thi, chuỗi giao diện hay tài liệu.
- **Thẩm quyền Server tuyệt đối:** Mọi phép toán thay đổi phẩm chất phải diễn ra ở Server. Client chỉ đọc để hiển thị.
- **Tương thích Mixin cho Container:** Khi can thiệp các khối tương tác (Anvil, Crafting Table), phải can thiệp từ lớp cha trừu tượng (`ItemCombinerMenu`) thay vì phụ thuộc duy nhất vào lớp Vanilla.
- **Nguyên tắc @Shadow trong Mixin:** Chỉ dùng `@Shadow` cho thành phần khai báo trực tiếp trên Target Class; phương thức của lớp cha bắt buộc phải gọi qua ép kiểu `((SuperClass) (Object) this).<method>()`.

---

## 6. Giấy Phép & Đóng Góp

- Bản quyền thuộc về tác giả **kha0305**.
- Mọi đóng góp, báo cáo lỗi hoặc đề xuất tính năng vui lòng tạo Issue hoặc Pull Request trực tiếp trên kho lưu trữ:
  https://github.com/kha0305/mod-mine.git
