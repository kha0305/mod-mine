package com.minequality.fabric.mixin;

import com.minequality.fabric.client.render.MagicCircleRenderLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin can thiep vao AvatarRenderer de dang ky MagicCircleRenderLayer cho nhan vat nguoi choi.
 * Su dung LivingEntityRendererAccessor de bo sung layer ma khong can @Shadow gay loi ke thua.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void minequality$registerMagicCircleLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        AvatarRenderer<?> self = (AvatarRenderer<?>) (Object) this;
        LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) self;
        accessor.minequality$getLayers().add(new MagicCircleRenderLayer(self));
    }
}
