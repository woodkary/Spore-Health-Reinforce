package com.Harbinger.Spore.Sitems.BaseWeapons;

import java.util.Arrays;
import java.util.Comparator;

public enum SporeArmorMutations {
   DEFAULT(0, -1, "default"),
   REINFORCED(1, -10092442, "reinforced"),
   SKELETAL(2, -154, "skeletal"),
   DROWNED(3, -16711732, "drowned"),
   CHARRED(4, -13421773, "charred");

   private final int id;
   private final int color;
   private final String name;
   private static final SporeArmorMutations[] BY_ID = (SporeArmorMutations[])Arrays.stream(values()).sorted(Comparator.comparingInt(SporeArmorMutations::getId)).toArray((x$0) -> new SporeArmorMutations[x$0]);

   private SporeArmorMutations(int id, int color, String name) {
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

   public static SporeArmorMutations byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static SporeArmorMutations[] $values() {
      return new SporeArmorMutations[]{DEFAULT, REINFORCED, SKELETAL, DROWNED, CHARRED};
   }
}
