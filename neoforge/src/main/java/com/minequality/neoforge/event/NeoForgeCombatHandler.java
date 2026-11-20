package com.minequality.neoforge.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityModifier;
import com.minequality.quality.QualityTier;
import com.minequality.quality.SpecialEffectHandler;

import java.util.List;

/**
 * Hook xu ly cac dac quyen chien dau, hieu ung dao boi va Full Set Bonus cho NeoForge Loader.
 * Bao gom:
 * - 20% Hut mau (Lifesteal) khi dung vu khi can chien Huyen Thoai.
 * - Hieu ung Dao nhanh II (Haste II) lien tuc khi cam cong cu khai thac Huyen Thoai tren tay chinh.
 * - Hieu ung giam toc dao (Mining Fatigue I) hoac suy yeu (Weakness I) khi cam do Junk.
 * - Hieu ung Full Set Bonus giap Huyen Thoai: Khang lua vinh vien va Cuu sinh khan cap (&lt; 30% HP: Khang cu I + Hoi phuc I).
 */
public final class NeoForgeCombatHandler {

    private static boolean registered = false;

    private NeoForgeCombatHandler() {
    }

    /**
     * Dang ky hook chien dau va hieu ung vao he thong Event Bus cua NeoForge.
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

    /**
     * Ket qua xu ly hut mau (Lifesteal) khi can chien tren NeoForge.
     */
    public record LifestealResult(
            boolean applied,
            double lifestealAmount,
            double newHealth
    ) {
        public static final LifestealResult NONE = new LifestealResult(false, 0.0, 0.0);
    }

    /**
     * Xu ly 20% Hut mau (Lifesteal) khi nguoi choi su dung vu khi can chien Huyen Thoai tan cong muc tieu.
     *
     * @param weaponId              Dinh danh vu khi dang cam tren tay.
     * @param weaponData            Du lieu pham chat cua vu khi.
     * @param damageDealt           Luong sat thuong thuc te da gay ra cho muc tieu.
     * @param currentAttackerHealth Luong mau hien tai cua nguoi tan cong.
     * @param maxAttackerHealth     Luong mau toi da cua nguoi tan cong.
     * @return Ket qua hut mau va luong mau moi sau khi hoi phuc.
     */
    public static LifestealResult handleMeleeAttack(
            String weaponId,
            ItemQualityData weaponData,
            double damageDealt,
            double currentAttackerHealth,
            double maxAttackerHealth
    ) {
        if (weaponId == null || weaponData == null || damageDealt <= 0 || maxAttackerHealth <= 0) {
            return LifestealResult.NONE;
        }

        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(weaponId);
        double lifesteal = SpecialEffectHandler.calculateLifesteal(weaponData.tier(), category, damageDealt);

        if (lifesteal <= 0) {
            return LifestealResult.NONE;
        }

        double newHealth = Math.min(maxAttackerHealth, currentAttackerHealth + lifesteal);
        return new LifestealResult(true, lifesteal, newHealth);
    }

    /**
     * Ket qua kiem tra hieu ung khi cam vat pham tren tay chinh.
     */
    public record HeldItemEffect(
            boolean haste2,
            boolean miningFatigue1,
            boolean weakness1
    ) {
        public static final HeldItemEffect NONE = new HeldItemEffect(false, false, false);
    }

    /**
     * Kiem tra cac hieu ung noi tai khi nguoi choi cam cong cu hoac vu khi tren tay chinh.
     *
     * @param heldItemId Dinh danh vat pham dang cam tren tay.
     * @param heldData   Du lieu pham chat cua vat pham.
     * @return Tap hop cac trang thai hieu ung can ap dung cho nguoi choi.
     */
    public static HeldItemEffect handleHeldItem(String heldItemId, ItemQualityData heldData) {
        if (heldItemId == null || heldData == null) {
            return HeldItemEffect.NONE;
        }

        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(heldItemId);
        QualityTier tier = heldData.tier();

        boolean haste = SpecialEffectHandler.shouldApplyHaste(tier, category);
        boolean fatigue = (tier == QualityTier.JUNK && category == QualityModifier.EquipmentCategory.MINING_TOOL);
        boolean weakness = (tier == QualityTier.JUNK && category == QualityModifier.EquipmentCategory.MELEE_WEAPON);

        return new HeldItemEffect(haste, fatigue, weakness);
    }

    /**
     * Ket qua kiem tra hieu ung Full Set Bonus cua bo 4 mon giap Huyen Thoai.
     */
    public record FullSetBonusResult(
            boolean fullSetLegendary,
            boolean permanentFireResistance,
            boolean emergencyRecoveryTriggered,
            int emergencyRecoveryDurationTicks,
            long newLastTriggerTick
    ) {
        public static final FullSetBonusResult INACTIVE = new FullSetBonusResult(false, false, false, 0, 0L);
    }

    /**
     * Xu ly tick dinh ky kiem tra bo 4 mon giap Huyen Thoai cua nguoi choi tren NeoForge.
     *
     * @param armorPieces     Danh sach du lieu pham chat cua 4 mon giap (Mu, Ao, Quan, Giay).
     * @param currentHealth   Luong mau hien tai cua nguoi choi.
     * @param maxHealth       Luong mau toi da cua nguoi choi.
     * @param currentTick     Thoi diem tick hien tai cua the gioi.
     * @param lastTriggerTick Thoi diem tick kich hoat cuu sinh gan nhat.
     * @return Ket qua xac dinh hieu ung khang lua va cuu sinh khan cap.
     */
    public static FullSetBonusResult handleArmorSetTick(
            List<ItemQualityData> armorPieces,
            double currentHealth,
            double maxHealth,
            long currentTick,
            long lastTriggerTick
    ) {
        if (armorPieces == null || armorPieces.size() != 4 || maxHealth <= 0) {
            return FullSetBonusResult.INACTIVE;
        }

        // Kiem tra tat ca 4 mon deu phai ton tai va thuoc bac Huyen Thoai
        int minAffinity = 100;
        for (ItemQualityData piece : armorPieces) {
            if (piece == null || piece.tier() != QualityTier.LEGENDARY) {
                return FullSetBonusResult.INACTIVE;
            }
            if (piece.affinity() < minAffinity) {
                minAffinity = piece.affinity();
            }
        }

        boolean canTriggerEmergency = SpecialEffectHandler.canTriggerEmergencyRecovery(
                true,
                currentHealth,
                maxHealth,
                currentTick,
                lastTriggerTick
        );

        if (canTriggerEmergency) {
            int durationTicks = SpecialEffectHandler.calculateEmergencyRecoveryDurationTicks(minAffinity);
            return new FullSetBonusResult(true, true, true, durationTicks, currentTick);
        }

        return new FullSetBonusResult(true, true, false, 0, lastTriggerTick);
    }
}
