package com.minequality.quality;

/**
 * Kiểm thử tính đúng đắn của bộ quản lý phẩm chất QualityManager.
 */
public class QualityManagerTest {
    public static void main(String[] args) {
        System.out.println("Bat dau kiem thu QualityManager...");

        // Kiem thu 1000 lan roll khong bi null va thuoc cac tier hop le
        int totalRolls = 1000;
        int[] counts = new int[QualityTier.values().length];

        for (int i = 0; i < totalRolls; i++) {
            QualityTier tier = QualityManager.rollQuality();
            if (tier == null) {
                throw new IllegalStateException("Loi: Roll ra ket qua null!");
            }
            counts[tier.ordinal()]++;
        }

        for (QualityTier tier : QualityTier.values()) {
            System.out.printf("Tier %s: %d / %d lan roll (Ty le thuc te: %.2f%%)\n",
                    tier.getId(), counts[tier.ordinal()], totalRolls, (counts[tier.ordinal()] * 100.0 / totalRolls));
        }

        // Kiem tra tier JUNK da duoc cap nhat dung ID va thong so
        QualityTier junk = QualityTier.JUNK;
        if (!"junk".equals(junk.getId()) || !"quality.mod_mine.tier.junk".equals(junk.getTranslationKey()) || junk.getWeight() != 15) {
            throw new IllegalStateException("Loi: Thong so cua QualityTier.JUNK khong hop le!");
        }

        System.out.println("Kiem thu QualityManager thanh cong dat 100%!");
    }
}