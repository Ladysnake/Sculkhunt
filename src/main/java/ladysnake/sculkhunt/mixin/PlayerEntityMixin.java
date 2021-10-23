package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
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

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo callbackInfo) {
        if ((SculkhuntComponents.SCULK.get(this).isDetected() || (this.getX() != this.prevX || this.getZ() != this.prevZ)) && !this.isSneaking() && !SculkhuntComponents.SCULK.get(this).isSculk()) {
            if (this.age % 20 == 0) {
                this.world.addParticle(Sculkhunt.SOUND, true, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 0, 0, 0);
            }
        }

        if (SculkhuntComponents.SCULK.get(this).isSculk() && this.random.nextInt(400) == 0) {
            this.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 1.0f, 1.0f);
        }
    }
}