package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.ExtremelySusThings.ClientUtils;
import com.Harbinger.Spore.Sitems.BaseWeapons.DeathRewardingWeapon;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeSwordBase;
import java.util.ArrayList;
import java.util.List;

import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsMutations;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public class InfectedCleaver extends SporeSwordBase implements DeathRewardingWeapon {
   private final List<EnAndItem> heads = this.getHeads();

   public InfectedCleaver() {
      super((double)(Integer)SConfig.SERVER.cleaver_damage.get(), (double)2.5F, (double)3.0F, (Integer)SConfig.SERVER.cleaver_durability.get(), "cleaver");
   }

   public int getUseDuration(ItemStack stack) {
      return 120;
   }

   public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
      return true;
   }

   private List<EnAndItem> getHeads() {
      List<EnAndItem> values = new ArrayList();

      for(String string : (List<String>)SConfig.SERVER.cleaver_drops.get()) {
         String[] str = string.split("\\|");
         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(str[1]));
         if (item != null) {
            values.add(new EnAndItem(str[0], item));
         }
      }

      return values;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   public void computeAfterEffect(LivingEntity victim, LivingEntity source, ItemStack weapon) {
      if (!victim.level().isClientSide) {
         for(EnAndItem item : this.heads) {
            if (item.id.equals(victim.getEncodeId()) && Math.random() < 0.1) {
               this.dropLoot(victim.level(), victim.getX(), victim.getY(), victim.getZ(), new ItemStack(item.item));
               break;
            }
         }

      }
   }

   public void dropLoot(Level level, double x, double y, double z, ItemStack stack) {
      ItemEntity entity = new ItemEntity(level, x, y, z, stack);
      level.addFreshEntity(entity);
   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      player.playNotifySound((SoundEvent)Ssounds.CLEAVER_SPIN.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
      player.startUsingItem(hand);
      this.hurtTool(player.getItemInHand(hand), player, 1);
      return InteractionResultHolder.consume(player.getItemInHand(hand));
   }

   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
      if (entity instanceof Player player) {
         int charge = this.getUseDuration(stack) - count;
         if (level.isClientSide) {
            ClientUtils.spinPlayer(player);
         } else if (charge % 5 == 0) {
            double radius = (double)2.5F;

            for(int i = 0; i < 10; ++i) {
               double angle = (Math.PI * 2D) * (double)i / (double)10.0F;
               double x = player.getX() + radius * Math.cos(angle);
               double z = player.getZ() + radius * Math.sin(angle);
               ((ServerLevel)entity.level()).sendParticles((SimpleParticleType)Sparticles.SPORE_SLASH.get(), x, player.getY() + (double)1.0F, z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
            }

            AABB area = player.getBoundingBox().inflate((double)3.5F, (double)1.0F, (double)3.5F);

            for(LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area, (e) -> e != player && e.isAlive())) {
               this.hurtEnemy(stack, target, player);
               DamageSource source = player.damageSources().playerAttack(player);
               float v = (float) (Integer) SConfig.SERVER.cleaver_damage.get() / 2.0F;
               target.hurt(source, v);
               if(this.getVariant(stack) == SporeToolsMutations.BEZERK) {
                  SporeAttackUtil.INSTANCE.dealDamage(target, player, source, v);
               }
               target.hurtTime = 10;
               target.invulnerableTime = 10;
            }
         }

         if (count <= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 0));
            player.getCooldowns().addCooldown(this, 200);
            player.stopUsingItem();
         }

         if (charge % 20 == 0) {
            player.playNotifySound((SoundEvent)Ssounds.CLEAVER_SPIN.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
         }

         super.onUseTick(level, entity, stack, count);
      }
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int p_41415_) {
      super.releaseUsing(stack, level, living, p_41415_);
      if (living instanceof Player player) {
         player.getCooldowns().addCooldown(this, 60);
      }

   }

   private static record EnAndItem(String id, Item item) {
   }
}
