package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum HazmatVariant {
   DEFAULT(0),
   TANK(1),
   COAT(2);

   private static final HazmatVariant[] BY_ID = (HazmatVariant[])Arrays.stream(values()).sorted(Comparator.comparingInt(HazmatVariant::getId)).toArray((x$0) -> new HazmatVariant[x$0]);
   private final int id;

   private HazmatVariant(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   public static HazmatVariant byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static HazmatVariant[] $values() {
      return new HazmatVariant[]{DEFAULT, TANK, COAT};
   }
}
