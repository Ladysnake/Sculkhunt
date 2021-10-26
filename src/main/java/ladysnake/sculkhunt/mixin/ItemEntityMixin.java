package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    public void onPlayerCollision(PlayerEntity player, CallbackInfo callbackInfo) {
        if (!this.world.isClient) {
            if (SculkhuntComponents.SCULK.get(player).isSculk()) {
                callbackInfo.cancel();
            }
        }
    }
}
