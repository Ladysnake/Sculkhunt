package ladysnake.sculkhunt.common.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Random;

public class SculkhuntDrops {
    public static ArrayList<DropEntry> DROPS = new ArrayList<>();
    public static Item[] SCULK_DROPS = {Items.PORKCHOP, Items.CHICKEN, Items.EGG, Items.ARROW, Items.BEEF, Items.MUTTON, Items.FERMENTED_SPIDER_EYE, Items.POISONOUS_POTATO, Items.ROTTEN_FLESH};

    public static void init() {
        DROPS.add(new DropEntry(Items.PORKCHOP, 1, 3));
        DROPS.add(new DropEntry(Items.BEEF, 1, 3));
        DROPS.add(new DropEntry(Items.MUTTON, 1, 3));
        DROPS.add(new DropEntry(Items.SNOWBALL, 6, 9));
        DROPS.add(new DropEntry(Items.ARROW, 6, 9));
        DROPS.add(new DropEntry(Items.ENDER_PEARL, 1, 1));
    }

    public static ItemStack getRandomDrop(Random random) {
        DropEntry drop = DROPS.get(random.nextInt(DROPS.size()));
        return new ItemStack(drop.item, drop.minCount + random.nextInt(drop.maxCount - drop.minCount + 1));
    }

}

class DropEntry {
    public Item item;
    public int minCount;
    public int maxCount;

    public DropEntry(Item item, int minCount, int maxCount) {
        this.item = item;
        this.minCount = minCount;
        this.maxCount = maxCount;
    }
}