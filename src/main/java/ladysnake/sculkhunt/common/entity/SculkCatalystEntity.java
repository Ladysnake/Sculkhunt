package ladysnake.sculkhunt.common.entity;

import com.google.common.collect.Lists;
import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.block.SculkVeinBlock;
import ladysnake.sculkhunt.common.init.SculkhuntBlocks;
import ladysnake.sculkhunt.common.init.SculkhuntDrops;
import ladysnake.sculkhunt.common.init.SculkhuntGamerules;
import ladysnake.sculkhunt.common.init.SculkhuntItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static ladysnake.sculkhunt.common.Sculkhunt.SPAWN_RADIUS;

public class SculkCatalystEntity extends Entity {
    private static final TrackedData<Integer> BLOOMING_PHASE = DataTracker.registerData(SculkCatalystEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final List<Sculk> sculks = Lists.newArrayList();
    private int bloomCounter = 0;
    private int incapacitatedTimer = 0;

    public SculkCatalystEntity(EntityType<SculkCatalystEntity> type, World world) {
        super(type, world);
    }

    public boolean canPlaceSculkAt(BlockPos blockPos) {
        return (world.getBlockState(blockPos.add(0, 1, 0)).getBlock() == SculkhuntBlocks.SCULK_VEIN || world.getBlockState(blockPos.add(0, 1, 0)).isAir() || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.BAMBOO || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.BAMBOO_SAPLING || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.COBWEB || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.FIRE || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.CARPET || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.CACTUS || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.PLANT || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.REPLACEABLE_PLANT || world.getBlockState(blockPos).getMaterial() == Material.SNOW_LAYER || world.getBlockState(blockPos.add(0, 1, 0)).getBlock() == Blocks.WATER)
                && world.getBlockState(blockPos).isSolidBlock(world, blockPos)
                && blockPos != this.getBlockPos()
                && this.random.nextInt(5) == 0
                && world.getBlockState(blockPos).getMaterial() != Material.SCULK;
    }

    public boolean canPlaceVeinAt(BlockPos blockPos) {
        return (world.getBlockState(blockPos).isAir() || world.getBlockState(blockPos).getMaterial() == Material.BAMBOO || world.getBlockState(blockPos).getMaterial() == Material.BAMBOO_SAPLING || world.getBlockState(blockPos).getMaterial() == Material.COBWEB || world.getBlockState(blockPos).getMaterial() == Material.FIRE || world.getBlockState(blockPos).getMaterial() == Material.CARPET || world.getBlockState(blockPos).getMaterial() == Material.CACTUS || world.getBlockState(blockPos).getMaterial() == Material.PLANT || world.getBlockState(blockPos).getMaterial() == Material.REPLACEABLE_PLANT || world.getBlockState(blockPos).getMaterial() == Material.REPLACEABLE_UNDERWATER_PLANT || world.getBlockState(blockPos).getMaterial() == Material.SNOW_LAYER || world.getBlockState(blockPos).getBlock() == Blocks.WATER)
                && (world.getBlockState(blockPos.add(0, -1, 0)).isSolidBlock(world, blockPos))
                && blockPos != this.getBlockPos()
                && world.getBlockState(blockPos.add(0, -1, 0)).getMaterial() != Material.SCULK;
    }

    public BlockState getBlockStateForVein(BlockPos blockPos) {
        BlockState sculkVein = SculkhuntBlocks.SCULK_VEIN.getDefaultState();

        Direction[] directions = {
                Direction.DOWN,
                Direction.UP,
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        };
        Vec3i[] pos = {
                new Vec3i(0, -1, 0),
                new Vec3i(0, 1, 0),
                new Vec3i(-1, 0, 0),
                new Vec3i(1, 0, 0),
                new Vec3i(0, 0, -1),
                new Vec3i(0, 0, 1),
        };
        for (int i = 0; i < directions.length; i++) {
            if (!world.getBlockState(blockPos.add(pos[i])).isSolidBlock(world, blockPos.add(pos[i]))) {
                sculkVein = sculkVein.with(ConnectingBlock.FACING_PROPERTIES.get(directions[i]), false);
            }
        }

        return sculkVein;
    }

    @Override
    public boolean collidesWith(Entity other) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        boolean hasSpread = false;
        if (!this.world.isClient) {
            if (this.isIncapacitated()) {
                this.incapacitatedTimer--;

                if (this.age % 5 == 0) {
                    ((ServerWorld) this.world).playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_SCULK_SENSOR_BREAK, SoundCategory.BLOCKS, 1.0f, 1.5f, false);
                    ((ServerWorld) this.world).spawnParticles(Sculkhunt.SOUND, this.getX(), this.getY() + .5f, this.getZ(), 1, 0, 0, 0, 0);
                }
            } else if (this.sculks.isEmpty() || (world.random.nextInt(Math.max(1, world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_BLOOM_DELAY).get())) == 0 && getBloomingPhase() == -1)) {
                for (int i = 0; i < world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_BLOOM_RADIUS).get(); i++) {
                    if (this.sculks.isEmpty()) {
                        BlockPos blockPos = this.getBlockPos().add(0, -1, 0);

                        this.sculks.add(new Sculk(blockPos, world.getBlockState(blockPos)));
                        hasSpread = true;
                        world.setBlockState(blockPos, SculkhuntBlocks.SCULK.getDefaultState());

                        for (int x = -1; x <= 1; x++) {
                            for (int y = -1; y <= 1; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    BlockPos placePos = blockPos.add(x, y, z);

                                    if ((x != 0 ^ y != 0 ^ z != 0) && canPlaceVeinAt(placePos)) {
                                        this.sculks.add(new Sculk(placePos, world.getBlockState(placePos)));
                                        world.setBlockState(placePos, SculkhuntBlocks.SCULK_VEIN.getDefaultState().with(SculkVeinBlock.WATERLOGGED, world.getBlockState(placePos).getFluidState().getFluid().equals(Fluids.WATER)).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.DOWN), true).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.UP), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.NORTH), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.SOUTH), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.EAST), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.WEST), false));
                                    }
                                }
                            }
                        }
                    } else {
                        Sculk sculk = this.sculks.get(random.nextInt(this.sculks.size()));
                        BlockPos blockPos = NbtHelper.toBlockPos(sculk.blockPos);

                        if (world.getBlockState(blockPos).getBlock() == SculkhuntBlocks.SCULK_VEIN) {
                            this.sculks.remove(sculk);
                            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());

                            this.sculks.add(new Sculk(blockPos.add(0, -1, 0), world.getBlockState(blockPos.add(0, -1, 0))));
                            world.setBlockState(blockPos.add(0, -1, 0), SculkhuntBlocks.SCULK.getDefaultState());
                            if (random.nextInt(100) == 0) {
                                this.sculks.add(new Sculk(blockPos, world.getBlockState(blockPos)));
                                world.setBlockState(blockPos, Blocks.SCULK_SENSOR.getDefaultState());
                            }

                            for (int x = -1; x <= 1; x++) {
                                for (int y = -1; y <= 1; y++) {
                                    for (int z = -1; z <= 1; z++) {
                                        BlockPos placePos = blockPos.add(x, y, z);

                                        if ((x != 0 ^ y != 0 ^ z != 0) && canPlaceVeinAt(placePos)) {
                                            this.sculks.add(new Sculk(placePos, world.getBlockState(placePos)));
                                            hasSpread = true;
                                            world.setBlockState(placePos, SculkhuntBlocks.SCULK_VEIN.getDefaultState().with(SculkVeinBlock.WATERLOGGED, world.getBlockState(placePos).getFluidState().getFluid().equals(Fluids.WATER)).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.DOWN), true).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.UP), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.NORTH), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.SOUTH), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.EAST), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.WEST), false));
                                        }
                                    }
                                }
                            }
                        } else if (world.getBlockState(blockPos).getBlock() == SculkhuntBlocks.SCULK) {
                            if (this.random.nextInt(world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_MOB_SPAWN_FREQUENCY).get()) == 0) {
                                Entity entity;
                                switch (random.nextInt(4)) {
                                    case 0:
                                        entity = EntityType.CREEPER.create(world);
                                        break;
                                    case 1:
                                        entity = EntityType.SPIDER.create(world);
                                        break;
                                    case 2:
                                        entity = EntityType.SKELETON.create(world);
                                        break;
                                    default:
                                        entity = EntityType.ZOMBIE.create(world);
                                }

                                entity.setPos(blockPos.getX(), blockPos.getY() - entity.getHeight(), blockPos.getZ());
                                entity.updateTrackedPosition(blockPos.getX(), blockPos.getY() - entity.getHeight(), blockPos.getZ());
                                SculkhuntComponents.SCULK.get(entity).setSculk(true);
                                this.world.spawnEntity(entity);
                            }
                        }
                    }
                }
            }
        }

        if (hasSpread) {
            this.setBloomingPhase(0);
            this.playSound(SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, 1.0f, 1.0f);
        }

        if (this.getBloomingPhase() != -1) {
            if (this.getBloomingPhase() >= 11) {
                this.setBloomingPhase(-1);
            } else {
                this.setBloomingPhase(this.getBloomingPhase() + 1);
            }
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (SculkhuntComponents.SCULK.get(player).isSculk() && !this.isIncapacitated()) {
            for (int i = 0; i < (player.getWidth() * player.getHeight()) * 100; i++) {
                world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SculkhuntBlocks.SCULK)), player.getX() + player.getRandom().nextGaussian() * player.getWidth() / 2f, (player.getY() + player.getHeight() / 2f) + player.getRandom().nextGaussian() * player.getHeight() / 2f, player.getZ() + player.getRandom().nextGaussian() * player.getWidth() / 2f, player.getRandom().nextGaussian() / 10f, player.getRandom().nextFloat() / 10f, player.getRandom().nextGaussian() / 10f);
            }
            player.playSound(SoundEvents.BLOCK_SCULK_SENSOR_BREAK, 1.0f, 0.9f);

            if (!this.world.isClient) {
                // respawn in sculk
                ServerWorld world = (ServerWorld) player.world;

                List<ServerPlayerEntity> players = world.getPlayers(serverPlayerEntity -> !serverPlayerEntity.isCreative() && !serverPlayerEntity.isSpectator() && !SculkhuntComponents.SCULK.get(serverPlayerEntity).isSculk());
                List<SculkCatalystEntity> catalysts;

                if (!players.isEmpty()) {
                    ServerPlayerEntity prey = players.get(world.random.nextInt(players.size()));
                    catalysts = world.getEntitiesByClass(SculkCatalystEntity.class, new Box(prey.getX() - SPAWN_RADIUS, prey.getY() - SPAWN_RADIUS / 2f, prey.getZ() - SPAWN_RADIUS, prey.getX() + SPAWN_RADIUS, prey.getY() + SPAWN_RADIUS / 2f, prey.getZ() + SPAWN_RADIUS), sculkCatalystEntity -> !sculkCatalystEntity.isIncapacitated());

                    if (!catalysts.isEmpty()) {
                        catalysts.sort((o1, o2) -> (int) (prey.getPos().distanceTo(o1.getPos()) - prey.getPos().distanceTo(o2.getPos())));
                        Vec3d newPos = catalysts.get(0).getPos().add(world.random.nextGaussian() * 2, -player.getHeight() * 2, world.random.nextGaussian() * 2);

                        ((ServerPlayerEntity) player).networkHandler.requestTeleport(newPos.getX(), newPos.getY(), newPos.getZ(), player.getYaw(), player.getPitch());
                        player.setHealth(0.1f);
                    }
                } else {
                    catalysts = world.getEntitiesByClass(SculkCatalystEntity.class, new Box(player.getX() - SPAWN_RADIUS * 5f, player.getY() - SPAWN_RADIUS * 5f / 2f, player.getZ() - SPAWN_RADIUS * 5f, player.getX() + SPAWN_RADIUS * 5f, player.getY() + SPAWN_RADIUS * 5f / 2f, player.getZ() + SPAWN_RADIUS * 5f), sculkCatalystEntity -> !sculkCatalystEntity.isIncapacitated());

                    if (!catalysts.isEmpty()) {
                        Vec3d newPos = catalysts.get(world.random.nextInt(catalysts.size())).getPos().add(world.random.nextGaussian() * 2, -player.getHeight() * 2, world.random.nextGaussian() * 2);

                        ((ServerPlayerEntity) player).networkHandler.requestTeleport(newPos.getX(), newPos.getY(), newPos.getZ(), player.getYaw(), player.getPitch());
                        player.setHealth(0.1f);
                    }
                }
            }
        }

        return super.interact(player, hand);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(SculkhuntBlocks.SCULK_CATALYST);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(BLOOMING_PHASE, -1);
    }

    public int getBloomingPhase() {
        return this.dataTracker.get(BLOOMING_PHASE);
    }

    public void setBloomingPhase(int bloomingPhase) {
        this.dataTracker.set(BLOOMING_PHASE, bloomingPhase);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.bloomCounter = nbt.getInt("BloomCounter");

        this.sculks.clear();
        NbtList nbtList = nbt.getList("Sculks", 10);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            Sculk sculk = new Sculk(NbtHelper.toBlockPos(nbtCompound.getCompound("BlockPos")), NbtHelper.toBlockState(nbtCompound.getCompound("BlockState")));
            this.sculks.add(sculk);
        }
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putInt("BloomCounter", this.bloomCounter);
        nbt.put("Sculks", this.getSculks());

        return nbt;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    public boolean collides() {
        return true;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public void animateDamage() {
        ((ServerWorld) world).spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SculkhuntBlocks.SCULK_CATALYST)), this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5, 100, random.nextGaussian() / 5f, random.nextGaussian() / 5f, random.nextGaussian() / 5f, 0.15);
        ((ServerWorld) world).playSoundFromEntity(null, this, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    public boolean isIncapacitated() {
        return this.incapacitatedTimer > 0;
    }

    public boolean damage(DamageSource source, float amount) {
        if (!this.isRemoved() && !this.world.isClient) {
            this.incapacitatedTimer = 200; // incapacitate for 10 seconds

            int amountOfSculkToRemove = Math.round(amount * amount);
            List<Sculk> newSculk = new ArrayList<>();

            if ((amountOfSculkToRemove >= this.sculks.size() || this.sculks.isEmpty())) {
                this.kill();
            } else {
                for (int i = 0; i < amountOfSculkToRemove; i++) {
                    BlockState blockState = NbtHelper.toBlockState(this.sculks.get(this.sculks.size() - 1).blockstate);
                    BlockPos blockPos = NbtHelper.toBlockPos(this.sculks.get(this.sculks.size() - 1).blockPos);
                    if (world.getBlockState(blockPos).getBlock() == SculkhuntBlocks.SCULK) {
                        this.sculks.remove(this.sculks.size() - 1);
                        world.breakBlock(blockPos, false);
                        world.setBlockState(blockPos, blockState);

                        newSculk.add(new Sculk(blockPos.add(0, 1, 0), world.getBlockState(blockPos.add(0, 1, 0))));

                        world.breakBlock(blockPos.add(0, 1, 0), false);
                        world.setBlockState(blockPos.add(0, 1, 0), SculkhuntBlocks.SCULK_VEIN.getDefaultState().with(ConnectingBlock.FACING_PROPERTIES.get(Direction.DOWN), true).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.UP), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.NORTH), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.SOUTH), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.EAST), false).with(ConnectingBlock.FACING_PROPERTIES.get(Direction.WEST), false));
                    } else if (world.getBlockState(blockPos).getBlock() == SculkhuntBlocks.SCULK_VEIN || world.getBlockState(blockPos).getBlock() == Blocks.SCULK_SENSOR) {
                        world.breakBlock(blockPos, false);
                        world.setBlockState(blockPos, blockState);
                        this.sculks.remove(this.sculks.size() - 1);
                    } else {
                        this.sculks.remove(this.sculks.size() - 1);
                    }
                }
            }

            sculks.addAll(newSculk);

            return true;
        } else {
            return false;
        }
    }

    public void kill() {
        if (!this.isRemoved() && !this.world.isClient) {
            for (Sculk sculk : sculks) {
                BlockState blockState = NbtHelper.toBlockState(sculk.blockstate);
                BlockPos blockPos = NbtHelper.toBlockPos(sculk.blockPos);
                if (world.getBlockState(blockPos).getBlock() == SculkhuntBlocks.SCULK || world.getBlockState(blockPos).getBlock() == SculkhuntBlocks.SCULK_VEIN || world.getBlockState(blockPos).getBlock() == Blocks.SCULK_SENSOR) {
                    world.breakBlock(blockPos, false);
                    world.setBlockState(blockPos, blockState);
                }
            }

            ((ServerWorld) world).spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SculkhuntBlocks.SCULK_CATALYST)), this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5, 100, random.nextGaussian() / 5f, random.nextGaussian() / 5f, random.nextGaussian() / 5f, 0.15);
            ((ServerWorld) world).playSoundFromEntity(null, this, SoundEvents.BLOCK_SCULK_SENSOR_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);

            ItemEntity droppedItem = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), SculkhuntDrops.getRandomDrop(random));
            droppedItem.setVelocity(random.nextGaussian() / 10f, random.nextFloat() / 3f, random.nextGaussian() / 10f);
            world.spawnEntity(droppedItem);

//            if (random.nextInt(3) == 0) {
            ItemEntity droppedEye = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(SculkhuntItems.SCULK_EYE, 1));
            droppedEye.setVelocity(random.nextGaussian() / 10f, random.nextFloat() / 3f, random.nextGaussian() / 10f);
            world.spawnEntity(droppedEye);
//            }

            super.kill();
        }
    }


    public NbtList getSculks() {
        NbtList nbtList = new NbtList();
        for (Sculk sculk : sculks) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.put("BlockPos", sculk.blockPos);
            nbtCompound.put("BlockState", sculk.blockstate);
            nbtList.add(nbtCompound);
        }

        return nbtList;
    }

    private static class Sculk {
        NbtCompound blockPos;
        NbtCompound blockstate;

        Sculk(BlockPos blockPos, BlockState blockState) {
            this.blockPos = NbtHelper.fromBlockPos(blockPos);
            this.blockstate = NbtHelper.fromBlockState(blockState);
        }
    }
}
