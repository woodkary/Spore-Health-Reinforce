package com.Harbinger.Spore.Core;

import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ScreativeTab {
   public static final DeferredRegister<CreativeModeTab> SPORE_TABS;
   public static final RegistryObject<CreativeModeTab> SPORE;
   public static final RegistryObject<CreativeModeTab> SPORE_T;

   public static void register(IEventBus eventBus) {
      SPORE_TABS.register(eventBus);
   }

   static {
      SPORE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "spore");
      SPORE = SPORE_TABS.register("spore", () -> {
         CreativeModeTab.Builder var10000 = CreativeModeTab.builder().title(Component.translatable("itemGroup.spore"));
         Item var10001 = (Item)Sitems.MUTATED_HEART.get();
         Objects.requireNonNull(var10001);
         return var10000.icon(var10001::getDefaultInstance).displayItems((parameters, output) -> Sitems.BIOLOGICAL_ITEMS.forEach((item) -> output.accept(item.asItem()))).build();
      });
      SPORE_T = SPORE_TABS.register("spore_t", () -> {
         CreativeModeTab.Builder var10000 = CreativeModeTab.builder().title(Component.translatable("itemGroup.spore_t"));
         Item var10001 = (Item)Sitems.CONTAINER.get();
         Objects.requireNonNull(var10001);
         return var10000.icon(var10001::getDefaultInstance).displayItems((parameters, output) -> Sitems.TECHNOLOGICAL_ITEMS.forEach((item) -> output.accept(item.asItem()))).build();
      });
   }
}
