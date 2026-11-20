package com.minequality.fabric.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityTooltipBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Hook xu ly them thong tin pham chat vao Tooltip vat pham tren Fabric Loader.
 * Su dung QualityTooltipBuilder de dam bao 100% dung dinh dang chuan tieng Anh en_us.
 */
public final class FabricTooltipHandler {

    private static boolean registered = false;

    private FabricTooltipHandler() {
    }

    /**
     * Dang ky hook Tooltip vao he thong Fabric.
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
    }

    /**
     * Kiem tra trang thai da dang ky hay chua.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * Dat lai trang thai dang ky (dung trong kiem thu).
     */
    public static synchronized void resetRegistration() {
        registered = false;
    }

    /**
     * Bo sung cac dong mo ta pham chat vao danh sach Tooltip hien tai cua vat pham.
     *
     * @param itemId         Dinh danh vat pham (vi du "minecraft:diamond_chestplate").
     * @param qualityData    Du lieu pham chat Data Component cua vat pham.
     * @param currentTooltip Danh sach cac dong Tooltip hien tai can bo sung.
     * @return Danh sach Tooltip sau khi da duoc bo sung cac dong pham chat.
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
