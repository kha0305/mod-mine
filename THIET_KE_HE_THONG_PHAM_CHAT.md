# TÀI LIỆU THIẾT KẾ HỆ THỐNG PHẨM CHẤT TRANG BỊ (MINEQUALITY SPECIFICATION)

Tài liệu đặc tả chi tiết toàn bộ cơ chế, chỉ số, thuật toán và logic nghiệp vụ của hệ thống phẩm chất vật phẩm MineQuality. Tài liệu làm kim chỉ nam để lập trình viên triển khai mã nguồn trên các nền tảng Mod (Fabric, NeoForge, Forge) hoặc Server Plugin (Paper, Purpur).

---

## 1. TỔNG QUAN Ý TƯỞNG CỐT LÕI
- **Cơ chế chính:** Mọi vật phẩm trang bị khi được chế tạo (Crafting) hoặc nhận được trong thế giới sẽ được bốc thăm ngẫu nhiên một bậc phẩm chất.
- **Tác động:** Phẩm chất quyết định trực tiếp đến chỉ số thưởng/phạt thuộc tính (Attribute Modifiers), độ bền tối đa, các hiệu ứng nội tại khi chiến đấu/sử dụng, và khả năng tồn tại trong dung nham (Fireproof).
- **Tính đối kháng hai chiều:** Đồ xịn mang lại sức mạnh vượt trội; đồ xấu mang lại bất lợi và thúc đẩy người chơi mang ra Đe (Anvil) để rèn lại.
- **Ngôn ngữ hiển thị in-game:** 100% tiếng Anh chuẩn quốc tế (`en_us`).

---

## 2. HỆ THỐNG 6 BẬC PHẨM CHẤT (QUALITY TIERS)

| Bậc Phẩm Chất | Tên Mã (ID) | Màu Sắc Tên | Mã Màu Hex | Trọng Số Bốc Thăm | Ý Nghĩa / Đặc Điểm |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Junk** | `junk` | Xám (Gray) | `#AAAAAA` | 15% | Phế phẩm. Bị phạt chỉ số, giảm độ bền, dính hiệu ứng bất lợi (Weakness, Mining Fatigue), cháy trong dung nham. |
| **Common** | `common` | Trắng (White) | `#FFFFFF` | 45% | Phổ thông. Giữ nguyên 100% chỉ số gốc của Minecraft Vanilla. |
| **Uncommon** | `uncommon` | Xanh lá (Green) | `#55FF55` | 22% | Bền chắc. Thưởng nhẹ chỉ số, tăng độ bền, an toàn tuyệt đối khi rèn. |
| **Rare** | `rare` | Xanh lam (Aqua) | `#55FFFF` | 12% | Hiếm. Thưởng chỉ số khá, tăng độ bền, bắt đầu bước vào mốc rèn có rủi ro vỡ đồ. |
| **Epic** | `epic` | Tím (Dark Purple) | `#AA00AA` | 5% | Sử thi. Thưởng chỉ số cao, hiệu ứng mạnh, rủi ro vỡ đồ cao khi rèn. |
| **Legendary** | `legendary` | Cam vàng (Gold) | `#FFAA00` | 1% | Huyền thoại. Thần khí tối thượng, sở hữu các đặc quyền độc bản Unbreakable và Fireproof. |

---

## 3. NGUYÊN TẮC TÍNH TOÁN CHỈ SỐ & LÀM TRÒN

### 3.1. Phép Toán Thuộc Tính (Attribute Operation)
- Toàn bộ chỉ số phần trăm thưởng/phạt áp dụng phép toán `MULTIPLY_BASE` (tính dựa trên chỉ số gốc của vật phẩm trong Minecraft).
- **Lý do:** Giữ nguyên giá trị phân cấp của vật liệu (đồ sắt tăng vừa phải, đồ kim cương/netherite tăng mạnh), và hoàn toàn độc lập với bùa phép (Enchantments), ngăn chặn hiện tượng lạm phát chỉ số theo cấp số nhân.

