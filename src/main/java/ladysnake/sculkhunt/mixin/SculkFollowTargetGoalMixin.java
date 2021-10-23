package ladysnake.sculkhunt.mixin;

import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FollowTargetGoal.class)
public abstract class SculkFollowTargetGoalMixin<T extends LivingEntity> extends TrackTargetGoal {
    @Shadow
    @Final
    protected Class<T> targetClass;

    @Shadow
    protected LivingEntity targetEntity;

    @Shadow
    protected TargetPredicate targetPredicate;

    public SculkFollowTargetGoalMixin(MobEntity mob, boolean checkVisibility) {
        super(mob, checkVisibility);
    }

    @Shadow
    protected abstract Box getSearchBox(double distance);

    @Inject(method = "findClosestTarget", at = @At("RETURN"))
    protected void findClosestTarget(CallbackInfo callbackInfo) {
        if (SculkhuntComponents.SCULK.get(this.mob).isSculk()) {
            this.targetEntity = this.mob.world.getClosestEntity(this.mob.world.getEntitiesByClass(this.targetClass, this.getSearchBox(this.getFollowRange()), (livingEntity) -> {
                return !SculkhuntComponents.SCULK.get(livingEntity).isSculk() && SculkhuntComponents.SCULK.get(livingEntity).isDetected();
            }), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            this.mob.setTarget(this.targetEntity);
        } else {
            if (this.targetClass == PlayerEntity.class || this.targetClass == ServerPlayerEntity.class) {
                this.targetEntity = this.mob.world.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());

                if (this.targetEntity != null && SculkhuntComponents.SCULK.get(this.targetEntity).isSculk()) {
                    this.targetEntity = null;
                }
            }
        }
    }
}
