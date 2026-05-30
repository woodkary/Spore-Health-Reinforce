package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Projectile.ThrownSickle;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeSwordBase;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class InfectedSickle extends SporeSwordBase {
   private static final String SICKLE_THROWN = "sickle_thrown";
   private static final String THROWN = "thrown";

   public InfectedSickle() {
      super((double)(Integer)SConfig.SERVER.sickle_damage.get(), (double)2.0F, (double)2.0F, (Integer)SConfig.SERVER.sickle_durability.get(), "sickle");
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity living, LivingEntity entity) {
      return this.getThrownSickle(stack) ? false : super.hurtEnemy(stack, living, entity);
   }

   public float getDestroySpeed(ItemStack stack, BlockState state) {
      return state.is(BlockTags.MINEABLE_WITH_HOE) ? 2.0F : 1.0F;
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      return (Multimap)(this.getThrownSickle(stack) ? ImmutableMultimap.of() : super.getAttributeModifiers(slot, stack));
   }

   public boolean reversedKnockback() {
      return true;
   }

   public void setThrownSickle(ItemStack stack, boolean value) {
      CompoundTag tag = stack.getOrCreateTagElement("sickle_thrown");
      tag.putBoolean("thrown", value);
   }

   public boolean getThrownSickle(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("sickle_thrown");
      return tag.getBoolean("thrown");
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.SPEAR;
   }

   public int getUseDuration(ItemStack stack) {
      return 72000;
   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResultHolder.pass(player.getItemInHand(hand));
      } else {
         ItemStack itemstack = player.getMainHandItem();
         if (!level.isClientSide) {
            List<ThrownSickle> projectiles = level.getEntitiesOfClass(ThrownSickle.class, player.getBoundingBox().inflate((double)32.0F), (s) -> s.getOwner() == player && !s.isRemoved());
            this.setThrownSickle(itemstack, false);
            if (projectiles.isEmpty()) {
               player.startUsingItem(hand);
               return InteractionResultHolder.success(itemstack);
            }

            ThrownSickle sickle = (ThrownSickle)projectiles.get(0);
            if (sickle.getHookState() == ThrownSickle.SickelState.HOOKED_IN_ENTITY && sickle.getHookedEntity() != null) {
               label28: {
                  Entity hooked = sickle.getHookedEntity();
                  if (hooked instanceof LivingEntity) {
                     LivingEntity le = (LivingEntity)hooked;
                     if (le.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue() > (double)0.5F) {
                        this.pullEntityToward(player, hooked.position());
                        break label28;
                     }
                  }

                  this.pullEntityToward(hooked, player.position());
               }
            } else if (sickle.getHookState() == ThrownSickle.SickelState.HOOKED_BLOCK && sickle.getHookedBlockPos() != null) {
               this.pullEntityToward(player, sickle.getHookedBlockPos());
            }

            sickle.discard();
         }

         return InteractionResultHolder.pass(itemstack);
      }
   }

   private void pullEntityToward(Entity toMove, Vec3 targetPos) {
      Vec3 direction = targetPos.subtract(toMove.position()).normalize();
      double strength = (double)4.0F;
      Vec3 velocity = direction.multiply(strength, strength / (double)2.0F, strength);
      toMove.setDeltaMovement(velocity);
      toMove.hurtMarked = true;
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int T) {
      if (entity instanceof Player player) {
         if (!this.getThrownSickle(stack)) {
            int i = this.getUseDuration(stack) - T;
            if (i >= 10 && !level.isClientSide) {
               stack.hurtAndBreak(1, player, (ss) -> ss.broadcastBreakEvent(entity.getUsedItemHand()));
               ThrownSickle thrownSpear = new ThrownSickle(level, player, stack, this.getVariant(stack).getColor());
               thrownSpear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 0.75F);
               level.addFreshEntity(thrownSpear);
               this.setThrownSickle(stack, true);
               level.playSound((Player)null, thrownSpear, (SoundEvent)Ssounds.INFECTED_WEAPON_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
               int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack);
               if (j > 0) {
                  thrownSpear.setBaseDamage(thrownSpear.getBaseDamage() + (double)j * (double)0.5F + (double)0.5F);
               }

               int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack);
               if (k > 0) {
                  thrownSpear.setKnockback(k);
               }
            }

            player.awardStat(Stats.ITEM_USED.get(this));
         }
      }

   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return super.canApplyAtEnchantingTable(stack, enchantment) && !ImmutableSet.of(Enchantments.KNOCKBACK).contains(enchantment);
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction);
   }
}
