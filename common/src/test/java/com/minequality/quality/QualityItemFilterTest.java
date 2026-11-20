package com.minequality.quality;

/**
 * Kiểm thử tự động tính chính xác của QualityItemFilter và cơ chế bảo hiểm rèn lại.
 */
public class QualityItemFilterTest {

    public static void main(String[] args) {
        System.out.println("Bat dau kiem thu QualityItemFilter...");

        // 1. Kiem tra nhan dien trang bi hop le
        if (!QualityItemFilter.isEligible("minecraft:diamond_sword")
                || !QualityItemFilter.isEligible("iron_pickaxe")
                || !QualityItemFilter.isEligible("netherite_chestplate")
                || !QualityItemFilter.isEligible("shield")
                || !QualityItemFilter.isEligible("elytra")
                || !QualityItemFilter.isEligible("mace")
                || !QualityItemFilter.isEligible("create:copper_sword")) {
            throw new IllegalStateException("Loi: Nhan dien thieu trang bi hop le!");
        }

        // Kiem tra loai bo vat pham khong phai trang bi
        if (QualityItemFilter.isEligible("minecraft:dirt")
                || QualityItemFilter.isEligible("minecraft:apple")
                || QualityItemFilter.isEligible("minecraft:diamond")
                || QualityItemFilter.isEligible("iron_ingot")
                || QualityItemFilter.isEligible(null)
                || QualityItemFilter.isEligible("")) {
            throw new IllegalStateException("Loi: Nhan dien nham vat pham khong phai trang bi!");
        }
        System.out.println("Kiem tra nhan dien trang bi dat 100%!");

        // 2. Kiem tra nhom trang bi (EquipmentCategory)
        if (QualityItemFilter.getCategory("diamond_sword") != QualityModifier.EquipmentCategory.MELEE_WEAPON
                || QualityItemFilter.getCategory("iron_pickaxe") != QualityModifier.EquipmentCategory.MINING_TOOL
                || QualityItemFilter.getCategory("bow") != QualityModifier.EquipmentCategory.RANGED_WEAPON
                || QualityItemFilter.getCategory("diamond_helmet") != QualityModifier.EquipmentCategory.ARMOR_HELMET
                || QualityItemFilter.getCategory("diamond_chestplate") != QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE
                || QualityItemFilter.getCategory("elytra") != QualityModifier.EquipmentCategory.ELYTRA
                || QualityItemFilter.getCategory("shield") != QualityModifier.EquipmentCategory.SHIELD) {
            throw new IllegalStateException("Loi: Phan loai nhom trang bi sai lech!");
        }
        System.out.println("Kiem tra phan loai nhom trang bi dat 100%!");

        // 3. Kiem tra nguyen lieu ren lai hop le
        if (!QualityItemFilter.isReforgeMaterial("diamond_sword", "diamond")
                || !QualityItemFilter.isReforgeMaterial("iron_chestplate", "iron_ingot")
                || !QualityItemFilter.isReforgeMaterial("netherite_axe", "netherite_ingot")
                || !QualityItemFilter.isReforgeMaterial("elytra", "phantom_membrane")
                || QualityItemFilter.isReforgeMaterial("diamond_sword", "dirt")
                || QualityItemFilter.isReforgeMaterial("diamond_sword", "iron_ingot")) {
            throw new IllegalStateException("Loi: Kiem tra nguyen lieu ren lai that bai!");
        }
        System.out.println("Kiem tra nguyen lieu ren lai dat 100%!");

        // 4. Kiem tra cong thuc tang chi phi EXP va muc tran bao ve 30 cap
        int cost0 = QualityItemFilter.calculateReforgeCost("diamond_sword", 0); // 3
        int cost1 = QualityItemFilter.calculateReforgeCost("diamond_sword", 1); // 5
        int cost2 = QualityItemFilter.calculateReforgeCost("diamond_sword", 2); // 7
        int costHigh = QualityItemFilter.calculateReforgeCost("diamond_sword", 20); // Phai tran tai 30

        if (cost0 != 3 || cost1 != 5 || cost2 != 7) {
            throw new IllegalStateException("Loi cong thuc tang EXP: cost0=" + cost0 + ", cost1=" + cost1 + ", cost2=" + cost2);
        }
        if (costHigh != QualityItemFilter.MAX_EXP_COST) {
            throw new IllegalStateException("Loi muc tran EXP: costHigh=" + costHigh + ", mong doi: 30");
        }
        System.out.printf("Kiem tra chi phi EXP tang dan: 0 lan -> %d cap, 1 lan -> %d cap, 2 lan -> %d cap, 20 lan -> %d cap dat 100%%!\n",
                cost0, cost1, cost2, costHigh);

        // 5. Kiem tra Vung An Toan (Safe Zone: JUNK, COMMON, UNCOMMON) - 100% khong bao gio bi vo do
        for (int i = 0; i < 200; i++) {
            QualityItemFilter.ReforgeResult resJunk = QualityItemFilter.performReforge(QualityTier.JUNK, 0, 0, false);
            QualityItemFilter.ReforgeResult resCommon = QualityItemFilter.performReforge(QualityTier.COMMON, 0, 0, false);
            QualityItemFilter.ReforgeResult resUncommon = QualityItemFilter.performReforge(QualityTier.UNCOMMON, 0, 0, false);
            if (resJunk.broken() || resCommon.broken() || resUncommon.broken()) {
                throw new IllegalStateException("Loi: Vung An Toan tuyet doi khong duoc lam vo do!");
            }
        }
        System.out.println("Kiem tra Vung An Toan (JUNK, COMMON, UNCOMMON) khong bao gio vo do dat 100%!");

        // 6. Kiem tra Bao hiem chong tut hang (Keep-Highest Guarantee)
        // Khi do dang la RARE, ren 100 lan khong bao gio bi tut xuong JUNK, COMMON hoac UNCOMMON
        for (int i = 0; i < 100; i++) {
            QualityItemFilter.ReforgeResult result = QualityItemFilter.performReforge(QualityTier.RARE, 0, 100, true);
            if (!result.broken() && result.resultingTier().ordinal() < QualityTier.RARE.ordinal()) {
                throw new IllegalStateException("Loi bao hiem chong tut hang: Tu RARE bi tut xuong " + result.resultingTier());
            }
        }
        System.out.println("Bao hiem chong tut hang (100 lan thu tu RARE deu giu hoac len cap) dat 100%!");

        // 7. Kiem tra Bao hiem Ngoc Luc Bao (Emerald Insurance) trong Vung Mao Hiem
        // Khi ren RARE co kẹp ngọc: neu co nguy co vo thi ngoc phai nat thay (emeraldConsumed = true) va do khong bi vo (broken = false)
        int emeraldSavedCount = 0;
        for (int i = 0; i < 500; i++) {
            QualityItemFilter.ReforgeResult result = QualityItemFilter.performReforge(QualityTier.EPIC, 0, 0, true);
            if (result.broken()) {
                throw new IllegalStateException("Loi: Khi co Bao Hiem Ngoc Luc Bao, trang bi tuyet doi khong duoc bi vo!");
            }
            if (result.emeraldConsumed()) {
                emeraldSavedCount++;
            }
        }
        if (emeraldSavedCount == 0) {
            throw new IllegalStateException("Loi: Trong 500 lan dap EPIC co bao hiem, phai co it nhat 1 lan ngoc duoc kich hoat de cuu do!");
        }
        System.out.printf("Bao hiem Ngoc Luc Bao cuu thoat %d / 500 lan nguy hiem thanh cong!\n", emeraldSavedCount);

        // 8. Kiem tra Bao hiem van may (Pity System) tai moc 8 lan dap (Chac chan ra EPIC hoac LEGENDARY va khong bao gio vo)
        for (int i = 0; i < 100; i++) {
            QualityItemFilter.ReforgeResult result = QualityItemFilter.performReforge(QualityTier.COMMON, 8, 0, false);
            if (result.broken()) {
                throw new IllegalStateException("Loi: Moc 8 Pity khong the bi vo do!");
            }
            if (result.resultingTier() != QualityTier.EPIC && result.resultingTier() != QualityTier.LEGENDARY) {
                throw new IllegalStateException("Loi Bao hiem moc 8: Phai ra Epic hoac Legendary, thuc te: " + result.resultingTier());
            }
        }
        System.out.println("Bao hiem van may (Moc 8 lan dap 100% ra Epic hoac Legendary, an toan tuyet doi) dat 100%!");

        System.out.println("Kiem thu QualityItemFilter thanh cong hoan hao dat 100%!");
    }
}