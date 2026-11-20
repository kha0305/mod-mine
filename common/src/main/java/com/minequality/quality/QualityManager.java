package com.minequality.quality;

import java.util.Random;

/**
 * Trình quản lý và tính toán phẩm chất ngẫu nhiên khi chế tạo trang bị.
 */
public final class QualityManager {
    private static final Random RANDOM = new Random();
    private static final int TOTAL_WEIGHT;

    static {
        int sum = 0;
        for (QualityTier tier : QualityTier.values()) {
            sum += tier.getWeight();
        }
        TOTAL_WEIGHT = sum;
    }

    private QualityManager() {
    }

    /**
     * Bốc thăm ngẫu nhiên một bậc phẩm chất dựa theo bảng trọng số.
     */
    public static QualityTier rollQuality() {
        int roll = RANDOM.nextInt(TOTAL_WEIGHT);
        int current = 0;
        for (QualityTier tier : QualityTier.values()) {
            current += tier.getWeight();
            if (roll < current) {
                return tier;
            }
        }
        return QualityTier.COMMON;
    }
}
