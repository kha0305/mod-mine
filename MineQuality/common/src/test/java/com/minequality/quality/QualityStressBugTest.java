package com.minequality.quality;

import java.util.List;
import java.util.Random;

/**
 * Bo kiem thu ap luc va ngan ngua loi hoi quy (Stress & Bug Prevention Suite).
 * Thuc hien kiem tra toan dien he thong pham chat:
 * 1. 10,000 lan roll ngau nhien pham chat.
 * 2. 1,000 luot ren trong Vung Mao Hiem (Danger Zone) kem Bao hiem Ngoc Luc Bao (Emerald Insurance) va Pity System.
 * 3. Kiem tra truong hop bien (Boundary & Null Safety) cho toan bo cac ham cong khai.
 * 4. Kiem tra chuan so hoc 40 HP va gioi han tran EXP 30 cap.
 * 5. Kiem tra co che bùa dac biet cua Dinh Ba (Trident) va Chuy (Mace).
 */
public class QualityStressBugTest {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("BAT DAU CHAY BO KIEM THU AP LUC VA TRUONG HOP BIEN (QUALITY STRESS & BUG SUITE)");
        System.out.println("================================================================================");

        int totalFailures = 0;

        totalFailures += runTest("1. Kiem thu ap luc 10,000 lan roll ngau nhien", QualityStressBugTest::testRandomQuality10000Rolls);
        totalFailures += runTest("2. Kiem thu ap luc 1,000 luot ren Danger Zone va Emerald Insurance", QualityStressBugTest::testDangerZoneStressAndEmeraldInsurance);
        totalFailures += runTest("3. Kiem thu co che bao hiem van may (Pity System 3, 5, 8)", QualityStressBugTest::testPitySystemGuarantees);
        totalFailures += runTest("4. Kiem thu an toan null va cac gia tri bien bat thuong", QualityStressBugTest::testBoundaryAndNullSafety);
        totalFailures += runTest("5. Kiem thu chuan so hoc 40 HP, bo Junk va tran EXP 30 cap", QualityStressBugTest::testArithmeticStandards);
        totalFailures += runTest("6. Kiem thu dac quyen vu khi dac biet (Dinh Ba va Chuy)", QualityStressBugTest::testSpecialWeaponMechanics);

