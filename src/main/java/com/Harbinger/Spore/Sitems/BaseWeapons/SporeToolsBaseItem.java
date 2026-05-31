package com.Harbinger.Spore.Sitems.BaseWeapons;

import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseItem;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.Nullable;

public class SporeToolsBaseItem extends BaseItem implements IForgeItem, SporeWeaponData {
   protected final double meleeDamage;
   protected final double meleeReach;
   protected final double meleeRecharge;
   protected final int miningLevel;
   protected final String desc;
   protected final UUID BONUS_DAMAGE_MODIFIER_UUID = UUID.fromString("035e66d6-5a74-402f-b64c-e61432ec39ba");
   protected final UUID BONUS_REACH_MODIFIER_UUID = UUID.fromString("d8c35ba5-f440-4335-92b2-3c8b1b703706");
   protected final UUID BONUS_RECHARGE_MODIFIER_UUID = UUID.fromString("6dee499d-60f9-4f91-9ae9-fa62f285cc24");

   public SporeToolsBaseItem(double meleeDamage, double meleeReach, double meleeRecharge, int durability, int miningLevel, String desc) {
      super((new Properties()).stacksTo(1).durability(durability));
      this.meleeDamage = meleeDamage;
      this.meleeReach = meleeReach;
      this.meleeRecharge = meleeRecharge;
      this.miningLevel = miningLevel;
      this.desc = desc;
      Sitems.TINTABLE_ITEMS.add(this);
   }

   public boolean isValidRepairItem(ItemStack stack, ItemStack itemStack) {
      return super.isValidRepairItem(stack, itemStack) || itemStack.getItem() == Sitems.BIOMASS.get();
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(this.BONUS_DAMAGE_MODIFIER_UUID, "Tool modifier", this.calculateTrueDamage(stack, this.meleeDamage) + this.modifyDamage(stack, this.meleeDamage), Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(this.BONUS_RECHARGE_MODIFIER_UUID, "Tool modifier", -this.meleeRecharge + this.modifyRecharge(stack), Operation.ADDITION));
      builder.put((Attribute)ForgeMod.ENTITY_REACH.get(), new AttributeModifier(this.BONUS_REACH_MODIFIER_UUID, "Tool modifier", this.meleeReach + this.modifyRange(stack), Operation.ADDITION));
      return slot == EquipmentSlot.MAINHAND && this.tooHurt(stack) ? builder.build() : ImmutableMultimap.of();
   }

   public float getDestroySpeed(ItemStack stack, BlockState blockState) {
      return this.tooHurt(stack) ? (float)this.miningLevel : 1.0F;
   }

   public int damageItem(ItemStack stack, int amount, LivingEntity entity, Consumer onBroken) {
      int durabilityLeft = stack.getMaxDamage() - stack.getDamageValue();
      if (durabilityLeft - amount <= 11) {
         entity.playSound((SoundEvent)Ssounds.INFECTED_GEAR_BREAK.get());
      }

      return durabilityLeft < 10 ? 0 : super.damageItem(stack, amount, entity, onBroken);
   }

   public boolean isEnchantable(ItemStack p_41456_) {
      return true;
   }

   public int getEnchantmentValue(ItemStack stack) {
      int luck = this.getLuck(stack);
      return luck > 0 ? luck : 1;
   }

   public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
      if (!this.tooHurt(stack) && entity instanceof Player player) {
         player.getCooldowns().addCooldown(this, 60);
      }

      return doASMRangeHurtOnSwing(stack,entity);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity living, LivingEntity entity) {
      if (this.tooHurt(stack)) {
         this.hurtTool(stack, entity, 1);
      }

      this.doEntityHurtAfterEffects(stack, living, entity);
      return super.hurtEnemy(stack, living, entity);
   }

   public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity living) {
      return !this.tooHurt(stack) ? false : super.mineBlock(stack, level, state, pos, living);
   }

   public boolean isBarVisible(ItemStack stack) {
      return super.isBarVisible(stack) || this.getAdditionalDurability(stack) > 0;
   }

   public int getBarColor(ItemStack stack) {
      return this.getAdditionalDurability(stack) > 0 ? Mth.hsvToRgb(240.0F, 100.0F, 100.0F) : super.getBarColor(stack);
   }

   public void appendHoverText(ItemStack stack, @Nullable Level p_41422_, List components, TooltipFlag p_41424_) {
      super.appendHoverText(stack, p_41422_, components, p_41424_);
      if (!this.tooHurt(stack)) {
         components.add(Component.translatable("spore.item.hurt").withStyle(ChatFormatting.RED));
      }

      if (Screen.hasShiftDown()) {
         if (this.getAdditionalDamage(stack) > (double)0.0F) {
            String var10001 = Component.translatable("spore.item.damage_increase").getString();
            components.add(Component.literal(var10001 + this.getAdditionalDamage(stack) + "%"));
         }

         if (this.getMaxAdditionalDurability(stack) > 0) {
            String var5 = Component.translatable("spore.item.durability_increase").getString();
            components.add(Component.literal(var5 + this.getMaxAdditionalDurability(stack) + "%"));
         }

         if (this.getAdditionalDurability(stack) > 0) {
            String var6 = Component.translatable("spore.item.additional_durability").getString();
            components.add(Component.literal(var6 + this.getAdditionalDurability(stack)));
         }

         if (this.getEnchantmentValue(stack) > 1) {
            String var7 = Component.translatable("spore.item.enchant").getString();
            components.add(Component.literal(var7 + this.getEnchantmentValue(stack)));
         }

         if (this.getVariant(stack) != SporeToolsMutations.DEFAULT) {
            String var8 = Component.translatable("spore.item.mutation").getString();
            components.add(Component.literal(var8 + Component.translatable(this.getVariant(stack).getName()).getString()));
         }

         components.add(Component.translatable("spore.item.desc." + this.desc).withStyle(this.getDesColor()));
      } else {
         components.add(Component.translatable("item.armor.normal").withStyle(ChatFormatting.GOLD));
      }

   }

   protected ChatFormatting getDesColor() {
      return ChatFormatting.RED;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(Enchantments.SHARPNESS, Enchantments.FIRE_ASPECT, Enchantments.KNOCKBACK, Enchantments.MOB_LOOTING, Enchantments.SMITE).contains(enchantment);
   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack itemStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
      if (clickAction == ClickAction.SECONDARY && stack.getEnchantmentLevel((Enchantment)Senchantments.VORACIOUS_MAW.get()) > 0 && stack.getDamageValue() > 0) {
         if (!itemStack.isEdible()) {
            return false;
         }

         FoodProperties properties = itemStack.getItem().getFoodProperties(itemStack, (LivingEntity)null);
         if (properties != null && properties.isMeat()) {
            stack.setDamageValue(this.getDamage(stack) - 50);
            itemStack.shrink(1);
            player.playNotifySound(SoundEvents.GENERIC_EAT, SoundSource.AMBIENT, 1.0F, 1.0F);
            return true;
         }
      }

      boolean shouldOverride = clickAction == ClickAction.SECONDARY && itemStack.getItem() == Sitems.SYRINGE.get() && this.getVariant(stack) != SporeToolsMutations.DEFAULT;
      if (shouldOverride) {
         this.setVariant(SporeToolsMutations.DEFAULT, stack);
         itemStack.shrink(1);
         player.playNotifySound((SoundEvent)Ssounds.SYRINGE_SUCK.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
      }

      return shouldOverride;
   }
}
