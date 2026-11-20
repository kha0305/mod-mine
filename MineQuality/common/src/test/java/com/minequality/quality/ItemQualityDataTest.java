package com.minequality.quality;

/**
 * Kiểm thử tính đúng đắn của bản ghi ItemQualityData.
 */
public class ItemQualityDataTest {

    public static void main(String[] args) {
        System.out.println("Bat dau kiem thu ItemQualityData...");

        // 1. Kiem thu mac dinh an toan null va rang buoc
        ItemQualityData dataNull = new ItemQualityData(null, -50, -10, false, false);
        if (dataNull.tier() != QualityTier.COMMON || dataNull.affinity() != 0 || dataNull.reforgeCount() != 0) {
            throw new IllegalStateException("Loi: Khoi tao voi tham so bat thuong khong duoc tu dong chuan hoa!");
        }
        System.out.println("Kiem tra canonical constructor an toan null dat 100%!");

        // 2. Kiem thu tu dong bat Unbreakable va Fireproof khi la LEGENDARY
        ItemQualityData legData = ItemQualityData.of(QualityTier.LEGENDARY);
        if (!legData.fireproof() || !legData.unbreakable() || !legData.isLegendary()) {
            throw new IllegalStateException("Loi: Phau thuat Legend bat buoc phai co Fireproof va Unbreakable!");
        }
        System.out.println("Kiem tra tu dong bat dac quyen Fireproof va Unbreakable cho Legendary dat 100%!");

        // 3. Kiem thu cac thao tac bat bien with
        ItemQualityData modified = legData.withAffinity(80).withReforgeCount(3);
        if (modified.affinity() != 80 || modified.reforgeCount() != 3 || modified.tier() != QualityTier.LEGENDARY) {
            throw new IllegalStateException("Loi: Cac thao tac copy-on-write bi loi!");
        }

        ItemQualityData incremented = modified.incrementReforgeCount().addAffinity(30);
        if (incremented.reforgeCount() != 4 || incremented.affinity() != 100) { // bi clamp o 100
            throw new IllegalStateException("Loi: incrementReforgeCount hoac addAffinity bi loi!");
        }
        System.out.println("Kiem tra cac thao tac bat bien dat 100%!");

        // 4. Kiem thu applyReforge voi Ngoc Luc Bao (Emerald Insurance)
        ItemQualityData epicData = ItemQualityData.of(QualityTier.EPIC, 50);
        boolean savedByEmerald = false;
        for (int i = 0; i < 200; i++) {
            ItemQualityData.ReforgeOutcome outcome = epicData.applyReforge(true);
            if (outcome.isBroken() || outcome.updatedData() == null) {
                throw new IllegalStateException("Loi: Khi co ngoc luc bao khong bao gio bi vo!");
            }
            if (outcome.isEmeraldConsumed()) {
                savedByEmerald = true;
            }
        }
        if (!savedByEmerald) {
            throw new IllegalStateException("Loi: Phai co lan ngoc luc bao hoat dong de cuu do!");
        }
        System.out.println("Kiem tra applyReforge tich hop bao hiem Ngoc Luc Bao dat 100%!");

        System.out.println("Kiem thu ItemQualityData thanh cong dat 100%!");
    }
}