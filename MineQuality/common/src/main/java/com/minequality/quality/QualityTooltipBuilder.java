package com.minequality.quality;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng danh sách chuỗi Tooltip hiển thị phẩm chất trang bị theo chuẩn tài liệu thiết kế (100% tiếng Anh en_us).
 */
public final class QualityTooltipBuilder {

    private QualityTooltipBuilder() {
    }

    /**
     * Xây dựng toàn bộ các dòng Tooltip cho một trang bị cụ thể.
     *
     * @param itemId Định danh vật phẩm (ví dụ "minecraft:diamond_chestplate").
     * @param data   Dữ liệu phẩm chất của vật phẩm.
     * @return Danh sách các dòng Tooltip bằng tiếng Anh chuẩn.
     */
    public static List<String> buildTooltipLines(String itemId, ItemQualityData data) {
        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(itemId);
        return buildTooltipLines(category, itemId, data);
    }

    /**
     * Xây dựng danh sách chuỗi Tooltip dựa theo nhóm trang bị, định danh vật phẩm và dữ liệu phẩm chất.
     */
    public static List<String> buildTooltipLines(QualityModifier.EquipmentCategory category, String itemId, ItemQualityData data) {
        List<String> lines = new ArrayList<>();
        if (data == null) {
            return lines;
        }

        QualityTier tier = (data.tier() != null) ? data.tier() : QualityTier.COMMON;
        QualityModifier.EquipmentCategory effectiveCategory = (category != null) ? category : QualityModifier.EquipmentCategory.OTHER;

        // 1. Dòng Phẩm chất (Quality: <TierName>)
        lines.add(String.format("Quality: %s", capitalize(tier.getId())));

        // 2. Dòng Gắn bó (Item Affinity: 100/100 (Kindred Spirits - Max Luck))
        lines.add(ItemAffinity.formatTooltip(data.affinity()));

        // 3. Chỉ số thuộc tính khi trang bị (Equipment Modifiers)
        QualityModifier.ModifierStats stats = QualityModifier.getStats(tier, effectiveCategory);
        List<String> statLines = formatModifierStats(stats, effectiveCategory, itemId);
        if (!statLines.isEmpty()) {
            lines.add("");
            lines.add(getSlotHeader(effectiveCategory));
            lines.addAll(statLines);
        }

        // 4. Hiệu ứng đặc biệt (Special Effects & Set Bonuses)
        List<String> effectLines = formatSpecialEffects(tier, effectiveCategory, itemId);
        if (!effectLines.isEmpty()) {
            lines.add("");
            lines.addAll(effectLines);
        }

        // 5. Cờ Bền Vĩnh Cửu & Chống Cháy (Unbreakable & Fireproof)
        String flagsLine = formatFlags(data.fireproof(), data.unbreakable());
        if (flagsLine != null) {
            lines.add("");
            lines.add(flagsLine);
        }

        // 6. Số lần rèn (Reforged: X times)
        lines.add(formatReforgedInfo(data.reforgeCount(), tier));

        return lines;
    }

    private static String getSlotHeader(QualityModifier.EquipmentCategory category) {
        return switch (category) {
            case ARMOR_HELMET -> "When on Head:";
            case ARMOR_CHESTPLATE, ELYTRA -> "When on Body:";
            case ARMOR_LEGGINGS -> "When on Legs:";
            case ARMOR_BOOTS -> "When on Feet:";
            case SHIELD -> "When in Off Hand:";
            case RANGED_WEAPON -> "When in Hand:";
            case MELEE_WEAPON, MINING_TOOL -> "When in Main Hand:";
            case OTHER -> "When Equipped:";
        };
    }

    private static List<String> formatModifierStats(QualityModifier.ModifierStats stats, QualityModifier.EquipmentCategory category, String itemId) {
        List<String> list = new ArrayList<>();
        if (stats == null || !stats.hasModifiers()) {
            return list;
        }

        // Máu tối đa (Max Health)
        if (stats.healthBonus() != 0) {
            double hp = stats.healthBonus();
            double hearts = Math.abs(hp) / 2.0;
            String heartStr = (hearts == (long) hearts) ? String.format("%d", (long) hearts) : String.format("%.1f", hearts);
            if (hp > 0) {
                list.add(String.format("  +%.0f Max Health (+%s Hearts)", hp, heartStr));
            } else {
                list.add(String.format("  %.0f Max Health (-%s Hearts)", hp, heartStr));
            }
        }

        // Sát thương (Attack Damage / Projectile Damage)
        if (stats.damageMultiplier() != 0) {
            double pct = stats.damageMultiplier() * 100.0;
            String label = (category == QualityModifier.EquipmentCategory.RANGED_WEAPON) ? "Projectile Damage" : "Attack Damage";
            list.add(String.format("  %+,.0f%% %s", pct, label));
        }

        // Tốc độ đánh (Attack Speed)
        if (stats.attackSpeedMultiplier() != 0) {
            list.add(String.format("  %+,.0f%% Attack Speed", stats.attackSpeedMultiplier() * 100.0));
        }

        // Tốc độ đào (Mining Speed)
        if (stats.miningSpeedMultiplier() != 0) {
            list.add(String.format("  %+,.0f%% Mining Speed", stats.miningSpeedMultiplier() * 100.0));
        }

        // Tốc độ di chuyển (Movement Speed)
        if (stats.movementSpeedMultiplier() != 0) {
            list.add(String.format("  %+,.0f%% Movement Speed", stats.movementSpeedMultiplier() * 100.0));
        }

        // Điểm may mắn (Luck)
        if (stats.luckBonus() != 0) {
            list.add(String.format("  %+,.0f Luck", stats.luckBonus()));
        }

        // Giáp (Armor)
        if (stats.armorBonus() != 0) {
            list.add(String.format("  %+.1f Armor", stats.armorBonus()));
        }

        // Độ cứng giáp (Armor Toughness)
        if (stats.toughnessBonus() != 0) {
            list.add(String.format("  %+.1f Armor Toughness", stats.toughnessBonus()));
        }

        // Kháng đẩy lùi (Knockback Resistance)
        if (stats.knockbackResistanceBonus() != 0) {
            list.add(String.format("  %+,.0f%% Knockback Resistance", stats.knockbackResistanceBonus() * 100.0));
        }

        // Độ bền (Max Durability)
        if (stats.durabilityMultiplier() != 0) {
            list.add(String.format("  %+,.0f%% Max Durability", stats.durabilityMultiplier() * 100.0));
        }

        return list;
    }

