package ladysnake.sculkhunt.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Collection;
import java.util.Iterator;

public class SculkBlacklistCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = (LiteralArgumentBuilder) CommandManager.literal("sculkBlacklist").requires((source) -> {
            return source.hasPermissionLevel(2);
        });
        GameMode[] var2 = GameMode.values();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            literalArgumentBuilder.then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> execute(context, EntityArgumentType.getPlayers(context, "target"))));
        }

        dispatcher.register(literalArgumentBuilder);
    }

    private static int execute(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets) {
        int i = 0;
        Iterator var4 = targets.iterator();

        while (var4.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) var4.next();
            Sculkhunt.SCULK_BLACKLIST.add(serverPlayerEntity.getUuid());
        }

        return i;
    }
}