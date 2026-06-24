package com.Harbinger.Spore.Sentities.AI;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public interface IPathTypePolicy {
    BlockPathTypes getLandOrAirBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType);
    BlockPathTypes getWaterCalamityLandBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType);
    BlockPathTypes getWaterCalamityWaterBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType);
}
