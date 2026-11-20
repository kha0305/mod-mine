package com.minequality.fabric;

import com.minequality.MineQuality;
import com.minequality.fabric.event.FabricAnvilHandler;
import com.minequality.fabric.event.FabricCombatHandler;
import com.minequality.fabric.event.FabricTooltipHandler;

/**
 * Điểm vào (Entrypoint) khởi động của mod trên nền tảng Fabric Loader.
 */
public final class MineQualityFabric {
    public void onInitialize() {
        MineQuality.init();
        FabricTooltipHandler.register();
        FabricAnvilHandler.register();
        FabricCombatHandler.register();
        MineQuality.LOGGER.info("Khoi chay MineQuality tren Fabric Loader thanh cong.");
    }
}
