package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum UsurperVariants {
   DEFAULT(0, "default"),
   BURST(1, "spore.entity.variant.burst"),
   SPRAY(2, "spore.entity.variant.spray");

   private static final UsurperVariants[] BY_ID = (UsurperVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(UsurperVariants::getId)).toArray((x$0) -> new UsurperVariants[x$0]);
   private final int id;
   private final String name;

   private UsurperVariants(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public static UsurperVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static UsurperVariants[] $values() {
      return new UsurperVariants[]{DEFAULT, BURST, SPRAY};
   }
}
