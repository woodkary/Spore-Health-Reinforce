package com.Harbinger.Spore.Sitems.BaseWeapons;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class SporeDiggerTools extends SporeToolsBaseItem {
   protected final TagKey<Block> blocks;

   public SporeDiggerTools(double meleeDamage, double meleeReach, double meleeRecharge, int durability, int miningLevel, String desc, TagKey<Block> blocks) {
      super(meleeDamage, meleeReach, meleeRecharge, durability, miningLevel, desc);
      this.blocks = blocks;
   }

   public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity living) {
      if (state.getDestroySpeed(level, pos) != 0.0F) {
         this.hurtTool(stack, living, 1);
      }

      if (this.canMultiBreak(stack, level, state, pos, living) && this.tooHurt(stack)) {
         for(BlockPos blockPos : getBlocksToBeDestroyed(1, pos, living)) {
            if (level.getBlockState(blockPos).is(this.blocks)) {
               level.destroyBlock(blockPos, true, living);
               this.hurtTool(stack, living, 1);
            }
         }
      }

      return super.mineBlock(stack, level, state, pos, living);
   }

   public float getDestroySpeed(ItemStack stack, BlockState state) {
      return state.is(this.blocks) ? (float)this.miningLevel : 1.0F;
   }

   public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
      return state.is(this.blocks);
   }

   public static List<BlockPos> getBlocksToBeDestroyed(int range, BlockPos initalBlockPos, LivingEntity player) {
      List<BlockPos> positions = new ArrayList();
      BlockHitResult traceResult = player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale((double)6.0F)), ClipContext.Block.COLLIDER, Fluid.NONE, player));
      if (traceResult.getType() == Type.MISS) {
         return positions;
      } else {
         if (traceResult.getDirection() == Direction.DOWN || traceResult.getDirection() == Direction.UP) {
            for(int x = -range; x <= range; ++x) {
               for(int y = -range; y <= range; ++y) {
                  positions.add(new BlockPos(initalBlockPos.getX() + x, initalBlockPos.getY(), initalBlockPos.getZ() + y));
               }
            }
         }

         if (traceResult.getDirection() == Direction.NORTH || traceResult.getDirection() == Direction.SOUTH) {
            for(int x = -range; x <= range; ++x) {
               for(int y = -range; y <= range; ++y) {
                  positions.add(new BlockPos(initalBlockPos.getX() + x, initalBlockPos.getY() + y, initalBlockPos.getZ()));
               }
            }
         }

         if (traceResult.getDirection() == Direction.EAST || traceResult.getDirection() == Direction.WEST) {
            for(int x = -range; x <= range; ++x) {
               for(int y = -range; y <= range; ++y) {
                  positions.add(new BlockPos(initalBlockPos.getX(), initalBlockPos.getY() + y, initalBlockPos.getZ() + x));
               }
            }
         }

         return positions;
      }
   }

   public boolean canMultiBreak(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity living) {
      return false;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.BLOCK_FORTUNE, Enchantments.SILK_TOUCH).contains(enchantment);
   }
}
