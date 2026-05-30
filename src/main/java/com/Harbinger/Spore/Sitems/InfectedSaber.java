package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeSwordBase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class InfectedSaber extends SporeSwordBase {
   public InfectedSaber() {
      super((double)(Integer)SConfig.SERVER.saber_damage.get(), (double)1.5F, (double)2.4F, (Integer)SConfig.SERVER.saber_durability.get(), "saber");
   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      if (player instanceof ServerPlayer serverPlayer) {
         this.leap(serverPlayer);
         this.hurtTool(player.getItemInHand(hand), serverPlayer, 1);
         return InteractionResultHolder.success(player.getItemInHand(hand));
      } else {
         return super.use(level, player, hand);
      }
   }

   public void leap(Player player) {
      player.playNotifySound((SoundEvent)Ssounds.SABER_LEAP.get(), SoundSource.AMBIENT, 2.0F, 1.0F);
      player.hurtMarked = true;
      player.knockback((double)2.5F, -player.getLookAngle().x, -player.getLookAngle().z);
      player.getCooldowns().addCooldown(this, 40);
   }
}
