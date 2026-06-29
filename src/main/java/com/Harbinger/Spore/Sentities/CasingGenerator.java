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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;


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
        for(int i = 0; i <= 2*radius; ++i) {
            for(int j = 0; j <= 2*radius; ++j) {
                for(int k = 0; k <= 2*radius; ++k) {
                    double distance = Mth.sqrt((i-radius)*(i-radius) + (j-radius)*(j-radius) + (k-radius)*(k-radius));
                    if (Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) {
                        if (distance>radius-(thickness/2d) && distance<radius+(thickness/2d)){
                            BlockPos blockpos = pos.offset(i-radius,j-radius,k-radius);
                            BlockState blockstate = level.getBlockState(blockpos);
                            if (Math.random() < 0.1 && !blockstate.isSolidRender(level,blockpos) && !blockstate.is(Blocks.BARRIER) && compare(level,blockpos)){
                                if (!level.isClientSide){
                                    level.setBlock(blockpos,this.randomCasingBlock(randomSource),3);
                                    if (Math.random() < 0.001){
                                        createSpots(blockpos,level,randomSource.nextInt(3,6),Math.random() < 0.5 ? Sblocks.SICKEN_BIOMASS_BLOCK.get().defaultBlockState():Sblocks.CALCIFIED_BIOMASS_BLOCK.get().defaultBlockState(), randomSource);
                                    }
                                    if (Math.random() < 0.001){
                                        createPussSpots(blockpos,level,randomSource.nextInt(2,5), randomSource);
                                    }
                                    if (Math.random() < 0.005){
                                        if (level.getBlockState(blockpos.above()).isAir()){
                                            createFungalStalks(blockpos,level,randomSource,false);
                                        }else if (level.getBlockState(blockpos.below()).isAir()){
                                            createFungalStalks(blockpos,level,randomSource,true);
                                        }
                                    }
                                }
                            }
                            if (Math.random() < 0.05 && blockstate.isSolidRender(level,blockpos)){
                                for (String str : SConfig.DATAGEN.block_infection.get()){
                                    String[] string = str.split("\\|" );
                                    ItemStack stack = new ItemStack(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[0])));
                                    if (stack != ItemStack.EMPTY && blockstate.getBlock().asItem() == stack.getItem()){
                                        ItemStack itemStack = new ItemStack(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[1])));
                                        if (itemStack != ItemStack.EMPTY && itemStack.getItem() instanceof BlockItem blockItem){
                                            level.setBlock(blockpos,blockItem.getBlock().defaultBlockState(),3);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    default void createSpots(BlockPos pos,Level level, int range,BlockState state){
        createSpots(pos, level, range, state, RandomSource.create());
    }
    default void createSpots(BlockPos pos,Level level, int range,BlockState state, RandomSource source){
        for(int i = 0; i <= 2*range; ++i) {
            for(int j = 0; j <= 2*range; ++j) {
                for(int k = 0; k <= 2*range; ++k) {
                    double distance = Mth.sqrt((float) ((i-range)*(i-range) + (j-range)*(j-range) + (k-range)*(k-range)));
                    if (Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) {
                        if (distance<range+(0.5)){
                            BlockPos blockpos = pos.offset( i-(int)range,j-(int)range,k-(int)range);
                            if (isPossibleBlock(level.getBlockState(blockpos))){
                                level.setBlock(blockpos,withCasingLight(state, source),3);
                            }
                        }}}}}
    }
    default void createPussSpots(BlockPos pos,Level level, int range){
        createPussSpots(pos, level, range, RandomSource.create());
    }
    default void createPussSpots(BlockPos pos,Level level, int range, RandomSource source){
        for(int i = 0; i <= 2*range; ++i) {
            for(int j = 0; j <= 2*range; ++j) {
                for(int k = 0; k <= 2*range; ++k) {
                    double distance = Mth.sqrt((float) ((i-range)*(i-range) + (j-range)*(j-range) + (k-range)*(k-range)));
                    if (Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) {
                        if (distance<range+(0.5) && distance>range-(0.5)){
                            BlockPos blockpos = pos.offset( i-(int)range,j-(int)range,k-(int)range);
                            if (level.getBlockState(blockpos).isAir()){
                                level.setBlock(blockpos,withCasingLight(Math.random() < 0.5 ? Sblocks.GASTRIC_BIOMASS.get().defaultBlockState() : Sblocks.BIOMASS_BLOCK.get().defaultBlockState(), source), 3);
                            }
                        }
                        if (distance<range-(0.5)){
                            BlockPos blockpos = pos.offset( i-(int)range,j-(int)range,k-(int)range);
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
