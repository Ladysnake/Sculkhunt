package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    protected abstract boolean sendToPlayerIfNearby(ServerPlayerEntity player, boolean force, double x, double y, double z, Packet<?> packet);

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Inject(method = "emitGameEvent", at = @At("HEAD"))
    public void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos, CallbackInfo callbackInfo) {
        if ((entity instanceof LivingEntity && !SculkhuntComponents.SCULK.get(entity).isSculk()) || entity == null) {
            if (event == GameEvent.PROJECTILE_LAND || event == GameEvent.DRINKING_FINISH || (event == GameEvent.EAT && entity instanceof PlayerEntity) || event == GameEvent.EXPLODE || event == GameEvent.PISTON_CONTRACT || event == GameEvent.PISTON_EXTEND || event == GameEvent.LIGHTNING_STRIKE || event == GameEvent.MINECART_MOVING || event == GameEvent.RAVAGER_ROAR || event == GameEvent.BLOCK_OPEN || event == GameEvent.CONTAINER_OPEN || event == GameEvent.RING_BELL) {
                Packet<?> packet = new ParticleS2CPacket(Sculkhunt.SOUND, true, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 0, 0, 0, 0, 1);
                for (ServerPlayerEntity player : this.getPlayers()) {
                    this.sendToPlayerIfNearby(player, true, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, packet);
                }
            }

            if ((event == GameEvent.BLOCK_DESTROY || event == GameEvent.BLOCK_PLACE) && entity instanceof PlayerEntity && !SculkhuntComponents.SCULK.get(entity).isSculk()) {
                Item usedItem = ((PlayerEntity) entity).getMainHandStack().getItem();

                if (usedItem instanceof ToolItem && ((ToolItem) usedItem).getMaterial() != ToolMaterials.WOOD) {
                    Packet<?> packet = new ParticleS2CPacket(Sculkhunt.SOUND, true, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 0, 0, 0, 0, 1);
                    for (ServerPlayerEntity player : this.getPlayers()) {
                        this.sendToPlayerIfNearby(player, true, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, packet);
                    }
                }
            }

            // bells make multiple random reverberations
            if (event == GameEvent.RING_BELL) {
                Random random = new Random();
                for (int i = 0; i < 25; i++) {
                    BlockPos randomPos = pos.add(random.nextGaussian()*30, random.nextGaussian()*30 ,random.nextGaussian()*30);
                    Packet<?> packet = new ParticleS2CPacket(Sculkhunt.SOUND, true, randomPos.getX(), randomPos.getY(), randomPos.getZ(), 0, 0, 0, 0, 1);
                    for (ServerPlayerEntity player : this.getPlayers()) {
                        this.sendToPlayerIfNearby(player, true, randomPos.getX(), randomPos.getY(), randomPos.getZ(), packet);
                    }
                }
            }
        }
    }
}
