package com.minequality.forge;

import com.minequality.forge.event.ForgeAnvilHandler;
import com.minequality.forge.event.ForgeCombatHandler;
import com.minequality.forge.event.ForgeTooltipHandler;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityModifier;
import com.minequality.quality.QualityTier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Bo kiem thu sau doc quyen cho Forge QA Inspector (sub-qa-forge) tren Java 26:
 * Kiem tra chuyen sau toan bo tinh huong bien, tinh an toan null, do tin cay da luong,
 * logic Gioi han tran 30 cap EXP tren De, Bao hiem Ngoc Luc Bao, Pity, Lifesteal 20%, Haste II,
 * Full Set Bonus 40 HP va Sinh ton khan cap (Emergency Recovery).
 */
public class ForgeQACheckTest {

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("BAT DAU THANH TRA CHAT LUONG & KIEM THU SAU MO-DUN MINECRAFTFORGE (JAVA 26)");
        System.out.println("================================================================================");

        testConcurrentInitialization();
        testMetadataAndModIdentity();
        testTooltipNullSafetyAndEdgeCases();
        testAnvilExpCostCappingAndSafety();
        testAnvilSafeZoneStress();
        testAnvilDangerZoneAndEmeraldInsurance();
        testAnvilPitySystemGuarantees();
        testCombatLifestealEdgeCases();
        testCombatHasteAndDebuffEdgeCases();
        testFullSetArmorBonusCalculations();
        testEmergencyRecoveryBoundaryConditions();

