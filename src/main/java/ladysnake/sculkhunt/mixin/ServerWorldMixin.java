package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow
    public abstract <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed);

    @Inject(method = "emitGameEvent", at = @At("HEAD"))
    public void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos, CallbackInfo callbackInfo) {
        if ((entity instanceof LivingEntity && !SculkhuntComponents.SCULK.get(entity).isSculk()) || entity == null) {
            if (event == GameEvent.PROJECTILE_LAND || event == GameEvent.DRINKING_FINISH || event == GameEvent.EAT || event == GameEvent.EXPLODE || event == GameEvent.PISTON_CONTRACT || event == GameEvent.PISTON_EXTEND || event == GameEvent.LIGHTNING_STRIKE || event == GameEvent.MINECART_MOVING || event == GameEvent.RAVAGER_ROAR || event == GameEvent.RING_BELL || event == GameEvent.BLOCK_OPEN || event == GameEvent.CONTAINER_OPEN) {
                this.spawnParticles(Sculkhunt.SOUND, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 1, 0, 0, 0, 0);
            }

            if ((event == GameEvent.BLOCK_DESTROY || event == GameEvent.BLOCK_PLACE) && entity instanceof PlayerEntity && !SculkhuntComponents.SCULK.get(entity).isSculk()) {
                Item usedItem = ((PlayerEntity) entity).getMainHandStack().getItem();

                if (usedItem instanceof ToolItem && ((ToolItem)usedItem).getMaterial() != ToolMaterials.WOOD) {
                    this.spawnParticles(Sculkhunt.SOUND, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 1, 0, 0, 0, 0);
                }
            }
        }
    }
}
