package ladysnake.sculkhunt.client;

import ladysnake.sculkhunt.client.particle.SoundParticle;
import ladysnake.sculkhunt.client.render.SculkCatalystEntityRenderer;
import ladysnake.sculkhunt.client.render.entity.model.SculkCatalystEntityModel;
import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.init.SculkhuntBlocks;
import ladysnake.sculkhunt.common.init.SculkhuntEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class SculkhuntClient implements ClientModInitializer {
    public static final EntityModelLayer SCULK_CATALYST_MODEl_LAYER = new EntityModelLayer(new Identifier(Sculkhunt.MODID, "sculk_catalyst"), "main");

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(SculkhuntBlocks.SCULK_VEIN, RenderLayer.getCutout());

        EntityModelLayerRegistry.registerModelLayer(SCULK_CATALYST_MODEl_LAYER, SculkCatalystEntityModel::getTexturedModelData);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SculkhuntEntityTypes.SCULK_CATALYST, SculkCatalystEntityRenderer::new);

        ParticleFactoryRegistry.getInstance().register(Sculkhunt.SOUND, SoundParticle.DefaultFactory::new);

    }

}
