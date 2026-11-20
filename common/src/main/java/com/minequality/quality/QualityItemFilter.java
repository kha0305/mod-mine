package com.minequality.quality;

import java.util.Random;

/**
 * Bộ lọc nhận diện trang bị hợp lệ, quản lý nguyên liệu và cơ chế bảo hiểm rèn lại trên Đe.
 */
public final class QualityItemFilter {

    private static final Random RANDOM = new Random();
    public static final int MAX_EXP_COST = 30; // Giới hạn trần EXP để không vượt ngưỡng 39 của Đe Vanilla (tránh lỗi Too Expensive)

    /**
     * Bản ghi chứa kết quả sau khi thực hiện rèn lại.
     *
     * @param resultingTier    Phẩm chất nhận được sau khi rèn (null nếu trang bị bị vỡ nát).
     * @param nextReforgeCount Số lần thất bại tích lũy cho lần rèn kế tiếp.
     * @param upgraded         Xác nhận có nâng cấp lên phẩm chất cao hơn hay không.
     * @param pityTriggered    Xác nhận có kích hoạt bảo hiểm vận may (Pity) hay không.
     * @param broken           Xác nhận trang bị có bị vỡ nát do tạch trong Vùng Mạo Hiểm hay không.
     * @param emeraldConsumed  Xác nhận viên Ngọc Lục Bảo có nát vụn thay để cứu trang bị hay không.
     */
    public record ReforgeResult(
            QualityTier resultingTier,
            int nextReforgeCount,
            boolean upgraded,
            boolean pityTriggered,
            boolean broken,
            boolean emeraldConsumed
    ) {
        /**
         * Constructor tương thích với mã nguồn cũ.
         */
        public ReforgeResult(QualityTier resultingTier, int nextReforgeCount, boolean upgraded, boolean pityTriggered) {
            this(resultingTier, nextReforgeCount, upgraded, pityTriggered, false, false);
        }
    }

    private QualityItemFilter() {
    }

