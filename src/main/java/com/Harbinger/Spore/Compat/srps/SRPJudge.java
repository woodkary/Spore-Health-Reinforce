package com.Harbinger.Spore.Compat.srps;

import com.Harbinger.Spore.Core.utils.ClassReflectionUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

public final class SRPJudge {
    private SRPJudge(){}
    private static final Field modListIndexedModsField;
    private static volatile boolean visited;
    private static volatile boolean srpLoaded;
    static{
        Field f = null;
        for (Field field : ClassReflectionUtil.getDeclaredFields(ModList.class)) {
            if(!field.getName().equals("indexedMods")){
                continue;
            }
            f = field;
            break;
        }
        modListIndexedModsField = f;
    }
    public static boolean isSRPLikeModLoaded(){
        if(visited){
            return srpLoaded;
        }
        ModList modList = ModList.get();
        if(modList==null||modListIndexedModsField==null||!(ClassUtil.getFieldValue(modListIndexedModsField,modList) instanceof Map indexedMods)){
            return false;
        }
        synchronized (SRPJudge.class){
            for (Object s : indexedMods.keySet()) {
                if(s instanceof String modId&&isSRPNamespace(modId)){
                    srpLoaded = true;
                    visited = true;
                    return true;
                }
            }
            srpLoaded = false;
            visited = true;
            return false;
        }

    }
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
