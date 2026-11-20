package com.minequality.fabric.mixin;

import com.minequality.fabric.client.render.MagicCircleRenderLayer;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin can thiep vao AvatarRenderer de dang ky MagicCircleRenderLayer cho nhan vat nguoi choi.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @Shadow
    protected abstract boolean addLayer(RenderLayer<AvatarRenderState, PlayerModel> layer);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void minequality$registerMagicCircleLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        AvatarRenderer<?> self = (AvatarRenderer<?>) (Object) this;
        this.addLayer(new MagicCircleRenderLayer(self));
    }
}
