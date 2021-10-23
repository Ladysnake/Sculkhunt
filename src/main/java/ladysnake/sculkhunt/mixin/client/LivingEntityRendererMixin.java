package ladysnake.sculkhunt.mixin.client;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.util.ResourceTextureUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.IOException;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    private Identifier texture;

    @ModifyVariable(
            method = "getRenderLayer",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/Identifier;")
    )
    private Identifier changeTexture(Identifier originalTexture, LivingEntity livingEntity) throws IOException {
        if (SculkhuntComponents.SCULK.get(livingEntity).isSculk()) {
            if (this.texture == null) {
                String textureSize = "6464";

                if (livingEntity instanceof PlayerEntity) {
                    this.texture = new Identifier("sculkhunt", "textures/entity/sculk_overlay_6464.png");
                } else {
                    if (ResourceTextureUtil.load(MinecraftClient.getInstance().getResourceManager(), originalTexture).getWidth() == 32 && ResourceTextureUtil.load(MinecraftClient.getInstance().getResourceManager(), originalTexture).getHeight() == 32) {
                        textureSize = "3232";
                    } else if (ResourceTextureUtil.load(MinecraftClient.getInstance().getResourceManager(), originalTexture).getWidth() == 64 && ResourceTextureUtil.load(MinecraftClient.getInstance().getResourceManager(), originalTexture).getHeight() == 32) {
                        textureSize = "6432";
                    } else if (ResourceTextureUtil.load(MinecraftClient.getInstance().getResourceManager(), originalTexture).getWidth() == 64 && ResourceTextureUtil.load(MinecraftClient.getInstance().getResourceManager(), originalTexture).getHeight() == 64) {
                        textureSize = "6464";
                    } else if (ResourceTextureUtil.load(MinecraftClient.getInstance().getResourceManager(), originalTexture).getWidth() == 128 && ResourceTextureUtil.load(MinecraftClient.getInstance().getResourceManager(), originalTexture).getHeight() == 64) {
                        textureSize = "12864";
                    }

                    this.texture = new Identifier("sculkhunt", "textures/entity/sculk_overlay_" + textureSize + ".png");
                }
            }

            return this.texture;
        } else {
            return originalTexture;
        }
    }
}


