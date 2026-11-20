package com.minequality.forge.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityModifier;
import com.minequality.quality.QualityTier;
import com.minequality.quality.SpecialEffectHandler;

/**
 * Xử lý các sự kiện chiến đấu và hiệu ứng trang bị trên MinecraftForge Loader:
 * - Kích hoạt 20% Hút máu (Lifesteal) khi dùng vũ khí cận chiến Huyền Thoại.
 * - Hiệu ứng Đào nhanh II (Haste II) liên tục khi cầm công cụ khai thác Huyền Thoại trên tay chính.
 * - Hiệu ứng Bộ Trang Bị (Full Set Bonus) cho 4 món giáp Huyền Thoại đạt mốc chuẩn 40 HP (20 HP gốc + 20 HP thưởng).
 * - Kích hoạt sinh tồn khẩn cấp (Kháng cự I + Hồi phục I trong 10-15 giây, hồi chiêu 60 giây) khi máu dưới 30% (< 12 HP).
 */
public final class ForgeCombatHandler {

    public static final double BASE_PLAYER_MAX_HEALTH = 20.0; // 10 tim đỏ gốc Vanilla
    public static final double LEGENDARY_FULL_SET_BONUS_HEALTH = 20.0; // Thưởng +20 HP từ 4 món Huyền Thoại
    public static final double LEGENDARY_FULL_SET_MAX_HEALTH = 40.0; // Mốc tròn 40 HP (20 tim đỏ - 2 hàng tim)

    private static boolean registered = false;

    private ForgeCombatHandler() {
    }

    /**
     * Đăng ký handler vào Event Bus của Forge.
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
     * Kết quả tính toán hút máu trong giao tranh.
     */
    public record LifestealResult(
            double lifestealAmount,
            double newHealth
    ) {
    }

    /**
     * Trạng thái các hiệu ứng nội tại khi cầm trang bị trên tay.
     */
    public record HeldItemEffects(
            boolean haste,
            boolean weakness,
            boolean miningFatigue
    ) {
    }

    /**
     * Kết quả kiểm tra kích hoạt hiệu ứng Cứu Sinh Khẩn Cấp của bộ giáp.
     */
    public record EmergencyRecoveryResult(
            boolean triggered,
            int durationTicks,
            long nextEligibleTick
    ) {
    }

    /**
     * Tính toán lượng máu hồi lại khi tấn công mục tiêu bằng vũ khí.
     *
     * @param weaponData   Dữ liệu phẩm chất của vũ khí.
     * @param weaponItemId Định danh vũ khí.
     * @param damageDealt  Lượng sát thương thực tế gây ra.
     * @return Lượng máu người chơi được hồi (20% nếu là cận chiến Huyền Thoại, 0 nếu không thỏa).
     */
    public static double calculateLifesteal(ItemQualityData weaponData, String weaponItemId, double damageDealt) {
        if (weaponData == null || weaponItemId == null || damageDealt <= 0) {
            return 0.0;
        }
        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(weaponItemId);
        return SpecialEffectHandler.calculateLifesteal(weaponData.tier(), category, damageDealt);
    }

    /**
     * Áp dụng hút máu vào thanh máu của người chơi khi tấn công trúng đích.
     *
     * @param weaponData    Dữ liệu phẩm chất của vũ khí.
     * @param weaponItemId  Định danh vũ khí.
     * @param damageDealt   Lượng sát thương thực tế gây ra.
     * @param currentHealth Lượng máu hiện tại của người chơi.
     * @param maxHealth     Lượng máu tối đa của người chơi.
     * @return Kết quả hút máu kèm lượng máu mới không vượt quá maxHealth.
     */
    public static LifestealResult applyLifesteal(
            ItemQualityData weaponData,
            String weaponItemId,
            double damageDealt,
            double currentHealth,
            double maxHealth
    ) {
        double lifesteal = calculateLifesteal(weaponData, weaponItemId, damageDealt);
        if (lifesteal <= 0 || maxHealth <= 0) {
            return new LifestealResult(0.0, currentHealth);
        }
        double safeCurrent = Math.max(0.0, currentHealth);
        double updatedHealth = Math.min(maxHealth, safeCurrent + lifesteal);
        return new LifestealResult(lifesteal, updatedHealth);
    }

    /**
     * Kiểm tra xem công cụ đang cầm có nhận hiệu ứng Đào nhanh II (Haste II) hay không.
     */
    public static boolean shouldApplyHaste(ItemQualityData toolData, String toolItemId) {
        if (toolData == null || toolItemId == null) {
            return false;
        }
        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(toolItemId);
        return SpecialEffectHandler.shouldApplyHaste(toolData.tier(), category);
    }

    /**
     * Đánh giá toàn bộ hiệu ứng nội tại khi cầm một trang bị trên tay.
     */
    public static HeldItemEffects evaluateHeldItemEffects(String itemId, ItemQualityData data) {
        if (itemId == null || data == null) {
            return new HeldItemEffects(false, false, false);
        }
        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(itemId);
        boolean haste = SpecialEffectHandler.shouldApplyHaste(data.tier(), category);
        boolean weakness = SpecialEffectHandler.shouldApplyWeakness(data.tier(), category);
        boolean miningFatigue = SpecialEffectHandler.shouldApplyMiningFatigue(data.tier(), category);
        return new HeldItemEffects(haste, weakness, miningFatigue);
    }