### 3.2. Quy Tắc Làm Tròn Độ Bền (Max Durability)
- Bậc **Junk (-20%):** Làm tròn xuống (`Math.floor`).
- Bậc **Uncommon (+10%), Rare (+20%), Epic (+35%):** Làm tròn lên (`Math.ceil`) để có lợi cho người chơi.
- Bậc **Legendary:** Gán cờ `minecraft:unbreakable` trong Data Component, loại bỏ hoàn toàn thanh độ bền.

---

## 4. CHI TIẾT CHỈ SỐ CHO 6 NHÓM TRANG BỊ

### 4.1. Nhóm Vũ Khí Cận Chiến (Melee Weapons: Kiếm, Rìu)
- **Junk:** -15% Attack Damage, -10% Attack Speed, -20% Max Durability. Khi cầm bị dính hiệu ứng phụ Weakness I.
- **Common:** Giữ nguyên chỉ số gốc.
- **Uncommon:** +5% Attack Damage, +10% Max Durability.
- **Rare:** +10% Attack Damage, +10% Attack Speed, +20% Max Durability.
- **Epic:** +20% Attack Damage, +15% Attack Speed, +5% Movement Speed, +35% Max Durability.
- **Legendary:** 
  - +35% Attack Damage, +25% Attack Speed, +10% Movement Speed, +1 Luck.
  - **Unbreakable** & **Fireproof**.
  - **Đặc quyền kích hoạt:** 20% Hút máu (Lifesteal: Hồi lại 20% lượng sát thương gây ra cho mục tiêu).

### 4.2. Nhóm Công Cụ Khai Thác (Mining Tools: Cúp, Xẻng, Cuốc)
- **Junk:** -15% Mining Speed, -10% Attack Speed, -20% Max Durability. Khi cầm bị dính hiệu ứng Mining Fatigue I.
- **Common:** Giữ nguyên chỉ số gốc.
- **Uncommon:** +5% Mining Speed, +10% Max Durability.
- **Rare:** +10% Mining Speed, +10% Attack Speed, +20% Max Durability.
- **Epic:** +20% Mining Speed, +15% Attack Speed, +5% Movement Speed, +35% Max Durability.
- **Legendary:** 
  - +35% Mining Speed, +25% Attack Speed, +10% Movement Speed, +1 Luck.
  - **Unbreakable** & **Fireproof**.
  - **Đặc quyền nội tại:** Nhận hiệu ứng Đào nhanh II (Haste II) liên tục khi cầm trên tay chính.

### 4.3. Nhóm Vũ Khí Tầm Xa (Ranged Weapons: Cung Tên, Nỏ)
- **Junk:** -15% Projectile Damage, -20% Max Durability.
- **Common:** Giữ nguyên chỉ số gốc.
- **Uncommon:** +5% Projectile Damage, +10% Max Durability.
- **Rare:** +10% Projectile Damage, +5% Movement Speed, +20% Max Durability.
- **Epic:** +20% Projectile Damage, +10% Movement Speed, +35% Max Durability.
- **Legendary:** 
  - +35% Projectile Damage, +15% Movement Speed, +1 Luck.
  - **Unbreakable** & **Fireproof**.
  - Tên bắn ra bay thẳng hơn và nhanh hơn 25%.

### 4.4. Nhóm Giáp Bảo Hộ (Armor: Mũ, Áo Giáp, Quần, Giày) - Chuẩn Máu Cứng 40 HP
Lượng máu gốc của người chơi là 20 HP (10 tim đỏ). Mặc trọn bộ 4 món giáp Legendary cộng đúng +20 HP để đạt mốc tròn 40 HP (20 tim đỏ - 2 hàng tim).

- **Junk:** 
  - Mũ: -1 HP, -0.5 Armor, -20% Durability.
  - Áo: -2 HP, -1.0 Armor, -20% Durability.
  - Quần: -2 HP, -1.0 Armor, -20% Durability.
  - Giày: -1 HP, -0.5 Armor, -20% Durability.
  - *Tổng cả bộ phạt: -6 HP (người chơi còn 14 HP - 7 tim).*
