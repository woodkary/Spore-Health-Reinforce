package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WaveEntity extends UtilityEntity {
   public PathfinderMob owner;
   private int life;

   public WaveEntity(EntityType type, Level level) {
      super(type, level);
   }

   public WaveEntity(Level level, PathfinderMob entity) {
      super((EntityType)Sentities.WAVE.get(), level);
      this.owner = entity;
      this.life = 160;
      this.moveTo(this.owner.getX(), this.owner.getY(), this.owner.getZ());
      this.setTarget(this.owner.getTarget());
      this.setMaxUpStep(1.0F);
   }

   public void tick() {
      super.tick();
      if (this.life > 0 && this.owner != null && this.getTarget() != null) {
         --this.life;
      } else {
         this.discard();
      }

   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new AOEMeleeAttackGoal(this, 1.1, false, (double)2.0F, 1.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      super.registerGoals();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)1.0F).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, (double)10.0F).add(Attributes.FOLLOW_RANGE, (double)16.0F);
   }

   public boolean hurt(DamageSource p_21016_, float p_21017_) {
      return false;
   }

   public void aiStep() {
      super.aiStep();
      BlockState block = this.level().getBlockState(this.getOnPos());
      Item item = block.getBlock().asItem();
      double x = this.getX() + (double)this.random.nextInt(-2, 2);
      double z = this.getZ() + (double)this.random.nextInt(-2, 2);
      if (item != ItemStack.EMPTY.getItem()) {
         Level var8 = this.level();
         if (var8 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var8;
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(item)), x, this.getY(), z, 3, ((double)this.random.nextFloat() - (double)1.0F) * 0.08, ((double)this.random.nextFloat() - (double)1.0F) * 0.08, ((double)this.random.nextFloat() - (double)1.0F) * 0.08, (double)0.15F);
         }
      }

   }

   public boolean doHurtTarget(Entity entity) {
      entity.setDeltaMovement(entity.getDeltaMovement().add((double)0.0F, 0.1, (double)0.0F));
      this.discard();
      return super.doHurtTarget(entity);
   }
}
