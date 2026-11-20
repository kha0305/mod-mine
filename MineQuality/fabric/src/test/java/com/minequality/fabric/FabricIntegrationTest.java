package com.minequality.fabric;

import com.minequality.fabric.event.FabricAnvilHandler;
import com.minequality.fabric.event.FabricCombatHandler;
import com.minequality.fabric.event.FabricTooltipHandler;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityTier;

import java.util.ArrayList;
import java.util.List;

/**
 * Bo kiem thu tich hop doc lap cho Fabric Loader.
 * Kiem tra toan dien tinh dung dan, cac truong hop bien va do an toan null cua:
 * - FabricTooltipHandler
 * - FabricAnvilHandler
 * - FabricCombatHandler
 * - MineQualityFabric entrypoint
 */
public class FabricIntegrationTest {

    public static void main(String[] args) {
        System.out.println("Bat dau kiem thu toan dien mo-dun Fabric Loader...");

        testEntrypointInitialization();
        testTooltipHandler();
        testAnvilHandlerReforgeLogic();
        testAnvilHandlerDangerZoneAndInsurance();
        testCombatHandlerLifesteal();
        testCombatHandlerHeldItemEffects();
        testCombatHandlerFullSetBonus();
        testNullSafety();

        System.out.println("TOAN BO KIEM THU TICH HOP FABRIC DA HOAN TAT XUAT SAC VA DAT 100%!");
    }

    private static void testEntrypointInitialization() {
        System.out.println("[Kiem thu 1] Khoi dong MineQualityFabric entrypoint va dang ky cac hook...");
        FabricTooltipHandler.resetRegistration();
        FabricAnvilHandler.resetRegistration();
        FabricCombatHandler.resetRegistration();

        if (FabricTooltipHandler.isRegistered() || FabricAnvilHandler.isRegistered() || FabricCombatHandler.isRegistered()) {
            throw new AssertionError("Loi: Cac handler phai o trang thai chua dang ky ban dau.");
        }

        MineQualityFabric mod = new MineQualityFabric();
        mod.onInitialize();

        if (!FabricTooltipHandler.isRegistered()) {
            throw new AssertionError("Loi: FabricTooltipHandler chua duoc dang ky thanh cong.");
        }
        if (!FabricAnvilHandler.isRegistered()) {
            throw new AssertionError("Loi: FabricAnvilHandler chua duoc dang ky thanh cong.");
        }
        if (!FabricCombatHandler.isRegistered()) {
            throw new AssertionError("Loi: FabricCombatHandler chua duoc dang ky thanh cong.");
        }
        System.out.println("  -> Dang ky cac hook Fabric thanh cong 100%.");
    }

    private static void testTooltipHandler() {
        System.out.println("[Kiem thu 2] Kiem thu FabricTooltipHandler...");

        // Kiem tra vat pham khong du dieu kien
        List<String> stickTooltip = FabricTooltipHandler.appendTooltip("minecraft:stick", ItemQualityData.DEFAULT, new ArrayList<>());
        if (!stickTooltip.isEmpty()) {
            throw new AssertionError("Loi: Vat pham khong du dieu kien khong duoc phep them tooltip.");
        }

        // Kiem tra ao giap kim cuong bac Legendary
        ItemQualityData legendaryData = ItemQualityData.of(QualityTier.LEGENDARY, 100);
        List<String> chestplateTooltip = FabricTooltipHandler.appendTooltip("minecraft:diamond_chestplate", legendaryData, new ArrayList<>());

        boolean hasQuality = chestplateTooltip.stream().anyMatch(s -> s.contains("Quality: Legendary"));
        boolean hasAffinity = chestplateTooltip.stream().anyMatch(s -> s.contains("Item Affinity: 100/100"));
        boolean hasSlot = chestplateTooltip.stream().anyMatch(s -> s.contains("When on Body:"));
        boolean hasSetBonusFire = chestplateTooltip.stream().anyMatch(s -> s.contains("Permanent Fire Resistance"));
        boolean hasSetBonusRecovery = chestplateTooltip.stream().anyMatch(s -> s.contains("Emergency Recovery"));
        boolean hasUnbreakable = chestplateTooltip.stream().anyMatch(s -> s.contains("Unbreakable & Fireproof"));

        if (!hasQuality || !hasAffinity || !hasSlot || !hasSetBonusFire || !hasSetBonusRecovery || !hasUnbreakable) {
            throw new AssertionError("Loi: Tooltip cua ao giap Legendary thieu dong thong tin bat buoc.");
        }

        // Kiem tra kiem sat bac Junk
        ItemQualityData junkData = ItemQualityData.of(QualityTier.JUNK, 0);
        List<String> swordTooltip = FabricTooltipHandler.appendTooltip("minecraft:iron_sword", junkData, new ArrayList<>());
        boolean hasJunkQuality = swordTooltip.stream().anyMatch(s -> s.contains("Quality: Junk"));
        boolean hasWeaknessDebuff = swordTooltip.stream().anyMatch(s -> s.contains("Debuff: Weakness I while held"));

        if (!hasJunkQuality || !hasWeaknessDebuff) {
            throw new AssertionError("Loi: Tooltip kiem Junk khong hien thi dung debuff Weakness.");
        }
        System.out.println("  -> FabricTooltipHandler hoat dong chuan xac 100%.");
    }