- **Common:** Giữ nguyên chỉ số gốc.
- **Uncommon:** Mỗi món cộng +1 HP, +10% Durability *(Tổng cả bộ cộng +4 HP -> 24 HP - 12 tim).*
- **Rare:** Mỗi món cộng +2 HP, +0.5 Armor, +0.5 Toughness, +20% Durability *(Tổng cả bộ cộng +8 HP -> 28 HP - 14 tim).*
- **Epic:** Mũ/Giày +3 HP, Áo/Quần +4 HP, +1.0 Armor, +1.0 Toughness, +0.03 Knockback Resistance, +35% Durability *(Tổng cả bộ cộng +14 HP -> 34 HP - 17 tim).*
- **Legendary:** 
  - Mũ: +4 HP, +1.5 Armor, +2.0 Toughness, +0.05 Knockback Resistance.
  - Áo: +6 HP, +2.0 Armor, +2.0 Toughness, +0.05 Knockback Resistance.
  - Quần: +6 HP, +2.0 Armor, +2.0 Toughness, +0.05 Knockback Resistance.
  - Giày: +4 HP, +1.5 Armor, +2.0 Toughness, +0.05 Knockback Resistance.
  - *Tổng cả bộ cộng đúng +20 HP -> Đạt mốc tròn 40 HP (20 tim đỏ - 2 hàng tim hoàn chỉnh).*
  - **Unbreakable** & **Fireproof**.
  - **Hiệu ứng Bộ Trang Bị (Full Set Bonus):** Miễn nhiễm lửa vĩnh viễn (Permanent Fire Resistance). Khi lượng máu tụt xuống dưới 30% (< 12 HP), tự động kích hoạt hiệu ứng sinh tồn khẩn cấp `Resistance I + Regeneration I` trong 10 giây (thời gian hồi chiêu 60 giây).

### 4.5. Nhóm Khiên Chắn (Shields)
- **Junk:** -1.0 Armor Toughness, -20% Max Durability.
- **Common:** Giữ nguyên chỉ số gốc.
- **Uncommon:** +10% Max Durability.
- **Rare:** +1.0 Armor Toughness, +20% Max Durability.
- **Epic:** +1.0 Armor, +1.5 Armor Toughness, +10% Knockback Resistance, +35% Max Durability.
- **Legendary:** 
  - +2.0 Armor, +2.0 Armor Toughness, +20% Knockback Resistance.
  - **Unbreakable** & **Fireproof**.
  - **Đặc quyền khi đỡ đòn:** Nhận hiệu ứng Kháng cự II (Resistance II) trong 3 giây khi đỡ thành công một đòn đánh cực mạnh.

### 4.6. Nhóm Cánh Cứng (Elytra)
- **Junk:** -20% Max Durability.
- **Common:** Giữ nguyên chỉ số gốc.
- **Uncommon:** +10% Max Durability.
- **Rare:** +1.0 Armor, +20% Max Durability.
- **Epic:** +2.0 Armor, +1.0 Armor Toughness, +5% Movement Speed, +35% Max Durability.
- **Legendary:** 
  - +3.0 Armor, +2.0 Armor Toughness, +10% Movement Speed.
  - **Unbreakable** & **Fireproof**. Bay lượn vĩnh cửu không lo rách cánh.

---

## 5. QUY TẮC ĐẶC BIỆT CHO ĐINH BA (TRIDENT) & CHÙY (MACE)

### 5.1. Chùy (Mace - Minecraft 1.21+)
- Chùy có cơ chế rơi đập (Smash Attack) độc nhất vô nhị. Phẩm chất tác động trực tiếp vào cơ chế này:
  - **Rare:** Tăng 15% bán kính văng chấn động (AoE Shockwave).
  - **Epic:** Tăng 30% bán kính văng chấn động, giảm độ cao rơi tối thiểu cần thiết để kích hoạt Smash Attack.
  - **Legendary:** Tăng 50% sát thương rơi đập + **Miễn nhiễm 100% sát thương rơi (Zero Fall Damage)** khi đang cầm Chùy trên tay và đáp trúng mục tiêu.

