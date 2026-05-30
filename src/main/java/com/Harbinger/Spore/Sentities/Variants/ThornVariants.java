package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum ThornVariants {
   DEFAULT(0, "default"),
   TOXIC(1, "spore.entity.variant.laced");

   private static final ThornVariants[] BY_ID = (ThornVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(ThornVariants::getId)).toArray((x$0) -> new ThornVariants[x$0]);
   private final int id;
   private final String name;

   private ThornVariants(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.id;
   }

   public static ThornVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static ThornVariants[] $values() {
      return new ThornVariants[]{DEFAULT, TOXIC};
   }
}
