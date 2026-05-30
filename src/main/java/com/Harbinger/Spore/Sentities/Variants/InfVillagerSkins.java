package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum InfVillagerSkins {
   DEFAULT(0),
   DESERT(1),
   JUNGLE(2),
   SAVANNA(3),
   SWAMP(4),
   TAIGA(5),
   TUNDRA(6);

   private static final InfVillagerSkins[] BY_ID = (InfVillagerSkins[])Arrays.stream(values()).sorted(Comparator.comparingInt(InfVillagerSkins::getId)).toArray((x$0) -> new InfVillagerSkins[x$0]);
   private final int id;

   private InfVillagerSkins(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   public static InfVillagerSkins byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static InfVillagerSkins[] $values() {
      return new InfVillagerSkins[]{DEFAULT, DESERT, JUNGLE, SAVANNA, SWAMP, TAIGA, TUNDRA};
   }
}
