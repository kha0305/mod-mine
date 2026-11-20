package com.minequality.fabric;

import com.minequality.MineQuality;

/**
 * Điểm vào (Entrypoint) khởi động của mod trên nền tảng Fabric Loader.
 */
public final class MineQualityFabric {
    public void onInitialize() {
        MineQuality.init();
        MineQuality.LOGGER.info("Khoi chay MineQuality tren Fabric Loader thanh cong.");
    }
}
