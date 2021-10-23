/// Made with Model Converter by Globox_Z
/// Generate all required imports
/// Made with Blockbench 3.8.4
/// Exported for Minecraft version 1.15
/// Paste this class into your mod and generate all required imports
package ladysnake.sculkhunt.client.render.entity.model;

import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class SculkCatalystEntityModel<T extends Entity> extends EntityModel<T> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier(Sculkhunt.MODID, "sculk_catalyst"), "main");

    private final ModelPart catalyst;

    public SculkCatalystEntityModel(ModelPart root) {
        super();
        this.catalyst = root.getChild("catalyst");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("catalyst", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                ModelTransform.pivot(0.0F, 0f, 0.0F)
        );
        return TexturedModelData.of(modelData, 64, 32);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        catalyst.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }
}