package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeSwordBase;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;

public class InfectedGreatSword extends SporeSwordBase {
   private final UUID BONUS_ARMOR_MODIFIER_UUID = UUID.fromString("6d8794d2-25bc-41ad-8da7-1d2ce0818d75");
   private final UUID BONUS_TOUGHNESS_MODIFIER_UUID = UUID.fromString("69df05bf-4f50-47eb-9108-a55dc2673144");

   public InfectedGreatSword() {
      super((double)(Integer)SConfig.SERVER.greatsword_damage.get(), (double)2.5F, (double)3.0F, (Integer)SConfig.SERVER.greatsword_durability.get(), "greatsword");
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ARMOR, new AttributeModifier(this.BONUS_ARMOR_MODIFIER_UUID, "Tool modifier", (double)(Integer)SConfig.SERVER.greatsword_armor.get(), Operation.ADDITION));
      builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(this.BONUS_TOUGHNESS_MODIFIER_UUID, "Tool modifier", (double)(Integer)SConfig.SERVER.greatsword_toughness.get(), Operation.ADDITION));
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(this.BONUS_DAMAGE_MODIFIER_UUID, "Tool modifier", this.calculateTrueDamage(stack, this.meleeDamage) + this.modifyDamage(stack, this.meleeDamage), Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(this.BONUS_RECHARGE_MODIFIER_UUID, "Tool modifier", -this.meleeRecharge + this.modifyRecharge(stack), Operation.ADDITION));
      builder.put((Attribute)ForgeMod.ENTITY_REACH.get(), new AttributeModifier(this.BONUS_REACH_MODIFIER_UUID, "Tool modifier", this.meleeReach + this.modifyRange(stack), Operation.ADDITION));
      return slot == EquipmentSlot.MAINHAND && this.tooHurt(stack) ? builder.build() : ImmutableMultimap.of();
   }
}
