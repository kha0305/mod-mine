package com.minequality.quality;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Định nghĩa và tính toán toàn bộ chỉ số thưởng và phạt cho từng cặp bậc phẩm chất và nhóm trang bị.
 */
public final class QualityModifier {

    /**
     * Phân loại các nhóm trang bị trong game.
     */
    public enum EquipmentCategory {
        MELEE_WEAPON,
        MINING_TOOL,
        RANGED_WEAPON,
        ARMOR_HELMET,
        ARMOR_CHESTPLATE,
        ARMOR_LEGGINGS,
        ARMOR_BOOTS,
        SHIELD,
        ELYTRA,
        OTHER
    }

    /**
     * Bản ghi chứa các giá trị chỉ số thưởng hoặc phạt của một phẩm chất.
     *
     * @param healthBonus              Lượng máu tối đa cộng thêm trực tiếp (Operation ADDITION).
     * @param damageMultiplier         Tỷ lệ tăng sát thương cận chiến hoặc tầm xa (Operation MULTIPLY_BASE).
     * @param attackSpeedMultiplier    Tỷ lệ tăng tốc độ đánh (Operation MULTIPLY_BASE).
     * @param miningSpeedMultiplier    Tỷ lệ tăng tốc độ khai thác (Operation MULTIPLY_BASE).
     * @param movementSpeedMultiplier  Tỷ lệ tăng tốc độ di chuyển (Operation MULTIPLY_BASE).
     * @param luckBonus                Chỉ số may mắn cộng thêm trực tiếp (Operation ADDITION).
     * @param durabilityMultiplier     Tỷ lệ tăng độ bền tối đa của vật phẩm.
     * @param armorBonus               Điểm giáp cộng thêm trực tiếp.
     * @param toughnessBonus           Điểm độ cứng giáp cộng thêm trực tiếp.
     * @param knockbackResistanceBonus Kháng đẩy lùi cộng thêm trực tiếp.
     */
    public record ModifierStats(
            double healthBonus,
            double damageMultiplier,
            double attackSpeedMultiplier,
            double miningSpeedMultiplier,
            double movementSpeedMultiplier,
            double luckBonus,
            double durabilityMultiplier,
            double armorBonus,
            double toughnessBonus,
            double knockbackResistanceBonus
    ) {
        public static final ModifierStats EMPTY = new ModifierStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        /**
         * Constructor tương thích với 8 tham số cũ.
         */
        public ModifierStats(
                double healthBonus,
                double damageMultiplier,
                double attackSpeedMultiplier,
                double miningSpeedMultiplier,
                double durabilityMultiplier,
                double armorBonus,
                double toughnessBonus,
                double knockbackResistanceBonus
        ) {
            this(healthBonus, damageMultiplier, attackSpeedMultiplier, miningSpeedMultiplier,
                    0.0, 0.0, durabilityMultiplier, armorBonus, toughnessBonus, knockbackResistanceBonus);
        }

        public boolean hasModifiers() {
            return healthBonus != 0
                    || damageMultiplier != 0
                    || attackSpeedMultiplier != 0
                    || miningSpeedMultiplier != 0
                    || movementSpeedMultiplier != 0
                    || luckBonus != 0
                    || durabilityMultiplier != 0
                    || armorBonus != 0
                    || toughnessBonus != 0
                    || knockbackResistanceBonus != 0;
        }
    }

    private static final Map<QualityTier, Map<EquipmentCategory, ModifierStats>> STATS_MAP = new EnumMap<>(QualityTier.class);

    static {
        for (QualityTier tier : QualityTier.values()) {
            Map<EquipmentCategory, ModifierStats> categoryMap = new EnumMap<>(EquipmentCategory.class);
            for (EquipmentCategory category : EquipmentCategory.values()) {
                categoryMap.put(category, buildStats(tier, category));
            }
            STATS_MAP.put(tier, Collections.unmodifiableMap(categoryMap));
        }
    }

    private QualityModifier() {
    }

