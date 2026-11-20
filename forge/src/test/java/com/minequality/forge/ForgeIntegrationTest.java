package com.minequality.forge;

import com.minequality.forge.event.ForgeAnvilHandler;
import com.minequality.forge.event.ForgeCombatHandler;
import com.minequality.forge.event.ForgeTooltipHandler;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityTier;

import java.util.ArrayList;
import java.util.List;

/**
 * Bộ kiểm thử tích hợp độc lập cho mô-đun MinecraftForge Loader:
 * 1. Kiểm tra vòng đời khởi tạo của MineQualityForge.
 * 2. Kiểm tra ForgeTooltipHandler: an toàn null, lọc trang bị, định dạng dòng phẩm chất en_us.
 * 3. Kiểm tra ForgeAnvilHandler: giới hạn trần 30 cấp EXP, phân vùng rủi ro, bảo hiểm Ngọc Lục Bảo.
 * 4. Kiểm tra ForgeCombatHandler: 20% Hút máu, Haste II, Full Set Bonus 40 HP và kích hoạt Emergency Recovery.
 */
public class ForgeIntegrationTest {

    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM THU TICH HOP MO-DUN MINECRAFTFORGE ===");

        testLifecycle();
        testTooltipHandler();
        testAnvilHandler();
        testCombatHandler();

