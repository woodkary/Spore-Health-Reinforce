package com.Harbinger.Spore.Sentities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.Sblocks.CasingBiomassBlock;
import com.Harbinger.Spore.Sblocks.MembraneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface CasingGenerator {
    private boolean compare(Level level,BlockPos blockpos){
        boolean propery1 = level.getBlockState(blockpos.below()).isSolidRender(level,blockpos);
        boolean propery2 = level.getBlockState(blockpos.above()).isSolidRender(level,blockpos);
        boolean properzx1 = level.getBlockState(blockpos.east()).isSolidRender(level,blockpos);
        boolean properzx2 = level.getBlockState(blockpos.south()).isSolidRender(level,blockpos);
        boolean properzx3 = level.getBlockState(blockpos.north()).isSolidRender(level,blockpos);
        boolean properzx4 = level.getBlockState(blockpos.west()).isSolidRender(level,blockpos);
        return propery1 || propery2 || properzx1 || properzx2 || properzx3 || properzx4;
    }

    default List<BlockState> possibleBlocks(){
        return List.of(
                Sblocks.BIOMASS_BLOCK.get().defaultBlockState(),
                Sblocks.BIOMASS_BLOCK.get().defaultBlockState(),
                Sblocks.BIOMASS_BLOCK.get().defaultBlockState(),
                Sblocks.ROOTED_BIOMASS.get().defaultBlockState(),
                Sblocks.ROOTED_BIOMASS.get().defaultBlockState(),
                Sblocks.ROOTED_BIOMASS.get().defaultBlockState(),
                Sblocks.CALCIFIED_BIOMASS_BLOCK.get().defaultBlockState(),
                Sblocks.SICKEN_BIOMASS_BLOCK.get().defaultBlockState(),
                Sblocks.GASTRIC_BIOMASS.get().defaultBlockState()
        );
    }
    default List<BlockState> fungalStalkBlocks(){
        return List.of(
                Sblocks.MYCELIUM_BLOCK.get().defaultBlockState(),
                Sblocks.FUNGAL_SHELL.get().defaultBlockState()
        );
    }
    default Level getProtoLevel(){
        return null;
    }
    private BlockState withCasingLight(BlockState state, RandomSource source) {
        if (!state.hasProperty(CasingBiomassBlock.LIT)) {
            return state;
        }
        BlockState unlitState = state.setValue(CasingBiomassBlock.LIT, false);
        Level level = this.getProtoLevel();
        if (level instanceof ServerLevel serverLevel && SporeSavedData.get(serverLevel).isCasingLightAllowed() && source.nextFloat() < 0.3F) {
            return unlitState.setValue(CasingBiomassBlock.LIT, true);
        }
        return unlitState;
    }
    private BlockState randomCasingBlock(RandomSource source) {
        List<BlockState> blocks = this.possibleBlocks();
        return this.withCasingLight(blocks.get(source.nextInt(blocks.size())), source);
    }
    private boolean isPossibleBlock(BlockState state) {
        if (state.hasProperty(CasingBiomassBlock.LIT)) {
            state = state.setValue(CasingBiomassBlock.LIT, false);
        }
        return this.possibleBlocks().contains(state);
    }
    default void generateChasing(BlockPos pos,Entity entity, int radius){
        this.generateChasing(pos,entity,radius,1);
    }
    default void generateChasing(BlockPos pos,Entity entity, int radius,int thickness){
        Level level = entity.level();
        RandomSource randomSource = RandomSource.create();
        Map<Block, Block> blockInfections = resolveBlockInfections();
        double minDistance = radius - thickness / 2.0D;
        double maxDistance = radius + thickness / 2.0D;
        double minDistanceSqr = minDistance * minDistance;
        double maxDistanceSqr = maxDistance * maxDistance;
        for (int offsetX = -radius; offsetX <= radius; ++offsetX) {
            for (int offsetY = -radius; offsetY <= radius; ++offsetY) {
                double horizontalDistanceSqr = offsetX * offsetX + offsetY * offsetY;
                double remainingOuterDistance = maxDistanceSqr - horizontalDistanceSqr;
                if (remainingOuterDistance <= 0.0D) {
                    continue;
                }
                int maxOffsetZ = Math.min(radius,
                        (int) Math.ceil(Math.sqrt(remainingOuterDistance)) - 1);
                double remainingInnerDistance = minDistanceSqr - horizontalDistanceSqr;
                int minOffsetZ = remainingInnerDistance < 0.0D
                        ? 0
                        : (int) Math.floor(Math.sqrt(remainingInnerDistance)) + 1;
                for (int offsetZ = minOffsetZ; offsetZ <= maxOffsetZ; ++offsetZ) {
                    generateCasingAt(level, pos, offsetX, offsetY, offsetZ, randomSource, blockInfections);
                    if (offsetZ != 0) {
                        generateCasingAt(level, pos, offsetX, offsetY, -offsetZ, randomSource, blockInfections);
                    }
                }
            }
        }
    }

    private void generateCasingAt(Level level, BlockPos center, int offsetX, int offsetY, int offsetZ,
                                  RandomSource source, Map<Block, Block> blockInfections) {
        BlockPos blockpos = center.offset(offsetX, offsetY, offsetZ);
        if (!level.hasChunkAt(blockpos)) {
            return;
        }
        BlockState blockstate = level.getBlockState(blockpos);
        if (source.nextFloat() < 0.1F && !blockstate.isSolidRender(level, blockpos)
                && !blockstate.is(Blocks.BARRIER) && compare(level, blockpos)) {
            if (!level.isClientSide) {
                level.setBlock(blockpos, this.randomCasingBlock(source), 3);
                if (source.nextFloat() < 0.001F) {
                    createSpots(blockpos, level, source.nextInt(3, 6),
                            source.nextBoolean()
                                    ? Sblocks.SICKEN_BIOMASS_BLOCK.get().defaultBlockState()
                                    : Sblocks.CALCIFIED_BIOMASS_BLOCK.get().defaultBlockState(),
                            source);
                }
                if (source.nextFloat() < 0.001F) {
                    createPussSpots(blockpos, level, source.nextInt(2, 5), source);
                }
                if (source.nextFloat() < 0.005F) {
                    if (level.getBlockState(blockpos.above()).isAir()) {
                        createFungalStalks(blockpos, level, source, false);
                    } else if (level.getBlockState(blockpos.below()).isAir()) {
                        createFungalStalks(blockpos, level, source, true);
                    }
                }
            }
        }
        if (source.nextFloat() < 0.05F && blockstate.isSolidRender(level, blockpos)) {
            Block replacement = blockInfections.get(blockstate.getBlock());
            if (replacement != null) {
                level.setBlock(blockpos, replacement.defaultBlockState(), 3);
            }
        }
    }

    private Map<Block, Block> resolveBlockInfections() {
        Map<Block, Block> result = new HashMap<>();
        for (String entry : SConfig.DATAGEN.block_infection.get()) {
            String[] parts = entry.split("\\|", 2);
            if (parts.length != 2) {
                continue;
            }
            ResourceLocation fromId = ResourceLocation.tryParse(parts[0]);
            ResourceLocation toId = ResourceLocation.tryParse(parts[1]);
            if (fromId == null || toId == null) {
                continue;
            }
            Block from = ForgeRegistries.BLOCKS.getValue(fromId);
            Block to = ForgeRegistries.BLOCKS.getValue(toId);
            if (from != null && to != null) {
                result.put(from, to);
            }
        }
        return result;
    }

    default void createSpots(BlockPos pos,Level level, int range,BlockState state){
        createSpots(pos, level, range, state, RandomSource.create());
    }
    default void createSpots(BlockPos pos,Level level, int range,BlockState state, RandomSource source){
        double maxDistance = range + 0.5D;
        double maxDistanceSqr = maxDistance * maxDistance;
        for(int i = 0; i <= 2*range; ++i) {
            for(int j = 0; j <= 2*range; ++j) {
                for(int k = 0; k <= 2*range; ++k) {
                    int offsetX = i - range;
                    int offsetY = j - range;
                    int offsetZ = k - range;
                    double distanceSqr = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
                    if (Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) {
                        if (distanceSqr < maxDistanceSqr){
                            BlockPos blockpos = pos.offset(offsetX, offsetY, offsetZ);
                            if (isPossibleBlock(level.getBlockState(blockpos))){
                                level.setBlock(blockpos,withCasingLight(state, source),3);
                            }
                        }}}}}
    }
    default void createPussSpots(BlockPos pos,Level level, int range){
        createPussSpots(pos, level, range, RandomSource.create());
    }
    default void createPussSpots(BlockPos pos,Level level, int range, RandomSource source){
        double outerDistance = range + 0.5D;
        double innerDistance = range - 0.5D;
        double outerDistanceSqr = outerDistance * outerDistance;
        double innerDistanceSqr = innerDistance * innerDistance;
        for(int i = 0; i <= 2*range; ++i) {
            for(int j = 0; j <= 2*range; ++j) {
                for(int k = 0; k <= 2*range; ++k) {
                    int offsetX = i - range;
                    int offsetY = j - range;
                    int offsetZ = k - range;
                    double distanceSqr = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
                    if (Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) {
                        if (distanceSqr < outerDistanceSqr && distanceSqr > innerDistanceSqr){
                            BlockPos blockpos = pos.offset(offsetX, offsetY, offsetZ);
                            if (level.getBlockState(blockpos).isAir()){
                                level.setBlock(blockpos,withCasingLight(Math.random() < 0.5 ? Sblocks.GASTRIC_BIOMASS.get().defaultBlockState() : Sblocks.BIOMASS_BLOCK.get().defaultBlockState(), source), 3);
                            }
                        }
                        if (distanceSqr < innerDistanceSqr){
                            BlockPos blockpos = pos.offset(offsetX, offsetY, offsetZ);
                            if (level.getBlockState(blockpos).isAir()){
                                level.setBlock(blockpos,withCasingLight(Math.random() < 0.5 ? Sblocks.SICKEN_BIOMASS_BLOCK.get().defaultBlockState() : Sblocks.BILE.get().defaultBlockState(), source), 3);
                            }
                        }
                    }}}}
    }
    default void createFungalStalks(BlockPos pos,Level level,RandomSource source,boolean down){
        int random  =down ? -source.nextInt(4,16) : source.nextInt(4,16);
        List<BlockState> states = fungalStalkBlocks();
        for (int i = 0; i <= random;i++){
            BlockState state = states.get(source.nextInt(states.size()));
            int randomX = source.nextInt(-1,1);
            int randomZ = source.nextInt(-1,1);
            BlockPos blockPos = pos.offset(randomX,i,randomZ);
            if (i % 2 == 0){
                if (level.getBlockState(blockPos).isAir()){
                    level.setBlock(blockPos,state,3);
                }
                if (level.getBlockState(blockPos.above()).isAir()){
                    level.setBlock(blockPos.above(),state,3);
                }
            }
            if (i == random-1){
                if (level.getBlockState(blockPos.above()).isAir()){
                    level.setBlock(blockPos.above(),Sblocks.ORGANITE.get().defaultBlockState(),3);
                }
                if (level.getBlockState(blockPos).isAir()){
                    level.setBlock(blockPos,state,3);
                }
            }
        }
    }
}
