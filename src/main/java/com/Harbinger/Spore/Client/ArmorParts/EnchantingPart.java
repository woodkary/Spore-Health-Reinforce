package com.Harbinger.Spore.Client.ArmorParts;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantingPart {
   Enchantment getEnchantment();

   ResourceLocation getTexture();

   List blacklistedItems();
}
