package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.Sentities.Projectile.AcidBall;
import com.Harbinger.Spore.Sentities.Projectile.Vomit;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

public class Organoid extends UtilityEntity implements Enemy, ColdWeakness,ICustomLifeCycleEntity {
   public static final EntityDataAccessor BORROW;
   public static final EntityDataAccessor EMERGE;

   protected Organoid(EntityType type, Level level) {
      super(type, level);
      this.xpReward = 25;
      initCustom();
   }
   @Override
   public void actuallyHurt(DamageSource source, float amount) {
      actualHurt(source, amount);
   }

   public void tick() {
      super.tick();
      tickCustomLifeCycle();
      if (this.onGround()) {
         this.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
      }

      if (!this.level().isClientSide) {
         if (this.isEmerging()) {
            this.despawnIfHardFloor();
            this.tickEmerging();
         } else if (this.isBurrowing()) {
            this.tickBurrowing();
         }
      }

      if (this.tickCount % 200 == 0 && !(this instanceof Proto) && !(this instanceof Mound)) {
         this.regulateSpawns();
      }

      this.spawnEmergingParticles();
   }

   public void despawnIfHardFloor() {
      BlockPos pos = this.getOnPos();
      BlockState state = this.level().getBlockState(pos);
      if (state.getDestroySpeed(this.level(), pos) > 4.0F || state.getDestroySpeed(this.level(), pos) < 0.0F) {
         this.discard();
      }

   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.ORGANOID_DAMAGE.get();
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   public boolean dampensVibrations() {
      return true;
   }

   public boolean hurt(DamageSource source, float p_21017_) {
      if(SporeEntityHeeaafastthManager.INSTANCE.isInvul(this,source)){
         return false;
      }
      return !(source.getDirectEntity() instanceof AcidBall) && !(source.getDirectEntity() instanceof Vomit) ? super.hurt(source, p_21017_) : false;
   }
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      addSaveData(tag);
   }
   public void heal(float amount) {
      healSelf(amount);
   }
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      readSaveData(tag);
   }
   public int getEmerge_tick() {
      return 20;
   }

   public int getBorrow_tick() {
      return 20;
   }

   public boolean isEmerging() {
      return (Integer)this.entityData.get(EMERGE) > 0;
   }

   public void tickEmerging() {
      int emerging = (Integer)this.entityData.get(EMERGE);
      if (emerging > this.getEmerge_tick()) {
         emerging = -1;
      }

      this.entityData.set(EMERGE, emerging + 1);
   }

   public boolean isBurrowing() {
      return (Integer)this.entityData.get(BORROW) > 0;
   }

   public void tickBurrowing() {
      int burrowing = (Integer)this.entityData.get(BORROW);
      if (burrowing > this.getBorrow_tick()) {
         burrowing = -1;
      }

      this.entityData.set(BORROW, burrowing + 1);
   }

   public int getEmerge() {
      return (Integer)this.entityData.get(EMERGE);
   }

   public int getBorrow() {
      return (Integer)this.entityData.get(BORROW);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BORROW, 0);
      this.entityData.define(EMERGE, 0);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33282_, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      this.tickEmerging();
      return super.finalizeSpawn(p_33282_, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth() && this.getTicksFrozen() <= 0) {
         this.setHealth(this.getHealth() + 1.0F);
      }

   }

   public void awardHivemind() {
      CompoundTag data = this.getPersistentData();
      if (data.contains("hivemind")) {
         int summonerUUID = data.getInt("hivemind");
         Level level = this.level();
         Entity summoner = level.getEntity(summonerUUID);
         if (summoner instanceof Proto) {
            Proto smartMob = (Proto)summoner;
            int decision = data.getInt("decision");
            int member = data.getInt("member");
            smartMob.praisedForDecision(decision, member);
         }
      }

   }

   public void punishHivemind() {
      CompoundTag data = this.getPersistentData();
      if (data.contains("hivemind")) {
         int summonerUUID = data.getInt("hivemind");
         Level level = this.level();
         Entity summoner = level.getEntity(summonerUUID);
         if (summoner instanceof Proto) {
            Proto smartMob = (Proto)summoner;
            int decision = data.getInt("decision");
            int member = data.getInt("member");
            smartMob.punishForDecision(decision, member);
         }
      }

   }

   private void spawnEmergingParticles() {
      if (this.isEmerging() || this.isBurrowing()) {
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var2;
            double var17 = this.getX();
            double y = this.getY();
            double z = this.getZ();
            RandomSource randomsource = this.getRandom();
            BlockPos belowPos = BlockPos.containing(var17, y - (double)1.0F, z);
            BlockState state = serverLevel.getBlockState(belowPos);
            if (!state.isAir()) {
               ItemStack stack = new ItemStack(state.getBlock());
               if (!stack.isEmpty()) {
                  for(int l = 0; l < this.getNumberOfParticles(); ++l) {
                     if (serverLevel.isLoaded(belowPos)) {
                        double xi = randomsource.nextDouble() - (double)0.5F;
                        double zi = randomsource.nextDouble() - (double)0.5F;
                        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), var17 + xi, y - 0.1, z + zi, 1, (randomsource.nextDouble() - (double)0.5F) * 0.1, (randomsource.nextDouble() - (double)0.5F) * 0.1, (randomsource.nextDouble() - (double)0.5F) * 0.1, (double)0.15F);
                     }
                  }

               }
            }
         }
      }
   }

   public int getNumberOfParticles() {
      return 2;
   }

   public boolean isCloseCombatant() {
      return false;
   }

   public boolean addEffect(MobEffectInstance instance, @org.jetbrains.annotations.Nullable Entity entity) {
      return instance.getEffect().getCategory() == MobEffectCategory.HARMFUL && instance.getAmplifier() < 1 ? false : super.addEffect(instance, entity);
   }

   public void regulateSpawns() {
      AABB aabb = this.getBoundingBox().inflate((double)6.0F);
      List<Entity> entityList = this.level().getEntities(this, aabb, (entity) -> entity instanceof Organoid && !(entity instanceof Proto) && !(entity instanceof Mound));
      if (entityList.size() > 4) {
         this.tickBurrowing();
      }

   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.EVOLVED;
   }

   static {
      BORROW = SynchedEntityData.defineId(Organoid.class, EntityDataSerializers.INT);
      EMERGE = SynchedEntityData.defineId(Organoid.class, EntityDataSerializers.INT);
   }
}
