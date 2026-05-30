package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum SlasherVariants {
   DEFAULT(0, "default"),
   PIERCER(1, "spore.entity.variant.piercer"),
   SMASHER(2, "spore.entity.variant.smasher"),
   GRABBER(3, "spore.entity.variant.grabber"),
   SCREW(4, "spore.entity.variant.screw");

   private static final SlasherVariants[] BY_ID = (SlasherVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(SlasherVariants::getId)).toArray((x$0) -> new SlasherVariants[x$0]);
   private final int id;
   private final String name;

   private SlasherVariants(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.id;
   }

   public static SlasherVariants byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static SlasherVariants[] $values() {
      return new SlasherVariants[]{DEFAULT, PIERCER, SMASHER, GRABBER, SCREW};
   }
}
