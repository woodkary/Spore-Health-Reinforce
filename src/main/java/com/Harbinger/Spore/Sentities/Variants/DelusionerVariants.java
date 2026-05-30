package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum DelusionerVariants {
   DEFAULT(0, "default"),
   MAGE(1, "spore.entity.variant.mage");

   private static final DelusionerVariants[] BY_ID = (DelusionerVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(DelusionerVariants::getId)).toArray((x$0) -> new DelusionerVariants[x$0]);
   private final int id;
   private final String name;

   private DelusionerVariants(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public static DelusionerVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static DelusionerVariants[] $values() {
      return new DelusionerVariants[]{DEFAULT, MAGE};
   }
}