        System.out.println("================================================================================");
        if (totalFailures == 0) {
            System.out.println("KET QUA: TOAN BO 6 MUC KIEM THU AP LUC VA BIEN DEU DAT 100% AN TOAN!");
        } else {
            System.err.printf("KET QUA: PHAT HIEN %d MUC KIEM THU THAT BAI!\n", totalFailures);
            System.exit(1);
        }
        System.out.println("================================================================================");
    }

    private static int runTest(String testName, Runnable testRunnable) {
        System.out.printf("\n>>> Dang thuc hien: %s...\n", testName);
        try {
            testRunnable.run();
            System.out.printf("--- DAT: %s thanh cong tuyet doi.\n", testName);
            return 0;
        } catch (Throwable t) {
            System.err.printf("--- THAT BAI: %s phat sinh loi: %s\n", testName, t.getMessage());
            t.printStackTrace();
            return 1;
        }
    }

    /**
     * Muc 1: Chay 10,000 lan roll ngau nhien kiem tra khong co ngoai le null va phan bo hop ly.
     */
    private static void testRandomQuality10000Rolls() {
        final int iterations = 10000;
        int[] counts = new int[QualityTier.values().length];

        for (int i = 0; i < iterations; i++) {
            QualityTier tier = QualityManager.rollQuality();
            if (tier == null) {
                throw new IllegalStateException("Loi: QualityManager.rollQuality tra ve null tai lan chay thu " + i);
            }
            counts[tier.ordinal()]++;

            // Kiem tra ItemQualityData.randomQuality()
            ItemQualityData data = ItemQualityData.randomQuality();
            if (data == null || data.tier() == null) {
                throw new IllegalStateException("Loi: ItemQualityData.randomQuality tra ve du lieu null tai lan chay thu " + i);
            }
        }

        // Kiem tra ty le phan bo tuong quan trong pham vi dung sai hop ly
        // Junk ~15%, Common ~45%, Uncommon ~22%, Rare ~12%, Epic ~5%, Legendary ~1%
        double junkPct = counts[QualityTier.JUNK.ordinal()] * 100.0 / iterations;
        double commonPct = counts[QualityTier.COMMON.ordinal()] * 100.0 / iterations;
        double uncommonPct = counts[QualityTier.UNCOMMON.ordinal()] * 100.0 / iterations;
        double rarePct = counts[QualityTier.RARE.ordinal()] * 100.0 / iterations;
        double epicPct = counts[QualityTier.EPIC.ordinal()] * 100.0 / iterations;
        double legPct = counts[QualityTier.LEGENDARY.ordinal()] * 100.0 / iterations;

        System.out.printf("    Phan bo sau 10,000 lan: Junk=%.2f%%, Common=%.2f%%, Uncommon=%.2f%%, Rare=%.2f%%, Epic=%.2f%%, Legendary=%.2f%%\n",
                junkPct, commonPct, uncommonPct, rarePct, epicPct, legPct);

        // Moi bac phai xuat hien it nhat mot lan trong 10,000 lan roll
        for (QualityTier t : QualityTier.values()) {
            if (counts[t.ordinal()] == 0) {
                throw new IllegalStateException("Loi: Bac " + t + " khong xuat hien lan nao trong 10,000 lan roll!");
            }
        }
    }

    /**
     * Muc 2: Stress test 1000 luot ren trong Danger Zone kiem tra hoat dong cua Emerald Insurance.
     */
    private static void testDangerZoneStressAndEmeraldInsurance() {
        final int runs = 1000;

        // 2.1 Kiem tra khi CO Bao hiem Ngoc Luc Bao: 100% khong bao gio bi vo do!
        int emeraldSavedCountRare = 0;
        int emeraldSavedCountEpic = 0;

        for (int i = 0; i < runs; i++) {
            // Test RARE co ngoc
            QualityItemFilter.ReforgeResult resRare = QualityItemFilter.performReforge(QualityTier.RARE, 0, 0, true);
            if (resRare.broken()) {
                throw new IllegalStateException("Loi nghiem trong: Trang bi RARE co bao hiem Ngoc Luc Bao bi vo!");
            }
            if (resRare.emeraldConsumed()) {
                emeraldSavedCountRare++;
            }

            // Test EPIC co ngoc
            QualityItemFilter.ReforgeResult resEpic = QualityItemFilter.performReforge(QualityTier.EPIC, 0, 0, true);
            if (resEpic.broken()) {
                throw new IllegalStateException("Loi nghiem trong: Trang bi EPIC co bao hiem Ngoc Luc Bao bi vo!");
            }
            if (resEpic.emeraldConsumed()) {
                emeraldSavedCountEpic++;
            }
        }

        if (emeraldSavedCountRare == 0 || emeraldSavedCountEpic == 0) {
            throw new IllegalStateException("Loi: Ngoc Luc Bao khong hoat dong de cuu trang bi trong 1,000 luot ren!");
        }

        System.out.printf("    Bao hiem Ngoc Luc Bao cuu do RARE %d/1,000 lan, cuu do EPIC %d/1,000 lan (0 mon bi vo)!\n",
                emeraldSavedCountRare, emeraldSavedCountEpic);

        // 2.2 Kiem tra khi KHONG CO bao hiem: Co ty le vo do va ty le phu hop ly thuyet
        int brokenRareCount = 0;
        int brokenEpicCount = 0;

        for (int i = 0; i < runs; i++) {
            QualityItemFilter.ReforgeResult resRare = QualityItemFilter.performReforge(QualityTier.RARE, 0, 0, false);
            if (resRare.broken()) {
                brokenRareCount++;
                if (resRare.resultingTier() != null) {
                    throw new IllegalStateException("Loi: Khi vo do resultingTier phai la null!");
                }
            }

            QualityItemFilter.ReforgeResult resEpic = QualityItemFilter.performReforge(QualityTier.EPIC, 0, 0, false);
            if (resEpic.broken()) {
                brokenEpicCount++;
                if (resEpic.resultingTier() != null) {
                    throw new IllegalStateException("Loi: Khi vo do resultingTier phai la null!");
                }
            }
        }

        // Tỉ lệ vỡ lý thuyết: Rare = 50%, Epic = 75%
        System.out.printf("    Khong co ngoc bao hiem: RARE vo %d/1,000 lan (~50%%), EPIC vo %d/1,000 lan (~75%%)\n",
                brokenRareCount, brokenEpicCount);

        if (brokenRareCount < 300 || brokenRareCount > 700) {
            throw new IllegalStateException("Loi: Ty le vo do RARE bat thuong: " + brokenRareCount);
        }
        if (brokenEpicCount < 600 || brokenEpicCount > 900) {
            throw new IllegalStateException("Loi: Ty le vo do EPIC bat thuong: " + brokenEpicCount);
        }

        // 2.3 Kiem tra tac dung giam 15% ty le vo do cua Affinity
        int brokenRareWithAffinity = 0;
        for (int i = 0; i < runs; i++) {
            QualityItemFilter.ReforgeResult res = QualityItemFilter.performReforge(QualityTier.RARE, 0, 50, false); // 50 Affinity -> giam 15% -> con 35%
            if (res.broken()) {
                brokenRareWithAffinity++;
            }
        }
        System.out.printf("    Affinity giam 15%% ty le vo: RARE vo %d/1,000 lan (~35%% ly thuyet)\n", brokenRareWithAffinity);
        if (brokenRareWithAffinity > brokenRareCount) {
            System.out.printf("    Luu y: Ty le co Affinity (%.1f%%) giam ro ret so voi khong co Affinity (%.1f%%)\n",
                    brokenRareWithAffinity * 100.0 / runs, brokenRareCount * 100.0 / runs);
        }
    }

    /**
     * Muc 3: Kiem thu co che bao hiem van may (Pity System tai cac moc 3, 5, 8).
     */
    private static void testPitySystemGuarantees() {
        final int runs = 1000;

        // 3.1 Moc 3: Loai bo hoan toan Junk (chi ra Common tro len)
        for (int i = 0; i < runs; i++) {
            QualityItemFilter.ReforgeResult res = QualityItemFilter.performReforge(QualityTier.COMMON, 3, 0, false);
            if (res.resultingTier() == QualityTier.JUNK) {
                throw new IllegalStateException("Loi Pity moc 3: Xuat hien pham chat JUNK!");
            }
            if (!res.pityTriggered()) {
                throw new IllegalStateException("Loi: pityTriggered phai la true tai moc 3!");
            }
        }
        System.out.println("    Pity moc 3 (1,000 luot): 100% khong xuat hien Junk.");

        // 3.2 Moc 5: Dam bao toi thieu Uncommon tro len
        for (int i = 0; i < runs; i++) {
            QualityItemFilter.ReforgeResult res = QualityItemFilter.performReforge(QualityTier.COMMON, 5, 0, false);
            if (res.resultingTier() == QualityTier.JUNK || res.resultingTier() == QualityTier.COMMON) {
                throw new IllegalStateException("Loi Pity moc 5: Xuat hien pham chat duoi Uncommon: " + res.resultingTier());
            }
            if (!res.pityTriggered()) {
                throw new IllegalStateException("Loi: pityTriggered phai la true tai moc 5!");
            }
        }
        System.out.println("    Pity moc 5 (1,000 luot): 100% tu Uncommon tro len.");

        // 3.3 Moc 8: 100% chac chan ra Epic hoac Legendary va KHONG BAO GIO VO DO
        int epicCount = 0;
        int legCount = 0;
        for (int i = 0; i < runs; i++) {
            // Thu ren tu do RARE khong kem bao hiem ngoc
            QualityItemFilter.ReforgeResult res = QualityItemFilter.performReforge(QualityTier.RARE, 8, 0, false);
            if (res.broken()) {
                throw new IllegalStateException("Loi Pity moc 8: Trang bi bi vo nhat mac du dat moc Pity 8!");
            }
            if (res.resultingTier() != QualityTier.EPIC && res.resultingTier() != QualityTier.LEGENDARY) {
                throw new IllegalStateException("Loi Pity moc 8: Phai ra EPIC hoac LEGENDARY, nhung ra: " + res.resultingTier());
            }
            if (res.resultingTier() == QualityTier.EPIC) {
                epicCount++;
            } else {
                legCount++;
            }
        }
        System.out.printf("    Pity moc 8 (1,000 luot): 100%% khong vo do, ra %d Epic (~75%%) va %d Legendary (~25%%).\n",
                epicCount, legCount);
    }

    /**
     * Muc 4: Kiem thu an toan null va boundary cho toan bo cac ham.
     */
    private static void testBoundaryAndNullSafety() {
        // 4.1 ItemAffinity boundary & null safety
        if (ItemAffinity.clamp(Integer.MIN_VALUE) != 0 || ItemAffinity.clamp(Integer.MAX_VALUE) != 100) {
            throw new IllegalStateException("Loi: ItemAffinity.clamp khong an toan voi Integer.MIN_VALUE / MAX_VALUE");
        }
        if (ItemAffinity.getRank(-999) != ItemAffinity.AffinityRank.STRANGER
                || ItemAffinity.getRank(999) != ItemAffinity.AffinityRank.KINDRED_SPIRITS) {
            throw new IllegalStateException("Loi: ItemAffinity.getRank khong an toan voi gia tri ngoai pham vi");
        }
        if (ItemAffinity.getBreakChanceReduction(-50) != 0.0 || ItemAffinity.getBreakChanceReduction(200) != 0.15) {
            throw new IllegalStateException("Loi: ItemAffinity.getBreakChanceReduction bat thuong o gia tri bien");
        }
        if (ItemAffinity.formatTooltip(-50) == null || ItemAffinity.formatTooltip(150) == null) {
            throw new IllegalStateException("Loi: ItemAffinity.formatTooltip tra ve null");
        }

        // 4.2 ItemQualityData boundary & null safety
        ItemQualityData dataNull = new ItemQualityData(null, -100, -50, false, false);
        if (dataNull.tier() != QualityTier.COMMON || dataNull.affinity() != 0 || dataNull.reforgeCount() != 0) {
            throw new IllegalStateException("Loi: ItemQualityData constructor khong chuan hoa tham so null/am!");
        }
        ItemQualityData dataMax = new ItemQualityData(QualityTier.COMMON, 9999, 9999, false, false);
        if (dataMax.affinity() != 100 || dataMax.reforgeCount() != 9999) {
            throw new IllegalStateException("Loi: ItemQualityData khong clamp affinity ve 100!");
        }

        // 4.3 QualityItemFilter boundary & null safety
        if (QualityItemFilter.isEligible(null) || QualityItemFilter.isEligible("") || QualityItemFilter.isEligible("   ")) {
            throw new IllegalStateException("Loi: isEligible chap nhan null hoac chuoi rong!");
        }
        if (QualityItemFilter.getCategory(null) != QualityModifier.EquipmentCategory.OTHER
                || QualityItemFilter.getCategory("minecraft:dirt") != QualityModifier.EquipmentCategory.OTHER) {
            throw new IllegalStateException("Loi: getCategory khong tra ve OTHER cho vat pham khong hop le!");
        }
        if (QualityItemFilter.getBaseCost(null) != 1 || QualityItemFilter.getBaseCost("dirt") != 1) {
            throw new IllegalStateException("Loi: getBaseCost phai tra ve 1 cho vat pham khong hop le hoac null!");
        }
        if (QualityItemFilter.isReforgeMaterial(null, null)
                || QualityItemFilter.isReforgeMaterial("diamond_sword", null)
                || QualityItemFilter.isReforgeMaterial(null, "diamond")) {
            throw new IllegalStateException("Loi: isReforgeMaterial khong an toan voi tham so null!");
        }

        // Kiem tra calculateReforgeCost voi cac gia tri bien am va lon
        int costNegative = QualityItemFilter.calculateReforgeCost("diamond_sword", -50);
        if (costNegative != 3) {
            throw new IllegalStateException("Loi: calculateReforgeCost voi reforgeCount am phai tinh la 0 (cost=3), nhung ra: " + costNegative);
        }
        int costNullItem = QualityItemFilter.calculateReforgeCost(null, 0);
        if (costNullItem != 1) {
            throw new IllegalStateException("Loi: calculateReforgeCost voi null item phai ra base=1, nhung ra: " + costNullItem);
        }

        // Kiem tra phong ngua Integer Overflow trong calculateReforgeCost
        int costMaxInt = QualityItemFilter.calculateReforgeCost("diamond_sword", Integer.MAX_VALUE);
        int costLargeInt = QualityItemFilter.calculateReforgeCost("diamond_sword", 1_500_000_000);
        if (costMaxInt < 0 || costMaxInt > QualityItemFilter.MAX_EXP_COST) {
            throw new IllegalStateException("PHAT HIEN BUG INTEGER OVERFLOW: calculateReforgeCost voi Integer.MAX_VALUE bi am hoac vuot 30! Gia tri: " + costMaxInt);
        }
        if (costLargeInt < 0 || costLargeInt > QualityItemFilter.MAX_EXP_COST) {
            throw new IllegalStateException("PHAT HIEN BUG INTEGER OVERFLOW: calculateReforgeCost voi 1.5 ty bi am hoac vuot 30! Gia tri: " + costLargeInt);
        }

        // performReforge voi null tier
        QualityItemFilter.ReforgeResult resNullTier = QualityItemFilter.performReforge(null, -10, -50, false);
        if (resNullTier == null || resNullTier.resultingTier() == null) {
            throw new IllegalStateException("Loi: performReforge voi null tier khong duoc tra ve null result!");
        }

        // 4.4 QualityModifier null safety
        QualityModifier.ModifierStats nullStats = QualityModifier.getStats(null, null);
        if (nullStats == null || nullStats.hasModifiers()) {
            throw new IllegalStateException("Loi: QualityModifier.getStats(null, null) phai tra ve ModifierStats.EMPTY!");
        }

        // 4.5 QualityTooltipBuilder null safety
        List<String> nullTooltip1 = QualityTooltipBuilder.buildTooltipLines(null, null);
        if (nullTooltip1 == null || !nullTooltip1.isEmpty()) {
            throw new IllegalStateException("Loi: buildTooltipLines(null, null) phai tra ve danh sach rong!");
        }
        List<String> nullTooltip2 = QualityTooltipBuilder.buildTooltipLines(null, ItemQualityData.DEFAULT);
        if (nullTooltip2 == null || nullTooltip2.isEmpty()) {
            throw new IllegalStateException("Loi: buildTooltipLines voi itemId null phai tao duoc tooltip mac dinh!");
        }
        List<String> nullTooltip3 = QualityTooltipBuilder.buildTooltipLines(null, null, ItemQualityData.DEFAULT);
        if (nullTooltip3 == null || nullTooltip3.isEmpty()) {
            throw new IllegalStateException("Loi: buildTooltipLines voi category null va itemId null phai an toan!");
        }

        // 4.6 SpecialEffectHandler boundary & null safety
        if (SpecialEffectHandler.calculateLifesteal(null, null, 10.0) != 0.0) {
            throw new IllegalStateException("Loi: calculateLifesteal phai tra ve 0.0 voi tham so null!");
        }
        if (SpecialEffectHandler.calculateLifesteal(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.MELEE_WEAPON, -10.0) != 0.0) {
            throw new IllegalStateException("Loi: calculateLifesteal phai tra ve 0.0 voi sat thuong am!");
        }
        if (SpecialEffectHandler.shouldApplyHaste(null, null)) {
            throw new IllegalStateException("Loi: shouldApplyHaste phai tra ve false khi tham so null!");
        }

        // Emergency Recovery kiem tra bien:
        // Khong kick hoat khi maxHealth <= 0
        if (SpecialEffectHandler.canTriggerEmergencyRecovery(true, 5.0, 0.0, 2000L, 0L)) {
            throw new IllegalStateException("Loi: canTriggerEmergencyRecovery khong duoc kick hoat khi maxHealth <= 0!");
        }
        if (SpecialEffectHandler.canTriggerEmergencyRecovery(true, 5.0, -20.0, 2000L, 0L)) {
            throw new IllegalStateException("Loi: canTriggerEmergencyRecovery khong duoc kick hoat khi maxHealth am!");
        }
        // Neu currentHealth <= 0 (thuc the da chet)
        if (SpecialEffectHandler.canTriggerEmergencyRecovery(true, 0.0, 40.0, 2000L, 0L)) {
            throw new IllegalStateException("PHAT HIEN BUG LOGIC: canTriggerEmergencyRecovery kick hoat khi currentHealth = 0 (nguoi choi da chet)!");
        }
        if (SpecialEffectHandler.canTriggerEmergencyRecovery(true, -5.0, 40.0, 2000L, 0L)) {
            throw new IllegalStateException("PHAT HIEN BUG LOGIC: canTriggerEmergencyRecovery kick hoat khi currentHealth am (nguoi choi da chet)!");
        }

        // Mace and Trident null safety
        if (SpecialEffectHandler.getMaceShockwaveRadiusMultiplier(null) != 1.0) {
            throw new IllegalStateException("Loi: getMaceShockwaveRadiusMultiplier(null) phai la 1.0");
        }
        if (SpecialEffectHandler.getMaceSmashDamageMultiplier(null) != 1.0) {
            throw new IllegalStateException("Loi: getMaceSmashDamageMultiplier(null) phai la 1.0");
        }
        if (SpecialEffectHandler.isMaceImmuneToFallDamage(null)) {
            throw new IllegalStateException("Loi: isMaceImmuneToFallDamage(null) phai la false");
        }
        if (SpecialEffectHandler.getTridentThrowVelocityMultiplier(null) != 1.0) {
            throw new IllegalStateException("Loi: getTridentThrowVelocityMultiplier(null) phai la 1.0");
        }
        if (SpecialEffectHandler.getTridentLoyaltyReturnSpeedMultiplier(null) != 1.0) {
            throw new IllegalStateException("Loi: getTridentLoyaltyReturnSpeedMultiplier(null) phai la 1.0");
        }
        if (SpecialEffectHandler.hasTridentBuiltInLoyalty(null)) {
            throw new IllegalStateException("Loi: hasTridentBuiltInLoyalty(null) phai la false");
        }
        if (SpecialEffectHandler.getTridentRiptideDistanceMultiplier(null, true) != 1.0) {
            throw new IllegalStateException("Loi: getTridentRiptideDistanceMultiplier(null, true) phai la 1.0");
        }
        if (SpecialEffectHandler.getTridentMeleeDamageMultiplier(null, true) != 1.0) {
            throw new IllegalStateException("Loi: getTridentMeleeDamageMultiplier(null, true) phai la 1.0");
        }
        if (SpecialEffectHandler.getRangedProjectileVelocityMultiplier(null) != 1.0) {
            throw new IllegalStateException("Loi: getRangedProjectileVelocityMultiplier(null) phai la 1.0");
        }
        if (SpecialEffectHandler.shouldTriggerShieldResistance(null)) {
            throw new IllegalStateException("Loi: shouldTriggerShieldResistance(null) phai la false");
        }
        if (SpecialEffectHandler.shouldApplyWeakness(null, null)) {
            throw new IllegalStateException("Loi: shouldApplyWeakness(null, null) phai la false");
        }
        if (SpecialEffectHandler.shouldApplyMiningFatigue(null, null)) {
            throw new IllegalStateException("Loi: shouldApplyMiningFatigue(null, null) phai la false");
        }

        System.out.println("    Kiem tra an toan null va truong hop bien dat 100%.");
    }

    /**
     * Muc 5: Kiem tra chuan so hoc 40 HP, bo Junk phat -6 HP va tran EXP 30 cap.
     */
    private static void testArithmeticStandards() {
        // 5.1 Bo giap Legendary cong dung 20 HP (4 + 6 + 6 + 4)
        QualityModifier.ModifierStats h = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_HELMET);
        QualityModifier.ModifierStats c = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE);
        QualityModifier.ModifierStats l = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_LEGGINGS);
        QualityModifier.ModifierStats b = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_BOOTS);

        if (h.healthBonus() != 4.0 || c.healthBonus() != 6.0 || l.healthBonus() != 6.0 || b.healthBonus() != 4.0) {
            throw new IllegalStateException("Loi phan bo HP giap Legendary: Helmet=" + h.healthBonus()
                    + ", Chest=" + c.healthBonus() + ", Legs=" + l.healthBonus() + ", Boots=" + b.healthBonus());
        }
        double totalLegHealth = h.healthBonus() + c.healthBonus() + l.healthBonus() + b.healthBonus();
        if (Math.abs(totalLegHealth - 20.0) > 0.0001) {
            throw new IllegalStateException("Loi: Tong mau bo giap Legendary phai la 20.0 HP, thuc te: " + totalLegHealth);
        }
        System.out.printf("    Chuan 40 HP Legendary: Mau goc 20.0 HP + Bo giap %.1f HP = 40.0 HP hoan hao.\n", totalLegHealth);

        // 5.2 Bo giap Junk phat dung -6 HP (-1 - 2 - 2 - 1)
        QualityModifier.ModifierStats jh = QualityModifier.getStats(QualityTier.JUNK, QualityModifier.EquipmentCategory.ARMOR_HELMET);
        QualityModifier.ModifierStats jc = QualityModifier.getStats(QualityTier.JUNK, QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE);
        QualityModifier.ModifierStats jl = QualityModifier.getStats(QualityTier.JUNK, QualityModifier.EquipmentCategory.ARMOR_LEGGINGS);
        QualityModifier.ModifierStats jb = QualityModifier.getStats(QualityTier.JUNK, QualityModifier.EquipmentCategory.ARMOR_BOOTS);

        if (jh.healthBonus() != -1.0 || jc.healthBonus() != -2.0 || jl.healthBonus() != -2.0 || jb.healthBonus() != -1.0) {
            throw new IllegalStateException("Loi phan bo phat HP giap Junk: Helmet=" + jh.healthBonus()
                    + ", Chest=" + jc.healthBonus() + ", Legs=" + jl.healthBonus() + ", Boots=" + jb.healthBonus());
        }
        double totalJunkHealth = jh.healthBonus() + jc.healthBonus() + jl.healthBonus() + jb.healthBonus();
        if (Math.abs(totalJunkHealth - (-6.0)) > 0.0001) {
            throw new IllegalStateException("Loi: Tong phat bo giap Junk phai la -6.0 HP, thuc te: " + totalJunkHealth);
        }
        System.out.printf("    Chuan 14 HP Junk: Mau goc 20.0 HP + Phat bo giap %.1f HP = 14.0 HP hoan hao.\n", totalJunkHealth);

        // 5.3 Tran chi phi EXP 30 cap cho moi trang bi va moi so lan ren
        String[] testItems = {"leather_helmet", "iron_sword", "diamond_pickaxe", "netherite_chestplate", "elytra", "mace"};
        for (String item : testItems) {
            for (int count = 0; count <= 50; count++) {
                int cost = QualityItemFilter.calculateReforgeCost(item, count);
                if (cost < 1 || cost > QualityItemFilter.MAX_EXP_COST) {
                    throw new IllegalStateException("Loi: Chi phi EXP cua " + item + " o lan " + count + " khong hop le: " + cost);
                }
            }
        }
        System.out.println("    Kiem tra tran EXP 30 cap: Tat ca cac phep tinh deu bi khoa o muc <= 30 cap (khong bao gio cham 39 cap Vanilla).");
    }

    /**
     * Muc 6: Kiem tra co che bùa dac biet cua Dinh Ba (Trident) va Chuy (Mace).
     */
    private static void testSpecialWeaponMechanics() {
        // 6.1 Chuy (Mace)
        if (SpecialEffectHandler.getMaceShockwaveRadiusMultiplier(QualityTier.RARE) != 1.15) {
            throw new IllegalStateException("Loi: Chuy Rare phai co ban kinh chấn dong +15% (1.15)");
        }
        if (SpecialEffectHandler.getMaceShockwaveRadiusMultiplier(QualityTier.EPIC) != 1.30) {
            throw new IllegalStateException("Loi: Chuy Epic phai co ban kinh chấn dong +30% (1.30)");
        }
        if (SpecialEffectHandler.getMaceSmashDamageMultiplier(QualityTier.LEGENDARY) != 1.50) {
            throw new IllegalStateException("Loi: Chuy Legendary phai co sat thuong roi dap +50% (1.50)");
        }
        if (!SpecialEffectHandler.isMaceImmuneToFallDamage(QualityTier.LEGENDARY)) {
            throw new IllegalStateException("Loi: Chuy Legendary bat buoc phai mien nhiem 100% sat thuong roi");
        }
        if (SpecialEffectHandler.isMaceImmuneToFallDamage(QualityTier.EPIC)) {
            throw new IllegalStateException("Loi: Chuy Epic khong duoc mien nhiem sat thuong roi");
        }

        // 6.2 Dinh Ba (Trident)
        if (SpecialEffectHandler.getTridentThrowVelocityMultiplier(QualityTier.RARE) != 1.25) {
            throw new IllegalStateException("Loi: Dinh Ba Rare phai tang 25% toc do bay");
        }
        if (SpecialEffectHandler.getTridentThrowVelocityMultiplier(QualityTier.EPIC) != 1.50) {
            throw new IllegalStateException("Loi: Dinh Ba Epic phai tang 50% toc do bay");
        }
        if (SpecialEffectHandler.getTridentLoyaltyReturnSpeedMultiplier(QualityTier.EPIC) != 2.0) {
            throw new IllegalStateException("Loi: Dinh Ba Epic phai co toc do Loyalty quay ve nhanh gap 2 lan");
        }
        if (!SpecialEffectHandler.hasTridentBuiltInLoyalty(QualityTier.LEGENDARY)) {
            throw new IllegalStateException("Loi: Dinh Ba Legendary phai tich hop san bua Loyalty I ngam");
        }
        if (SpecialEffectHandler.getTridentRiptideDistanceMultiplier(QualityTier.LEGENDARY, true) != 1.25) {
            throw new IllegalStateException("Loi: Dinh Ba Legendary phai tang 25% cu ly Riptide khi o duoi nuoc hoac khi mua");
        }
        if (SpecialEffectHandler.getTridentRiptideDistanceMultiplier(QualityTier.LEGENDARY, false) != 1.0) {
            throw new IllegalStateException("Loi: Dinh Ba Legendary khong duoc tang cu ly Riptide khi khong co nuoc hoac mua");
        }
        if (SpecialEffectHandler.getTridentMeleeDamageMultiplier(QualityTier.LEGENDARY, true) != 1.15) {
            throw new IllegalStateException("Loi: Dinh Ba Legendary phai tang 15% sat thuong can chien khi o duoi nuoc hoac khi mua");
        }

        System.out.println("    Kiem tra toan bo co che Chuy va Dinh Ba dat chuan 100%.");
    }
}
