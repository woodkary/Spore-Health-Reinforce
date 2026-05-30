package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.Recipes.GraftingRecipe;
import com.Harbinger.Spore.Recipes.InjectionRecipe;
import com.Harbinger.Spore.Recipes.SurgeryRecipe;
import com.Harbinger.Spore.Recipes.WombRecipe;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Srecipes {
   public static final DeferredRegister SERIALIZERS;
   public static final RegistryObject SURGERY_SERIALIZER;
   public static final RegistryObject GRAFTING_SERIALIZER;
   public static final RegistryObject INJECTION_SERIALIZER;
   public static final RegistryObject WOMB_SERIALIZER;

   public static void register(IEventBus eventBus) {
      SERIALIZERS.register(eventBus);
   }

   static {
      SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "spore");
      SURGERY_SERIALIZER = SERIALIZERS.register("surgery", () -> SurgeryRecipe.SurgeryRecipeSerializer.INSTANCE);
      GRAFTING_SERIALIZER = SERIALIZERS.register("grafting", () -> GraftingRecipe.GraftingRecipeSerializer.INSTANCE);
      INJECTION_SERIALIZER = SERIALIZERS.register("injection", () -> InjectionRecipe.InjectionRecipeSerializer.INSTANCE);
      WOMB_SERIALIZER = SERIALIZERS.register("assimilation", () -> WombRecipe.WombRecipeSerializer.INSTANCE);
   }
}
