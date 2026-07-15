package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public final class InfectedUpPants extends UpgradedInfectedExoskeleton {
    public InfectedUpPants() {
        super(Type.LEGGINGS);
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
        builder.put(ForgeMod.STEP_HEIGHT_ADDITION.get(), new AttributeModifier(uuid, "Armor Step modifier", 0.75, AttributeModifier.Operation.ADDITION));
        if (getVariant(stack) == SporeArmorMutations.DROWNED) {
            builder.put(ForgeMod.SWIM_SPEED.get(), new AttributeModifier(uuid, "Armor Speed modifier", 0.25, AttributeModifier.Operation.ADDITION));
        }
        float speed = getVariant(stack) == SporeArmorMutations.REINFORCED ? -0.01F
                : getVariant(stack) == SporeArmorMutations.SKELETAL ? 0.01F : 0.0F;
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "Armor Speed modifier", 0.02F + speed, AttributeModifier.Operation.ADDITION));
        return slot == type.getSlot() && tooHurt(stack) ? builder.build() : ImmutableMultimap.of();
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        super.onArmorTick(stack, level, player);
        if (player.tickCount % 30 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, false, false));
        }
    }
}
