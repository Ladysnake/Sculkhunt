package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @Shadow
    private PlayerEntity target;

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    public void tick(CallbackInfo callbackInfo) {
        if (target != null && SculkhuntComponents.SCULK.get(target).isSculk()) {
            target = null;
        }
    }

    @Inject(method = "onPlayerCollision", at = @At("TAIL"), cancellable = true)
    public void onPlayerCollision(PlayerEntity player, CallbackInfo callbackInfo) {
        if (SculkhuntComponents.SCULK.get(player).isSculk()) {
            callbackInfo.cancel();
        }
    }
}