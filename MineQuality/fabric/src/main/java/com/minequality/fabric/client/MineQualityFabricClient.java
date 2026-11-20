package com.minequality.fabric.client;

import com.minequality.MineQuality;
import com.minequality.fabric.event.FabricTooltipHandler;
import net.fabricmc.api.ClientModInitializer;

/**
 * Entrypoint khoi tao phia Client cua mod MineQuality tren Fabric Loader.
 * Dam nhan dang ky cac hook giao dien nguoi dung nhu Tooltip, Render va Event Client.
 */
public final class MineQualityFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricTooltipHandler.registerClientTooltipCallback();
        MineQuality.LOGGER.info("Khoi tao phan Client cua MineQuality thanh cong.");
    }
}
