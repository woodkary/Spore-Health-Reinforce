package com.Harbinger.Spore.Sitems.BaseWeapons;

import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

public abstract class SporeBaseArmor extends ArmorItem implements SporeArmorData {
   public final EnumMap UUIDS = (EnumMap)Util.make(Maps.newEnumMap(EquipmentSlot.class), (p_114874_) -> {
      p_114874_.put(EquipmentSlot.HEAD, UUID.fromString("bcd36ebc-8e7e-4598-b35d-de98063823f3"));
      p_114874_.put(EquipmentSlot.CHEST, UUID.fromString("8e55d62a-8302-4422-9f70-a1cb4efbb93e"));
      p_114874_.put(EquipmentSlot.LEGS, UUID.fromString("5424cab4-14d5-49c2-a4f0-b6778a728a0a"));
      p_114874_.put(EquipmentSlot.FEET, UUID.fromString("8cea5f8e-4b19-43ba-85de-91d49faf8c28"));
   });
   protected final int[] protection;
   protected final float toughness;
   protected float knockback;

   public SporeBaseArmor(Type type, final int[] durability, int[] protection, float toughness, float knockback, final SoundEvent soundEvent, final String name) {
      super(new ArmorMaterial() {
         public int getDurabilityForType(Type type) {
            return durability[type.getSlot().getIndex()];
         }

         public int getDefenseForType(Type type) {
            return 0;
         }

         public int getEnchantmentValue() {
            return 0;
         }

         public SoundEvent getEquipSound() {
            return soundEvent;
         }

         public Ingredient getRepairIngredient() {
            return Ingredient.of(new ItemLike[]{(ItemLike)Sitems.BIOMASS.get()});
         }

         public String getName() {
            return name;
         }

         public float getToughness() {
            return 0.0F;
         }

         public float getKnockbackResistance() {
            return 0.0F;
         }
      }, type, new Properties());
      Sitems.TINTABLE_ITEMS.add(this);
      Sitems.BIOLOGICAL_ITEMS.add(this);
      this.protection = protection;
      this.toughness = toughness;
      this.knockback = knockback;
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      UUID uuid = (UUID)this.UUIDS.get(slot);
      builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", this.calculateTrueDefense(stack, (double)this.protection[this.type.getSlot().getIndex()]) + this.modifyProtection(stack, (double)this.protection[this.type.getSlot().getIndex()]), Operation.ADDITION));
      builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor modifier", this.calculateTrueToughness(stack, (double)this.toughness) + this.modifyToughness(stack, (double)this.toughness), Operation.ADDITION));
      if (this.knockback > 0.0F) {
         builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", ((double)this.knockback + this.modifyKnockbackResistance(stack, (double)this.knockback)) * (double)0.1F, Operation.ADDITION));
      }

      if (this.getVariant(stack) == SporeArmorMutations.DROWNED) {
         builder.put((Attribute)ForgeMod.SWIM_SPEED.get(), new AttributeModifier(uuid, "Armor Speed modifier", (double)0.25F, Operation.ADDITION));
      }

      if (this.getVariant(stack) == SporeArmorMutations.REINFORCED || this.getVariant(stack) == SporeArmorMutations.SKELETAL) {
         builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "Armor Speed modifier", this.getVariant(stack) == SporeArmorMutations.REINFORCED ? -0.01 : 0.01, Operation.ADDITION));
      }

      return slot == this.type.getSlot() && this.tooHurt(stack) ? builder.build() : ImmutableMultimap.of();
   }

   public int damageItem(ItemStack stack, int amount, LivingEntity entity, Consumer onBroken) {
      int durabilityLeft = stack.getMaxDamage() - stack.getDamageValue();
      if (durabilityLeft - amount <= 11) {
         entity.playSound((SoundEvent)Ssounds.INFECTED_GEAR_BREAK.get());
      }

      if (this.tooHurt(stack)) {
         if (this.getAdditionalDurability(stack) > 0) {
            this.hurtExtraDurability(stack, amount, entity);
            return 0;
         } else {
            return super.damageItem(stack, this.calculateDurabilityLost(stack, amount), entity, onBroken);
         }
      } else {
         return 0;
      }
   }

   public int calculateDurabilityLost(ItemStack stack, int value) {
      return this.getVariant(stack) == SporeArmorMutations.CHARRED ? value * 2 : value;
   }

   public int getEnchantmentValue(ItemStack stack) {
      int luck = this.getLuck(stack);
      return luck > 0 ? luck : 1;
   }

   public double modifyProtection(ItemStack stack, double value) {
      if (this.getVariant(stack) == SporeArmorMutations.REINFORCED) {
         return value * (double)0.2F;
      } else {
         return this.getVariant(stack) == SporeArmorMutations.SKELETAL ? value * (double)-0.2F : (double)0.0F;
      }
   }

   public double modifyToughness(ItemStack stack, double value) {
      return this.getVariant(stack) == SporeArmorMutations.SKELETAL ? (double)1.0F : (double)0.0F;
   }

   public double modifyKnockbackResistance(ItemStack stack, double value) {
      return (double)0.0F;
   }

   public float calculateAdditionalDamage(DamageSource source, ItemStack stack, float value) {
      if (this.getVariant(stack) == SporeArmorMutations.CHARRED && source.is(DamageTypeTags.IS_FIRE)) {
         return -value * 0.25F;
      } else {
         return this.getVariant(stack) == SporeArmorMutations.DROWNED && source.is(DamageTypeTags.IS_FIRE) ? value * 0.25F : 0.0F;
      }
   }

   public void appendHoverText(ItemStack stack, @Nullable Level p_41422_, List components, TooltipFlag p_41424_) {
      super.appendHoverText(stack, p_41422_, components, p_41424_);
      if (!this.tooHurt(stack)) {
         components.add(Component.translatable("spore.item.hurt").withStyle(ChatFormatting.RED));
      }

      if (Screen.hasShiftDown()) {
         if (this.getAdditionalProtection(stack) > (double)0.0F) {
            String var10001 = Component.translatable("spore.item.armor_increase").getString();
            components.add(Component.literal(var10001 + this.getAdditionalProtection(stack) + "%"));
         }

         if (this.getAdditionalToughness(stack) > (double)0.0F) {
            String var5 = Component.translatable("spore.item.toughness_increase").getString();
            components.add(Component.literal(var5 + this.getAdditionalToughness(stack) + "%"));
         }

         if (this.getMaxAdditionalDurability(stack) > 0) {
            String var6 = Component.translatable("spore.item.durability_increase").getString();
            components.add(Component.literal(var6 + this.getMaxAdditionalDurability(stack) + "%"));
         }

         if (this.getAdditionalDurability(stack) > 0) {
            String var7 = Component.translatable("spore.item.additional_durability").getString();
            components.add(Component.literal(var7 + this.getAdditionalDurability(stack)));
         }

         if (this.getEnchantmentValue(stack) > 0) {
            String var8 = Component.translatable("spore.item.enchant").getString();
            components.add(Component.literal(var8 + this.getEnchantmentValue(stack)));
         }

         if (this.getVariant(stack) != SporeArmorMutations.DEFAULT) {
            String var9 = Component.translatable("spore.item.mutation").getString();
            components.add(Component.literal(var9 + Component.translatable(this.getVariant(stack).getName()).getString()));
         }
      }

   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack itemStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
      if (clickAction == ClickAction.SECONDARY && stack.getEnchantmentLevel((Enchantment)Senchantments.VORACIOUS_MAW.get()) > 0 && stack.getDamageValue() > 0) {
         if (!itemStack.isEdible()) {
            return false;
         }

         FoodProperties properties = itemStack.getItem().getFoodProperties(itemStack, (LivingEntity)null);
         if (properties != null && properties.isMeat()) {
            stack.setDamageValue(this.getDamage(stack) - 50);
            itemStack.shrink(1);
            player.playNotifySound(SoundEvents.GENERIC_EAT, SoundSource.AMBIENT, 1.0F, 1.0F);
            return true;
         }
      }

      boolean shouldOverride = clickAction == ClickAction.SECONDARY && itemStack.getItem() == Sitems.SYRINGE.get() && this.getVariant(stack) != SporeArmorMutations.DEFAULT;
      if (shouldOverride) {
         this.setVariant(SporeArmorMutations.DEFAULT, stack);
         itemStack.shrink(1);
         player.playNotifySound((SoundEvent)Ssounds.SYRINGE_SUCK.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
      }

      return shouldOverride;
   }
}
