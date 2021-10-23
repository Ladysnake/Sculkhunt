package ladysnake.sculkhunt.mixin;

import com.google.common.collect.ImmutableList;
import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.init.SculkhuntBlocks;
import ladysnake.sculkhunt.common.init.SculkhuntDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    @Final
    public float randomLargeSeed;
    @Shadow
    public int deathTime;
    @Shadow
    protected boolean jumping;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract ImmutableList<EntityPose> getPoses();

    @Shadow
    public abstract double getAttributeValue(EntityAttribute attribute);

    @Shadow
    public abstract boolean isInsideWall();

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource source, CallbackInfo callbackInfo) {
        if (source == SculkhuntDamageSources.SCULK) {
            LivingEntity sculkedEntity = (LivingEntity) this.getType().create(world);
            sculkedEntity.setPos(this.getX(), this.getY() - this.getHeight(), this.getZ());
            sculkedEntity.updateTrackedPosition(this.getX(), this.getY() - this.getHeight(), this.getZ());
            SculkhuntComponents.SCULK.get(sculkedEntity).setSculk(true);
            sculkedEntity.world.spawnEntity(sculkedEntity);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo callbackInfo) {
        if (SculkhuntComponents.SCULK.get(this).isDetected()) {
            SculkhuntComponents.SCULK.get(this).decrementDetectedTime();
        }

        if (SculkhuntComponents.SCULK.get(this).isSculk()) {
            // rise from sculk
            if (world.getBlockState(this.getBlockPos()).isSolidBlock(world, this.getBlockPos())) {
                if (this.isInsideWall()) {
                    this.requestTeleport(this.getX(), this.getY() + 1, this.getZ());
                }

                if (!world.isClient) {
                    this.setVelocity(0, 0.05, 0);
                    this.velocityModified = true;
                    this.velocityDirty = true;
                }
                this.fallDistance = 0f;
                for (int i = 0; i < (this.getWidth() * this.getHeight()) * 25; i++) {
                    world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SculkhuntBlocks.SCULK)), this.getX() + random.nextGaussian() * this.getWidth() / 5f, this.getY() + random.nextGaussian() * this.getHeight() / 5f, this.getZ() + random.nextGaussian() * this.getWidth() / 5f, random.nextGaussian() / 10f, random.nextFloat() / 5f, random.nextGaussian() / 10f);
                }
                this.playSound(SoundEvents.BLOCK_SCULK_SENSOR_STEP, 1.0f, 0.9f);
            }
        }

    }

    @Inject(method = "updatePostDeath", at = @At("RETURN"))
    protected void updatePostDeath(CallbackInfo callbackInfo) {
        if (SculkhuntComponents.SCULK.get(this).isSculk()) {
            this.setInvisible(true);
            for (int i = 0; i < (this.getWidth() * this.getHeight()) * 100; i++) {
                world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SculkhuntBlocks.SCULK)), this.getX() + random.nextGaussian() * this.getWidth() / 2f, (this.getY() + this.getHeight() / 2f) + random.nextGaussian() * this.getHeight() / 2f, this.getZ() + random.nextGaussian() * this.getWidth() / 2f, random.nextGaussian() / 10f, random.nextFloat() / 10f, random.nextGaussian() / 10f);
            }
            this.playSound(SoundEvents.BLOCK_SCULK_SENSOR_BREAK, 1.0f, 0.9f);
            if (deathTime >= 3) {
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
    }


    @Inject(method = "shouldDropLoot", at = @At("RETURN"), cancellable = true)
    protected void shouldDropLoot(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (SculkhuntComponents.SCULK.get(this).isSculk()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = "shouldDropXp", at = @At("RETURN"), cancellable = true)
    protected void shouldDropXp(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (SculkhuntComponents.SCULK.get(this).isSculk()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
