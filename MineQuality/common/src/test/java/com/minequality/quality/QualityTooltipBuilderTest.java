package com.minequality.quality;

import java.util.List;

/**
 * Kiểm thử tính đúng đắn của bộ sinh Tooltip QualityTooltipBuilder.
 */
public class QualityTooltipBuilderTest {

    public static void main(String[] args) {
        System.out.println("Bat dau kiem thu QualityTooltipBuilder...");

        // 1. Kiem thu Tooltip cho Ao Giap Kim Cuong Huyen Thoai (Moc 8 tai lieu thiet ke)
        ItemQualityData legendChestData = new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true);
        List<String> chestLines = QualityTooltipBuilder.buildTooltipLines("minecraft:diamond_chestplate", legendChestData);

        // Kiem tra cac dong bat buoc theo bang mau muc 8
        assertContains(chestLines, "Quality: Legendary");
        assertContains(chestLines, "Item Affinity: 100/100 (Kindred Spirits - Max Luck)");
        assertContains(chestLines, "When on Body:");
        assertContains(chestLines, "  +6 Max Health (+3 Hearts)");
        assertContains(chestLines, "  +2.0 Armor");
        assertContains(chestLines, "  +2.0 Armor Toughness");
        assertContains(chestLines, "  +5% Knockback Resistance");
        assertContains(chestLines, "Set Bonus: Permanent Fire Resistance");
        assertContains(chestLines, "Set Bonus: Emergency Recovery (<30% HP: Res I + Regen I)");
        assertContains(chestLines, "Unbreakable & Fireproof");
        assertContains(chestLines, "Reforged: 0 times (Max Quality Reached)");
        System.out.println("Kiem tra Tooltip chuan muc 8 cho Diamond Chestplate Legendary dat 100%!");

        // 2. Kiem thu Tooltip cho Kiem Phe Pham (Junk Melee Weapon)
        ItemQualityData junkSwordData = new ItemQualityData(QualityTier.JUNK, 10, 2, false, false);
        List<String> swordLines = QualityTooltipBuilder.buildTooltipLines("minecraft:iron_sword", junkSwordData);

        assertContains(swordLines, "Quality: Junk");
        assertContains(swordLines, "Item Affinity: 10/100 (Stranger)");
        assertContains(swordLines, "When in Main Hand:");
        assertContains(swordLines, "  -15% Attack Damage");
        assertContains(swordLines, "  -10% Attack Speed");
        assertContains(swordLines, "  -20% Max Durability");
        assertContains(swordLines, "Debuff: Weakness I while held");
        assertContains(swordLines, "Reforged: 2 times");
        System.out.println("Kiem tra Tooltip cho Junk Melee Weapon dat 100%!");

        // 3. Kiem thu Tooltip cho Cup Khai Thac Huyen Thoai (Mining Tool Legendary)
        ItemQualityData legendPickData = new ItemQualityData(QualityTier.LEGENDARY, 60, 1, true, true);
        List<String> pickLines = QualityTooltipBuilder.buildTooltipLines("minecraft:netherite_pickaxe", legendPickData);

        assertContains(pickLines, "Quality: Legendary");
        assertContains(pickLines, "Item Affinity: 60/100 (Trusted Companion)");
        assertContains(pickLines, "  +35% Mining Speed");
        assertContains(pickLines, "  +25% Attack Speed");
        assertContains(pickLines, "  +10% Movement Speed");
        assertContains(pickLines, "  +1 Luck");
        assertContains(pickLines, "Passive: Permanent Haste II while held");
        assertContains(pickLines, "Unbreakable & Fireproof");
        System.out.println("Kiem tra Tooltip cho Mining Tool Legendary dat 100%!");

        // 4. Kiem thu Tooltip cho Vu khi dac biet Chuy (Mace) va Dinh Ba (Trident)
        ItemQualityData maceData = new ItemQualityData(QualityTier.LEGENDARY, 0, 0, true, true);
        List<String> maceLines = QualityTooltipBuilder.buildTooltipLines("minecraft:mace", maceData);
        assertContains(maceLines, "Special: +50% Smash Damage");
        assertContains(maceLines, "Special: Zero Fall Damage on Hit");

        ItemQualityData tridentData = new ItemQualityData(QualityTier.LEGENDARY, 0, 0, true, true);
        List<String> tridentLines = QualityTooltipBuilder.buildTooltipLines("minecraft:trident", tridentData);
        assertContains(tridentLines, "Special: Built-in Loyalty I");
        assertContains(tridentLines, "Special: +25% Riptide Distance & +15% Melee in Water/Rain");
        System.out.println("Kiem tra Tooltip cho Mace va Trident dat 100%!");

        System.out.println("Kiem thu QualityTooltipBuilder thanh cong dat 100%!");
    }

    private static void assertContains(List<String> lines, String expected) {
        for (String line : lines) {
            if (line.contains(expected)) {
                return;
            }
        }
        throw new IllegalStateException("Loi: Tooltip thieu dong mong doi: \"" + expected + "\"\nThuc te gom cac dong:\n" + String.join("\n", lines));
    }
}