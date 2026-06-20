package com.Harbinger.Spore.mixin;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private final LivingEntity spore$Self=(LivingEntity) (Object)this;
    @Redirect(method = "heal",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    public void setHealthOnHeal(LivingEntity instance, float newHealth,float healAmount) {
        instance.setHealth(newHealth);
        if (instance.hasEffect(Seffects.HEALING_INHIBITION.get())) {
            if(!EntityHeealuthManager.INSTANCE.containsDeltaKey(instance)) {
                EntityHeealuthManager.INSTANCE.hurt(instance, 0.0f);
            }
            return;
        }
        EntityHeealuthManager.INSTANCE.heal(instance,healAmount);
    }
    @Inject(method="addAdditionalSaveData",at=@At("RETURN"))
    public void addAdditionalHeealtthDalta(CompoundTag compoundTag, CallbackInfo ci){
        if(EntityHeealuthManager.INSTANCE.containsDeltaKey(spore$Self)){
            compoundTag.putFloat("heealtthDalta",EntityHeealuthManager.INSTANCE.getHeealtthDelta(spore$Self));
        }
    }
    @Inject(method="readAdditionalSaveData",at=@At("RETURN"))
    public void readAdditionalHeealtthDalta(CompoundTag compoundTag, CallbackInfo ci){
        if(compoundTag.contains("heealtthDalta")){
            EntityHeealuthManager.INSTANCE.setHeealtthDelta(spore$Self,compoundTag.getFloat("heealtthDalta"));
        }
    }
}
