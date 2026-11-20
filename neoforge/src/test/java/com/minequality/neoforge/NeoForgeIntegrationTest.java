package com.minequality.neoforge;

import com.minequality.neoforge.event.NeoForgeAnvilHandler;
import com.minequality.neoforge.event.NeoForgeCombatHandler;
import com.minequality.neoforge.event.NeoForgeTooltipHandler;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityTier;

import java.util.ArrayList;
import java.util.List;

/**
 * Bo kiem thu tich hop toan dien cho mo-dun NeoForge Loader.
 * Kiem tra kha nang tich hop, an toan tuyet doi voi null, co che Tooltip, Anvil Reforge va Combat Handlers.
 */
public class NeoForgeIntegrationTest {

    public static void main(String[] args) {
        System.out.println("Bat dau chuoi kiem thu tich hop NeoForge Loader tren Java 26...");

        testLifecycleAndRegistration();
        testNeoForgeTooltipHandler();
        testNeoForgeAnvilHandler();
        testNeoForgeCombatHandler();

        System.out.println("Tat ca cac bai kiem thu NeoForge Loader deu dat 100% thanh cong!");
    }

    /**
     * Kiem thu vong doi khoi dong va dang ky Event Bus cua MineQualityNeoForge.
     */
    private static void testLifecycleAndRegistration() {
        System.out.println("[Kiem thu 1] Vong doi va dang ky NeoForge Event Bus...");

        MineQualityNeoForge.reset();
        assertTrue(!MineQualityNeoForge.isInitialized(), "Mod khong duoc o trang thai da khoi tao sau reset");
        assertTrue(!NeoForgeTooltipHandler.isRegistered(), "TooltipHandler khong duoc dang ky sau reset");
        assertTrue(!NeoForgeAnvilHandler.isRegistered(), "AnvilHandler khong duoc dang ky sau reset");
        assertTrue(!NeoForgeCombatHandler.isRegistered(), "CombatHandler khong duoc dang ky sau reset");

        // Khoi tao qua constructor
        MineQualityNeoForge mod = new MineQualityNeoForge();
        assertTrue(MineQualityNeoForge.isInitialized(), "Mod phai o trang thai da khoi tao");
        assertTrue(NeoForgeTooltipHandler.isRegistered(), "TooltipHandler phai duoc dang ky");
        assertTrue(NeoForgeAnvilHandler.isRegistered(), "AnvilHandler phai duoc dang ky");
        assertTrue(NeoForgeCombatHandler.isRegistered(), "CombatHandler phai duoc dang ky");

        // Goi init lan nua phai an toan
        MineQualityNeoForge.init();
        assertTrue(MineQualityNeoForge.isInitialized(), "Mod van phai o trang thai da khoi tao");

        System.out.println("-> Kiem thu vong doi va dang ky NeoForge thanh cong!");
    }

    /**
     * Kiem thu hook Tooltip tren NeoForge.
     */
    private static void testNeoForgeTooltipHandler() {
        System.out.println("[Kiem thu 2] Hook Tooltip tren NeoForge...");

        // 1. Tooltip Ao Giap Kim Cuong Huyen Thoai
        ItemQualityData legendChestData = new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true);
        List<String> chestTooltip = new ArrayList<>();
        chestTooltip.add("Diamond Chestplate");
        List<String> resultChest = NeoForgeTooltipHandler.appendTooltip("minecraft:diamond_chestplate", legendChestData, chestTooltip);

        assertContains(resultChest, "Quality: Legendary");
        assertContains(resultChest, "Item Affinity: 100/100 (Kindred Spirits - Max Luck)");
        assertContains(resultChest, "When on Body:");
        assertContains(resultChest, "  +6 Max Health (+3 Hearts)");
        assertContains(resultChest, "  +2.0 Armor");
        assertContains(resultChest, "  +2.0 Armor Toughness");
        assertContains(resultChest, "Set Bonus: Permanent Fire Resistance");
        assertContains(resultChest, "Unbreakable & Fireproof");

        // 2. Tooltip Kiem Sat Junk
        ItemQualityData junkSwordData = new ItemQualityData(QualityTier.JUNK, 0, 1, false, false);
        List<String> swordTooltip = NeoForgeTooltipHandler.appendTooltip("minecraft:iron_sword", junkSwordData, new ArrayList<>());
        assertContains(swordTooltip, "Quality: Junk");
        assertContains(swordTooltip, "When in Main Hand:");
        assertContains(swordTooltip, "  -15% Attack Damage");
        assertContains(swordTooltip, "Debuff: Weakness I while held");

