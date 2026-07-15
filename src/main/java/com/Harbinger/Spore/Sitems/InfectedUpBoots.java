package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public final class InfectedUpBoots extends UpgradedInfectedExoskeleton {
    public InfectedUpBoots() {
        super(Type.BOOTS);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        UUID uuid = UUIDS.get(slot);
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", calculateTrueDefense(stack, protection[type.getSlot().getIndex()]) + modifyProtection(stack, protection[type.getSlot().getIndex()]), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor modifier", calculateTrueToughness(stack, toughness) + modifyToughness(stack, toughness), AttributeModifier.Operation.ADDITION));
        if (knockback > 0.0F) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", (knockback + modifyKnockbackResistance(stack, knockback)) * 0.1F, AttributeModifier.Operation.ADDITION));
        }
        builder.put(ForgeMod.SWIM_SPEED.get(), new AttributeModifier(uuid, "Armor Speed modifier", getVariant(stack) == SporeArmorMutations.DROWNED ? 0.75 : 0.5, AttributeModifier.Operation.ADDITION));
        if (getVariant(stack) == SporeArmorMutations.REINFORCED || getVariant(stack) == SporeArmorMutations.SKELETAL) {
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "Armor Speed modifier", getVariant(stack) == SporeArmorMutations.REINFORCED ? -0.01 : 0.01, AttributeModifier.Operation.ADDITION));
        }
        return slot == type.getSlot() && tooHurt(stack) ? builder.build() : ImmutableMultimap.of();
    }
}
