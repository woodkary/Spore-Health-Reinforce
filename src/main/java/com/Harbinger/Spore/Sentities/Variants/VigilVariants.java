package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum VigilVariants {
   DEFAULT(0, "default"),
   STALKER(1, "spore.entity.variant.stalker"),
   TROLL(2, "spore.entity.variant.troll"),
   RINGER(3, "spore.entity.variant.ringer");

   private static final VigilVariants[] BY_ID = (VigilVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(VigilVariants::getId)).toArray((x$0) -> new VigilVariants[x$0]);
   private final int id;
   private final String name;

   private VigilVariants(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public static VigilVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static VigilVariants[] $values() {
      return new VigilVariants[]{DEFAULT, STALKER, TROLL, RINGER};
   }
}