### 5.2. Đinh Ba (Trident)
- Đinh ba là vũ khí lai giữa cận chiến, ném tầm xa và di chuyển phóng người (Riptide):
  - **Rare:** Tăng 25% tốc độ bay khi ném.
  - **Epic:** Tăng 50% tốc độ bay khi ném, tốc độ bùa Dẫn hướng (Loyalty) bay ngược về tay người chơi nhanh gấp 2 lần.
  - **Legendary:**
    - Tích hợp sẵn bùa Loyalty I ngầm (ném đi tự động bay về kể cả chưa ép sách).
    - Khi ở dưới nước hoặc khi trời mưa: Tăng 25% cự ly lướt của bùa Phóng (Riptide) và tăng thêm 15% sát thương cận chiến.

---

## 6. HỆ THỐNG ĐỘ GẮN BÓ (ITEM BOND / AFFINITY)

- **Thang điểm:** Từ **0 đến 100 điểm** (tương đương 0% đến 100%).
- **Cách tích lũy tự nhiên:**
  - Cận chiến / Tầm xa: Gây sát thương và tiêu diệt quái vật.
  - Công cụ đào: Khai thác khối tài nguyên.
  - Giáp / Khiên: Hấp thụ sát thương bảo vệ chủ nhân.
- **Các mốc danh hiệu:**
  - `0 - 24`: Stranger (Xa lạ)
  - `25 - 49`: Acquaintance (Quen thuộc)
  - `50 - 74`: Trusted Companion (Bạn đồng hành)
  - `75 - 99`: Soulbound (Gắn kết linh hồn)
  - `100/100`: Kindred Spirits (Tâm đầu ý hợp)
- **Tác dụng:**
  - Tăng tỉ lệ kích hoạt các hiệu ứng đặc biệt (Proc Chance).
  - Kéo dài thời gian hiệu ứng thêm 50% ở mốc 100/100.
  - **Giảm trực tiếp 15% tỉ lệ tạch** khi mang trang bị lên Đe để rèn lại.

---

## 7. HỆ THỐNG RÈN LẠI (REFORGE) & CƠ CHẾ VỠ ĐỒ

### 7.1. Cách Thức Rèn Lại Tại Đe (Anvil)
- **Ô 1 (Bên trái):** Trang bị cần rèn lại.
- **Ô 2 (Bên phải):** 1 Nguyên liệu gốc tương ứng (Kim cương cho đồ kim cương, Thỏi sắt cho đồ sắt, Màng da ma cà rồng cho Elytra...).
- **Chi phí EXP:** Tính theo công thức tăng dần theo số lần thất bại:
  $$\text{EXP Level Cost} = \min\left(30, \text{baseCost} + (\text{reforgeCount} \times 2)\right)$$
  - `baseCost`: Đồ da = 1, Đồ sắt/vàng = 2, Đồ kim cương = 3, Đồ Netherite/Elytra/Chùy = 4.
  - **Mức trần bảo vệ 30 cấp:** Tuyệt đối không để chi phí vượt quá 39 cấp độ để không bao giờ bị dính lỗi khóa cứng "Too Expensive!" của Đe Vanilla.

### 7.2. Phân Vùng Rủi Ro Khi Rèn (Risk Zones)
- **Vùng an toàn (Safe Zone):** Áp dụng cho trang bị bậc **Junk, Common, Uncommon**.
  - Rèn thất bại: Chỉ tiêu hao 1 nguyên liệu và cấp EXP. **Món đồ 100% an toàn, không bao giờ bị vỡ!**
