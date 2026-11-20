package com.minequality.quality;

/**
 * Kiểm thử tính đúng đắn của ItemAffinity.
 */
public class ItemAffinityTest {

    public static void main(String[] args) {
        System.out.println("Bat dau kiem thu ItemAffinity...");

        // 1. Kiem thu clamp trong pham vi [0, 100]
        if (ItemAffinity.clamp(-10) != 0 || ItemAffinity.clamp(150) != 100 || ItemAffinity.clamp(50) != 50) {
            throw new IllegalStateException("Loi: Ham clamp tinh toan sai!");
        }

        // 2. Kiem thu 5 moc danh hieu
        if (ItemAffinity.getRank(0) != ItemAffinity.AffinityRank.STRANGER
                || ItemAffinity.getRank(24) != ItemAffinity.AffinityRank.STRANGER
                || ItemAffinity.getRank(25) != ItemAffinity.AffinityRank.ACQUAINTANCE
                || ItemAffinity.getRank(49) != ItemAffinity.AffinityRank.ACQUAINTANCE
                || ItemAffinity.getRank(50) != ItemAffinity.AffinityRank.TRUSTED_COMPANION
                || ItemAffinity.getRank(74) != ItemAffinity.AffinityRank.TRUSTED_COMPANION
                || ItemAffinity.getRank(75) != ItemAffinity.AffinityRank.SOULBOUND
                || ItemAffinity.getRank(99) != ItemAffinity.AffinityRank.SOULBOUND
                || ItemAffinity.getRank(100) != ItemAffinity.AffinityRank.KINDRED_SPIRITS) {
            throw new IllegalStateException("Loi: Xac dinh moc danh hieu bi sai lech!");
        }
        System.out.println("Kiem tra 5 moc danh hieu Affinity dat 100%!");

        // 3. Kiem thu tac dung giam 15% ty le vo do tai De
        if (ItemAffinity.getBreakChanceReduction(0) != 0.0) {
            throw new IllegalStateException("Loi: Moc 0 khong duoc giam ty le vo do!");
        }
        if (ItemAffinity.getBreakChanceReduction(25) != 0.15 || ItemAffinity.getBreakChanceReduction(100) != 0.15) {
            throw new IllegalStateException("Loi: Tu 25 diem tro len phai giam dung 15% (0.15) ty le vo do!");
        }
        System.out.println("Kiem tra giam 15% ty le vo do dat 100%!");

        // 4. Kiem thu he so nhan thoi gian hieu ung (+50% tai moc 100/100)
        if (ItemAffinity.getDurationMultiplier(99) != 1.0) {
            throw new IllegalStateException("Loi: Duoi 100 diem he so nhan thoi gian phai la 1.0!");
        }
        if (ItemAffinity.getDurationMultiplier(100) != 1.5) {
            throw new IllegalStateException("Loi: Tai 100 diem he so nhan thoi gian phai la 1.5 (+50%)!");
        }
        System.out.println("Kiem tra he so nhan thoi gian +50% tai moc 100 dat 100%!");

        // 5. Kiem thu format chuoi Tooltip
        String tooltip100 = ItemAffinity.formatTooltip(100);
        if (!"Item Affinity: 100/100 (Kindred Spirits - Max Luck)".equals(tooltip100)) {
            throw new IllegalStateException("Loi format tooltip tai moc 100: " + tooltip100);
        }
        String tooltip50 = ItemAffinity.formatTooltip(50);
        if (!"Item Affinity: 50/100 (Trusted Companion)".equals(tooltip50)) {
            throw new IllegalStateException("Loi format tooltip tai moc 50: " + tooltip50);
        }
        System.out.println("Kiem tra dinh dang chuoi Tooltip Affinity dat 100%!");

        System.out.println("Kiem thu ItemAffinity thanh cong dat 100%!");
    }
}