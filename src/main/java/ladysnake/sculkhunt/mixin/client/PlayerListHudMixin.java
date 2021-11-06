package ladysnake.sculkhunt.mixin.client;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Inject(method = "applyGameModeFormatting", at = @At("RETURN"), cancellable = true)
    private void applyGameModeFormatting(PlayerListEntry entry, MutableText name, CallbackInfoReturnable<Text> callbackInfoReturnable) {
        if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.getPlayerByUuid(entry.getProfile().getId()) != null && MinecraftClient.getInstance().world != null && SculkhuntComponents.SCULK.get(MinecraftClient.getInstance().world.getPlayerByUuid(entry.getProfile().getId())).isSculk()) {
            callbackInfoReturnable.setReturnValue(name.formatted(Formatting.DARK_AQUA));
        }
    }
}