package com.Harbinger.Spore.Compat.l2Hostility;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;

import java.util.Locale;
import java.util.Map;

public final class L2HostilityMobTraits implements IMobTraits {
    public static final IMobTraits INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IMobTraits.class,
            L2HostilityMobTraits.class
    );
    private Map<MobTrait, Integer> getMobTraits(LivingEntity liv){
        if(!ModList.get().isLoaded("l2hostility")){
            return Map.of();
        }
        LazyOptional<MobTraitCap> opt = liv.getCapability(MobTraitCap.CAPABILITY);
        if (opt.resolve().isEmpty()) {
            return Map.of();
        }
        MobTraitCap cap = opt.resolve().get();
        return cap.traits;
    }
    public int getTraitLevel(LivingEntity liv, String traitName){
        if(!ModList.get().isLoaded("l2hostility")){
            return 0;
        }
        traitName=traitName.toLowerCase(Locale.ROOT);
        for (Map.Entry<MobTrait, Integer> entry : getMobTraits(liv).entrySet()) {
            if (entry.getKey().getID().toLowerCase(Locale.ROOT).contains(traitName)) {
                return entry.getValue();
            }
        }
        return 0;
    }
}
