package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockDetectionMixin {
    @Shadow
    @Final
    public static BooleanProperty WATERLOGGED;

    @Inject(method = "setActive", at = @At("RETURN"))
    private static void setActive(World world, BlockPos pos, BlockState state, int power, CallbackInfo callbackInfo) {
        for (Entity entity : world.getOtherEntities(null, new Box(pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8, pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8))) {
            if (entity instanceof LivingEntity && !SculkhuntComponents.SCULK.get(entity).isSculk()) {
                SculkhuntComponents.SCULK.get(entity).setDetectedTime(200); // 10 seconds of detection
            }
        }
    }

}
