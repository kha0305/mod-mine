package com.minequality.fabric.mixin;

import java.util.List;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor can thiep vao LivingEntityRenderer de lay danh sach layers hoac goi addLayer an toan.
 */
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {

    @Accessor("layers")
    @SuppressWarnings("rawtypes")
    List<RenderLayer> minequality$getLayers();

    @Invoker("addLayer")
    @SuppressWarnings("rawtypes")
    boolean minequality$callAddLayer(RenderLayer layer);
}
