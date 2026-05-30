package com.Harbinger.Spore.Core;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Sfeatures {
   public static final DeferredRegister SPORE_FEATURE;

   public static void register(IEventBus eventBus) {
      SPORE_FEATURE.register(eventBus);
   }

   static {
      SPORE_FEATURE = DeferredRegister.create(ForgeRegistries.FEATURES, "spore");
   }
}