    private static List<String> formatSpecialEffects(QualityTier tier, QualityModifier.EquipmentCategory category, String itemId) {
        List<String> list = new ArrayList<>();
        String normalized = (itemId != null) ? itemId.toLowerCase() : "";

        // Kiểm tra vũ khí đặc biệt Chùy (Mace) và Đinh Ba (Trident)
        if (normalized.contains("mace")) {
            if (tier == QualityTier.RARE) {
                list.add("Special: +15% Shockwave Radius");
            } else if (tier == QualityTier.EPIC) {
                list.add("Special: +30% Shockwave Radius");
                list.add("Special: Reduced Minimum Fall Height for Smash");
            } else if (tier == QualityTier.LEGENDARY) {
                list.add("Special: +50% Smash Damage");
                list.add("Special: Zero Fall Damage on Hit");
            }
        } else if (normalized.contains("trident")) {
            if (tier == QualityTier.RARE) {
                list.add("Special: +25% Throw Velocity");
            } else if (tier == QualityTier.EPIC) {
                list.add("Special: +50% Throw Velocity & 2x Loyalty Recall");
            } else if (tier == QualityTier.LEGENDARY) {
                list.add("Special: Built-in Loyalty I");
                list.add("Special: +25% Riptide Distance & +15% Melee in Water/Rain");
            }
        }

        // Hiệu ứng chung theo bậc và nhóm
        if (tier == QualityTier.LEGENDARY) {
            if (isArmor(category)) {
                list.add("Set Bonus: Permanent Fire Resistance");
                list.add("Set Bonus: Passive Vitality (Slow Health & Hunger Recovery)");
                list.add("Set Bonus: Emergency Recovery (<30% HP: Res I + Regen II)");
            } else if (category == QualityModifier.EquipmentCategory.MELEE_WEAPON && !normalized.contains("mace") && !normalized.contains("trident")) {
                list.add("Special: 20% Lifesteal on Hit");
            } else if (category == QualityModifier.EquipmentCategory.MINING_TOOL) {
                list.add("Passive: Permanent Haste II while held");
            } else if (category == QualityModifier.EquipmentCategory.RANGED_WEAPON) {
                list.add("Special: +25% Projectile Velocity & True Flight");
            } else if (category == QualityModifier.EquipmentCategory.SHIELD) {
                list.add("Special: Resistance II on Heavy Block");
            } else if (category == QualityModifier.EquipmentCategory.ELYTRA) {
                list.add("Passive: Infinite Flight Duration");
            }
        } else if (tier == QualityTier.JUNK) {
            if (category == QualityModifier.EquipmentCategory.MELEE_WEAPON) {
                list.add("Debuff: Weakness I while held");
            } else if (category == QualityModifier.EquipmentCategory.MINING_TOOL) {
                list.add("Debuff: Mining Fatigue I while held");
            }
        }

        return list;
    }

    private static boolean isArmor(QualityModifier.EquipmentCategory category) {
        return category == QualityModifier.EquipmentCategory.ARMOR_HELMET
                || category == QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE
                || category == QualityModifier.EquipmentCategory.ARMOR_LEGGINGS
                || category == QualityModifier.EquipmentCategory.ARMOR_BOOTS;
    }

    private static String formatFlags(boolean fireproof, boolean unbreakable) {
        if (fireproof && unbreakable) {
            return "Unbreakable & Fireproof";
        }
        if (unbreakable) {
            return "Unbreakable";
        }
        if (fireproof) {
            return "Fireproof";
        }
        return null;
    }

    private static String formatReforgedInfo(int reforgeCount, QualityTier tier) {
        int safeCount = Math.max(0, reforgeCount);
        if (tier == QualityTier.LEGENDARY) {
            return String.format("Reforged: %d times (Max Quality Reached)", safeCount);
        }
        int threshold = QualityItemFilter.getPityThreshold(tier);
        int upgradeChance = QualityItemFilter.getUpgradeChancePercent(tier, safeCount);
        if (safeCount >= threshold) {
            return String.format("Reforged: %d times (Pity: %d/%d - Guaranteed Next!)", safeCount, safeCount, threshold);
        }
        return String.format("Reforged: %d times (Pity: %d/%d - %d%% Upgrade Chance)", safeCount, safeCount, threshold, upgradeChance);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}