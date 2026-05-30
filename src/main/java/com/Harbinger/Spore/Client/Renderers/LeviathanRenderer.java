package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.WaterCalamityCamo;
import com.Harbinger.Spore.Client.Models.LeviathanModel;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.FinPart1Model;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.FinPart2Model;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.FinPart3Model;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.FinPart4Model;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.FootSegLevi;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg1;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg2;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg3;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg4;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg5;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg6;
import com.Harbinger.Spore.Client.Special.CalamityRenderer;
import com.Harbinger.Spore.Sentities.BaseEntities.IkUtil.IkLeviLeg;
import com.Harbinger.Spore.Sentities.Calamities.Leviathan;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeviathanRenderer extends CalamityRenderer<Leviathan> {
   private final Seg1 tentacleSegmentModel1 = new Seg1();
   private final Seg2 tentacleSegmentModel2 = new Seg2();
   private final Seg3 tentacleSegmentModel3 = new Seg3();
   private final Seg4 tentacleSegmentModel4 = new Seg4();
   private final Seg5 tentacleSegmentModel5 = new Seg5();
   private final Seg6 tentacleSegmentModel6 = new Seg6();
   private final FootSegLevi foot = new FootSegLevi();
   private final FinPart1Model flip1 = new FinPart1Model();
   private final FinPart2Model flip2 = new FinPart2Model();
   private final FinPart3Model flip3 = new FinPart3Model();
   private final FinPart4Model flip4 = new FinPart4Model();
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/leviathan.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/leviathan.png");
   private static final ResourceLocation TENTACLES = new ResourceLocation("spore", "textures/entity/kraken/kraken_t1.png");
   private static final ResourceLocation FIN = new ResourceLocation("spore", "textures/entity/kraken/levi1.png");

   public LeviathanRenderer(EntityRendererProvider.Context context) {
      super(context, new LeviathanModel(), 4.0F);
      this.addLayer(new WaterCalamityCamo(this));
   }

   public EntityModel<Entity> getTentacleModel(int i) {
      EntityModel<Entity> var10000;
      switch (i) {
         case 1 -> var10000 = this.tentacleSegmentModel2;
         case 2 -> var10000 = this.tentacleSegmentModel3;
         case 3 -> var10000 = this.tentacleSegmentModel4;
         case 4 -> var10000 = this.tentacleSegmentModel5;
         case 5 -> var10000 = this.tentacleSegmentModel6;
         default -> var10000 = this.tentacleSegmentModel1;
      }

      return var10000;
   }

   public EntityModel<Entity> getFlipModel(int i) {
      EntityModel<Entity> var10000;
      switch (i) {
         case 1 -> var10000 = this.flip2;
         case 2 -> var10000 = this.flip3;
         case 3 -> var10000 = this.flip4;
         default -> var10000 = this.flip1;
      }

      return var10000;
   }

   public ResourceLocation getTextureLocation(Leviathan entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Leviathan entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int light) {
      super.render(entity, entityYaw, partialTicks, stack, bufferSource, light);
      Vec3 entityPos = entity.getPosition(partialTicks);
      stack.pushPose();
      stack.translate(-entityPos.x, -entityPos.y, -entityPos.z);
      if (!entity.isInvisible()) {
         for(IkLeviLeg leg : entity.getLegs()) {
            this.renderTentacle(stack, entity, light, bufferSource, leg.getEntities(), leg.getSegmentVar(), entity, partialTicks, false, false);
         }

         if (entity.getFins().length > 1) {
            this.renderTentacle(stack, entity, light, bufferSource, entity.getFins()[0].getEntities(), (int[])null, entity, partialTicks, true, true);
            this.renderTentacle(stack, entity, light, bufferSource, entity.getFins()[1].getEntities(), (int[])null, entity, partialTicks, true, false);
         }
      }

      stack.popPose();
   }

   private void renderTentacle(PoseStack stack, Leviathan type, int light, MultiBufferSource buffer, Vec3[] segments, int[] var, LivingEntity parent, float partial, boolean arm, boolean right) {
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
            int e = var == null ? i : var[i];
            Vec3 currentPos = segments[i];
            this.renderConnection(origin, currentPos, type, light, stack, buffer, i, e, partial, baseR, g, b, i == segments.length - 1, arm, right);
            origin = currentPos;
         }

      }
   }

   private void renderConnection(Vec3 from, Vec3 to, Leviathan parent, int light, PoseStack stack, MultiBufferSource buffer, int index, int var, float partial, float r, float g, float b, boolean last, boolean arm, boolean right) {
      if (from != null && to != null) {
         Vec3 direction = to.subtract(from);
         float length = (float)direction.length();
         if (!(length < 1.0E-4F)) {
            direction = direction.normalize();
            float yaw = (float)Math.atan2(direction.x, direction.z);
            float pitch = (float)(-Math.asin(direction.y));
            float size = arm ? (index % 2 == 0 ? 1.75F : 1.5F) : (index % 2 == 0 ? 1.2F : 1.0F);
            stack.pushPose();
            stack.translate(from.x, from.y, from.z);
            stack.mulPose(Axis.YP.rotation(yaw));
            stack.mulPose(Axis.XP.rotation(pitch));
            if (arm) {
               stack.mulPose(Axis.ZP.rotation(right ? 90.0F : -90.0F));
            }

            stack.pushPose();
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(arm ? FIN : TENTACLES));
            EntityModel<Entity> typeEntityModel = arm ? this.getFlipModel(var) : (last ? this.foot : this.getTentacleModel(var));
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
