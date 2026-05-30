package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeBaseArmor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class PlatedExoskeleton extends SporeBaseArmor implements CustomModelArmorData {
   private static final ResourceLocation location = new ResourceLocation("spore:textures/armor/plated_armor_set.png");

   public PlatedExoskeleton(Type type) {
      super(type, new int[]{(Integer)SConfig.SERVER.boots_durability1.get(), (Integer)SConfig.SERVER.pants_durability1.get(), (Integer)SConfig.SERVER.chestplate_durability1.get(), (Integer)SConfig.SERVER.helmet_durability1.get()}, new int[]{(Integer)SConfig.SERVER.boots_protection1.get(), (Integer)SConfig.SERVER.pants_protection1.get(), (Integer)SConfig.SERVER.chestplate_protection1.get(), (Integer)SConfig.SERVER.helmet_protection1.get()}, (float)(Integer)SConfig.SERVER.armor_toughness1.get(), (float)(Integer)SConfig.SERVER.knockback_resistance1.get(), (SoundEvent)Ssounds.INFECTED_GEAR_EQUIP.get(), "Plated");
   }

   public boolean isValidRepairItem(ItemStack stack, ItemStack itemStack) {
      return super.isValidRepairItem(stack, itemStack) || itemStack.getItem() == Sitems.ARMOR_FRAGMENT.get();
   }

   public ResourceLocation getTextureLocation() {
      return location;
   }
}
