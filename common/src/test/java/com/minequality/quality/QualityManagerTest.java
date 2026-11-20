package com.minequality.quality;

/**
 * Kiểm thử tính đúng đắn của bộ quản lý phẩm chất.
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
                throw new IllegalStateException("Loi: Phau thuat roll ra ket qua null!");
            }
            counts[tier.ordinal()]++;
        }

        for (QualityTier tier : QualityTier.values()) {
            System.out.printf("Tier %s: %d / %d lan roll (Ty le thuc te: %.2f%%)\n",
                tier.getId(), counts[tier.ordinal()], totalRolls, (counts[tier.ordinal()] * 100.0 / totalRolls));
        }

        System.out.println("Kiem thu QualityManager thanh cong dat 100%!");
    }
}
