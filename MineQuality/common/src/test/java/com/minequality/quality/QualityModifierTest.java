package com.minequality.quality;

/**
 * Kiểm thử tính đúng đắn của bảng chỉ số QualityModifier.
 */
public class QualityModifierTest {

    public static void main(String[] args) {
        System.out.println("Bat dau kiem thu QualityModifier...");

        // 1. Kiem thu bo giap Huyen thoai cong dung 20 HP (4 + 6 + 6 + 4)
        QualityModifier.ModifierStats helmet = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_HELMET);
        QualityModifier.ModifierStats chest = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE);
        QualityModifier.ModifierStats legs = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_LEGGINGS);
        QualityModifier.ModifierStats boots = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_BOOTS);

        double totalHealthBonus = helmet.healthBonus() + chest.healthBonus() + legs.healthBonus() + boots.healthBonus();
        if (Math.abs(totalHealthBonus - 20.0) > 0.0001) {
            throw new IllegalStateException("Loi: Bo giap Huyen thoai phai cong dung 20 HP, thuc te: " + totalHealthBonus);
        }
        System.out.printf("Bo giap Huyen thoai cong HP: %.1f HP (Chuan 40 HP khi ket hop mau goc)\n", totalHealthBonus);

        // 2. Kiem thu bo giap Phe pham (JUNK) tru dung 6 HP (-1 - 2 - 2 - 1)
        QualityModifier.ModifierStats junkHelmet = QualityModifier.getStats(QualityTier.JUNK, QualityModifier.EquipmentCategory.ARMOR_HELMET);
        QualityModifier.ModifierStats junkChest = QualityModifier.getStats(QualityTier.JUNK, QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE);
        QualityModifier.ModifierStats junkLegs = QualityModifier.getStats(QualityTier.JUNK, QualityModifier.EquipmentCategory.ARMOR_LEGGINGS);
        QualityModifier.ModifierStats junkBoots = QualityModifier.getStats(QualityTier.JUNK, QualityModifier.EquipmentCategory.ARMOR_BOOTS);

        double totalJunkPenalty = junkHelmet.healthBonus() + junkChest.healthBonus() + junkLegs.healthBonus() + junkBoots.healthBonus();
        if (Math.abs(totalJunkPenalty - (-6.0)) > 0.0001) {
            throw new IllegalStateException("Loi: Bo giap Phe pham phai tru dung -6 HP, thuc te: " + totalJunkPenalty);
        }
        System.out.printf("Bo giap Phe pham phat HP: %.1f HP\n", totalJunkPenalty);

        // 3. Kiem thu pham chat Pho thong khong mang chi so
        for (QualityModifier.EquipmentCategory category : QualityModifier.EquipmentCategory.values()) {
            QualityModifier.ModifierStats stats = QualityModifier.getStats(QualityTier.COMMON, category);
            if (stats.hasModifiers()) {
                throw new IllegalStateException("Loi: Pham chat Pho thong khong duoc mang chi so!");
            }
        }
        System.out.println("Pham chat Pho thong (Common) hoan toan trung tinh (0 chi so) dat chuan!");

        // 4. Kiem thu chi so Elytra va cac chi so toc do, may man moi bo sung
        QualityModifier.ModifierStats legendaryElytra = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ELYTRA);
        if (legendaryElytra.armorBonus() != 3.0 || legendaryElytra.toughnessBonus() != 2.0 || legendaryElytra.movementSpeedMultiplier() != 0.10) {
            throw new IllegalStateException("Loi: Chi so Elytra Huyen thoai khong dung thiet ke!");
        }

        QualityModifier.ModifierStats legendaryWeapon = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.MELEE_WEAPON);
        if (legendaryWeapon.movementSpeedMultiplier() != 0.10 || legendaryWeapon.luckBonus() != 1.0) {
            throw new IllegalStateException("Loi: Vu khi Huyen thoai phai co +10% Movement Speed va +1 Luck!");
        }

        // 5. Kiem thu an toan null cho toan bo enum va gia tri bat ky
        for (QualityTier tier : QualityTier.values()) {
            for (QualityModifier.EquipmentCategory category : QualityModifier.EquipmentCategory.values()) {
                QualityModifier.ModifierStats stats = QualityModifier.getStats(tier, category);
                if (stats == null) {
                    throw new IllegalStateException("Loi: Tra ve null tai " + tier + " / " + category);
                }
            }
        }
        if (QualityModifier.getStats(null, null) == null) {
            throw new IllegalStateException("Loi: Khong duoc tra ve null khi tham so null!");
        }
        System.out.println("Kiem tra an toan null cho tat ca cac truong hop dat 100%!");

        System.out.println("Kiem thu QualityModifier thanh cong dat 100%!");
    }
}