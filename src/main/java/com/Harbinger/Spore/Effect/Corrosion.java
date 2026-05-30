package com.Harbinger.Spore.Effect;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Damage.SdamageTypes;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class Corrosion extends MobEffect {
   public Corrosion() {
      super(MobEffectCategory.HARMFUL, -13369549);
   }

   public void applyEffectTick(LivingEntity entity, int p_19468_) {
      if (((List)SConfig.SERVER.corrosion.get()).contains(entity.getEncodeId()) && this == Seffects.CORROSION.get()) {
         entity.hurt(SdamageTypes.acid(entity), 1.0F);
      }

   }

   public boolean isDurationEffectTick(int duration, int intensity) {
      if (this == Seffects.CORROSION.get()) {
         int i = 60 >> intensity;
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
