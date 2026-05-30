package com.Harbinger.Spore.Sitems.Agents;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ConnectingAgent extends MutationAgents {
   public ConnectingAgent() {
      super(30);
   }

   public void mutateWeapon(ItemStack stack) {
      Item stackItem = stack.getItem();
      if (stackItem instanceof SporeWeaponData item) {
         int enchantability = 1 + (Integer)SConfig.SERVER.agent_enchantability.get();
         if (enchantability > 2) {
            item.setLuck(this.source.nextInt(1, enchantability), stack);
         }
      }

      stackItem = stack.getItem();
      if (stackItem instanceof SporeArmorData item) {
         int enchantability = 1 + (Integer)SConfig.SERVER.agent_enchantability.get();
         if (enchantability > 2) {
            item.setLuck(this.source.nextInt(1, enchantability), stack);
         }
      }

   }
}
