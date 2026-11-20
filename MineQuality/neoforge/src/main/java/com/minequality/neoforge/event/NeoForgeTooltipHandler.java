package com.minequality.neoforge.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityTooltipBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Hook xu ly them thong tin pham chat vao Tooltip vat pham tren NeoForge Loader.
 * Su dung QualityTooltipBuilder de dam bao dinh dang chuan tieng Anh en_us theo tai lieu thiet ke.
 */
public final class NeoForgeTooltipHandler {

    private static boolean registered = false;

    private NeoForgeTooltipHandler() {
    }

    /**
     * Dang ky hook Tooltip vao he thong Event Bus cua NeoForge.
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
     * Dat lai trang thai dang ky (phuc vu moi truong kiem thu).
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
     * @return Danh sach Tooltip sau khi da duoc bo sung thong tin pham chat.
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
