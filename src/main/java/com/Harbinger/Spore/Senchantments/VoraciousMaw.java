package com.Harbinger.Spore.Senchantments;

import com.Harbinger.Spore.Core.Senchantments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

public class VoraciousMaw extends BaseSporeEnchantment {
   public VoraciousMaw(EquipmentSlot... slots) {
      super(Rarity.COMMON, Senchantments.FUNGAL_ITEMS, slots);
   }

   public void doPostAttack(LivingEntity livingEntity, Entity entity, int value) {
      super.doPostAttack(livingEntity, entity, value);
      if (livingEntity.getItemBySlot(EquipmentSlot.HEAD).getEnchantmentLevel(this) > 0 && entity instanceof Mob && Math.random() < (double)0.1F && livingEntity instanceof Player player) {
         player.playNotifySound(SoundEvents.GENERIC_EAT, SoundSource.AMBIENT, 1.0F, 1.0F);
         player.getFoodData().eat(6, 0.0F);
      }

   }
}
