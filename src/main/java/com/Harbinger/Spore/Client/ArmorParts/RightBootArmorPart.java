package com.Harbinger.Spore.Client.ArmorParts;

import java.util.function.Supplier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public class RightBootArmorPart extends BaseArmorRenderingBit {
   public RightBootArmorPart(Item item, Supplier model, Supplier part, float x, float y, float z, float expand) {
      super(EquipmentSlot.FEET, item, model, part, x, y, z, expand);
   }

   protected ModelPart getPiece(HumanoidModel model) {
      return model.rightLeg;
   }
}
