package com.Harbinger.Spore.Sitems.BaseWeapons.scytheFunctions;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.world.item.context.UseOnContext;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class TrueUseOnContextPredicate implements Predicate<UseOnContext> {
    public static final Predicate<UseOnContext> INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            Predicate.class,
            TrueUseOnContextPredicate.class
    );
    @Override
    public boolean test(UseOnContext useOnContext) {
        return true;
    }
}
