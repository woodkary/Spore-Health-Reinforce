package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public final class ReaverTier implements Tier {
    @Override
    public int getUses() {
        return SConfig.SERVER.reaver_durability.get();
    }

    @Override
    public float getSpeed() {
        return -2;
    }

    @Override
    public float getAttackDamageBonus() {
        return SConfig.SERVER.reaver_damage.get() -1;
    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    public int getEnchantmentValue() {
        return 3;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Sitems.COMPOUND_PLATE.get());
    }
}
