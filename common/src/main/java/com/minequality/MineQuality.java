package com.minequality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lớp điều phối trung tâm của mod MineQuality.
 * Chứa các hằng số định danh và khởi tạo logic dùng chung cho mọi loader.
 */
public final class MineQuality {
    public static final String MOD_ID = "mod_mine";
    public static final String MOD_NAME = "MineQuality";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private MineQuality() {
        // Lớp tiện ích không khởi tạo đối tượng trực tiếp
    }

    public static void init() {
        LOGGER.info("Khoi tao {} thanh cong tren Minecraft 26.3!", MOD_NAME);
    }
}
