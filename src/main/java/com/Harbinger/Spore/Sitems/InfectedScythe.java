package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeDiggerTools;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

public class InfectedScythe extends SporeDiggerTools {
   protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES;

   public InfectedScythe() {
      super((double)(Integer)SConfig.SERVER.scythe_damage.get(), (double)2.5F, (double)3.0F, (Integer)SConfig.SERVER.scythe_durability.get(), 3, "scythe", BlockTags.MINEABLE_WITH_HOE);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos blockpos = context.getClickedPos();
      Player player = context.getPlayer();
      BlockState toolModifiedState = level.getBlockState(blockpos).getToolModifiedState(context, ToolActions.HOE_TILL, false);
      Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = toolModifiedState == null ? null : Pair.of((Predicate<UseOnContext>)(ctx) -> true, changeIntoState(toolModifiedState));
      if (pair == null) {
         return InteractionResult.PASS;
      } else {
         Predicate<UseOnContext> predicate = pair.getFirst();
         Consumer<UseOnContext> consumer = pair.getSecond();
         if (predicate.test(context)) {
            level.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide) {
               consumer.accept(context);
               if (player != null) {
                  this.hurtTool(context.getItemInHand(), player, 1);
               }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return toolAction == ToolActions.SWORD_SWEEP || ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction) || toolAction == ToolActions.SHEARS_DIG;
   }

   public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
      return new AABB(target.getX() - (double)4.0F, target.getY(), target.getZ() - (double)4.0F, target.getX() + (double)4.0F, target.getY() + (double)4.0F, target.getZ() + (double)4.0F);
   }

   public static Consumer<UseOnContext> changeIntoState(BlockState p_150859_) {
      return (p_238241_) -> {
         p_238241_.getLevel().setBlock(p_238241_.getClickedPos(), p_150859_, 11);
         p_238241_.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, p_238241_.getClickedPos(), Context.of(p_238241_.getPlayer(), p_150859_));
      };
   }

   public static Consumer<UseOnContext> changeIntoStateAndDropItem(BlockState p_150850_, ItemLike p_150851_) {
      return (p_238246_) -> {
         p_238246_.getLevel().setBlock(p_238246_.getClickedPos(), p_150850_, 11);
         p_238246_.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, p_238246_.getClickedPos(), Context.of(p_238246_.getPlayer(), p_150850_));
         Block.popResourceFromFace(p_238246_.getLevel(), p_238246_.getClickedPos(), p_238246_.getClickedFace(), new ItemStack(p_150851_));
      };
   }

   static {
      TILLABLES = Maps.newHashMap(ImmutableMap.of(
         Blocks.GRASS_BLOCK, Pair.<Predicate<UseOnContext>, Consumer<UseOnContext>>of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
         Blocks.DIRT_PATH, Pair.<Predicate<UseOnContext>, Consumer<UseOnContext>>of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
         Blocks.DIRT, Pair.<Predicate<UseOnContext>, Consumer<UseOnContext>>of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
         Blocks.COARSE_DIRT, Pair.<Predicate<UseOnContext>, Consumer<UseOnContext>>of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.DIRT.defaultBlockState())),
         Blocks.ROOTED_DIRT, Pair.<Predicate<UseOnContext>, Consumer<UseOnContext>>of((p_238242_) -> true, changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))));
   }
}
