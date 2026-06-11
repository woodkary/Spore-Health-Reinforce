package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.AI.HurtTargetGoal;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public class UtilityEntity extends PathfinderMob {
   public Predicate<LivingEntity> TARGET_SELECTOR = (entity) -> Utilities.TARGET_SELECTOR.Test(entity);

   protected UtilityEntity(EntityType type, Level level) {
      super(type, level);
   }

   protected boolean shouldDespawnInPeaceful() {
      return true;
   }

   public List<String> getDropList() {
      return null;
   }

   public boolean doHurtTarget(Entity entity) {
      float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      if (entity instanceof LivingEntity) {
         f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)entity).getMobType());
         f1 += (float)EnchantmentHelper.getKnockbackBonus(this);
      }

      int i = EnchantmentHelper.getFireAspect(this);
      if (i > 0) {
         entity.setSecondsOnFire(i * 4);
      }

      boolean flag = entity.hurt(this.getCustomDamage(this), f);
      if (flag) {
         if (f1 > 0.0F && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.knockback((double)(f1 * 0.5F), (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, (double)1.0F, 0.6));
         }

         this.doEnchantDamageEffects(this, entity);
         this.setLastHurtMob(entity);
      }

      if (entity instanceof Player player) {
         this.maybeDisableShield(player, this.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
      }

      return flag;
   }

   public void maybeDisableShield(Player p_21425_, ItemStack p_21426_, ItemStack p_21427_) {
      if (!p_21426_.isEmpty() && !p_21427_.isEmpty() && p_21426_.getItem() instanceof AxeItem && p_21427_.is(Items.SHIELD)) {
         float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
         if (this.random.nextFloat() < f) {
            p_21425_.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.level().broadcastEntityEvent(p_21425_, (byte)30);
         }
      }

   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      return this.damageSources().mobAttack(this);
   }

   protected void addTargettingGoals() {
      this.goalSelector.addGoal(2, (new HurtTargetGoal(this, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity), new Class[]{Infected.class})).setAlertOthers(Infected.class));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> livingEntity instanceof Player || ((List)SConfig.SERVER.whitelist.get()).contains(livingEntity.getEncodeId())) {
         protected AABB getTargetSearchArea(double value) {
            return this.mob.getBoundingBox().inflate(value, Math.max(4.0,value), value);
         }
      });
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> (Boolean)SConfig.SERVER.at_mob.get() && this.TARGET_SELECTOR.test(livingEntity)) {
         protected AABB getTargetSearchArea(double value) {
            return this.mob.getBoundingBox().inflate(value, Math.max(4.0,value), value);
         }
      });
   }

   public void dropCustomDeathLoot(DamageSource source, int val, boolean bool) {
      super.dropCustomDeathLoot(source, val, bool);
      if (this.getDropList() != null) {
         if (!this.getDropList().isEmpty()) {
            for(String str : this.getDropList()) {
               String[] string = str.split("\\|");
               ItemStack itemStack = new ItemStack((ItemLike)Objects.requireNonNull((Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string[0]))));
               int m = 1;
               if (Integer.parseUnsignedInt(string[2]) == Integer.parseUnsignedInt(string[3])) {
                  int o = Integer.parseUnsignedInt(string[3]);
                  m = val > 0 ? this.random.nextInt(o, o + val) : o;
               } else if (Integer.parseUnsignedInt(string[2]) >= 1 && Integer.parseUnsignedInt(string[2]) >= 1) {
                  int v1 = Integer.parseUnsignedInt(string[2]);
                  int v2 = Integer.parseUnsignedInt(string[3]);
                  float e = (float)m * 0.15F * (float)val;
                  int i = e > (float)val ? (int)e : val;
                  m = this.random.nextInt(v1, v2 + i);
               }

               int value = Integer.parseUnsignedInt(string[1]) + val * 10;
               if (Math.random() < (double)((float)value / 100.0F)) {
                  itemStack.setCount(m);
                  ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack);
                  item.setPickUpDelay(10);
                  this.level().addFreshEntity(item);
               }
            }
         }

      }
   }

   protected boolean Cold() {
      BlockPos pos = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
      Biome biome = (Biome)this.level().getBiome(pos).value();
      return (Boolean)SConfig.SERVER.weaktocold.get() && (double)biome.getBaseTemperature() <= 0.2;
   }

   public String getMutation() {
      return null;
   }
}
