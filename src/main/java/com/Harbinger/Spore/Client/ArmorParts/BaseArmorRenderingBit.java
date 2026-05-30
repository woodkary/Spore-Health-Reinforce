package com.Harbinger.Spore.Client.ArmorParts;

import com.Harbinger.Spore.Sitems.CustomModelArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BaseArmorRenderingBit {
   public final EquipmentSlot slot;
   public final Item item;
   public final Supplier model;
   public final Supplier part;
   public final float x;
   public final float y;
   public final float z;
   public final float expand;
   public final float Xspin;
   public final float Yspin;
   public final float Zspin;

   public BaseArmorRenderingBit(EquipmentSlot slot, Item item, Supplier model, Supplier part, float x, float y, float z, float expand, float xspin, float yspin, float zspin) {
      this.slot = slot;
      this.item = item;
      this.model = model;
      this.part = part;
      this.x = x;
      this.y = y;
      this.z = z;
      this.expand = expand;
      this.Xspin = xspin;
      this.Yspin = yspin;
      this.Zspin = zspin;
   }

   public BaseArmorRenderingBit(EquipmentSlot slot, Item item, Supplier model, Supplier part, float x, float y, float z, float expand) {
      this(slot, item, model, part, x, y, z, expand, 0.0F, 0.0F, 0.0F);
   }

   public ItemStack stack(LivingEntity livingEntity) {
      return livingEntity.getItemBySlot(this.slot);
   }

   protected VertexConsumer consumer(MultiBufferSource source, CustomModelArmorData data, HumanoidModel model, LivingEntity livingEntity) {
      if (this instanceof EnchantingPart enchantingPart) {
         return ItemRenderer.getFoilBufferDirect(source, model.renderType(enchantingPart.getTexture()), false, this.stack(livingEntity).hasFoil());
      } else {
         return ItemRenderer.getFoilBufferDirect(source, model.renderType(data.getTextureLocation()), false, this.stack(livingEntity).hasFoil());
      }
   }

   public void tickMovement(LivingEntity livingEntity, PoseStack poseStack, HumanoidModel model, int light, MultiBufferSource buffer) {
      ItemStack itemStack = this.stack(livingEntity);
      Item item = itemStack.getItem();
      float red;
      float green;
      float blue;
      if (item instanceof SporeArmorData armorData) {
         int color = armorData.getVariant(itemStack).getColor();
         red = (float)(color >> 16 & 255) / 255.0F;
         green = (float)(color >> 8 & 255) / 255.0F;
         blue = (float)(color & 255) / 255.0F;
      } else {
         item = itemStack.getItem();
         if (item instanceof SporeWeaponData weaponData) {
            int color = weaponData.getVariant(itemStack).getColor();
            red = (float)(color >> 16 & 255) / 255.0F;
            green = (float)(color >> 8 & 255) / 255.0F;
            blue = (float)(color & 255) / 255.0F;
         } else {
            red = 1.0F;
            blue = 1.0F;
            green = 1.0F;
         }
      }

      if (this instanceof EnchantingPart enchantingPart) {
         if (itemStack.getEnchantmentLevel(enchantingPart.getEnchantment()) > 0) {
            VertexConsumer consumer = this.consumer(buffer, (CustomModelArmorData)null, model, livingEntity);
            this.applyTransformEx(poseStack, this.getPiece(model), this.x, this.y, this.z, this.expand, this.Xspin, this.Yspin, this.Zspin, () -> ((ModelPart)this.part.get()).render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F));
         }
      } else {
         item = itemStack.getItem();
         if (item instanceof CustomModelArmorData armorData) {
            if (itemStack.getItem().equals(this.item)) {
               VertexConsumer consumer = this.consumer(buffer, armorData, model, livingEntity);
               this.applyTransformEx(poseStack, this.getPiece(model), this.x, this.y, this.z, this.expand, this.Xspin, this.Yspin, this.Zspin, () -> ((ModelPart)this.part.get()).render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F));
            }
         }
      }

   }

   protected abstract ModelPart getPiece(HumanoidModel var1);

   protected void applyTransformEx(PoseStack poseStack, ModelPart origin, float x, float y, float z, float scale, float xSpin, float ySpin, float ZSpin, Runnable render) {
      poseStack.pushPose();
      origin.translateAndRotate(poseStack);
      poseStack.translate(x, y, z);
      poseStack.scale(scale, scale, scale);
      poseStack.mulPose(Axis.XP.rotationDegrees(xSpin));
      poseStack.mulPose(Axis.YP.rotationDegrees(ySpin));
      poseStack.mulPose(Axis.ZP.rotationDegrees(ZSpin));
      render.run();
      poseStack.popPose();
   }
}
