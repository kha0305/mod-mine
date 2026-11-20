package com.minequality.fabric.event;

import com.minequality.quality.ItemAffinity;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityModifier;
import com.minequality.quality.QualityTier;
import com.minequality.quality.SpecialEffectHandler;

import java.util.List;

/**
 * Hook xu ly cac su kien chien dau, dao khoang va hieu ung giap bao ho tren Fabric Loader.
 * Dam bao an toan tuyet doi truoc ngoai le null va tich hop chat che voi cac quy tac can bang game.
 */
public final class FabricCombatHandler {

    private static boolean registered = false;

    private FabricCombatHandler() {
    }

    /**
     * Dang ky hook chien dau vao he thong Fabric.
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
    }

    public static boolean isRegistered() {
        return registered;
    }

    public static synchronized void resetRegistration() {
        registered = false;
    }

    // =========================================================================
    // API CHUAN CHO FABRIC INTEGRATION TEST VA HE THONG
    // =========================================================================

    /**
     * Ban ghi ket qua hut mau (Lifesteal) khi danh can chien.
     */
    public record LifestealResult(
            boolean applied,
            double lifestealAmount,
            double newHealth
    ) {
        public static LifestealResult none(double currentHealth) {
            return new LifestealResult(false, 0.0, currentHealth);
        }
    }

    /**
     * Xu ly don danh can chien tinh toan hut mau 20% cho vu khi Legendary.
     *
     * @param itemId        Dinh danh vu khi.
     * @param qualityData   Du lieu pham chat cua vu khi.
     * @param damageDealt   Sat thuong gay ra.
     * @param currentHealth Mau hien tai cua nguoi choi.
     * @param maxHealth     Mau toi da cua nguoi choi.
     * @return Ket qua hut mau va chi so mau moi (gioi han boi maxHealth).
     */
    public static LifestealResult handleMeleeAttack(
            String itemId,
            ItemQualityData qualityData,
            double damageDealt,
            double currentHealth,
            double maxHealth
    ) {
        if (itemId == null || qualityData == null || damageDealt <= 0.0) {
            return LifestealResult.none(currentHealth);
        }
        if (!QualityItemFilter.isEligible(itemId)) {
            return LifestealResult.none(currentHealth);
        }

        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(itemId);
        QualityTier tier = qualityData.tier();

        double lifesteal = SpecialEffectHandler.calculateLifesteal(tier, category, damageDealt);
        if (lifesteal > 0.0) {
            double newHealth = Math.min(maxHealth, currentHealth + lifesteal);
            return new LifestealResult(true, lifesteal, newHealth);
        }
        return LifestealResult.none(currentHealth);
    }

    /**
     * Ban ghi hieu ung khi cam vat pham tren tay.
     */
    public record HeldItemEffect(
            boolean haste2,
            boolean miningFatigue1,
            boolean weakness1
    ) {
        public static final HeldItemEffect NONE = new HeldItemEffect(false, false, false);
    }

    /**
     * Xu ly hieu ung vat pham khi duoc cam tren tay nguoi choi.
     *
     * @param itemId      Dinh danh vat pham.
     * @param qualityData Du lieu pham chat.
     * @return Hieu ung Haste II (Legendary tool), Fatigue I (Junk tool), Weakness I (Junk weapon).
     */
    public static HeldItemEffect handleHeldItem(String itemId, ItemQualityData qualityData) {
        if (itemId == null || qualityData == null || !QualityItemFilter.isEligible(itemId)) {
            return HeldItemEffect.NONE;
        }

        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(itemId);
        QualityTier tier = qualityData.tier();

        boolean haste = SpecialEffectHandler.shouldApplyHaste(tier, category);
        boolean fatigue = SpecialEffectHandler.shouldApplyMiningFatigue(tier, category);
        boolean weakness = SpecialEffectHandler.shouldApplyWeakness(tier, category);

        return new HeldItemEffect(haste, fatigue, weakness);
    }

    /**
     * Ban ghi ket qua Full Set Bonus cua bo giap bao ho.
     */
    public record FullSetBonusResult(
            boolean fullSetLegendary,
            boolean permanentFireResistance,
            boolean emergencyRecoveryTriggered,
            int emergencyRecoveryDurationTicks,
            long newLastTriggerTick
    ) {
        public static FullSetBonusResult none(long lastTriggerTick) {
            return new FullSetBonusResult(false, false, false, 0, lastTriggerTick);
        }
    }

