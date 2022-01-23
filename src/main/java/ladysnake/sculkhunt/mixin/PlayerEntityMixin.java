package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.init.SculkhuntBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    private final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(20.0D);
    @Shadow
    protected boolean isSubmergedInWater;
    @Shadow
    @Final
    private PlayerInventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true)
    public void isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (SculkhuntComponents.SCULK.get(this).isSculk() && damageSource.equals(DamageSource.IN_WALL)) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Shadow
    public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    public abstract PlayerInventory getInventory();

    @Shadow
    public abstract boolean isCreative();

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo callbackInfo) {
        // particles for non sneaky moving players and detected players
        if (SculkhuntComponents.SCULK.get(this).isDetected() || ((this.getX() != this.prevX || this.getY() != this.prevY || this.getZ() != this.prevZ) && (!this.isSneaking() && !this.isSubmergedInWater() && !(this.isTouchingWater() && this.isOnGround())) && !SculkhuntComponents.SCULK.get(this).isSculk())) {
            if (this.age % 20 == 0) {
                this.world.addParticle(Sculkhunt.SOUND, true, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 0, 0, 0);
            }
        }

        // emit smell particles to indicate general area
//        if (!SculkhuntComponents.SCULK.get(this).isSculk()) {
//            for (int i = 0; i < 20; i++) {
//                world.addParticle(ParticleTypes.FIREWORK, this.getX() + random.nextGaussian() * 15, this.getY() + random.nextGaussian() * 15, this.getZ() + random.nextGaussian() * 15, 0, 0, 0);
//            }
//        }

        // clear sculk inventory of undesired items
        if (!this.isCreative() && SculkhuntComponents.SCULK.get(this).isSculk()) {
            if (this.getMainHandStack().getItem() != SculkhuntBlocks.SCULK.asItem()) {
                this.dropStack(this.getMainHandStack());
                this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }

            if (this.getOffHandStack().getItem() != SculkhuntBlocks.SCULK.asItem()) {
                this.dropStack(this.getOffHandStack());
                this.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            }
        }

        // particles for people who hide too well, particle no matter what every 30 second
//        if (!SculkhuntComponents.SCULK.get(this).isSculk()) {
//            if (this.age % 600 == 0) {
//                this.world.addParticle(Sculkhunt.SOUND, true, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 0, 0, 0);
//            }
//        }

        // ambient sculk breathing
        if (SculkhuntComponents.SCULK.get(this).isSculk() && this.random.nextInt(400) == 0) {
            this.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 1.0f, 1.0f);
        }

        // stronger the more there are players 16 blocks around the sculk tracker
        // rate of 2 hearts of damage per player above 1 player
        if (SculkhuntComponents.SCULK.get(this).isSculk() && this.age % 20 == 0) {
            int amountOfPlayersAround = (int) world.getPlayers(PLAYERS_IN_RANGE_PREDICATE, this, this.getBoundingBox().expand(16.0D, 16.0D, 16.0D)).stream().filter(playerEntity -> !SculkhuntComponents.SCULK.get(playerEntity).isSculk()).count();
            // always have at least 4 damage points
//            float baseDamage = 4f;
//            this.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(baseDamage + 2f * Math.max(0, amountOfPlayersAround - 1));

            // give dolphin grace
            if (this.isSwimming()) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 60, 0, false, false, false));
            }
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
        if (SculkhuntComponents.SCULK.get(this).isSculk() && !SculkhuntComponents.SCULK.get(player).isSculk() && ) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = "collideWithEntity", at = @At("TAIL"), cancellable = true)
    public void collideWithEntity(Entity entity, CallbackInfo callbackInfo) {
        if (!SculkhuntComponents.SCULK.get(this).isSculk() && entity instanceof PlayerEntity && !SculkhuntComponents.SCULK.get(entity).isSculk()) {
            SculkhuntComponents.SCULK.get(this).setDetectedTime(40);
        }
    }
}