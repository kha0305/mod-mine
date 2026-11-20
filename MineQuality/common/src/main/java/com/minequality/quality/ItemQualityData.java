package com.minequality.quality;

/**
 * Bản ghi dữ liệu Data Component đại diện cho phẩm chất, độ gắn bó và thuộc tính đặc biệt của trang bị.
 *
 * @param tier         Bậc phẩm chất của vật phẩm (mặc định COMMON nếu null).
 * @param affinity     Điểm gắn bó của vật phẩm (nằm trong đoạn [0, 100]).
 * @param reforgeCount Số lần rèn thất bại tích lũy (dùng cho hệ thống Pity và tính phí EXP).
 * @param fireproof    Cờ xác nhận khả năng không bị thiêu rụi trong lửa/dung nham (tự động bật ở bậc LEGENDARY).
 * @param unbreakable  Cờ xác nhận độ bền vĩnh cửu không bao giờ hỏng (tự động bật ở bậc LEGENDARY).
 */
public record ItemQualityData(
        QualityTier tier,
        int affinity,
        int reforgeCount,
        boolean fireproof,
        boolean unbreakable
) {
    public static final ItemQualityData DEFAULT = new ItemQualityData(QualityTier.COMMON, 0, 0, false, false);

    /**
     * Canonical constructor kiểm tra tính hợp lệ và tự động đồng bộ đặc quyền cho bậc Huyền Thoại.
     */
    public ItemQualityData {
        QualityTier safeTier = (tier != null) ? tier : QualityTier.COMMON;
        int safeAffinity = ItemAffinity.clamp(affinity);
        int safeReforgeCount = Math.max(0, reforgeCount);

        // Bậc Huyền thoại sở hữu đặc quyền độc bản Unbreakable và Fireproof theo chuẩn tài liệu thiết kế
        boolean isLegendary = (safeTier == QualityTier.LEGENDARY);
        boolean safeFireproof = fireproof || isLegendary;
        boolean safeUnbreakable = unbreakable || isLegendary;

        tier = safeTier;
        affinity = safeAffinity;
        reforgeCount = safeReforgeCount;
        fireproof = safeFireproof;
        unbreakable = safeUnbreakable;
    }

    /**
     * Tạo dữ liệu phẩm chất nhanh từ một bậc cụ thể.
     */
    public static ItemQualityData of(QualityTier tier) {
        return new ItemQualityData(tier, 0, 0, false, false);
    }

    /**
     * Tạo dữ liệu phẩm chất nhanh với bậc và điểm gắn bó.
     */
    public static ItemQualityData of(QualityTier tier, int affinity) {
        return new ItemQualityData(tier, affinity, 0, false, false);
    }

    /**
     * Tạo dữ liệu phẩm chất bốc thăm ngẫu nhiên (dùng khi chế tạo thành công trang bị).
     */
    public static ItemQualityData randomQuality() {
        return of(QualityManager.rollQuality());
    }

    /**
     * Tạo bản sao với bậc phẩm chất mới.
     */
    public ItemQualityData withTier(QualityTier newTier) {
        return new ItemQualityData(newTier, this.affinity, this.reforgeCount, this.fireproof, this.unbreakable);
    }

    /**
     * Tạo bản sao với điểm gắn bó mới.
     */
    public ItemQualityData withAffinity(int newAffinity) {
        return new ItemQualityData(this.tier, newAffinity, this.reforgeCount, this.fireproof, this.unbreakable);
    }

    /**
     * Tăng thêm điểm gắn bó và giới hạn an toàn trong đoạn [0, 100].
     */
    public ItemQualityData addAffinity(int delta) {
        return withAffinity(this.affinity + delta);
    }

    /**
     * Tạo bản sao với số lần rèn tích lũy mới.
     */
    public ItemQualityData withReforgeCount(int newReforgeCount) {
        return new ItemQualityData(this.tier, this.affinity, newReforgeCount, this.fireproof, this.unbreakable);
    }

    /**
     * Tăng số lần rèn thất bại lên 1 đơn vị.
     */
    public ItemQualityData incrementReforgeCount() {
        return withReforgeCount(this.reforgeCount + 1);
    }

    /**
     * Tạo bản sao với cờ chống cháy mới.
     */
    public ItemQualityData withFireproof(boolean newFireproof) {
        return new ItemQualityData(this.tier, this.affinity, this.reforgeCount, newFireproof, this.unbreakable);
    }

    /**
     * Tạo bản sao với cờ không thể phá hủy mới.
     */
    public ItemQualityData withUnbreakable(boolean newUnbreakable) {
        return new ItemQualityData(this.tier, this.affinity, this.reforgeCount, this.fireproof, newUnbreakable);
    }

    /**
     * Kiểm tra trang bị có phải bậc Huyền Thoại hay không.
     */
    public boolean isLegendary() {
        return this.tier == QualityTier.LEGENDARY;
    }

    /**
     * Kiểm tra trang bị có phải bậc Phế Phẩm (Junk) hay không.
     */
    public boolean isJunk() {
        return this.tier == QualityTier.JUNK;
    }

    /**
     * Kết quả rèn bao gồm dữ liệu sau khi cập nhật và trạng thái chi tiết của đợt rèn.
     */
    public record ReforgeOutcome(
            ItemQualityData updatedData,
            QualityItemFilter.ReforgeResult rawResult
    ) {
        public boolean isBroken() {
            return rawResult.broken();
        }

        public boolean isUpgraded() {
            return rawResult.upgraded();
        }

        public boolean isEmeraldConsumed() {
            return rawResult.emeraldConsumed();
        }
    }

    /**
     * Thực hiện rèn lại trang bị tích hợp đầy đủ cơ chế bảo hiểm Ngọc Lục Bảo và độ gắn bó.
     *
     * @param hasEmeraldInsurance Xác nhận người chơi có kẹp thêm 1 viên Ngọc Lục Bảo trên Đe hay không.
     * @return Kết quả rèn kèm dữ liệu trang bị đã cập nhật (updatedData = null nếu trang bị bị vỡ nát).
     */
    public ReforgeOutcome applyReforge(boolean hasEmeraldInsurance) {
        QualityItemFilter.ReforgeResult result = QualityItemFilter.performReforge(
                this.tier,
                this.reforgeCount,
                this.affinity,
                hasEmeraldInsurance
        );

        if (result.broken()) {
            // Trang bị nát hoàn toàn
            return new ReforgeOutcome(null, result);
        }

        // Cập nhật bậc mới hoặc giữ nguyên, cập nhật số lần rèn, bảo tồn điểm gắn bó
        ItemQualityData updated = new ItemQualityData(
                result.resultingTier(),
                this.affinity,
                result.nextReforgeCount(),
                this.fireproof,
                this.unbreakable
        );
        return new ReforgeOutcome(updated, result);
    }

    /**
     * Phuong thuc cau noi dong bo voi applyReforge de tranh sai lech goi ham.
     */
    public ReforgeOutcome reforge(boolean hasEmeraldInsurance) {
        return applyReforge(hasEmeraldInsurance);
    }
}
