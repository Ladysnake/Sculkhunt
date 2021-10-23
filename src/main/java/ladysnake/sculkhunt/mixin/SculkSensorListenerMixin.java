package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.SculkSensorListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkSensorListener.class)
public class SculkSensorListenerMixin {
    @Inject(method = "shouldActivate", at = @At("RETURN"), cancellable = true)
    private void shouldActivate(GameEvent event, @Nullable Entity entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (entity instanceof LivingEntity && SculkhuntComponents.SCULK.get(entity).isSculk()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
