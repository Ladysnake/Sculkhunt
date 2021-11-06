package ladysnake.sculkhunt.mixin;

import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameEvent.class)
public abstract class GameEventMixin {
    @Shadow
    @Final
    private int range;

    @Inject(method = "getRange", at = @At("RETURN"), cancellable = true)
    public void getRange(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(this.range * 5);
    }
}
