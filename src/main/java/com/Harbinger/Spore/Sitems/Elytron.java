package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeBaseArmor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class Elytron extends SporeBaseArmor implements CustomModelArmorData{
    private final ResourceLocation TEXTURE = new ResourceLocation("spore:textures/armor/elytron.png");
    public Elytron(Type type) {
        super(type, new int[]{0, 0, SConfig.SERVER.ely_durability.get(),0},new int[]{0, 0, SConfig.SERVER.ely_protection.get(), 0}, SConfig.SERVER.ely_toughness.get(), SConfig.SERVER.ely_knockback_resistance.get() /10F, Ssounds.INFECTED_GEAR_EQUIP.get(), "Elytron");
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
