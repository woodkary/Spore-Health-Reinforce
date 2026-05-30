package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeAxeItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InfectedHalbert extends SporeAxeItems {
   public InfectedHalbert() {
      super((double)(Integer)SConfig.SERVER.halberd_damage.get(), (double)4.5F, 3.2, (Integer)SConfig.SERVER.halberd_durability.get(), 3, "halbert");
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int val, boolean value) {
      super.inventoryTick(stack, level, entity, val, value);
      if (entity instanceof LivingEntity livingEntity) {
         if (livingEntity.tickCount % 60 == 0 && livingEntity.getVehicle() != null && livingEntity.getMainHandItem().getItem().equals(this)) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, false, false));
         }
      }

   }
}
