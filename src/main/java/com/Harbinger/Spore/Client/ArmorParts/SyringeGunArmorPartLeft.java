package com.Harbinger.Spore.Client.ArmorParts;

import com.Harbinger.Spore.Client.Models.SyringeGunModelArm;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Sitems.CustomModelArmorData;
import com.Harbinger.Spore.Sitems.SyringeGun;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SyringeGunArmorPartLeft extends BaseArmorRenderingBit {
   private final SyringeGunModelArm parentModel;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore:textures/item/syringe_gun_juice.png");
   private static final ResourceLocation TEXTURET = new ResourceLocation("spore:textures/item/syringe_gun_syringe.png");

   public SyringeGunArmorPartLeft(SyringeGunModelArm parentModel, Supplier model, Supplier part, float x, float y, float z, float expand) {
      super(EquipmentSlot.OFFHAND, (Item)Sitems.SYRINGE_GUN.get(), model, part, x, y, z, expand);
      this.parentModel = parentModel;
   }

   protected VertexConsumer consumer(MultiBufferSource source, CustomModelArmorData data, HumanoidModel model, LivingEntity livingEntity) {
      return ItemRenderer.getFoilBufferDirect(source, RenderType.entityTranslucent(data.getTextureLocation()), false, this.stack(livingEntity).hasFoil());
   }

   protected ModelPart getPiece(HumanoidModel model) {
      return model.leftArm;
   }

   public void tickMovement(LivingEntity livingEntity, PoseStack poseStack, HumanoidModel model, int light, MultiBufferSource buffer) {
      super.tickMovement(livingEntity, poseStack, model, light, buffer);
      ItemStack stack = livingEntity.getOffhandItem();
      if (stack.getItem().equals(this.item)) {
         Item var8 = stack.getItem();
         if (var8 instanceof SyringeGun) {
            SyringeGun syringeGun = (SyringeGun)var8;
            List<Integer> clipColors = syringeGun.getClip(stack);
            this.applyTransformEx(poseStack, this.getPiece(model), this.x, this.y, this.z, this.expand, this.Xspin, this.Yspin, this.Zspin, () -> {
               this.handleColorRendering(this.parentModel.syringe, (Integer)clipColors.get(0), poseStack, buffer, light);
               this.handleColorRendering(this.parentModel.syringe2, (Integer)clipColors.get(1), poseStack, buffer, light);
               this.handleColorRendering(this.parentModel.syringe3, (Integer)clipColors.get(2), poseStack, buffer, light);
               this.handleColorRendering(this.parentModel.syringe4, (Integer)clipColors.get(3), poseStack, buffer, light);
            });
         }
      }

   }

   public void handleColorRendering(ModelPart syringe, int color, PoseStack stack, MultiBufferSource source, int light) {
      if (color != 0) {
         float r = (float)(color >> 16 & 255) / 255.0F;
         float g = (float)(color >> 8 & 255) / 255.0F;
         float b = (float)(color & 255) / 255.0F;
         VertexConsumer consumer = source.getBuffer(RenderType.entityCutout(TEXTURE));
         VertexConsumer consumerS = source.getBuffer(RenderType.entityTranslucent(TEXTURET));
         stack.pushPose();
         this.parentModel.syringeGun.translateAndRotate(stack);
         this.parentModel.magazine.translateAndRotate(stack);
         if (color != -1) {
            syringe.render(stack, consumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
         }

         syringe.render(stack, consumerS, light, OverlayTexture.NO_OVERLAY);
         stack.popPose();
      }
   }
}
