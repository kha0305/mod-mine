package com.minequality.fabric.mixin;

import com.minequality.fabric.event.FabricTooltipHandler;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin can thiep vao AnvilMenu cua Minecraft de kich hoat co che ren lai (Reforge).
 * Xu ly mo khoa o ket qua tren De khi vat pham con nguyen do ben, tinh toan chi phi EXP
 * va thuc hien quy tac Vung An Toan / Vung Mao Hiem khi lay do ra.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow
    private int repairItemCountCost;

    @Shadow
    @Final
    private DataSlot cost;

    public AnvilMenuMixin(MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(type, containerId, playerInventory, access, slotDefinition);
    }

    /**
     * Can thiep sau khi Minecraft Vanilla tinh toan ket qua De.
     * Neu Vanilla tu choi nhung o 1 va o 2 la cong thuc ren hop le cua MineQuality,
     * mo khoa o ket qua voi chi phi 1 nguyen lieu va muc EXP chuan.
     */
    @Inject(method = "createResult", at = @At("RETURN"))
    private void onAfterCreateResult(CallbackInfo ci) {
        if (this.resultSlots.getItem(0).isEmpty()) {
            ItemStack input = this.inputSlots.getItem(0);
            ItemStack material = this.inputSlots.getItem(1);
            if (input == null || input.isEmpty() || material == null || material.isEmpty()) {
                return;
            }

            Identifier inputId = BuiltInRegistries.ITEM.getKey(input.getItem());
            Identifier materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
            if (inputId == null || materialId == null) {
                return;
            }

            String inputStr = inputId.toString();
            String materialStr = materialId.toString();

            boolean isMaterial = QualityItemFilter.isReforgeMaterial(inputStr, materialStr);
            boolean isEmerald = materialStr.equals("minecraft:emerald") || materialStr.endsWith(":emerald");

            if (isMaterial || isEmerald) {
                ItemQualityData currentData = FabricTooltipHandler.readQualityDataOnly(input);
                if (currentData == null) {
                    currentData = ItemQualityData.DEFAULT;
                }

                int expCost = QualityItemFilter.calculateReforgeCost(inputStr, currentData.reforgeCount());
                this.cost.set(expCost);
                this.repairItemCountCost = 1;

                ItemStack resultStack = input.copy();
                this.resultSlots.setItem(0, resultStack);
                this.broadcastChanges();
            }
        }
    }

    /**
     * Can thiep khi nguoi choi click lay vat pham ra khoi o ket qua De.
     * Thuc hien boc tham nang cap pham chat, xu ly nguy co vo do hoac bao hiem ngoc luc bao.
     */
    @Inject(method = "onTake", at = @At("HEAD"))
    private void onTakeQualityReforge(Player player, ItemStack stack, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        ItemStack input = this.inputSlots.getItem(0);
        ItemStack material = this.inputSlots.getItem(1);
        if (input == null || input.isEmpty() || material == null || material.isEmpty()) {
            return;
        }

        Identifier inputId = BuiltInRegistries.ITEM.getKey(input.getItem());
        Identifier materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
        if (inputId == null || materialId == null) {
            return;
        }

        String inputStr = inputId.toString();
        String materialStr = materialId.toString();

        boolean isMaterial = QualityItemFilter.isReforgeMaterial(inputStr, materialStr);
        boolean isEmerald = materialStr.equals("minecraft:emerald") || materialStr.endsWith(":emerald");

        if (!isMaterial && !isEmerald) {
            return;
        }

        ItemQualityData currentData = FabricTooltipHandler.readQualityDataOnly(input);
        if (currentData == null) {
            currentData = ItemQualityData.DEFAULT;
        }

        // Thuc hien thuat toan ren lai chuan theo tai lieu thiet ke
        QualityItemFilter.ReforgeResult outcome = QualityItemFilter.performReforge(
                currentData.tier(),
                currentData.reforgeCount(),
                currentData.affinity(),
                isEmerald
        );

        if (outcome.broken()) {
            // Trang bi bi vo nat do tach trong Vung Mao Hiem!
            stack.setCount(0);
            return;
        }

        // Cap nhat pham chat moi cho trang bi
        ItemQualityData newData = new ItemQualityData(
                outcome.resultingTier(),
                Math.min(100, currentData.affinity() + 5),
                outcome.nextReforgeCount(),
                currentData.fireproof(),
                currentData.unbreakable()
        );

        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            net.minecraft.nbt.CompoundTag qTag = new net.minecraft.nbt.CompoundTag();
            qTag.putString("tier", newData.tier().name());
            qTag.putInt("affinity", newData.affinity());
            qTag.putInt("reforge_count", newData.reforgeCount());
            qTag.putBoolean("fireproof", newData.fireproof());
            qTag.putBoolean("unbreakable", newData.unbreakable());
            tag.put("minequality", qTag);
        });
    }
}
