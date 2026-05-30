package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.SBlockEntities.BiomassLumpEntity;
import com.Harbinger.Spore.SBlockEntities.BrainRemnantBlockEntity;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.Harbinger.Spore.SBlockEntities.CabinetBlockEntity;
import com.Harbinger.Spore.SBlockEntities.ContainerBlockEntity;
import com.Harbinger.Spore.SBlockEntities.HiveSpawnBlockEntity;
import com.Harbinger.Spore.SBlockEntities.IncubatorBlockEntity;
import com.Harbinger.Spore.SBlockEntities.OutpostWatcherBlockEntity;
import com.Harbinger.Spore.SBlockEntities.OvergrownSpawnerEntity;
import com.Harbinger.Spore.SBlockEntities.SurgeryTableBlockEntity;
import com.Harbinger.Spore.SBlockEntities.ZoaholicBlockEntity;
import com.mojang.datafixers.types.Type;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SblockEntities {
   public static final DeferredRegister BLOCK_ENTITIES;
   public static final RegistryObject CONTAINER;
   public static final RegistryObject BIOMASS_LUMP;
   public static final RegistryObject HIVE_SPAWN;
   public static final RegistryObject CDU;
   public static final RegistryObject ZOAHOLIC;
   public static final RegistryObject INCUBATOR;
   public static final RegistryObject OVERGROWN_SPAWNER;
   public static final RegistryObject BRAIN_REMNANTS;
   public static final RegistryObject OUTPOST_WATCHER;
   public static final RegistryObject SURGERY_TABLE_ENTITY;
   public static final RegistryObject CABINET_ENTITY;

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }

   static {
      BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "spore");
      CONTAINER = BLOCK_ENTITIES.register("bcu_container", () -> Builder.of(ContainerBlockEntity::new, new Block[]{(Block)Sblocks.CONTAINER.get()}).build((Type)null));
      BIOMASS_LUMP = BLOCK_ENTITIES.register("biomass_lump", () -> Builder.of(BiomassLumpEntity::new, new Block[]{(Block)Sblocks.BIOMASS_LUMP.get()}).build((Type)null));
      HIVE_SPAWN = BLOCK_ENTITIES.register("hive_spawn", () -> Builder.of(HiveSpawnBlockEntity::new, new Block[]{(Block)Sblocks.HIVE_SPAWN.get()}).build((Type)null));
      CDU = BLOCK_ENTITIES.register("cdu", () -> Builder.of(CDUBlockEntity::new, new Block[]{(Block)Sblocks.CDU.get()}).build((Type)null));
      ZOAHOLIC = BLOCK_ENTITIES.register("zoaholic", () -> Builder.of(ZoaholicBlockEntity::new, new Block[]{(Block)Sblocks.ZOAHOLIC.get()}).build((Type)null));
      INCUBATOR = BLOCK_ENTITIES.register("incubator_entity", () -> Builder.of(IncubatorBlockEntity::new, new Block[]{(Block)Sblocks.INCUBATOR.get()}).build((Type)null));
      OVERGROWN_SPAWNER = BLOCK_ENTITIES.register("overgrown_spawner", () -> Builder.of(OvergrownSpawnerEntity::new, new Block[]{(Block)Sblocks.OVERGROWN_SPAWNER.get()}).build((Type)null));
      BRAIN_REMNANTS = BLOCK_ENTITIES.register("brain_remnants", () -> Builder.of(BrainRemnantBlockEntity::new, new Block[]{(Block)Sblocks.BRAIN_REMNANTS.get()}).build((Type)null));
      OUTPOST_WATCHER = BLOCK_ENTITIES.register("outpost_watcher_entity", () -> Builder.of(OutpostWatcherBlockEntity::new, new Block[]{(Block)Sblocks.OUTPOST_WATCHER.get()}).build((Type)null));
      SURGERY_TABLE_ENTITY = BLOCK_ENTITIES.register("surgery_table_entity", () -> Builder.of(SurgeryTableBlockEntity::new, new Block[]{(Block)Sblocks.SURGERY_TABLE.get()}).build((Type)null));
      CABINET_ENTITY = BLOCK_ENTITIES.register("cabinet_entity", () -> Builder.of(CabinetBlockEntity::new, new Block[]{(Block)Sblocks.CABINET.get()}).build((Type)null));
   }
}
