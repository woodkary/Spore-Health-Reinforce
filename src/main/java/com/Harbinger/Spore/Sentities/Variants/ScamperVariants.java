package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum ScamperVariants {
   DEFAULT(0),
   VILLAGER(1),
   DROWNED(2);

   private static final ScamperVariants[] BY_ID = (ScamperVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(ScamperVariants::getId)).toArray((x$0) -> new ScamperVariants[x$0]);
   private final int id;

   private ScamperVariants(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   public static ScamperVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static ScamperVariants[] $values() {
      return new ScamperVariants[]{DEFAULT, VILLAGER, DROWNED};
   }
}