    private static void testAnvilHandlerReforgeLogic() {
        System.out.println("[Kiem thu 3] Kiem thu logic co ban va chi phi cua FabricAnvilHandler...");

        String sword = "minecraft:diamond_sword";
        ItemQualityData commonData = ItemQualityData.of(QualityTier.COMMON, 0);

        // Kiem tra sai nguyen lieu
        boolean invalidMat = FabricAnvilHandler.canPerformReforge(sword, commonData, "minecraft:iron_ingot", 1, 30);
        if (invalidMat) {
            throw new AssertionError("Loi: Sai nguyen lieu ma van cho phep ren.");
        }

        // Kiem tra so luong nguyen lieu = 0
        boolean zeroMat = FabricAnvilHandler.canPerformReforge(sword, commonData, "minecraft:diamond", 0, 30);
        if (zeroMat) {
            throw new AssertionError("Loi: Khong co nguyen lieu ma van cho phep ren.");
        }

        // Kiem tra thieu cap do EXP
        int expCost = FabricAnvilHandler.getExpCost(sword, 0); // Diamond baseCost = 3
        if (expCost != 3) {
            throw new AssertionError("Loi: Chi phi goc do kim cuong lan dau phai la 3 cap EXP.");
        }
        boolean notEnoughExp = FabricAnvilHandler.canPerformReforge(sword, commonData, "minecraft:diamond", 1, 2);
        if (notEnoughExp) {
            throw new AssertionError("Loi: Thieu cap do EXP ma van cho phep ren.");
        }

        // Kiem tra muc tran bao ve 30 cap EXP
        int cappedExp = FabricAnvilHandler.getExpCost(sword, 20); // 3 + 20*2 = 43 -> phai chan o 30
        if (cappedExp != 30) {
            throw new AssertionError("Loi: Muc tran EXP phai la 30 cap de tranh loi Too Expensive.");
        }

        // Kiem tra khong the ren tiep neu da la Legendary
        ItemQualityData legData = ItemQualityData.of(QualityTier.LEGENDARY);
        boolean legCanReforge = FabricAnvilHandler.canPerformReforge(sword, legData, "minecraft:diamond", 1, 30);
        if (legCanReforge) {
            throw new AssertionError("Loi: Trang bi Legendary khong duoc phep ren them.");
        }
        FabricAnvilHandler.AnvilReforgeOutput legOutput = FabricAnvilHandler.processReforge(sword, legData, "minecraft:diamond", 1, 30, false);
        if (legOutput.canReforge()) {
            throw new AssertionError("Loi: processReforge phai tu choi trang bi Legendary.");
        }

        System.out.println("  -> Logic co ban va gioi han EXP 30 cap dat chuan 100%.");
    }

    private static void testAnvilHandlerDangerZoneAndInsurance() {
        System.out.println("[Kiem thu 4] Kiem thu Vung An Toan, Vung Mao Hiem va Bao Hiem Ngoc Luc Bao...");

        String axe = "minecraft:iron_axe";

        // Vung an toan (Safe Zone: Junk, Common, Uncommon) -> Tuyet doi khong bao gio vo do
        ItemQualityData safeData = ItemQualityData.of(QualityTier.UNCOMMON, 0);
        for (int i = 0; i < 50; i++) {
            FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                    axe, safeData, "minecraft:iron_ingot", 1, 30, false
            );
            if (out.broken()) {
                throw new AssertionError("Loi nghiem trong: Vung an toan ma bi vo do!");
            }
            if (out.resultingData() == null) {
                throw new AssertionError("Loi: Ket qua trong vung an toan phai luon co du lieu.");
            }
        }
        System.out.println("    + Vung an toan 50 lan thu nghiem 0% vo do: Dat!");

