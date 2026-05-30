package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeAxeItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class InfectedArmads extends SporeAxeItems {
   public InfectedArmads() {
      super((double)(Integer)SConfig.SERVER.armads_damage.get(), (double)3.5F, 3.2, (Integer)SConfig.SERVER.armads_durability.get(), 3, "armads");
   }

   public boolean canMultiBreak(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity living) {
      return !living.isCrouching();
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int val, boolean value) {
      super.inventoryTick(stack, level, entity, val, value);
      if (entity instanceof LivingEntity livingEntity) {
         if (livingEntity.tickCount % 60 == 0 && livingEntity.getHealth() <= livingEntity.getMaxHealth() / 2.0F && livingEntity.getMainHandItem().getItem().equals(this)) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
         }
      }

   }
}
