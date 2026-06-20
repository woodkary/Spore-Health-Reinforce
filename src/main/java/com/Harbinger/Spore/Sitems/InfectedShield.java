package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Fluids.BileLiquid;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsBaseItem;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsMutations;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class InfectedShield extends SporeToolsBaseItem {
   public static final String CHARGE_TAG = "ShieldCharge";
   public static final int MAX_CHARGE = 10;

   public InfectedShield() {
      super((double)(Integer)SConfig.SERVER.shield_damage.get(), (double)0.0F, (double)1.0F, (Integer)SConfig.SERVER.shield_durability.get(), 0, "shield");
      DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
   }

   public UseAnim getUseAnimation(ItemStack p_43105_) {
      return UseAnim.BLOCK;
   }

   public int getUseDuration(ItemStack p_43107_) {
      return 72000;
   }

   public InteractionResultHolder use(Level p_43099_, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (this.tooHurt(itemstack)) {
         player.startUsingItem(hand);
      }

      return InteractionResultHolder.consume(itemstack);
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      this.hurtTool(stack, attacker, 1);
      return true;
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.OFFHAND;
   }

   public void setCharge(ItemStack stack, int value) {
      stack.getOrCreateTag().putInt("ShieldCharge", value);
   }

   public int getCharge(ItemStack stack) {
      return stack.getOrCreateTag().getInt("ShieldCharge");
   }

   public int damageItem(ItemStack stack, int amount, LivingEntity entity, Consumer onBroken) {
      int current = this.getCharge(stack);
      ++current;
      this.setCharge(stack, current);
      if (current >= 10) {
         this.triggerBash(entity, stack);
      }

      int durabilityLeft = stack.getMaxDamage() - stack.getDamageValue();
      if (durabilityLeft - amount <= 11) {
         entity.playSound((SoundEvent)Ssounds.INFECTED_GEAR_BREAK.get());
      }

      if (this.tooHurt(stack)) {
         if (this.getAdditionalDurability(stack) > 0) {
            this.hurtExtraDurability(stack, amount, entity);
            return 0;
         } else {
            return super.damageItem(stack, this.calculateDurabilityLostForMutations(amount, stack), entity, onBroken);
         }
      } else {
         return 0;
      }
   }

   public void triggerBash(LivingEntity player, ItemStack stack) {
      if (!player.level().isClientSide) {
         double radius = (double)5.0F;
         Vec3 look = player.getLookAngle();
         AABB area = player.getBoundingBox().expandTowards(look.scale(radius)).inflate((double)2.0F);

         for(LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area, (e) -> e != player && e.isAlive() && player.hasLineOfSight(e))) {
            ((ServerLevel)target.level()).sendParticles((SimpleParticleType)Sparticles.SPORE_IMPACT.get(), target.getX(), target.getY() + (double)1.0F, target.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
            Vec3 direction = target.position().subtract(player.position()).normalize();
            target.hurtMarked = true;
            target.knockback(this.getVariant(stack) == SporeToolsMutations.CALCIFIED ? (double)2.5F : (double)1.5F, -direction.x, -direction.z);
            target.hurt(player.damageSources().generic(), (float)(Integer)SConfig.SERVER.shield_damage.get());
            this.abstractEffects(stack, target);
            if (this.getVariant(stack) == SporeToolsMutations.TOXIC) {
               target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            }

            if (this.getVariant(stack) == SporeToolsMutations.ROTTEN) {
               target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
               addHealingInhibitRandom(target);
            }
         }

         if (this.getVariant(stack) == SporeToolsMutations.VAMPIRIC) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0));
         }

         if (this.getVariant(stack) == SporeToolsMutations.BEZERK) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
         }

         this.setCharge(stack, 0);
         player.level().playSound((Player)null, player.blockPosition(), (SoundEvent)Ssounds.SHIELD_BASH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         this.hurtTool(stack, player, 1);
      }

   }

   public void abstractEffects(ItemStack stack, LivingEntity arrow) {
      if (stack.getEnchantmentLevel((Enchantment)Senchantments.CORROSIVE_POTENCY.get()) > 0) {
         arrow.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, 1));
      }

      if (stack.getEnchantmentLevel((Enchantment)Senchantments.GASTRIC_SPEWAGE.get()) > 0) {
         for(MobEffectInstance instance : BileLiquid.bileEffects()) {
            arrow.addEffect(instance);
         }
      }

      if (stack.getEnchantmentLevel((Enchantment)Senchantments.CRYOGENIC_ASPECT.get()) > 0 && arrow.canFreeze()) {
         arrow.setTicksFrozen(arrow.getTicksFrozen() + 300);
      }

      arrow.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
   }

   public void appendHoverText(ItemStack stack, @Nullable Level p_41422_, List components, TooltipFlag p_41424_) {
      super.appendHoverText(stack, p_41422_, components, p_41424_);
      int var10001 = this.getCharge(stack);
      components.add(Component.literal("Charge " + var10001 + "/10"));
   }
}
