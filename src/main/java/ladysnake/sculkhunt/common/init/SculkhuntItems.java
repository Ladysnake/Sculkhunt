package ladysnake.sculkhunt.common.init;

import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.item.SculkEyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class SculkhuntItems {

    public static Item SCULK_EYE;

    public static void init() {
        SCULK_EYE = registerItem(new SculkEyeItem(new Item.Settings().group(ItemGroup.MISC).maxCount(64)), "sculk_eye");
    }

    public static Item registerItem(Item item, String name) {
        Registry.register(Registry.ITEM, Sculkhunt.MODID + ":" + name, item);

        return item;
    }

}
