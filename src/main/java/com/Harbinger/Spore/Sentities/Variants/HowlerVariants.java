package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum HowlerVariants {
   DEFAULT(0, "default"),
   BANSHEE(1, "spore.entity.variant.banshee"),
   SONIC(2, "spore.entity.variant.sonic"),
   FORLORN(3, "spore.entity.variant.forlorn"),
   SWARMER(4, "spore.entity.variant.swarmer");

   private static final HowlerVariants[] BY_ID = (HowlerVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(HowlerVariants::getId)).toArray((x$0) -> new HowlerVariants[x$0]);
   private final int id;
   private final String name;

   private HowlerVariants(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public static HowlerVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static HowlerVariants[] $values() {
      return new HowlerVariants[]{DEFAULT, BANSHEE, SONIC, FORLORN, SWARMER};
   }
}
