package ladysnake.sculkhunt.common.block;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.init.SculkhuntBlocks;
import ladysnake.sculkhunt.common.init.SculkhuntDamageSources;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.OreBlock;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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

                    entity.damage(SculkhuntDamageSources.SCULK, 2.0f);
                }

                ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 1, false, false, false));
                if (((LivingEntity) entity).getEquippedStack(EquipmentSlot.FEET).isEmpty()) {
                    ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, false, false, false));
                }
            } else {
                if (!((LivingEntity) entity).hasStatusEffect(StatusEffects.REGENERATION)) {
                    ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 1, false, false, false));
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

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        super.afterBreak(world, player, pos, state, blockEntity, stack);

        if (!world.isClient && SculkhuntComponents.SCULK.get(player).isSculk()) {
            world.setBlockState(pos, SculkhuntBlocks.SCULK_VEIN.getDefaultState().with(ConnectingBlock.FACING_PROPERTIES.get(Direction.DOWN), true).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.UP), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.NORTH), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.SOUTH), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.EAST), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.WEST), false));
            player.getInventory().insertStack(new ItemStack(SculkhuntBlocks.SCULK, 1));
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (SculkhuntComponents.SCULK.get(player).isSculk() && world.getBlockState(player.getBlockPos().add(0, -1, 0)).getBlock() == SculkhuntBlocks.SCULK) {
            for (int i = 0; i < (player.getWidth() * player.getHeight()) * 100; i++) {
                world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SculkhuntBlocks.SCULK)), player.getX() + player.getRandom().nextGaussian() * player.getWidth() / 2f, (player.getY() + player.getHeight() / 2f) + player.getRandom().nextGaussian() * player.getHeight() / 2f, player.getZ() + player.getRandom().nextGaussian() * player.getWidth() / 2f, player.getRandom().nextGaussian() / 10f, player.getRandom().nextFloat() / 10f, player.getRandom().nextGaussian() / 10f);
            }
            player.playSound(SoundEvents.BLOCK_SCULK_SENSOR_BREAK, 1.0f, 0.9f);
            player.setPosition(pos.getX()+.5, pos.getY(), pos.getZ()+.5);
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }
}