    /**
     * Lấy bảng chỉ số chi tiết cho một bậc phẩm chất và một nhóm trang bị cụ thể.
     */
    public static ModifierStats getStats(QualityTier tier, EquipmentCategory category) {
        if (tier == null || category == null) {
            return ModifierStats.EMPTY;
        }
        Map<EquipmentCategory, ModifierStats> categoryMap = STATS_MAP.get(tier);
        if (categoryMap == null) {
            return ModifierStats.EMPTY;
        }
        return categoryMap.getOrDefault(category, ModifierStats.EMPTY);
    }

    private static ModifierStats buildStats(QualityTier tier, EquipmentCategory category) {
        return switch (tier) {
            case JUNK -> buildJunk(category);
            case COMMON -> ModifierStats.EMPTY;
            case UNCOMMON -> buildUncommon(category);
            case RARE -> buildRare(category);
            case EPIC -> buildEpic(category);
            case LEGENDARY -> buildLegendary(category);
        };
    }

    private static ModifierStats buildJunk(EquipmentCategory category) {
        return switch (category) {
            case MELEE_WEAPON -> new ModifierStats(0, -0.15, -0.10, 0, 0, 0, -0.20, 0, 0, 0);
            case MINING_TOOL -> new ModifierStats(0, 0, -0.10, -0.15, 0, 0, -0.20, 0, 0, 0);
            case RANGED_WEAPON -> new ModifierStats(0, -0.15, 0, 0, 0, 0, -0.20, 0, 0, 0);
            case ARMOR_HELMET -> new ModifierStats(-1.0, 0, 0, 0, 0, 0, -0.20, -0.5, 0, 0);
            case ARMOR_CHESTPLATE -> new ModifierStats(-2.0, 0, 0, 0, 0, 0, -0.20, -1.0, 0, 0);
            case ARMOR_LEGGINGS -> new ModifierStats(-2.0, 0, 0, 0, 0, 0, -0.20, -1.0, 0, 0);
            case ARMOR_BOOTS -> new ModifierStats(-1.0, 0, 0, 0, 0, 0, -0.20, -0.5, 0, 0);
            case SHIELD -> new ModifierStats(0, 0, 0, 0, 0, 0, -0.20, 0, -1.0, 0);
            case ELYTRA -> new ModifierStats(0, 0, 0, 0, 0, 0, -0.20, 0, 0, 0);
            case OTHER -> ModifierStats.EMPTY;
        };
    }

    private static ModifierStats buildUncommon(EquipmentCategory category) {
        return switch (category) {
            case MELEE_WEAPON -> new ModifierStats(0, 0.05, 0, 0, 0, 0, 0.10, 0, 0, 0);
            case MINING_TOOL -> new ModifierStats(0, 0, 0, 0.05, 0, 0, 0.10, 0, 0, 0);
            case RANGED_WEAPON -> new ModifierStats(0, 0.05, 0, 0, 0, 0, 0.10, 0, 0, 0);
            case ARMOR_HELMET -> new ModifierStats(1.0, 0, 0, 0, 0, 0, 0.10, 0, 0, 0);
            case ARMOR_CHESTPLATE -> new ModifierStats(1.0, 0, 0, 0, 0, 0, 0.10, 0, 0, 0);
            case ARMOR_LEGGINGS -> new ModifierStats(1.0, 0, 0, 0, 0, 0, 0.10, 0, 0, 0);
            case ARMOR_BOOTS -> new ModifierStats(1.0, 0, 0, 0, 0, 0, 0.10, 0, 0, 0);
            case SHIELD -> new ModifierStats(0, 0, 0, 0, 0, 0, 0.10, 0, 0, 0);
            case ELYTRA -> new ModifierStats(0, 0, 0, 0, 0, 0, 0.10, 0, 0, 0);
            case OTHER -> ModifierStats.EMPTY;
        };
    }

