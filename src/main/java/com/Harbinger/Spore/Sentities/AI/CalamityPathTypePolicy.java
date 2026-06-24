package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.Calamities.Gazenbrecher;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.event.ForgeEventFactory;

final class CalamityPathTypePolicy {
   static final float WATER_CALAMITY_WATER_MALUS = 0.0F;
   private static final BlockPathTypes FIRE_ADAPTED_GAZEN_LAVA_PATH_TYPE = BlockPathTypes.BREACH;

   private CalamityPathTypePolicy() {
   }

   static BlockPathTypes getLandOrAirBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType) {
      return getBlockPathTypeWithFluidPolicy(mob, getter, pos, originalType, FluidAvoidanceProfile.LAND_OR_AIR);
   }

   static BlockPathTypes getWaterCalamityLandBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType) {
      return getBlockPathTypeWithFluidPolicy(mob, getter, pos, originalType, FluidAvoidanceProfile.WATER_CALAMITY_ON_LAND);
   }

   static BlockPathTypes getWaterCalamityWaterBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType) {
      return getBlockPathTypeWithFluidPolicy(mob, getter, pos, originalType, FluidAvoidanceProfile.WATER_CALAMITY_IN_WATER);
   }

   private static BlockPathTypes getBlockPathTypeWithFluidPolicy(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType, FluidAvoidanceProfile profile) {
      BlockPathTypes pathType = getCalamityBlockPathType(mob, getter, pos, originalType);
      if (isFireAdaptedGazenLava(mob, getter, pos, pathType)) {
         return FIRE_ADAPTED_GAZEN_LAVA_PATH_TYPE;
      }
      if (pathType != BlockPathTypes.BLOCKED && shouldAvoidFluidOrSnow(mob, getter, pos, pathType, profile)) {
         return BlockPathTypes.DANGER_OTHER;
      }
      return pathType;
   }

   private static BlockPathTypes getCalamityBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType) {
      if (originalType != BlockPathTypes.BLOCKED || !ForgeEventFactory.getMobGriefingEvent(mob.level(), mob)) {
         return originalType;
      }
      BlockState blockState = getter.getBlockState(pos);
      if (canDestroyForPath(mob, pos, blockState)) {
         return BlockPathTypes.DANGER_OTHER;
      }
      return BlockPathTypes.BLOCKED;
   }

   private static boolean canDestroyForPath(Mob mob, BlockPos pos, BlockState blockState) {
      if (!(mob instanceof Calamity calamity)) {
         return false;
      }
      if (blockState.isAir()) {
         return false;
      }
      float destroySpeed = blockState.getDestroySpeed(mob.level(), pos);
      return blockState.is(Utilities.biomass) || destroySpeed >= 0.0F && destroySpeed < calamity.getDestroySpeed();
   }

   private static boolean shouldAvoidFluidOrSnow(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes pathType, FluidAvoidanceProfile profile) {
      FluidState fluidState = getter.getFluidState(pos);
      return switch (profile) {
         case LAND_OR_AIR -> isWaterPathType(pathType) || isAvoidedLavaPathType(mob, pathType) || isPowderSnowPathType(pathType) || !fluidState.isEmpty();
         case WATER_CALAMITY_ON_LAND -> isAvoidedLavaPathType(mob, pathType) || isPowderSnowPathType(pathType) || isNonWaterFluid(fluidState);
         case WATER_CALAMITY_IN_WATER -> pathType == BlockPathTypes.WATER_BORDER || isAvoidedLavaPathType(mob, pathType) || isPowderSnowPathType(pathType) || isNonWaterFluid(fluidState);
      };
   }

   private static boolean isWaterPathType(BlockPathTypes pathType) {
      return pathType == BlockPathTypes.WATER || pathType == BlockPathTypes.WATER_BORDER;
   }

   private static boolean isAvoidedLavaPathType(Mob mob, BlockPathTypes pathType) {
      return pathType == BlockPathTypes.LAVA && !isFireAdaptedGazen(mob);
   }

   private static boolean isFireAdaptedGazenLava(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes pathType) {
      return isFireAdaptedGazen(mob) && (pathType == BlockPathTypes.LAVA || getter.getFluidState(pos).is(FluidTags.LAVA));
   }

   private static boolean isFireAdaptedGazen(Mob mob) {
      return mob instanceof Gazenbrecher gazen && gazen.isAdaptedToFire();
   }

   private static boolean isPowderSnowPathType(BlockPathTypes pathType) {
      return pathType == BlockPathTypes.POWDER_SNOW || pathType == BlockPathTypes.DANGER_POWDER_SNOW;
   }

   private static boolean isNonWaterFluid(FluidState fluidState) {
      return !fluidState.isEmpty() && !fluidState.is(FluidTags.WATER);
   }

   private enum FluidAvoidanceProfile {
      LAND_OR_AIR,
      WATER_CALAMITY_ON_LAND,
      WATER_CALAMITY_IN_WATER
   }
}
