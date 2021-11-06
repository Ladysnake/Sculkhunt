package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.common.init.SculkhuntBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.BubbleColumnBlock.DRAG;

@Mixin(BubbleColumnBlock.class)
public class BubbleColumnBlockMixin {
    @Inject(method = "getBubbleState", at = @At("RETURN"), cancellable = true)
    private static void getBubbleState(BlockState state, CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (state.isOf(SculkhuntBlocks.SCULK)) {
            callbackInfoReturnable.setReturnValue(Blocks.BUBBLE_COLUMN.getDefaultState().with(DRAG, false));
        }
    }

    @Inject(method = "canPlaceAt", at = @At("RETURN"), cancellable = true)
    public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (world.getBlockState(pos.down()).isOf(SculkhuntBlocks.SCULK)) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }
}