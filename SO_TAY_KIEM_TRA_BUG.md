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

### [BUG-001] - Tràn số nguyên (Integer Overflow) trong QualityItemFilter.calculateReforgeCost
- Thời gian: 2026-11-20
- Hiện tượng lỗi: Khi số lần rèn tích lũy lớn (ví dụ `reforgeCount` >= 1.5 tỷ hoặc `Integer.MAX_VALUE`), chi phí EXP tính ra bị âm (ví dụ -1,294,967,293 cấp), gây lỗi giao diện Đe Vanilla hoặc kẽ hở gian lận cấp độ kinh nghiệm.
- Nguyên nhân gốc rễ (Root Cause): Biểu thức `safeCount * 2` thực hiện phép nhân trên số nguyên 32-bit có dấu. Khi `safeCount` lớn, kết quả bị tràn số học thành số âm trước khi gọi hàm `Math.min(MAX_EXP_COST, calculated)`.
- Cách khắc phục: Trong [QualityItemFilter.java](file:///D:/tool/mod-mine/common/src/main/java/com/minequality/quality/QualityItemFilter.java#L130-L135), kiểm tra sớm điều kiện trần: nếu `safeCount >= MAX_EXP_COST` thì lập tức trả về `MAX_EXP_COST` (30 cấp), không thực hiện phép nhân.
- Cách phòng ngừa: Thêm bài kiểm thử áp lực số nguyên lớn (`Integer.MAX_VALUE` và 1.5 tỷ) trong bộ `QualityStressBugTest.java`.

### [BUG-002] - Logic kích hoạt Cứu Sinh Khẩn Cấp khi thực thể đã chết hoặc máu âm
- Thời gian: 2026-11-20
- Hiện tượng lỗi: Hàm `SpecialEffectHandler.canTriggerEmergencyRecovery` trả về `true` khi `currentHealth <= 0` (máu bằng 0 hoặc âm), dẫn tới việc kích hoạt hiệu ứng hồi phục và kháng cự cho người chơi đã chết, có thể gây bất đồng bộ giao diện hồi sinh.
- Nguyên nhân gốc rễ (Root Cause): Hàm chỉ kiểm tra `currentHealth / maxHealth < 0.30` mà không kiểm tra cận dưới `currentHealth > 0`. Khi máu bằng 0 hoặc âm, tỉ lệ luôn < 0.30 nên điều kiện bị thỏa mãn sai.
- Cách khắc phục: Bổ sung điều kiện kiểm tra `currentHealth > 0 && !Double.isNaN(currentHealth) && !Double.isNaN(maxHealth)` trong [SpecialEffectHandler.java](file:///D:/tool/mod-mine/common/src/main/java/com/minequality/quality/SpecialEffectHandler.java#L47-L60). Đồng thời hỗ trợ `lastTriggerTick < 0` để người chơi sẵn sàng hiệu ứng ngay lần đầu tiên trong thế giới.
- Cách phòng ngừa: Thêm bài kiểm tra trường hợp biên `currentHealth = 0.0` và `-5.0` trong bộ `QualityStressBugTest.java`.

### [BUG-003] - Sai lệch tên gọi phương thức rèn trong FabricAnvilHandler và thiếu thư viện ghi nhật ký khi biên dịch độc lập
- Thời gian: 2026-11-20
- Hiện tượng lỗi: Trình biên dịch `javac` báo lỗi `cannot find symbol: method reforge(boolean)` tại [FabricAnvilHandler.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/event/FabricAnvilHandler.java#L151) và `package org.slf4j does not exist` khi biên dịch mô-đun Fabric độc lập.
- Nguyên nhân gốc rễ (Root Cause): Lớp [ItemQualityData.java](file:///D:/tool/mod-mine/common/src/main/java/com/minequality/quality/ItemQualityData.java) ban đầu chỉ khai báo phương thức `applyReforge(boolean)` nhưng trong `FabricAnvilHandler.java` lại gọi `currentData.reforge(...)`. Đồng thời lớp [MineQuality.java](file:///D:/tool/mod-mine/common/src/main/java/com/minequality/MineQuality.java) phụ thuộc vào SLF4J nhưng môi trường `javac` thuần dòng lệnh không có sẵn classpath ngoài.
- Cách khắc phục:
  + Trong [FabricAnvilHandler.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/event/FabricAnvilHandler.java#L151), sửa thành `currentData.applyReforge(hasEmeraldInsurance)`.
  + Bổ sung phương thức đồng bộ `reforge(boolean)` trong [ItemQualityData.java](file:///D:/tool/mod-mine/common/src/main/java/com/minequality/quality/ItemQualityData.java#L175-L181) để hỗ trợ tương thích hai chiều.
  + Khởi tạo giao diện Logger và LoggerFactory chuẩn SLF4J nội bộ tại [Logger.java](file:///D:/tool/mod-mine/common/src/main/java/org/slf4j/Logger.java) và [LoggerFactory.java](file:///D:/tool/mod-mine/common/src/main/java/org/slf4j/LoggerFactory.java) phục vụ biên dịch độc lập zero-dependency.
- Cách phòng ngừa: Kiểm tra đối chiếu signature các phương thức public giữa các mô-đun trước khi hoàn tất và bổ sung kiểm thử biên dịch chéo tự động.


---

### [BUG-004] - Lỗi Xung Đột Phiên Bản Java Runtime Trên Fabric Loader (Incompatible mods found)
- Thời gian: 2026-11-20
- Hiện tượng lỗi: Khởi động game Minecraft Fabric 26.3 qua Prism Launcher bị dừng với thông báo lỗi:
  `Mod 'MineQuality' (mod_mine) 0.1.0 requires version 26 or later of 'OpenJDK 64-Bit Server VM' (java), but only the wrong version is present: 25!`
- Nguyên nhân gốc rễ (Root Cause):
  + Tệp cấu hình `fabric/src/main/resources/fabric.mod.json` khai báo cứng ràng buộc `"java": ">=26"`.
  + Môi trường thực tế của instance Prism Launcher đang chạy phiên bản OpenJDK 25.
  + Bytecode được biên dịch nhắm tới Java 26 thay vì tương thích ngược với các runtime LTS/phổ biến.
- Cách khắc phục:
  + Chỉnh sửa `fabric/src/main/resources/fabric.mod.json` tại dòng 24: đổi `"java": ">=26"` thành `"java": ">=21"`.
  + Biên dịch lại toàn bộ mã nguồn với cờ `--release 21` bằng JDK 26 để tạo bytecode tương thích từ Java 21 đến Java 26.
  + Tự động đóng gói lại và cập nhật tệp `mod_mine-fabric-0.1.0.jar` vào thư mục mods của Prism Launcher tại `C:\Users\ASUS\AppData\Roaming\PrismLauncher\instances\Fabric 26.3\minecraft\mods\`.
- Cách phòng ngừa:
  + Luôn sử dụng `--release 21` cho các dự án Minecraft hiện đại nếu mã nguồn không sử dụng tính năng chỉ có riêng ở các bản Java mới hơn.
  + Khai báo ràng buộc Java trong `fabric.mod.json` ở mức tối thiểu tương thích (`>=21`) thay vì ép người dùng phải dùng đúng bản Java mới nhất.


---

### [BUG-005] - Lỗi Thiếu Giao Diện net.fabricmc.api.ModInitializer Trên Entrypoint Fabric
- Thời gian: 2026-11-20
- Hiện tượng lỗi: Khởi động game Minecraft trên Fabric Loader bị crash với lỗi:
  `Caused by: net.fabricmc.loader.api.LanguageAdapterException: Class com.minequality.fabric.MineQualityFabric cannot be cast to net.fabricmc.api.ModInitializer!`
- Nguyên nhân gốc rễ (Root Cause):
  + Lớp entrypoint `com.minequality.fabric.MineQualityFabric` khai báo phương thức `onInitialize()` nhưng thiếu từ khóa `implements net.fabricmc.api.ModInitializer`.
  + Fabric Loader khi kích hoạt stage `main` thực hiện ép kiểu đối tượng entrypoint sang `ModInitializer`. Do không kế thừa giao diện, JVM ném ngoại lệ `ClassCastException`.
- Cách khắc phục:
  + Bổ sung thư viện `fabric-loader-0.20.1.jar` vào classpath biên dịch tại `fabric/libs/`.
  + Khai báo `implements ModInitializer` và thêm `@Override` cho phương thức `onInitialize()` trong [MineQualityFabric.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/MineQualityFabric.java).
  + Biên dịch lại bằng `--release 21`, đóng gói lại JAR và triển khai ngay vào thư mục mods của Prism Launcher tại `C:\Users\ASUS\AppData\Roaming\PrismLauncher\instances\Fabric 26.3\minecraft\mods\mod_mine-fabric-0.1.0.jar`.
- Cách phòng ngừa:
  + Mọi lớp entrypoint của Fabric bắt buộc phải `implements net.fabricmc.api.ModInitializer` (hoặc `ClientModInitializer` cho client, `DedicatedServerModInitializer` cho server).
  + Thêm quy tắc kiểm tra bytecode bằng `javap` hoặc reflection trong bộ kiểm thử CI để đảm bảo lớp kế thừa đúng giao diện Fabric trước khi phát hành.


---

### [BUG-006] - Mod Không Hoạt Động Trong Game Thực Tế (Không Hiện Phẩm Chất & Tooltip Trống)
- Thời gian: 2026-11-20
- Hiện tượng lỗi: Khởi động game thành công nhưng khi người chơi chế tạo Cúp Sắt (Iron Pickaxe) hoặc rê chuột xem trang bị thì Tooltip chỉ hiển thị thông tin mặc định của Minecraft Vanilla, hoàn toàn không có dòng phẩm chất hay chỉ số mở rộng của mod.
- Nguyên nhân gốc rễ (Root Cause):
  + [FabricTooltipHandler.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/event/FabricTooltipHandler.java) ban đầu chỉ là mock POJO (chỉ gán cờ `registered = true`), chưa kết nối vào Fabric API client thực tế `net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT`.
  + Tệp cấu hình [fabric.mod.json](file:///D:/tool/mod-mine/fabric/src/main/resources/fabric.mod.json) thiếu entrypoint client `"client": ["com.minequality.fabric.client.MineQualityFabricClient"]`.
  + Thiếu cơ chế tự động bốc thăm và lưu trữ phẩm chất (On-Demand Roll & Save) cho các trang bị vừa được chế tạo hoặc lấy từ kho đồ vào thành phần `DataComponents.CUSTOM_DATA`.
- Cách khắc phục:
  + Tạo mới lớp [MineQualityFabricClient.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/client/MineQualityFabricClient.java) triển khai `ClientModInitializer`.
  + Triển khai logic thực tế trong [FabricTooltipHandler.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/event/FabricTooltipHandler.java): Đăng ký `ItemTooltipCallback.EVENT`, tự động bốc thăm `QualityManager.rollQuality()` khi trang bị đủ điều kiện (`QualityItemFilter.isEligible`) chưa có dữ liệu và lưu an toàn vào `DataComponents.CUSTOM_DATA`.
  + Bổ sung bộ định dạng mã màu sắc Minecraft chuẩn (`§6`, `§d`, `§9`, `§a`, `§f`, `§8`, `§2`, `§c`, `§b`) và chèn dòng Component vào Tooltip ngay dưới tên trang bị.
  + Khai báo entrypoint `"client"` trong [fabric.mod.json](file:///D:/tool/mod-mine/fabric/src/main/resources/fabric.mod.json).
  + Biên dịch lại bằng `--release 21`, đóng gói thành `mod_mine-fabric-0.1.0.jar` (50.955 bytes) và triển khai ngay vào Prism Launcher.
- Cách phòng ngừa:
  + Luôn tích hợp trực tiếp với API sự kiện thực tế của Loader (`ItemTooltipCallback` cho Fabric, `ItemTooltipEvent` cho NeoForge/Forge).
  + Thiết kế cơ chế On-Demand Roll phòng thủ để đảm bảo bất kỳ trang bị hợp lệ nào xuất hiện trên màn hình người chơi đều tự động sở hữu phẩm chất.


---

### [BUG-007] - Ô Chế Tạo Lộ Phẩm Chất & Phẩm Chất Bị Random Lại Liên Tục Khi Lấy Đồ / Đồng Bộ Túi Đồ
- Thời gian: 2026-11-20
- Hiện tượng lỗi:
  + Khi xếp nguyên liệu trên bàn chế tạo, ô kết quả (Result Slot) không ẩn phẩm chất mà hiển thị lộ kết quả (ví dụ Junk).
  + Khi người chơi click lấy cúp ra khỏi bàn chế tạo vào túi đồ, phẩm chất bị random lại thành một phẩm chất khác (từ Junk thành Uncommon).
  + Toàn bộ các trang bị khác đang có trong túi đồ cũng bị random phẩm chất lung tung mỗi khi di chuyển hoặc đồng bộ.
- Nguyên nhân gốc rễ (Root Cause):
  + Tầng Client ([FabricTooltipHandler.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/event/FabricTooltipHandler.java)) trước đó tự ý gọi `QualityManager.rollQuality()` khi thấy thiếu thẻ `minequality`.
  + Vật phẩm ở ô kết quả chế tạo chỉ là bản xem trước tạm thời (Preview ItemStack). Khi người chơi click lấy ra, Server gửi một `ItemStack` mới xuống Client, khiến Client tiếp tục bốc thăm lần 2.
  + Do Client không thể lưu trữ bền vững lên Server, mỗi lần Server gửi gói tin đồng bộ vị trí slot, Client lại coi đó là vật phẩm mới và bốc thăm lại từ đầu, làm phẩm chất nhảy loạn xạ trên toàn bộ trang bị.
- Cách khắc phục:
  + Chuyển 100% quyền sinh và lưu trữ phẩm chất về phía Server (Server-Authoritative):
    - Tạo mới [FabricServerItemManager.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/event/FabricServerItemManager.java) lắng nghe `ServerTickEvents.END_SERVER_TICK`.
    - Server kiểm tra túi đồ (`player.getInventory()`) và con trỏ chuột (`containerMenu.getCarried()`): Bốc thăm duy nhất 1 lần khi trang bị hợp lệ chưa có thẻ `minequality`, lưu trực tiếp vào `DataComponents.CUSTOM_DATA`.
    - Một khi đã có `minequality`, Server bảo lưu nguyên vẹn 100%, tuyệt đối không bao giờ roll lại.
    - Loại trừ hoàn toàn ô kết quả chế tạo `ResultSlot` (`slot instanceof ResultSlot`), không bao giờ gán dữ liệu cho vật phẩm đang xem trước.
  + Phía Client ([FabricTooltipHandler.java](file:///D:/tool/mod-mine/fabric/src/main/java/com/minequality/fabric/event/FabricTooltipHandler.java)):
    - Chuyển sang chế độ Read-Only hoàn toàn thông qua phương thức `readQualityDataOnly(stack)`. Tuyệt đối không tự ý bốc thăm ngẫu nhiên ở Client.
    - Khi vật phẩm chưa có phẩm chất từ Server (đang ở ô kết quả chế tạo), hiển thị: `§7§oQuality: ??? (Craft to reveal)` và không hiển thị chỉ số để giấu kín phẩm chất trước khi lấy ra.
- Cách phòng ngừa:
  + Quy tắc bất di bất dịch trong lập trình Mod Multiplayer/Singleplayer Minecraft: *"Dữ liệu trạng thái trò chơi (Gameplay Data/State) bắt buộc phải do Server quản lý và ghi nhận; Client chỉ được phép đọc và hiển thị (Read-Only Rendering), tuyệt đối không được tự ý bốc thăm ngẫu nhiên ở tầng View/Tooltip."*
