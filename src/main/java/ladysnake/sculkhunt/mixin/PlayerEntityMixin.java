package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Collectors;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(20.0D);

    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true)
    public void isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (SculkhuntComponents.SCULK.get(this).isSculk() && damageSource.equals(DamageSource.IN_WALL)) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Shadow
    public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo callbackInfo) {
        // particles for non sneaky moving players and detected players
        if ((SculkhuntComponents.SCULK.get(this).isDetected() || (this.getX() != this.prevX || this.getZ() != this.prevZ)) && !this.isSneaking() && !SculkhuntComponents.SCULK.get(this).isSculk()) {
            if (this.age % 20 == 0) {
                this.world.addParticle(Sculkhunt.SOUND, true, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 0, 0, 0);
            }
        }

        // particles for people who hide too well
        if (!SculkhuntComponents.SCULK.get(this).isSculk()) {
            if (this.age % 600 == 0) {
                this.world.addParticle(Sculkhunt.SOUND, true, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 0, 0, 0);
            }
        }

        // ambient sculk breathing
        if (SculkhuntComponents.SCULK.get(this).isSculk() && this.random.nextInt(400) == 0) {
            this.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 1.0f, 1.0f);
        }

        // stronger the more there are players 16 blocks around the sculk tracker
        // rate of 2 hearts of damage per player above 1 player
        if (SculkhuntComponents.SCULK.get(this).isSculk() && this.age % 20 == 0) {
            int amountOfPlayersAround = (int) world.getPlayers(PLAYERS_IN_RANGE_PREDICATE, this, this.getBoundingBox().expand(16.0D, 16.0D, 16.0D)).stream().filter(playerEntity -> !SculkhuntComponents.SCULK.get(playerEntity).isSculk()).count();
            // always have at least 4 damage points
            if (amountOfPlayersAround == 0) {
                amountOfPlayersAround = 1;
            }

            this.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(4f * amountOfPlayersAround);
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    public void getBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        if (SculkhuntComponents.SCULK.get(this).isSculk()) {
            callbackInfoReturnable.setReturnValue(callbackInfoReturnable.getReturnValue() * 5f);
        }
    }

    @Inject(method = "shouldDamagePlayer", at = @At("RETURN"), cancellable = true)
    public void shouldDamagePlayer(PlayerEntity player, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (SculkhuntComponents.SCULK.get(this).isSculk() && SculkhuntComponents.SCULK.get(player).isSculk()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}