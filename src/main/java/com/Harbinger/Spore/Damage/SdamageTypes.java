package com.Harbinger.Spore.Damage;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class SdamageTypes {
   public static final ResourceKey MYCELIUM_OVERTAKE = create("mycelium_overtake");
   public static final ResourceKey ACID = create("acid");
   public static final ResourceKey INFECTED_DAMAGE1 = create("infected_damage1");
   public static final ResourceKey INFECTED_DAMAGE2 = create("infected_damage2");
   public static final ResourceKey INFECTED_DAMAGE3 = create("infected_damage3");
   public static final ResourceKey CALAMITY_DAMAGE1 = create("calamity_damage1");
   public static final ResourceKey CALAMITY_DAMAGE2 = create("calamity_damage2");
   public static final ResourceKey CALAMITY_DAMAGE3 = create("calamity_damage3");
   public static final ResourceKey INFECTED_WITCH_DAMAGE = create("infected_witch_damage");
   public static final ResourceKey GRIEFER_DAMAGE = create("griefer_damage");
   public static final ResourceKey RADIATION_DAMAGE = create("radiation_damage");
   public static final ResourceKey KNIGHT_DAMAGE = create("knight_damage");
   public static final ResourceKey BRAIOMIL_DAMAGE = create("braiomil_damage");
   public static final ResourceKey LEAPER_DAMAGE = create("leaper_damage");
   public static final ResourceKey SLASHER_DAMAGE = create("slasher_damage");
   public static final ResourceKey SLASHER_PIERCING_DAMAGE = create("slasher_piercing_damage");
   public static final ResourceKey MENTAL_DAMAGE = create("mental_damage");

   public static ResourceKey create(String id) {
      return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("spore", id));
   }

   public static DamageSource damageSource(Entity entity, ResourceKey registryKey) {
      return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(registryKey));
   }

   public static DamageSource damageSource(Entity entity, ResourceKey registryKey, @Nullable Entity entity2) {
      return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(registryKey), entity2);
   }

   public static DamageSource mycelium_overtake(LivingEntity entity) {
      return damageSource(entity, MYCELIUM_OVERTAKE);
   }

   public static DamageSource acid(LivingEntity entity) {
      return damageSource(entity, ACID);
   }

   public static DamageSource infected_damage1(LivingEntity entity) {
      return damageSource(entity, INFECTED_DAMAGE1, entity);
   }

   public static DamageSource infected_damage2(LivingEntity entity) {
      return damageSource(entity, INFECTED_DAMAGE2, entity);
   }

   public static DamageSource infected_damage3(LivingEntity entity) {
      return damageSource(entity, INFECTED_DAMAGE3, entity);
   }

   public static DamageSource calamity_damage1(LivingEntity entity) {
      return damageSource(entity, CALAMITY_DAMAGE1, entity);
   }

   public static DamageSource calamity_damage2(LivingEntity entity) {
      return damageSource(entity, CALAMITY_DAMAGE2, entity);
   }

   public static DamageSource calamity_damage3(LivingEntity entity) {
      return damageSource(entity, CALAMITY_DAMAGE3, entity);
   }

   public static DamageSource infected_witch_damage(LivingEntity entity) {
      return damageSource(entity, INFECTED_WITCH_DAMAGE, entity);
   }

   public static DamageSource griefer_damage(LivingEntity entity) {
      return damageSource(entity, GRIEFER_DAMAGE, entity);
   }

   public static DamageSource radiation_damage(LivingEntity entity) {
      return damageSource(entity, RADIATION_DAMAGE, entity);
   }

   public static DamageSource knight_damage(LivingEntity entity) {
      return damageSource(entity, KNIGHT_DAMAGE, entity);
   }

   public static DamageSource braiomil_damage(LivingEntity entity) {
      return damageSource(entity, BRAIOMIL_DAMAGE, entity);
   }

   public static DamageSource leaper_damage(LivingEntity entity) {
      return damageSource(entity, LEAPER_DAMAGE, entity);
   }

   public static DamageSource slasher_damage(LivingEntity entity) {
      return damageSource(entity, SLASHER_DAMAGE, entity);
   }

   public static DamageSource slasher_piercing_damage(LivingEntity entity) {
      return damageSource(entity, SLASHER_PIERCING_DAMAGE, entity);
   }

   public static DamageSource mental_damage(LivingEntity entity) {
      return damageSource(entity, MENTAL_DAMAGE, entity);
   }
}
