package com.minequality.fabric.mixin;

import com.minequality.fabric.event.FabricTooltipHandler;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin vao ItemCombinerMenu de bat su kien thay doi o chua (slotsChanged).
 * Dam bao ho tro ca Vanilla AnvilMenu lan cac mod tuy bien De (nhu Easy Anvils / ModAnvilMenu).
 */
@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin {

    @Shadow
    @Final
    protected Container inputSlots;

    @Shadow
    @Final
    protected ResultContainer resultSlots;

    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void onAfterSlotsChanged(Container container, CallbackInfo ci) {
        if (!((Object) this instanceof AnvilMenu anvilMenu)) {
            return;
        }

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

                try {
                    AnvilMenuAccessor accessor = (AnvilMenuAccessor) anvilMenu;
                    accessor.getCost().set(expCost);
                    accessor.setRepairItemCountCost(1);
                } catch (Throwable ignored) {
                }

                ItemStack resultStack = input.copy();
                this.resultSlots.setItem(0, resultStack);
                ((net.minecraft.world.inventory.AbstractContainerMenu) (Object) this).broadcastChanges();
            }
        }
    }
}
