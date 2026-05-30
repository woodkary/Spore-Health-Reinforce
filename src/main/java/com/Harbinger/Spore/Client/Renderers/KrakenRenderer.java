package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.SpecialEffects;
import com.Harbinger.Spore.Client.Layers.GrakenMembraneLayer;
import com.Harbinger.Spore.Client.Layers.GrakenShipLayer;
import com.Harbinger.Spore.Client.Layers.WaterCalamityCamo;
import com.Harbinger.Spore.Client.Models.GrakensenkerModel;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.FootSeg;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.HandSeg1;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.HandSeg2;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg1;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg10;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg11;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg12;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg2;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg3;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg4;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg5;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg6;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg7;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg8;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg9;
import com.Harbinger.Spore.Client.Special.CalamityRenderer;
import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class KrakenRenderer extends CalamityRenderer<Grakensenker> {
   private final Seg1 tentacleSegmentModel1 = new Seg1();
   private final Seg2 tentacleSegmentModel2 = new Seg2();
   private final Seg3 tentacleSegmentModel3 = new Seg3();
   private final Seg4 tentacleSegmentModel4 = new Seg4();
   private final Seg5 tentacleSegmentModel5 = new Seg5();
   private final Seg6 tentacleSegmentModel6 = new Seg6();
   private final Seg7 tentacleSegmentModel7 = new Seg7();
   private final Seg8 tentacleSegmentModel8 = new Seg8();
   private final Seg9 tentacleSegmentModel9 = new Seg9();
   private final Seg10 tentacleSegmentModel10 = new Seg10();
   private final Seg11 tentacleSegmentModel11 = new Seg11();
   private final Seg12 tentacleSegmentModel12 = new Seg12();
   private final FootSeg foot = new FootSeg();
   private final HandSeg1 armModel1 = new HandSeg1();
   private final HandSeg2 armModel2 = new HandSeg2();
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/graken.png");
   private static final ResourceLocation TENTACLES = new ResourceLocation("spore", "textures/entity/kraken/kraken_t1.png");
   private static final ResourceLocation KRAKEN_HAND = new ResourceLocation("spore", "textures/entity/kraken/hand.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/graken.png");
   private static final ResourceLocation WATER = new ResourceLocation("spore", "textures/entity/vortex/water_vortex.png");
   private static final ResourceLocation WATER_RIPTIDE = new ResourceLocation("spore", "textures/entity/vortex/vortex_riptide.png");

   public KrakenRenderer(EntityRendererProvider.Context context) {
      super(context, new GrakensenkerModel(), 4.0F);
      this.addLayer(new GrakenMembraneLayer(this));
      this.addLayer(new WaterCalamityCamo(this));
      this.addLayer(new GrakenShipLayer(this));
   }

   public ResourceLocation getTextureLocation(Grakensenker entity) {
      return TEXTURE;
   }

   public EntityModel<Entity> getTentacleModel(int i) {
      EntityModel<Entity> var10000;
      switch (i) {
         case 0 -> var10000 = this.tentacleSegmentModel1;
         case 1 -> var10000 = this.tentacleSegmentModel2;
         case 2 -> var10000 = this.tentacleSegmentModel3;
         case 3 -> var10000 = this.tentacleSegmentModel4;
         case 4 -> var10000 = this.tentacleSegmentModel5;
         case 5 -> var10000 = this.tentacleSegmentModel6;
         case 6 -> var10000 = this.tentacleSegmentModel7;
         case 7 -> var10000 = this.tentacleSegmentModel8;
         case 8 -> var10000 = this.tentacleSegmentModel9;
         case 9 -> var10000 = this.tentacleSegmentModel10;
         case 10 -> var10000 = this.tentacleSegmentModel11;
         default -> var10000 = this.tentacleSegmentModel12;
      }

      return var10000;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Grakensenker entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int light) {
      int color = ((Biome)entity.level().getBiome(entity.getOnPos()).value()).getWaterColor();
      int packedColor = color | -16777216;
      Entity camera = Minecraft.getInstance().getCameraEntity();
      float time = ((float)entity.tickCount + partialTicks) * -0.25F;
      float time2 = ((float)entity.tickCount + partialTicks) * -0.35F;
      stack.pushPose();
      stack.translate(0.0F, entity.getExtendedHeight(), 0.0F);
      stack.mulPose(Axis.YP.rotationDegrees((float)entity.getWaterTicks()));
      super.render(entity, entityYaw, partialTicks, stack, bufferSource, light);
      stack.popPose();
      Vec3 entityPos = entity.getPosition(partialTicks);
      stack.pushPose();
      stack.translate(-entityPos.x, -entityPos.y, -entityPos.z);
      if (!entity.isInvisible()) {
         this.renderTentacle(stack, entity, light, bufferSource, entity.getBackRightTentacle().getEntities(), entity.getBackRightTentacle().getSegmentVar(), entity, partialTicks, false, false);
         this.renderTentacle(stack, entity, light, bufferSource, entity.getBackLeftTentacle().getEntities(), entity.getBackLeftTentacle().getSegmentVar(), entity, partialTicks, false, false);
         this.renderTentacle(stack, entity, light, bufferSource, entity.getMiddleLeftTentacle().getEntities(), entity.getMiddleLeftTentacle().getSegmentVar(), entity, partialTicks, false, false);
         this.renderTentacle(stack, entity, light, bufferSource, entity.getMiddleRightTentacle().getEntities(), entity.getMiddleRightTentacle().getSegmentVar(), entity, partialTicks, false, false);
         this.renderTentacle(stack, entity, light, bufferSource, entity.getFrontLeftTentacle().getEntities(), entity.getFrontLeftTentacle().getSegmentVar(), entity, partialTicks, false, false);
         this.renderTentacle(stack, entity, light, bufferSource, entity.getFrontRightTentacle().getEntities(), entity.getFrontRightTentacle().getSegmentVar(), entity, partialTicks, false, false);
         this.renderTentacle(stack, entity, light, bufferSource, entity.getRightArmTentacle().getEntities(), entity.getRightArmTentacle().getSegmentVar(), entity, partialTicks, true, false);
         this.renderTentacle(stack, entity, light, bufferSource, entity.getLeftArmTentacle().getEntities(), entity.getLeftArmTentacle().getSegmentVar(), entity, partialTicks, true, true);
      }

      if (entity.hasVortex() && entity.getVortexTimeOut() <= 0 && camera != null && camera.isEyeInFluidType(Fluids.WATER.getFluidType())) {
         PoseStack.Pose pose = stack.last();
         Matrix4f matrix4f = pose.pose();
         Matrix3f matrix3f = pose.normal();
         SpecialEffects.renderFunnel(matrix4f, matrix3f, light, bufferSource, entity.getVortexFunnel().getEntities(), time, packedColor, 1.0F, WATER);
         SpecialEffects.renderFunnel(matrix4f, matrix3f, light, bufferSource, entity.getVortexFunnel().getEntities(), time, -1, 1.1F, WATER_RIPTIDE);
         SpecialEffects.renderFunnel(matrix4f, matrix3f, light, bufferSource, entity.getVortexFunnel().getEntities(), time2, packedColor, 0.9F, WATER);
      }

      stack.popPose();
   }

   private void renderTentacle(PoseStack stack, Grakensenker type, int light, MultiBufferSource buffer, Vec3[] segments, int[] var, LivingEntity parent, float partial, boolean arm, boolean right) {
      if (segments != null && segments.length >= 2) {
         float hurtTime = (float)parent.hurtTime - partial;
         float flashIntensity = 0.0F;
         int mutationColor = type.getMutationColor() == 0 ? -1 : type.getMutationColor();
         Vec3 origin = null;
         if (hurtTime > 0.0F) {
            flashIntensity = Math.min(hurtTime / 10.0F, 1.0F);
         }

         float baseR = (float)(mutationColor >> 16 & 255) / 255.0F;
         float baseG = (float)(mutationColor >> 8 & 255) / 255.0F;
         float baseB = (float)(mutationColor & 255) / 255.0F;
         float flash = flashIntensity * 0.5F;
         float g = Mth.lerp(flash, baseG, 0.2F);
         float b = Mth.lerp(flash, baseB, 0.2F);

         for(int i = 0; i < segments.length; ++i) {
            Vec3 currentPos = segments[i];
            this.renderConnection(origin, currentPos, type, light, stack, buffer, i, var[i], partial, baseR, g, b, i == segments.length - 1, arm, right);
            origin = currentPos;
         }

      }
   }

   private void renderConnection(Vec3 from, Vec3 to, Grakensenker parent, int light, PoseStack stack, MultiBufferSource buffer, int index, int var, float partial, float r, float g, float b, boolean last, boolean arm, boolean right) {
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
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(arm ? KRAKEN_HAND : TENTACLES));
            EntityModel<Entity> typeEntityModel = last ? (arm ? (right ? this.armModel1 : this.armModel2) : this.foot) : this.getTentacleModel(var);
            stack.mulPose(Axis.XP.rotationDegrees(90.0F));
            stack.translate(0.0F, -length / 2.0F, 0.0F);
            stack.scale(size, length * 1.05F, size);
            typeEntityModel.setupAnim(parent, 0.0F, 0.0F, (float)parent.tickCount + partial, 0.0F, 0.0F);
            typeEntityModel.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            stack.popPose();
            stack.popPose();
         }
      }
   }
}