        // 3. Kiem thu an toan Null va danh sach rong
        List<String> nullTooltip = NeoForgeTooltipHandler.appendTooltip(null, null, null);
        assertTrue(nullTooltip != null && nullTooltip.isEmpty(), "Null input phai tra ve list rong");

        List<String> ineligibleTooltip = NeoForgeTooltipHandler.appendTooltip("minecraft:apple", legendChestData, new ArrayList<>());
        assertTrue(ineligibleTooltip.isEmpty(), "Vat pham khong phai trang bi khong duoc them tooltip");

        System.out.println("-> Kiem thu Tooltip tren NeoForge thanh cong!");
    }

    /**
     * Kiem thu logic Anvil Reforge tren NeoForge.
     */
    private static void testNeoForgeAnvilHandler() {
        System.out.println("[Kiem thu 3] Hook Anvil Reforge tren NeoForge...");

        // 1. Kiem thu tinh phi EXP va chan tran 30 cap
        int cost0 = NeoForgeAnvilHandler.getExpCost("minecraft:diamond_sword", 0);
        assertTrue(cost0 == 3, "Chi phi goc cua do diamond phai la 3 cap (thuc te: " + cost0 + ")");

        int cost5 = NeoForgeAnvilHandler.getExpCost("minecraft:diamond_sword", 5);
        assertTrue(cost5 == 13, "Chi phi 5 lan tach phai la 3 + 10 = 13 cap (thuc te: " + cost5 + ")");

        int cost20 = NeoForgeAnvilHandler.getExpCost("minecraft:diamond_sword", 20);
        assertTrue(cost20 == 30, "Chi phi 20 lan tach phai bi chan tran o 30 cap (thuc te: " + cost20 + ")");

        // 2. Kiem thu canPerformReforge
        ItemQualityData rareData = new ItemQualityData(QualityTier.RARE, 50, 0, false, false);
        boolean canReforgeValid = NeoForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", rareData, "diamond", 1, 30);
        assertTrue(canReforgeValid, "Giao dich ren hop le phai duoc chap thuan");

        boolean canReforgeWrongMat = NeoForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", rareData, "iron_ingot", 1, 30);
        assertTrue(!canReforgeWrongMat, "Nguyen lieu khong khop phai bi tu choi");

        boolean canReforgeLowExp = NeoForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", rareData, "diamond", 1, 1);
        assertTrue(!canReforgeLowExp, "Khong du EXP phai bi tu choi");

        ItemQualityData legendData = new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true);
        boolean canReforgeLegend = NeoForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", legendData, "diamond", 1, 30);
        assertTrue(!canReforgeLegend, "Trang bi Legendary khong duoc phep ren");

        // 3. Kiem thu processReforge vung an toan (Junk/Common khong bao gio vo)
        ItemQualityData junkData = new ItemQualityData(QualityTier.JUNK, 10, 0, false, false);
        for (int i = 0; i < 50; i++) {
            NeoForgeAnvilHandler.AnvilReforgeOutput out = NeoForgeAnvilHandler.processReforge(
                    "minecraft:iron_sword", junkData, "iron_ingot", 1, 30, false);
            assertTrue(out.canReforge(), "Ren Junk phai thanh cong");
            assertTrue(!out.broken(), "Junk trong Safe Zone khong bao gio bi vo");
            assertTrue(out.resultingData() != null, "Phai co ket qua pham chat");
        }

        // 4. Kiem thu co che bao hiem Ngoc Luc Bao trong Danger Zone
        ItemQualityData epicData = new ItemQualityData(QualityTier.EPIC, 0, 0, false, false);
        boolean emeraldTriggeredOnce = false;
        for (int i = 0; i < 100; i++) {
            NeoForgeAnvilHandler.AnvilReforgeOutput out = NeoForgeAnvilHandler.processReforge(
                    "minecraft:diamond_chestplate", epicData, "diamond", 1, 30, true);
            assertTrue(!out.broken(), "Khi co vien ngoc luc bao bao hiem, trang bi tuyet doi khong bao gio bi vo!");
            if (out.emeraldConsumed()) {
                emeraldTriggeredOnce = true;
            }
        }
        assertTrue(emeraldTriggeredOnce, "Vien ngoc luc bao phai duoc kich hoat nat vun thay cho do trong 100 luot test");

        // 5. An toan Null
        NeoForgeAnvilHandler.AnvilReforgeOutput nullOutput = NeoForgeAnvilHandler.processReforge(
                null, null, null, 0, 0, false);
        assertTrue(!nullOutput.canReforge(), "Null parameters phai bi tu choi an toan");

        System.out.println("-> Kiem thu Anvil Reforge tren NeoForge thanh cong!");
    }

    /**
     * Kiem thu cac hieu ung chien dau, dao boi va Full Set Bonus tren NeoForge.
     */
    private static void testNeoForgeCombatHandler() {
        System.out.println("[Kiem thu 4] Hook Combat va Full Set Bonus tren NeoForge...");

        // 1. Kiem thu Lifesteal 20% can chien Huyen Thoai
        ItemQualityData legendSword = new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true);
        NeoForgeCombatHandler.LifestealResult ls1 = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:netherite_sword", legendSword, 10.0, 30.0, 40.0);
        assertTrue(ls1.applied(), "Vũ khí cận chiến Huyền Thoại phải kích hoạt Lifesteal");
        assertTrue(Math.abs(ls1.lifestealAmount() - 2.0) < 0.001, "Lifesteal 20% của 10 dmg phải là 2.0 HP");
        assertTrue(Math.abs(ls1.newHealth() - 32.0) < 0.001, "Máu mới phải là 32.0 HP (30 + 2)");

        // Chặn trần máu tối đa khi lifesteal
        NeoForgeCombatHandler.LifestealResult lsCap = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:netherite_sword", legendSword, 10.0, 39.0, 40.0);
        assertTrue(Math.abs(lsCap.newHealth() - 40.0) < 0.001, "Máu sau khi hồi không được vượt quá max health 40.0");

        // Không lifesteal với vũ khí tầm xa hoặc không phải Huyền Thoại
        NeoForgeCombatHandler.LifestealResult lsBow = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:bow", legendSword, 10.0, 30.0, 40.0);
        assertTrue(!lsBow.applied(), "Vũ khí tầm xa không được kích hoạt cận chiến Lifesteal");

        ItemQualityData rareSword = new ItemQualityData(QualityTier.RARE, 50, 0, false, false);
        NeoForgeCombatHandler.LifestealResult lsRare = NeoForgeCombatHandler.handleMeleeAttack(
                "minecraft:diamond_sword", rareSword, 10.0, 30.0, 40.0);
        assertTrue(!lsRare.applied(), "Vũ khí không phải Huyền Thoại không có Lifesteal");

        // 2. Kiem thu hieu ung cam tren tay (Held item)
        ItemQualityData legendPick = new ItemQualityData(QualityTier.LEGENDARY, 50, 0, true, true);
        NeoForgeCombatHandler.HeldItemEffect pickEffect = NeoForgeCombatHandler.handleHeldItem("minecraft:diamond_pickaxe", legendPick);
        assertTrue(pickEffect.haste2(), "Cuốc Huyền Thoại phải có Haste II");
        assertTrue(!pickEffect.miningFatigue1(), "Không được dính Mining Fatigue");

        ItemQualityData junkPick = new ItemQualityData(QualityTier.JUNK, 0, 0, false, false);
        NeoForgeCombatHandler.HeldItemEffect junkPickEffect = NeoForgeCombatHandler.handleHeldItem("minecraft:iron_pickaxe", junkPick);
        assertTrue(junkPickEffect.miningFatigue1(), "Cuốc Junk phải dính Mining Fatigue I");

        ItemQualityData junkMelee = new ItemQualityData(QualityTier.JUNK, 0, 0, false, false);
        NeoForgeCombatHandler.HeldItemEffect junkMeleeEffect = NeoForgeCombatHandler.handleHeldItem("minecraft:iron_sword", junkMelee);
        assertTrue(junkMeleeEffect.weakness1(), "Kiếm Junk phải dính Weakness I");

        // 3. Kiem thu Full Set Bonus 4 mon giap Huyen Thoai (Máu cứng chuẩn 40 HP)
        List<ItemQualityData> fullArmor = List.of(
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true), // Mũ
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true), // Áo
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true), // Quần
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true)  // Giày
        );

        // Trường hợp máu thấp < 30% (< 12 HP trên 40 HP max)
        NeoForgeCombatHandler.FullSetBonusResult lowHp = NeoForgeCombatHandler.handleArmorSetTick(
                fullArmor, 10.0, 40.0, 2000L, 0L);
        assertTrue(lowHp.fullSetLegendary(), "Phải nhận diện đủ bộ 4 món Huyền Thoại");
        assertTrue(lowHp.permanentFireResistance(), "Phải có kháng lửa vĩnh viễn");
        assertTrue(lowHp.emergencyRecoveryTriggered(), "Máu 10/40 (<30%) phải kích hoạt cứu sinh khẩn cấp");
        assertTrue(lowHp.emergencyRecoveryDurationTicks() > 0, "Thời gian hiệu ứng phải lớn hơn 0");

        // Cooldown 60s (1200 ticks): Ở tick 2100 (mới qua 100 ticks), không được kích hoạt lại
        NeoForgeCombatHandler.FullSetBonusResult cdActive = NeoForgeCombatHandler.handleArmorSetTick(
                fullArmor, 8.0, 40.0, 2100L, 2000L);
        assertTrue(!cdActive.emergencyRecoveryTriggered(), "Không được kích hoạt khi đang trong thời gian hồi chiêu 60s");

        // Sau 1201 ticks (tick 3250 vs last 2000): kích hoạt lại được
        NeoForgeCombatHandler.FullSetBonusResult cdElapsed = NeoForgeCombatHandler.handleArmorSetTick(
                fullArmor, 8.0, 40.0, 3250L, 2000L);
        assertTrue(cdElapsed.emergencyRecoveryTriggered(), "Đã hết hồi chiêu 60s phải kích hoạt lại được");

        // Khi máu >= 30% (ví dụ 20 / 40 HP): Không kích hoạt cứu sinh nhưng vẫn có kháng lửa
        NeoForgeCombatHandler.FullSetBonusResult healthy = NeoForgeCombatHandler.handleArmorSetTick(
                fullArmor, 20.0, 40.0, 5000L, 0L);
        assertTrue(!healthy.emergencyRecoveryTriggered(), "Máu >= 30% không được kích hoạt cứu sinh");
        assertTrue(healthy.permanentFireResistance(), "Vẫn phải duy trì kháng lửa vĩnh viễn");

        // Thiếu 1 món giáp (chỉ có 3 món)
        List<ItemQualityData> incompleteArmor = List.of(
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true),
                new ItemQualityData(QualityTier.LEGENDARY, 100, 0, true, true)
        );
        NeoForgeCombatHandler.FullSetBonusResult incomplete = NeoForgeCombatHandler.handleArmorSetTick(
                incompleteArmor, 10.0, 40.0, 1000L, 0L);
        assertTrue(!incomplete.fullSetLegendary(), "Thiếu món giáp không được kích hoạt Full Set Bonus");

        // 4. An toan Null
        NeoForgeCombatHandler.LifestealResult nullLs = NeoForgeCombatHandler.handleMeleeAttack(null, null, 0, 0, 0);
        assertTrue(!nullLs.applied(), "Null melee attack phai tra ve NONE an toan");

        NeoForgeCombatHandler.HeldItemEffect nullHeld = NeoForgeCombatHandler.handleHeldItem(null, null);
        assertTrue(!nullHeld.haste2(), "Null held item phai tra ve NONE an toan");

        NeoForgeCombatHandler.FullSetBonusResult nullArmor = NeoForgeCombatHandler.handleArmorSetTick(null, 0, 0, 0, 0);
        assertTrue(!nullArmor.fullSetLegendary(), "Null armor tick phai tra ve INACTIVE an toan");

        System.out.println("-> Kiem thu Combat va Full Set Bonus tren NeoForge thanh cong!");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Kiem thu that bai: " + message);
        }
    }

    private static void assertContains(List<String> list, String expected) {
        if (list == null || !list.contains(expected)) {
            throw new AssertionError("Khong tim thay chuoi mong doi trong Tooltip: '" + expected + "'. Danh sach thuc te: " + list);
        }
    }
}
