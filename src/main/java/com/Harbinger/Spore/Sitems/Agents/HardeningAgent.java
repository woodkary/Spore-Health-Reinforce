package com.Harbinger.Spore.Sitems.Agents;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HardeningAgent extends MutationAgents {
   public HardeningAgent() {
      super(20);
   }

   public void mutateWeapon(ItemStack stack) {
      Item stackItem = stack.getItem();
      if (stackItem instanceof SporeWeaponData item) {
         int durability = 1 + (Integer)SConfig.SERVER.agent_durability.get();
         if (durability > 2) {
            item.setMaxAdditionalDurability(this.source.nextInt(durability / 2, durability), stack);
         }
      }

      stackItem = stack.getItem();
      if (stackItem instanceof SporeArmorData item) {
         int durability = 1 + (Integer)SConfig.SERVER.agent_durability.get();
         int j = 1 + (Integer)SConfig.SERVER.agent_toughness.get();
         if (durability > 2) {
            item.setMaxAdditionalDurability(this.source.nextInt(durability / 2, durability), stack);
         }

         if (j > 2) {
            item.setAdditionalToughness((double)this.source.nextInt(1, j), stack);
         }
      }

   }
}
