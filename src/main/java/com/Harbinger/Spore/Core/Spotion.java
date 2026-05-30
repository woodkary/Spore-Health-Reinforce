package com.Harbinger.Spore.Core;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Spotion {
   public static final DeferredRegister POTIONS;
   public static RegistryObject MYCELIUM_POTION;
   public static RegistryObject MARKER_POTION;
   public static RegistryObject CORROSION_POTION;
   public static RegistryObject CORROSION_POTION_STRONG;

   public static void register(IEventBus eventBus) {
      POTIONS.register(eventBus);
   }

   static {
      POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, "spore");
      MYCELIUM_POTION = POTIONS.register("mycelium_potion", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 3600, 1)}));
      MARKER_POTION = POTIONS.register("marker_potion", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)Seffects.MARKER.get(), 3600, 1)}));
      CORROSION_POTION = POTIONS.register("corrosion_potion", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 3600, 0)}));
      CORROSION_POTION_STRONG = POTIONS.register("corrosion_potion_strong", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 3600, 3)}));
   }
}
