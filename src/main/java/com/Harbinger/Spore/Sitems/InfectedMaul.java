package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporePickaxeItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class InfectedMaul extends SporePickaxeItems {
   public InfectedMaul() {
      super((double)(Integer)SConfig.SERVER.maul_damage.get(), (double)3.0F, (double)3.0F, (Integer)SConfig.SERVER.maul_durability.get(), 3, "maul");
   }

   public boolean canMultiBreak(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity living) {
      return !living.isCrouching();
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity living, LivingEntity entity) {
      living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
      return super.hurtEnemy(stack, living, entity);
   }
}
