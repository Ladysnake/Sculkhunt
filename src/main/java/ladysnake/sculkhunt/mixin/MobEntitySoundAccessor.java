package ladysnake.sculkhunt.mixin;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEntity.class)
public interface MobEntitySoundAccessor {
    @Invoker("getAmbientSound")
    public SoundEvent accessAmbientSound();
}
