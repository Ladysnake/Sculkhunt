package ladysnake.sculkhunt.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.sculkhunt.cca.SculkhuntComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

public class SoundParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    public SoundParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setSpriteForAge(spriteProvider);

        this.maxAge = 10;
        this.collidesWithWorld = false;

        this.colorRed = 1f;
        this.colorGreen = 1f;
        this.colorBlue = 1f;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        if (camera.getFocusedEntity() instanceof PlayerEntity && SculkhuntComponents.SCULK.get(camera.getFocusedEntity()).isSculk()) {

            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);

            Vec3d vec3d = camera.getPos();
            float f = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
            float g = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
            float h = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
            Quaternion quaternion2;
            if (this.angle == 0.0F) {
                quaternion2 = camera.getRotation();
            } else {
                quaternion2 = new Quaternion(camera.getRotation());
                float i = MathHelper.lerp(tickDelta, this.prevAngle, this.angle);
                quaternion2.hamiltonProduct(Vec3f.POSITIVE_Z.getRadialQuaternion(i));
            }

            Vec3f Vec3f = new Vec3f(-1.0F, -1.0F, 0.0F);
            Vec3f.rotate(quaternion2);
            Vec3f[] Vec3fs = new Vec3f[]{new Vec3f(-1.0F, -1.0F, 0.0F), new Vec3f(-1.0F, 1.0F, 0.0F), new Vec3f(1.0F, 1.0F, 0.0F), new Vec3f(1.0F, -1.0F, 0.0F)};
            float j = this.getSize(tickDelta);

            for (int k = 0; k < 4; ++k) {
                Vec3f Vec3f2 = Vec3fs[k];
                Vec3f2.rotate(quaternion2);
                Vec3f2.scale((float) new Vec3d(this.x, this.y, this.z).distanceTo(camera.getPos()) / 10f);
                Vec3f2.add(f, g, h);
            }

            float minU = this.getMinU();
            float maxU = this.getMaxU();
            float minV = this.getMinV();
            float maxV = this.getMaxV();
            int l = 15728880;
            float a = Math.min(1f, Math.max(0f, this.colorAlpha));

            vertexConsumer.vertex(Vec3fs[0].getX(), Vec3fs[0].getY(), Vec3fs[0].getZ()).texture(maxU, maxV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
            vertexConsumer.vertex(Vec3fs[1].getX(), Vec3fs[1].getY(), Vec3fs[1].getZ()).texture(maxU, minV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
            vertexConsumer.vertex(Vec3fs[2].getX(), Vec3fs[2].getY(), Vec3fs[2].getZ()).texture(minU, minV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
            vertexConsumer.vertex(Vec3fs[3].getX(), Vec3fs[3].getY(), Vec3fs[3].getZ()).texture(minU, maxV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
        } else {
            this.markDead();
        }
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        this.setSpriteForAge(spriteProvider);

        if (this.age++ > this.maxAge) {
            this.markDead();
        }

        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public DefaultFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new SoundParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }

}
