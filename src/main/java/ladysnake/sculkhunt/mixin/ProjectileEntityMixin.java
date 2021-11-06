package ladysnake.sculkhunt.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {
    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

//    @Inject(method = "onCollision", at = @At("RETURN"))
//    protected void onCollision(HitResult hitResult, CallbackInfo callbackInfo) {
//        this.world.addParticle(Sculkhunt.SOUND, true, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 0, 0, 0);
//    }
}