        // Vung mao hiem (Danger Zone) kem Bao Hiem Ngoc Luc Bao: Khong bao gio bi mat trang bi!
        ItemQualityData epicData = ItemQualityData.of(QualityTier.EPIC, 10);
        int emeraldSavedCount = 0;
        for (int i = 0; i < 50; i++) {
            FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                    axe, epicData, "minecraft:iron_ingot", 1, 30, true
            );
            if (out.broken()) {
                throw new AssertionError("Loi nghiem trong: Co bao hiem Ngoc Luc Bao ma trang bi van bi vo!");
            }
            if (out.emeraldConsumed()) {
                emeraldSavedCount++;
            }
        }
        if (emeraldSavedCount == 0) {
            System.out.println("    [Luu y: Ngoc Luc Bao khong can kich hoat trong dot thu nay do may man]");
        } else {
            System.out.println("    + Co che Bao Hiem Ngoc Luc Bao da cuu song do thanh cong " + emeraldSavedCount + " lan!");
        }

        // Vung mao hiem khong co bao hiem: Co nguy co vo do (broken = true va resultingData = null)
        boolean hasBrokenObserved = false;
        for (int i = 0; i < 100; i++) {
            FabricAnvilHandler.AnvilReforgeOutput out = FabricAnvilHandler.processReforge(
                    axe, epicData, "minecraft:iron_ingot", 1, 30, false
            );
            if (out.broken()) {
                hasBrokenObserved = true;
                if (out.resultingData() != null) {
                    throw new AssertionError("Loi: Khi bi vo do thi resultingData phai la null!");
                }
                break;
            }
        }
        if (!hasBrokenObserved) {
            throw new AssertionError("Loi: Vung mao hiem 75% vo do ma sau 100 lan khong ghi nhan vo!");
        }
        System.out.println("    + Vung mao hiem vo do khi khong co bao hiem: Dat!");
    }

    private static void testCombatHandlerLifesteal() {
        System.out.println("[Kiem thu 5] Kiem thu 20% Lifesteal khi can chien Legendary...");

        String sword = "minecraft:netherite_sword";
        ItemQualityData legSword = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData epicSword = ItemQualityData.of(QualityTier.EPIC);

        // Sat thuong 20.0 gay ra boi kiem Legendary -> phai hoi dung 4.0 mau (20%)
        FabricCombatHandler.LifestealResult result = FabricCombatHandler.handleMeleeAttack(
                sword, legSword, 20.0, 10.0, 20.0
        );
        if (!result.applied() || Math.abs(result.lifestealAmount() - 4.0) > 0.001 || Math.abs(result.newHealth() - 14.0) > 0.001) {
            throw new AssertionError("Loi: Hut mau Legendary can chien tinh toan sai (Phai hoi 4.0 HP).");
        }

        // Gioi han khong vuot qua maxHealth
        FabricCombatHandler.LifestealResult cappedResult = FabricCombatHandler.handleMeleeAttack(
                sword, legSword, 20.0, 18.0, 20.0
        );
        if (Math.abs(cappedResult.newHealth() - 20.0) > 0.001) {
            throw new AssertionError("Loi: Mau sau khi hut khong duoc vuot qua maxHealth.");
        }

        // Kiem Epic khong duoc phep hut mau
        FabricCombatHandler.LifestealResult epicResult = FabricCombatHandler.handleMeleeAttack(
                sword, epicSword, 20.0, 10.0, 20.0
        );
        if (epicResult.applied() || epicResult.lifestealAmount() != 0.0) {
            throw new AssertionError("Loi: Chi co vu khi can chien Legendary moi co quyen hut mau.");
        }

        // Cup Legendary danh quai khong duoc hut mau (khong phai nhom Melee Weapon)
        FabricCombatHandler.LifestealResult toolResult = FabricCombatHandler.handleMeleeAttack(
                "minecraft:diamond_pickaxe", legSword, 20.0, 10.0, 20.0
        );
        if (toolResult.applied()) {
            throw new AssertionError("Loi: Cong cu dao khong duoc phep hut mau.");
        }

        System.out.println("  -> Co che Lifesteal 20% can chien Legendary dat 100%.");
    }

    private static void testCombatHandlerHeldItemEffects() {
        System.out.println("[Kiem thu 6] Kiem thu hieu ung khi cam vat pham (Haste II va Debuff Junk)...");

        // Cup Legendary -> Haste II
        FabricCombatHandler.HeldItemEffect pickResult = FabricCombatHandler.handleHeldItem(
                "minecraft:diamond_pickaxe", ItemQualityData.of(QualityTier.LEGENDARY)
        );
        if (!pickResult.haste2() || pickResult.miningFatigue1() || pickResult.weakness1()) {
            throw new AssertionError("Loi: Cup Legendary phai co Haste II.");
        }

        // Cup Junk -> Mining Fatigue I
        FabricCombatHandler.HeldItemEffect junkPickResult = FabricCombatHandler.handleHeldItem(
                "minecraft:iron_pickaxe", ItemQualityData.of(QualityTier.JUNK)
        );
        if (junkPickResult.haste2() || !junkPickResult.miningFatigue1()) {
            throw new AssertionError("Loi: Cup Junk phai bi dinh Mining Fatigue I.");
        }

        // Kiem Junk -> Weakness I
        FabricCombatHandler.HeldItemEffect junkSwordResult = FabricCombatHandler.handleHeldItem(
                "minecraft:iron_sword", ItemQualityData.of(QualityTier.JUNK)
        );
        if (junkSwordResult.haste2() || !junkSwordResult.weakness1()) {
            throw new AssertionError("Loi: Kiem Junk phai bi dinh Weakness I.");
        }

        // Kiem Legendary -> khong co Haste II
        FabricCombatHandler.HeldItemEffect legSwordResult = FabricCombatHandler.handleHeldItem(
                "minecraft:diamond_sword", ItemQualityData.of(QualityTier.LEGENDARY)
        );
        if (legSwordResult.haste2()) {
            throw new AssertionError("Loi: Kiem Legendary khong duoc phep nhan Haste II.");
        }

        System.out.println("  -> Cac hieu ung cam vat pham (Haste II, Fatigue, Weakness) dat 100%.");
    }

    private static void testCombatHandlerFullSetBonus() {
        System.out.println("[Kiem thu 7] Kiem thu Full Set Bonus giap Legendary (Khang lua va Cuu sinh <30% HP)...");

        List<ItemQualityData> fullLeg = List.of(
                ItemQualityData.of(QualityTier.LEGENDARY, 100),
                ItemQualityData.of(QualityTier.LEGENDARY, 100),
                ItemQualityData.of(QualityTier.LEGENDARY, 100),
                ItemQualityData.of(QualityTier.LEGENDARY, 100)
        );

        // Khong du 4 mon -> khong hoat dong
        FabricCombatHandler.FullSetBonusResult incomplete = FabricCombatHandler.handleArmorSetTick(
                List.of(ItemQualityData.of(QualityTier.LEGENDARY)), 10.0, 40.0, 1000L, 0L
        );
        if (incomplete.fullSetLegendary()) {
            throw new AssertionError("Loi: Thieu mon giap thi khong the kich hoat Full Set.");
        }

        // Du 4 mon nhung co 1 mon khong phai Legendary
        List<ItemQualityData> mixed = List.of(
                ItemQualityData.of(QualityTier.LEGENDARY),
                ItemQualityData.of(QualityTier.LEGENDARY),
                ItemQualityData.of(QualityTier.EPIC),
                ItemQualityData.of(QualityTier.LEGENDARY)
        );
        FabricCombatHandler.FullSetBonusResult mixedResult = FabricCombatHandler.handleArmorSetTick(
                mixed, 10.0, 40.0, 1000L, 0L
        );
        if (mixedResult.fullSetLegendary()) {
            throw new AssertionError("Loi: Co mon giap khong phai Legendary thi khong kich hoat Full Set.");
        }

        // Du 4 mon Legendary, mau day (40.0 / 40.0 HP = 100%) -> Khang lua bat, khong kich hoat cuu sinh
        FabricCombatHandler.FullSetBonusResult fullHealthResult = FabricCombatHandler.handleArmorSetTick(
                fullLeg, 40.0, 40.0, 1000L, 0L
        );
        if (!fullHealthResult.fullSetLegendary() || !fullHealthResult.permanentFireResistance() || fullHealthResult.emergencyRecoveryTriggered()) {
            throw new AssertionError("Loi: Mau day thi khong duoc phep kich hoat cuu sinh.");
        }

        // Mau tut duoi 30% (< 12 HP tren tong 40 HP, vi du 10.0 HP) va chua cooldown -> Kich hoat cuu sinh!
        // 100 Affinity -> thoi gian duoc nhan 1.5 lan (200 * 1.5 = 300 ticks)
        FabricCombatHandler.FullSetBonusResult emergencyResult = FabricCombatHandler.handleArmorSetTick(
                fullLeg, 10.0, 40.0, 2000L, 500L
        );
        if (!emergencyResult.emergencyRecoveryTriggered() || emergencyResult.emergencyRecoveryDurationTicks() != 300) {
            throw new AssertionError("Loi: Cuu sinh khan cap phai kich hoat va keo dai 300 ticks nho 100 Affinity.");
        }
        if (emergencyResult.newLastTriggerTick() != 2000L) {
            throw new AssertionError("Loi: newLastTriggerTick phai duoc cap nhat thanh 2000L.");
        }

        // Kiem tra cooldown: Trong vong 60 giay (1200 ticks), vi du tick 2500L (cach 500 ticks < 1200 ticks) -> khong kich hoat
        FabricCombatHandler.FullSetBonusResult onCooldownResult = FabricCombatHandler.handleArmorSetTick(
                fullLeg, 10.0, 40.0, 2500L, 2000L
        );
        if (onCooldownResult.emergencyRecoveryTriggered()) {
            throw new AssertionError("Loi: Dang trong thoi gian hoi chiêu (cooldown) ma van cho phep kich hoat cuu sinh.");
        }

        // Sau khi het cooldown: Tick 3300L (cach 1300 ticks >= 1200 ticks) -> duoc phep kich hoat lai!
        FabricCombatHandler.FullSetBonusResult postCooldownResult = FabricCombatHandler.handleArmorSetTick(
                fullLeg, 8.0, 40.0, 3300L, 2000L
        );
        if (!postCooldownResult.emergencyRecoveryTriggered()) {
            throw new AssertionError("Loi: Da het thoi gian hoi chieu ma khong the kich hoat lai cuu sinh.");
        }

        System.out.println("  -> Full Set Bonus giap Legendary dat 100%.");
    }

    private static void testNullSafety() {
        System.out.println("[Kiem thu 8] Kiem thu an toan tuyet doi voi cac tham so Null...");

        // Tooltip handler
        FabricTooltipHandler.appendTooltip(null, null, null);
        FabricTooltipHandler.appendTooltip("minecraft:iron_sword", null, null);

        // Anvil handler
        FabricAnvilHandler.canPerformReforge(null, null, null, 0, 0);
        FabricAnvilHandler.AnvilReforgeOutput outNull = FabricAnvilHandler.processReforge(null, null, null, 0, 0, false);
        if (outNull.canReforge()) {
            throw new AssertionError("Loi: Anvil phai tu choi tham so null.");
        }

        // Combat handler
        FabricCombatHandler.LifestealResult lsNull = FabricCombatHandler.handleMeleeAttack(null, null, 0, 0, 0);
        if (lsNull.applied()) {
            throw new AssertionError("Loi: Lifesteal phai an toan null.");
        }
        FabricCombatHandler.HeldItemEffect heldNull = FabricCombatHandler.handleHeldItem(null, null);
        if (heldNull.haste2()) {
            throw new AssertionError("Loi: HeldItem phai an toan null.");
        }
        FabricCombatHandler.FullSetBonusResult fsNull = FabricCombatHandler.handleArmorSetTick(null, 0, 0, 0, 0);
        if (fsNull.fullSetLegendary()) {
            throw new AssertionError("Loi: FullSetBonus phai an toan null.");
        }

        System.out.println("  -> Toan bo co che kiem tra Null an toan 100%.");
    }
}
