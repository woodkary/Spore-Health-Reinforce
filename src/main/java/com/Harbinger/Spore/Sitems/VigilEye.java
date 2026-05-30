package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class VigilEye extends BaseItem {
   public VigilEye() {
      super((new Properties()).stacksTo(1).durability(1).rarity(Rarity.RARE));
   }

   public int getUseDuration(ItemStack p_40680_) {
      return 72000;
   }

   public UseAnim getUseAnimation(ItemStack p_40678_) {
      return UseAnim.BOW;
   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(itemstack);
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int value) {
      super.releaseUsing(stack, level, livingEntity, value);
      livingEntity.playSound((SoundEvent)Ssounds.VIGIL_EYE_USE.get());
      if (!level.isClientSide) {
         AABB searchbox = livingEntity.getBoundingBox().inflate((double)128.0F);

         for(Entity entity : level.getEntities(livingEntity, searchbox)) {
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               if (living instanceof Infected || living instanceof Organoid || living instanceof Calamity) {
                  living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));
               }
            }
         }

         stack.hurtAndBreak(1, livingEntity, (ss) -> ss.broadcastBreakEvent(livingEntity.getUsedItemHand()));
      }

   }
}
