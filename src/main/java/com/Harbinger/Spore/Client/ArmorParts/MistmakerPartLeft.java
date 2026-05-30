package com.Harbinger.Spore.Client.ArmorParts;

import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Sitems.CustomModelArmorData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Supplier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public class MistmakerPartLeft extends BaseArmorRenderingBit {
   public MistmakerPartLeft(Supplier model, Supplier part, float x, float y, float z, float expand) {
      super(EquipmentSlot.OFFHAND, (Item)Sitems.MISTMAKER.get(), model, part, x, y, z, expand);
   }

   protected VertexConsumer consumer(MultiBufferSource source, CustomModelArmorData data, HumanoidModel model, LivingEntity livingEntity) {
      return ItemRenderer.getFoilBufferDirect(source, RenderType.entityCutoutNoCull(data.getTextureLocation()), false, this.stack(livingEntity).hasFoil());
   }

   protected ModelPart getPiece(HumanoidModel model) {
      return model.leftArm;
   }
}