    /**
     * Xu ly tick dinh ky cua bo giap nguoi choi de xet duyet Full Set Bonus va Cuu sinh khan cap.
     *
     * @param armorPieces     Danh sach 4 manh giap.
     * @param currentHealth   Mau hien tai.
     * @param maxHealth       Mau toi da.
     * @param currentTick     Tick hien tai cua the gioi.
     * @param lastTriggerTick Tick kich hoat gan nhat truoc do.
     * @return Ket qua Full Set va trang thai kich hoat cuu sinh.
     */
    public static FullSetBonusResult handleArmorSetTick(
            List<ItemQualityData> armorPieces,
            double currentHealth,
            double maxHealth,
            long currentTick,
            long lastTriggerTick
    ) {
        if (armorPieces == null || armorPieces.size() != 4) {
            return FullSetBonusResult.none(lastTriggerTick);
        }
        for (ItemQualityData piece : armorPieces) {
            if (piece == null || piece.tier() != QualityTier.LEGENDARY) {
                return FullSetBonusResult.none(lastTriggerTick);
            }
        }

        boolean emergencyTriggered = SpecialEffectHandler.canTriggerEmergencyRecovery(
                true,
                currentHealth,
                maxHealth,
                currentTick,
                lastTriggerTick
        );

        if (emergencyTriggered) {
            int totalAffinity = 0;
            for (ItemQualityData piece : armorPieces) {
                totalAffinity += piece.affinity();
            }
            int avgAffinity = totalAffinity / 4;
            int duration = SpecialEffectHandler.calculateEmergencyRecoveryDurationTicks(avgAffinity);
            return new FullSetBonusResult(true, true, true, duration, currentTick);
        }

        return new FullSetBonusResult(true, true, false, 0, lastTriggerTick);
    }

    // =========================================================================
    // CAC TIEN ICH PHAN TICH CHI TIET BO SUNG CHO FABRIC QA CHECK TEST
    // =========================================================================

    public record AttackOutcome(
            double damageDealt,
            double lifestealAmount,
            boolean lifestealTriggered,
            boolean applyWeakness
    ) {
        public static AttackOutcome empty(double damage) {
            return new AttackOutcome(Math.max(0.0, damage), 0.0, false, false);
        }
    }

    public static AttackOutcome onPlayerAttack(String itemId, ItemQualityData qualityData, double damageDealt) {
        if (damageDealt <= 0.0 || itemId == null || qualityData == null || !QualityItemFilter.isEligible(itemId)) {
            return AttackOutcome.empty(damageDealt);
        }

        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(itemId);
        QualityTier tier = qualityData.tier();

        double lifesteal = SpecialEffectHandler.calculateLifesteal(tier, category, damageDealt);
        boolean lifestealTriggered = (lifesteal > 0.0);
        boolean applyWeakness = SpecialEffectHandler.shouldApplyWeakness(tier, category);

        return new AttackOutcome(damageDealt, lifesteal, lifestealTriggered, applyWeakness);
    }

    public record MiningToolEffect(
            boolean applyHaste,
            boolean applyFatigue
    ) {
        public static final MiningToolEffect NONE = new MiningToolEffect(false, false);
    }

    public static MiningToolEffect onHoldMiningTool(String itemId, ItemQualityData qualityData) {
        HeldItemEffect eff = handleHeldItem(itemId, qualityData);
        return new MiningToolEffect(eff.haste2(), eff.miningFatigue1());
    }

    public record ArmorSetOutcome(
            boolean isFullLegendarySet,
            boolean hasPermanentFireResistance,
            boolean emergencyRecoveryTriggered,
            int durationTicks,
            double totalMaxHealthBonus
    ) {
        public static final ArmorSetOutcome DEFAULT = new ArmorSetOutcome(false, false, false, 0, 0.0);
    }

