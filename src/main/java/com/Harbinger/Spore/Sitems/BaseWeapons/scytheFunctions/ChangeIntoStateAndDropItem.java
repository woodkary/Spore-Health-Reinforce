package com.Harbinger.Spore.Sitems.BaseWeapons.scytheFunctions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Consumer;

public final class ChangeIntoStateAndDropItem implements Consumer<UseOnContext> {
    private final BlockState blockState;
    private final ItemLike item;

    public ChangeIntoStateAndDropItem(BlockState blockState, ItemLike item) {
        this.blockState = blockState;
        this.item = item;
    }

    @Override
    public void accept(UseOnContext useOnContext) {
        useOnContext.getLevel().setBlock(useOnContext.getClickedPos(), blockState, 11);
        useOnContext.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, useOnContext.getClickedPos(), GameEvent.Context.of(useOnContext.getPlayer(), blockState));
        Block.popResourceFromFace(useOnContext.getLevel(), useOnContext.getClickedPos(), useOnContext.getClickedFace(), new ItemStack(item));
    }
}
