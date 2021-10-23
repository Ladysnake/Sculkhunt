package ladysnake.sculkhunt.common.init;

import ladysnake.sculkhunt.common.Sculkhunt;
import ladysnake.sculkhunt.common.entity.SculkCatalystEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;

public class SculkhuntEntityTypes {
    public static EntityType<SculkCatalystEntity> SCULK_CATALYST;

    public static void init() {
        SCULK_CATALYST = register("sculk_catalyst", FabricEntityTypeBuilder.create(SpawnGroup.MISC, SculkCatalystEntity::new).dimensions(EntityDimensions.changing(1f, 1f)).trackRangeBlocks(64).trackedUpdateRate(Integer.MAX_VALUE).build());
    }

    private static <T extends Entity> EntityType<T> register(String s, EntityType<T> entityType) {
        return Registry.register(Registry.ENTITY_TYPE, Sculkhunt.MODID + ":" + s, entityType);
    }
}
