package com.Harbinger.Spore.Sitems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum SpawnEggType {
   INFECTED("spore.name.infected"),
   EVOLVED("spore.name.evolved"),
   HYPER("spore.name.hyper"),
   ORGANOID("spore.name.organoid"),
   CALAMITY("spore.name.calamity"),
   EXPERIMENT("spore.name.experiment"),
   UNKNOWN("spore.name.unknown");

   private final Component component;

   private SpawnEggType(String string) {
      this.component = Component.translatable(string).withStyle(ChatFormatting.GOLD);
   }

   public Component getName() {
      return this.component;
   }

   // $FF: synthetic method
   private static SpawnEggType[] $values() {
      return new SpawnEggType[]{INFECTED, EVOLVED, HYPER, ORGANOID, CALAMITY, EXPERIMENT, UNKNOWN};
   }
}
