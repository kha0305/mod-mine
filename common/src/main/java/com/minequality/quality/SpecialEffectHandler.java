package com.minequality.quality;

/**
 * Xử lý logic và tính toán hiệu ứng đặc biệt, đặc quyền kích hoạt và bùa nội tại của các trang bị phẩm chất cao hoặc phế phẩm.
 */
public final class SpecialEffectHandler {

    public static final double LIFESTEAL_PERCENTAGE = 0.20; // 20% Hút máu cho vũ khí cận chiến Huyền Thoại
    public static final long EMERGENCY_RECOVERY_COOLDOWN_TICKS = 1200L; // Thời gian hồi chiêu 60 giây (20 ticks/giây)
    public static final int BASE_EMERGENCY_RECOVERY_DURATION_TICKS = 200; // 10 giây hiệu ứng cơ bản

    private SpecialEffectHandler() {
    }

    /**
     * Tính toán lượng máu hồi lại khi vũ khí cận chiến Huyền Thoại đánh trúng mục tiêu.
     *
     * @param tier        Bậc phẩm chất của vũ khí.
     * @param category    Nhóm trang bị.
     * @param damageDealt Lượng sát thương thực tế gây ra cho mục tiêu.
     * @return Lượng máu được hồi lại cho người chơi (0 nếu không phải cận chiến Huyền Thoại).
     */
    public static double calculateLifesteal(QualityTier tier, QualityModifier.EquipmentCategory category, double damageDealt) {
        if (tier == QualityTier.LEGENDARY && category == QualityModifier.EquipmentCategory.MELEE_WEAPON && damageDealt > 0) {
            return damageDealt * LIFESTEAL_PERCENTAGE;
        }
        return 0.0;
    }

    /**
     * Kiểm tra xem công cụ khai thác có đủ điều kiện nhận hiệu ứng Đào nhanh II (Haste II) liên tục hay không.
     */
    public static boolean shouldApplyHaste(QualityTier tier, QualityModifier.EquipmentCategory category) {
        return tier == QualityTier.LEGENDARY && category == QualityModifier.EquipmentCategory.MINING_TOOL;
    }

    /**
     * Kiểm tra điều kiện kích hoạt hiệu ứng Cứu Sinh Khẩn Cấp (Emergency Recovery) của bộ giáp Huyền Thoại.
     *
     * @param isFullLegendarySet Xác nhận người chơi có đang mặc trọn bộ 4 món giáp Huyền Thoại hay không.
     * @param currentHealth      Lượng máu hiện tại của người chơi.
     * @param maxHealth          Lượng máu tối đa của người chơi.
     * @param currentTick        Thời điểm tick hiện tại của thế giới.
     * @param lastTriggerTick    Thời điểm tick kích hoạt gần nhất trước đó.
     * @return true nếu máu tụt dưới 30% và đã hết thời gian hồi chiêu 60 giây.
     */
    public static boolean canTriggerEmergencyRecovery(
            boolean isFullLegendarySet,
            double currentHealth,
            double maxHealth,
            long currentTick,
            long lastTriggerTick
    ) {
        if (!isFullLegendarySet || maxHealth <= 0 || currentHealth <= 0 || Double.isNaN(currentHealth) || Double.isNaN(maxHealth)) {
            return false;
        }
        boolean isLowHealth = (currentHealth / maxHealth) < 0.30;
        boolean cooldownElapsed = (lastTriggerTick < 0) || (currentTick < lastTriggerTick) || ((currentTick - lastTriggerTick) >= EMERGENCY_RECOVERY_COOLDOWN_TICKS);
        return isLowHealth && cooldownElapsed;
    }

    /**
     * Tính toán thời gian duy trì hiệu ứng Cứu Sinh Khẩn Cấp (Kháng cự I + Hồi phục I).
     * Mốc gắn bó tối đa 100/100 kéo dài thời gian thêm 50% (từ 10 giây lên 15 giây).
     *
     * @param affinity Điểm gắn bó của trang bị kích hoạt.
     * @return Thời gian duy trì tính bằng tick (20 ticks = 1 giây).
     */
    public static int calculateEmergencyRecoveryDurationTicks(int affinity) {
        double multiplier = ItemAffinity.getDurationMultiplier(affinity);
        return (int) Math.round(BASE_EMERGENCY_RECOVERY_DURATION_TICKS * multiplier);
    }

