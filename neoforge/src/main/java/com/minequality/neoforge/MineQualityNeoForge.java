package com.minequality.neoforge;

import com.minequality.MineQuality;

/**
 * Điểm vào (Entrypoint) khởi động của mod trên nền tảng NeoForge Loader.
 */
public final class MineQualityNeoForge {
    public MineQualityNeoForge() {
        MineQuality.init();
        MineQuality.LOGGER.info("Khoi chay MineQuality tren NeoForge Loader thanh cong.");
    }
}
