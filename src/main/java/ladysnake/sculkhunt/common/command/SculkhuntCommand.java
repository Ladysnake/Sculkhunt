package ladysnake.sculkhunt.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ladysnake.sculkhunt.cca.SculkhuntComponents;
import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.entity.SculkCatalystEntity;
import ladysnake.sculkhunt.common.init.SculkhuntEntityTypes;
import ladysnake.sculkhunt.common.init.SculkhuntGamerules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SculkhuntCommand {
    private static final int PREP_DEFAULT_DURATION = 900; // 15 minutes of preparation time

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("sculkhunt").requires((source) -> {
            return source.hasPermissionLevel(2);
        })).then(((LiteralArgumentBuilder) CommandManager.literal("start").executes((context) -> {
            return startSculkhunt(context.getSource(), PREP_DEFAULT_DURATION * 20);
        })).then(CommandManager.argument("prepDuration", IntegerArgumentType.integer(0, 1000000)).executes((context) -> {
            return startSculkhunt(context.getSource(), IntegerArgumentType.getInteger(context, "prepDuration") * 20);
        }))).then(CommandManager.literal("stop").executes((context) -> {
            return stopSculkhunt(context.getSource());
        })));
    }

    private static int startSculkhunt(ServerCommandSource source, int prepDuration) {
        source.sendFeedback(new TranslatableText("commands.sculkhunt.start"), true);
        Sculkhunt.sculkhuntPhase = 1;
        Sculkhunt.prepTime = prepDuration;

        Sculkhunt.playersToBeSculk = new ArrayList<>();

        MinecraftServer server = source.getServer();
        server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
        server.getOverworld().setTimeOfDay(6000);

        for (ServerWorld world : source.getServer().getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                player.setHealth(20f);
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(0f);
//                player.getInventory().clear();

                Function<Text, Packet<?>> constructor = TitleS2CPacket::new;
                Text title = new LiteralText("Sculkhunt has started!").setStyle(Style.EMPTY.withColor(Formatting.AQUA));
                player.playSound(SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, 0.1f, 1.0f);
                player.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 0.1f, 1.0f);
                try {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, player.getX(), player.getY(), player.getZ(), 1.0F, 1.5F));
                    player.networkHandler.sendPacket(constructor.apply(Texts.parse(source, title, player, 0)));
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        List<ServerPlayerEntity> playerPool = server.getPlayerManager().getPlayerList().stream().filter(serverPlayerEntity -> !serverPlayerEntity.isSpectator() && !serverPlayerEntity.isCreative()).collect(Collectors.toList());
        int sculkPlayers = Math.max(1, Math.round(playerPool.size() / 5f)); // 1 in 5 players / 20% become sculk at the start
        for (int i = 0; i < sculkPlayers; i++) {
            if (!playerPool.isEmpty()) {
                ServerPlayerEntity playerToBeSculk = playerPool.get(server.getOverworld().random.nextInt(playerPool.size()));

                Sculkhunt.playersToBeSculk.add(playerToBeSculk.getUuid());

                Function<Text, Packet<?>> constructor = TitleS2CPacket::new;
                Text title = new LiteralText("You will become a Sculk Tracker").setStyle(Style.EMPTY.withColor(Formatting.DARK_RED));
                try {
                    playerToBeSculk.networkHandler.sendPacket(constructor.apply(Texts.parse(server.getCommandSource(), title, playerToBeSculk, 0)));
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }

                playerPool.remove(playerToBeSculk);
            }
        }

        return prepDuration;
    }

    public static int stopSculkhunt(ServerCommandSource source) {
        Sculkhunt.sculkhuntPhase = 0;
        MinecraftServer server = source.getServer();
        server.getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING).set(false, server);
        server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
        server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(true, server);
        server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(true, server);
        server.getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(false, server);
        for (ServerWorld world : source.getServer().getWorlds()) {
            for (SculkCatalystEntity sculkCatalystEntity : world.getEntitiesByType(SculkhuntEntityTypes.SCULK_CATALYST, Entity::isAlive)) {
                sculkCatalystEntity.kill();
            }
        }

        source.sendFeedback(new TranslatableText("commands.sculkhunt.stop"), true);
        return 0;
    }

}
