package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum GrieferVariants {
   DEFAULT(0, "default"),
   TOXIC(1, "spore.entity.variant.toxic"),
   RADIOACTIVE(2, "spore.entity.variant.radioactive"),
   BILE(3, "spore.entity.variant.bile"),
   NAPALM(4, "spore.entity.variant.napalm");

   private static final GrieferVariants[] BY_ID = (GrieferVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(GrieferVariants::getId)).toArray((x$0) -> new GrieferVariants[x$0]);
   private final int id;
   private final String name;

   private GrieferVariants(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.id;
   }

   public static GrieferVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static GrieferVariants[] $values() {
      return new GrieferVariants[]{DEFAULT, TOXIC, RADIOACTIVE, BILE, NAPALM};
   }
}
