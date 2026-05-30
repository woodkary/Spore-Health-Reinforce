package com.Harbinger.Spore.Client.ArmorParts;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public class LeftArmArmorPartEnchant extends BaseArmorRenderingBit implements EnchantingPart {
   private final Enchantment enchantment;
   private final ResourceLocation location;
   private final List blacklist;

   public LeftArmArmorPartEnchant(Supplier model, Supplier part, float x, float y, float z, float expand, Enchantment enchantment, ResourceLocation location, List blacklist) {
      super(EquipmentSlot.CHEST, (Item)null, model, part, x, y, z, expand);
      this.enchantment = enchantment;
      this.location = location;
      this.blacklist = blacklist;
   }

   protected ModelPart getPiece(HumanoidModel model) {
      return model.leftArm;
   }

   public Enchantment getEnchantment() {
      return this.enchantment;
   }

   public ResourceLocation getTexture() {
      return this.location;
   }

   public List blacklistedItems() {
      return this.blacklist;
   }
}
