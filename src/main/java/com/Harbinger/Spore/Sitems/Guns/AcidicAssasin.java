package com.Harbinger.Spore.Sitems.Guns;

import com.Harbinger.Spore.Client.AnimationTrackers.AssassinReloadAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.AssassinShootAnimationTracker;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Projectile.GunProjectiles.AssassinBullet;
import com.Harbinger.Spore.Sitems.CustomModelArmorData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class AcidicAssasin extends AbstractSporeGun implements CustomModelArmorData {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore:textures/item/acidic_assasin.png");

   public AcidicAssasin() {
      super((Integer)SConfig.SERVER.acidic_assassin_durability.get());
   }

   public boolean needsToReload() {
      return true;
   }

   public int getDefaultTimeBeforeReload() {
      return 80;
   }

   public int getTimeBeforeChangingClip(ItemStack stack) {
      return 40;
   }

   public int timeBeforeStomachContentsConvertIntoAmmo() {
      return 200;
   }

   public int getClipSize() {
      return 6;
   }

   public Item getAmmoItem() {
      return (Item)Sitems.ACID_VIAL.get();
   }

   public void triggerReloadAnimation(Player player) {
      super.triggerReloadAnimation(player);
      AssassinReloadAnimationTracker.trigger(player);
   }

   public void clientShoot(Player player, InteractionHand interactionHand) {
      AssassinShootAnimationTracker.trigger(player);
   }

   public void serverShoot(ItemStack stack, ServerPlayer player, InteractionHand hand, Vec3 vec3) {
      super.serverShoot(stack, player, hand, vec3);
      int getVar = this.getTypeVariant(stack);
      AssassinBullet bullet = new AssassinBullet((EntityType)Sentities.ASSASSIN_BULLET.get(), player.level());
      bullet.setVariant(getVar);
      bullet.moveTo(player.getX() + vec3.x, player.getY() + (double)1.25F, player.getZ() + vec3.z);
      bullet.shootFrom(player, 7.0F, 0.0F, (float)this.calculateTrueDamage(stack, (double)(Integer)SConfig.SERVER.acidic_assassin_damage.get()));
      player.level().addFreshEntity(bullet);
      player.level().playSound((Player)null, player.getX(), player.getY(), player.getZ(), (SoundEvent)Ssounds.ASSASSIN_SHOT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
   }

   public ResourceLocation getTextureLocation() {
      return TEXTURE;
   }

   public Component extraTips() {
      return Component.translatable("spore.item.desc.assassin").withStyle(ChatFormatting.GREEN);
   }
}
