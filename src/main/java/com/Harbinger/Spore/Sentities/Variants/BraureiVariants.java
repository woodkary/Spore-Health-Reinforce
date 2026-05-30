package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum BraureiVariants {
   DEFAULT(0, "default"),
   HAZARD(1, "spore.entity.variant.hazard");

   private static final BraureiVariants[] BY_ID = (BraureiVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(BraureiVariants::getId)).toArray((x$0) -> new BraureiVariants[x$0]);
   private final int id;
   private final String name;

   private BraureiVariants(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public static BraureiVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static BraureiVariants[] $values() {
      return new BraureiVariants[]{DEFAULT, HAZARD};
   }
}
