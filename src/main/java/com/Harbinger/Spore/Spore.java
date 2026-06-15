package com.Harbinger.Spore;

import com.Harbinger.Spore.Core.*;
import com.Harbinger.Spore.Core.agents.transformers.SporeLivingEntityHealthTransformerBootstrap;
import com.Harbinger.Spore.Core.entityStorages.SporeClientEntityCallback;
import com.Harbinger.Spore.Core.entityStorages.SporeServerEntityCallback;
import com.Harbinger.Spore.ExtremelySusThings.BiomeModification;
import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import com.Harbinger.Spore.ExtremelySusThings.StructureModification;
import com.Harbinger.Spore.network.HealthDeltaPacketHandler;
import com.Harbinger.Spore.network.HealthPacketHandler;
import com.Harbinger.Spore.sEvents.HandlerEvents;
import com.Harbinger.Spore.sEvents.SporeEventBus;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("spore")
public class Spore {
   public static Spore instance;
   public static final String MODID = "spore";
   public static final Logger LOGGER = LogManager.getLogger("spore");

   public Spore() {
      instance = this;
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      ModLoadingContext.get().registerConfig(Type.COMMON, SConfig.DATAGEN_SPEC, "sporedata.toml");
      ModLoadingContext.get().registerConfig(Type.COMMON, SConfig.SERVER_SPEC, "sporeconfig.toml");
      SConfig.loadConfig(SConfig.SERVER_SPEC, FMLPaths.CONFIGDIR.get().resolve("sporeconfig.toml").toString());
      MinecraftForge.EVENT_BUS.register(this);
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(HandlerEvents::SpawnPlacement);
      Sblocks.register(modEventBus);
      Sitems.register(modEventBus);
      Sentities.register(modEventBus);
      Senchantments.register(modEventBus);
      Seffects.register(modEventBus);
      Spotion.register(modEventBus);
      Sparticles.register(modEventBus);
      Ssounds.register(modEventBus);
      Srecipes.register(modEventBus);
      Sfluids.SPORE_FLUID.register(modEventBus);
      Sfluids.SPORE_FLUID_TYPE.register(modEventBus);
      Sfeatures.register(modEventBus);
      ScreativeTab.register(modEventBus);
      SMenu.register(modEventBus);
      Spaintings.register(modEventBus);
      SblockEntities.register(modEventBus);
      SAttributes.register(modEventBus);
      SticketType.init();
      DeferredRegister<Codec<? extends BiomeModifier>> biomeModifiers = DeferredRegister.create(Keys.BIOME_MODIFIER_SERIALIZERS, "spore");
      biomeModifiers.register(modEventBus);
      biomeModifiers.register("inf_spawns", BiomeModification::makeCodec);
      DeferredRegister<Codec<? extends StructureModifier>> structureModifiers = DeferredRegister.create(Keys.STRUCTURE_MODIFIER_SERIALIZERS, "spore");
      structureModifiers.register(modEventBus);
      structureModifiers.register("spore_structure_spawns", StructureModification::makeCodec);
      SporeEventBus.tick().addSelfListener();
   }

   public void commonSetup(FMLCommonSetupEvent event) {
      SporePacketHandler.registerPackets();
      SporeLivingEntityHealthTransformerBootstrap.INSTANCE.installAndRetransform();
      event.enqueueWork(Sfluids::postInit);
   }
}
