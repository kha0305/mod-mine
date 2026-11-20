package com.minequality.quality;

/**
 * Kiểm thử tính đúng đắn của bộ xử lý hiệu ứng đặc biệt SpecialEffectHandler.
 */
public class SpecialEffectHandlerTest {

    public static void main(String[] args) {
        System.out.println("Bat dau kiem thu SpecialEffectHandler...");

        // 1. Kiem thu 20% Lifesteal cho vu khi can chien Huyen Thoai
        double damage = 15.0;
        double lifestealLeg = SpecialEffectHandler.calculateLifesteal(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.MELEE_WEAPON, damage);
        if (Math.abs(lifestealLeg - 3.0) > 0.0001) { // 15 * 0.20 = 3.0
            throw new IllegalStateException("Loi: Lifesteal phai bang 3.0 HP (20%), thuc te: " + lifestealLeg);
        }
        double lifestealEpic = SpecialEffectHandler.calculateLifesteal(QualityTier.EPIC, QualityModifier.EquipmentCategory.MELEE_WEAPON, damage);
        if (lifestealEpic != 0.0) {
            throw new IllegalStateException("Loi: Vu khi khong phai Legendary khong duoc huong Lifesteal!");
        }
        double lifestealTool = SpecialEffectHandler.calculateLifesteal(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.MINING_TOOL, damage);
        if (lifestealTool != 0.0) {
            throw new IllegalStateException("Loi: Cong cu dao khong phai vu khi can chien, khong duoc Lifesteal!");
        }
        System.out.println("Kiem tra 20% Lifesteal cho can chien Huyen thoai dat 100%!");

        // 2. Kiem thu Haste II cho Mining Tool Legendary
        if (!SpecialEffectHandler.shouldApplyHaste(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.MINING_TOOL)) {
            throw new IllegalStateException("Loi: Mining tool Legendary phai duoc huong Haste II!");
        }
        if (SpecialEffectHandler.shouldApplyHaste(QualityTier.EPIC, QualityModifier.EquipmentCategory.MINING_TOOL)
                || SpecialEffectHandler.shouldApplyHaste(QualityTier.LEGENDARY, QualityModifier.EquipmentCategory.MELEE_WEAPON)) {
            throw new IllegalStateException("Loi: Haste II chi ap dung cho Mining Tool Legendary!");
        }
        System.out.println("Kiem tra Haste II cho Mining Tool Legendary dat 100%!");

        // 3. Kiem thu Cuu sinh khan cap (Emergency Recovery)
        // Mau < 30% (< 12 HP tren 40 HP max), het 60s cooldown (1200 ticks)
        long currentTick = 2000L;
        long lastTriggerTick = 500L; // Cach 1500 ticks (> 1200 ticks)
        boolean canTrigger = SpecialEffectHandler.canTriggerEmergencyRecovery(true, 10.0, 40.0, currentTick, lastTriggerTick);
        if (!canTrigger) {
            throw new IllegalStateException("Loi: Du dieu kien mau < 30% va het cooldown nhung khong kich hoat duoc Emergency Recovery!");
        }

        // Kiem tra khong du set
        if (SpecialEffectHandler.canTriggerEmergencyRecovery(false, 10.0, 40.0, currentTick, lastTriggerTick)) {
            throw new IllegalStateException("Loi: Khong du bo giap Legendary khong duoc kich hoat!");
        }
        // Kiem tra mau >= 30%
        if (SpecialEffectHandler.canTriggerEmergencyRecovery(true, 15.0, 40.0, currentTick, lastTriggerTick)) {
            throw new IllegalStateException("Loi: Mau tren 30% khong duoc kich hoat!");
        }
        // Kiem tra chua het cooldown
        if (SpecialEffectHandler.canTriggerEmergencyRecovery(true, 10.0, 40.0, currentTick, 1500L)) { // Cach 500 ticks (< 1200)
            throw new IllegalStateException("Loi: Chua het thoi gian hoi chieu khong duoc kich hoat!");
        }

        // Kiem tra thoi gian duy tri (10s = 200 ticks co ban, 15s = 300 ticks tai 100 affinity)
        int normalDuration = SpecialEffectHandler.calculateEmergencyRecoveryDurationTicks(50);
        int maxAffinityDuration = SpecialEffectHandler.calculateEmergencyRecoveryDurationTicks(100);
        if (normalDuration != 200 || maxAffinityDuration != 300) {
            throw new IllegalStateException("Loi: Thoi gian Emergency Recovery khong dung: normal=" + normalDuration + ", max=" + maxAffinityDuration);
        }
        System.out.println("Kiem tra Emergency Recovery va thoi gian hoi chieu dat 100%!");

        // 4. Kiem thu cac hieu ung dac biet cua Chuy (Mace)
        if (SpecialEffectHandler.getMaceShockwaveRadiusMultiplier(QualityTier.RARE) != 1.15
                || SpecialEffectHandler.getMaceShockwaveRadiusMultiplier(QualityTier.EPIC) != 1.30
                || SpecialEffectHandler.getMaceSmashDamageMultiplier(QualityTier.LEGENDARY) != 1.50
                || !SpecialEffectHandler.isMaceImmuneToFallDamage(QualityTier.LEGENDARY)
                || SpecialEffectHandler.isMaceImmuneToFallDamage(QualityTier.EPIC)) {
            throw new IllegalStateException("Loi: Hieu ung dac biet cua Mace khong chinh xac!");
        }
        System.out.println("Kiem tra dac quyen Chuy (Mace) dat 100%!");

        // 5. Kiem thu cac hieu ung dac biet cua Dinh Ba (Trident)
        if (SpecialEffectHandler.getTridentThrowVelocityMultiplier(QualityTier.RARE) != 1.25
                || SpecialEffectHandler.getTridentThrowVelocityMultiplier(QualityTier.EPIC) != 1.50
                || SpecialEffectHandler.getTridentLoyaltyReturnSpeedMultiplier(QualityTier.EPIC) != 2.0
                || !SpecialEffectHandler.hasTridentBuiltInLoyalty(QualityTier.LEGENDARY)
                || SpecialEffectHandler.getTridentRiptideDistanceMultiplier(QualityTier.LEGENDARY, true) != 1.25
                || SpecialEffectHandler.getTridentRiptideDistanceMultiplier(QualityTier.LEGENDARY, false) != 1.0
                || SpecialEffectHandler.getTridentMeleeDamageMultiplier(QualityTier.LEGENDARY, true) != 1.15) {
            throw new IllegalStateException("Loi: Hieu ung dac biet cua Dinh Ba (Trident) khong chinh xac!");
        }
        System.out.println("Kiem tra dac quyen Dinh Ba (Trident) dat 100%!");

        // 6. Kiem thu cac bat loi cua Phế phẩm (Junk debuffs)
        if (!SpecialEffectHandler.shouldApplyWeakness(QualityTier.JUNK, QualityModifier.EquipmentCategory.MELEE_WEAPON)
                || !SpecialEffectHandler.shouldApplyMiningFatigue(QualityTier.JUNK, QualityModifier.EquipmentCategory.MINING_TOOL)
                || SpecialEffectHandler.shouldApplyWeakness(QualityTier.COMMON, QualityModifier.EquipmentCategory.MELEE_WEAPON)) {
            throw new IllegalStateException("Loi: Bat loi cua Junk khong hoat dong chinh xac!");
        }
        System.out.println("Kiem tra bat loi cua Phe pham (Junk) dat 100%!");

        System.out.println("Kiem thu SpecialEffectHandler thanh cong dat 100%!");
    }
}