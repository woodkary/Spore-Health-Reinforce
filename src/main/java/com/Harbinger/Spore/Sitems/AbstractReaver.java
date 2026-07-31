package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Sitems.BaseWeapons.LootModifierWeapon;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public abstract class AbstractReaver extends SwordItem implements LootModifierWeapon {
    public AbstractReaver(Tier p_43269_, int p_43270_, float p_43271_, Properties p_43272_) {
        super(p_43269_, p_43270_, p_43271_, p_43272_);
    }
}
