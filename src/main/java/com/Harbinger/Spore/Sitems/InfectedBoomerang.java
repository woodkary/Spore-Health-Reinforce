package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Projectile.ThrownBoomerang;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeSwordBase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class InfectedBoomerang extends SporeSwordBase {
   public InfectedBoomerang() {
      super((double)(Integer)SConfig.SERVER.boomerang_damage.get(), (double)1.0F, (double)3.0F, (Integer)SConfig.SERVER.boomerang_durability.get(), "boomerang");
   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player instanceof ServerPlayer && !level.isClientSide) {
         stack.hurtAndBreak(1, player, (ss) -> ss.broadcastBreakEvent(player.getUsedItemHand()));
         ThrownBoomerang thrownSpear = new ThrownBoomerang(level, player, stack, this.getVariant(stack).getColor());
         thrownSpear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.0F, 0.75F);
         if (player.getAbilities().instabuild) {
            thrownSpear.pickup = Pickup.CREATIVE_ONLY;
         }

         level.addFreshEntity(thrownSpear);
         level.playSound((Player)null, thrownSpear, (SoundEvent)Ssounds.INFECTED_WEAPON_THROW.get(), SoundSource.PLAYERS, 1.5F, 0.9F);
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

         player.awardStat(Stats.ITEM_USED.get(this));
         return InteractionResultHolder.success(player.getItemInHand(hand));
      } else {
         return super.use(level, player, hand);
      }
   }
}
