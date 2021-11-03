package ladysnake.sculkhunt.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntity {
    @Shadow @Final public MinecraftServer server;

    protected ServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource source, CallbackInfo callbackInfo) {
        if (Sculkhunt.sculkhuntPhase == 2) {
            Sculkhunt.playersToTurnToSculk.add(this.getUuid());

            if (!SculkhuntComponents.SCULK.get(this).isSculk()) {
                for (ServerWorld serverWorld : server.getWorlds()) {
                    for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                        Text message = new LiteralText(this.getEntityName()+" joined the sculk...").setStyle(Style.EMPTY.withColor(Formatting.DARK_RED));
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, player.getX(), player.getY(), player.getZ(), 1.0F, 1.5F));
                        player.sendMessage(message, false);
                    }
                }
            }
        }
    }


}
