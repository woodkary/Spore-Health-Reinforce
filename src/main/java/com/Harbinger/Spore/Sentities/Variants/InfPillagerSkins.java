package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum InfPillagerSkins {
   DEFAULT(0),
   CAPTAIN(1);

   private static final InfPillagerSkins[] BY_ID = (InfPillagerSkins[])Arrays.stream(values()).sorted(Comparator.comparingInt(InfPillagerSkins::getId)).toArray((x$0) -> new InfPillagerSkins[x$0]);
   private final int id;

   private InfPillagerSkins(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   public static InfPillagerSkins byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static InfPillagerSkins[] $values() {
      return new InfPillagerSkins[]{DEFAULT, CAPTAIN};
   }
}
