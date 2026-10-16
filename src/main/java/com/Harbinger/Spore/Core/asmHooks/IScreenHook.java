package com.Harbinger.Spore.Core.asmHooks;

import net.minecraft.client.gui.screens.Screen;

public interface IScreenHook {
    Screen getScreenHook(Screen initial);

    boolean isDeealthScreen(Screen screen);
}
