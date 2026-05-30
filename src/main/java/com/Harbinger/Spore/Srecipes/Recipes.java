package com.Harbinger.Spore.Srecipes;

import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Spotion;
import com.Harbinger.Spore.ExtremelySusThings.QualityBrewingRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(
   bus = Bus.MOD
)
public class Recipes {
   @SubscribeEvent
   public static void create(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> {
         BrewingRecipeRegistry.addRecipe(new QualityBrewingRecipe(Potions.WATER, (Item)Sitems.MUTATED_FIBER.get(), (Potion)Spotion.MYCELIUM_POTION.get()));
         BrewingRecipeRegistry.addRecipe(new QualityBrewingRecipe(Potions.WATER, (Item)Sitems.ALVEOLIC_SACK.get(), (Potion)Spotion.MARKER_POTION.get()));
         BrewingRecipeRegistry.addRecipe(new QualityBrewingRecipe(Potions.POISON, (Item)Sitems.CORROSIVE_SACK.get(), (Potion)Spotion.CORROSION_POTION.get()));
         BrewingRecipeRegistry.addRecipe(new QualityBrewingRecipe((Potion)Spotion.CORROSION_POTION.get(), (Item)Sitems.INNARDS.get(), (Potion)Spotion.CORROSION_POTION_STRONG.get()));
      });
   }
}