    private static ModifierStats buildRare(EquipmentCategory category) {
        return switch (category) {
            case MELEE_WEAPON -> new ModifierStats(0, 0.10, 0.10, 0, 0, 0, 0.20, 0, 0, 0);
            case MINING_TOOL -> new ModifierStats(0, 0, 0.10, 0.10, 0, 0, 0.20, 0, 0, 0);
            case RANGED_WEAPON -> new ModifierStats(0, 0.10, 0, 0, 0.05, 0, 0.20, 0, 0, 0);
            case ARMOR_HELMET -> new ModifierStats(2.0, 0, 0, 0, 0, 0, 0.20, 0.5, 0.5, 0);
            case ARMOR_CHESTPLATE -> new ModifierStats(2.0, 0, 0, 0, 0, 0, 0.20, 0.5, 0.5, 0);
            case ARMOR_LEGGINGS -> new ModifierStats(2.0, 0, 0, 0, 0, 0, 0.20, 0.5, 0.5, 0);
            case ARMOR_BOOTS -> new ModifierStats(2.0, 0, 0, 0, 0, 0, 0.20, 0.5, 0.5, 0);
            case SHIELD -> new ModifierStats(0, 0, 0, 0, 0, 0, 0.20, 0, 1.0, 0);
            case ELYTRA -> new ModifierStats(0, 0, 0, 0, 0, 0, 0.20, 1.0, 0, 0);
            case OTHER -> ModifierStats.EMPTY;
        };
    }

    private static ModifierStats buildEpic(EquipmentCategory category) {
        return switch (category) {
            case MELEE_WEAPON -> new ModifierStats(0, 0.20, 0.15, 0, 0.05, 0, 0.35, 0, 0, 0);
            case MINING_TOOL -> new ModifierStats(0, 0, 0.15, 0.20, 0.05, 0, 0.35, 0, 0, 0);
            case RANGED_WEAPON -> new ModifierStats(0, 0.20, 0, 0, 0.10, 0, 0.35, 0, 0, 0);
            case ARMOR_HELMET -> new ModifierStats(3.0, 0, 0, 0, 0, 0, 0.35, 1.0, 1.0, 0.03);
            case ARMOR_CHESTPLATE -> new ModifierStats(4.0, 0, 0, 0, 0, 0, 0.35, 1.0, 1.0, 0.03);
            case ARMOR_LEGGINGS -> new ModifierStats(4.0, 0, 0, 0, 0, 0, 0.35, 1.0, 1.0, 0.03);
            case ARMOR_BOOTS -> new ModifierStats(3.0, 0, 0, 0, 0, 0, 0.35, 1.0, 1.0, 0.03);
            case SHIELD -> new ModifierStats(0, 0, 0, 0, 0, 0, 0.35, 1.0, 1.5, 0.10);
            case ELYTRA -> new ModifierStats(0, 0, 0, 0, 0.05, 0, 0.35, 2.0, 1.0, 0);
            case OTHER -> ModifierStats.EMPTY;
        };
    }

    private static ModifierStats buildLegendary(EquipmentCategory category) {
        return switch (category) {
            case MELEE_WEAPON -> new ModifierStats(0, 0.35, 0.25, 0, 0.10, 1.0, 0.50, 0, 0, 0);
            case MINING_TOOL -> new ModifierStats(0, 0, 0.25, 0.35, 0.10, 1.0, 0.50, 0, 0, 0);
            case RANGED_WEAPON -> new ModifierStats(0, 0.35, 0, 0, 0.15, 1.0, 0.50, 0, 0, 0);
            // Tổng 4 món giáp đạt đúng +20 HP (4 + 6 + 6 + 4), kết hợp với 20 HP gốc của nhân vật chạm đúng mốc 40 HP chuẩn
            case ARMOR_HELMET -> new ModifierStats(4.0, 0, 0, 0, 0, 0, 0.50, 1.5, 2.0, 0.05);
            case ARMOR_CHESTPLATE -> new ModifierStats(6.0, 0, 0, 0, 0, 0, 0.50, 2.0, 2.0, 0.05);
            case ARMOR_LEGGINGS -> new ModifierStats(6.0, 0, 0, 0, 0, 0, 0.50, 2.0, 2.0, 0.05);
            case ARMOR_BOOTS -> new ModifierStats(4.0, 0, 0, 0, 0, 0, 0.50, 1.5, 2.0, 0.05);
            case SHIELD -> new ModifierStats(0, 0, 0, 0, 0, 0, 0.50, 2.0, 2.0, 0.20);
            case ELYTRA -> new ModifierStats(0, 0, 0, 0, 0.10, 0, 0.50, 3.0, 2.0, 0);
            case OTHER -> ModifierStats.EMPTY;
        };
    }
}