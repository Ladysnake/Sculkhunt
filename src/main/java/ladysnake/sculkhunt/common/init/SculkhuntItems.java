package ladysnake.sculkhunt.common.init;

import ladysnake.sculkhunt.common.Sculkhunt;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class SculkhuntItems {

//    public static Item BOMBARD;

    public static void init() {
//        BOMBARD = registerItem(new BombardItem(new Item.Settings().group(ItemGroup.COMBAT).maxCount(1).maxDamage(250)), "bombard");
    }

    public static Item registerItem(Item item, String name) {
        Registry.register(Registry.ITEM, Sculkhunt.MODID + ":" + name, item);

        return item;
    }

}
