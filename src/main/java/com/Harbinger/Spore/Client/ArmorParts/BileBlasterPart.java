package com.Harbinger.Spore.Client.ArmorParts;

import com.Harbinger.Spore.Client.Models.BileBlasterModel;
import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public class BileBlasterPart extends ComplexHandModelItem {
   public BileBlasterPart(InteractionHand slot, BileBlasterModel model, ModelPart part, float x, float y, float z, float expand, float xspin, float yspin, float zspin) {
      super(slot, (Item)Sitems.BILE_BLASTER.get(), model, part, x, y, z, expand, xspin, yspin, zspin);
   }

   public RenderType type(ResourceLocation location) {
      return RenderType.entityCutout(location);
   }
}
