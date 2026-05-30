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

public class VomitParticle extends TextureSheetParticle {
   protected VomitParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
      super(level, xCoord, yCoord, zCoord, xd, yd, zd);
      this.gravity = 1.0F;
      this.hasPhysics = true;
      this.friction = 0.0F;
      this.xd = xd;
      this.yd = yd;
      this.zd = zd;
      this.quadSize *= 1.2F;
      this.lifetime = 30;
      this.setSpriteFromAge(spriteSet);
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
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
         return new VomitParticle(level, x, y, z, this.sprites, dx, dy + 0.1, dz);
      }
   }
}