- **Vùng mạo hiểm (Danger Zone):** Áp dụng cho 3 mốc cao nhất (**Rare, Epic, Legendary**).
  - Từ Rare đập lên Epic: 50% Thành công | **50% Tạch vỡ nát mất luôn trang bị**.
  - Từ Epic đập lên Legendary: 25% Thành công | **75% Tạch vỡ nát mất luôn trang bị**.

### 7.3. Cơ Chế Bảo Hiểm 3 Lớp Cứu Nguy
1. **Bảo hiểm chống tụt hạng (Keep-Highest Guarantee):** Khi rèn thành công, phẩm chất chỉ giữ nguyên hoặc nâng cấp lên bậc cao hơn, không bao giờ bị giảm cấp.
2. **Bảo hiểm vận may (Pity System):**
   - Đập thất bại từ 3 lần: Loại bỏ hoàn toàn khả năng rớt vào Junk.
   - Đập thất bại từ 5 lần: Đảm bảo tối thiểu nhận được từ Uncommon trở lên.
   - Đập thất bại từ 8 lần: **100% chắc chắn ra Epic hoặc Legendary** (cưỡng chế thành công, không thể bị vỡ đồ ở mốc này).
3. **Vé Bảo Hiểm Ngọc Lục Bảo (Insurance Catalyst):**
   - Người chơi có thể kẹp thêm **1 viên Ngọc Lục Bảo (Emerald)** vào Đe.
   - Nếu rơi vào % tạch vỡ đồ, **viên ngọc lục bảo sẽ nát vụn thay cho trang bị**, cứu sống món đồ của người chơi.

---

## 8. BẢNG TOOLTIP TIẾNG ANH MẪU (UI/UX)

```text
+-------------------------------------------------------------+
| Diamond Chestplate                                          | <- Gold Color (#FFAA00)
|                                                             |
| Quality: Legendary                                          | <- Gold Color
| Item Affinity: 100/100 (Kindred Spirits - Max Luck)         | <- Aqua / Yellow
|                                                             |
| When on Body:                                               |
|   +6 Max Health (+3 Hearts)                                 | <- Green (#55FF55)
|   +2.0 Armor                                                | <- Green (#55FF55)
|   +2.0 Armor Toughness                                      | <- Green (#55FF55)
|   +5% Knockback Resistance                                  | <- Green (#55FF55)
|   +8 Armor                                                  | <- Base Vanilla Stat
|   +2 Armor Toughness                                        | <- Base Vanilla Stat
|                                                             |
| Set Bonus: Permanent Fire Resistance                        | <- Gold / Amber
| Set Bonus: Emergency Recovery (<30% HP: Res I + Regen I)    | <- Aqua
|                                                             |
| Unbreakable & Fireproof                                     | <- Orange (#FF8800)
| Reforged: 0 times (Max Quality Reached)                     | <- Gray (#AAAAAA)
+-------------------------------------------------------------+
```

---

## 9. ĐỊNH HƯỚNG TRIỂN KHAI KỸ THUẬT CHO LẬP TRÌNH VIÊN
- **Nếu triển khai Mod (Minecraft 1.21+ / 26.3):**
  - Tận dụng hệ thống **Data Components** hiện đại (thay thế NBT cũ).
  - Sử dụng `DataComponentType<QualityComponent>` với Record Codec để đồng bộ Server - Client và lưu file thế giới.
  - Sử dụng `ItemAttributeModifiers` component để gán `generic.max_health` an toàn theo từng `EquipmentSlotGroup`.
  - Can thiệp vào `AnvilMenu` / `ScreenHandler` để xử lý logic rèn, trừ EXP và cơ chế vỡ đồ / bảo hiểm ngọc lục bảo.
- **Nếu triển khai Plugin Paper/Purpur:**
  - Sử dụng `PersistentDataContainer` (PDC) gắn thẻ `NamespacedKey`.
  - Lắng nghe sự kiện `PrepareAnvilEvent`, `InventoryClickEvent`, `EntityDamageByEntityEvent`, `BlockBreakEvent`.
