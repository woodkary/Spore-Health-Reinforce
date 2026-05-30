package com.Harbinger.Spore.Senchantments;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

public class MutagenicReactant extends BaseSporeEnchantment {
   public MutagenicReactant(EquipmentSlot... slots) {
      super(Rarity.COMMON, Senchantments.FUNGAL_ITEMS, slots);
   }

   public boolean isCurse() {
      return true;
   }

   public void doPostAttack(LivingEntity livingEntity, Entity entity, int value) {
      if (Math.random() < 0.1 && entity instanceof Infected infected) {
         if (infected instanceof EvolvingInfected evolvedInfected) {
            if (evolvedInfected instanceof EvolvedInfected evolved) {
               evolved.setEvolution((Integer)SConfig.SERVER.evolution_age_human.get());
               evolved.setEvoPoints(evolved.getEvoPoints() + (Integer)SConfig.SERVER.min_kills_hyper.get());
            } else {
               infected.setEvolution((Integer)SConfig.SERVER.evolution_age_human.get());
               infected.setEvoPoints(infected.getEvoPoints() + (Integer)SConfig.SERVER.min_kills.get());
            }

            if (livingEntity instanceof Player player) {
               player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() / 2);
            }
         }
      }

      super.doPostAttack(livingEntity, entity, value);
   }

   public void doPostHurt(LivingEntity livingEntity, Entity entity, int value) {
      if (Math.random() < 0.2) {
         livingEntity.getArmorSlots().forEach((stack) -> {
            if (stack.getEnchantmentLevel(this) > 0) {
               MobEffectInstance effect = (MobEffectInstance)badMutations().get(livingEntity.getRandom().nextInt(badMutations().size()));
               livingEntity.addEffect(effect);
            }

         });
      }

      super.doPostAttack(livingEntity, entity, value);
   }

   public static List badMutations() {
      List<MobEffectInstance> values = new ArrayList();
      values.add(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
      values.add(new MobEffectInstance(MobEffects.POISON, 80, 0));
      values.add(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 0));
      values.add(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
      return values;
   }
}
