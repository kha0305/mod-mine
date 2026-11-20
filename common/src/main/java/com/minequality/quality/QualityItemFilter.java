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

    /**
     * Thực hiện cơ chế rèn lại đầy đủ theo chuẩn tài liệu thiết kế mục 7:
     * - Vùng an toàn (Junk, Common, Uncommon): 100% không bao giờ vỡ đồ.
     * - Vùng mạo hiểm (Rare, Epic): Có rủi ro vỡ đồ (Rare: 50%, Epic: 75%).
     * - Giảm 15% rủi ro vỡ đồ nếu có Affinity (từ 25 điểm trở lên).
     * - Bảo hiểm Ngọc Lục Bảo (Emerald Insurance): Ngọc nát thay cho đồ, cứu sống trang bị.
     * - Bảo hiểm vận may (Pity System): Thất bại 8 lần chắc chắn ra Epic/Legendary và không thể vỡ.
     * - Bảo hiểm chống tụt hạng (Keep-Highest Guarantee).
     */
    public static ReforgeResult performReforge(QualityTier currentTier, int reforgeCount, int affinity, boolean hasEmeraldInsurance) {
        QualityTier effectiveCurrent = (currentTier != null) ? currentTier : QualityTier.COMMON;
        int safeCount = Math.max(0, reforgeCount);

        // Nếu đã đạt cấp tối đa (Huyền thoại), giữ nguyên và không cần đập thêm
        if (effectiveCurrent == QualityTier.LEGENDARY) {
            return new ReforgeResult(QualityTier.LEGENDARY, 0, false, false, false, false);
        }

        // Kiểm tra Vùng Mạo Hiểm (Danger Zone) cho Rare và Epic
        if (safeCount < 8 && (effectiveCurrent == QualityTier.RARE || effectiveCurrent == QualityTier.EPIC)) {
            double baseBreakChance = (effectiveCurrent == QualityTier.RARE) ? 0.50 : 0.75;
            double affinityReduction = ItemAffinity.getBreakChanceReduction(affinity);
            double effectiveBreakChance = Math.max(0.0, baseBreakChance - affinityReduction);

            if (RANDOM.nextDouble() < effectiveBreakChance) {
                // Rơi vào nguy cơ tạch vỡ đồ!
                if (hasEmeraldInsurance) {
                    // Vé Bảo Hiểm Ngọc Lục Bảo: Ngọc nát vụn thay cho trang bị, cứu sống món đồ!
                    return new ReforgeResult(effectiveCurrent, safeCount + 1, false, false, false, true);
                } else {
                    // Không có bảo hiểm: Món đồ vỡ nát hoàn toàn!
                    return new ReforgeResult(null, 0, false, false, true, false);
                }
            }
        }

        boolean pityTriggered = false;
        QualityTier rolledTier;

        // Cơ chế Bảo Hiểm Tích Lũy Vận May (Pity System)
        if (safeCount >= 8) {
            // Mốc 8 lần đập: Chắc chắn ra Sử thi hoặc Huyền thoại (75% Sử thi, 25% Huyền thoại)
            pityTriggered = true;
            rolledTier = (RANDOM.nextInt(100) < 25) ? QualityTier.LEGENDARY : QualityTier.EPIC;
        } else if (safeCount >= 5) {
            // Mốc 5 lần đập: Đảm bảo tối thiểu nhận Bền chắc (Uncommon) trở lên
            pityTriggered = true;
            rolledTier = rollAboveOrEqual(QualityTier.UNCOMMON);
        } else if (safeCount >= 3) {
            // Mốc 3 lần đập: Loại bỏ hoàn toàn khả năng rơi vào Phế phẩm (Junk)
            pityTriggered = true;
            rolledTier = rollAboveOrEqual(QualityTier.COMMON);
        } else {
            // Dưới 3 lần: Bốc thăm trọng số thông thường
            rolledTier = QualityManager.rollQuality();
        }

        // Cơ chế Bảo Hiểm Chống Tụt Hạng: Không bao giờ bị rớt phẩm chất thấp hơn hiện tại
        if (rolledTier.ordinal() > effectiveCurrent.ordinal()) {
            // Nâng cấp thành công!
            return new ReforgeResult(rolledTier, 0, true, pityTriggered, false, false);
        } else {
            // Đập xịt hoặc bằng cấp cũ: Giữ nguyên phẩm chất cao nhất hiện có, tăng số lần tích lũy bảo hiểm
            return new ReforgeResult(effectiveCurrent, safeCount + 1, false, pityTriggered, false, false);
        }
    }

    private static QualityTier rollAboveOrEqual(QualityTier minTier) {
        QualityTier safeMinTier = (minTier != null) ? minTier : QualityTier.COMMON;
        QualityTier[] allTiers = QualityTier.values();
        int totalWeight = 0;
        for (QualityTier t : allTiers) {
            if (t.ordinal() >= safeMinTier.ordinal()) {
                totalWeight += t.getWeight();
            }
        }
        if (totalWeight <= 0) {
            return safeMinTier;
        }
        int roll = RANDOM.nextInt(totalWeight);
        int current = 0;
        for (QualityTier t : allTiers) {
            if (t.ordinal() >= safeMinTier.ordinal()) {
                current += t.getWeight();
                if (roll < current) {
                    return t;
                }
            }
        }
        return safeMinTier;
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