    /**
     * Kiểm tra xem một vật phẩm có đủ điều kiện nhận phẩm chất hay không.
     */
    public static boolean isEligible(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }
        String id = normalizeId(itemId);
        return id.endsWith("_sword")
                || id.endsWith("_pickaxe")
                || id.endsWith("_axe")
                || id.endsWith("_shovel")
                || id.endsWith("_hoe")
                || id.endsWith("_helmet")
                || id.endsWith("_chestplate")
                || id.endsWith("_leggings")
                || id.endsWith("_boots")
                || id.equals("bow")
                || id.equals("crossbow")
                || id.equals("shield")
                || id.equals("trident")
                || id.equals("mace")
                || id.equals("elytra");
    }

    /**
     * Xác định nhóm trang bị (EquipmentCategory) tương ứng với định danh vật phẩm.
     */
    public static QualityModifier.EquipmentCategory getCategory(String itemId) {
        if (!isEligible(itemId)) {
            return QualityModifier.EquipmentCategory.OTHER;
        }
        String id = normalizeId(itemId);
        if (id.endsWith("_sword") || id.equals("trident") || id.equals("mace") || id.endsWith("_axe")) {
            return QualityModifier.EquipmentCategory.MELEE_WEAPON;
        }
        if (id.endsWith("_pickaxe") || id.endsWith("_shovel") || id.endsWith("_hoe")) {
            return QualityModifier.EquipmentCategory.MINING_TOOL;
        }
        if (id.equals("bow") || id.equals("crossbow")) {
            return QualityModifier.EquipmentCategory.RANGED_WEAPON;
        }
        if (id.endsWith("_helmet")) {
            return QualityModifier.EquipmentCategory.ARMOR_HELMET;
        }
        if (id.equals("elytra")) {
            return QualityModifier.EquipmentCategory.ELYTRA;
        }
        if (id.endsWith("_chestplate")) {
            return QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE;
        }
        if (id.endsWith("_leggings")) {
            return QualityModifier.EquipmentCategory.ARMOR_LEGGINGS;
        }
        if (id.endsWith("_boots")) {
            return QualityModifier.EquipmentCategory.ARMOR_BOOTS;
        }
        if (id.equals("shield")) {
            return QualityModifier.EquipmentCategory.SHIELD;
        }
        return QualityModifier.EquipmentCategory.OTHER;
    }

    /**
     * Lấy chi phí cấp độ kinh nghiệm gốc (baseCost) dựa theo cấp độ vật liệu.
     */
    public static int getBaseCost(String itemId) {
        if (!isEligible(itemId)) {
            return 1;
        }
        String id = normalizeId(itemId);
        if (id.contains("netherite") || id.equals("elytra") || id.equals("mace") || id.equals("trident")) {
            return 4;
        }
        if (id.contains("diamond")) {
            return 3;
        }
        if (id.contains("iron") || id.contains("chainmail") || id.contains("golden") || id.contains("gold")
                || id.equals("bow") || id.equals("crossbow") || id.equals("shield")) {
            return 2;
        }
        return 1; // Gỗ, đá, da
    }

    /**
     * Tính toán chi phí cấp độ kinh nghiệm (XP) cho lần rèn hiện tại, áp dụng giới hạn trần bảo vệ.
     */
    public static int calculateReforgeCost(String itemId, int reforgeCount) {
        int base = getBaseCost(itemId);
        int safeCount = Math.max(0, reforgeCount);
        if (safeCount >= MAX_EXP_COST) {
            return MAX_EXP_COST;
        }
        int calculated = base + (safeCount * 2);
        return Math.min(MAX_EXP_COST, calculated);
    }

    /**
     * Kiểm tra nguyên liệu đặt vào ô thứ hai của Đe có phải là nguyên liệu rèn hợp lệ của trang bị hay không.
     */
    public static boolean isReforgeMaterial(String itemId, String materialId) {
        if (!isEligible(itemId) || materialId == null || materialId.isBlank()) {
            return false;
        }
        String item = normalizeId(itemId);
        String mat = normalizeId(materialId);

        if (item.contains("netherite")) {
            return mat.equals("netherite_ingot");
        }
        if (item.contains("diamond")) {
            return mat.equals("diamond");
        }
        if (item.contains("iron") || item.contains("chainmail")) {
            return mat.equals("iron_ingot");
        }
        if (item.contains("golden") || item.contains("gold")) {
            return mat.equals("gold_ingot");
        }
        if (item.contains("leather")) {
            return mat.equals("leather");
        }
        if (item.contains("stone")) {
            return mat.equals("cobblestone") || mat.equals("cobbled_deepslate") || mat.equals("blackstone");
        }
        if (item.contains("wooden")) {
            return mat.endsWith("_planks") || mat.equals("bamboo_planks");
        }
        if (item.equals("elytra")) {
            return mat.equals("phantom_membrane");
        }
        if (item.equals("shield")) {
            return mat.endsWith("_planks") || mat.equals("iron_ingot");
        }
        if (item.equals("bow") || item.equals("crossbow")) {
            return mat.equals("string") || mat.equals("iron_ingot");
        }
        if (item.equals("trident")) {
            return mat.equals("prismarine_shard") || mat.equals("heart_of_the_sea");
        }
        if (item.equals("mace")) {
            return mat.equals("heavy_core") || mat.equals("breeze_rod") || mat.equals("iron_ingot");
        }
        if (item.contains("turtle")) {
            return mat.equals("turtle_scute");
        }
        return false;
    }

    /**
     * Thực hiện rèn lại phiên bản cơ bản không kèm Affinity và Bảo hiểm Ngọc Lục Bảo.
     */
    public static ReforgeResult performReforge(QualityTier currentTier, int reforgeCount) {
        return performReforge(currentTier, reforgeCount, 0, false);
    }

    public static int getPityThreshold(QualityTier currentTier) {
        if (currentTier == null) {
            return 2;
        }
        return switch (currentTier) {
            case JUNK -> 1;
            case COMMON -> 2;
            case UNCOMMON -> 4;
            case RARE -> 5;
            case EPIC -> 7;
            case LEGENDARY -> 0;
        };
    }

    /**
     * Tinh toan xac suat nang cap thanh cong (%) dua tren pham chat hien tai va so lan dap xit tich luy.
     */
    public static int getUpgradeChancePercent(QualityTier currentTier, int reforgeCount) {
        if (currentTier == QualityTier.LEGENDARY) {
            return 0;
        }
        int threshold = getPityThreshold(currentTier);
        int safeCount = Math.max(0, reforgeCount);
        if (safeCount >= threshold) {
            return 100;
        }

        int baseChance = switch (currentTier) {
            case JUNK -> 100;
            case COMMON -> 50;
            case UNCOMMON -> 35;
            case RARE -> 25;
            case EPIC -> 15;
            default -> 50;
        };

        int step = (100 - baseChance) / Math.max(1, threshold);
        return Math.min(100, baseChance + (safeCount * step));
    }

    /**
     * Thuc hien co che ren lai day du theo chuan tai lieu thiet ke:
     * - Vung an toan (Junk, Common, Uncommon): 100% khong bao gio vo do.
     * - Vung mao hiem (Rare, Epic): Co rui ro vo do (Rare: 50%, Epic: 75%).
     * - Giam 15% rui ro vo do neu co Affinity (tu 25 diem tro len).
     * - Bao hiem Ngoc Luc Bao (Emerald Insurance): Ngoc nat thay cho do, cuu song trang bi.
     * - Nang cap luy tien: Moi lan dap xit tang ti le thanh cong cho lan toi.
     * - Bao hiem van may (Pity Guarantee): Dat nguong threshold chac chan 100% len bac cao hon.
     * - Bao hiem chong tut hang (Keep-Highest Guarantee).
     */
    public static ReforgeResult performReforge(QualityTier currentTier, int reforgeCount, int affinity, boolean hasEmeraldInsurance) {
        QualityTier effectiveCurrent = (currentTier != null) ? currentTier : QualityTier.COMMON;
        int safeCount = Math.max(0, reforgeCount);

        // Neu da dat cap toi da (Huyen thoai), giu nguyen va khong can dap them
        if (effectiveCurrent == QualityTier.LEGENDARY) {
            return new ReforgeResult(QualityTier.LEGENDARY, 0, false, false, false, false);
        }

        // Kiem tra Vung Mao Hiem (Danger Zone) cho Rare va Epic
        int pityThreshold = getPityThreshold(effectiveCurrent);
        boolean isGuaranteedPity = safeCount >= pityThreshold;

        if (!isGuaranteedPity && (effectiveCurrent == QualityTier.RARE || effectiveCurrent == QualityTier.EPIC)) {
            double baseBreakChance = (effectiveCurrent == QualityTier.RARE) ? 0.50 : 0.75;
            double affinityReduction = ItemAffinity.getBreakChanceReduction(affinity);
            double effectiveBreakChance = Math.max(0.0, baseBreakChance - affinityReduction);

            if (RANDOM.nextDouble() < effectiveBreakChance) {
                // Roi vao nguy co tach vo do!
                if (hasEmeraldInsurance) {
                    // Ve Bao Hiem Ngoc Luc Bao: Ngoc nat vun thay cho trang bi, cuu song mon do!
                    return new ReforgeResult(effectiveCurrent, safeCount + 1, false, false, false, true);
                } else {
                    // Khong co bao hiem: Mon do vo nat hoan toan!
                    return new ReforgeResult(null, 0, false, false, true, false);
                }
            }
        }

        int upgradeChance = getUpgradeChancePercent(effectiveCurrent, safeCount);
        boolean rollSuccess = (RANDOM.nextInt(100) < upgradeChance) || isGuaranteedPity;

        if (rollSuccess) {
            // Nang cap thanh cong len bac cao hon!
            QualityTier nextTier = getGuaranteedHigherTier(effectiveCurrent);
            return new ReforgeResult(nextTier, 0, true, isGuaranteedPity, false, false);
        } else {
            // Dap xit: Giu nguyen pham chat hien tai, tich luy them 1 lan cho Pity lan toi
            return new ReforgeResult(effectiveCurrent, safeCount + 1, false, false, false, false);
        }
    }

    private static QualityTier getGuaranteedHigherTier(QualityTier current) {
        QualityTier[] tiers = QualityTier.values();
        int currentOrd = current.ordinal();
        if (currentOrd >= tiers.length - 1) {
            return QualityTier.LEGENDARY;
        }

        // Bốc thăm trong số các bậc cao hơn bậc hiện tại theo tỉ lệ trọng số
        int totalWeight = 0;
        for (int i = currentOrd + 1; i < tiers.length; i++) {
            totalWeight += tiers[i].getWeight();
        }
        if (totalWeight <= 0) {
            return tiers[Math.min(tiers.length - 1, currentOrd + 1)];
        }

        int roll = RANDOM.nextInt(totalWeight);
        int currentWeight = 0;
        for (int i = currentOrd + 1; i < tiers.length; i++) {
            currentWeight += tiers[i].getWeight();
            if (roll < currentWeight) {
                return tiers[i];
            }
        }
        return tiers[Math.min(tiers.length - 1, currentOrd + 1)];
    }

    private static String normalizeId(String id) {
        String s = id.trim().toLowerCase();
        int colonIndex = s.indexOf(':');
        if (colonIndex >= 0) {
            return s.substring(colonIndex + 1);
        }
        return s;
    }
}