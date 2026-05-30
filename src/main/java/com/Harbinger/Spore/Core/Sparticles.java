package com.Harbinger.Spore.Core;

import java.util.function.Supplier;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Sparticles {
   public static final DeferredRegister PARTICLE_TYPES;
   public static final RegistryObject SPORE_PARTICLE;
   public static final RegistryObject ACID_PARTICLE;
   public static final RegistryObject BLOOD_PARTICLE;
   public static final RegistryObject SPORE_SLASH;
   public static final RegistryObject SPORE_IMPACT;
   public static final RegistryObject VOMIT;
   public static final RegistryObject VOMIT_BONE;
   public static final RegistryObject VOMIT_ORES;
   public static final Supplier ACID_BULLET;
   public static final Supplier GORE_BULLET;
   public static final Supplier BILE_BULLET;

   public static void register(IEventBus eventBus) {
      PARTICLE_TYPES.register(eventBus);
   }

   static {
      PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "spore");
      SPORE_PARTICLE = PARTICLE_TYPES.register("spore_particle", () -> new SimpleParticleType(true));
      ACID_PARTICLE = PARTICLE_TYPES.register("acid_particle", () -> new SimpleParticleType(true));
      BLOOD_PARTICLE = PARTICLE_TYPES.register("blood_particle", () -> new SimpleParticleType(true));
      SPORE_SLASH = PARTICLE_TYPES.register("spore_slash", () -> new SimpleParticleType(true));
      SPORE_IMPACT = PARTICLE_TYPES.register("spore_impact", () -> new SimpleParticleType(true));
      VOMIT = PARTICLE_TYPES.register("vomit", () -> new SimpleParticleType(true));
      VOMIT_BONE = PARTICLE_TYPES.register("vomit_bone", () -> new SimpleParticleType(true));
      VOMIT_ORES = PARTICLE_TYPES.register("vomit_ores", () -> new SimpleParticleType(true));
      ACID_BULLET = PARTICLE_TYPES.register("acid_bullet", () -> new SimpleParticleType(true));
      GORE_BULLET = PARTICLE_TYPES.register("gore_bullet", () -> new SimpleParticleType(true));
      BILE_BULLET = PARTICLE_TYPES.register("bile_bullet", () -> new SimpleParticleType(true));
   }
}