    public static double calculateTotalArmorHealthBonus(List<ItemQualityData> armorDataList) {
        if (armorDataList == null || armorDataList.size() != 4) {
            return 0.0;
        }
        QualityModifier.EquipmentCategory[] slots = {
                QualityModifier.EquipmentCategory.ARMOR_HELMET,
                QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE,
                QualityModifier.EquipmentCategory.ARMOR_LEGGINGS,
                QualityModifier.EquipmentCategory.ARMOR_BOOTS
        };

        double totalHealth = 0.0;
        for (int i = 0; i < 4; i++) {
            ItemQualityData piece = armorDataList.get(i);
            if (piece != null) {
                QualityModifier.ModifierStats stats = QualityModifier.getStats(piece.tier(), slots[i]);
                totalHealth += stats.healthBonus();
            }
        }
        return totalHealth;
    }

    public static boolean isFullLegendaryArmor(List<ItemQualityData> armorDataList) {
        if (armorDataList == null || armorDataList.size() != 4) {
            return false;
        }
        for (ItemQualityData piece : armorDataList) {
            if (piece == null || piece.tier() != QualityTier.LEGENDARY) {
                return false;
            }
        }
        return true;
    }

    public static ArmorSetOutcome checkArmorSetEffects(
            List<ItemQualityData> armorDataList,
            double currentHealth,
            double maxHealth,
            long currentTick,
            long lastTriggerTick
    ) {
        double healthBonus = calculateTotalArmorHealthBonus(armorDataList);
        FullSetBonusResult result = handleArmorSetTick(armorDataList, currentHealth, maxHealth, currentTick, lastTriggerTick);

        return new ArmorSetOutcome(
                result.fullSetLegendary(),
                result.permanentFireResistance(),
                result.emergencyRecoveryTriggered(),
                result.emergencyRecoveryDurationTicks(),
                healthBonus
        );
    }

    public record MaceSmashOutcome(
            double damageMultiplier,
            double shockwaveRadiusMultiplier,
            boolean immuneToFallDamage
    ) {
        public static final MaceSmashOutcome DEFAULT = new MaceSmashOutcome(1.0, 1.0, false);
    }

    public static MaceSmashOutcome onMaceSmash(ItemQualityData qualityData) {
        if (qualityData == null) {
            return MaceSmashOutcome.DEFAULT;
        }
        QualityTier tier = qualityData.tier();
        double dmgMult = SpecialEffectHandler.getMaceSmashDamageMultiplier(tier);
        double shockMult = SpecialEffectHandler.getMaceShockwaveRadiusMultiplier(tier);
        boolean noFallDmg = SpecialEffectHandler.isMaceImmuneToFallDamage(tier);

        return new MaceSmashOutcome(dmgMult, shockMult, noFallDmg);
    }

    public record TridentOutcome(
            double throwVelocityMultiplier,
            double loyaltyReturnSpeedMultiplier,
            boolean hasBuiltInLoyalty,
            double riptideDistanceMultiplier,
            double waterMeleeDamageMultiplier
    ) {
        public static final TridentOutcome DEFAULT = new TridentOutcome(1.0, 1.0, false, 1.0, 1.0);
    }

    public static TridentOutcome onTridentAction(ItemQualityData qualityData, boolean inWaterOrRain) {
        if (qualityData == null) {
            return TridentOutcome.DEFAULT;
        }
        QualityTier tier = qualityData.tier();
        double velocity = SpecialEffectHandler.getTridentThrowVelocityMultiplier(tier);
        double loyaltySpeed = SpecialEffectHandler.getTridentLoyaltyReturnSpeedMultiplier(tier);
        boolean builtInLoyalty = SpecialEffectHandler.hasTridentBuiltInLoyalty(tier);
        double riptideDist = SpecialEffectHandler.getTridentRiptideDistanceMultiplier(tier, inWaterOrRain);
        double meleeDmg = SpecialEffectHandler.getTridentMeleeDamageMultiplier(tier, inWaterOrRain);

        return new TridentOutcome(velocity, loyaltySpeed, builtInLoyalty, riptideDist, meleeDmg);
    }

    public static boolean onShieldBlock(ItemQualityData qualityData) {
        if (qualityData == null) {
            return false;
        }
        return SpecialEffectHandler.shouldTriggerShieldResistance(qualityData.tier());
    }
}
