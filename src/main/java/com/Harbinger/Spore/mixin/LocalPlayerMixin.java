package com.Harbinger.Spore.mixin;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Unique
    LocalPlayer spore$localPlayer =(LocalPlayer)(Object)this;
    @Inject(method = "respawn",at=@At("HEAD"))
    public void removeDeafgthTags(CallbackInfo ci){
        EntityHeealuthManager.INSTANCE.setPlayerAlliive(spore$localPlayer);
    }
    @Inject(method="removeEffectNoUpdate",at=@At("RETURN"))
    public void removeISporeMapEffectNoUpdate(MobEffect effect, CallbackInfoReturnable<MobEffectInstance> cir){
        if(!(spore$localPlayer.activeEffects instanceof ISporeMap<MobEffect, MobEffectInstance> sporeMap)){
            return;
        }
        sporeMap.actualRemove(effect);
    }
}
