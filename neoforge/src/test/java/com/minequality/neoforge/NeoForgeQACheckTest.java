package com.minequality.neoforge;

import com.minequality.neoforge.event.NeoForgeAnvilHandler;
import com.minequality.neoforge.event.NeoForgeCombatHandler;
import com.minequality.neoforge.event.NeoForgeTooltipHandler;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityTier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bo kiem thu chat luong va tinh huong bien toan dien cho mo-dun NeoForge Loader (NeoForge QA Check Suite).
 * Thuc hien boi NeoForge QA Inspector nham doi chieu nghiem ngat voi SO_TAY_KIEM_TRA_BUG.md va THIET_KE_HE_THONG_PHAM_CHAT.md.
 */
public class NeoForgeQACheckTest {

    private static int testsRun = 0;
    private static int assertionsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=== KHOI CHAY NEOFORGE QA INSPECTOR TEST SUITE (MINECRAFT 26.3 / JAVA 26) ===");

        long startTime = System.currentTimeMillis();

        testLifecycleEdgeCases();
        testTooltipEdgeCases();
        testAnvilCostAndMaterialBoundaries();
        testAnvilDangerZoneAndEmeraldInsuranceStress();
        testCombatLifestealEdgeCases();
        testHeldItemEffectsEdgeCases();
        testArmorSetBonusBoundaryAndCooldown();

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("================================================================================");
        System.out.println("KET QUA: " + testsRun + " nhom kiem thu, " + assertionsPassed + " phep kiem tra dat 100% thanh cong!");
        System.out.println("Thoi gian thuc thi: " + elapsed + " ms.");
        System.out.println("Toan bo chi tieu ky thuat NeoForge Loader dat tieu chuan ban giao.");
    }

    private static void assertTrue(boolean condition, String message) {
        assertionsPassed++;
        if (!condition) {
            throw new AssertionError("QA Kiem tra that bai: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        assertionsPassed++;
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("QA Kiem tra that bai [" + message + "]: Ky vong <" + expected + ">, Thuc te <" + actual + ">");
        }
    }

    private static void assertEquals(double expected, double actual, double delta, String message) {
        assertionsPassed++;
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError("QA Kiem tra that bai [" + message + "]: Ky vong <" + expected + ">, Thuc te <" + actual + "> (delta=" + delta + ")");
        }
    }

    /**
     * Nhom 1: Kiem thu cac tinh huong bien cua vong doi khoi dong tren NeoForge.
     */
    private static void testLifecycleEdgeCases() {
        testsRun++;
        System.out.println("[QA-01] Kiem thu vong doi va tinh an toan khoi tao lien tuc tren NeoForge...");

        MineQualityNeoForge.reset();
        assertTrue(!MineQualityNeoForge.isInitialized(), "Sau reset, mod khong duoc o trang thai khoi tao");

        // Khoi tao lan dau
        MineQualityNeoForge.init();
        assertTrue(MineQualityNeoForge.isInitialized(), "Sau init(), mod phai duoc danh dau la khoi tao");
        assertTrue(NeoForgeTooltipHandler.isRegistered(), "TooltipHandler phai dang ky");
        assertTrue(NeoForgeAnvilHandler.isRegistered(), "AnvilHandler phai dang ky");
        assertTrue(NeoForgeCombatHandler.isRegistered(), "CombatHandler phai dang ky");

        // Goi init lien tiep 10 lan (idempotent check)
        for (int i = 0; i < 10; i++) {
            MineQualityNeoForge.init();
            assertTrue(MineQualityNeoForge.isInitialized(), "Init lap lai phai luon an toan");
        }

        // Khoi tao bang constructor
        MineQualityNeoForge mod = new MineQualityNeoForge();
        assertTrue(mod != null, "Doi tuong mod khong duoc null");
        assertTrue(MineQualityNeoForge.isInitialized(), "Constructor phai kich hoat init()");

        // Reset va kiem tra lai
        MineQualityNeoForge.reset();
        assertTrue(!MineQualityNeoForge.isInitialized(), "Sau reset lan 2, trang thai phai tro ve false");
        assertTrue(!NeoForgeTooltipHandler.isRegistered(), "TooltipHandler phai huy dang ky");
        assertTrue(!NeoForgeAnvilHandler.isRegistered(), "AnvilHandler phai huy dang ky");
        assertTrue(!NeoForgeCombatHandler.isRegistered(), "CombatHandler phai huy dang ky");

        // Khoi dong lai san sang cho cac bai test tiep theo
        MineQualityNeoForge.init();
    }

    /**
     * Nhom 2: Kiem thu cac truong hop bien cua Tooltip Handler tren NeoForge.
     */
    private static void testTooltipEdgeCases() {
        testsRun++;
        System.out.println("[QA-02] Kiem thu tinh huong bien cua NeoForgeTooltipHandler...");

        ItemQualityData legendAxe = new ItemQualityData(QualityTier.LEGENDARY, 75, 2, true, true);

        // 1. Danh sach hien tai null: Handler phai tu khoi tao danh sach moi, khong throw NPE
        List<String> fromNull = NeoForgeTooltipHandler.appendTooltip("minecraft:netherite_axe", legendAxe, null);
        assertTrue(fromNull != null, "Tu tooltip null phai tra ve danh sach moi khac null");
        assertTrue(!fromNull.isEmpty(), "Netherite Axe Legendary phai duoc them cac dong mo ta pham chat");

        // 2. Danh sach hien tai da co san nhieu dong vanilla
        List<String> existing = new ArrayList<>();
        existing.add("Netherite Axe");
        existing.add("+10 Attack Damage");
        existing.add("4.0 Attack Speed");
        int originalSize = existing.size();

        List<String> appended = NeoForgeTooltipHandler.appendTooltip("minecraft:netherite_axe", legendAxe, existing);
        assertTrue(appended.size() > originalSize, "Danh sach tooltip phai duoc bo sung them dong moi");
        assertEquals("Netherite Axe", appended.get(0), "Dong dau tien van phai la ten vat pham goc");

        // 3. Vat pham khong co namespace (chi co name)
        List<String> noNamespace = NeoForgeTooltipHandler.appendTooltip("diamond_sword", legendAxe, new ArrayList<>());
        assertTrue(!noNamespace.isEmpty(), "Dinh danh khong kem namespace phai duoc nhan dien hop le");

        // 4. Vat pham khong du dieu kien (Stick, Dirt, Apple, Golden Apple)
        List<String> stick = NeoForgeTooltipHandler.appendTooltip("minecraft:stick", legendAxe, new ArrayList<>());
        assertTrue(stick.isEmpty(), "Gay go khong duoc phep them tooltip pham chat");

        List<String> apple = NeoForgeTooltipHandler.appendTooltip("minecraft:golden_apple", legendAxe, new ArrayList<>());
        assertTrue(apple.isEmpty(), "Tao vang khong duoc phep them tooltip pham chat");

        // 5. Tham so null tuyet doi
        List<String> allNull = NeoForgeTooltipHandler.appendTooltip(null, null, null);
        assertTrue(allNull != null && allNull.isEmpty(), "Tat ca tham so null phai tra ve list rong an toan");
    }

    /**
     * Nhom 3: Kiem thu chan tran EXP 30 cap va ma tran nguyen lieu rèn.
     */
    private static void testAnvilCostAndMaterialBoundaries() {
        testsRun++;
        System.out.println("[QA-03] Kiem thu gioi han tran 30 EXP va nguyen lieu ren hop le...");

        // Kiem tra cong thuc chi phi: min(30, baseCost + (reforgeCount * 2))
        // Netherite base = 4
        assertEquals(4, NeoForgeAnvilHandler.getExpCost("minecraft:netherite_sword", 0), "Netherite lan dau ren phai ton 4 cap");
        assertEquals(6, NeoForgeAnvilHandler.getExpCost("minecraft:netherite_sword", 1), "Netherite lan 1 tach ton 6 cap");
        assertEquals(14, NeoForgeAnvilHandler.getExpCost("minecraft:netherite_sword", 5), "Netherite lan 5 tach ton 14 cap");
        assertEquals(30, NeoForgeAnvilHandler.getExpCost("minecraft:netherite_sword", 13), "Netherite lan 13 tach ton 30 cap");
        assertEquals(30, NeoForgeAnvilHandler.getExpCost("minecraft:netherite_sword", 14), "Netherite lan 14 tach bi chan o 30 cap");
        assertEquals(30, NeoForgeAnvilHandler.getExpCost("minecraft:netherite_sword", 100), "Netherite 100 lan tach bi chan o 30 cap");
        assertEquals(30, NeoForgeAnvilHandler.getExpCost("minecraft:netherite_sword", 1000), "Netherite 1000 lan tach bi chan o 30 cap");

        // ReforgeCount am phai duoc phong ve an toan, khong giam chi phi xuong duoi baseCost
        assertEquals(4, NeoForgeAnvilHandler.getExpCost("minecraft:netherite_sword", -5), "Reforge count am phai coi nhu 0");

        // Do da (Leather): base = 1
        assertEquals(1, NeoForgeAnvilHandler.getExpCost("minecraft:leather_chestplate", 0), "Do da ton 1 cap ban dau");
        assertEquals(29, NeoForgeAnvilHandler.getExpCost("minecraft:leather_chestplate", 14), "Do da 14 lan tach ton 29 cap");
        assertEquals(30, NeoForgeAnvilHandler.getExpCost("minecraft:leather_chestplate", 15), "Do da 15 lan tach ton 30 cap");
        assertEquals(30, NeoForgeAnvilHandler.getExpCost("minecraft:leather_chestplate", 50), "Do da 50 lan tach bi chan o 30 cap");

        // Kiem tra nguyen lieu ren dac biet
        ItemQualityData data = new ItemQualityData(QualityTier.COMMON, 0, 0, false, false);

        // Elytra -> phantom_membrane
        assertTrue(NeoForgeAnvilHandler.canPerformReforge("minecraft:elytra", data, "minecraft:phantom_membrane", 1, 30), "Elytra phai ren bang phantom_membrane");
        assertTrue(!NeoForgeAnvilHandler.canPerformReforge("minecraft:elytra", data, "minecraft:leather", 1, 30), "Elytra khong the ren bang leather");

        // Khiên (Shield) -> planks hoac iron_ingot
        assertTrue(NeoForgeAnvilHandler.canPerformReforge("minecraft:shield", data, "minecraft:oak_planks", 1, 30), "Khien ren duoc bang oak_planks");
        assertTrue(NeoForgeAnvilHandler.canPerformReforge("minecraft:shield", data, "minecraft:iron_ingot", 1, 30), "Khien ren duoc bang iron_ingot");
        assertTrue(!NeoForgeAnvilHandler.canPerformReforge("minecraft:shield", data, "minecraft:diamond", 1, 30), "Khien khong the ren bang diamond");

        // Chùy (Mace) -> heavy_core, breeze_rod, iron_ingot
        assertTrue(NeoForgeAnvilHandler.canPerformReforge("minecraft:mace", data, "minecraft:heavy_core", 1, 30), "Chuy ren duoc bang heavy_core");
        assertTrue(NeoForgeAnvilHandler.canPerformReforge("minecraft:mace", data, "minecraft:breeze_rod", 1, 30), "Chuy ren duoc bang breeze_rod");
        assertTrue(NeoForgeAnvilHandler.canPerformReforge("minecraft:mace", data, "minecraft:iron_ingot", 1, 30), "Chuy ren duoc bang iron_ingot");

        // Đinh Ba (Trident) -> prismarine_shard, heart_of_the_sea
        assertTrue(NeoForgeAnvilHandler.canPerformReforge("minecraft:trident", data, "minecraft:prismarine_shard", 1, 30), "Dinh ba ren duoc bang prismarine_shard");
        assertTrue(NeoForgeAnvilHandler.canPerformReforge("minecraft:trident", data, "minecraft:heart_of_the_sea", 1, 30), "Dinh ba ren duoc bang heart_of_the_sea");
    }

    /**
     * Nhom 4: Kiem thu stress 1,000 luot rèn trong Vùng Mạo Hiểm (Danger Zone) kem Vé Bảo Hiểm Ngọc Lục Bảo.
     */
    private static void testAnvilDangerZoneAndEmeraldInsuranceStress() {
        testsRun++;
        System.out.println("[QA-04] Kiem thu stress 1000 luot ren Danger Zone voi Bao hiem Ngoc Luc Bao...");

        ItemQualityData epicItem = new ItemQualityData(QualityTier.EPIC, 0, 0, false, false);

        int emeraldSavedCount = 0;
        int successfulReforgeCount = 0;

        for (int i = 0; i < 1000; i++) {
            NeoForgeAnvilHandler.AnvilReforgeOutput output = NeoForgeAnvilHandler.processReforge(
                    "minecraft:diamond_chestplate",
                    epicItem,
                    "minecraft:diamond",
                    1,
                    30,
                    true // Co bao hiem Emerald
            );

            assertTrue(output.canReforge(), "Moi luot ren phai hop le");
            // Khi co bao hiem Emerald, vat pham TUYET DOI KHONG BAO GIO BI VO!
            assertTrue(!output.broken(), "Bao hiem Ngoc Luc Bao phai dam bao 100% vat pham khong bao gio bi vo!");

            if (output.emeraldConsumed()) {
                emeraldSavedCount++;
                assertTrue(output.resultingData() != null, "Khi Emerald vo thay, du lieu vat pham van phai ton tai");
                assertEquals(QualityTier.EPIC, output.resultingData().tier(), "Khi cuu do, pham chat giu nguyen bac EPIC");
            } else {
                successfulReforgeCount++;
            }
        }

        System.out.println("-> Qua 1000 luot ren EPIC: Emerald nat cuu do " + emeraldSavedCount + " lan, Thanh cong/giu cap " + successfulReforgeCount + " lan. Ty le vo: 0%!");
        assertTrue(emeraldSavedCount > 0, "Trong 1000 luot ren EPIC (ty le nguy hiem 75%), Emerald bat buoc phai duoc kich hoat nhieu lan");

        // Kiem tra khi KHONG co bao hiem: Phai co truong hop bi vo nat thuc su
        int brokenWithoutInsurance = 0;
        for (int i = 0; i < 200; i++) {
            NeoForgeAnvilHandler.AnvilReforgeOutput output = NeoForgeAnvilHandler.processReforge(
                    "minecraft:diamond_chestplate",
                    epicItem,
                    "minecraft:diamond",
                    1,
                    30,
                    false // Khong co bao hiem
            );
            if (output.broken()) {
                brokenWithoutInsurance++;
                assertEquals(null, output.resultingData(), "Trang bi vo nat phai co resultingData = null");
            }
        }
        assertTrue(brokenWithoutInsurance > 0, "Trong 200 luot ren EPIC khong bao hiem, phai co vat pham bi vo (thuc te: " + brokenWithoutInsurance + ")");
    }

    /**
     * Nhom 5: Kiem thu chi tiet Lifesteal 20% va cac dieu kien can chien.
     */
    private static void testCombatLifestealEdgeCases() {
        testsRun++;
        System.out.println("[QA-05] Kiem thu logic Lifesteal 20% va cac truong hop bien tan cong...");

        ItemQualityData legendSword = new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true);
        ItemQualityData legendAxe = new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true);
        ItemQualityData epicSword = new ItemQualityData(QualityTier.EPIC, 100, 0, false, false);
        ItemQualityData legendBow = new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true);

        // 1. Kiem Legendary: 20% cua 15 sát thương = 3.0 HP
        NeoForgeCombatHandler.LifestealResult res1 = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:diamond_sword", legendSword, 15.0, 20.0, 40.0);
        assertTrue(res1.applied(), "Kiem Legendary phai kich hoat Lifesteal");
        assertEquals(3.0, res1.lifestealAmount(), 0.001, "Lifesteal phai bang 20% sat thuong");
        assertEquals(23.0, res1.newHealth(), 0.001, "Mau moi phai bang 20 + 3 = 23");

        // 2. Riu Legendary (Axe cung thuoc MELEE_WEAPON): 20% cua 20 sat thuong = 4.0 HP
        NeoForgeCombatHandler.LifestealResult resAxe = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:netherite_axe", legendAxe, 20.0, 10.0, 40.0);
        assertTrue(resAxe.applied(), "Riu Legendary phai kich hoat Lifesteal vi la vu khi can chien");
        assertEquals(4.0, resAxe.lifestealAmount(), 0.001, "Lifesteal cua riu phai la 4.0 HP");
        assertEquals(14.0, resAxe.newHealth(), 0.001, "Mau sau khi hoi phai la 14.0 HP");

        // 3. Hoi phuc vuot qua Max Health: Phai bi chan o Max Health
        NeoForgeCombatHandler.LifestealResult resOverheal = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:diamond_sword", legendSword, 20.0, 38.0, 40.0);
        assertTrue(resOverheal.applied(), "Phai kich hoat du gan day mau");
        assertEquals(4.0, resOverheal.lifestealAmount(), 0.001, "Luong hoi tinh toan la 4.0");
        assertEquals(40.0, resOverheal.newHealth(), 0.001, "Mau thuc te phai bi chan tran o 40.0");

        // 4. Kiem EPIC (khong phai Legendary): Khong co Lifesteal
        NeoForgeCombatHandler.LifestealResult resEpic = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:diamond_sword", epicSword, 15.0, 20.0, 40.0);
        assertTrue(!resEpic.applied(), "Kiem EPIC khong duoc phep co Lifesteal");

        // 5. Cung Legendary (Ranged): Khong co Lifesteal can chien
        NeoForgeCombatHandler.LifestealResult resBow = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:bow", legendBow, 15.0, 20.0, 40.0);
        assertTrue(!resBow.applied(), "Cung khong duoc phep kich hoat can chien Lifesteal");

        // 6. Sat thuong gay ra am hoac bang 0
        NeoForgeCombatHandler.LifestealResult resZero = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:diamond_sword", legendSword, 0.0, 20.0, 40.0);
        assertTrue(!resZero.applied(), "Sat thuong bang 0 khong kich hoat Lifesteal");

        // 7. Max Health am hoac bang 0
        NeoForgeCombatHandler.LifestealResult resZeroMax = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:diamond_sword", legendSword, 10.0, 10.0, 0.0);
        assertTrue(!resZeroMax.applied(), "Max health <= 0 phai tra ve NONE an toan");
    }

    /**
     * Nhom 6: Kiem thu hieu ung cam vat pham tren tay chinh.
     */
    private static void testHeldItemEffectsEdgeCases() {
        testsRun++;
        System.out.println("[QA-06] Kiem thu hieu ung Held Item (Haste II, Fatigue I, Weakness I)...");

        ItemQualityData legendPick = new ItemQualityData(QualityTier.LEGENDARY, 50, 0, true, true);
        ItemQualityData legendShovel = new ItemQualityData(QualityTier.LEGENDARY, 50, 0, true, true);
        ItemQualityData legendSword = new ItemQualityData(QualityTier.LEGENDARY, 50, 0, true, true);
        ItemQualityData junkPick = new ItemQualityData(QualityTier.JUNK, 0, 0, false, false);
        ItemQualityData junkShovel = new ItemQualityData(QualityTier.JUNK, 0, 0, false, false);
        ItemQualityData junkHoe = new ItemQualityData(QualityTier.JUNK, 0, 0, false, false);
        ItemQualityData junkSword = new ItemQualityData(QualityTier.JUNK, 0, 0, false, false);
        ItemQualityData junkAxe = new ItemQualityData(QualityTier.JUNK, 0, 0, false, false);

        // Cuoc & Xeng Legendary -> Mining Tool -> Haste II
        assertTrue(NeoForgeCombatHandler.handleHeldItem("minecraft:diamond_pickaxe", legendPick).haste2(), "Cuoc Legendary phai co Haste II");
        assertTrue(NeoForgeCombatHandler.handleHeldItem("minecraft:diamond_shovel", legendShovel).haste2(), "Xeng Legendary phai co Haste II");
        // Kiem Legendary khong phai Mining Tool -> khong co Haste II
        assertTrue(!NeoForgeCombatHandler.handleHeldItem("minecraft:diamond_sword", legendSword).haste2(), "Kiem Legendary khong co Haste II");

        // Junk Mining Tools -> Mining Fatigue I
        assertTrue(NeoForgeCombatHandler.handleHeldItem("minecraft:iron_pickaxe", junkPick).miningFatigue1(), "Cuoc Junk phai dinh Mining Fatigue I");
        assertTrue(NeoForgeCombatHandler.handleHeldItem("minecraft:iron_shovel", junkShovel).miningFatigue1(), "Xeng Junk phai dinh Mining Fatigue I");
        assertTrue(NeoForgeCombatHandler.handleHeldItem("minecraft:iron_hoe", junkHoe).miningFatigue1(), "Cuoc vuon Junk phai dinh Mining Fatigue I");

        // Junk Melee Weapons -> Weakness I
        assertTrue(NeoForgeCombatHandler.handleHeldItem("minecraft:iron_sword", junkSword).weakness1(), "Kiem Junk phai dinh Weakness I");
        assertTrue(NeoForgeCombatHandler.handleHeldItem("minecraft:iron_axe", junkAxe).weakness1(), "Riu Junk phai dinh Weakness I");

        // Input null
        assertEquals(NeoForgeCombatHandler.HeldItemEffect.NONE, NeoForgeCombatHandler.handleHeldItem(null, null), "Held item null phai tra ve NONE");
    }

    /**
     * Nhom 7: Kiem thu Full Set Bonus 4 mon giap 40 HP, nguong mau <30% va thoi gian hoi chieu 60 giay.
     */
    private static void testArmorSetBonusBoundaryAndCooldown() {
        testsRun++;
        System.out.println("[QA-07] Kiem thu Full Set Bonus giap 40 HP, nguong < 30% HP va thoi gian hoi chieu...");

        List<ItemQualityData> setAffinity100 = List.of(
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true)
        );

        List<ItemQualityData> setAffinity0 = List.of(
                new ItemQualityData(QualityTier.LEGENDARY, 0, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 0, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 0, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 0, 0, true, true)
        );

        double maxHp = 40.0; // 40 HP chuan theo tai lieu

        // 1. Kiem tra nguong mau: 30% cua 40 HP la 12.0 HP
        // Máu bang dung 12.0 HP (12.0 / 40.0 = 0.30) -> Khong duoc kich hoat vi yeu cau < 0.30
        NeoForgeCombatHandler.FullSetBonusResult at30 = NeoForgeCombatHandler.handleArmorSetTick(
                setAffinity100, 12.0, maxHp, 2000L, 0L);
        assertTrue(!at30.emergencyRecoveryTriggered(), "Mau dung 12.0 HP (30%) khong duoc phep kich hoat (yeu cau < 30%)");
        assertTrue(at30.permanentFireResistance(), "Nhung van phai co khang lua vinh vien");

        // Máu 11.9 HP (< 30%) voi thoi gian hoi chieu da het (2000 - 0 = 2000 ticks >= 1200 ticks) -> Kich hoat thanh cong
        NeoForgeCombatHandler.FullSetBonusResult below30 = NeoForgeCombatHandler.handleArmorSetTick(
                setAffinity100, 11.9, maxHp, 2000L, 0L);
        assertTrue(below30.emergencyRecoveryTriggered(), "Mau 11.9 HP (< 30%) phai kich hoat cuu sinh khi da het hoi chieu");
        // Voi Affinity 100/100: thoi gian duy tri tang 50% tu 200 ticks len 300 ticks
        assertEquals(300, below30.emergencyRecoveryDurationTicks(), "Affinity 100 phai keo dai 300 ticks (15 giay)");

        // Voi Affinity 0/100: thoi gian duy tri co ban la 200 ticks (10 giay)
        NeoForgeCombatHandler.FullSetBonusResult aff0Res = NeoForgeCombatHandler.handleArmorSetTick(
                setAffinity0, 5.0, maxHp, 2000L, 0L);
        assertEquals(200, aff0Res.emergencyRecoveryDurationTicks(), "Affinity 0 phai duy tri 200 ticks (10 giay)");

        // 2. Kiem tra bien Cooldown 60 giay (1200 ticks)
        long triggerTick = 10000L;
        // O tick 11199 (da troi qua 1199 ticks < 1200) -> Khong duoc phep kich hoat
        NeoForgeCombatHandler.FullSetBonusResult tick1199 = NeoForgeCombatHandler.handleArmorSetTick(
                setAffinity100, 8.0, maxHp, triggerTick + 1199L, triggerTick);
        assertTrue(!tick1199.emergencyRecoveryTriggered(), "O tick 1199 chua du 1200 ticks cooldown, khong duoc kich hoat");

        // O tick 12000 (dung 1200 ticks) -> Kich hoat thanh cong
        NeoForgeCombatHandler.FullSetBonusResult tick1200 = NeoForgeCombatHandler.handleArmorSetTick(
                setAffinity100, 8.0, maxHp, triggerTick + 1200L, triggerTick);
        assertTrue(tick1200.emergencyRecoveryTriggered(), "Dung 1200 ticks troi qua phai kich hoat lai duoc");
        assertEquals(triggerTick + 1200L, tick1200.newLastTriggerTick(), "Last trigger tick moi phai duoc cap nhat");

        // 3. Giap co mon null hoac chi co 3 mon
        List<ItemQualityData> withNullPiece = Arrays.asList(
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true),
                null,
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true)
        );
        NeoForgeCombatHandler.FullSetBonusResult nullPieceResult = NeoForgeCombatHandler.handleArmorSetTick(
                withNullPiece, 8.0, maxHp, 20000L, 0L);
        assertTrue(!nullPieceResult.fullSetLegendary(), "Co 1 mon null khong duoc tinh la Full Set");
        assertTrue(!nullPieceResult.emergencyRecoveryTriggered(), "Co 1 mon null khong duoc kich hoat cuu sinh");
        assertTrue(!nullPieceResult.permanentFireResistance(), "Co 1 mon null khong co khang lua vinh vien");
    }
}
