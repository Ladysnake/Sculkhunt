package ladysnake.sculkhunt.common.init;

import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.block.SculkBlock;
import ladysnake.sculkhunt.common.block.SculkCatalystBlock;
import ladysnake.sculkhunt.common.block.SculkVeinBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.Registry;

public class SculkhuntBlocks {
    public static Block SCULK;
    public static Block SCULK_VEIN;
    public static Block SCULK_CATALYST;

    public static void init() {
        SCULK = registerBlock(new SculkBlock(FabricBlockSettings.of(Material.SCULK, MapColor.CYAN).strength(1.5F).sounds(BlockSoundGroup.SCULK_SENSOR).breakByTool(FabricToolTags.HOES).suffocates((state, world, pos) -> false), UniformIntProvider.create(0, 3)), "sculk", ItemGroup.BUILDING_BLOCKS);
        SCULK_VEIN = registerBlock(new SculkVeinBlock(FabricBlockSettings.of(Material.SCULK, MapColor.CYAN).strength(1.5F).sounds(BlockSoundGroup.SCULK_SENSOR).breakByTool(FabricToolTags.HOES).noCollision()), "sculk_vein", ItemGroup.DECORATIONS);
        SCULK_CATALYST = registerBlock(new SculkCatalystBlock(FabricBlockSettings.of(Material.SCULK, MapColor.CYAN).strength(1.5F).sounds(BlockSoundGroup.SCULK_SENSOR).breakByTool(FabricToolTags.HOES).nonOpaque()), "sculk_catalyst", ItemGroup.DECORATIONS);
    }

    private static Block registerBlock(Block block, String name, ItemGroup itemGroup) {
        Registry.register(Registry.BLOCK, Sculkhunt.MODID + ":" + name, block);

        if (itemGroup != null) {
            BlockItem item = new BlockItem(block, new Item.Settings().group(itemGroup));
            item.appendBlocks(Item.BLOCK_ITEMS, item);
            SculkhuntItems.registerItem(item, name);
        }

        return block;
    }

}
