package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class SoundEntityMixin {
    @Shadow
    public World world;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract SoundCategory getSoundCategory();

    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
    public void playSound(SoundEvent sound, float volume, float pitch, CallbackInfo callbackInfo) {
        if ((Object) this instanceof MobEntity) {
            if (SculkhuntComponents.SCULK.get(this).isSculk()) {
                MobEntity mobEntity = ((MobEntity) (Object) this);
                if (((LivingEntitySoundAccessor) mobEntity).accessDeathSound() == sound) {
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, this.getSoundCategory(), volume, pitch);
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BREATH, this.getSoundCategory(), volume, pitch / 2);
                    callbackInfo.cancel();
                }
                if (((LivingEntitySoundAccessor) mobEntity).accessHurtSound(DamageSource.GENERIC) == sound) {
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, this.getSoundCategory(), volume, pitch);
                    callbackInfo.cancel();
                }
                if (((MobEntitySoundAccessor) mobEntity).accessAmbientSound() == sound) {
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BREATH, this.getSoundCategory(), volume, pitch);
                    callbackInfo.cancel();
                }
                if (((LivingEntitySoundAccessor) mobEntity).accessHurtSound(DamageSource.GENERIC) == sound) {
                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, this.getSoundCategory(), volume, pitch);
                    callbackInfo.cancel();
                }
            }
        }
    }
}
