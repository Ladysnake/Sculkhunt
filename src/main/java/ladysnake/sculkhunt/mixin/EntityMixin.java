package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    public void isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if ((Object) this instanceof LivingEntity && SculkhuntComponents.SCULK.get(this).isSculk()) {
            if (damageSource.equals(DamageSource.FALL)) {
                callbackInfoReturnable.setReturnValue(true);
            }
        }
    }
}
