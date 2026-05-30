package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Seffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SkullSoupItem extends BlockItemBase2 {
   public SkullSoupItem(Block block) {
      super(block, (new Properties()).food((new FoodProperties.Builder()).nutrition(4).saturationMod(2.0F).effect(() -> new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 0), 0.4F).effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0), 1.0F).meat().build()));
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

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.DRINK;
   }
}
