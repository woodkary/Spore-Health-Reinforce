package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Projectile.ThrownSpear;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeSwordBase;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class InfectedSpearItem extends SporeSwordBase {
   public InfectedSpearItem() {
      super((double)(Integer)SConfig.SERVER.spear_damage.get(), (double)2.5F, (double)3.0F, (Integer)SConfig.SERVER.spear_durability.get(), "spear");
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.SPEAR;
   }

   public int getUseDuration(ItemStack stack) {
      return 72000;
   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (!this.tooHurt(itemstack)) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         player.startUsingItem(hand);
         return InteractionResultHolder.consume(itemstack);
      }
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int T) {
      if (entity instanceof Player player) {
         int i = this.getUseDuration(stack) - T;
         if (i >= 10 && !level.isClientSide) {
            stack.hurtAndBreak(1, player, (ss) -> ss.broadcastBreakEvent(entity.getUsedItemHand()));
            ThrownSpear thrownSpear = new ThrownSpear(level, player, stack, this.getVariant(stack).getColor());
            thrownSpear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            if (player.getAbilities().instabuild) {
               thrownSpear.pickup = Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(thrownSpear);
            level.playSound((Player)null, thrownSpear, (SoundEvent)Ssounds.INFECTED_WEAPON_THROW.get(), SoundSource.PLAYERS, 0.5F, 1.1F);
            if (!player.getAbilities().instabuild) {
               player.getInventory().removeItem(stack);
            }

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

   public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
      this.hurtTool(stack, entity, 1);
      return true;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(Enchantments.LOYALTY).contains(enchantment);
   }
}
