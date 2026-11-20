package com.minequality.fabric.client.render;

import com.minequality.fabric.event.FabricTooltipHandler;
import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityTier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * RenderLayer ve vong tron ma thuat (Magic Circle) duoi chan nguoi choi.
 * Su dung mo hinh mat phang 1-Quad toi uu tuyet doi voi ket cau Texture 32-bit ARGB,
 * xoay deu theo thoi gian va tu phat quang trong bong toi (Emissive Rendering).
 */
public final class MagicCircleRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(
            "minequality",
            "textures/entity/magic_circle.png"
    );

    private static final int FULL_BRIGHT_LIGHT = 15728880;
    private static final float BASE_RADIUS = 1.75f;
    private static final float ROTATION_SPEED = 1.5f;

    public MagicCircleRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            AvatarRenderState state,
            float limbSwing,
            float limbSwingAmount
    ) {
        if (!shouldRender(state)) {
            return;
        }

        poseStack.pushPose();

        // 1. Dịch chuyển xuống mặt đất sát chân người chơi
        // Trong hệ tọa độ LivingEntityRenderer, trục Y đảo chiều hướng xuống và đáy bàn chân nằm ở Y = 1.50f.
        // Đặt Y = 1.49f để vòng tròn ma thuật áp sát mặt sàn (nổi nhẹ 0.01f tránh Z-fighting với khối bên dưới).
        poseStack.translate(0.0f, 1.49f, 0.0f);

        // 2. Góc xoay đều theo thời gian thực (1.5 độ mỗi tick = chu kỳ 12 giây cho 1 vòng tròn 360 độ)
        float angle = (state.ageInTicks * ROTATION_SPEED) % 360.0f;
        poseStack.rotateDegrees(Axis.YP, angle);

        // 3. Hiệu ứng thở nhẹ (Pulse Breathing: nhấp nhô biên độ 3% kích thước tạo cảm giác ma thuật sống động)
        float pulse = 1.0f + 0.03f * (float) Math.sin(state.ageInTicks * 0.08f);
        float radius = BASE_RADIUS * pulse;

        // 4. RenderType phát quang (Emissive) để vòng tròn ma thuật luôn rực rỡ và tỏa sáng trong đêm tối
        RenderType renderType = RenderTypes.entityTranslucentEmissive(TEXTURE_LOCATION);

        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            // Mặt phẳng trên (Góc nhìn từ trên xuống)
            consumer.addVertex(pose, -radius, 0.0f, -radius)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT_LIGHT)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f);

            consumer.addVertex(pose, -radius, 0.0f, radius)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT_LIGHT)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f);

            consumer.addVertex(pose, radius, 0.0f, radius)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT_LIGHT)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f);

            consumer.addVertex(pose, radius, 0.0f, -radius)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT_LIGHT)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f);

            // Mặt phẳng dưới (Góc nhìn từ dưới lên khi người chơi nhảy cao hoặc bay lơ lửng)
            consumer.addVertex(pose, radius, 0.0f, -radius)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT_LIGHT)
                    .setNormal(pose, 0.0f, -1.0f, 0.0f);

            consumer.addVertex(pose, radius, 0.0f, radius)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT_LIGHT)
                    .setNormal(pose, 0.0f, -1.0f, 0.0f);

            consumer.addVertex(pose, -radius, 0.0f, radius)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT_LIGHT)
                    .setNormal(pose, 0.0f, -1.0f, 0.0f);

            consumer.addVertex(pose, -radius, 0.0f, -radius)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT_LIGHT)
                    .setNormal(pose, 0.0f, -1.0f, 0.0f);
        });

        poseStack.popPose();
    }

    /**
     * Kiem tra dieu kien kich hoat hien thi vong tron ma thuat.
     */
    private static boolean shouldRender(AvatarRenderState state) {
        if (state == null || state.isInvisible || state.isSpectator) {
            return false;
        }

        // 1. Kích hoạt khi mặc bất kỳ món giáp Huyền Thoại nào (Mũ, Áo, Quần, Giày)
        if (isLegendary(state.headEquipment)
                || isLegendary(state.chestEquipment)
                || isLegendary(state.legsEquipment)
                || isLegendary(state.feetEquipment)) {
            return true;
        }

        // 2. Kích hoạt khi cầm trang bị Huyền Thoại trên tay chính hoặc tay phụ
        if (isLegendary(state.getMainHandItemStack()) || isLegendary(state.leftHandItemStack)) {
            return true;
        }

        // 3. Cơ chế Demo: Kích hoạt khi cầm gậy (Stick), mảnh thạch anh tím (Amethyst Shard),
        // sao Nether (Nether Star) hoặc bất kỳ trang bị nào có phẩm chất từ Rare trở lên
        if (isDemoTrigger(state.getMainHandItemStack()) || isDemoTrigger(state.leftHandItemStack)) {
            return true;
        }

        return false;
    }

    private static boolean isLegendary(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ItemQualityData data = FabricTooltipHandler.readQualityDataOnly(stack);
        return data != null && data.isLegendary();
    }

    private static boolean isDemoTrigger(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.STICK
                || stack.getItem() == Items.AMETHYST_SHARD
                || stack.getItem() == Items.NETHER_STAR) {
            return true;
        }
        ItemQualityData data = FabricTooltipHandler.readQualityDataOnly(stack);
        if (data != null) {
            QualityTier tier = data.tier();
            return tier == QualityTier.RARE || tier == QualityTier.EPIC || tier == QualityTier.LEGENDARY;
        }
        return false;
    }
}
