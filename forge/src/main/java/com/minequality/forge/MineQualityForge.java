package com.minequality.forge;

import com.minequality.forge.event.ForgeAnvilHandler;
import com.minequality.forge.event.ForgeCombatHandler;
import com.minequality.forge.event.ForgeTooltipHandler;

/**
 * Điểm vào (Entrypoint) khởi động của mod trên nền tảng MinecraftForge Loader.
 * Điều phối đăng ký các bộ lắng nghe sự kiện (Event Handlers) cho Forge.
 */
public final class MineQualityForge {
    public static final String MOD_ID = "mod_mine";
    public static final String MOD_NAME = "MineQuality";

    private static boolean initialized = false;

    public MineQualityForge() {
        init();
    }

    /**
     * Khởi tạo mod và kích hoạt đăng ký các sự kiện trên MinecraftForge.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        ForgeTooltipHandler.register();
        ForgeAnvilHandler.register();
        ForgeCombatHandler.register();
        initialized = true;
        System.out.println("[" + MOD_NAME + "] Khoi chay MineQuality tren MinecraftForge Loader thanh cong.");
    }

    /**
     * Kiểm tra trạng thái đã khởi tạo hay chưa.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Đặt lại trạng thái khởi tạo (sử dụng trong kiểm thử đơn vị).
     */
    public static synchronized void resetInitialization() {
        initialized = false;
        ForgeTooltipHandler.resetRegistration();
        ForgeAnvilHandler.resetRegistration();
        ForgeCombatHandler.resetRegistration();
    }
}