        System.out.println("=== TAT CA KIEM THU TICH HOP FORGE DAT 100% THANH CONG ===");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("THAT BAI: " + message);
        }
    }

    private static void testLifecycle() {
        System.out.println("[Kiem thu 1] Khoi tao va vong doi MineQualityForge...");
        MineQualityForge.resetInitialization();
        assertTrue(!MineQualityForge.isInitialized(), "Ban dau MineQualityForge phai chua duoc khoi tao.");
        assertTrue(!ForgeTooltipHandler.isRegistered(), "TooltipHandler phai chua dang ky.");
        assertTrue(!ForgeAnvilHandler.isRegistered(), "AnvilHandler phai chua dang ky.");
        assertTrue(!ForgeCombatHandler.isRegistered(), "CombatHandler phai chua dang ky.");

        MineQualityForge.init();
        assertTrue(MineQualityForge.isInitialized(), "MineQualityForge phai o trang thai da khoi tao.");
        assertTrue(ForgeTooltipHandler.isRegistered(), "TooltipHandler phai da duoc dang ky.");
        assertTrue(ForgeAnvilHandler.isRegistered(), "AnvilHandler phai da duoc dang ky.");
        assertTrue(ForgeCombatHandler.isRegistered(), "CombatHandler phai da duoc dang ky.");

        System.out.println("  -> Vong doi khoi tao Forge dat yeu cau.");
    }

    private static void testTooltipHandler() {
        System.out.println("[Kiem thu 2] Kiem tra ForgeTooltipHandler...");

        // 1. An toan null
        List<String> nullResult = ForgeTooltipHandler.appendTooltip(null, null, null);
        assertTrue(nullResult != null && nullResult.isEmpty(), "An toan null khi cac tham so deu null.");

        // 2. Vat pham khong thuoc danh sach trang bi
        List<String> dirtList = new ArrayList<>();
        dirtList.add("Dirt Block");
        ForgeTooltipHandler.appendTooltip("minecraft:dirt", ItemQualityData.of(QualityTier.LEGENDARY), dirtList);
        assertTrue(dirtList.size() == 1, "Vat pham khong hop le khong duoc bo sung dong pham chat.");

        // 3. Ao giap kim cuong Huyen Thoai (Diamond Chestplate)
        ItemQualityData legendaryData = new ItemQualityData(QualityTier.LEGENDARY, 100, 0, false, false);
        List<String> chestplateTooltip = new ArrayList<>();
        chestplateTooltip.add("Diamond Chestplate");
        ForgeTooltipHandler.appendTooltip("minecraft:diamond_chestplate", legendaryData, chestplateTooltip);

        String fullText = String.join("\n", chestplateTooltip);
        assertTrue(fullText.contains("Quality: Legendary"), "Phai chua dong Quality: Legendary.");
        assertTrue(fullText.contains("Item Affinity: 100/100 (Kindred Spirits - Max Luck)"), "Phai chua dong Affinity 100.");
        assertTrue(fullText.contains("+6 Max Health (+3 Hearts)"), "Ao giap Huyen Thoai phai cong +6 Max Health.");
        assertTrue(fullText.contains("+2.0 Armor"), "Phai co +2.0 Armor.");
        assertTrue(fullText.contains("Set Bonus: Permanent Fire Resistance"), "Phai co Set Bonus Fire Resistance.");
        assertTrue(fullText.contains("Set Bonus: Emergency Recovery (<30% HP: Res I + Regen I)"), "Phai co Set Bonus Emergency Recovery.");
        assertTrue(fullText.contains("Unbreakable & Fireproof"), "Phai co co Unbreakable & Fireproof.");
        assertTrue(fullText.contains("Reforged: 0 times (Max Quality Reached)"), "Phai co thong tin so lan ren dat moc toi da.");

        // 4. Kiem phe pham (Junk Iron Sword) co debuff Weakness I
        ItemQualityData junkData = ItemQualityData.of(QualityTier.JUNK);
        List<String> junkSwordTooltip = new ArrayList<>();
        junkSwordTooltip.add("Iron Sword");
        ForgeTooltipHandler.appendTooltip("minecraft:iron_sword", junkData, junkSwordTooltip);
        String junkText = String.join("\n", junkSwordTooltip);
        assertTrue(junkText.contains("Quality: Junk"), "Kiem phe pham phai co Quality: Junk.");
        assertTrue(junkText.contains("Debuff: Weakness I while held"), "Kiem phe pham phai chua debuff Weakness I.");

        System.out.println("  -> ForgeTooltipHandler dat 100% yeu cau.");
    }

    private static void testAnvilHandler() {
        System.out.println("[Kiem thu 3] Kiem tra ForgeAnvilHandler...");

        // 1. Kiem tra gioi han tran 30 cap EXP
        int cost0 = ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 0);
        int cost5 = ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 5);
        int cost10 = ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 10);
        int cost20 = ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 20);
        int cost50 = ForgeAnvilHandler.calculateExpCost("minecraft:diamond_sword", 50);

        assertTrue(cost0 == 3, "baseCost diamond sword la 3 (reforgeCount = 0).");
        assertTrue(cost5 == 13, "baseCost 3 + 5*2 = 13.");
        assertTrue(cost10 == 23, "baseCost 3 + 10*2 = 23.");
        assertTrue(cost20 == 30, "baseCost 3 + 20*2 = 43 -> phai bi chan tran tai 30 cap.");
        assertTrue(cost50 == 30, "reforgeCount = 50 van phai bi chan tran toi da 30 cap.");

        // 2. Kiem tra nguyen lieu ren hop le
        ItemQualityData commonSword = ItemQualityData.of(QualityTier.COMMON);
        assertTrue(ForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", commonSword, "minecraft:diamond", 1, 30),
                "Kim cuong phai la nguyen lieu ren hop le cua kiem kim cuong.");
        assertTrue(!ForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", commonSword, "minecraft:iron_ingot", 1, 30),
                "Thoi sat khong the dung de ren kiem kim cuong.");
        assertTrue(!ForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", commonSword, "minecraft:diamond", 0, 30),
                "Nguyen lieu so luong 0 phai bi tu choi.");
        assertTrue(!ForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", commonSword, "minecraft:diamond", 1, 1),
                "Khong du EXP phai bi tu choi.");

        // 3. Khong the ren trang bi Huyen Thoai
        ItemQualityData legendarySword = ItemQualityData.of(QualityTier.LEGENDARY);
        assertTrue(!ForgeAnvilHandler.canPerformReforge("minecraft:diamond_sword", legendarySword, "minecraft:diamond", 1, 30),
                "Trang bi Huyen Thoai khong the ren them.");
        ForgeAnvilHandler.AnvilReforgeResult rejectedLeg = ForgeAnvilHandler.processReforge(
                "minecraft:diamond_sword", legendarySword, "minecraft:diamond", 1, 30, false
        );
        assertTrue(!rejectedLeg.canReforge() && rejectedLeg.failureReason() != null,
                "ProcessReforge phai tu choi trang bi Huyen Thoai kem ly do.");

        // 4. Vung an toan (Safe Zone: Junk, Common, Uncommon) khong bao gio vo do
        for (int i = 0; i < 50; i++) {
            ForgeAnvilHandler.AnvilReforgeResult res = ForgeAnvilHandler.processReforge(
                    "minecraft:diamond_sword", commonSword, "minecraft:diamond", 1, 30, false
            );
            assertTrue(res.canReforge(), "Luot ren hop le phai thanh cong.");
            assertTrue(!res.broken(), "Vung an toan tuyet doi khong bao gio bi vo do.");
            assertTrue(res.expCost() == 3, "Chi phi EXP phai dung 3 cap.");
        }

        // 5. Co che Bao hiem Ngoc Luc Bao trong Vung Mao Hiem
        ItemQualityData epicSword = ItemQualityData.of(QualityTier.EPIC);
        boolean sawEmeraldSaved = false;
        for (int i = 0; i < 100; i++) {
            ForgeAnvilHandler.AnvilReforgeResult res = ForgeAnvilHandler.processReforge(
                    "minecraft:diamond_sword", epicSword, "minecraft:diamond", 1, 30, true
            );
            assertTrue(!res.broken(), "Khi co bao hiem Ngoc Luc Bao, trang bi tuyet doi khong bao gio bi vo!");
            if (res.emeraldConsumed()) {
                sawEmeraldSaved = true;
            }
        }
        assertTrue(sawEmeraldSaved, "Phai co it nhat mot luot Ngoc Luc Bao vo thay de cuu song trang bi.");

        System.out.println("  -> ForgeAnvilHandler dat 100% yeu cau.");
    }

    private static void testCombatHandler() {
        System.out.println("[Kiem thu 4] Kiem tra ForgeCombatHandler...");

        // 1. Kiem thu 20% Hut mau (Lifesteal)
        ItemQualityData legWeapon = ItemQualityData.of(QualityTier.LEGENDARY);
        double lifestealLeg = ForgeCombatHandler.calculateLifesteal(legWeapon, "minecraft:diamond_sword", 10.0);
        assertTrue(Math.abs(lifestealLeg - 2.0) < 0.0001, "20% cua 10.0 sat thuong phai la 2.0 HP hoi phuc.");

        // Khong phai can chien hoac khong phai Huyen thoai thi khong hut mau
        ItemQualityData rareWeapon = ItemQualityData.of(QualityTier.RARE);
        double lifestealRare = ForgeCombatHandler.calculateLifesteal(rareWeapon, "minecraft:diamond_sword", 10.0);
        assertTrue(lifestealRare == 0.0, "Vu khi Rare khong duoc hut mau.");

        double lifestealBow = ForgeCombatHandler.calculateLifesteal(legWeapon, "minecraft:bow", 10.0);
        assertTrue(lifestealBow == 0.0, "Cung ten khong phai can chien, khong duoc tinh lifesteal can chien.");

        // Ap dung lifesteal vao thanh mau va chan tran tai maxHealth
        ForgeCombatHandler.LifestealResult healRes1 = ForgeCombatHandler.applyLifesteal(
                legWeapon, "minecraft:diamond_sword", 10.0, 15.0, 20.0
        );
        assertTrue(Math.abs(healRes1.newHealth() - 17.0) < 0.0001, "Mau 15.0 + 2.0 hoi phuc phai bang 17.0.");

        ForgeCombatHandler.LifestealResult healRes2 = ForgeCombatHandler.applyLifesteal(
                legWeapon, "minecraft:diamond_sword", 10.0, 19.5, 20.0
        );
        assertTrue(Math.abs(healRes2.newHealth() - 20.0) < 0.0001, "Mau 19.5 + 2.0 phai bi chan tran tai 20.0.");

        // 2. Kiem thu Haste II tren cong cu dao
        ItemQualityData legTool = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData commonTool = ItemQualityData.of(QualityTier.COMMON);
        assertTrue(ForgeCombatHandler.shouldApplyHaste(legTool, "minecraft:netherite_pickaxe"),
                "Cup Netherite Huyen Thoai phai nhan Haste II.");
        assertTrue(ForgeCombatHandler.shouldApplyHaste(legTool, "minecraft:diamond_shovel"),
                "Xeng Kim Cuong Huyen Thoai phai nhan Haste II.");
        assertTrue(!ForgeCombatHandler.shouldApplyHaste(commonTool, "minecraft:netherite_pickaxe"),
                "Cup Common khong duoc nhan Haste II.");
        assertTrue(!ForgeCombatHandler.shouldApplyHaste(legWeapon, "minecraft:diamond_sword"),
                "Kiem khong phai cong cu dao, khong nhan Haste II.");

        // 3. Kiem thu debuff khi cam do Junk
        ItemQualityData junk = ItemQualityData.of(QualityTier.JUNK);
        ForgeCombatHandler.HeldItemEffects junkSwordEffects = ForgeCombatHandler.evaluateHeldItemEffects("minecraft:iron_sword", junk);
        assertTrue(junkSwordEffects.weakness(), "Kiem Junk phai gay Weakness I.");
        assertTrue(!junkSwordEffects.miningFatigue(), "Kiem Junk khong gay Mining Fatigue.");

        ForgeCombatHandler.HeldItemEffects junkPickaxeEffects = ForgeCombatHandler.evaluateHeldItemEffects("minecraft:iron_pickaxe", junk);
        assertTrue(junkPickaxeEffects.miningFatigue(), "Cup Junk phai gay Mining Fatigue I.");
        assertTrue(!junkPickaxeEffects.weakness(), "Cup Junk khong gay Weakness I.");

        // 4. Kiem thu Bo Giap Huyen Thoai chuan Mau Cung 40 HP
        ItemQualityData helmet = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData chestplate = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData leggings = ItemQualityData.of(QualityTier.LEGENDARY);
        ItemQualityData boots = ItemQualityData.of(QualityTier.LEGENDARY);

        assertTrue(ForgeCombatHandler.isFullLegendaryArmor(helmet, chestplate, leggings, boots),
                "4 mon deu la Huyen Thoai phai duoc xac nhan la Full Set.");

        double bonusHealth = ForgeCombatHandler.calculateTotalArmorBonusHealth(helmet, chestplate, leggings, boots);
        assertTrue(Math.abs(bonusHealth - 20.0) < 0.0001,
                "Bo giap Huyen Thoai phai cong dung +20 HP (Mu 4 + Ao 6 + Quan 6 + Giay 4).");

        double totalMaxHealth = ForgeCombatHandler.calculatePlayerMaxHealth(helmet, chestplate, leggings, boots);
        assertTrue(Math.abs(totalMaxHealth - 40.0) < 0.0001,
                "Tong luong mau nguoi choi phai dat chuan tron 40 HP (20 tim do - 2 hang tim).");

        assertTrue(ForgeCombatHandler.hasPermanentFireResistance(helmet, chestplate, leggings, boots),
                "Bo giap Huyen Thoai phai co dac quyen Permanent Fire Resistance.");

        // Kiem tra bo giap khuyet hoac co mon khong phai Huyen Thoai
        ItemQualityData rareBoots = ItemQualityData.of(QualityTier.RARE);
        assertTrue(!ForgeCombatHandler.isFullLegendaryArmor(helmet, chestplate, leggings, rareBoots),
                "Khi 1 mon la Rare thi khong duoc tinh la Full Legendary Set.");
        assertTrue(!ForgeCombatHandler.hasPermanentFireResistance(helmet, chestplate, leggings, rareBoots),
                "Khong du bo Huyen Thoai khong duoc kich hoat Fire Resistance vinh vien.");

        // 5. Kiem thu Kich hoat Sinh Ton Khan Cap (Emergency Recovery) khi mau < 30% (< 12 HP tren 40 HP)
        // Truong hop 1: Mau = 15 HP (15/40 = 37.5% > 30%) -> khong kich hoat
        ForgeCombatHandler.EmergencyRecoveryResult noTrigger = ForgeCombatHandler.checkEmergencyRecovery(
                true, 15.0, 40.0, 1000L, 0L, 0
        );
        assertTrue(!noTrigger.triggered(), "Mau >= 30% khong duoc kich hoat Emergency Recovery.");

        // Truong hop 2: Mau = 11.0 HP (11/40 = 27.5% < 30%), da het hoi chieu 1200 ticks -> kich hoat thanh cong
        ForgeCombatHandler.EmergencyRecoveryResult triggeredAffinity0 = ForgeCombatHandler.checkEmergencyRecovery(
                true, 11.0, 40.0, 1500L, 0L, 0
        );
        assertTrue(triggeredAffinity0.triggered(), "Mau < 30% va het hoi chieu phai kich hoat Emergency Recovery.");
        assertTrue(triggeredAffinity0.durationTicks() == 200, "Thoi gian hieu ung co ban phai la 200 ticks (10 giay).");
        assertTrue(triggeredAffinity0.nextEligibleTick() == 2700L, "Tick san sang tiep theo phai la 1500 + 1200 = 2700.");

        // Truong hop 3: Mau < 30% nhung chua het hoi chieu (dang trong 60 giay hoi) -> khong kich hoat
        ForgeCombatHandler.EmergencyRecoveryResult inCooldown = ForgeCombatHandler.checkEmergencyRecovery(
                true, 10.0, 40.0, 2000L, 1500L, 0
        );
        assertTrue(!inCooldown.triggered(), "Dang trong thoi gian hoi chieu (500 ticks < 1200 ticks) khong duoc kich hoat.");

        // Truong hop 4: Giap dat moc Affinity 100/100 -> thoi gian hieu ung keo dai them 50% (300 ticks = 15 giay)
        ForgeCombatHandler.EmergencyRecoveryResult triggeredAffinity100 = ForgeCombatHandler.checkEmergencyRecovery(
                true, 8.0, 40.0, 3000L, 1500L, 100
        );
        assertTrue(triggeredAffinity100.triggered(), "Kich hoat thanh cong sau khi het hoi chieu.");
        assertTrue(triggeredAffinity100.durationTicks() == 300,
                "Affinity 100/100 phai keo dai thoi gian them 50% (300 ticks = 15 giay).");

        System.out.println("  -> ForgeCombatHandler dat 100% yeu cau.");
    }
}