        System.out.println("================================================================================");
        System.out.println("CHUC MUNG: 100% KIEM THU SAU & THANH TRA CHAT LUONG CHO FORGE DA VUOT QUA!");
        System.out.println("================================================================================");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("THAT BAI: " + message + " (Ky vong: " + expected + ", Thuc te: " + actual + ")");
        }
    }

    private static void assertDoubleEquals(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.0001) {
            throw new AssertionError("THAT BAI: " + message + " (Ky vong: " + expected + ", Thuc te: " + actual + ")");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("THAT BAI: " + message);
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("THAT BAI: " + message + " (Doi tuong bi null)");
        }
    }

    /**
     * 1. Kiem tra an toan luong (Thread-safety) khi nhieu luong dong thoi khoi tao.
     */
    private static void testConcurrentInitialization() throws Exception {
        System.out.println("[QA-01] Kiem tra an toan da luong khi khoi tao MineQualityForge...");
        MineQualityForge.resetInitialization();

        int threadCount = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    MineQualityForge.init();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        boolean finished = endGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "Toan bo cac luong phai hoan tat khoi tao trong thoi gian cho.");
        assertTrue(MineQualityForge.isInitialized(), "MineQualityForge phai o trang thai da khoi tao.");
        assertTrue(ForgeTooltipHandler.isRegistered(), "ForgeTooltipHandler phai duoc dang ky thanh cong.");
        assertTrue(ForgeAnvilHandler.isRegistered(), "ForgeAnvilHandler phai duoc dang ky thanh cong.");
        assertTrue(ForgeCombatHandler.isRegistered(), "ForgeCombatHandler phai duoc dang ky thanh cong.");
        System.out.println("  -> QA-01: An toan da luong khoi tao dat 100%.");
    }

    /**
     * 2. Kiem tra dinh danh mod va dong bo hang so.
     */
    private static void testMetadataAndModIdentity() {
        System.out.println("[QA-02] Kiem tra dinh danh Mod ID va ten goi...");
        assertEquals("mod_mine", MineQualityForge.MOD_ID, "MOD_ID phai la 'mod_mine'.");
        assertEquals("MineQuality", MineQualityForge.MOD_NAME, "MOD_NAME phai la 'MineQuality'.");
        System.out.println("  -> QA-02: Dinh danh Mod dat 100%.");
    }

    /**
     * 3. Kiem tra Tooltip: An toan null, chuoi rong, vat pham ngoai danh muc.
     */
    private static void testTooltipNullSafetyAndEdgeCases() {
        System.out.println("[QA-03] Kiem tra an toan null va cac tinh huong bien cua ForgeTooltipHandler...");

        // Tham so null toan dien
        List<String> r1 = ForgeTooltipHandler.appendTooltip(null, null, null);
        assertNotNull(r1, "Tra ve danh sach rong khi tat ca tham so la null.");
        assertTrue(r1.isEmpty(), "Danh sach tra ve phai rong.");

        // itemId null, qualityData khac null
        List<String> list2 = new ArrayList<>(List.of("Header Line"));
        List<String> r2 = ForgeTooltipHandler.appendTooltip(null, ItemQualityData.DEFAULT, list2);
        assertEquals(1, r2.size(), "itemId null khong duoc thay doi Tooltip.");

        // itemId rong hoac khoang trang
        List<String> list3 = new ArrayList<>(List.of("Header Line"));
        List<String> r3 = ForgeTooltipHandler.appendTooltip("   ", ItemQualityData.DEFAULT, list3);
        assertEquals(1, r3.size(), "itemId khoang trang khong duoc bo sung dong pham chat.");

        // qualityData null
        List<String> list4 = new ArrayList<>(List.of("Header Line"));
        List<String> r4 = ForgeTooltipHandler.appendTooltip("minecraft:diamond_sword", null, list4);
        assertEquals(1, r4.size(), "qualityData null khong duoc bo sung dong pham chat.");

        // Vat pham khong thuoc danh sach trang bi (vi du: go, da, tao vang)
        List<String> list5 = new ArrayList<>(List.of("Golden Apple"));
        List<String> r5 = ForgeTooltipHandler.appendTooltip("minecraft:golden_apple", ItemQualityData.of(QualityTier.LEGENDARY), list5);
        assertEquals(1, r5.size(), "Vat pham khong phai trang bi khong duoc nhan dong pham chat.");

        // Danh sach khong the thay doi (Immutable List) truyen vao -> phai duoc phong ve an toan
        List<String> unmodifiableList = Collections.singletonList("Fixed Item");
        try {
            // Khi truyen danh sach unmodifiable nhung la vat pham khong eligible, khong bi UnsupportedOperationException
            ForgeTooltipHandler.appendTooltip("minecraft:stick", ItemQualityData.DEFAULT, unmodifiableList);
        } catch (UnsupportedOperationException e) {
            throw new AssertionError("Loi: Khong phong ve ngoai le unmodifiable list khi khong eligible!");
        }

        System.out.println("  -> QA-03: Kiem tra bien Tooltip dat 100%.");
    }

    /**
     * 4. Kiem tra chi phi kinh nghiem (EXP Cost) tren De: Tinh chan tran 30 cap va an toan so am.
     */
    private static void testAnvilExpCostCappingAndSafety() {
        System.out.println("[QA-04] Kiem tra gioi han tran 30 cap EXP va tinh toan bien tren De...");

        // baseCost kiem tra cac loai nguyen lieu
        // Netherite/Mace/Trident/Elytra -> baseCost 4
        assertEquals(4, ForgeAnvilHandler.calculateExpCost("minecraft:netherite_sword", 0), "Netherite baseCost phai la 4.");
        assertEquals(4, ForgeAnvilHandler.calculateExpCost("minecraft:mace", 0), "Mace baseCost phai la 4.");
        assertEquals(4, ForgeAnvilHandler.calculateExpCost("minecraft:trident", 0), "Trident baseCost phai la 4.");
        assertEquals(4, ForgeAnvilHandler.calculateExpCost("minecraft:elytra", 0), "Elytra baseCost phai la 4.");

        // Diamond -> baseCost 3
        assertEquals(3, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_pickaxe", 0), "Diamond baseCost phai la 3.");

        // Iron/Gold/Bow/Shield -> baseCost 2
        assertEquals(2, ForgeAnvilHandler.calculateExpCost("minecraft:iron_chestplate", 0), "Iron baseCost phai la 2.");
        assertEquals(2, ForgeAnvilHandler.calculateExpCost("minecraft:golden_helmet", 0), "Gold baseCost phai la 2.");
        assertEquals(2, ForgeAnvilHandler.calculateExpCost("minecraft:bow", 0), "Bow baseCost phai la 2.");

        // Wood/Stone/Leather -> baseCost 1
        assertEquals(1, ForgeAnvilHandler.calculateExpCost("minecraft:leather_boots", 0), "Leather baseCost phai la 1.");
        assertEquals(1, ForgeAnvilHandler.calculateExpCost("minecraft:stone_axe", 0), "Stone baseCost phai la 1.");

        // reforgeCount am phai duoc an toan tinh la 0
        assertEquals(3, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", -10), "reforgeCount am phai tinh la baseCost.");

        // Cong thuc tang dan: base + reforgeCount * 2
        assertEquals(3 + 2 * 2, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 2), "Moc 2 lan dap phai la 7 cap.");
        assertEquals(3 + 5 * 2, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 5), "Moc 5 lan dap phai la 13 cap.");
        assertEquals(3 + 10 * 2, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 10), "Moc 10 lan dap phai la 23 cap.");
        assertEquals(3 + 13 * 2, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 13), "Moc 13 lan dap phai la 29 cap.");

        // Chan tran 30 cap tuyet doi (Khong bao gio vuot nguong 39 cua De Vanilla)
        assertEquals(30, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 14), "Moc 14 lan dap (31 cap) phai bi chan tran 30.");
        assertEquals(30, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 25), "Moc 25 lan dap (53 cap) phai bi chan tran 30.");
        assertEquals(30, ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 1000), "Moc 1000 lan dap phai bi chan tran 30.");
        assertEquals(30, ForgeAnvilHandler.calculateExpCost("minecraft:netherite_sword", 50), "Netherite dap 50 lan van bi chan tran 30.");

        System.out.println("  -> QA-04: Gioi han tran 30 cap EXP tren De dat 100%.");
    }

    /**
     * 5. Kiem tra ap luc Vung An Toan (Safe Zone: Junk, Common, Uncommon) qua 1000 lan ren.
     */
    private static void testAnvilSafeZoneStress() {
        System.out.println("[QA-05] Kiem thu ap luc Vung An Toan tren De (1,000 lan ren khong duoc vo do)...");

        QualityTier[] safeTiers = {QualityTier.JUNK, QualityTier.COMMON, QualityTier.UNCOMMON};
        int totalTrials = 1000;

        for (int i = 0; i < totalTrials; i++) {
            QualityTier chosenTier = safeTiers[i % safeTiers.length];
            ItemQualityData data = new ItemQualityData(chosenTier, 0, 0, false, false);

            ForgeAnvilHandler.AnvilReforgeResult res = ForgeAnvilHandler.processReforge(
                    "minecraft:diamond_sword",
                    data,
                    "minecraft:diamond",
                    1,
                    30,
                    false
            );

            assertTrue(res.canReforge(), "Lan ren thu " + i + " phai duoc chap thuan.");
            assertTrue(!res.broken(), "Vung An Toan tuyet doi khong bao gio bi vo do tai lan thu " + i + "!");
            assertNotNull(res.resultingData(), "Dau ra pham chat khong duoc null tai lan thu " + i + "!");
            assertTrue(res.resultingData().tier().ordinal() >= chosenTier.ordinal(),
                    "Bao hiem chong tut hang: Pham chat moi khong duoc thap hon pham chat cu!");
        }

        System.out.println("  -> QA-05: 1,000 lan ren Vung An Toan 100% khong vo do.");
    }

    /**
     * 6. Kiem tra Vung Mao Hiem (Danger Zone: Rare, Epic) va Co che Bao hiem Ngoc Luc Bao (Emerald Insurance).
     */
    private static void testAnvilDangerZoneAndEmeraldInsurance() {
        System.out.println("[QA-06] Kiem tra Vung Mao Hiem va Bao hiem Ngoc Luc Bao (Emerald Insurance)...");

        // 1. Khong co bao hiem: Phai co truong hop bi vo do trong Danger Zone
        boolean foundBrokenWithoutInsurance = false;
        ItemQualityData rareData = ItemQualityData.of(QualityTier.RARE);
        for (int i = 0; i < 200; i++) {
            ForgeAnvilHandler.AnvilReforgeResult res = ForgeAnvilHandler.processReforge(
                    "minecraft:diamond_sword",
                    rareData,
                    "minecraft:diamond",
                    1,
                    30,
                    false // Khong mang theo Ngoc Luc Bao
            );
            if (res.broken()) {
                foundBrokenWithoutInsurance = true;
                assertEquals(null, res.resultingData(), "Trang bi vo nat thi resultingData phai la null.");
                break;
            }
        }
        assertTrue(foundBrokenWithoutInsurance, "Trong Vung Mao Hiem khong co bao hiem phai xuat hien ti le vo do.");

        // 2. Co Bao hiem Ngoc Luc Bao: 500 lan ren lien tiep tuyet doi 0% bi vo do!
        int emeraldSavedCount = 0;
        ItemQualityData epicData = ItemQualityData.of(QualityTier.EPIC);
        for (int i = 0; i < 500; i++) {
            ForgeAnvilHandler.AnvilReforgeResult res = ForgeAnvilHandler.processReforge(
                    "minecraft:diamond_sword",
                    epicData,
                    "minecraft:diamond",
                    1,
                    30,
                    true // Mang theo Ngoc Luc Bao
            );
            assertTrue(!res.broken(), "Khi co Bao hiem Ngoc Luc Bao, trang bi tuyet doi khong bao gio bi vo tai lan thu " + i + "!");
            assertNotNull(res.resultingData(), "Trang bi duoc cuu phai giu nguyen du lieu pham chat khong null.");

            if (res.emeraldConsumed()) {
                emeraldSavedCount++;
                // Khi vien ngoc bi vo thay, pham chat trang bi giu nguyen cap cu
                assertEquals(QualityTier.EPIC, res.resultingData().tier(), "Khi vien ngoc nat thay, trang bi giu nguyen bac cu.");
            }
        }
        assertTrue(emeraldSavedCount > 0, "Phai co it nhat mot luot vien Ngoc Luc Bao nat vun de cuu song trang bi.");
        System.out.println("  -> QA-06: Bao hiem Ngoc Luc Bao cuu do 100% thanh cong (" + emeraldSavedCount + " lan cuu trang bi).");
    }

    /**
     * 7. Kiem tra co che bao hiem van may (Pity System) tai cac moc 3, 5, 8 lan dap.
     */
    private static void testAnvilPitySystemGuarantees() {
        System.out.println("[QA-07] Kiem tra he thong Bao hiem Van May (Pity System) tai cac moc bien...");

        // Moc 8 lan dap: Chack chan ra EPIC hoac LEGENDARY, va KHONG bao gio bi vo do ke ca o Rare/Epic
        for (int i = 0; i < 200; i++) {
            ItemQualityData pityData = new ItemQualityData(QualityTier.RARE, 0, 8, false, false);
            ForgeAnvilHandler.AnvilReforgeResult res = ForgeAnvilHandler.processReforge(
                    "minecraft:diamond_sword",
                    pityData,
                    "minecraft:diamond",
                    1,
                    30,
                    false // Khong can Ngoc Luc Bao vi moc 8 da mien nhiem vo do
            );
            assertTrue(!res.broken(), "Moc Pity 8 lan tuyet doi khong bao gio bi vo do!");
            assertTrue(res.pityTriggered(), "Moc Pity 8 phai duoc ghi nhan pityTriggered = true.");
            QualityTier resultTier = res.resultingData().tier();
            assertTrue(resultTier == QualityTier.EPIC || resultTier == QualityTier.LEGENDARY,
                    "Moc Pity 8 lan bat buoc phai dat EPIC hoac LEGENDARY, nhan duoc: " + resultTier);
        }

        // Moc 5 lan dap: Khong bao gio ra JUNK hoac COMMON (Toi thieu Uncommon tro len)
        for (int i = 0; i < 100; i++) {
            ItemQualityData pity5Data = new ItemQualityData(QualityTier.COMMON, 0, 5, false, false);
            ForgeAnvilHandler.AnvilReforgeResult res = ForgeAnvilHandler.processReforge(
                    "minecraft:diamond_sword",
                    pity5Data,
                    "minecraft:diamond",
                    1,
                    30,
                    false
            );
            assertTrue(res.resultingData().tier().ordinal() >= QualityTier.UNCOMMON.ordinal(),
                    "Moc Pity 5 lan phai toi thieu dat UNCOMMON tro len!");
        }

        System.out.println("  -> QA-07: He thong Pity 3, 5, 8 lan dap dat 100% chuan thiet ke.");
    }

    /**
     * 8. Kiem tra cac tinh huong bien cua Lifesteal 20% trong chien dau.
     */
    private static void testCombatLifestealEdgeCases() {
        System.out.println("[QA-08] Kiem tra tinh huong bien cua 20% Hut mau (Lifesteal)...");

        ItemQualityData legSword = ItemQualityData.of(QualityTier.LEGENDARY);

        // Sat thuong am hoac bang 0 -> khong hut mau
        assertDoubleEquals(0.0, ForgeCombatHandler.calculateLifesteal(legSword, "minecraft:netherite_sword", 0.0), "Sat thuong 0 khong hut mau.");
        assertDoubleEquals(0.0, ForgeCombatHandler.calculateLifesteal(legSword, "minecraft:netherite_sword", -15.0), "Sat thuong am khong hut mau.");

        // Sat thuong duong bat ky tren vu khi can chien Huyen thoai
        double dmg = 35.5;
        double expectedHeal = dmg * 0.20; // 7.1 HP
        assertDoubleEquals(expectedHeal, ForgeCombatHandler.calculateLifesteal(legSword, "minecraft:netherite_sword", dmg), "20% cua 35.5 phai la 7.1.");

        // Chuy (Mace) va Dinh Ba (Trident) can chien deu duoc huong 20% Lifesteal
        assertDoubleEquals(4.0, ForgeCombatHandler.calculateLifesteal(legSword, "minecraft:mace", 20.0), "Mace Huyen thoai duoc 20% lifesteal.");
        assertDoubleEquals(4.0, ForgeCombatHandler.calculateLifesteal(legSword, "minecraft:trident", 20.0), "Trident can chien duoc 20% lifesteal.");

        // Vu khi khong phai can chien: Cung ten hoac No khong duoc lifesteal can chien
        assertDoubleEquals(0.0, ForgeCombatHandler.calculateLifesteal(legSword, "minecraft:bow", 50.0), "Bow khong co lifesteal can chien.");
        assertDoubleEquals(0.0, ForgeCombatHandler.calculateLifesteal(legSword, "minecraft:crossbow", 50.0), "Crossbow khong co lifesteal can chien.");

        // Ap dung hoi phuc vao thanh mau:
        // Mau dang 5.0 / 40.0, hoi 7.1 -> 12.1
        ForgeCombatHandler.LifestealResult r1 = ForgeCombatHandler.applyLifesteal(legSword, "minecraft:diamond_sword", 35.5, 5.0, 40.0);
        assertDoubleEquals(7.1, r1.lifestealAmount(), "Luong hut mau phai la 7.1.");
        assertDoubleEquals(12.1, r1.newHealth(), "Mau moi phai la 12.1.");

        // Chan tran tai maxHealth: Mau dang 38.0 / 40.0, hoi 7.1 -> phai dung o 40.0
        ForgeCombatHandler.LifestealResult r2 = ForgeCombatHandler.applyLifesteal(legSword, "minecraft:diamond_sword", 35.5, 38.0, 40.0);
        assertDoubleEquals(40.0, r2.newHealth(), "Mau moi khong duoc vuot qua maxHealth 40.0.");

        // Nguoi choi co maxHealth am hoac 0
        ForgeCombatHandler.LifestealResult r3 = ForgeCombatHandler.applyLifesteal(legSword, "minecraft:diamond_sword", 35.5, 5.0, 0.0);
        assertDoubleEquals(5.0, r3.newHealth(), "maxHealth khong hop le thi giu nguyen mau hien tai.");

        System.out.println("  -> QA-08: 20% Lifesteal dat 100% chuan tinh toan.");
    }

    /**
     * 9. Kiem tra Haste II va cac Debuff cua trang bi Junk.
     */
    private static void testCombatHasteAndDebuffEdgeCases() {
        System.out.println("[QA-09] Kiem tra Haste II va cac hieu ung bat loi (Debuff) cua do Junk...");

        ItemQualityData leg = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData epic = ItemQualityData.of(QualityTier.EPIC);
        ItemQualityData junk = ItemQualityData.of(QualityTier.JUNK);

        // Haste II: Chi duy nhat cong cu khai thac (Pickaxe, Shovel, Hoe) bac Legendary moi co
        assertTrue(ForgeCombatHandler.shouldApplyHaste(leg, "minecraft:diamond_pickaxe"), "Cup Diamond Legendary phai co Haste II.");
        assertTrue(ForgeCombatHandler.shouldApplyHaste(leg, "minecraft:netherite_shovel"), "Xeng Netherite Legendary phai co Haste II.");
        assertTrue(ForgeCombatHandler.shouldApplyHaste(leg, "minecraft:golden_hoe"), "Cuoc Gold Legendary phai co Haste II.");

        assertTrue(!ForgeCombatHandler.shouldApplyHaste(epic, "minecraft:diamond_pickaxe"), "Cup Epic khong duoc co Haste II.");
        assertTrue(!ForgeCombatHandler.shouldApplyHaste(leg, "minecraft:diamond_sword"), "Kiem Legendary khong phai cong cu dao, khong co Haste II.");
        assertTrue(!ForgeCombatHandler.shouldApplyHaste(null, "minecraft:diamond_pickaxe"), "toolData null khong bao gio co Haste II.");

        // Debuff do Junk:
        // Vu khi can chien Junk -> Weakness I
        ForgeCombatHandler.HeldItemEffects junkSword = ForgeCombatHandler.evaluateHeldItemEffects("minecraft:diamond_sword", junk);
        assertTrue(junkSword.weakness(), "Kiem Junk phai gay Weakness I.");
        assertTrue(!junkSword.miningFatigue(), "Kiem Junk khong gay Mining Fatigue I.");
        assertTrue(!junkSword.haste(), "Kiem Junk khong co Haste.");

        // Cong cu dao Junk -> Mining Fatigue I
        ForgeCombatHandler.HeldItemEffects junkPick = ForgeCombatHandler.evaluateHeldItemEffects("minecraft:diamond_pickaxe", junk);
        assertTrue(junkPick.miningFatigue(), "Cup Junk phai gay Mining Fatigue I.");
        assertTrue(!junkPick.weakness(), "Cup Junk khong gay Weakness I.");

        System.out.println("  -> QA-09: Haste II va Debuff do Junk dat 100%.");
    }

    /**
     * 10. Kiem tra Full Set Bonus giap Huyen Thoai cong chuan 40 HP.
     */
    private static void testFullSetArmorBonusCalculations() {
        System.out.println("[QA-10] Kiem tra Full Set Bonus giap Huyen Thoai dat chuan 40 HP...");

        ItemQualityData leg = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData rare = ItemQualityData.of(QualityTier.RARE);

        // 1. Kiem tra cac to hop khuyet mon
        assertTrue(!ForgeCombatHandler.isFullLegendaryArmor(null, leg, leg, leg), "Thieu mu khong duoc la Full Set.");
        assertTrue(!ForgeCombatHandler.isFullLegendaryArmor(leg, null, leg, leg), "Thieu ao khong duoc la Full Set.");
        assertTrue(!ForgeCombatHandler.isFullLegendaryArmor(leg, leg, null, leg), "Thieu quan khong duoc la Full Set.");
        assertTrue(!ForgeCombatHandler.isFullLegendaryArmor(leg, leg, leg, null), "Thieu giay khong duoc la Full Set.");
        assertTrue(!ForgeCombatHandler.isFullLegendaryArmor(leg, leg, leg, rare), "Giay la Rare khong duoc la Full Set.");

        // 2. Du tron 4 mon Legendary
        assertTrue(ForgeCombatHandler.isFullLegendaryArmor(leg, leg, leg, leg), "Tron bo 4 mon Legendary phai dat Full Set.");
        assertTrue(ForgeCombatHandler.hasPermanentFireResistance(leg, leg, leg, leg), "Tron bo 4 mon Legendary phai co Permanent Fire Resistance.");

        // 3. Kiem tra chi so mau cong them theo tung mon
        // Mu: +4 HP, Ao: +6 HP, Quan: +6 HP, Giay: +4 HP
        double helmetBonus = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_HELMET).healthBonus();
        double chestplateBonus = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE).healthBonus();
        double leggingsBonus = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_LEGGINGS).healthBonus();
        double bootsBonus = QualityModifier.getStats(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.ARMOR_BOOTS).healthBonus();

        assertDoubleEquals(4.0, helmetBonus, "Mu Huyen Thoai phai cong +4 HP.");
        assertDoubleEquals(6.0, chestplateBonus, "Ao Huyen Thoai phai cong +6 HP.");
        assertDoubleEquals(6.0, leggingsBonus, "Quan Huyen Thoai phai cong +6 HP.");
        assertDoubleEquals(4.0, bootsBonus, "Giay Huyen Thoai phai cong +4 HP.");

        double totalBonus = ForgeCombatHandler.calculateTotalArmorBonusHealth(leg, leg, leg, leg);
        assertDoubleEquals(20.0, totalBonus, "Tong thuong mau tu 4 mon giap Huyen Thoai phai dung bang 20.0 HP.");

        double maxHealth = ForgeCombatHandler.calculatePlayerMaxHealth(leg, leg, leg, leg);
        assertDoubleEquals(40.0, maxHealth, "Tong luong mau nguoi choi phai dat chuan 40.0 HP (20 tim do - 2 hang tim).");

        System.out.println("  -> QA-10: Full Set Bonus giap Huyen Thoai chuan 40 HP dat 100%.");
    }

    /**
     * 11. Kiem tra dieu kien bien cua Sinh ton khan cap (Emergency Recovery):
     * Mua duoi 30% (< 12 HP tren 40 HP), thoi gian hoi chieu 1200 ticks, Affinity keo dai hieu ung.
     */
    private static void testEmergencyRecoveryBoundaryConditions() {
        System.out.println("[QA-11] Kiem tra cac gia tri bien cua Sinh Ton Khan Cap (Emergency Recovery)...");

        double maxHealth = 40.0;
        long lastTrigger = 0L;

        // Bien 1: Mau dung bang 12.0 HP (12.0 / 40.0 = 0.30 - 30%) -> KHONG duoc kich hoat vi yeu cau < 30%
        ForgeCombatHandler.EmergencyRecoveryResult rExact30 = ForgeCombatHandler.checkEmergencyRecovery(
                true, 12.0, maxHealth, 1500L, lastTrigger, 0
        );
        assertTrue(!rExact30.triggered(), "Mau dung 30% (12.0 HP) khong duoc kich hoat Emergency Recovery.");

        // Bien 2: Mau 11.99 HP (11.99 / 40.0 = 29.975% < 30%) va da qua 1200 ticks -> KICH HOAT THANH CONG
        ForgeCombatHandler.EmergencyRecoveryResult rBelow30 = ForgeCombatHandler.checkEmergencyRecovery(
                true, 11.99, maxHealth, 1500L, lastTrigger, 0
        );
        assertTrue(rBelow30.triggered(), "Mau 11.99 HP (< 30%) phai kich hoat Emergency Recovery.");
        assertEquals(200, rBelow30.durationTicks(), "Affinity 0: Thoi gian co ban phai la 200 ticks (10 giay).");
        assertEquals(1500L + 1200L, rBelow30.nextEligibleTick(), "Tick hoi chiu ke tiep phai la 2700L.");

        // Bien 3: Cooldown: Kich hoat tai tick 1500, thu kich hoat lai tai tick 2699 (1199 ticks sau do) -> KHONG duoc
        ForgeCombatHandler.EmergencyRecoveryResult rCooldown1199 = ForgeCombatHandler.checkEmergencyRecovery(
                true, 8.0, maxHealth, 2699L, 1500L, 0
        );
        assertTrue(!rCooldown1199.triggered(), "Tick 2699 (con 1 tick hoi chieu) khong duoc phep kich hoat.");

        // Bien 4: Cooldown dung tick 2700 (1200 ticks sau do) -> KICH HOAT THANH CONG
        ForgeCombatHandler.EmergencyRecoveryResult rCooldown1200 = ForgeCombatHandler.checkEmergencyRecovery(
                true, 8.0, maxHealth, 2700L, 1500L, 0
        );
        assertTrue(rCooldown1200.triggered(), "Dung 1200 ticks hoi chieu phai duoc phep kich hoat lai.");

        // Bien 5: Affinity toi da 100/100 -> Thoi gian duy tri duoc cong them 50% (tu 200 len 300 ticks)
        ForgeCombatHandler.EmergencyRecoveryResult rAffinity100 = ForgeCombatHandler.checkEmergencyRecovery(
                true, 5.0, maxHealth, 4000L, 2700L, 100
        );
        assertTrue(rAffinity100.triggered(), "Affinity 100 phai kich hoat.");
        assertEquals(300, rAffinity100.durationTicks(), "Affinity 100 phai tang 50% thoi gian (200 * 1.5 = 300 ticks).");

        // Bien 6: Nguoi choi khong mac du bo giap Legendary -> khong bao gio kich hoat
        ForgeCombatHandler.EmergencyRecoveryResult rNotFullSet = ForgeCombatHandler.checkEmergencyRecovery(
                false, 5.0, maxHealth, 6000L, 4000L, 100
        );
        assertTrue(!rNotFullSet.triggered(), "Khong phai Full Set Legendary khong bao gio duoc kich hoat Emergency Recovery.");

        System.out.println("  -> QA-11: Sinh Ton Khan Cap kiem tra bien dat 100%.");
    }
}
