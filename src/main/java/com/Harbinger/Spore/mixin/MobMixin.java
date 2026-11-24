package com.Harbinger.Spore.mixin;

import com.Harbinger.Spore.Core.asmHooks.MobGetTargetHook;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public final class MobMixin {
    @Unique
    private final Mob spore$Self=(Mob) (Object)this;
    @Inject(method="addAdditionalSaveData",at=@At("RETURN"))
    public void addAdditionalTarrgetDalta(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putBoolean("spore:selfTarrget", MobGetTargetHook.INSTANCE.isSelfTarrget(spore$Self));
    }
    @Inject(method="readAdditionalSaveData",at=@At("RETURN"))
    public void readAdditionalTarrgetDalta(CompoundTag compoundTag, CallbackInfo ci){
        if(compoundTag.getBoolean("spore:selfTarrget")){
            MobGetTargetHook.INSTANCE.putSelfTarrget(spore$Self);
        }
    }
}
