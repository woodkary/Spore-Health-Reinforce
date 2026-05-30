package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Fluids.BileLiquid;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public class InfectedGreatBow extends BowItem implements SporeWeaponData {
   private final UUID BONUS_DAMAGE_MODIFIER_UUID = UUID.fromString("035e66d6-5a74-402f-b64c-e61432ec39ba");
   private final UUID BONUS_REACH_MODIFIER_UUID = UUID.fromString("d8c35ba5-f440-4335-92b2-3c8b1b703706");
   private final UUID BONUS_RECHARGE_MODIFIER_UUID = UUID.fromString("6dee499d-60f9-4f91-9ae9-fa62f285cc24");

   public InfectedGreatBow(Properties properties) {
      super(properties);
      Sitems.BIOLOGICAL_ITEMS.add(this);
      Sitems.TINTABLE_ITEMS.add(this);
   }

   public boolean isValidRepairItem(ItemStack itemstack, ItemStack repairitem) {
      return Objects.equals(Sitems.BIOMASS.get(), repairitem.getItem());
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int p_40670_) {
      if (living instanceof Player player) {
         if (this.tooHurt(stack)) {
            boolean flag = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack itemstack = player.getProjectile(stack);
            int i = this.getUseDuration(stack) - p_40670_;
            i = ForgeEventFactory.onArrowLoose(stack, level, player, i, !itemstack.isEmpty() || flag);
            if (i < 0) {
               return;
            }

            if (!itemstack.isEmpty() || flag) {
               if (itemstack.isEmpty()) {
                  itemstack = new ItemStack(Items.ARROW);
               }

               float f = getPowerForTime(i);
               if (!((double)f < 0.1)) {
                  boolean flag1 = player.getAbilities().instabuild || itemstack.getItem() instanceof ArrowItem && ((ArrowItem)itemstack.getItem()).isInfinite(itemstack, stack, player);
                  if (!level.isClientSide) {
                     ArrowItem arrowitem = (ArrowItem)(itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                     AbstractArrow abstractarrow = arrowitem.createArrow(level, itemstack, player);
                     abstractarrow = this.customArrow(abstractarrow);
                     abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 4.5F, 1.0F);
                     if (f == 1.0F) {
                        abstractarrow.setCritArrow(true);
                     }

                     abstractarrow.setBaseDamage(this.calculateTrueDamage(stack, abstractarrow.getBaseDamage()) * (Double)SConfig.SERVER.bow_arrow_damage_multiplier.get());
                     int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                     if (j > 0) {
                        abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + (double)j * (double)0.5F + (double)0.5F);
                     }

                     int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                     if (k > 0) {
                        abstractarrow.setKnockback(k);
                     }

                     if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                        abstractarrow.setSecondsOnFire(100);
                     }

                     this.hurtTool(stack, player, 1);
                     if (flag1 || player.getAbilities().instabuild && (itemstack.is(Items.SPECTRAL_ARROW) || itemstack.is(Items.TIPPED_ARROW))) {
                        abstractarrow.pickup = Pickup.CREATIVE_ONLY;
                     }

                     if (abstractarrow instanceof Arrow) {
                        Arrow arrow = (Arrow)abstractarrow;
                        this.abstractEffects(stack, arrow);
                     }

                     level.addFreshEntity(abstractarrow);
                  }

                  level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                  if (!flag1 && !player.getAbilities().instabuild) {
                     itemstack.shrink(1);
                     if (itemstack.isEmpty()) {
                        player.getInventory().removeItem(itemstack);
                     }
                  }

                  player.awardStat(Stats.ITEM_USED.get(this));
               }
            }
         }
      }

   }

   public Multimap getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(this.BONUS_DAMAGE_MODIFIER_UUID, "Tool modifier", this.calculateTrueDamage(stack, (double)(Integer)SConfig.SERVER.bow_melee_damage.get()) + this.modifyDamage(stack, (double)(Integer)SConfig.SERVER.bow_melee_damage.get()), Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(this.BONUS_RECHARGE_MODIFIER_UUID, "Tool modifier", -2.4 + this.modifyRecharge(stack), Operation.ADDITION));
      builder.put((Attribute)ForgeMod.ENTITY_REACH.get(), new AttributeModifier(this.BONUS_REACH_MODIFIER_UUID, "Tool modifier", (double)1.0F + this.modifyRange(stack), Operation.ADDITION));
      return slot == EquipmentSlot.MAINHAND && this.tooHurt(stack) ? builder.build() : ImmutableMultimap.of();
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(Enchantments.SHARPNESS, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING).contains(enchantment);
   }

   public void abstractEffects(ItemStack stack, Arrow arrow) {
      if (stack.getEnchantmentLevel((Enchantment)Senchantments.CORROSIVE_POTENCY.get()) > 0) {
         arrow.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, 1));
      }

      if (stack.getEnchantmentLevel((Enchantment)Senchantments.GASTRIC_SPEWAGE.get()) > 0) {
         for(MobEffectInstance instance : BileLiquid.bileEffects()) {
            arrow.addEffect(instance);
         }
      }

      if (this.getVariant(stack) == SporeToolsMutations.ROTTEN) {
         arrow.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
      }

      if (this.getVariant(stack) == SporeToolsMutations.TOXIC) {
         arrow.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
      }

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

         components.add(Component.translatable("spore.item.desc.bow").withStyle(ChatFormatting.RED));
      } else {
         components.add(Component.translatable("item.armor.normal").withStyle(ChatFormatting.GOLD));
      }

   }

   public boolean isBarVisible(ItemStack stack) {
      return super.isBarVisible(stack) || this.getAdditionalDurability(stack) > 0;
   }

   public int getBarColor(ItemStack stack) {
      return this.getAdditionalDurability(stack) > 0 ? Mth.hsvToRgb(240.0F, 100.0F, 100.0F) : super.getBarColor(stack);
   }

   public int getEnchantmentValue(ItemStack stack) {
      int luck = this.getLuck(stack);
      return luck > 0 ? luck : 1;
   }

   public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
      if (!this.tooHurt(stack) && entity instanceof Player player) {
         player.getCooldowns().addCooldown(this, 60);
      }

      return false;
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity living, LivingEntity entity) {
      if (this.tooHurt(stack)) {
         this.hurtTool(stack, entity, 1);
      }

      this.doEntityHurtAfterEffects(stack, living, entity);
      return super.hurtEnemy(stack, living, entity);
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
