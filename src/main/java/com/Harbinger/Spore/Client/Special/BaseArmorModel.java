package com.Harbinger.Spore.Client.Special;

import com.Harbinger.Spore.Core.Seffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BaseArmorModel extends EntityModel<LivingEntity> {
   public void setupAnim(LivingEntity p_102618_, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_) {
   }

   public void animateCrouch(LivingEntity entity, ModelPart body) {
      if (entity.isCrouching()) {
         body.xRot = 0.5F;
         body.y = 3.2F;
      } else {
         body.xRot = 0.0F;
         body.y = 0.0F;
      }

   }

   public void animateCore(LivingEntity entity, ModelPart part, float value) {
      if (entity.isCrouching()) {
         part.xRot = 0.5F;
         part.y = 7.2F;
         part.z = -1.25F;
      } else {
         part.xRot = 0.0F;
         part.y = 3.0F;
         part.z = -2.25F;
      }

      if (entity.hasEffect((MobEffect)Seffects.SYMBIOSIS.get())) {
         part.yScale = 1.0F + Mth.cos(value / 6.0F) / 6.0F;
         part.zScale = 1.0F + Mth.cos(value / 6.0F) / 6.0F;
         part.xScale = 1.0F + Mth.sin(value / 6.0F) / 6.0F;
      }

   }

   public void animatedElytra(LivingEntity entity, ModelPart rightWing, ModelPart leftWing) {
      float f = 0.2617994F;
      float f1 = -0.2617994F;
      float f2 = 0.0F;
      float f3 = 0.0F;
      if (entity.isFallFlying()) {
         float f4 = 1.0F;
         Vec3 vec3 = entity.getDeltaMovement();
         if (vec3.y < (double)0.0F) {
            Vec3 vec31 = vec3.normalize();
            f4 = 1.0F - (float)Math.pow(-vec31.y, (double)1.5F);
         }

         f = f4 * 0.34906584F + (1.0F - f4) * f;
         f1 = f4 * (-(float)Math.PI / 2F) + (1.0F - f4) * f1;
      } else if (entity.isCrouching()) {
         f = 0.6981317F;
         f1 = (-(float)Math.PI / 4F);
         f2 = 3.0F;
         f3 = 0.08726646F;
      }

      leftWing.y = f2;
      if (entity instanceof AbstractClientPlayer abstractclientplayer) {
         abstractclientplayer.elytraRotX += (f - abstractclientplayer.elytraRotX) * 0.1F;
         abstractclientplayer.elytraRotY += (f3 - abstractclientplayer.elytraRotY) * 0.1F;
         abstractclientplayer.elytraRotZ += (f1 - abstractclientplayer.elytraRotZ) * 0.1F;
         leftWing.xRot = abstractclientplayer.elytraRotX;
         leftWing.yRot = abstractclientplayer.elytraRotY;
         leftWing.zRot = abstractclientplayer.elytraRotZ;
      } else {
         leftWing.xRot = f;
         leftWing.zRot = f1;
         leftWing.yRot = f3;
      }

      rightWing.yRot = -leftWing.yRot;
      rightWing.y = leftWing.y;
      rightWing.xRot = leftWing.xRot;
      rightWing.zRot = -leftWing.zRot;
   }

   public void renderToBuffer(PoseStack p_103111_, VertexConsumer p_103112_, int p_103113_, int p_103114_, float p_103115_, float p_103116_, float p_103117_, float p_103118_) {
   }
}
