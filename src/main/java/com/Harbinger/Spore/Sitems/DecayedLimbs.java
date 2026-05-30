package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Seffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DecayedLimbs extends BaseItem {
   public DecayedLimbs(Properties properties) {
      super(properties);
   }

   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
      if (!world.isClientSide) {
         if (entity.hasEffect((MobEffect)Seffects.MADNESS.get())) {
            entity.addEffect(new MobEffectInstance(MobEffects.SATURATION, 300, 1));
         } else {
            entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
         }
      }

      return super.finishUsingItem(stack, world, entity);
   }
}
