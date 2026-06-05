package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public final class SGameRules implements IGameRules {
    public static final IGameRules INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IGameRules.class,
            SGameRules.class
    );
    private final GameRules.Key<GameRules.BooleanValue> CASLING_LIGHT;
    public SGameRules() {
        CASLING_LIGHT=GameRules.register("doCasingLight",
                GameRules.Category.MISC, GameRules.BooleanValue.create(false));
    }
    @Override
    public boolean casingLightAllowed(Level level) {
        return level.getGameRules().getBoolean(CASLING_LIGHT);
    }


}
