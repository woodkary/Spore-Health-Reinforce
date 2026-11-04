package com.Harbinger.Spore.Compat.srps;

import com.Harbinger.Spore.Core.utils.SporeJudge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Locale;

public final class SRPJudge {
    private SRPJudge(){}
    public static boolean isSRPEntity(Entity entity){
        return !SporeJudge.isSporeEntity(entity)&&isSRPLikeEntity(entity);
    }
    public static boolean isSRPLikeEntity(Entity entity){
        String lowerModId = EntityType.getKey(entity.getType()).getNamespace().toLowerCase(Locale.ROOT);
        return lowerModId.contains("srp")||
                lowerModId.contains("scape")&&lowerModId.contains("run")&&lowerModId.contains("parasite");
    }
}
