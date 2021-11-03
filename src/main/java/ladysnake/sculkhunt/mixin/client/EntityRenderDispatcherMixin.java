package ladysnake.sculkhunt.mixin.client;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (entity instanceof LivingEntity &&  SculkhuntComponents.SCULK.get(MinecraftClient.getInstance().player).isSculk() && !SculkhuntComponents.SCULK.get(entity).isSculk()) {
            if ((!SculkhuntComponents.SCULK.get(entity).isDetected() && (entity.getX() == entity.prevX && entity.getZ() == entity.prevZ)) || entity.isSneaking()) {
                callbackInfoReturnable.cancel();
            }
        }
    }
}
