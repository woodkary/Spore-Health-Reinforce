package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.HarpoonModel;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.ChainModel;
import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import com.Harbinger.Spore.Sentities.Projectile.HarpoonProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HarpoonRenderer extends EntityRenderer<HarpoonProjectile> {
   public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("spore", "textures/entity/graken_ship.png");
   public static final ResourceLocation CHAIN_LOCATION = new ResourceLocation("spore", "textures/entity/chains.png");
   private final EntityModel chains = new ChainModel();
   private final HarpoonModel model = new HarpoonModel();
   protected List<Vec3> entities = new ArrayList<>();

   public HarpoonRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public void render(HarpoonProjectile spear, float p_116112_, float partial, PoseStack stack, MultiBufferSource bufferSource, int light) {
      stack.pushPose();
      stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial, spear.yRotO, spear.getYRot()) - 90.0F));
      stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial, spear.xRotO, spear.getXRot()) + 90.0F));
      VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(spear)));
      this.model.renderToBuffer(stack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      stack.popPose();
      super.render(spear, p_116112_, partial, stack, bufferSource, light);
      Entity entity = spear.level().getEntity(spear.getOwnerId());
      if (entity != null) {
         Vec3 vec3;
         if (entity instanceof Grakensenker) {
            Grakensenker grakensenker = (Grakensenker)entity;
            float yawRad = grakensenker.getYRot() * ((float)Math.PI / 180F);
            float spinRad = (float)grakensenker.getWaterTicks() * 0.05F;
            Vec3 offset = new Vec3((double)3.0F, (double)4.75F + (double)grakensenker.getExtendedHeight(), 0.65);
            vec3 = grakensenker.getPosition(partial).add(offset.yRot(-yawRad - ((float)Math.PI / 2F) + spinRad));
         } else {
            vec3 = entity.getPosition(partial);
         }

         Vec3 entityPos = spear.getPosition(partial);
         this.applyIK(partial, spear, vec3);
         stack.pushPose();
         stack.translate(-entityPos.x, -entityPos.y, -entityPos.z);
         this.renderChain(stack, light, bufferSource);
         stack.popPose();
      }
   }

   public ResourceLocation getTextureLocation(HarpoonProjectile p_116109_) {
      return TEXTURE_LOCATION;
   }

   protected void moveSegmentTowards(int index, Vec3 target, boolean far) {
      Vec3 currentPos = (Vec3)this.entities.get(index);
      Vec3 newPos = currentPos.lerp(target, (double)0.35F);
      this.entities.set(index, far ? target : newPos);
   }

   public void applyIK(float partial, HarpoonProjectile t, Vec3 camera) {
      Vec3 basePos = t.getPosition(partial);
      this.rebuildChain(basePos, camera);
      if (this.entities != null && this.entities.size() >= 3) {
         this.moveSegmentTowards(this.entities.size() - 1, camera, true);

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
            this.moveSegmentTowards(i, solvedPos, ((Vec3)this.entities.get(i + 1)).distanceTo((Vec3)this.entities.get(i)) > (double)10.0F);
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
            this.moveSegmentTowards(i, solvedPos, ((Vec3)this.entities.get(i - 1)).distanceTo((Vec3)this.entities.get(i)) > (double)10.0F);
         }

      }
   }

   private void rebuildChain(Vec3 start, Vec3 end) {
      double distance = start.distanceTo(end);
      int desiredSegments = Mth.clamp((int)(distance * 1.1), 4, 40);
      if (desiredSegments != this.entities.size()) {
         this.entities.clear();

         for(int i = 0; i < desiredSegments; ++i) {
            double t = (double)i / (double)(desiredSegments - 1);
            this.entities.add(new Vec3(Mth.lerp(t, start.x, end.x), Mth.lerp(t, start.y, end.y), Mth.lerp(t, start.z, end.z)));
         }

      }
   }

   private void renderChain(PoseStack stack, int light, MultiBufferSource buffer) {
      if (this.entities != null && this.entities.size() >= 2) {
         Vec3 origin = null;

         for(int i = 0; i < this.entities.size(); ++i) {
            Vec3 currentPos = (Vec3)this.entities.get(i);
            this.renderConnection(origin, currentPos, light, stack, buffer, i);
            origin = currentPos;
         }

      }
   }

   private void renderConnection(Vec3 from, Vec3 to, int light, PoseStack stack, MultiBufferSource buffer, int index) {
      if (from != null && to != null) {
         Vec3 direction = to.subtract(from);
         float length = (float)direction.length();
         if (!(length < 1.0E-4F)) {
            direction = direction.normalize();
            float yaw = (float)Math.atan2(direction.x, direction.z);
            float pitch = (float)(-Math.asin(direction.y));
            float size = index % 2 == 0 ? 1.2F : 1.0F;
            stack.pushPose();
            stack.translate(from.x, from.y, from.z);
            stack.mulPose(Axis.YP.rotation(yaw));
            stack.mulPose(Axis.XP.rotation(pitch));
            stack.pushPose();
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(CHAIN_LOCATION));
            stack.mulPose(Axis.XP.rotationDegrees(90.0F));
            stack.translate(0.0F, -length / 2.0F, 0.0F);
            stack.scale(size, length * 1.05F, size);
            this.chains.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            stack.popPose();
            stack.popPose();
         }
      }
   }
}
