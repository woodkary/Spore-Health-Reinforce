package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum InfPlayerSkins {
   STEVE(0),
   ALEX(1),
   EFE(2),
   MAKENA(3),
   SUNNY(4),
   ZURI(5),
   ARI(6),
   KAI(7),
   NO0R(8);

   private static final InfPlayerSkins[] BY_ID = (InfPlayerSkins[])Arrays.stream(values()).sorted(Comparator.comparingInt(InfPlayerSkins::getId)).toArray((x$0) -> new InfPlayerSkins[x$0]);
   private final int id;

   private InfPlayerSkins(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   public static InfPlayerSkins byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static InfPlayerSkins[] $values() {
      return new InfPlayerSkins[]{STEVE, ALEX, EFE, MAKENA, SUNNY, ZURI, ARI, KAI, NO0R};
   }
}
