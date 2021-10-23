package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {
    @Shadow
    private int prevFoodLevel;

    @Shadow
    public abstract void setFoodLevel(int foodLevel);

    @Shadow
    public abstract void setSaturationLevel(float saturationLevel);

    @Inject(method = "update", at = @At("RETURN"), cancellable = true)
    public void update(PlayerEntity player, CallbackInfo callbackInfo) {
        if (SculkhuntComponents.SCULK.get(player).isSculk()) {
            this.setFoodLevel(20);
            this.setSaturationLevel(0);
        }
    }
}
