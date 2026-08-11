package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeDiggerTools;
import com.Harbinger.Spore.Sitems.BaseWeapons.scytheFunctions.ChangeIntoState;
import com.Harbinger.Spore.Sitems.BaseWeapons.scytheFunctions.ChangeIntoStateAndDropItem;
import com.Harbinger.Spore.Sitems.BaseWeapons.scytheFunctions.TrueUseOnContextPredicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class InfectedScythe extends SporeDiggerTools implements Predicate<UseOnContext> {
    private final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES;
    public InfectedScythe() {
        super(SConfig.SERVER.scythe_damage.get(), 2.5f, 3F, SConfig.SERVER.scythe_durability.get(), 3,"scythe", BlockTags.MINEABLE_WITH_HOE);
        TILLABLES = new HashMap<>(ImmutableMap.of(Blocks.GRASS_BLOCK, Pair.of(this, new ChangeIntoState(Blocks.FARMLAND.defaultBlockState())), Blocks.DIRT_PATH, Pair.of(this, new ChangeIntoState(Blocks.FARMLAND.defaultBlockState())), Blocks.DIRT, Pair.of(this, new ChangeIntoState(Blocks.FARMLAND.defaultBlockState())), Blocks.COARSE_DIRT, Pair.of(this, new ChangeIntoState(Blocks.DIRT.defaultBlockState())), Blocks.ROOTED_DIRT, Pair.of(TrueUseOnContextPredicate.INSTANCE, new ChangeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))));
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState toolModifiedState = level.getBlockState(blockpos).getToolModifiedState(context, ToolActions.HOE_TILL, false);
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = toolModifiedState == null ? null : Pair.of(TrueUseOnContextPredicate.INSTANCE, new ChangeIntoState(toolModifiedState));
        if (pair == null) {
            return InteractionResult.PASS;
        } else {
            Predicate<UseOnContext> predicate = pair.getFirst();
           Consumer<UseOnContext> consumer = pair.getSecond();
            if (predicate.test(context)) {
                level.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!level.isClientSide) {
                    consumer.accept(context);
                    if (player != null){
                        hurtTool(context.getItemInHand(),player,1);
                    }
                }
                 return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return toolAction == ToolActions.SWORD_SWEEP || ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction) || toolAction == ToolActions.SHEARS_DIG;
    }

    @Override
    public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
        return new AABB(target.getX()-4,target.getY(),target.getZ()-4,target.getX()+4,target.getY()+4,target.getZ()+4);
    }

    @Override
    public boolean test(UseOnContext useOnContext) {
        return HoeItem.onlyIfAirAbove(useOnContext);
    }
}

