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
