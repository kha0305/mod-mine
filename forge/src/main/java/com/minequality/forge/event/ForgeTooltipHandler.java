package com.minequality.forge.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityTooltipBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Lắng nghe và bổ sung thông tin phẩm chất trang bị vào Tooltip trên MinecraftForge Loader.
 * Sử dụng QualityTooltipBuilder để hiển thị thông tin 100% tiếng Anh chuẩn quốc tế (en_us).
 */
public final class ForgeTooltipHandler {

    private static boolean registered = false;

    private ForgeTooltipHandler() {
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

    /**
     * Kiểm tra trạng thái đăng ký.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * Đặt lại trạng thái đăng ký (dùng cho kiểm thử đơn vị).
     */
    public static synchronized void resetRegistration() {
        registered = false;
    }

    /**
     * Bổ sung các dòng mô tả phẩm chất vào danh sách Tooltip của vật phẩm trên Forge.
     *
     * @param itemId         Định danh vật phẩm (ví dụ "minecraft:diamond_chestplate").
     * @param qualityData    Dữ liệu phẩm chất Data Component của vật phẩm.
     * @param currentTooltip Danh sách các dòng Tooltip hiện tại.
     * @return Danh sách Tooltip đã được bổ sung thông tin phẩm chất.
     */
    public static List<String> appendTooltip(String itemId, ItemQualityData qualityData, List<String> currentTooltip) {
        if (currentTooltip == null) {
            currentTooltip = new ArrayList<>();
        }
        if (itemId == null || qualityData == null) {
            return currentTooltip;
        }
        if (!QualityItemFilter.isEligible(itemId)) {
            return currentTooltip;
        }

        List<String> qualityLines = QualityTooltipBuilder.buildTooltipLines(itemId, qualityData);
        if (qualityLines != null && !qualityLines.isEmpty()) {
            currentTooltip.addAll(qualityLines);
        }
        return currentTooltip;
    }
}