    /**
     * Hệ số nhân bán kính chấn động văng xung quanh (AoE Shockwave) của Chùy (Mace).
     */
    public static double getMaceShockwaveRadiusMultiplier(QualityTier tier) {
        if (tier == null) {
            return 1.0;
        }
        return switch (tier) {
            case RARE -> 1.15; // +15%
            case EPIC, LEGENDARY -> 1.30; // +30%
            default -> 1.0;
        };
    }

    /**
     * Hệ số nhân sát thương rơi đập (Smash Damage) của Chùy (Mace).
     */
    public static double getMaceSmashDamageMultiplier(QualityTier tier) {
        if (tier == QualityTier.LEGENDARY) {
            return 1.50; // +50% Sát thương đập
        }
        return 1.0;
    }

    /**
     * Kiểm tra Chùy có được miễn nhiễm 100% sát thương rơi (Zero Fall Damage) khi đáp trúng mục tiêu hay không.
     */
    public static boolean isMaceImmuneToFallDamage(QualityTier tier) {
        return tier == QualityTier.LEGENDARY;
    }

    /**
     * Hệ số nhân tốc độ bay khi ném Đinh Ba (Trident).
     */
    public static double getTridentThrowVelocityMultiplier(QualityTier tier) {
        if (tier == null) {
            return 1.0;
        }
        return switch (tier) {
            case RARE -> 1.25; // +25%
            case EPIC, LEGENDARY -> 1.50; // +50%
            default -> 1.0;
        };
    }

    /**
     * Hệ số nhân tốc độ bùa Dẫn Hướng (Loyalty) bay ngược về tay người chơi.
     */
    public static double getTridentLoyaltyReturnSpeedMultiplier(QualityTier tier) {
        if (tier == QualityTier.EPIC || tier == QualityTier.LEGENDARY) {
            return 2.0; // Tốc độ bay về nhanh gấp 2 lần
        }
        return 1.0;
    }

    /**
     * Kiểm tra Đinh Ba có tích hợp sẵn bùa Dẫn Hướng (Loyalty I ngầm) hay không.
     */
    public static boolean hasTridentBuiltInLoyalty(QualityTier tier) {
        return tier == QualityTier.LEGENDARY;
    }

    /**
     * Hệ số nhân cự ly lướt của bùa Phóng (Riptide) cho Đinh Ba dưới nước hoặc khi trời mưa.
     */
    public static double getTridentRiptideDistanceMultiplier(QualityTier tier, boolean inWaterOrRain) {
        if (tier == QualityTier.LEGENDARY && inWaterOrRain) {
            return 1.25; // +25% cự ly lướt
        }
        return 1.0;
    }

    /**
     * Hệ số nhân sát thương cận chiến cộng thêm cho Đinh Ba dưới nước hoặc khi trời mưa.
     */
    public static double getTridentMeleeDamageMultiplier(QualityTier tier, boolean inWaterOrRain) {
        if (tier == QualityTier.LEGENDARY && inWaterOrRain) {
            return 1.15; // +15% sát thương cận chiến
        }
        return 1.0;
    }

    /**
     * Hệ số nhân tốc độ bay của mũi tên bắn từ Cung Tên hoặc Nỏ Huyền Thoại.
     */
    public static double getRangedProjectileVelocityMultiplier(QualityTier tier) {
        if (tier == QualityTier.LEGENDARY) {
            return 1.25; // +25% tốc độ bay và quỹ đạo bay thẳng hơn
        }
        return 1.0;
    }

    /**
     * Kiểm tra điều kiện kích hoạt hiệu ứng Kháng cự II (Resistance II trong 3 giây) khi dùng Khiên Huyền Thoại đỡ đòn cực mạnh.
     */
    public static boolean shouldTriggerShieldResistance(QualityTier tier) {
        return tier == QualityTier.LEGENDARY;
    }

    /**
     * Kiểm tra vũ khí cận chiến Phế Phẩm (Junk) có gây hiệu ứng suy yếu (Weakness I) khi cầm hay không.
     */
    public static boolean shouldApplyWeakness(QualityTier tier, QualityModifier.EquipmentCategory category) {
        return tier == QualityTier.JUNK && category == QualityModifier.EquipmentCategory.MELEE_WEAPON;
    }

    /**
     * Kiểm tra công cụ khai thác Phế Phẩm (Junk) có gây hiệu ứng đào chậm (Mining Fatigue I) khi cầm hay không.
     */
    public static boolean shouldApplyMiningFatigue(QualityTier tier, QualityModifier.EquipmentCategory category) {
        return tier == QualityTier.JUNK && category == QualityModifier.EquipmentCategory.MINING_TOOL;
    }
}