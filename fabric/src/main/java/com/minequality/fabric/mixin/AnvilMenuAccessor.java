package com.minequality.fabric.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor cho phep doc va ghi cac thuoc tinh can thiet tren AnvilMenu.
 */
@Mixin(AnvilMenu.class)
public interface AnvilMenuAccessor {

    @Accessor("repairItemCountCost")
    int getRepairItemCountCost();

    @Accessor("repairItemCountCost")
    void setRepairItemCountCost(int repairItemCountCost);

    @Accessor("cost")
    DataSlot getCost();
}
