package com.minequality.neoforge;

import com.minequality.neoforge.event.NeoForgeAnvilHandler;
import com.minequality.neoforge.event.NeoForgeCombatHandler;
import com.minequality.neoforge.event.NeoForgeTooltipHandler;

/**
 * Diem vao (Entrypoint) khoi dong cua mod MineQuality tren nen tang NeoForge Loader.
 * Dieu phoi dang ky toan bo cac handler su kien tren Event Bus cua NeoForge:
 * - NeoForgeTooltipHandler: Hien thi Tooltip pham chat chuan tieng Anh en_us.
 * - NeoForgeAnvilHandler: Xu ly logic ren lai tren De, gioi han tran 30 EXP, vo do va bao hiem Ngoc Luc Bao.
 * - NeoForgeCombatHandler: Xu ly hut mau 20%, dao nhanh Haste II, hieu ung do Junk va Full Set Bonus giap Huyen Thoai.
 */
public final class MineQualityNeoForge {

    public static final String MOD_ID = "mod_mine";
    public static final String MOD_NAME = "MineQuality";

    private static boolean initialized = false;

    /**
     * Constructor khoi dong mac dinh duoc trieu goi boi NeoForge Mod Engine.
     */
    public MineQualityNeoForge() {
        init();
    }

    /**
     * Khoi tao va dang ky toan bo cac he thong xu ly su kien NeoForge.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }

        // Dang ky cac hook xu ly su kien phia NeoForge
        NeoForgeTooltipHandler.register();
        NeoForgeAnvilHandler.register();
        NeoForgeCombatHandler.register();

        initialized = true;
    }

    /**
     * Kiem tra trang thai khoi dong cua mod tren NeoForge.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Dat lai trang thai khoi dong va cac handler (phuc vu kiem thu doc lap).
     */
    public static synchronized void reset() {
        initialized = false;
        NeoForgeTooltipHandler.resetRegistration();
        NeoForgeAnvilHandler.resetRegistration();
        NeoForgeCombatHandler.resetRegistration();
    }
}
