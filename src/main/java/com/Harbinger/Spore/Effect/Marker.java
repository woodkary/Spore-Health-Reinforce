package com.Harbinger.Spore.Effect;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeMobEffect;

public class Marker extends MobEffect implements IForgeMobEffect {
   public Marker() {
      super(MobEffectCategory.NEUTRAL, 8412043);
   }

   public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
      super.applyEffectTick(pLivingEntity, pAmplifier);
      MobEffectInstance instance = pLivingEntity.getEffect((MobEffect)Seffects.MARKER.get());
      if (instance != null) {
         int j = instance.getDuration();
         if (!(pLivingEntity instanceof Infected) && !(pLivingEntity instanceof UtilityEntity)) {
            AABB boundingBox = pLivingEntity.getBoundingBox().inflate((double)(16 * (pAmplifier + 1)));

            for(Entity entity : pLivingEntity.level().getEntities(pLivingEntity, boundingBox)) {
               if (entity instanceof Infected) {
                  Infected livingEntity = (Infected)entity;
                  livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MARKER.get(), j * 2, pAmplifier + 1, false, false));
               }
            }
         }

      }
   }

   public List getCurativeItems() {
      ArrayList<ItemStack> ret = new ArrayList();
      ret.add(ItemStack.EMPTY);
      return ret;
   }

   public boolean isDurationEffectTick(int duration, int intensity) {
      if (this == Seffects.MARKER.get()) {
         int i = 80 >> intensity;
         if (i > 0) {
            return duration % i == 0;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }
}
