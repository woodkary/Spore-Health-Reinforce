package com.Harbinger.Spore.Client.ArmorParts;

import com.Harbinger.Spore.Client.Layers.CustomHorseArmorLayer;
import com.Harbinger.Spore.Client.Models.FleshHorseArmorModel;
import com.Harbinger.Spore.Sitems.CustomModelArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FleshHorseArmorBit extends HorseArmorBit {
   private final FleshHorseArmorModel fleshHorseArmorModel = new FleshHorseArmorModel();

   public FleshHorseArmorBit(Item item) {
      super(item);
   }

   public EntityModel getModel() {
      return this.fleshHorseArmorModel;
   }

   public void tickMovement(AbstractHorse livingEntity, PoseStack poseStack, CustomHorseArmorLayer.HorseHandlerModel model, int light, MultiBufferSource buffer) {
      ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
      if (!itemStack.isEmpty()) {
         Item item = itemStack.getItem();
         float red;
         float green;
         float blue;
         if (item instanceof SporeArmorData) {
            SporeArmorData armorData = (SporeArmorData)item;
            int color = armorData.getVariant(itemStack).getColor();
            red = (float)(color >> 16 & 255) / 255.0F;
            green = (float)(color >> 8 & 255) / 255.0F;
            blue = (float)(color & 255) / 255.0F;
         } else {
            red = 1.0F;
            blue = 1.0F;
            green = 1.0F;
         }

         item = itemStack.getItem();
         if (item instanceof CustomModelArmorData) {
            CustomModelArmorData armorData = (CustomModelArmorData)item;
            if (itemStack.getItem().equals(this.item)) {
               VertexConsumer consumer = this.consumer(buffer, armorData, model, livingEntity);
               ModelPart head = this.fleshHorseArmorModel.Neck;
               ModelPart body = this.fleshHorseArmorModel.Body;
               ModelPart rightFront = this.fleshHorseArmorModel.FrontRightLeg;
               ModelPart leftFront = this.fleshHorseArmorModel.FrontLeftLeg;
               ModelPart rightBack = this.fleshHorseArmorModel.BackRightLeg;
               ModelPart leftBack = this.fleshHorseArmorModel.BackLeftLeg;
               this.applyTransformEx(poseStack, model.getHead(), 0.0F, -0.05F, 0.64F, 1.15F, 0.0F, 0.0F, 0.0F, () -> head.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F));
               this.applyTransformEx(poseStack, model.getBody(), 0.0F, -0.71F, -0.35F, 1.05F, 0.0F, 0.0F, 0.0F, () -> body.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F));
               this.applyTransformEx(poseStack, model.getRightFrontLeg(), 0.275F, -1.0F, 0.625F, 1.1F, 0.0F, 0.0F, 0.0F, () -> rightFront.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F));
               this.applyTransformEx(poseStack, model.getLeftFrontLeg(), -0.275F, -1.0F, 0.625F, 1.1F, 0.0F, 0.0F, 0.0F, () -> leftFront.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F));
               this.applyTransformEx(poseStack, model.getRightHindLeg(), 0.275F, -1.0F, -0.55F, 1.1F, 0.0F, 0.0F, 0.0F, () -> rightBack.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F));
               this.applyTransformEx(poseStack, model.getLeftHindLeg(), -0.275F, -1.0F, -0.55F, 1.1F, 0.0F, 0.0F, 0.0F, () -> leftBack.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F));
            }
         }

      }
   }
}
