package com.minequality.fabric;

import com.minequality.fabric.event.FabricAnvilHandler;
import com.minequality.fabric.event.FabricCombatHandler;
import com.minequality.fabric.event.FabricTooltipHandler;
import com.minequality.quality.ItemAffinity;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityModifier;
import com.minequality.quality.QualityTier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bo kiem thu toan dien danh rieng cho mo-dun Fabric Loader tren Minecraft 26.3 va Java 26.
 * Kiem tra sau cac truong hop bien, co che can bang, an toan ngoai le null va kiem thu tai ap luc cao.
 */
public final class FabricQACheckTest {

    private static int totalAssertions = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("BAT DAU KIEM TRA TOAN DIEN MO-DUN FABRIC LOADER (MINECRAFT 26.3, JAVA 26)");
        System.out.println("======================================================================");

        long startTime = System.currentTimeMillis();

        testFabricMetadata();
        testFabricHandlerRegistration();
        testFabricTooltipHandler();
        testFabricAnvilHandlerExpCap();
        testFabricAnvilHandlerNullSafetyAndRejections();
        testFabricAnvilSafeZoneNeverBreaks();
        testFabricAnvilDangerZoneAndEmeraldInsurance();
        testFabricAnvilPitySystem();
        testFabricCombatLifesteal();
        testFabricCombatMiningHaste();
        testFabricCombatFullArmorSetAndEmergencyRecovery();
        testFabricCombatSpecialWeapons();
        testFabricLoadAndStressTesting();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("======================================================================");
        System.out.println("KET QUA: TAT CA KIEM THU FABRIC DA HOAN TAT 100% THANH CONG!");
        System.out.println("Tong so phep kiem tra khang dinh (assertions): " + totalAssertions);
        System.out.println("Tong thoi gian thuc thi: " + elapsedTime + " ms");
        System.out.println("======================================================================");
    }

    private static void assertTrue(boolean condition, String message) {
        totalAssertions++;
        if (!condition) {
            throw new AssertionError("Kiem thu that bai: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        totalAssertions++;
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("Kiem thu that bai: " + message + " | Mong doi: " + expected + ", Thuc te: " + actual);
        }
    }

    private static void assertEquals(double expected, double actual, double delta, String message) {
        totalAssertions++;
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError("Kiem thu that bai: " + message + " | Mong doi: " + expected + ", Thuc te: " + actual);
        }
    }

    /**
     * 1. Kiem tra metadata fabric.mod.json
     */
    private static void testFabricMetadata() {
        System.out.println("[MUC 1] Kiem tra metadata fabric.mod.json...");
        Path jsonPath = Path.of("fabric", "src", "main", "resources", "fabric.mod.json");
        assertTrue(Files.exists(jsonPath), "Tep fabric.mod.json phai ton tai tren dia.");

        try {
            String content = Files.readString(jsonPath);
            assertTrue(content.contains("\"schemaVersion\": 1"), "schemaVersion phai la 1.");
            assertTrue(content.contains("\"id\": \"mod_mine\""), "id mod phai la mod_mine.");
            assertTrue(content.contains("\"name\": \"MineQuality\""), "name mod phai la MineQuality.");
            assertTrue(content.contains("com.minequality.fabric.MineQualityFabric"), "entrypoint main phai tro vao MineQualityFabric.");
            assertTrue(content.contains("\"minecraft\": \">=26.3\""), "depends phai yeu cau minecraft >=26.3.");
            assertTrue(content.contains("\"java\": \">=26\""), "depends phai yeu cau java >=26.");
            assertTrue(content.contains("\"fabricloader\": \">=0.19.0\""), "depends phai yeu cau fabricloader >=0.19.0.");
        } catch (Exception e) {
            throw new RuntimeException("Khong the doc tep fabric.mod.json", e);
        }
        System.out.println("  -> Metadata fabric.mod.json hoan toan hop le.");
    }

    /**
     * 2. Kiem tra dang ky cac Handler
     */
    private static void testFabricHandlerRegistration() {
        System.out.println("[MUC 2] Kiem tra dang ky va reset registration cac Handler...");

        FabricTooltipHandler.resetRegistration();
        FabricAnvilHandler.resetRegistration();
        FabricCombatHandler.resetRegistration();

        assertTrue(!FabricTooltipHandler.isRegistered(), "TooltipHandler phai chua dang ky.");
        assertTrue(!FabricAnvilHandler.isRegistered(), "AnvilHandler phai chua dang ky.");
        assertTrue(!FabricCombatHandler.isRegistered(), "CombatHandler phai chua dang ky.");

        MineQualityFabric fabricMod = new MineQualityFabric();
        fabricMod.onInitialize();

        assertTrue(FabricTooltipHandler.isRegistered(), "TooltipHandler phai da dang ky sau onInitialize.");
        assertTrue(FabricAnvilHandler.isRegistered(), "AnvilHandler phai da dang ky sau onInitialize.");
        assertTrue(FabricCombatHandler.isRegistered(), "CombatHandler phai da dang ky sau onInitialize.");
        System.out.println("  -> Dang ky cac Handler hoat dong chinh xac.");
    }

    /**
     * 3. Kiem tra FabricTooltipHandler
     */
    private static void testFabricTooltipHandler() {
        System.out.println("[MUC 3] Kiem tra FabricTooltipHandler (an toan null va format)...");

        // An toan null
        List<String> listNull = FabricTooltipHandler.appendTooltip(null, null, null);
        assertTrue(listNull != null && listNull.isEmpty(), "Null toan phan phai tra ve list rong khong loi.");

        List<String> listNullItem = FabricTooltipHandler.appendTooltip(null, ItemQualityData.DEFAULT, new ArrayList<>());
        assertTrue(listNullItem.isEmpty(), "Null itemId phai tra ve list ban dau.");

        List<String> listNullData = FabricTooltipHandler.appendTooltip("minecraft:diamond_sword", null, new ArrayList<>());
        assertTrue(listNullData.isEmpty(), "Null qualityData phai tra ve list ban dau.");

        List<String> listIneligible = FabricTooltipHandler.appendTooltip("minecraft:apple", ItemQualityData.DEFAULT, new ArrayList<>());
        assertTrue(listIneligible.isEmpty(), "Vat pham khong du dieu kien (apple) khong duoc bo sung tooltip.");

        // Kiem tra bo sung tooltip hop le cho Diamond Chestplate Legendary
        ItemQualityData legData = ItemQualityData.of(QualityTier.LEGENDARY, 100);
        List<String> tooltip = new ArrayList<>();
        tooltip.add("Diamond Chestplate");
        List<String> result = FabricTooltipHandler.appendTooltip("minecraft:diamond_chestplate", legData, tooltip);

        assertTrue(result.size() > 1, "Tooltip phai co nhieu hon 1 dong.");
        boolean hasLegendary = result.stream().anyMatch(l -> l.contains("Quality: Legendary"));
        boolean hasAffinity = result.stream().anyMatch(l -> l.contains("Kindred Spirits"));
        boolean hasHealth = result.stream().anyMatch(l -> l.contains("+6 Max Health"));
        boolean hasUnbreakable = result.stream().anyMatch(l -> l.contains("Unbreakable & Fireproof"));
        boolean hasSetBonus = result.stream().anyMatch(l -> l.contains("Set Bonus:"));

        assertTrue(hasLegendary, "Tooltip phai co dong Quality: Legendary.");
        assertTrue(hasAffinity, "Tooltip phai co danh hieu Kindred Spirits.");
        assertTrue(hasHealth, "Tooltip phai co dong +6 Max Health.");
        assertTrue(hasUnbreakable, "Tooltip phai co dong Unbreakable & Fireproof.");
        assertTrue(hasSetBonus, "Tooltip phai co dong Set Bonus.");

        System.out.println("  -> FabricTooltipHandler hoat dong chinh xac va an toan null.");
    }

    /**
     * 4. Kiem tra gioi han tran EXP tai Anvil khong vuot 30 cap
     */
    private static void testFabricAnvilHandlerExpCap() {
        System.out.println("[MUC 4] Kiem tra muc tran EXP rèn tai Anvil (khong vuot 30 cap)...");

        String[] testItems = {
                "minecraft:netherite_sword", // base 4
                "minecraft:diamond_pickaxe", // base 3
                "minecraft:iron_chestplate", // base 2
                "minecraft:leather_helmet",  // base 1
                "minecraft:elytra",          // base 4
                "minecraft:mace",            // base 4
                "minecraft:trident"          // base 4
        };

        for (String item : testItems) {
            for (int reforgeCount = 0; reforgeCount <= 100; reforgeCount++) {
                int cost = FabricAnvilHandler.getExpCost(item, reforgeCount);
                assertTrue(cost >= 1, "Chi phi EXP phai toi thieu 1 cap.");
                assertTrue(cost <= 30, "Chi phi EXP khong duoc vuot qua tran 30 cap (Vat pham: " + item + ", count: " + reforgeCount + ", cost: " + cost + ")");
            }
        }

        // Kiem tra cong thuc cu the
        assertEquals(4, FabricAnvilHandler.getExpCost("minecraft:netherite_sword", 0), "Netherite lan 0 phai la 4.");
        assertEquals(6, FabricAnvilHandler.getExpCost("minecraft:netherite_sword", 1), "Netherite lan 1 phai la 6.");
        assertEquals(24, FabricAnvilHandler.getExpCost("minecraft:netherite_sword", 10), "Netherite lan 10 phai la 24.");
        assertEquals(30, FabricAnvilHandler.getExpCost("minecraft:netherite_sword", 13), "Netherite lan 13 phai cham tran 30.");
        assertEquals(30, FabricAnvilHandler.getExpCost("minecraft:netherite_sword", 50), "Netherite lan 50 phai bi chan o tran 30.");

        System.out.println("  -> Kiem tra tran 30 cap EXP dat 100% tren tat ca cac bac vat lieu.");
    }

    /**
     * 5. Kiem tra an toan null va tu choi giao dich khong hop le tai Anvil
     */
    private static void testFabricAnvilHandlerNullSafetyAndRejections() {
        System.out.println("[MUC 5] Kiem tra an toan null va tu choi giao dich sai...");

        // Tham so null
        assertTrue(!FabricAnvilHandler.canPerformReforge(null, null, null, 0, 0), "Null canPerformReforge phai tra ve false.");
        FabricAnvilHandler.AnvilReforgeOutput outNull = FabricAnvilHandler.processReforge(null, null, null, 0, 0, false);
        assertTrue(!outNull.canReforge(), "processReforge voi tham so null phai bi tu choi.");

        // Vat pham khong du dieu kien
        assertTrue(!FabricAnvilHandler.canPerformReforge("minecraft:stick", ItemQualityData.DEFAULT, "minecraft:iron_ingot", 1, 30), "Vat pham khong du dieu kien phai bi tu choi.");

        // Trang bi da dat Legendary (khong the ren tiep)
        ItemQualityData legData = ItemQualityData.of(QualityTier.LEGENDARY);
        assertTrue(!FabricAnvilHandler.canPerformReforge("minecraft:diamond_sword", legData, "minecraft:diamond", 1, 30), "Legendary khong the ren them.");
        FabricAnvilHandler.AnvilReforgeOutput outLeg = FabricAnvilHandler.processReforge("minecraft:diamond_sword", legData, "minecraft:diamond", 1, 30, false);
        assertTrue(!outLeg.canReforge(), "processReforge cho Legendary phai bi tu choi.");

        // Nguyen lieu sai lech (Diamond Sword nhung dung Iron Ingot)
        ItemQualityData commonData = ItemQualityData.of(QualityTier.COMMON);
        assertTrue(!FabricAnvilHandler.canPerformReforge("minecraft:diamond_sword", commonData, "minecraft:iron_ingot", 1, 30), "Sai nguyen lieu phai bi tu choi.");

        // Khong du so luong nguyen lieu
        assertTrue(!FabricAnvilHandler.canPerformReforge("minecraft:diamond_sword", commonData, "minecraft:diamond", 0, 30), "Nguyen lieu = 0 phai bi tu choi.");

        // Khong du EXP
        int reqExp = FabricAnvilHandler.getExpCost("minecraft:diamond_sword", 0); // 3
        assertTrue(!FabricAnvilHandler.canPerformReforge("minecraft:diamond_sword", commonData, "minecraft:diamond", 1, reqExp - 1), "Thieu EXP phai bi tu choi.");
        assertTrue(FabricAnvilHandler.canPerformReforge("minecraft:diamond_sword", commonData, "minecraft:diamond", 1, reqExp), "Du EXP phai chap nhan.");

        System.out.println("  -> Cac truong hop bien va tu choi tai Anvil hoat dong an toan tuyet doi.");
    }

    /**
     * 6. Kiem tra Vung An Toan (Safe Zone: Junk, Common, Uncommon) khong bao gio vo do
     */
    private static void testFabricAnvilSafeZoneNeverBreaks() {
        System.out.println("[MUC 6] Kiem tra Vung An Toan (Junk, Common, Uncommon) 100% khong vo do...");

        QualityTier[] safeTiers = {QualityTier.JUNK, QualityTier.COMMON, QualityTier.UNCOMMON};

        for (QualityTier tier : safeTiers) {
            ItemQualityData data = ItemQualityData.of(tier);
            for (int i = 0; i < 300; i++) {
                FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                        "minecraft:iron_sword",
                        data,
                        "minecraft:iron_ingot",
                        10,
                        50,
                        false // Khong dung ngoc luc bao
                );
                assertTrue(out.canReforge(), "Giao dich phai thanh cong.");
                assertTrue(!out.broken(), "Vung an toan tuyet doi khong bao gio vo do!");
                assertTrue(out.resultingData() != null, "Du lieu trang bi phai luon ton tai.");
            }
        }
        System.out.println("  -> 900 luot ren trong Vung An Toan deu an toan 100%, khong mot vat pham nao bi vo.");
    }

    /**
     * 7. Kiem tra Vung Mao Hiem (Danger Zone: Rare, Epic) va Ve Bao Hiem Ngoc Luc Bao
     */
    private static void testFabricAnvilDangerZoneAndEmeraldInsurance() {
        System.out.println("[MUC 7] Kiem tra Vung Mao Hiem va Ve Bao Hiem Ngoc Luc Bao...");

        // 7.1. Khi KHONG co Ngoc Luc Bao: phai co ti le bi vo do
        ItemQualityData rareData = ItemQualityData.of(QualityTier.RARE);
        boolean witnessedBreak = false;
        for (int i = 0; i < 200; i++) {
            FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                    "minecraft:diamond_sword",
                    rareData,
                    "minecraft:diamond",
                    10,
                    50,
                    false // Khong co bao hiem
            );
            if (out.broken()) {
                witnessedBreak = true;
                assertTrue(out.resultingData() == null, "Khi vo do thi du lieu pham chat phai la null.");
                break;
            }
        }
        assertTrue(witnessedBreak, "Trong Vung Mao Hiem khong co bao hiem phai xuat hien hien tuong vo do.");

        // 7.2. Khi CO Ngoc Luc Bao: 100% KHONG BAO GIO BI VO DO!
        ItemQualityData epicData = ItemQualityData.of(QualityTier.EPIC);
        boolean emeraldTriggered = false;
        for (int i = 0; i < 500; i++) {
            FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                    "minecraft:diamond_sword",
                    epicData,
                    "minecraft:diamond",
                    10,
                    50,
                    true // Co bao hiem Ngoc Luc Bao
            );
            assertTrue(!out.broken(), "Co Ngoc Luc Bao bao hiem thi trang bi TUYET DOI KHONG BI VO!");
            assertTrue(out.resultingData() != null, "Trang bi phai duoc bao toan.");
            if (out.emeraldConsumed()) {
                emeraldTriggered = true;
            }
        }
        assertTrue(emeraldTriggered, "Phai co it nhat mot lan vien Ngoc Luc Bao bi tieu hao de cuu song trang bi.");
        System.out.println("  -> Ve Bao Hiem Ngoc Luc Bao cuu nguy thanh cong 100%, trang bi khong he bi pha huy.");
    }

    /**
     * 8. Kiem tra he thong van may (Pity System) tai Anvil
     */
    private static void testFabricAnvilPitySystem() {
        System.out.println("[MUC 8] Kiem tra he thong van may (Pity System) tai Anvil...");

        // Moc 8: 100% ra Epic hoac Legendary, khong bao gio vo do
        ItemQualityData rarePity8 = new ItemQualityData(QualityTier.RARE, 0, 8, false, false);
        for (int i = 0; i < 100; i++) {
            FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                    "minecraft:diamond_sword",
                    rarePity8,
                    "minecraft:diamond",
                    10,
                    50,
                    false // Khong dung bao hiem
            );
            assertTrue(out.pityTriggered(), "Moc 8 phai kich hoat Pity.");
            assertTrue(!out.broken(), "Moc 8 cuong che thanh cong khong the bi vo do.");
            QualityTier resTier = out.resultingData().tier();
            assertTrue(resTier == QualityTier.EPIC || resTier == QualityTier.LEGENDARY, "Moc 8 phai ra Epic hoac Legendary.");
        }

        // Moc 3: Loai bo hoan toan Junk
        ItemQualityData commonPity3 = new ItemQualityData(QualityTier.COMMON, 0, 3, false, false);
        for (int i = 0; i < 100; i++) {
            FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                    "minecraft:iron_sword",
                    commonPity3,
                    "minecraft:iron_ingot",
                    10,
                    50,
                    false
            );
            assertTrue(out.pityTriggered(), "Moc 3 phai kich hoat Pity.");
            assertTrue(out.resultingData().tier() != QualityTier.JUNK, "Moc 3 khong bao gio roi vao Junk.");
        }

        System.out.println("  -> He thong Pity tai Anvil hoat dong dung 100% theo tai lieu thiet ke.");
    }

    /**
     * 9. Kiem tra hieu ung 20% Lifesteal khi can chien Legendary
     */
    private static void testFabricCombatLifesteal() {
        System.out.println("[MUC 9] Kiem tra 20% Lifesteal can chien Legendary va Suy yeu can chien Junk...");

        ItemQualityData legData = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData epicData = ItemQualityData.of(QualityTier.EPIC);
        ItemQualityData junkData = ItemQualityData.of(QualityTier.JUNK);

        // Kiem tra kiem Legendary gay 10.0 sat thuong
        FabricCombatHandler.AttackOutcome outLegSword = FabricCombatHandler.onPlayerAttack("minecraft:diamond_sword", legData, 10.0);
        assertEquals(2.0, outLegSword.lifestealAmount(), 0.001, "Kiem Legendary phai hoi lai dung 20% cua 10.0 (2.0 HP).");
        assertTrue(outLegSword.lifestealTriggered(), "lifestealTriggered phai la true.");
        assertTrue(!outLegSword.applyWeakness(), "Legendary khong bi Weakness.");

        // Kiem tra riu Legendary gay 15.0 sat thuong
        FabricCombatHandler.AttackOutcome outLegAxe = FabricCombatHandler.onPlayerAttack("minecraft:netherite_axe", legData, 15.0);
        assertEquals(3.0, outLegAxe.lifestealAmount(), 0.001, "Riu Legendary phai hoi lai dung 20% cua 15.0 (3.0 HP).");

        // Vu khi khong phai Legendary khong co Lifesteal
        FabricCombatHandler.AttackOutcome outEpicSword = FabricCombatHandler.onPlayerAttack("minecraft:diamond_sword", epicData, 10.0);
        assertEquals(0.0, outEpicSword.lifestealAmount(), 0.001, "Epic sword khong co lifesteal.");
        assertTrue(!outEpicSword.lifestealTriggered(), "lifestealTriggered phai la false cho Epic.");

        // Vu khi Junk bi Weakness
        FabricCombatHandler.AttackOutcome outJunkSword = FabricCombatHandler.onPlayerAttack("minecraft:diamond_sword", junkData, 5.0);
        assertEquals(0.0, outJunkSword.lifestealAmount(), 0.001, "Junk sword khong co lifesteal.");
        assertTrue(outJunkSword.applyWeakness(), "Junk sword phai bi Weakness.");

        // Khong phai can chien (vi du Cung ten) khong co lifesteal
        FabricCombatHandler.AttackOutcome outBow = FabricCombatHandler.onPlayerAttack("minecraft:bow", legData, 20.0);
        assertEquals(0.0, outBow.lifestealAmount(), 0.001, "Cung ten Legendary khong duoc huong lifesteal can chien.");

        // An toan null va sat thuong am
        FabricCombatHandler.AttackOutcome outNeg = FabricCombatHandler.onPlayerAttack("minecraft:diamond_sword", legData, -5.0);
        assertEquals(0.0, outNeg.lifestealAmount(), 0.001, "Sat thuong am khong hut mau.");

        System.out.println("  -> 20% Lifesteal can chien Legendary hoat dong hoan hao.");
    }

    /**
     * 10. Kiem tra Haste II khi cam cong cu khai khoang Legendary
     */
    private static void testFabricCombatMiningHaste() {
        System.out.println("[MUC 10] Kiem tra Dao nhanh II (Haste II) cho cong cu khai khoang...");

        ItemQualityData legData = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData junkData = ItemQualityData.of(QualityTier.JUNK);
        ItemQualityData epicData = ItemQualityData.of(QualityTier.EPIC);

        String[] miningTools = {"minecraft:diamond_pickaxe", "minecraft:netherite_shovel", "minecraft:iron_hoe"};
        for (String tool : miningTools) {
            FabricCombatHandler.MiningToolEffect eff = FabricCombatHandler.onHoldMiningTool(tool, legData);
            assertTrue(eff.applyHaste(), "Cong cu khai khoang Legendary phai nhan Haste II: " + tool);
            assertTrue(!eff.applyFatigue(), "Legendary khong bi Mining Fatigue: " + tool);
        }

        // Junk mining tool
        FabricCombatHandler.MiningToolEffect effJunk = FabricCombatHandler.onHoldMiningTool("minecraft:diamond_pickaxe", junkData);
        assertTrue(!effJunk.applyHaste(), "Junk khong duoc Haste.");
        assertTrue(effJunk.applyFatigue(), "Junk pickaxe phai bi Mining Fatigue.");

        // Epic mining tool (khong Haste, khong Fatigue)
        FabricCombatHandler.MiningToolEffect effEpic = FabricCombatHandler.onHoldMiningTool("minecraft:diamond_pickaxe", epicData);
        assertTrue(!effEpic.applyHaste(), "Epic pickaxe khong duoc Haste II.");
        assertTrue(!effEpic.applyFatigue(), "Epic pickaxe khong bi Fatigue.");

        // Vu khi / giap cam tren tay khong kich hoat Haste
        FabricCombatHandler.MiningToolEffect effSword = FabricCombatHandler.onHoldMiningTool("minecraft:diamond_sword", legData);
        assertTrue(!effSword.applyHaste(), "Kiem khong phai la mining tool, khong duoc Haste.");

        // An toan null
        FabricCombatHandler.MiningToolEffect effNull = FabricCombatHandler.onHoldMiningTool(null, null);
        assertTrue(!effNull.applyHaste() && !effNull.applyFatigue(), "Null phai tra ve NONE.");

        System.out.println("  -> Hieu ung Dao nhanh II va Dao cham I phan loai chinh xac.");
    }

    /**
     * 11. Kiem tra Full Set Bonus giap 40 HP va Cuu Sinh Khan Cap khi mau < 30%
     */
    private static void testFabricCombatFullArmorSetAndEmergencyRecovery() {
        System.out.println("[MUC 11] Kiem tra Full Set Bonus giap 40 HP va Cuu Sinh Khan Cap...");

        ItemQualityData legHelmet = ItemQualityData.of(QualityTier.LEGENDARY, 100);
        ItemQualityData legChest = ItemQualityData.of(QualityTier.LEGENDARY, 100);
        ItemQualityData legLegs = ItemQualityData.of(QualityTier.LEGENDARY, 100);
        ItemQualityData legBoots = ItemQualityData.of(QualityTier.LEGENDARY, 100);

        List<ItemQualityData> fullLegArmor = Arrays.asList(legHelmet, legChest, legLegs, legBoots);

        // 11.1. Tinh tong mau thuong tu bo giap Legendary: Mu (+4) + Ao (+6) + Quan (+6) + Giay (+4) = +20 HP
        double armorHealthBonus = FabricCombatHandler.calculateTotalArmorHealthBonus(fullLegArmor);
        assertEquals(20.0, armorHealthBonus, 0.001, "Tong 4 mon giap Legendary phai cong dung +20 HP!");

        double playerBaseHealth = 20.0;
        double totalMaxHealth = playerBaseHealth + armorHealthBonus;
        assertEquals(40.0, totalMaxHealth, 0.001, "Tong mau toi da phai dat tron chuan 40.0 HP (20 tim do)!");

        // 11.2. Mien nhiem lua vinh vien
        assertTrue(FabricCombatHandler.isFullLegendaryArmor(fullLegArmor), "Phai xac nhan full bo Legendary.");

        // 11.3. Cuu Sinh Khan Cap: Nguong mau duoi 30% (< 12.0 HP tren 40.0 HP)
        // Truong hop 1: Mau con 15.0 HP (37.5% > 30%) -> KHONG KICH HOAT
        FabricCombatHandler.ArmorSetOutcome outNormal = FabricCombatHandler.checkArmorSetEffects(
                fullLegArmor,
                15.0,
                40.0,
                1000L,
                0L
        );
        assertTrue(outNormal.isFullLegendarySet(), "Phai la bo Legendary.");
        assertTrue(outNormal.hasPermanentFireResistance(), "Phai co Fire Resistance.");
        assertTrue(!outNormal.emergencyRecoveryTriggered(), "Mau 15.0/40.0 (>30%) khong duoc kich hoat Emergency Recovery.");

        // Truong hop 2: Mau tụt xuong 11.0 HP (27.5% < 30%) va cooldown da het -> KICH HOAT!
        FabricCombatHandler.ArmorSetOutcome outLow = FabricCombatHandler.checkArmorSetEffects(
                fullLegArmor,
                11.0,
                40.0,
                2000L,
                500L // Cooldown da troi qua 1500 ticks > 1200 ticks
        );
        assertTrue(outLow.emergencyRecoveryTriggered(), "Mau 11.0/40.0 (<30%) phai kich hoat Emergency Recovery.");
        // Affinity 100/100 tang thoi gian them 50% (200 * 1.5 = 300 ticks = 15 giay)
        assertEquals(300, outLow.durationTicks(), "Affinity 100/100 phai keo dai thoi gian len 300 ticks (15s).");

        // Truong hop 3: Dang trong thoi gian hoi chieu (Cooldown 60s = 1200 ticks) -> KHONG KICH HOAT
        FabricCombatHandler.ArmorSetOutcome outCooldown = FabricCombatHandler.checkArmorSetEffects(
                fullLegArmor,
                10.0,
                40.0,
                1500L,
                1000L // Cooldown moi troi qua 500 ticks < 1200 ticks
        );
        assertTrue(!outCooldown.emergencyRecoveryTriggered(), "Dang trong cooldown 60s thi khong duoc kich hoat lai.");

        // 11.4. Bo giap khong day du (3 Legendary + 1 Epic) -> Khong duoc Set Bonus
        List<ItemQualityData> incompleteArmor = Arrays.asList(
                legHelmet,
                legChest,
                legLegs,
                ItemQualityData.of(QualityTier.EPIC) // 1 mon Epic
        );
        FabricCombatHandler.ArmorSetOutcome outIncomplete = FabricCombatHandler.checkArmorSetEffects(
                incompleteArmor,
                5.0,
                37.0,
                2000L,
                0L
        );
        assertTrue(!outIncomplete.isFullLegendarySet(), "Bo giap thieu khong the la full Legendary.");
        assertTrue(!outIncomplete.emergencyRecoveryTriggered(), "Khong du full bo Legendary thi khong co Emergency Recovery.");
        assertTrue(!outIncomplete.hasPermanentFireResistance(), "Khong du full bo thi khong co Fire Resistance.");

        System.out.println("  -> Mua chuan 40 HP va Cuu Sinh Khan Cap < 30% HP dat 100% dung tieu chuan.");
    }

    /**
     * 12. Kiem tra vu khi dac thu (Chuy Mace, Dinh Ba Trident, Khien Shield)
     */
    private static void testFabricCombatSpecialWeapons() {
        System.out.println("[MUC 12] Kiem tra cac vu khi dac thu (Chuy Mace, Dinh Ba Trident, Khien)...");

        ItemQualityData legData = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData rareData = ItemQualityData.of(QualityTier.RARE);

        // Chuy Mace
        FabricCombatHandler.MaceSmashOutcome maceLeg = FabricCombatHandler.onMaceSmash(legData);
        assertEquals(1.50, maceLeg.damageMultiplier(), 0.001, "Mace Legendary phai tang 50% smash damage.");
        assertEquals(1.30, maceLeg.shockwaveRadiusMultiplier(), 0.001, "Mace Legendary phai tang 30% shockwave.");
        assertTrue(maceLeg.immuneToFallDamage(), "Mace Legendary phai mien nhiem 100% fall damage.");

        // Dinh Ba Trident
        FabricCombatHandler.TridentOutcome tridentLeg = FabricCombatHandler.onTridentAction(legData, true);
        assertEquals(1.50, tridentLeg.throwVelocityMultiplier(), 0.001, "Trident tang 50% throw velocity.");
        assertEquals(2.0, tridentLeg.loyaltyReturnSpeedMultiplier(), 0.001, "Trident tang gap doi return speed.");
        assertTrue(tridentLeg.hasBuiltInLoyalty(), "Trident Legendary phai tich hop san Loyalty.");
        assertEquals(1.25, tridentLeg.riptideDistanceMultiplier(), 0.001, "Trident duoi nuoc tang 25% riptide.");
        assertEquals(1.15, tridentLeg.waterMeleeDamageMultiplier(), 0.001, "Trident duoi nuoc tang 15% melee damage.");

        // Khien Shield
        assertTrue(FabricCombatHandler.onShieldBlock(legData), "Khien Legendary phai kich hoat Resistance II khi do don.");
        assertTrue(!FabricCombatHandler.onShieldBlock(rareData), "Khien Rare khong kich hoat Resistance II.");

        System.out.println("  -> Vu khi dac thu Mace, Trident, Shield hoat dong chinh xac.");
    }

    /**
     * 13. Kiem thu tai va ap luc cao (Load & Stress Testing)
     */
    private static void testFabricLoadAndStressTesting() {
        System.out.println("[MUC 13] Kiem thu tai ap luc cao (10,000 luot goi moi ham)...");

        long start = System.currentTimeMillis();

        // 13.1. 10,000 luot xu ly don danh combat
        ItemQualityData legData = ItemQualityData.of(QualityTier.LEGENDARY);
        for (int i = 0; i < 10000; i++) {
            FabricCombatHandler.AttackOutcome out = FabricCombatHandler.onPlayerAttack("minecraft:netherite_sword", legData, 12.5);
            if (out.lifestealAmount() != 2.5) {
                throw new AssertionError("Loi tinh toan lifesteal trong vong lap stress test!");
            }
        }

        // 13.2. 10,000 luot kiem tra Tooltip
        List<String> baseTooltip = new ArrayList<>(List.of("Netherite Chestplate"));
        for (int i = 0; i < 10000; i++) {
            List<String> list = FabricTooltipHandler.appendTooltip("minecraft:netherite_chestplate", legData, new ArrayList<>(baseTooltip));
            if (list.size() <= 1) {
                throw new AssertionError("Loi tao tooltip trong vong lap stress test!");
            }
        }

        // 13.3. 10,000 luot giao dich tai Anvil
        ItemQualityData commonData = ItemQualityData.of(QualityTier.COMMON);
        for (int i = 0; i < 10000; i++) {
            FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                    "minecraft:iron_sword",
                    commonData,
                    "minecraft:iron_ingot",
                    5,
                    30,
                    false
            );
            if (!out.canReforge() || out.broken()) {
                throw new AssertionError("Loi giao dich Anvil trong vong lap stress test!");
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  -> 30,000 luot goi stress test hoan tat thanh cong trong " + elapsed + " ms (khong ro ri null, khong ngoai le).");
    }
}
