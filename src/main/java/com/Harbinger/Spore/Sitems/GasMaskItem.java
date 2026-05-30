package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

public class GasMaskItem extends ArmorItem implements CustomModelArmorData {
   private final ResourceLocation location = new ResourceLocation("spore:textures/armor/gas_mask.png");

   public GasMaskItem() {
      super(new ArmorMaterial() {
         public int getDurabilityForType(Type p_266807_) {
            return 0;
         }

         public int getDefenseForType(Type p_267168_) {
            return 0;
         }

         public int getEnchantmentValue() {
            return 0;
         }

         public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_IRON;
         }

         public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
         }

         public String getName() {
            return "Gas Mask";
         }

         public float getToughness() {
            return 0.0F;
         }

         public float getKnockbackResistance() {
            return 0.0F;
         }
      }, Type.HELMET, (new Properties()).stacksTo(1));
      Sitems.TECHNOLOGICAL_ITEMS.add(this);
   }

   public boolean canBeDepleted() {
      return false;
   }

   public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
      return "spore:textures/entity/empty.png";
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return false;
   }

   public ResourceLocation getTextureLocation() {
      return this.location;
   }
}
