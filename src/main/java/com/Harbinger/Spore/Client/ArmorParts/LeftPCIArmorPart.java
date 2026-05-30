package com.Harbinger.Spore.Client.ArmorParts;

import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Sitems.CustomModelArmorData;
import com.Harbinger.Spore.Sitems.PCI;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Supplier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LeftPCIArmorPart extends BaseArmorRenderingBit {
   private final ResourceLocation location;

   public LeftPCIArmorPart(Supplier model, Supplier part, ResourceLocation location) {
      super(EquipmentSlot.OFFHAND, (Item)Sitems.PCI.get(), model, part, 0.15F, -0.35F, 0.05F, 1.0F);
      this.location = location;
   }

   protected ModelPart getPiece(HumanoidModel model) {
      return model.leftArm;
   }

   public void tickMovement(LivingEntity livingEntity, PoseStack poseStack, HumanoidModel model, int light, MultiBufferSource buffer) {
      ItemStack itemStack = this.stack(livingEntity);
      Item var8 = itemStack.getItem();
      if (var8 instanceof CustomModelArmorData armorData) {
         if (itemStack.getItem().equals(this.item)) {
            VertexConsumer consumer = this.consumer(buffer, armorData, model, livingEntity);
            this.applyTransformEx(poseStack, this.getPiece(model), this.x, this.y, this.z, this.expand, this.Xspin, this.Yspin, this.Zspin, () -> {
               ((ModelPart)this.part.get()).render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
               VertexConsumer vertexConsumer = this.glowConsumer(buffer, livingEntity);
               if (vertexConsumer != null) {
                  ((ModelPart)this.part.get()).render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
               }

            });
         }
      }

   }

   private VertexConsumer glowConsumer(MultiBufferSource bufferSource, LivingEntity livingEntity) {
      ItemStack stack = this.stack(livingEntity);
      Item var5 = stack.getItem();
      if (var5 instanceof PCI pci) {
         if (pci.getCharge(stack) > 0) {
            return bufferSource.getBuffer(RenderType.eyes(this.location));
         }
      }

      return null;
   }
}
