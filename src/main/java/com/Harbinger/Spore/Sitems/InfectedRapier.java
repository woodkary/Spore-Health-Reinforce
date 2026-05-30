package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Sitems.BaseWeapons.DamagePiercingModifier;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeSwordBase;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class InfectedRapier extends SporeSwordBase implements DamagePiercingModifier {
   public InfectedRapier() {
      super((double)(Integer)SConfig.SERVER.rapier_damage.get(), (double)2.0F, (double)2.0F, (Integer)SConfig.SERVER.rapier_durability.get(), "rapier");
   }

   public void doEntityHurtAfterEffects(ItemStack stack, LivingEntity victim, LivingEntity entity) {
      super.doEntityHurtAfterEffects(stack, victim, entity);
      int level = stack.getEnchantmentLevel((Enchantment)Senchantments.CORROSIVE_POTENCY.get()) > 0 ? 3 : 1;
      victim.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 60, level, true, true));
   }

   protected ChatFormatting getDesColor() {
      return ChatFormatting.GREEN;
   }

   public float getMinimalDamage(float damage) {
      return (float)(Integer)SConfig.SERVER.rapier_damage.get() * 0.3F;
   }
}
