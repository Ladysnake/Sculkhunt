package ladysnake.sculkhunt.common;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.command.SculkBlacklistCommand;
import ladysnake.sculkhunt.common.command.SculkhuntCommand;
import ladysnake.sculkhunt.common.entity.SculkCatalystEntity;
import ladysnake.sculkhunt.common.init.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ladysnake.sculkhunt.common.init.SculkhuntGamerules.SCULK_CATALYST_TERRITORY_RADIUS;

public class Sculkhunt implements ModInitializer {
    public static final String MODID = "sculkhunt";
    public static final int SPAWN_RADIUS = 250;

    public static List<UUID> playersToBeSculk = new ArrayList<>();
    public static ArrayList<UUID> playersToTurnToSculk = new ArrayList<>();

    public static int targetTimer;

    // event variables
    public static int sculkhuntPhase = 0; // 0: no sculkhunt event, 1: preparation, 2: hunt
    public static int prepTime; // preparation time
    public static ArrayList<UUID> SCULK_BLACKLIST;

    public static DefaultParticleType SOUND;

    public static boolean isBlockReplaceable(World world, BlockPos blockPos) {
        return (world.getBlockState(blockPos).isAir() || world.getBlockState(blockPos).getMaterial() == Material.BAMBOO || world.getBlockState(blockPos).getMaterial() == Material.BAMBOO_SAPLING || world.getBlockState(blockPos).getMaterial() == Material.COBWEB || world.getBlockState(blockPos).getMaterial() == Material.FIRE || world.getBlockState(blockPos).getMaterial() == Material.CARPET || world.getBlockState(blockPos).getMaterial() == Material.CACTUS || world.getBlockState(blockPos).getMaterial() == Material.PLANT || world.getBlockState(blockPos).getMaterial() == Material.REPLACEABLE_PLANT || world.getBlockState(blockPos).getMaterial() == Material.REPLACEABLE_UNDERWATER_PLANT || world.getBlockState(blockPos).getMaterial() == Material.SNOW_LAYER || world.getBlockState(blockPos).getBlock() == Blocks.WATER);
    }

    public static boolean isBlockReplaceableNotUnderwater(World world, BlockPos blockPos) {
        return (world.getBlockState(blockPos).isAir() || world.getBlockState(blockPos).getMaterial() == Material.BAMBOO || world.getBlockState(blockPos).getMaterial() == Material.BAMBOO_SAPLING || world.getBlockState(blockPos).getMaterial() == Material.COBWEB || world.getBlockState(blockPos).getMaterial() == Material.FIRE || world.getBlockState(blockPos).getMaterial() == Material.CARPET || world.getBlockState(blockPos).getMaterial() == Material.CACTUS || world.getBlockState(blockPos).getMaterial() == Material.PLANT || world.getBlockState(blockPos).getMaterial() == Material.REPLACEABLE_PLANT || world.getBlockState(blockPos).getMaterial() == Material.SNOW_LAYER);
    }

