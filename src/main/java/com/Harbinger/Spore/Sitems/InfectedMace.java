package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.DamagePiercingModifier;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsBaseItem;

public class InfectedMace extends SporeToolsBaseItem implements DamagePiercingModifier {
   public InfectedMace() {
      super((double)(Integer)SConfig.SERVER.mace_damage.get(), (double)2.0F, (double)3.0F, (Integer)SConfig.SERVER.mace_durability.get(), 1, "mace");
   }

   public float getMinimalDamage(float damage) {
      return (float)(Integer)SConfig.SERVER.mace_damage.get() * 0.15F;
   }

   public boolean doesExtraKnockBack() {
      return true;
   }
}
