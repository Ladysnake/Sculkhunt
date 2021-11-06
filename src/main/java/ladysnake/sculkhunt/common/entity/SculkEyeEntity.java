package ladysnake.sculkhunt.common.entity;

import ladysnake.sculkhunt.common.init.SculkhuntEntityTypes;
import ladysnake.sculkhunt.common.init.SculkhuntItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class SculkEyeEntity extends Entity implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM;

    static {
        ITEM = DataTracker.registerData(SculkEyeEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    }

    private double targetX;
    private double targetY;
    private double targetZ;
    private int lifespan;
    private boolean dropsItem;

    public SculkEyeEntity(EntityType<SculkEyeEntity> entityType, World world) {
        super(entityType, world);
    }

    public SculkEyeEntity(World world, double x, double y, double z) {
        this(SculkhuntEntityTypes.SCULK_EYE, world);
        this.setPosition(x, y, z);
    }

    protected static float updateRotation(float prevRot, float newRot) {
        while (newRot - prevRot < -180.0F) {
            prevRot -= 360.0F;
        }

        while (newRot - prevRot >= 180.0F) {
            prevRot += 360.0F;
        }

        return MathHelper.lerp(0.2F, prevRot, newRot);
    }

    public void setItem(ItemStack stack) {
        if (!stack.isOf(SculkhuntItems.SCULK_EYE) || stack.hasNbt()) {
            this.getDataTracker().set(ITEM, Util.make(stack.copy(), (stackx) -> {
                stackx.setCount(1);
            }));
        }
    }

    private ItemStack getTrackedItem() {
        return this.getDataTracker().get(ITEM);
    }

    public ItemStack getStack() {
        ItemStack itemStack = this.getTrackedItem();
        return itemStack.isEmpty() ? new ItemStack(Items.ENDER_EYE) : itemStack;
    }

    protected void initDataTracker() {
        this.getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
    }

    public boolean shouldRender(double distance) {
        double d = this.getBoundingBox().getAverageSideLength() * 4.0D;
        if (Double.isNaN(d)) {
            d = 4.0D;
        }

        d *= 64.0D;
        return distance < d * d;
    }

    /**
     * Sets where the eye will fly towards.
     * If close enough, it will fly directly towards it, otherwise, it will fly upwards, in the direction of the BlockPos.
     *
     * @param pos the block the eye of ender is drawn towards
     */
    public void initTargetPos(BlockPos pos) {
        double d = (double) pos.getX();
        int i = pos.getY();
        double e = (double) pos.getZ();
        double f = d - this.getX();
        double g = e - this.getZ();
        double h = Math.sqrt(f * f + g * g);
        if (h > 12.0D) {
            this.targetX = this.getX() + f / h * 12.0D;
            this.targetZ = this.getZ() + g / h * 12.0D;
            this.targetY = this.getY() + 8.0D;
        } else {
            this.targetX = d;
            this.targetY = (double) i;
            this.targetZ = e;
        }

        this.lifespan = 0;
        this.dropsItem = this.random.nextInt(5) > 0;
    }

    public void setVelocityClient(double x, double y, double z) {
        this.setVelocity(x, y, z);
        if (this.prevPitch == 0.0F && this.prevYaw == 0.0F) {
            double d = Math.sqrt(x * x + z * z);
            this.setYaw((float) (MathHelper.atan2(x, z) * 57.2957763671875D));
            this.setPitch((float) (MathHelper.atan2(y, d) * 57.2957763671875D));
            this.prevYaw = this.getYaw();
            this.prevPitch = this.getPitch();
        }

    }

    public void tick() {
        super.tick();
        Vec3d vec3d = this.getVelocity();
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
        double g = vec3d.horizontalLength();
        this.setPitch(updateRotation(this.prevPitch, (float) (MathHelper.atan2(vec3d.y, g) * 57.2957763671875D)));
        this.setYaw(updateRotation(this.prevYaw, (float) (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D)));
        if (!this.world.isClient) {
            double h = this.targetX - d;
            double i = this.targetZ - f;
            float j = (float) Math.sqrt(h * h + i * i);
            float k = (float) MathHelper.atan2(i, h);
            double l = MathHelper.lerp(0.0025D, g, (double) j);
            double m = vec3d.y;
            if (j < 1.0F) {
                l *= 0.8D;
                m *= 0.8D;
            }

            int n = this.getY() < this.targetY ? 1 : -1;
            vec3d = new Vec3d(Math.cos((double) k) * l, m + ((double) n - m) * 0.014999999664723873D, Math.sin((double) k) * l);
            this.setVelocity(vec3d);
        }

        if (this.isTouchingWater()) {
            for (int p = 0; p < 4; ++p) {
                this.world.addParticle(ParticleTypes.BUBBLE, d - vec3d.x * 0.25D, e - vec3d.y * 0.25D, f - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
            }
        } else {
            this.world.addParticle(ParticleTypes.PORTAL, d - vec3d.x * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, e - vec3d.y * 0.25D - 0.5D, f - vec3d.z * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, vec3d.x, vec3d.y, vec3d.z);
        }

        if (!this.world.isClient) {
            this.setPosition(d, e, f);
            ++this.lifespan;
            if (this.lifespan > 80 && !this.world.isClient) {
                this.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
                this.discard();
                this.world.syncWorldEvent(WorldEvents.EYE_OF_ENDER_BREAKS, this.getBlockPos(), 0);
            }
        } else {
            this.setPos(d, e, f);
        }

    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        ItemStack itemStack = this.getTrackedItem();
        if (!itemStack.isEmpty()) {
            nbt.put("Item", itemStack.writeNbt(new NbtCompound()));
        }

    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        ItemStack itemStack = ItemStack.fromNbt(nbt.getCompound("Item"));
        this.setItem(itemStack);
    }

    public float getBrightnessAtEyes() {
        return 1.0F;
    }

    public boolean isAttackable() {
        return false;
    }

    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