    /**
     * Kiểm tra người chơi có đang mặc trọn bộ 4 món giáp Huyền Thoại hay không.
     */
    public static boolean isFullLegendaryArmor(
            ItemQualityData helmet,
            ItemQualityData chestplate,
            ItemQualityData leggings,
            ItemQualityData boots
    ) {
        return helmet != null && helmet.tier() == QualityTier.LEGENDARY
                && chestplate != null && chestplate.tier() == QualityTier.LEGENDARY
                && leggings != null && leggings.tier() == QualityTier.LEGENDARY
                && boots != null && boots.tier() == QualityTier.LEGENDARY;
    }

    /**
     * Tính toán tổng lượng máu cộng thêm từ cả 4 món giáp đang mặc.
     * Mũ +4 HP, Áo +6 HP, Quần +6 HP, Giày +4 HP đối với bộ Huyền Thoại (+20 HP tổng cộng).
     */
    public static double calculateTotalArmorBonusHealth(
            ItemQualityData helmet,
            ItemQualityData chestplate,
            ItemQualityData leggings,
            ItemQualityData boots
    ) {
        double total = 0.0;
        if (helmet != null) {
            total += QualityModifier.getStats(helmet.tier(), QualityModifier.EquipmentCategory.ARMOR_HELMET).healthBonus();
        }
        if (chestplate != null) {
            total += QualityModifier.getStats(chestplate.tier(), QualityModifier.EquipmentCategory.ARMOR_CHESTPLATE).healthBonus();
        }
        if (leggings != null) {
            total += QualityModifier.getStats(leggings.tier(), QualityModifier.EquipmentCategory.ARMOR_LEGGINGS).healthBonus();
        }
        if (boots != null) {
            total += QualityModifier.getStats(boots.tier(), QualityModifier.EquipmentCategory.ARMOR_BOOTS).healthBonus();
        }
        return total;
    }

    /**
     * Tính toán lượng máu tối đa của người chơi khi mặc giáp phẩm chất.
     */
    public static double calculatePlayerMaxHealth(
            ItemQualityData helmet,
            ItemQualityData chestplate,
            ItemQualityData leggings,
            ItemQualityData boots
    ) {
        return BASE_PLAYER_MAX_HEALTH + calculateTotalArmorBonusHealth(helmet, chestplate, leggings, boots);
    }

    /**
     * Kiểm tra người chơi có kích hoạt đặc quyền Miễn nhiễm lửa vĩnh viễn (Permanent Fire Resistance) hay không.
     */
    public static boolean hasPermanentFireResistance(
            ItemQualityData helmet,
            ItemQualityData chestplate,
            ItemQualityData leggings,
            ItemQualityData boots
    ) {
        return isFullLegendaryArmor(helmet, chestplate, leggings, boots);
    }

    /**
     * Kiểm tra và kích hoạt hiệu ứng Cứu Sinh Khẩn Cấp (Emergency Recovery) của bộ giáp Huyền Thoại.
     * Máu tụt dưới 30% (< 12 HP trên chuẩn 40 HP), nhận Kháng cự I + Hồi phục I trong 10-15 giây (hồi chiêu 60 giây).
     *
     * @param isFullLegendarySet Xác nhận người chơi đang mặc đủ bộ 4 món Huyền Thoại.
     * @param currentHealth      Lượng máu hiện tại.
     * @param maxHealth          Lượng máu tối đa.
     * @param currentTick        Tick thế giới hiện tại.
     * @param lastTriggerTick    Tick kích hoạt gần nhất trước đó.
     * @param maxArmorAffinity   Điểm gắn bó cao nhất của các món giáp đang mặc (0-100).
     * @return Trạng thái kích hoạt và thời gian duy trì hiệu ứng.
     */
    public static EmergencyRecoveryResult checkEmergencyRecovery(
            boolean isFullLegendarySet,
            double currentHealth,
            double maxHealth,
            long currentTick,
            long lastTriggerTick,
            int maxArmorAffinity
    ) {
        boolean canTrigger = SpecialEffectHandler.canTriggerEmergencyRecovery(
                isFullLegendarySet,
                currentHealth,
                maxHealth,
                currentTick,
                lastTriggerTick
        );

        if (!canTrigger) {
            long nextEligible = lastTriggerTick + SpecialEffectHandler.EMERGENCY_RECOVERY_COOLDOWN_TICKS;
            return new EmergencyRecoveryResult(false, 0, Math.max(currentTick, nextEligible));
        }

        int durationTicks = SpecialEffectHandler.calculateEmergencyRecoveryDurationTicks(maxArmorAffinity);
        long nextEligible = currentTick + SpecialEffectHandler.EMERGENCY_RECOVERY_COOLDOWN_TICKS;
        return new EmergencyRecoveryResult(true, durationTicks, nextEligible);
    }
}
