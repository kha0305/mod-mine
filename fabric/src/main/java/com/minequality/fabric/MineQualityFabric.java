package com.minequality.fabric;

import com.minequality.MineQuality;
import com.minequality.fabric.event.FabricAnvilHandler;
import com.minequality.fabric.event.FabricCombatHandler;
import com.minequality.fabric.event.FabricServerItemManager;
import com.minequality.fabric.event.FabricTooltipHandler;
import net.fabricmc.api.ModInitializer;

/**
 * Điểm vào (Entrypoint) khởi động của mod trên nền tảng Fabric Loader.
 */
public final class MineQualityFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MineQuality.init();
        FabricServerItemManager.register();
        FabricTooltipHandler.register();
        FabricAnvilHandler.register();
        FabricCombatHandler.register();
        MineQuality.LOGGER.info("Khoi chay MineQuality tren Fabric Loader thanh cong.");
    }
}
