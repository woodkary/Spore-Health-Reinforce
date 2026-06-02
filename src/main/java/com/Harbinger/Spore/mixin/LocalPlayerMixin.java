package com.Harbinger.Spore.mixin;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Unique
    LocalPlayer spore$localPlayer =(LocalPlayer)(Object)this;
    @Inject(method = "respawn",at=@At("HEAD"))
    public void removeDeafgthTags(CallbackInfo ci){
        EntityHeealuthManager.INSTANCE.setPlayerAlliive(spore$localPlayer);
    }
}
