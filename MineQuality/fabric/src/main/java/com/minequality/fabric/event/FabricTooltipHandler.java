package com.minequality.fabric.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityManager;
import com.minequality.quality.QualityTier;
import com.minequality.quality.QualityTooltipBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Hook xu ly bo sung thong tin pham chat vao Tooltip vat pham tren Fabric Loader.
 * Ket noi truc tiep vao Fabric Item API v1 (ItemTooltipCallback) de hien thi day du tren giao dien Minecraft.
 */
public final class FabricTooltipHandler {

    private static boolean registered = false;
    private static boolean clientCallbackRegistered = false;

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
        tryRegisterClientCallbackSafe();
    }

    /**
     * Dang ky rieng biet cho phia Client thong qua ClientModInitializer.
     */
    public static synchronized void registerClientTooltipCallback() {
        if (clientCallbackRegistered) {
            return;
        }
        tryRegisterClientCallbackSafe();
    }

    private static void tryRegisterClientCallbackSafe() {
        try {
            Class.forName("net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback");
            registerActualFabricCallback();
            clientCallbackRegistered = true;
        } catch (Throwable ignored) {
            // Môi trường kiểm thử đơn vị hoặc môi trường Dedicated Server không có Fabric Client API
        }
    }

    private static void registerActualFabricCallback() {
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register(
                (stack, context, type, lines) -> handleMinecraftItemTooltip(stack, lines)
        );
    }

    /**
     * Xu ly them noi dung pham chat vao danh sach Component Tooltip cua Minecraft.
     */
    public static void handleMinecraftItemTooltip(net.minecraft.world.item.ItemStack stack, java.util.List<net.minecraft.network.chat.Component> lines) {
        if (stack == null || stack.isEmpty() || lines == null) {
            return;
        }

        net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return;
        }
        String itemId = id.toString();
        if (!QualityItemFilter.isEligible(itemId)) {
            return;
        }

        ItemQualityData qualityData = readQualityDataOnly(stack);
        int insertIndex = Math.min(1, lines.size());
        if (qualityData == null) {
            // Vat pham chua duoc Server gan pham chat (dang trong o che tao ResultSlot hoac preview)
            // AN PHAM CHAT: Hien thi "Quality: ??? (Craft to reveal)"
            lines.add(insertIndex, net.minecraft.network.chat.Component.literal("§7§oQuality: ??? (Craft to reveal)"));
            return;
        }

        List<String> rawLines = QualityTooltipBuilder.buildTooltipLines(itemId, qualityData);
        if (rawLines == null || rawLines.isEmpty()) {
            return;
        }

        for (String raw : rawLines) {
            if (raw.isEmpty()) {
                lines.add(insertIndex++, net.minecraft.network.chat.Component.empty());
                continue;
            }
            String coloredText = applyMinecraftColorFormatting(raw, qualityData.tier());
            lines.add(insertIndex++, net.minecraft.network.chat.Component.literal(coloredText));
        }
    }

    /**
     * Doc du lieu pham chat tu ItemStack (Che do Read-Only an toan tu Client).
     * Tuyet doi khong tu y random tren Client de tranh bi thay doi pham chat khi dong bo.
     */
    public static ItemQualityData readQualityDataOnly(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        try {
            net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                net.minecraft.nbt.CompoundTag rootTag = customData.copyTag();
                if (rootTag.contains("minequality")) {
                    net.minecraft.nbt.CompoundTag qTag = rootTag.getCompoundOrEmpty("minequality");
                    String tierStr = qTag.getStringOr("tier", "COMMON");
                    QualityTier tier;
                    try {
                        tier = QualityTier.valueOf(tierStr.toUpperCase());
                    } catch (Exception ex) {
                        tier = QualityTier.COMMON;
                    }
                    int affinity = qTag.getIntOr("affinity", 0);
                    int reforgeCount = qTag.getIntOr("reforge_count", 0);
                    boolean fireproof = qTag.getBooleanOr("fireproof", false);
                    boolean unbreakable = qTag.getBooleanOr("unbreakable", false);
                    return new ItemQualityData(tier, affinity, reforgeCount, fireproof, unbreakable);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Gan ma mau Minecraft chuan theo pham chat va loai chi so.
     */
    public static String applyMinecraftColorFormatting(String line, QualityTier tier) {
        if (line == null || line.isEmpty()) {
            return "";
        }

        // 1. Dong Phẩm chất: "Quality: <Tier>"
        if (line.startsWith("Quality:")) {
            String color = getTierColorCode(tier);
            return color + "§l" + line;
        }

        // 2. Dong Do gan bo: "Item Affinity:"
        if (line.startsWith("Item Affinity:")) {
            return "§b" + line;
        }

        // 3. Dong Vi tri trang bi: "When in Main Hand:", "When on Body:", v.v.
        if (line.startsWith("When ")) {
            return "§7" + line;
        }

        // 4. Dong Chi so cong hoac tru
        if (line.trim().startsWith("+")) {
            return "§2" + line;
        }
        if (line.trim().startsWith("-")) {
            return "§c" + line;
        }

        // 5. Hieu ung dac biet hoac Set Bonus
        if (line.startsWith("Special:") || line.startsWith("Set Bonus:")) {
            return "§6" + line;
        }
        if (line.startsWith("Passive:")) {
            return "§a" + line;
        }
        if (line.startsWith("Debuff:")) {
            return "§4" + line;
        }

        // 6. Ben vinh cuu hoac Chống cháy
        if (line.contains("Unbreakable") || line.contains("Fireproof")) {
            return "§d" + line;
        }

        // 7. Thong tin reforge va Pity
        if (line.startsWith("Reforged:")) {
            if (line.contains("Guaranteed")) {
                return "§a§l" + line;
            }
            if (line.contains("Max Quality")) {
                return "§6" + line;
            }
            return "§e" + line;
        }

        return "§7" + line;
    }

    private static String getTierColorCode(QualityTier tier) {
        if (tier == null) return "§f";
        return switch (tier) {
            case JUNK -> "§8";
            case COMMON -> "§f";
            case UNCOMMON -> "§a";
            case RARE -> "§9";
            case EPIC -> "§d";
            case LEGENDARY -> "§6";
        };
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
        clientCallbackRegistered = false;
    }

    /**
     * Bo sung cac dong mo ta pham chat vao danh sach Tooltip hien tai cua vat pham (dung cho test va logic chung).
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
