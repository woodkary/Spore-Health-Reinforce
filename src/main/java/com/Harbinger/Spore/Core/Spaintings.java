package com.Harbinger.Spore.Core;

import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Spaintings {
   public static final DeferredRegister PAINTING_MOVTIES;
   public static final RegistryObject CHANGE;

   public static void register(IEventBus eventBus) {
      PAINTING_MOVTIES.register(eventBus);
   }

   static {
      PAINTING_MOVTIES = DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, "spore");
      CHANGE = PAINTING_MOVTIES.register("change", () -> new PaintingVariant(16, 16));
   }
}
