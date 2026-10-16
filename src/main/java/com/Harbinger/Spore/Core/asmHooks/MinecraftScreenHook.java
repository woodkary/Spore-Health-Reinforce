package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassReflectionUtil;
import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MinecraftScreenHook implements IScreenHook {
    public static final IScreenHook INSTANCE;
    @Override
    public Screen getScreenHook(Screen initial) {
        if (initial == null) {
            return null;
        }
        Minecraft minecraft = initial.getMinecraft();
        if (minecraft.player == null) {
            return initial;
        }
        return !LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(minecraft.player)&&
                EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(minecraft.player)&&
                isDeealthScreen(initial)?null:initial;
    }
    private boolean isDeealthScreen(Screen screen){
        if (screen instanceof DeathScreen) {
            return true;
        }
        if (screen != null) {
            String name = screen.getClass().getName();
            if(name.startsWith("net.minecraft.client.gui.screens.")||
                name.startsWith("net.minecraftforge.client.gui.")){
                return false;
            }
            String lowerCase = name.toLowerCase();
            if(lowerCase.contains("dead")||
                    lowerCase.contains("death")||
                    lowerCase.contains("defeat")||
                    lowerCase.contains("kill")){
                return true;
            }
            for(Renderable renderable : screen.renderables) {
                if (renderable instanceof Button button) {
                    Component message = button.getMessage();
                    if (message.equals(Component.translatable("deathScreen.titleScreen")) || message.equals(Component.translatable("deathScreen.respawn")) || message.equals(Component.translatable("deathScreen.spectate"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    static{
        Class<? extends IScreenHook>[] instanceClass=new Class[1];
        INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
                instanceClass,
                IScreenHook.class,
                MinecraftScreenHook.class,
                new Class<?>[0]
        );
        if(instanceClass[0]!=null){
            ClassReflectionUtil.removeCachedReflectionData(instanceClass[0]);
        }
        ClassReflectionUtil.removeCachedReflectionData(MinecraftScreenHook.class);
    }
}
