package com.Harbinger.Spore.Particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BileBulletParticle extends TextureSheetParticle {
   protected BileBulletParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, double r, double g, double b) {
      super(level, xCoord, yCoord, zCoord, (double)0.0F, -0.02, (double)0.0F);
      this.gravity = 1.0F;
      this.hasPhysics = true;
      this.friction = 0.0F;
      this.xd = (double)0.0F;
      this.yd = -0.03;
      this.zd = (double)0.0F;
      this.quadSize *= 1.2F;
      this.lifetime = 50;
      this.rCol = (float)r;
      this.gCol = (float)g;
      this.bCol = (float)b;
   }

   public void tick() {
      super.tick();
      this.fadeOut();
   }

   private void fadeOut() {
      this.alpha = -(1.0F / (float)this.lifetime) * (float)this.age + 1.0F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteSet) {
         this.sprites = spriteSet;
      }

      public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         BileBulletParticle particle = new BileBulletParticle(level, x, y, z, dx, dy, dz);
         particle.pickSprite(this.sprites);
         return particle;
      }
   }
}
