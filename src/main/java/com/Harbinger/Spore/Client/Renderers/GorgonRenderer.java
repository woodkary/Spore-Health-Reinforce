package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.SporeRenderTypes;
import com.Harbinger.Spore.Client.Models.GorgonSpookyModel;
import com.Harbinger.Spore.Client.Models.gorgonModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Gorgon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class GorgonRenderer extends BaseInfectedRenderer<Gorgon> {
   private final EntityModel def = this.getModel();
   private final EntityModel spooky;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/gorgon.png");
   private static final ResourceLocation TEXTURE_SPOOK = new ResourceLocation("spore", "textures/entity/spooky_gorgon.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/gorgon.png");
   protected List<Vec3> entities = new ArrayList<>();
   private final List<SporeParticle> activeParticles = new ArrayList<>();
   private static final int MAX_PARTICLES = 15;
   private static final float PARTICLE_SPEED = 0.15F;
   private int particleTimer = 0;

   public GorgonRenderer(EntityRendererProvider.Context context) {
      super(context, new gorgonModel(context.bakeLayer(gorgonModel.LAYER_LOCATION)), 0.5F);
      this.spooky = new GorgonSpookyModel(context.bakeLayer(GorgonSpookyModel.LAYER_LOCATION));
      this.addLayer(new VolatileGlowingLayers(this));
   }

   public ResourceLocation getTextureLocation(Gorgon entity) {
      return entity.spooky() ? TEXTURE_SPOOK : TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Gorgon type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      super.render(type, value1, value2, stack, bufferSource, light);
      this.model = type.spooky() ? this.spooky : this.def;
      Entity instance = type.level().getEntity(type.getTargetId());
      if (instance != null) {
         Vec3 vec3 = instance.getPosition(value2).add((double)0.0F, (double)instance.getEyeHeight(), (double)0.0F);
         Vec3 entityPos = type.getPosition(value2);
         this.applyIK(value2, type, vec3);
         this.spawnParticlesAlongChain(type);
         this.updateParticles(type);
         stack.pushPose();
         stack.translate(-entityPos.x, -entityPos.y, -entityPos.z);
         this.renderParticles(stack, bufferSource, entityPos);
         stack.popPose();
      }
   }

   protected void moveSegmentTowards(int index, Vec3 target) {
      this.entities.set(index, target);
   }

   public void applyIK(float partial, Gorgon t, Vec3 camera) {
      Vec3 vec3 = (new Vec3(0.1, 1.8, (double)0.0F)).yRot(-t.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      Vec3 basePos = t.getPosition(partial).add(vec3);
      this.rebuildChain(basePos, camera);
      if (this.entities != null && this.entities.size() >= 3) {
         this.moveSegmentTowards(this.entities.size() - 1, camera);

         for(int i = this.entities.size() - 2; i >= 0; --i) {
            Vec3 nextPos = (Vec3)this.entities.get(i + 1);
            Vec3 dir = ((Vec3)this.entities.get(i)).subtract(nextPos);
            float segmentLength = 1.0F;
            if (dir.lengthSqr() > (double)1.0E-4F) {
               dir = dir.normalize().scale((double)segmentLength);
            } else {
               dir = new Vec3((double)segmentLength, (double)0.0F, (double)0.0F);
            }

            Vec3 solvedPos = nextPos.add(dir);
            this.moveSegmentTowards(i, solvedPos);
         }

         this.entities.set(0, basePos);

         for(int i = 1; i < this.entities.size(); ++i) {
            Vec3 prevPos = (Vec3)this.entities.get(i - 1);
            Vec3 dir = ((Vec3)this.entities.get(i)).subtract(prevPos);
            float segmentLength = 1.0F;
            if (dir.lengthSqr() > (double)1.0E-4F) {
               dir = dir.normalize().scale((double)segmentLength);
            } else {
               dir = new Vec3((double)segmentLength, (double)0.0F, (double)0.0F);
            }

            Vec3 solvedPos = prevPos.add(dir);
            this.moveSegmentTowards(i, solvedPos);
         }

      }
   }

   private void rebuildChain(Vec3 start, Vec3 end) {
      double distance = start.distanceTo(end);
      int desiredSegments = Mth.clamp((int)(distance * 1.1), 4, 40);
      if (this.entities.size() == desiredSegments && !this.entities.isEmpty()) {
         Vec3 oldStart = (Vec3)this.entities.get(0);
         Vec3 oldEnd = (Vec3)this.entities.get(this.entities.size() - 1);
         if (oldStart.distanceToSqr(start) < 0.01 && oldEnd.distanceToSqr(end) < 0.01) {
            return;
         }
      }

      this.entities.clear();

      for(int i = 0; i < desiredSegments; ++i) {
         double t = (double)i / (double)(desiredSegments - 1);
         this.entities.add(new Vec3(Mth.lerp(t, start.x, end.x), Mth.lerp(t, start.y, end.y), Mth.lerp(t, start.z, end.z)));
      }

   }

   private void spawnParticlesAlongChain(Gorgon entity) {
      if (!this.entities.isEmpty() && this.entities.size() >= 2) {
         ++this.particleTimer;
         if (this.particleTimer >= 3) {
            this.particleTimer = 0;
            ClientLevel level = (ClientLevel)entity.level();
            int particlesToSpawn = entity.getRandom().nextInt(2) + 1;

            for(int p = 0; p < particlesToSpawn && this.activeParticles.size() < 15; ++p) {
               int segmentIndex = entity.getRandom().nextInt(this.entities.size() - 1);
               Vec3 start = (Vec3)this.entities.get(segmentIndex);
               Vec3 end = (Vec3)this.entities.get(segmentIndex + 1);
               float t = entity.getRandom().nextFloat();
               Vec3 position = start.lerp(end, (double)t);
               Vec3 direction = ((Vec3)this.entities.get(this.entities.size() - 1)).subtract(position).normalize();
               direction = direction.add((entity.getRandom().nextDouble() - (double)0.5F) * 0.2, (entity.getRandom().nextDouble() - (double)0.5F) * 0.2, (entity.getRandom().nextDouble() - (double)0.5F) * 0.2).normalize();
               SporeParticle particle = new SporeParticle(position, direction.scale((double)(0.15F * (0.5F + entity.getRandom().nextFloat() * 0.5F))), this.entities.size() - 1, -1, 20 + entity.getRandom().nextInt(20));
               this.activeParticles.add(particle);
               if (entity.getRandom().nextFloat() < 0.3F) {
                  level.addParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get(), position.x, position.y, position.z, direction.x * 0.1, direction.y * 0.1, direction.z * 0.1);
               }
            }

         }
      }
   }

   private void updateParticles(Gorgon entity) {
      ClientLevel level = (ClientLevel)entity.level();
      Iterator<SporeParticle> iterator = this.activeParticles.iterator();

      while(iterator.hasNext()) {
         SporeParticle particle = (SporeParticle)iterator.next();
         --particle.life;
         if (particle.life <= 0) {
            if (particle.reachedEnd) {
               this.createDispersalEffect(level, particle.position);
            }

            iterator.remove();
         } else {
            Vec3 oldPos = particle.position;
            Vec3 newPos = oldPos.add(particle.velocity);
            particle.position = newPos;
            if (!particle.reachedEnd && this.entities.size() > particle.targetIndex) {
               Vec3 endPos = (Vec3)this.entities.get(particle.targetIndex);
               double distanceToEnd = newPos.distanceTo(endPos);
               if (distanceToEnd < 0.2 || particle.position.distanceTo(endPos) < particle.oldDistanceToEnd) {
                  particle.reachedEnd = true;
                  particle.life = Math.min(particle.life, 10);
                  this.createDispersalEffect(level, newPos);
               }

               particle.oldDistanceToEnd = distanceToEnd;
            }

            if (entity.tickCount % 2 == 0) {
               level.addParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get(), newPos.x, newPos.y, newPos.z, (entity.getRandom().nextDouble() - (double)0.5F) * 0.05, (entity.getRandom().nextDouble() - (double)0.5F) * 0.05, (entity.getRandom().nextDouble() - (double)0.5F) * 0.05);
            }
         }
      }

   }

   private void createDispersalEffect(ClientLevel level, Vec3 position) {
      Random random = new Random();

      for(int i = 0; i < 8; ++i) {
         double angle = random.nextDouble() * Math.PI * (double)2.0F;
         double pitch = random.nextDouble() * Math.PI;
         double speed = 0.1 + random.nextDouble() * 0.2;
         double vx = Math.sin(angle) * Math.cos(pitch) * speed;
         double vy = Math.sin(pitch) * speed;
         double vz = Math.cos(angle) * Math.cos(pitch) * speed;
         level.addParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get(), position.x, position.y, position.z, vx, vy, vz);
         level.addParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get(), position.x, position.y, position.z, vx * (double)0.5F, vy * (double)0.5F, vz * (double)0.5F);
      }

      level.addParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get(), position.x, position.y, position.z, (double)0.0F, 0.05, (double)0.0F);
   }

   private void renderParticles(PoseStack stack, MultiBufferSource buffer, Vec3 entityPos) {
      if (!this.activeParticles.isEmpty()) {
         stack.pushPose();

         for(SporeParticle particle : this.activeParticles) {
            Vec3 localPos = particle.position.subtract(entityPos);
            stack.pushPose();
            stack.translate(localPos.x, localPos.y, localPos.z);
            VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());
            float size = 0.05F;
            float alpha = Math.min(1.0F, (float)particle.life / 10.0F);
            int color = particle.color;
            int r = color >> 16 & 255;
            int g = color >> 8 & 255;
            int b = color & 255;
            int packedColor = (int)(alpha * 255.0F) << 24 | r << 16 | g << 8 | b;
            Matrix4f pose = stack.last().pose();
            consumer.vertex(pose, -size, -size, 0.0F).color(packedColor).uv(0.0F, 0.0F).uv2(0, 15728880).normal(1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(pose, size, -size, 0.0F).color(packedColor).uv(1.0F, 0.0F).uv2(0, 15728880).normal(1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(pose, size, size, 0.0F).color(packedColor).uv(1.0F, 1.0F).uv2(0, 15728880).normal(1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(pose, -size, size, 0.0F).color(packedColor).uv(0.0F, 1.0F).uv2(0, 15728880).normal(1.0F, 0.0F, 0.0F).endVertex();
            stack.popPose();
         }

         stack.popPose();
      }
   }

   static class VolatileGlowingLayers extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Gorgon> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/gorgon_light.png");
      private static final ResourceLocation TEXTURE_SPOOKY = new ResourceLocation("spore", "textures/entity/gorgon_light_spooky.png");

      public VolatileGlowingLayers(RenderLayerParent p_117346_) {
         super(p_117346_);
      }

      public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, Gorgon entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
         if (!entity.isInvisible()) {
            float alpha = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.1F);
            VertexConsumer vertexConsumer = buffer.getBuffer(SporeRenderTypes.glowingTranslucent(entity.spooky() ? TEXTURE_SPOOKY : TEXTURE));
            this.getParentModel().renderToBuffer(matrixStack, vertexConsumer, packedLight, 15728640, 1.0F, 1.0F, 1.0F, alpha);
         }

      }
   }

   private static class SporeParticle {
      Vec3 position;
      Vec3 velocity;
      int targetIndex;
      int life;
      boolean reachedEnd;
      double oldDistanceToEnd;
      int color;

      SporeParticle(Vec3 position, Vec3 velocity, int targetIndex, int color, int life) {
         this.position = position;
         this.velocity = velocity;
         this.targetIndex = targetIndex;
         this.color = color;
         this.life = life;
         this.reachedEnd = false;
         this.oldDistanceToEnd = Double.MAX_VALUE;
      }
   }
}
