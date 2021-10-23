package ladysnake.sculkhunt.client.render;

import ladysnake.sculkhunt.client.SculkhuntClient;
import ladysnake.sculkhunt.client.render.entity.model.SculkCatalystEntityModel;
import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.entity.SculkCatalystEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SculkCatalystEntityRenderer extends EntityRenderer<SculkCatalystEntity> {
    private static final Identifier TEXTURE = new Identifier(Sculkhunt.MODID, "textures/entity/sculk_catalyst.png");
    private SculkCatalystEntityModel<SculkCatalystEntity> model;

    public SculkCatalystEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);

        this.shadowRadius = 0;
        this.shadowOpacity = 0;

        this.model = new SculkCatalystEntityModel<>(ctx.getPart(SculkhuntClient.SCULK_CATALYST_MODEl_LAYER));
    }

    public void render(SculkCatalystEntity sculkCatalystEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.scale(1, -1, 1);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer2 = immediate.getBuffer(RenderLayer.getEntityCutoutNoCull(this.getTexture(sculkCatalystEntity)));
        this.model.render(matrixStack, vertexConsumer2, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0f);
        immediate.draw();

        super.render(sculkCatalystEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(SculkCatalystEntity entity) {
        if (entity.getBloomingPhase() == -1) {
            return TEXTURE;
        } else {
            return new Identifier(Sculkhunt.MODID, "textures/entity/sculk_catalyst_blooming_" + entity.getBloomingPhase() + ".png");
        }
    }
}