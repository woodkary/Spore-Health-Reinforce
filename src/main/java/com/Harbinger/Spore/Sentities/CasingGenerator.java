package com.Harbinger.Spore.Sentities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public interface CasingGenerator {
   private boolean compare(Level level, BlockPos blockpos) {
      boolean propery1 = level.getBlockState(blockpos.below()).isSolidRender(level, blockpos);
      boolean propery2 = level.getBlockState(blockpos.above()).isSolidRender(level, blockpos);
      boolean properzx1 = level.getBlockState(blockpos.east()).isSolidRender(level, blockpos);
      boolean properzx2 = level.getBlockState(blockpos.south()).isSolidRender(level, blockpos);
      boolean properzx3 = level.getBlockState(blockpos.north()).isSolidRender(level, blockpos);
      boolean properzx4 = level.getBlockState(blockpos.west()).isSolidRender(level, blockpos);
      return propery1 || propery2 || properzx1 || properzx2 || properzx3 || properzx4;
   }
   Level getProtoLevel();

   default List<BlockState> possibleBlocks() {
      List<BlockState> values = new ArrayList<>();
      values.add(((Block)Sblocks.BIOMASS_BLOCK.get()).defaultBlockState());
      values.add(((Block)Sblocks.BIOMASS_BLOCK.get()).defaultBlockState());
      values.add(((Block)Sblocks.BIOMASS_BLOCK.get()).defaultBlockState());
      values.add(((Block)Sblocks.ROOTED_BIOMASS.get()).defaultBlockState());
      values.add(((Block)Sblocks.ROOTED_BIOMASS.get()).defaultBlockState());
      values.add(((Block)Sblocks.ROOTED_BIOMASS.get()).defaultBlockState());
      values.add(((Block)Sblocks.CALCIFIED_BIOMASS_BLOCK.get()).defaultBlockState());
      values.add(((Block)Sblocks.SICKEN_BIOMASS_BLOCK.get()).defaultBlockState());
      values.add(((Block)Sblocks.GASTRIC_BIOMASS.get()).defaultBlockState());
      return values;
   }

   default void generateChasing(BlockPos pos, Entity entity, int radius) {
      this.generateChasing(pos, entity, radius, 1);
   }

   default void generateChasing(BlockPos pos, Entity entity, int radius, int thickness) {
      Level level = entity.level();
      RandomSource randomSource = RandomSource.create();

      for(int i = 0; i <= 2 * radius; ++i) {
         for(int j = 0; j <= 2 * radius; ++j) {
            for(int k = 0; k <= 2 * radius; ++k) {
               double distance = (double)Mth.sqrt((float)((i - radius) * (i - radius) + (j - radius) * (j - radius) + (k - radius) * (k - radius)));
               if ((Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) && distance > (double)radius - (double)thickness / (double)2.0F && distance < (double)radius + (double)thickness / (double)2.0F) {
                  BlockPos blockpos = pos.offset(i - radius, j - radius, k - radius);
                  BlockState blockstate = level.getBlockState(blockpos);
                  if (Math.random() < 0.1 && !blockstate.isSolidRender(level, blockpos) && !blockstate.is(Blocks.BARRIER) && this.compare(level, blockpos) && !level.isClientSide) {
                     level.setBlock(blockpos, (BlockState)this.possibleBlocks().get(randomSource.nextInt(this.possibleBlocks().size())), 3);
                     if (Math.random() < 0.001) {
                        this.createSpots(blockpos, level, randomSource.nextInt(3, 6), Math.random() < (double)0.5F ? ((Block)Sblocks.SICKEN_BIOMASS_BLOCK.get()).defaultBlockState() : ((Block)Sblocks.CALCIFIED_BIOMASS_BLOCK.get()).defaultBlockState());
                     }

                     if (Math.random() < 0.001) {
                        this.createPussSpots(blockpos, level, randomSource.nextInt(2, 5));
                     }

                     if (Math.random() < 0.005) {
                        if (level.getBlockState(blockpos.above()).isAir()) {
                           this.createFungalStalks(blockpos, level, randomSource, false);
                        } else if (level.getBlockState(blockpos.below()).isAir()) {
                           this.createFungalStalks(blockpos, level, randomSource, true);
                        }
                     }
                  }

                  if (Math.random() < 0.05 && blockstate.isSolidRender(level, blockpos)) {
                     for(String str : (List<String>)SConfig.DATAGEN.block_infection.get()) {
                        String[] string = str.split("\\|");
                        ItemStack stack = new ItemStack((ItemLike)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[0])));
                        if (stack != ItemStack.EMPTY && blockstate.getBlock().asItem() == stack.getItem()) {
                           ItemStack itemStack = new ItemStack((ItemLike)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[1])));
                           if (itemStack != ItemStack.EMPTY) {
                              Item var20 = itemStack.getItem();
                              if (var20 instanceof BlockItem) {
                                 BlockItem blockItem = (BlockItem)var20;
                                 level.setBlock(blockpos, blockItem.getBlock().defaultBlockState(), 3);
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

   default void createSpots(BlockPos pos, Level level, int range, BlockState state) {
      for(int i = 0; i <= 2 * range; ++i) {
         for(int j = 0; j <= 2 * range; ++j) {
            for(int k = 0; k <= 2 * range; ++k) {
               double distance = (double)Mth.sqrt((float)((i - range) * (i - range) + (j - range) * (j - range) + (k - range) * (k - range)));
               if ((Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) && distance < (double)range + (double)0.5F) {
                  BlockPos blockpos = pos.offset(i - range, j - range, k - range);
                  if (this.possibleBlocks().contains(level.getBlockState(blockpos))) {
                     level.setBlock(blockpos, state, 3);
                  }
               }
            }
         }
      }

   }

   default void createPussSpots(BlockPos pos, Level level, int range) {
      for(int i = 0; i <= 2 * range; ++i) {
         for(int j = 0; j <= 2 * range; ++j) {
            for(int k = 0; k <= 2 * range; ++k) {
               double distance = (double)Mth.sqrt((float)((i - range) * (i - range) + (j - range) * (j - range) + (k - range) * (k - range)));
               if (Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) {
                  if (distance < (double)range + (double)0.5F && distance > (double)range - (double)0.5F) {
                     BlockPos blockpos = pos.offset(i - range, j - range, k - range);
                     if (level.getBlockState(blockpos).isAir()) {
                        level.setBlock(blockpos, Math.random() < (double)0.5F ? ((Block)Sblocks.GASTRIC_BIOMASS.get()).defaultBlockState() : ((Block)Sblocks.BIOMASS_BLOCK.get()).defaultBlockState(), 3);
                     }
                  }

                  if (distance < (double)range - (double)0.5F) {
                     BlockPos blockpos = pos.offset(i - range, j - range, k - range);
                     if (level.getBlockState(blockpos).isAir()) {
                        level.setBlock(blockpos, Math.random() < (double)0.5F ? ((Block)Sblocks.SICKEN_BIOMASS_BLOCK.get()).defaultBlockState() : ((LiquidBlock)Sblocks.BILE.get()).defaultBlockState(), 3);
                     }
                  }
               }
            }
         }
      }

   }

   default void createFungalStalks(BlockPos pos, Level level, RandomSource source, boolean down) {
      int random = down ? -source.nextInt(4, 16) : source.nextInt(4, 16);
      List<BlockState> states = new ArrayList() {
         {
            this.add(((Block)Sblocks.MYCELIUM_BLOCK.get()).defaultBlockState());
            this.add(((Block)Sblocks.FUNGAL_SHELL.get()).defaultBlockState());
         }
      };

      for(int i = 0; i <= random; ++i) {
         BlockState state = (BlockState)states.get(source.nextInt(states.size()));
         int randomX = source.nextInt(-1, 1);
         int randomZ = source.nextInt(-1, 1);
         BlockPos blockPos = pos.offset(randomX, i, randomZ);
         if (i % 2 == 0) {
            if (level.getBlockState(blockPos).isAir()) {
               level.setBlock(blockPos, state, 3);
            }

            if (level.getBlockState(blockPos.above()).isAir()) {
               level.setBlock(blockPos.above(), state, 3);
            }
         }

         if (i == random - 1) {
            if (level.getBlockState(blockPos.above()).isAir()) {
               level.setBlock(blockPos.above(), ((Block)Sblocks.ORGANITE.get()).defaultBlockState(), 3);
            }

            if (level.getBlockState(blockPos).isAir()) {
               level.setBlock(blockPos, state, 3);
            }
         }
      }

   }
}