    @Override
    public void onInitialize() {
        SculkhuntItems.init();
        SculkhuntBlocks.init();
        SculkhuntBlockEntityTypes.init();
        SculkhuntEntityTypes.init();
        SculkhuntGamerules.init();
        SculkhuntDrops.init();

        SCULK_BLACKLIST = new ArrayList<>();
        SOUND = Registry.register(Registry.PARTICLE_TYPE, Sculkhunt.MODID + ":sound", FabricParticleTypes.simple(true));

        CommandRegistrationCallback.EVENT.register((commandDispatcher, b) -> {
                    SculkhuntCommand.register(commandDispatcher);
                    SculkBlacklistCommand.register(commandDispatcher);
                }
        );

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (playersToTurnToSculk.contains(newPlayer.getUuid())) {
                SculkhuntComponents.SCULK.get(newPlayer).setSculk(true);
                playersToTurnToSculk.remove(newPlayer.getUuid());
            }

            if (SculkhuntComponents.SCULK.get(newPlayer).isSculk()) {
                float sculkPercentage = getSculkPlayerPercentage(newPlayer.getServerWorld());
                float sculkMaxHealth = 12f;
                if (sculkPercentage >= 0.8f) {
                    sculkMaxHealth = 4f;
                } else if (sculkPercentage >= 0.6f) {
                    sculkMaxHealth = 6f;
                } else if (sculkPercentage >= 0.4f) {
                    sculkMaxHealth = 8f;
                } else if (sculkPercentage >= 0.2f) {
                    sculkMaxHealth = 10f;
                }

                newPlayer.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(sculkMaxHealth);
                newPlayer.setHealth(newPlayer.getMaxHealth());
                newPlayer.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.12f);
                newPlayer.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(4f);
                newPlayer.giveItemStack(new ItemStack(SculkhuntBlocks.SCULK, 1 + newPlayer.getRandom().nextInt(3)));

                // respawn in sculk
                ServerWorld world = ((ServerWorld) newPlayer.world);

                List<ServerPlayerEntity> players = world.getPlayers(serverPlayerEntity -> !serverPlayerEntity.isCreative() && !serverPlayerEntity.isSpectator() && !SculkhuntComponents.SCULK.get(serverPlayerEntity).isSculk());
                List<SculkCatalystEntity> catalysts;

                if (!players.isEmpty()) {
                    ServerPlayerEntity prey = players.get(world.random.nextInt(players.size()));
                    catalysts = world.getEntitiesByClass(SculkCatalystEntity.class, new Box(prey.getX() - SPAWN_RADIUS, prey.getY() - SPAWN_RADIUS / 2f, prey.getZ() - SPAWN_RADIUS, prey.getX() + SPAWN_RADIUS, prey.getY() + SPAWN_RADIUS / 2f, prey.getZ() + SPAWN_RADIUS), sculkCatalystEntity -> !sculkCatalystEntity.isIncapacitated());

                    if (!catalysts.isEmpty()) {
                        // filter catalysts that aren't in a 30 block radius of the player
                        catalysts = catalysts.stream().filter(sculkCatalystEntity -> sculkCatalystEntity.getBlockPos().getSquaredDistance(prey.getBlockPos()) >= 30).collect(Collectors.toList());
                        if (!catalysts.isEmpty()) {
                            catalysts.sort((o1, o2) -> (int) (prey.getPos().distanceTo(o1.getPos()) - prey.getPos().distanceTo(o2.getPos())));
                            BlockPos newPos = new BlockPos(catalysts.get(0).getPos().add(world.random.nextGaussian() * 2, 0, world.random.nextGaussian() * 2));

                            int tries = 25;
                            while (tries > 0 && !world.getBlockState(newPos).isAir() && !world.getBlockState(newPos.add(0, 1, 0)).isAir()) {
                                tries--;
                                newPos = new BlockPos(catalysts.get(0).getPos().add(world.random.nextGaussian() * 2, 0, world.random.nextGaussian() * 2));
                            }

                            if (world.getBlockState(newPos).isAir() && world.getBlockState(newPos.add(0, 1, 0)).isAir()) {
                                newPlayer.networkHandler.requestTeleport(newPos.getX(), newPos.getY() - newPlayer.getHeight() * 2, newPos.getZ(), newPlayer.getYaw(), newPlayer.getPitch());
                            } else {
                                Sculkhunt.respawnAtRandomCatalyst(world, oldPlayer, newPlayer);
                            }
                        } else {
                            Sculkhunt.respawnAtRandomCatalyst(world, oldPlayer, newPlayer);
                        }
                    } else {
                        respawnAtRandomCatalyst(world, oldPlayer, newPlayer);
                    }
                } else {
                    respawnAtRandomCatalyst(world, oldPlayer, newPlayer);
                }
            }
        });

        // spawn sculk catalysts around players
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (sculkhuntPhase == 2) {
                for (ServerWorld world : server.getWorlds()) {
                    if (world.getTime() % 20 == 0) {
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            Text message = new LiteralText("Sculk Trackers: " + world.getPlayers().stream().filter(serverPlayerEntity -> SculkhuntComponents.SCULK.get(serverPlayerEntity).isSculk()).count() + " | Survivors: " + world.getPlayers().stream().filter(serverPlayerEntity -> !SculkhuntComponents.SCULK.get(serverPlayerEntity).isSculk() && !serverPlayerEntity.isCreative() && !serverPlayerEntity.isSpectator()).count());
                            player.sendMessage(message, true);
                        }
                    }
                }
            }

            if (sculkhuntPhase == 1) {
                if (prepTime-- % 20 == 0) {
                    for (ServerWorld world : server.getWorlds()) {
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            int timeInSeconds = prepTime / 20;
                            Text message;
                            if (timeInSeconds < 60) {
                                message = new LiteralText("Preparation: " + timeInSeconds % 60 + "s left");
                            } else {
                                message = new LiteralText("Preparation: " + (int) Math.floor(timeInSeconds / 60f) + "m " + timeInSeconds % 60 + "s left");
                            }
                            player.sendMessage(message, true);
                        }
                    }
                }

                if (prepTime <= 0) {
                    sculkhuntPhase = 2;
                    server.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING).set(true, server);

                    server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, server);
                    server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
                    server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
                    server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
                    server.getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
                    server.getOverworld().setTimeOfDay(18000);

                    for (ServerWorld world : server.getWorlds()) {
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            Function<Text, Packet<?>> constructor = TitleS2CPacket::new;
                            Text title = new LiteralText("The hunt begins...").setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA));
                            try {
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, player.getX(), player.getY(), player.getZ(), 1.0F, 1.5F));
                                player.networkHandler.sendPacket(constructor.apply(Texts.parse(server.getCommandSource(), title, player, 0)));
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    for (UUID uuid : playersToBeSculk) {
                        try {
                            ServerPlayerEntity playerToSculk = server.getPlayerManager().getPlayer(uuid);
                            if (playerToSculk != null) {

                                SculkhuntComponents.SCULK.get(playerToSculk).setSculk(true);

//                            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
//                                Text message = new LiteralText(playerToSculk.getEntityName() + " joined the sculk...").setStyle(Style.EMPTY.withColor(Formatting.DARK_RED));
//                                serverPlayerEntity.sendMessage(message, false);
//                            }

                                playerToSculk.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(12f);
                                playerToSculk.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.12f);
                                playerToSculk.setHealth(playerToSculk.getMaxHealth());
                                playerToSculk.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(4f);
                                playerToSculk.getInventory().dropAll();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            server.getWorlds().forEach(world -> {
                if (world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING).get() && world.random.nextInt(world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING_DELAY).get()) == 0) {
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        if (world.getEntitiesByClass(SculkCatalystEntity.class, player.getBoundingBox().expand(world.getGameRules().get(SCULK_CATALYST_TERRITORY_RADIUS).get()), sculkCatalystEntity -> !sculkCatalystEntity.isIncapacitated()).isEmpty()) {
                            int radius = world.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING_RADIUS).get();

                            BlockPos placePos = player.getBlockPos().add(Math.round(world.random.nextGaussian() * radius), 100, Math.round(world.random.nextGaussian() * radius));

                            while (placePos.getY() > 1 &&
                                    !(world.getBlockState(placePos.add(0, -1, 0)).isSolidBlock(world, placePos.add(0, -1, 0))
                                            && isBlockReplaceableNotUnderwater(world, placePos) && isBlockReplaceableNotUnderwater(world, placePos.add(0, 1, 0)))) {
                                placePos = placePos.add(0, -1, 0);
                            }

                            if (world.getBlockState(placePos.add(0, -1, 0)).isSolidBlock(world, placePos.add(0, -1, 0))
                                    && isBlockReplaceableNotUnderwater(world, placePos)) {
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

    public float getSculkPlayerPercentage(ServerWorld world) {
        List<ServerPlayerEntity> playingPlayers = world.getPlayers().stream().filter(serverPlayerEntity -> !serverPlayerEntity.isCreative() && !serverPlayerEntity.isSpectator()).collect(Collectors.toList());
        float sculkPLayerAmount = (float) playingPlayers.stream().filter(serverPlayerEntity -> SculkhuntComponents.SCULK.get(serverPlayerEntity).isSculk()).count();

        return sculkPLayerAmount / ((float) playingPlayers.size());
    }

    public static void respawnAtRandomCatalyst(World world, ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        List<SculkCatalystEntity> catalysts = world.getEntitiesByClass(SculkCatalystEntity.class, new Box(oldPlayer.getX() - SPAWN_RADIUS * 5f, oldPlayer.getY() - SPAWN_RADIUS * 5f / 2f, oldPlayer.getZ() - SPAWN_RADIUS * 5f, oldPlayer.getX() + SPAWN_RADIUS * 5f, oldPlayer.getY() + SPAWN_RADIUS * 5f / 2f, oldPlayer.getZ() + SPAWN_RADIUS * 5f), sculkCatalystEntity -> !sculkCatalystEntity.isIncapacitated());

        if (!catalysts.isEmpty()) {
            Vec3d newPos = catalysts.get(world.random.nextInt(catalysts.size())).getPos().add(world.random.nextGaussian() * 2, -newPlayer.getHeight() * 2, world.random.nextGaussian() * 2);

            newPlayer.networkHandler.requestTeleport(newPos.getX(), newPos.getY(), newPos.getZ(), newPlayer.getYaw(), newPlayer.getPitch());
        }
    }

}
