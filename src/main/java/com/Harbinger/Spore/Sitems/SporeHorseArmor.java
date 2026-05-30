package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;

public abstract class SporeHorseArmor extends HorseArmorItem implements SporeArmorData {
   public final UUID HORSE_ARMOR_UUID = UUID.fromString("5c00d05d-5002-42e9-96f1-d758c065075f");
   private final int protection;

   public SporeHorseArmor(int armorValue) {
      super(0, new ResourceLocation("spore:textures/entity/empty.png"), (new Properties()).stacksTo(1));
      this.protection = armorValue;
      Sitems.TINTABLE_ITEMS.add(this);
      Sitems.BIOLOGICAL_ITEMS.add(this);
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      double baseArmor = this.calculateTrueDefense(stack, (double)this.protection) + this.modifyProtection(stack, (double)this.protection);
      double baseToughness = this.calculateTrueToughness(stack, (double)0.0F) + this.modifyToughness(stack, (double)0.0F);
      builder.put(Attributes.ARMOR, new AttributeModifier(this.HORSE_ARMOR_UUID, "Armor modifier", baseArmor, Operation.ADDITION));
      builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(this.HORSE_ARMOR_UUID, "Armor modifier", baseToughness, Operation.ADDITION));
      if (this.getVariant(stack) == SporeArmorMutations.DROWNED) {
         builder.put((Attribute)ForgeMod.SWIM_SPEED.get(), new AttributeModifier(this.HORSE_ARMOR_UUID, "Armor Speed modifier", (double)0.25F, Operation.ADDITION));
      }

      if (this.getVariant(stack) == SporeArmorMutations.REINFORCED || this.getVariant(stack) == SporeArmorMutations.SKELETAL) {
         builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(this.HORSE_ARMOR_UUID, "Armor Speed modifier", this.getVariant(stack) == SporeArmorMutations.REINFORCED ? -0.01 : 0.01, Operation.ADDITION));
      }

      return slot == EquipmentSlot.CHEST ? builder.build() : ImmutableMultimap.of();
   }

   public double modifyProtection(ItemStack stack, double value) {
      if (this.getVariant(stack) == SporeArmorMutations.REINFORCED) {
         return value * (double)0.2F;
      } else {
         return this.getVariant(stack) == SporeArmorMutations.SKELETAL ? value * (double)-0.2F : (double)0.0F;
      }
   }

   public double modifyToughness(ItemStack stack, double value) {
      return this.getVariant(stack) == SporeArmorMutations.SKELETAL ? (double)1.0F : (double)0.0F;
   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack itemStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
      boolean shouldOverride = clickAction == ClickAction.SECONDARY && itemStack.getItem() == Sitems.SYRINGE.get() && this.getVariant(stack) != SporeArmorMutations.DEFAULT;
      if (shouldOverride) {
         this.setVariant(SporeArmorMutations.DEFAULT, stack);
         itemStack.shrink(1);
         player.playNotifySound((SoundEvent)Ssounds.SYRINGE_SUCK.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
      }

      return shouldOverride;
   }
}
