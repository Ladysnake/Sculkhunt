package ladysnake.sculkhunt.common;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.entity.SculkCatalystEntity;
import ladysnake.sculkhunt.common.init.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ladysnake.sculkhunt.common.init.SculkhuntGamerules.SCULK_CATALYST_TERRITORY_RADIUS;

public class Sculkhunt implements ModInitializer {
    public static final String MODID = "sculkhunt";
    public static final int SPAWN_RADIUS = 250;
    public static ArrayList<UUID> playersToTurnToSculk = new ArrayList<>();

    public static boolean isBlockReplaceable(World world, BlockPos blockPos) {
        return (world.getBlockState(blockPos).isAir() || world.getBlockState(blockPos).getMaterial() == Material.BAMBOO || world.getBlockState(blockPos).getMaterial() == Material.BAMBOO_SAPLING || world.getBlockState(blockPos).getMaterial() == Material.COBWEB || world.getBlockState(blockPos).getMaterial() == Material.FIRE || world.getBlockState(blockPos).getMaterial() == Material.CARPET || world.getBlockState(blockPos).getMaterial() == Material.CACTUS || world.getBlockState(blockPos).getMaterial() == Material.PLANT || world.getBlockState(blockPos).getMaterial() == Material.REPLACEABLE_PLANT || world.getBlockState(blockPos).getMaterial() == Material.UNDERWATER_PLANT || world.getBlockState(blockPos).getMaterial() == Material.REPLACEABLE_UNDERWATER_PLANT || world.getBlockState(blockPos.add(0, 1, 0)).getMaterial() == Material.SNOW_LAYER || world.getBlockState(blockPos).getBlock() == Blocks.WATER);
    }

    public static DefaultParticleType SOUND;

    @Override
    public void onInitialize() {
        SculkhuntItems.init();
        SculkhuntBlocks.init();
        SculkhuntBlockEntityTypes.init();
        SculkhuntEntityTypes.init();
        SculkhuntGamerules.init();

        SOUND = Registry.register(Registry.PARTICLE_TYPE, Sculkhunt.MODID + ":sound", FabricParticleTypes.simple(true));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (playersToTurnToSculk.contains(newPlayer.getUuid())) {
                SculkhuntComponents.SCULK.get(newPlayer).setSculk(true);
                playersToTurnToSculk.remove(newPlayer.getUuid());
            }

            if (SculkhuntComponents.SCULK.get(newPlayer).isSculk()) {
                newPlayer.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10f);
                newPlayer.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.12f);
                newPlayer.setHealth(newPlayer.getMaxHealth());
                newPlayer.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(4f);

                // respawn in sculk
                ServerWorld world = ((ServerWorld) newPlayer.world);

                List<ServerPlayerEntity> players = world.getPlayers(serverPlayerEntity -> !serverPlayerEntity.isCreative() && !serverPlayerEntity.isSpectator() && !SculkhuntComponents.SCULK.get(serverPlayerEntity).isSculk());
                List<SculkCatalystEntity> catalysts;

                // respawn furthest from players
                if (!players.isEmpty()) {
                    ServerPlayerEntity prey = players.get(world.random.nextInt(players.size()));
                    catalysts = world.getEntitiesByClass(SculkCatalystEntity.class, new Box(prey.getX() - SPAWN_RADIUS, prey.getY() - SPAWN_RADIUS / 2f, prey.getZ() - SPAWN_RADIUS, prey.getX() + SPAWN_RADIUS, prey.getY() + SPAWN_RADIUS / 2f, prey.getZ() + SPAWN_RADIUS), sculkCatalystEntity -> true);

                    if (!catalysts.isEmpty()) {
                        catalysts.sort((o1, o2) -> (int) (prey.getPos().distanceTo(o1.getPos()) - prey.getPos().distanceTo(o2.getPos())));
                        Vec3d newPos = catalysts.get(0).getPos().add(world.random.nextGaussian() * 5, -newPlayer.getHeight() * 2, world.random.nextGaussian() * 5);

                        newPlayer.networkHandler.requestTeleport(newPos.getX(), newPos.getY(), newPos.getZ(), newPlayer.getYaw(), newPlayer.getPitch());
                    }
                } else {
                    catalysts = world.getEntitiesByClass(SculkCatalystEntity.class, new Box(oldPlayer.getX() - SPAWN_RADIUS * 5f, oldPlayer.getY() - SPAWN_RADIUS * 5f / 2f, oldPlayer.getZ() - SPAWN_RADIUS * 5f, oldPlayer.getX() + SPAWN_RADIUS * 5f, oldPlayer.getY() + SPAWN_RADIUS * 5f / 2f, oldPlayer.getZ() + SPAWN_RADIUS * 5f), sculkCatalystEntity -> true);

                    if (!catalysts.isEmpty()) {
                        Vec3d newPos = catalysts.get(world.random.nextInt(catalysts.size())).getPos().add(world.random.nextGaussian() * 5, -newPlayer.getHeight() * 2, world.random.nextGaussian() * 5);

                        newPlayer.networkHandler.requestTeleport(newPos.getX(), newPos.getY(), newPos.getZ(), newPlayer.getYaw(), newPlayer.getPitch());
                    }
                }
            }
        });

        // spawn sculk catalysts around players
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getWorlds().forEach(world -> {
                if (world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING).get() && world.random.nextInt(world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING_DELAY).get()) == 0) {
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        if (world.getEntitiesByClass(SculkCatalystEntity.class, player.getBoundingBox().expand(world.getGameRules().get(SCULK_CATALYST_TERRITORY_RADIUS).get()), sculkCatalystEntity -> true).isEmpty()) {
                            int radius = world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING_RADIUS).get();

                            BlockPos placePos = player.getBlockPos().add(Math.round(world.random.nextGaussian() * radius), 100, Math.round(world.random.nextGaussian() * radius));

                            while (placePos.getY() > 1 &&
                                    !(world.getBlockState(placePos.add(0, -1, 0)).isSolidBlock(world, placePos.add(0, -1, 0))
                                            && isBlockReplaceable(world, placePos) && isBlockReplaceable(world, placePos.add(0, 1, 0)))) {
                                placePos = placePos.add(0, -1, 0);
                            }

                            if (world.getBlockState(placePos.add(0, -1, 0)).isSolidBlock(world, placePos.add(0, -1, 0))
                                    && isBlockReplaceable(world, placePos)) {
                                SculkCatalystEntity sculkCatalystEntity = SculkhuntEntityTypes.SCULK_CATALYST.create(world);
                                sculkCatalystEntity.refreshPositionAndAngles(placePos.getX() + .5f, placePos.getY(), placePos.getZ() + .5f, 0, 0);
                                world.spawnEntity(sculkCatalystEntity);
                                world.setBlockState(placePos, Blocks.AIR.getDefaultState());
                            }
                        }
                    }
                }
            });
        });
    }
}
