package com.Harbinger.Spore.sEvents;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Sitems.InfectedCleaver;
import com.Harbinger.Spore.Sitems.InfectedShield;
import com.Harbinger.Spore.Sitems.InfectedSickle;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class SItemProperties {
   public static void addCustomItemProperties() {
      makeBow((Item)Sitems.INFECTED_BOW.get());
      makeTrident((Item)Sitems.INFECTED_SPEAR.get());
      makeCrossbow((Item)Sitems.INFECTED_CROSSBOW.get());
      makeDecayedLimbs((Item)Sitems.DECAYED_LIMBS.get());
      makeSickle((Item)Sitems.SICKLE.get());
      makeCleaver((Item)Sitems.CLEAVER.get());
      makeShield((Item)Sitems.SHIELD.get());
   }

   private static void makeBow(Item item) {
      ItemProperties.register(item, new ResourceLocation("pull"), (p_174635_, p_174636_, p_174637_, p_174638_) -> {
         if (p_174637_ == null) {
            return 0.0F;
         } else {
            return p_174637_.getUseItem() != p_174635_ ? 0.0F : (float)(p_174635_.getUseDuration() - p_174637_.getUseItemRemainingTicks()) / 20.0F;
         }
      });
      ItemProperties.register(item, new ResourceLocation("pulling"), (p_174630_, p_174631_, p_174632_, p_174633_) -> p_174632_ != null && p_174632_.isUsingItem() && p_174632_.getUseItem() == p_174630_ ? 1.0F : 0.0F);
   }

   private static void makeTrident(Item item) {
      ItemProperties.register(item, new ResourceLocation("throw"), (p_174635_, p_174636_, p_174637_, p_174638_) -> {
         if (p_174637_ == null) {
            return 0.0F;
         } else {
            return p_174637_.getUseItem() != p_174635_ ? 0.0F : (float)(p_174635_.getUseDuration() - p_174637_.getUseItemRemainingTicks()) / 20.0F;
         }
      });
      ItemProperties.register(item, new ResourceLocation("throwing"), (p_174585_, p_174586_, p_174587_, p_174588_) -> p_174587_ != null && p_174587_.isUsingItem() && p_174587_.getUseItem() == p_174585_ ? 1.0F : 0.0F);
   }

   private static void makeCrossbow(Item item) {
      ItemProperties.register(item, new ResourceLocation("pull"), (p_174620_, p_174621_, p_174622_, p_174623_) -> {
         if (p_174622_ == null) {
            return 0.0F;
         } else {
            return CrossbowItem.isCharged(p_174620_) ? 0.0F : (float)(p_174620_.getUseDuration() - p_174622_.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(p_174620_);
         }
      });
      ItemProperties.register(item, new ResourceLocation("pulling"), (p_174615_, p_174616_, p_174617_, p_174618_) -> p_174617_ != null && p_174617_.isUsingItem() && p_174617_.getUseItem() == p_174615_ && !CrossbowItem.isCharged(p_174615_) ? 1.0F : 0.0F);
      ItemProperties.register(item, new ResourceLocation("charged"), (p_174610_, p_174611_, p_174612_, p_174613_) -> p_174612_ != null && CrossbowItem.isCharged(p_174610_) ? 1.0F : 0.0F);
      ItemProperties.register(item, new ResourceLocation("firework"), (p_174605_, p_174606_, p_174607_, p_174608_) -> p_174607_ != null && CrossbowItem.isCharged(p_174605_) && CrossbowItem.containsChargedProjectile(p_174605_, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);
   }

   private static void makeDecayedLimbs(Item item) {
      ItemProperties.register(item, new ResourceLocation("decayed"), (p_174585_, p_174586_, p_174587_, p_174588_) -> {
         float var10000;
         if (p_174587_ instanceof Player player) {
            if (player.hasEffect((MobEffect)Seffects.MADNESS.get())) {
               var10000 = 1.0F;
               return var10000;
            }
         }

         var10000 = 0.0F;
         return var10000;
      });
   }

   private static void makeSickle(Item item) {
      ItemProperties.register(item, new ResourceLocation("thrown"), (p_174585_, p_174586_, p_174587_, p_174588_) -> {
         Item patt4156$temp = p_174585_.getItem();
         float var10000;
         if (patt4156$temp instanceof InfectedSickle sickle) {
            if (sickle.getThrownSickle(p_174585_)) {
               var10000 = 1.0F;
               return var10000;
            }
         }

         var10000 = 0.0F;
         return var10000;
      });
   }

   private static void makeCleaver(Item item) {
      ItemProperties.register(item, new ResourceLocation("swipe"), (stack, p_174586_, player, p_174588_) -> stack.getItem() instanceof InfectedCleaver && player != null && player.isUsingItem() ? 1.0F : 0.0F);
   }

   private static void makeShield(Item item) {
      ItemProperties.register(item, new ResourceLocation("use"), (stack, p_174586_, player, p_174588_) -> stack.getItem() instanceof InfectedShield && player != null && player.isUsingItem() ? 1.0F : 0.0F);
   }
}
