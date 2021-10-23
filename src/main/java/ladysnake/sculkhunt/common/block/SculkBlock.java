package ladysnake.sculkhunt.common.block;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.init.SculkhuntBlocks;
import ladysnake.sculkhunt.common.init.SculkhuntDamageSources;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

public class SculkBlock extends OreBlock {
    public SculkBlock(Settings settings, UniformIntProvider experienceDropped) {
        super(settings, experienceDropped);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        super.onSteppedOn(world, pos, state, entity);

        if (entity instanceof LivingEntity) {
            if (!SculkhuntComponents.SCULK.get(entity).isSculk()) {
                if (!(entity instanceof PlayerEntity)) {
                    for (int i = 0; i < 25; i++) {
                        world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SculkhuntBlocks.SCULK)), entity.getX() + world.random.nextGaussian() / 10f, entity.getY() + world.random.nextGaussian() / 10f, entity.getZ() + world.random.nextGaussian() / 10f, world.random.nextGaussian() / 10f, world.random.nextFloat() / 5f, world.random.nextGaussian() / 10f);
                    }
                    world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BLOCK_SCULK_SENSOR_STEP, SoundCategory.NEUTRAL, 1.0f, 0.9f, true);

                    entity.damage(SculkhuntDamageSources.SCULK, 2.5f);
                }

                ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 2, false, false, false));
                if (((LivingEntity) entity).getEquippedStack(EquipmentSlot.FEET).isEmpty()) {
                    ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 2, false, false, false));
                }
            } else {
                if (!((LivingEntity) entity).hasStatusEffect(StatusEffects.REGENERATION)) {
                    ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 2, false, false, false));
                }
                ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20, 1, false, false, false));
            }
        } else {
            for (int i = 0; i < 25; i++) {
                world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SculkhuntBlocks.SCULK)), entity.getX() + world.random.nextGaussian() / 10f, entity.getY() + world.random.nextGaussian() / 10f, entity.getZ() + world.random.nextGaussian() / 10f, world.random.nextGaussian() / 10f, world.random.nextFloat() / 5f, world.random.nextGaussian() / 10f);
            }
            world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BLOCK_SCULK_SENSOR_STEP, SoundCategory.NEUTRAL, 1.0f, 0.9f, true);
            if (world.random.nextInt(50) == 0) {
                entity.kill();
            }
        }
    }
}
