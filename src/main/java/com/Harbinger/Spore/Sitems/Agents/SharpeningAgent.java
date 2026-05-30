package com.Harbinger.Spore.Sitems.Agents;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SharpeningAgent extends MutationAgents {
   public SharpeningAgent() {
      super(25);
   }

   public void mutateWeapon(ItemStack stack) {
      Item stackItem = stack.getItem();
      if (stackItem instanceof SporeWeaponData item) {
         int damage = 1 + (Integer)SConfig.SERVER.agent_damage.get();
         if (damage > 2) {
            item.setAdditionalDamage((double)this.source.nextInt(damage / 2, damage), stack);
         }
      }

      stackItem = stack.getItem();
      if (stackItem instanceof SporeArmorData item) {
         int protection = 1 + (Integer)SConfig.SERVER.agent_protection.get();
         if (protection > 2) {
            item.setAdditionalProtection((double)this.source.nextInt(protection / 2, protection), stack);
         }
      }

   }
}
