package ladysnake.sculkhunt.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ladysnake.sculkhunt.client.render.entity.model.SculkCatalystEntityModel;
import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.entity.SculkCatalystEntity;
import ladysnake.sculkhunt.common.init.SculkhuntEntityTypes;
import ladysnake.sculkhunt.common.init.SculkhuntGamerules;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.function.Function;

public class SculkhuntCommand {
    private static final int PREP_DEFAULT_DURATION = 900; // 15 minutes of preparation time

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("sculkhunt").requires((source) -> {
            return source.hasPermissionLevel(2);
        })).then(((LiteralArgumentBuilder)CommandManager.literal("start").executes((context) -> {
            return startSculkhunt(context.getSource(), PREP_DEFAULT_DURATION);
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

        for (ServerWorld world : source.getServer().getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                player.setHealth(20f);
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(0f);
                player.getInventory().clear();

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

        return prepDuration;
    }

    private static int stopSculkhunt(ServerCommandSource source) {
        Sculkhunt.sculkhuntPhase = 0;
        source.getServer().getGameRules().get(SculkhuntGamerules.SCULK_CATALYST_SPAWNING).set(false, source.getServer());
        for (ServerWorld world : source.getServer().getWorlds()) {
            for (SculkCatalystEntity sculkCatalystEntity : world.getEntitiesByType(SculkhuntEntityTypes.SCULK_CATALYST, Entity::isAlive)) {
                sculkCatalystEntity.kill();
            }
        }

        source.sendFeedback(new TranslatableText("commands.sculkhunt.stop"), true);
        return 0;
    }

}
