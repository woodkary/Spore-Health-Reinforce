package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeBaseArmor;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

public class UpgradedInfectedExoskeleton extends SporeBaseArmor implements CustomModelArmorData {
   private static final ResourceLocation location = new ResourceLocation("spore:textures/armor/living_armor_set_mk.png");

   public UpgradedInfectedExoskeleton(Type type) {
      super(type, new int[]{(Integer)SConfig.SERVER.boots_up_durability.get(), (Integer)SConfig.SERVER.pants_up_durability.get(), (Integer)SConfig.SERVER.chestplate_up_durability.get(), (Integer)SConfig.SERVER.helmet_up_durability.get()}, new int[]{(Integer)SConfig.SERVER.boots_up_protection.get(), (Integer)SConfig.SERVER.pants_up_protection.get(), (Integer)SConfig.SERVER.chestplate_up_protection.get(), (Integer)SConfig.SERVER.helmet_protection.get()}, (float)((Integer)SConfig.SERVER.armor_toughness.get() + 2), (float)(Integer)SConfig.SERVER.knockback_resistance.get(), (SoundEvent)Ssounds.INFECTED_GEAR_EQUIP.get(), "Upgraded Living Armor");
   }

   public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
      return "spore:textures/entity/empty.png";
   }

   public void onArmorTick(ItemStack stack, Level level, Player player) {
      this.geteffect(player);
      super.onArmorTick(stack, level, player);
   }

   public void geteffect(LivingEntity entity) {
      if (entity.tickCount % 20 == 0 && !entity.hasEffect((MobEffect)Seffects.SYMBIOSIS.get()) && !entity.hasEffect((MobEffect)Seffects.FROSTBITE.get())) {
         int val = this.getEffectMod(entity);
         if (val != -1) {
            entity.addEffect(new MobEffectInstance((MobEffect)Seffects.SYMBIOSIS.get(), 200, val, false, false));
         }
      }

   }

   private int getEffectMod(LivingEntity living) {
      int i = 0;
      ItemStack helmet = living.getItemBySlot(EquipmentSlot.HEAD);
      ItemStack chest = living.getItemBySlot(EquipmentSlot.CHEST);
      ItemStack legs = living.getItemBySlot(EquipmentSlot.LEGS);
      ItemStack feet = living.getItemBySlot(EquipmentSlot.FEET);
      if (!helmet.equals(ItemStack.EMPTY) && !chest.equals(ItemStack.EMPTY) && !legs.equals(ItemStack.EMPTY) && !feet.equals(ItemStack.EMPTY)) {
         if (helmet.getItem().equals(Sitems.INF_UP_HELMET.get())) {
            i += 2;
         }

         if (chest.getItem().equals(Sitems.INF_UP_CHESTPLATE.get())) {
            i += 2;
         }

         if (legs.getItem().equals(Sitems.INF_UP_PANTS.get())) {
            i += 2;
         }

         if (feet.getItem().equals(Sitems.INF_UP_BOOTS.get())) {
            i += 2;
         }

         if (helmet.getItem().equals(Sitems.INF_HELMET.get())) {
            ++i;
         }

         if (chest.getItem().equals(Sitems.INF_CHEST.get())) {
            ++i;
         }

         if (legs.getItem().equals(Sitems.INF_PANTS.get())) {
            ++i;
         }

         if (feet.getItem().equals(Sitems.INF_BOOTS.get())) {
            ++i;
         }

         return i < 4 ? -1 : (i > 7 ? 1 : 0);
      } else {
         return -1;
      }
   }

   public ResourceLocation getTextureLocation() {
      return location;
   }

   public static class InfectedUpChestplate extends UpgradedInfectedExoskeleton {
      public InfectedUpChestplate() {
         super(Type.CHESTPLATE);
      }

      public static boolean isFlyEnabled(ItemStack stack) {
         return stack.getDamageValue() < stack.getMaxDamage() - 10;
      }

      public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
         return isFlyEnabled(stack);
      }

      public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
         if (!entity.level().isClientSide) {
            int nextFlightTick = flightTicks + 1;
            if (nextFlightTick % 10 == 0) {
               if (nextFlightTick % 20 == 0) {
                  stack.hurtAndBreak(1, entity, (e) -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
                  if (entity instanceof Player) {
                     Player player = (Player)entity;
                     player.causeFoodExhaustion(0.1F);
                  }
               }

               entity.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
         }

         return true;
      }

      public int getEnchantmentValue() {
         return 2;
      }

      public void onArmorTick(ItemStack stack, Level level, Player entity) {
         if (entity.horizontalCollision && entity.isCrouching()) {
            Vec3 initialVec = entity.getDeltaMovement();
            Vec3 climbVec = new Vec3(initialVec.x, 0.2, initialVec.z);
            entity.setDeltaMovement(climbVec.x * 0.91, climbVec.y * 0.98, climbVec.z * 0.91);
         }

         super.onArmorTick(stack, level, entity);
      }

      public void appendHoverText(ItemStack itemStack, @Nullable Level level, List components, TooltipFlag tooltipFlag) {
         if (Screen.hasShiftDown()) {
            components.add(Component.translatable("item.armor.shift").withStyle(ChatFormatting.DARK_RED));
         } else {
            components.add(Component.translatable("item.armor.normal").withStyle(ChatFormatting.GOLD));
         }

         super.appendHoverText(itemStack, level, components, tooltipFlag);
      }
   }

   public static class InfectedUpHelmet extends UpgradedInfectedExoskeleton {
      public InfectedUpHelmet() {
         super(Type.HELMET);
      }

      public void onArmorTick(ItemStack stack, Level level, Player player) {
         super.onArmorTick(stack, level, player);
         if (player.tickCount % 10 == 0 && player.isCrouching()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false));
         }

         if (player.tickCount % 20 == 0 && player.isInWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false));
         }

      }
   }

   public static class InfectedUpPants extends UpgradedInfectedExoskeleton {
      public InfectedUpPants() {
         super(Type.LEGGINGS);
      }

      public Multimap getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
         ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
         UUID uuid = (UUID)this.UUIDS.get(slot);
         builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", this.calculateTrueDefense(stack, (double)this.protection[this.type.getSlot().getIndex()]) + this.modifyProtection(stack, (double)this.protection[this.type.getSlot().getIndex()]), Operation.ADDITION));
         builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor modifier", this.calculateTrueToughness(stack, (double)this.toughness) + this.modifyToughness(stack, (double)this.toughness), Operation.ADDITION));
         if (this.knockback > 0.0F) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", ((double)this.knockback + this.modifyKnockbackResistance(stack, (double)this.knockback)) * (double)0.1F, Operation.ADDITION));
         }

         builder.put((Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get(), new AttributeModifier(uuid, "Armor Step modifier", (double)0.75F, Operation.ADDITION));
         if (this.getVariant(stack) == SporeArmorMutations.DROWNED) {
            builder.put((Attribute)ForgeMod.SWIM_SPEED.get(), new AttributeModifier(uuid, "Armor Speed modifier", (double)0.25F, Operation.ADDITION));
         }

         float speed = this.getVariant(stack) != SporeArmorMutations.REINFORCED && this.getVariant(stack) != SporeArmorMutations.SKELETAL ? 0.0F : (this.getVariant(stack) == SporeArmorMutations.REINFORCED ? -0.01F : 0.01F);
         builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "Armor Speed modifier", 0.02 + (double)speed, Operation.ADDITION));
         return slot == this.type.getSlot() && this.tooHurt(stack) ? builder.build() : ImmutableMultimap.of();
      }

      public void onArmorTick(ItemStack stack, Level level, Player player) {
         super.onArmorTick(stack, level, player);
         if (player.tickCount % 30 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, false, false));
         }

      }
   }

   public static class InfectedUpBoots extends UpgradedInfectedExoskeleton {
      public InfectedUpBoots() {
         super(Type.BOOTS);
      }

      public Multimap getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
         ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
         UUID uuid = (UUID)this.UUIDS.get(slot);
         builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", this.calculateTrueDefense(stack, (double)this.protection[this.type.getSlot().getIndex()]) + this.modifyProtection(stack, (double)this.protection[this.type.getSlot().getIndex()]), Operation.ADDITION));
         builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor modifier", this.calculateTrueToughness(stack, (double)this.toughness) + this.modifyToughness(stack, (double)this.toughness), Operation.ADDITION));
         if (this.knockback > 0.0F) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", ((double)this.knockback + this.modifyKnockbackResistance(stack, (double)this.knockback)) * (double)0.1F, Operation.ADDITION));
         }

         builder.put((Attribute)ForgeMod.SWIM_SPEED.get(), new AttributeModifier(uuid, "Armor Speed modifier", this.getVariant(stack) == SporeArmorMutations.DROWNED ? (double)0.75F : (double)0.5F, Operation.ADDITION));
         if (this.getVariant(stack) == SporeArmorMutations.REINFORCED || this.getVariant(stack) == SporeArmorMutations.SKELETAL) {
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "Armor Speed modifier", this.getVariant(stack) == SporeArmorMutations.REINFORCED ? -0.01 : 0.01, Operation.ADDITION));
         }

         return slot == this.type.getSlot() && this.tooHurt(stack) ? builder.build() : ImmutableMultimap.of();
      }
   }
}
