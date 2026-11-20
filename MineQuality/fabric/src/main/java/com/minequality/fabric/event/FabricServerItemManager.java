package com.minequality.fabric.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityManager;

/**
 * Quan ly vong doi pham chat vat pham tu phia Server cua Fabric Loader.
 * Dam bao pham chat chi duoc boc tham va luu vao CustomData duy nhat mot lan boi Server,
 * dong thoi loai tru hoan toan cac o xem truoc che tao (ResultSlot) de tranh tiet lo truoc pham chat.
 */
public final class FabricServerItemManager {

    private static boolean registered = false;

    private FabricServerItemManager() {
    }

    /**
     * Dang ky su kien Server Tick de quan ly pham chat vat pham.
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        tryRegisterServerTickSafe();
    }

    /**
     * Kiem tra trang thai da dang ky.
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

    private static void tryRegisterServerTickSafe() {
        try {
            Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents");
            registerActualServerTick();
        } catch (Throwable ignored) {
            // Môi trường kiểm thử đơn vị không có Server Lifecycle API
        }
    }

    private static void registerActualServerTick() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server == null || server.getPlayerList() == null) {
                return;
            }

            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player == null) {
                    continue;
                }

                // 1. Kiem tra toan bo tui do (Inventory)
                net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
                if (inventory != null) {
                    int size = inventory.getContainerSize();
                    for (int i = 0; i < size; i++) {
                        net.minecraft.world.item.ItemStack stack = inventory.getItem(i);
                        ensureQualityAssigned(stack);
                    }
                }

                // 2. Kiem tra vat pham dang cam tren con tro chuot (Carried Stack) khi vua lay tu o che tao
                if (player.containerMenu != null) {
                    net.minecraft.world.item.ItemStack carried = player.containerMenu.getCarried();
                    ensureQualityAssigned(carried);

                    // 3. Kiem tra cac o container khac nhung TUYET DOI BO QUA ResultSlot
                    for (net.minecraft.world.inventory.Slot slot : player.containerMenu.slots) {
                        if (slot == null || slot instanceof net.minecraft.world.inventory.ResultSlot) {
                            // Bo qua o xem truoc che tao de an pham chat
                            continue;
                        }
                        ensureQualityAssigned(slot.getItem());
                    }
                }
            }
        });
    }

    /**
     * Kiem tra va gan pham chat cho vat pham neu hop le va chua tung duoc gan.
     * Tra ve true neu da thuc hien gan pham chat moi, false neu vat pham khong phu hop hoac da co pham chat.
     */
    public static boolean ensureQualityAssigned(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        try {
            net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id == null) {
                return false;
            }
            String itemId = id.toString();
            if (!QualityItemFilter.isEligible(itemId)) {
                return false;
            }

            // Kiem tra xem stack da co the "minequality" chua
            net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                net.minecraft.nbt.CompoundTag rootTag = customData.copyTag();
                if (rootTag.contains("minequality")) {
                    // Da co pham chat, giu nguyen 100%, tuyet doi khong roll lai
                    return false;
                }
            }

            // Neu chua co: Server tien hanh roll DUY NHAT 1 LAN va luu truc tiep vao DataComponents.CUSTOM_DATA
            ItemQualityData rolled = ItemQualityData.of(QualityManager.rollQuality());
            final ItemQualityData finalRolled = rolled;
            net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
                net.minecraft.nbt.CompoundTag qTag = new net.minecraft.nbt.CompoundTag();
                qTag.putString("tier", finalRolled.tier().name());
                qTag.putInt("affinity", finalRolled.affinity());
                qTag.putInt("reforge_count", finalRolled.reforgeCount());
                qTag.putBoolean("fireproof", finalRolled.fireproof());
                qTag.putBoolean("unbreakable", finalRolled.unbreakable());
                tag.put("minequality", qTag);
            });
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
