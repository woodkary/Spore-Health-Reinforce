package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.Fluids.BileLiquid;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.FluidType.Properties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class Sfluids {
   public static final DeferredRegister SPORE_FLUID_TYPE;
   public static final DeferredRegister SPORE_FLUID;
   public static final RegistryObject BILE_FLUID_TYPE;
   public static final RegistryObject Bile_FLUID_SOURCE;
   public static final RegistryObject BILE_FLUID_FLOWING;

   private static ForgeFlowingFluid.Properties bileProperties() {
      return (new ForgeFlowingFluid.Properties(BILE_FLUID_TYPE, Bile_FLUID_SOURCE, BILE_FLUID_FLOWING)).bucket(Sitems.BUCKET_OF_BILE).block(Sblocks.BILE);
   }

   public static void postInit() {
      FluidInteractionRegistry.addInteraction((FluidType)BILE_FLUID_TYPE.get(), new FluidInteractionRegistry.InteractionInformation((FluidType)ForgeMod.WATER_TYPE.get(), (fluidState) -> ((Block)Sblocks.BIOMASS_BLOCK.get()).defaultBlockState()));
      FluidInteractionRegistry.addInteraction((FluidType)BILE_FLUID_TYPE.get(), new FluidInteractionRegistry.InteractionInformation((FluidType)ForgeMod.LAVA_TYPE.get(), (fluidState) -> ((Block)Sblocks.ROOTED_BIOMASS.get()).defaultBlockState()));
   }

   static {
      SPORE_FLUID_TYPE = DeferredRegister.create(Keys.FLUID_TYPES, "spore");
      SPORE_FLUID = DeferredRegister.create(ForgeRegistries.FLUIDS, "spore");
      BILE_FLUID_TYPE = SPORE_FLUID_TYPE.register("bile", () -> new BileLiquid(Properties.create().lightLevel(5).density(1024).viscosity(1024).pathType(BlockPathTypes.LAVA).adjacentPathType(BlockPathTypes.DANGER_OTHER).sound(SoundActions.BUCKET_EMPTY, SoundEvents.WATER_AMBIENT).sound(SoundActions.BUCKET_FILL, SoundEvents.WATER_AMBIENT)));
      Bile_FLUID_SOURCE = SPORE_FLUID.register("bile", () -> new ForgeFlowingFluid.Source(bileProperties()));
      BILE_FLUID_FLOWING = SPORE_FLUID.register("bile_flowing", () -> new ForgeFlowingFluid.Flowing(bileProperties()));
   }
}
