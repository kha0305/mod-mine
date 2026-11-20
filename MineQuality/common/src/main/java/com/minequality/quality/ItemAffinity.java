package com.minequality.quality;

/**
 * Quản lý điểm gắn bó (Item Affinity / Item Bond) của trang bị với người chơi.
 * Điểm số nằm trong phạm vi từ 0 đến 100.
 */
public final class ItemAffinity {

    public static final int MIN_AFFINITY = 0;
    public static final int MAX_AFFINITY = 100;

    /**
     * Tỉ lệ giảm nguy cơ vỡ đồ tại Đe khi trang bị đạt độ gắn bó (15%).
     */
    public static final double BREAK_CHANCE_REDUCTION = 0.15;

    /**
     * 5 mốc danh hiệu gắn bó theo chuẩn tài liệu thiết kế.
     */
    public enum AffinityRank {
        STRANGER(0, 24, "Stranger", "quality.mod_mine.affinity.stranger"),
        ACQUAINTANCE(25, 49, "Acquaintance", "quality.mod_mine.affinity.acquaintance"),
        TRUSTED_COMPANION(50, 74, "Trusted Companion", "quality.mod_mine.affinity.trusted_companion"),
        SOULBOUND(75, 99, "Soulbound", "quality.mod_mine.affinity.soulbound"),
        KINDRED_SPIRITS(100, 100, "Kindred Spirits", "quality.mod_mine.affinity.kindred_spirits");

        private final int minPoints;
        private final int maxPoints;
        private final String title;
        private final String translationKey;

        AffinityRank(int minPoints, int maxPoints, String title, String translationKey) {
            this.minPoints = minPoints;
            this.maxPoints = maxPoints;
            this.title = title;
            this.translationKey = translationKey;
        }

        public int getMinPoints() {
            return minPoints;
        }

        public int getMaxPoints() {
            return maxPoints;
        }

        public String getTitle() {
            return title;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    private ItemAffinity() {
    }

    /**
     * Giới hạn điểm gắn bó luôn nằm trong đoạn [0, 100].
     */
    public static int clamp(int affinity) {
        if (affinity < MIN_AFFINITY) {
            return MIN_AFFINITY;
        }
        if (affinity > MAX_AFFINITY) {
            return MAX_AFFINITY;
        }
        return affinity;
    }

    /**
     * Xác định danh hiệu tương ứng với số điểm gắn bó hiện tại.
     */
    public static AffinityRank getRank(int affinity) {
        int safe = clamp(affinity);
        if (safe >= 100) {
            return AffinityRank.KINDRED_SPIRITS;
        }
        if (safe >= 75) {
            return AffinityRank.SOULBOUND;
        }
        if (safe >= 50) {
            return AffinityRank.TRUSTED_COMPANION;
        }
        if (safe >= 25) {
            return AffinityRank.ACQUAINTANCE;
        }
        return AffinityRank.STRANGER;
    }

    /**
     * Tính toán tỉ lệ giảm nguy cơ vỡ đồ khi rèn tại Đe (giảm trực tiếp 15% khi đạt mốc gắn bó từ Acquaintance trở lên).
     */
    public static double getBreakChanceReduction(int affinity) {
        int safe = clamp(affinity);
        return (safe >= 25) ? BREAK_CHANCE_REDUCTION : 0.0;
    }

    /**
     * Hệ số nhân thời gian duy trì hiệu ứng: tăng thêm 50% (x1.5) tại mốc tối đa Kindred Spirits (100/100).
     */
    public static double getDurationMultiplier(int affinity) {
        return (clamp(affinity) >= MAX_AFFINITY) ? 1.5 : 1.0;
    }

    /**
     * Tỉ lệ thưởng cơ hội kích hoạt hiệu ứng (Proc Chance Bonus) theo điểm gắn bó (tối đa +15% tại 100 điểm).
     */
    public static double getProcChanceBonus(int affinity) {
        int safe = clamp(affinity);
        return (safe / (double) MAX_AFFINITY) * 0.15;
    }

    /**
     * Kiểm tra trang bị đã đạt mức gắn bó tối đa hay chưa.
     */
    public static boolean isMaxAffinity(int affinity) {
        return clamp(affinity) >= MAX_AFFINITY;
    }

    /**
     * Định dạng chuỗi hiển thị Tooltip theo chuẩn tài liệu thiết kế mục 8.
     * Ví dụ: "Item Affinity: 100/100 (Kindred Spirits - Max Luck)" hoặc "Item Affinity: 50/100 (Trusted Companion)"
     */
    public static String formatTooltip(int affinity) {
        int safe = clamp(affinity);
        AffinityRank rank = getRank(safe);
        if (safe >= MAX_AFFINITY) {
            return String.format("Item Affinity: %d/%d (%s - Max Luck)", safe, MAX_AFFINITY, rank.getTitle());
        }
        return String.format("Item Affinity: %d/%d (%s)", safe, MAX_AFFINITY, rank.getTitle());
    }
}