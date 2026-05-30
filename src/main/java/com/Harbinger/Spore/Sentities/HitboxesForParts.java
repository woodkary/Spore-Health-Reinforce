package com.Harbinger.Spore.Sentities;

import com.Harbinger.Spore.Core.Sitems;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public enum HitboxesForParts {
   SIEGER_BODY(0, 3.5F, 4.5F, CALAMITY_TYPE.GROUND),
   SIEGER_JAW(1, 1.0F, 1.0F, CALAMITY_TYPE.GROUND),
   SIEGER_RIGHT_LEG(2, 1.5F, 2.0F, CALAMITY_TYPE.GROUND),
   SIEGER_LEFT_LEG(3, 1.5F, 2.0F, CALAMITY_TYPE.GROUND),
   SIEGER_BACK_RIGHT_LEG(4, 1.75F, 1.75F, CALAMITY_TYPE.GROUND),
   SIEGER_BACK_LEFT_LEG(5, 1.75F, 2.0F, CALAMITY_TYPE.GROUND),
   SIEGER_TAIL(6, 3.0F, 1.0F, CALAMITY_TYPE.GROUND),
   GAZEN_TAIL(7, 2.0F, 2.0F, CALAMITY_TYPE.WATER),
   GAZEN_HEAD(8, 2.0F, 2.0F, CALAMITY_TYPE.WATER),
   GAZEN_RIGHT_LEG(9, 1.75F, 1.0F, CALAMITY_TYPE.WATER),
   GAZEN_LEFT_LEG(10, 1.75F, 1.0F, CALAMITY_TYPE.WATER),
   LICKER(11, 1.0F, 1.0F, CALAMITY_TYPE.WATER),
   HINDEN_FRONT(12, 3.5F, 3.5F, CALAMITY_TYPE.AIR),
   HINDEN_BACK(13, 3.5F, 3.5F, CALAMITY_TYPE.AIR),
   MAW(14, 3.0F, 1.5F, CALAMITY_TYPE.AIR),
   RIGHT_CANNON(15, 1.5F, 1.0F, CALAMITY_TYPE.AIR),
   LEFT_CANNON(16, 1.5F, 1.0F, CALAMITY_TYPE.AIR),
   HOWI_CANNON1(17, 3.0F, 1.5F, CALAMITY_TYPE.GROUND),
   HOWI_CANNON2(18, 3.0F, 1.5F, CALAMITY_TYPE.GROUND),
   HOWI_CANNON3(19, 3.0F, 1.5F, CALAMITY_TYPE.GROUND),
   HOWI_LEFT_LEG(20, 1.5F, 1.5F, CALAMITY_TYPE.GROUND),
   HOWI_RIGHT_LEG(21, 2.0F, 1.0F, CALAMITY_TYPE.GROUND),
   HOWI_LEFT_ARM(22, 3.0F, 2.0F, CALAMITY_TYPE.GROUND),
   HOWI_RIGHT_ARM(23, 3.0F, 2.0F, CALAMITY_TYPE.GROUND),
   HOWI_SACK(24, 2.0F, 1.0F, CALAMITY_TYPE.GROUND),
   HOHL_JAW(25, 3.0F, 1.2F, CALAMITY_TYPE.GROUND),
   HOHL_HEAD(26, 3.0F, 2.5F, CALAMITY_TYPE.GROUND),
   HOHL_SEG1(27, 3.0F, 3.0F, CALAMITY_TYPE.GROUND),
   HOHL_SEG2(28, 3.0F, 3.0F, CALAMITY_TYPE.GROUND),
   HOHL_SEG3(29, 3.0F, 3.0F, CALAMITY_TYPE.GROUND),
   HOHL_TAIL(30, 3.0F, 3.0F, CALAMITY_TYPE.GROUND),
   GRAKEN_BODY(31, 3.0F, 3.0F, CALAMITY_TYPE.WATER),
   GRAKEN_BACK_MAW(32, 1.0F, 1.0F, CALAMITY_TYPE.WATER),
   GRAKEN_FRONT_MAW(33, 2.0F, 2.0F, CALAMITY_TYPE.WATER),
   GRAKEN_HINGE(34, 3.0F, 3.0F, CALAMITY_TYPE.WATER),
   STAHL_RIGHT_LEG(35, 2.0F, 1.0F, CALAMITY_TYPE.GROUND),
   STAHL_LEFT_LEG(36, 2.0F, 1.0F, CALAMITY_TYPE.GROUND),
   STAHL_BLADE_ARM(37, 5.0F, 3.0F, CALAMITY_TYPE.GROUND),
   STAHL_ARM_ARM(38, 4.0F, 1.5F, CALAMITY_TYPE.GROUND),
   STAHL_ARM_ARM2(39, 4.0F, 1.5F, CALAMITY_TYPE.GROUND),
   STAHL_MOUTH(40, 2.0F, 2.0F, CALAMITY_TYPE.GROUND),
   LEVI_BODY(41, 3.0F, 3.0F, CALAMITY_TYPE.WATER),
   LEVI_SEGMENT(42, 3.0F, 3.0F, CALAMITY_TYPE.WATER),
   LEVI_TAIL(43, 5.0F, 2.5F, CALAMITY_TYPE.WATER),
   LEVI_RIGHT_JAW(44, 2.5F, 1.0F, CALAMITY_TYPE.WATER),
   LEVI_LEFT_JAW(45, 2.5F, 1.0F, CALAMITY_TYPE.WATER),
   HOHL_ADA_JAW(46, 3.0F, 1.2F, CALAMITY_TYPE.GROUND),
   HOHL_ADA_HEAD(47, 3.0F, 2.5F, CALAMITY_TYPE.GROUND),
   HOHL_ADA_SEG1(48, 3.0F, 3.0F, CALAMITY_TYPE.GROUND),
   HOHL_ADA_SEG2(49, 3.0F, 3.0F, CALAMITY_TYPE.GROUND),
   HOHL_ADA_SEG3(50, 3.0F, 3.0F, CALAMITY_TYPE.GROUND),
   HOHL_ADA_TAIL(51, 3.0F, 3.0F, CALAMITY_TYPE.GROUND);

   private final int ID;
   private final float width;
   private final float height;
   private final CALAMITY_TYPE calamityType;
   private static final HitboxesForParts[] BY_ID = (HitboxesForParts[])Arrays.stream(values()).sorted(Comparator.comparingInt(HitboxesForParts::getID)).toArray((x$0) -> new HitboxesForParts[x$0]);

   private HitboxesForParts(int id, float width, float height, CALAMITY_TYPE calamityType) {
      this.ID = id;
      this.width = width;
      this.height = height;
      this.calamityType = calamityType;
   }

   public int getID() {
      return this.ID;
   }

   public float getHeight() {
      return this.height;
   }

   public CALAMITY_TYPE getCalamityType() {
      return this.calamityType;
   }

   public float getWidth() {
      return this.width;
   }

   public static HitboxesForParts byId(int id) {
      return BY_ID[id % BY_ID.length];
   }

   // $FF: synthetic method
   private static HitboxesForParts[] $values() {
      return new HitboxesForParts[]{SIEGER_BODY, SIEGER_JAW, SIEGER_RIGHT_LEG, SIEGER_LEFT_LEG, SIEGER_BACK_RIGHT_LEG, SIEGER_BACK_LEFT_LEG, SIEGER_TAIL, GAZEN_TAIL, GAZEN_HEAD, GAZEN_RIGHT_LEG, GAZEN_LEFT_LEG, LICKER, HINDEN_FRONT, HINDEN_BACK, MAW, RIGHT_CANNON, LEFT_CANNON, HOWI_CANNON1, HOWI_CANNON2, HOWI_CANNON3, HOWI_LEFT_LEG, HOWI_RIGHT_LEG, HOWI_LEFT_ARM, HOWI_RIGHT_ARM, HOWI_SACK, HOHL_JAW, HOHL_HEAD, HOHL_SEG1, HOHL_SEG2, HOHL_SEG3, HOHL_TAIL, GRAKEN_BODY, GRAKEN_BACK_MAW, GRAKEN_FRONT_MAW, GRAKEN_HINGE, STAHL_RIGHT_LEG, STAHL_LEFT_LEG, STAHL_BLADE_ARM, STAHL_ARM_ARM, STAHL_ARM_ARM2, STAHL_MOUTH, LEVI_BODY, LEVI_SEGMENT, LEVI_TAIL, LEVI_RIGHT_JAW, LEVI_LEFT_JAW, HOHL_ADA_JAW, HOHL_ADA_HEAD, HOHL_ADA_SEG1, HOHL_ADA_SEG2, HOHL_ADA_SEG3, HOHL_ADA_TAIL};
   }

   public static enum CALAMITY_TYPE {
      GROUND(new ItemStack((ItemLike)Sitems.REFORGED_BIOMASS_T.get())),
      WATER(new ItemStack((ItemLike)Sitems.REFORGED_BIOMASS_W.get())),
      AIR(new ItemStack((ItemLike)Sitems.REFORGED_BIOMASS_A.get()));

      private final ItemStack stack;

      private CALAMITY_TYPE(ItemStack stack) {
         this.stack = stack;
      }

      public ItemStack getStack() {
         return this.stack;
      }

      // $FF: synthetic method
      private static CALAMITY_TYPE[] $values() {
         return new CALAMITY_TYPE[]{GROUND, WATER, AIR};
      }
   }
}
