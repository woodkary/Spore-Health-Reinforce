package com.Harbinger.Spore.Compat.srps;

import com.Harbinger.Spore.Core.utils.SporeJudge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Locale;

public final class SRPJudge {
    private SRPJudge(){}
    public static boolean isSRPEntity(Entity entity){
        return !SporeJudge.isSporeEntity(entity)&&isSRPLikeEntity(entity);
    }
    public static boolean isSRPLikeEntity(Entity entity){
        if(entity==null) return false;
        return isSRPNamespace(EntityType.getKey(entity.getType()).getNamespace());
    }
    public static boolean isSRPEffect(MobEffect effect){
        ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(effect);
        if(key==null) return false;
        return isSRPNamespace(key.getNamespace());
    }
    private static boolean isSRPNamespace(String namespace){
        String lower=namespace.toLowerCase(Locale.ROOT);
        return lower.contains("srp")||
                lower.contains("scape")&&lower.contains("run")&&lower.contains("parasite");
    }
}
