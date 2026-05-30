package com.Harbinger.Spore.Sitems;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import org.jetbrains.annotations.Nullable;

public class InfectedHelmet extends InfectedExoskeleton {
   public InfectedHelmet() {
      super(Type.HELMET);
   }

   public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
      return "spore:textures/entity/empty.png";
   }
}
