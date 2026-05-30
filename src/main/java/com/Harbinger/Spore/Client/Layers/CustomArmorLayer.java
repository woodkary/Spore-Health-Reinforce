package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.ArmorModelList;
import com.Harbinger.Spore.Client.ArmorParts.BaseArmorRenderingBit;
import com.Harbinger.Spore.Client.ArmorParts.EnchantingPart;
import com.Harbinger.Spore.Core.SConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public class CustomArmorLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private final HumanoidModel origin = (HumanoidModel)this.getParentModel();

   public CustomArmorLayer(RenderLayerParent<T, M> p_117346_) {
      super(p_117346_);
   }

   public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float v5) {
      if (!((List)SConfig.SERVER.armor_blacklist.get()).contains(entity.getEncodeId())) {
         this.handleArmorPartsRendering(entity, poseStack, light, buffer, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         this.handleArmorEnchantingPartsRendering(entity, poseStack, light, buffer, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      }
   }

   protected void handleArmorPartsRendering(LivingEntity entity, PoseStack poseStack, int light, MultiBufferSource buffer, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      List<BaseArmorRenderingBit> parts = ArmorModelList.ARMOR_RENDERING_BITS;
      if (!parts.isEmpty()) {
         for(BaseArmorRenderingBit bit : parts) {
            bit.tickMovement(entity, poseStack, this.origin, light, buffer);
            ((EntityModel)bit.model.get()).setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks + (float)entity.tickCount, netHeadYaw, headPitch);
         }

      }
   }

   protected void handleArmorEnchantingPartsRendering(LivingEntity entity, PoseStack poseStack, int light, MultiBufferSource buffer, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      List<EnchantingPart> parts = ArmorModelList.ENCHANTING_RENDERING_BITS;
      if (!parts.isEmpty()) {
         for(EnchantingPart bit : parts) {
            if (bit instanceof BaseArmorRenderingBit) {
               BaseArmorRenderingBit armorRenderingBit = (BaseArmorRenderingBit)bit;
               if (!bit.blacklistedItems().contains(armorRenderingBit.stack(entity).getItem())) {
                  armorRenderingBit.tickMovement(entity, poseStack, this.origin, light, buffer);
                  ((EntityModel)armorRenderingBit.model.get()).setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks + (float)entity.tickCount, netHeadYaw, headPitch);
               }
            }
         }

      }
   }
}
