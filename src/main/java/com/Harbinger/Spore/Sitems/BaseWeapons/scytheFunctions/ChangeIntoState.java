package com.Harbinger.Spore.Sitems.BaseWeapons.scytheFunctions;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Consumer;

public final class ChangeIntoState implements Consumer<UseOnContext> {
    private final BlockState state;
    public ChangeIntoState(BlockState state) {
        this.state = state;
    }
    @Override
    public void accept(UseOnContext useOnContext) {
        useOnContext.getLevel().setBlock(useOnContext.getClickedPos(), state, 11);
        useOnContext.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, useOnContext.getClickedPos(), GameEvent.Context.of(useOnContext.getPlayer(), state));
    }
}
