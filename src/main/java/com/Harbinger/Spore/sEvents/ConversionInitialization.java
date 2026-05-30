package com.Harbinger.Spore.sEvents;

import com.Harbinger.Spore.ExtremelySusThings.CustomJsonReader.SporeCduConversionReloadListener;
import com.Harbinger.Spore.ExtremelySusThings.CustomJsonReader.SporeConversionReloadListener;
import com.Harbinger.Spore.ExtremelySusThings.CustomJsonReader.SporeMobConversionReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "spore",
   bus = Bus.FORGE
)
public class ConversionInitialization {
   @SubscribeEvent
   public static void onRegisterReloadListeners(AddReloadListenerEvent event) {
      event.addListener(new SporeConversionReloadListener());
      event.addListener(new SporeMobConversionReloadListener());
      event.addListener(new SporeCduConversionReloadListener());
   }
}
