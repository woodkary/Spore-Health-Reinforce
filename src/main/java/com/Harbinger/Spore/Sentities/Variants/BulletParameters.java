package com.Harbinger.Spore.Sentities.Variants;

import java.util.Arrays;
import java.util.Comparator;

public enum BulletParameters {
   INFECTIOUS(0, -10092544, 8.0F),
   EXPLOSIVE(1, -13434880, 6.0F),
   CORROSIVE(2, -13382656, 8.0F),
   FLAMMABLE(3, -26368, 12.0F);

   private static final BulletParameters[] BY_ID = (BulletParameters[])Arrays.stream(values()).sorted(Comparator.comparingInt(BulletParameters::getId)).toArray((x$0) -> new BulletParameters[x$0]);
   private final int id;
   private final int particle;
   private final float damage;

   private BulletParameters(int id, int particle, float damage) {
      this.id = id;
      this.particle = particle;
      this.damage = damage;
   }

   public int getParticle() {
      return this.particle;
   }

   public float getDamage() {
      return this.damage;
   }

   public int getId() {
      return this.id;
   }

   public static BulletParameters byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static BulletParameters[] $values() {
      return new BulletParameters[]{INFECTIOUS, EXPLOSIVE, CORROSIVE, FLAMMABLE};
   }
}
