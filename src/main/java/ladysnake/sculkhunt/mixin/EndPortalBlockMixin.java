package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo callbackInfo) {
        if (world instanceof ServerWorld && Sculkhunt.sculkhuntPhase != 0 && entity instanceof ServerPlayerEntity) {
            if (!SculkhuntComponents.SCULK.get(entity).isSculk()) {
                ((ServerPlayerEntity) entity).changeGameMode(GameMode.SPECTATOR);

                for (ServerPlayerEntity player : ((ServerWorld) world).getPlayers()) {
                    player.sendMessage(new LiteralText(entity.getEntityName() + " made it out!").setStyle(Style.EMPTY.withColor(Formatting.AQUA)), false);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, player.getX(), player.getY(), player.getZ(), 1.0F, 1.5F));
                }

            }

            callbackInfo.cancel();
        }
    }
}
