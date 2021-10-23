package ladysnake.sculkhunt.common.block;

import ladysnake.sculkhunt.common.entity.SculkCatalystEntity;
import ladysnake.sculkhunt.common.init.SculkhuntEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SculkCatalystBlock extends Block {
    public SculkCatalystBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        SculkCatalystEntity sculkCatalystEntity = SculkhuntEntityTypes.SCULK_CATALYST.create(world);
        sculkCatalystEntity.refreshPositionAndAngles(pos.getX() + .5f, pos.getY(), pos.getZ() + .5f, 0, 0);
        world.spawnEntity(sculkCatalystEntity);
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
    }
}
