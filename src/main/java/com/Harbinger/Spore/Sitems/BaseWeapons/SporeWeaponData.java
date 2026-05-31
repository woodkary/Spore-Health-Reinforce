package com.Harbinger.Spore.Sitems.BaseWeapons;

import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

public interface SporeWeaponData {
   String BASE_TAG = "agent";
   String MELEE_TAG = "mutant_damage";
   String MELEE_DURABILITY = "mutant_durability";
   String MAX_DURABILITY = "mutant_max_durability";
   String ENCHANTING = "mutant_enchanting";
   String MUTATION = "mutation";

   default boolean tooHurt(ItemStack stack) {
      return stack.getDamageValue() < stack.getMaxDamage() - 10;
   }
   default boolean doASMRangeHurtOnSwing(ItemStack stack, LivingEntity attacker){
      if(!(attacker instanceof Player player)||this.getVariant(stack) != SporeToolsMutations.BEZERK){
         return false;
      }
      Entity target=SporeAttackUtil.INSTANCE.getTargetedEntity(player,player.getEntityReach());
      if(target==null||target instanceof AbstractVillager){
         return false;
      }
      SporeAttackUtil.INSTANCE.attack(player,target,stack);
      return false;
   }

   default double calculateTrueDamage(ItemStack stack, double meleeDamage) {
      double value = this.getAdditionalDamage(stack) * 0.01;
      return value > (double)0.0F ? meleeDamage + meleeDamage * value : meleeDamage;
   }

   default void setAdditionalDamage(double value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putDouble("mutant_damage", value);
   }

   default double getAdditionalDamage(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTagElement("agent");
      return tag.getDouble("mutant_damage");
   }

   default int getMaxAdditionalDurability(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      return tag.getInt("mutant_max_durability");
   }

   default void setMaxAdditionalDurability(int value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putInt("mutant_max_durability", value);
   }

   default int getAdditionalDurability(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      return tag.getInt("mutant_durability");
   }

   default void setAdditionalDurability(int value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putInt("mutant_durability", value);
   }

   default void hurtTool(ItemStack stack, LivingEntity entity, int value) {
      int lostDurability = this.calculateDurabilityLostForMutations(value, stack);
      if (this.getAdditionalDurability(stack) > 0) {
         this.hurtExtraDurability(stack, lostDurability, entity);
      } else {
         stack.hurtAndBreak(lostDurability, entity, (p_43296_) -> p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      }

   }

   default int calculateDurabilityLostForMutations(int value, ItemStack stack) {
      if (this.getVariant(stack) == SporeToolsMutations.TOXIC) {
         return value * 2;
      } else {
         return this.getVariant(stack) == SporeToolsMutations.ROTTEN ? value * 2 : value;
      }
   }

   default void hurtExtraDurability(ItemStack stack, int value, @Nullable LivingEntity living) {
      this.setAdditionalDurability(this.getAdditionalDurability(stack) - value, stack);
   }

   default void setLuck(int value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putDouble("mutant_enchanting", (double)value);
   }

   default int getLuck(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTagElement("agent");
      return tag.getInt("mutant_enchanting");
   }

   default SporeToolsMutations getVariant(ItemStack stack) {
      return SporeToolsMutations.byId(this.getTypeVariant(stack) & 255);
   }

   default int getTypeVariant(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      return tag.getInt("mutation");
   }

   default void setVariant(SporeToolsMutations variant, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putInt("mutation", variant.getId() & 255);
   }

   default boolean doesExtraKnockBack() {
      return false;
   }

   default boolean reversedKnockback() {
      return false;
   }

   default void doEntityHurtAfterEffects(ItemStack stack, LivingEntity victim, LivingEntity entity) {
      if (this.reversedKnockback()) {
         victim.knockback((double)1.2F, (double)(-Mth.sin(entity.getYRot() * ((float)Math.PI / 180F))), (double)Mth.cos(entity.getYRot() * ((float)Math.PI / 180F)));
      }

      if (this.doesExtraKnockBack()) {
         victim.knockback((double)2.2F, (double)Mth.sin(entity.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(entity.getYRot() * ((float)Math.PI / 180F))));
      }

      if (this.getVariant(stack) == SporeToolsMutations.TOXIC) {
         victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
      }

      if (this.getVariant(stack) == SporeToolsMutations.ROTTEN) {
         victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
      }

      if (this.getVariant(stack) == SporeToolsMutations.CALCIFIED) {
         victim.hurtMarked = true;
         double knockback = this.reversedKnockback() ? (double)(-Mth.sin(entity.getYRot() * ((float)Math.PI / 180F))) : (double)Mth.sin(entity.getYRot() * ((float)Math.PI / 180F));
         double knockback2 = this.reversedKnockback() ? (double)Mth.cos(entity.getYRot() * ((float)Math.PI / 180F) * ((float)Math.PI / 180F)) : (double)(-Mth.cos(entity.getYRot() * ((float)Math.PI / 180F)));
         victim.knockback((double)1.5F, knockback, knockback2);
      }

      if (this.getVariant(stack) == SporeToolsMutations.VAMPIRIC && entity.getHealth() < entity.getMaxHealth()) {
         entity.heal(2.0F);
      }

      if (this.getVariant(stack) == SporeToolsMutations.BEZERK && Math.random() < 0.3) {
         if (Math.random() < (double)0.5F) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
         } else if (Math.random() < (double)0.5F) {
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0));
         } else {
            entity.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0));
         }
      }

   }

   default double modifyDamage(ItemStack stack, double value) {
      float sharpness = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack) > 0 ? (float)EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack) * 0.5F + 1.0F : 0.0F;
      return (double)sharpness + (this.getVariant(stack) == SporeToolsMutations.VAMPIRIC ? this.calculateTrueDamage(stack, value) * -0.2 : (double)0.0F);
   }

   default double modifyRange(ItemStack stack) {
      return (double)0.0F;
   }

   default double modifyRecharge(ItemStack stack) {
      return this.getVariant(stack) == SporeToolsMutations.CALCIFIED ? (double)-0.5F : (double)0.0F;
   }

   default int getMaxTrueAdditionalDurability(ItemStack stack) {
      return (int)((double)stack.getMaxDamage() * (double)this.getMaxAdditionalDurability(stack) * 0.01);
   }

   default void healTool(ItemStack stack, int value) {
      if (stack.getDamageValue() < stack.getMaxDamage()) {
         stack.setDamageValue(stack.getDamageValue() - value);
      }

      if (this.getMaxTrueAdditionalDurability(stack) > this.getAdditionalDurability(stack)) {
         this.setAdditionalDurability(this.getAdditionalDurability(stack) + value, stack);
      }

   }

   default void abstractMutationBuffs(LivingEntity victim, LivingEntity owner, ItemStack stack, SporeWeaponData data) {
      if (data.getVariant(stack) == SporeToolsMutations.TOXIC) {
         victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
      }

      if (data.getVariant(stack) == SporeToolsMutations.ROTTEN) {
         victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
      }

      if (data.getVariant(stack) == SporeToolsMutations.VAMPIRIC && owner.getHealth() < owner.getMaxHealth()) {
         owner.heal(2.0F);
      }

      if (data.getVariant(stack) == SporeToolsMutations.BEZERK && Math.random() < 0.3) {
         if (Math.random() < (double)0.5F) {
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
         } else if (Math.random() < (double)0.5F) {
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0));
         } else {
            owner.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0));
         }
      }

   }
}
