package com.Harbinger.Spore.Sitems.BaseWeapons;

import java.util.Arrays;
import java.util.Comparator;

public enum SporeToolsMutations {
   DEFAULT(0, -1, "default"),
   VAMPIRIC(1, -52429, "vampiric"),
   CALCIFIED(2, -103, "calcified"),
   BEZERK(3, -26368, "bezerk"),
   TOXIC(4, -16751104, "toxic"),
   ROTTEN(5, -6710887, "rotten");

   private final int id;
   private final int color;
   private final String name;
   private static final SporeToolsMutations[] BY_ID = (SporeToolsMutations[])Arrays.stream(values()).sorted(Comparator.comparingInt(SporeToolsMutations::getId)).toArray((x$0) -> new SporeToolsMutations[x$0]);

   private SporeToolsMutations(int id, int color, String name) {
      this.id = id;
      this.color = color;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public int getColor() {
      return this.color;
   }

   public String getName() {
      return "spore.item.mutation." + this.name;
   }

   public static SporeToolsMutations byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static SporeToolsMutations[] $values() {
      return new SporeToolsMutations[]{DEFAULT, VAMPIRIC, CALCIFIED, BEZERK, TOXIC, ROTTEN};
   }
}
