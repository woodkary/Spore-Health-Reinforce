package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Compat.l2Hostility.L2HostilityMobTraits;
import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.ExtremelySusThings.ChunkLoaderHelper;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.*;
import com.Harbinger.Spore.Sentities.AI.CalamityPathNavigation;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.CalamityVigilCall;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.MovementControls.CalamityMovementControl;
import com.Harbinger.Spore.Sentities.MovementControls.SmoothLookControl;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.Harbinger.Spore.Sentities.Utility.CorpseEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class Calamity extends UtilityEntity implements Enemy, ArmorPersentageBypass, ChunkLoaderMob, ColdWeakness,ICustomLifeCycleEntity, AdaptableEntity {
   public static final EntityDataAccessor KILLS;
   public static final EntityDataAccessor MUTATION;
   public static final EntityDataAccessor SEARCH_AREA;
   public static final EntityDataAccessor ROOTED;
   protected int breakCounter;
   private int stun = 0;
   private int crushingTick=0;
   private static final List states;
   private int adaptationCount=0;
   private LivingEntity sporeTarget;

   public int getAdaptationCount() {
      return adaptationCount;
   }
   public void setAdaptationCount(int adaptationCount) {
      this.adaptationCount = adaptationCount;
   }


   public Calamity(EntityType type, Level level) {
      super(type, level);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
      this.navigation = new CalamityPathNavigation(this, level);
      this.moveControl = new CalamityMovementControl(this, 20);
      this.lookControl = new SmoothLookControl(this, 3.0F, 2.0F, 0.35F);
      this.xpReward = 50;
      initCustom();
   }

   protected int calculateFallDamage(float p_149389_, float p_149390_) {
      return super.calculateFallDamage(p_149389_, p_149390_) - 25;
   }
   @Override
   public void onRemovedFromWorld() {
      onRemoved();
   }

   public void setStun(int i) {
      this.stun = i;
   }

   public boolean isStunned() {
      return this.stun > 0;
   }

   public boolean doHurtTarget(Entity entity) {
      if (super.doHurtTarget(entity)) {
         if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            Utilities.doCustomModifiersAfterEffects(this, livingEntity);
            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 1), this);
         }

         return true;
      } else {
         return false;
      }
   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      if (Math.random() < (double)0.5F) {
         return SdamageTypes.calamity_damage1(this);
      } else {
         return Math.random() < (double)0.5F ? SdamageTypes.calamity_damage2(this) : SdamageTypes.calamity_damage3(this);
      }
   }
   @Override
   public LivingEntity getTarget() {
      return this.sporeTarget;
   }
   @Override
   public void setTarget(@Nullable LivingEntity p_21544_) {
      if(SporeJudge.isSporeEntity(p_21544_)){
         return;
      }
      this.sporeTarget = p_21544_;
      if (this.isRooted()) {
         this.setRooted(false);
      }

   }

   protected void tickPart(CalamityMultipart part, double e, double i, double o) {
      part.setPos(this.getX() + e, this.getY() + i, this.getZ() + o);
   }

   protected void tickPart(CalamityMultipart part, Vec3 vec3i) {
      Vec3 vec3 = vec3i.yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      part.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
   }

   protected void tickPart(CalamityMultipart part, Vec3 vec3i, double Y) {
      Vec3 vec3 = vec3i.yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      part.setPos(this.getX() + vec3.x, this.getY() + Y, this.getZ() + vec3.z);
   }

   public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
      AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
      this.entityData.set(KILLS, (Integer)this.entityData.get(KILLS) + 1);
      if (this.canCalcify(entity) && armor != null) {
         armor.setBaseValue(armor.getValue() + (double)1.0F);
      }

      super.awardKillScore(entity, i, damageSource);
   }

   public void travel(Vec3 p_32858_) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.1F, p_32858_);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
         if (this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, -0.005, (double)0.0F));
         }
      } else {
         super.travel(p_32858_);
      }

   }
   @Override
   public void actuallyHurt(DamageSource source, float amount) {
      float lastHealth=0.0f;
      if(!source.is(DamageTypes.FREEZE)){
         amount*=(1.0f-this.getAdaptationCount()*0.02f);
         lastHealth=this.getHealth();
      }
      actualHurt(source, amount);
      int master = L2HostilityMobTraits.INSTANCE.getTraitLevel(this, "master");
      //受到伤害大于一定值
      if(master>0&&lastHealth-this.getHealth()>=50.0f){
         BlockPos blockPos = this.blockPosition();
         this.level().getEntitiesOfClass(Calamity.class,
                 new AABB(blockPos).inflate(32.0*master),
                 calamity -> !this.equals(calamity)&&calamity.getSearchArea()==BlockPos.ZERO).forEach(calamity ->
                 calamity.setSearchArea(blockPos));
      }
   }
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("kills", (Integer)this.entityData.get(KILLS));
      tag.putInt("mutation", (Integer)this.entityData.get(MUTATION));
      tag.putBoolean("rooted", (Boolean)this.entityData.get(ROOTED));
      tag.putInt("AreaX", this.getSearchArea().getX());
      tag.putInt("AreaY", this.getSearchArea().getY());
      tag.putInt("AreaZ", this.getSearchArea().getZ());
      tag.putInt("adaptationCount",this.getAdaptationCount());
      addSaveData(tag);
   }

   public void setMutationColor() {
      int value = this.colorMap().isEmpty() ? 0 : Utilities.mixColors(this.colorMap());
      this.entityData.set(MUTATION, value);
   }

   public int getMutationColor() {
      return (Integer)this.entityData.get(MUTATION);
   }

   Map colorMap() {
      Map<Integer, Float> values = new HashMap();
      float toxic = this.getAtLevel(this.getAttribute((Attribute)SAttributes.TOXICITY.get()));
      float rejuvenation = this.getAtLevel(this.getAttribute((Attribute)SAttributes.REJUVENATION.get()));
      float local = this.getAtLevel(this.getAttribute((Attribute)SAttributes.LOCALIZATION.get()));
      float laceration = this.getAtLevel(this.getAttribute((Attribute)SAttributes.LACERATION.get()));
      float corrosive = this.getAtLevel(this.getAttribute((Attribute)SAttributes.CORROSIVES.get()));
      float ballistic = this.getAtLevel(this.getAttribute((Attribute)SAttributes.BALLISTIC.get()));
      float grinding = this.getAtLevel(this.getAttribute((Attribute)SAttributes.GRINDING.get()));
      if (toxic > 0.0F) {
         values.put(-16751104, toxic);
      }

      if (rejuvenation > 0.0F) {
         values.put(-10092442, rejuvenation);
      }

      if (local > 0.0F) {
         values.put(-6711040, local);
      }

      if (laceration > 0.0F) {
         values.put(-65536, laceration);
      }

      if (corrosive > 0.0F) {
         values.put(-13369549, corrosive);
      }

      if (ballistic > 0.0F) {
         values.put(-10066330, ballistic);
      }

      if (grinding > 0.0F) {
         values.put(-16764058, grinding);
      }

      return values;
   }

   public float getAtLevel(AttributeInstance instance) {
      return instance != null ? (float)instance.getValue() : 0.0F;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean isPushedByFluid(FluidType type) {
      return false;
   }

   public void setSearchArea(BlockPos blockPos) {
      this.entityData.set(SEARCH_AREA, blockPos);
   }

   public void setSearchAreaIfAbsent(BlockPos blockPos){
      BlockPos searchArea = getSearchArea();
      if(searchArea==BlockPos.ZERO){
         this.setSearchArea(blockPos);
      }
   }

   public BlockPos getSearchArea() {
      return (BlockPos)this.entityData.get(SEARCH_AREA);
   }

   public void setKills(Integer count) {
      this.entityData.set(KILLS, count);
   }

   public int getKills() {
      return (Integer)this.entityData.get(KILLS);
   }

   public Packet getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(KILLS, tag.getInt("kills"));
      this.entityData.set(MUTATION, tag.getInt("mutation"));
      this.entityData.set(ROOTED, tag.getBoolean("rooted"));
      int i = tag.getInt("AreaX");
      int j = tag.getInt("AreaY");
      int k = tag.getInt("AreaZ");
      this.setSearchArea(new BlockPos(i, j, k));
      if(tag.contains("adaptationCount")){
         this.setAdaptationCount(tag.getInt("adaptationCount"));
      }
      readSaveData(tag);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ROOTED, false);
      this.entityData.define(KILLS, 0);
      this.entityData.define(MUTATION, 0);
      this.entityData.define(SEARCH_AREA, BlockPos.ZERO);
   }

   public boolean canCalcify(Entity entity) {
      return entity.getType().is(EntityTypeTags.SKELETONS);
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   public boolean isRooted() {
      return (Boolean)this.entityData.get(ROOTED);
   }

   public void setRooted(boolean value) {
      this.entityData.set(ROOTED, value);
   }

   public void registerGoals() {
      this.goalSelector.addGoal(1, new GoToLocation(this, 1.1));
      this.addTargettingGoals();
      this.goalSelector.addGoal(7, new CalamityVigilCall(this));
   }

   public double getDamageCap() {
      return (double)0.0F;
   }

   public boolean hurt(DamageSource source, float amount) {
      if(SporeEntityHeeaafastthManager.INSTANCE.isInvul(this,source)){
         return false;
      }
      this.setRooted(false);
      if (this.getRandom().nextInt(20) == 0) {
         this.grief(this.getBoundingBox().inflate(this.setInflation(), (double)0.0F, this.setInflation()));
      }

      if ((double)amount > this.getDamageCap() && this.getDamageCap() > (double)0.0F) {
         return super.hurt(source, (float)this.getDamageCap());
      } else {
         if (source.is(DamageTypes.FREEZE) && Math.random() < (double)0.2F) {
            forceStart(findGoal(this, SporeBurstSupport.class));
         }

         return super.hurt(source, amount);
      }
   }
   private static MethodHandle startMethod=null;
   public static void forceStart(Goal goal) {
      if(goal==null){
         return;
      }
      if(startMethod==null){
         try{
            startMethod= ClassUtil.getLookup().findVirtual(
                    Goal.class,
                    "m_8056_",
                    MethodType.methodType(void.class)
            );
         }catch (Throwable throwable){
            LogUtil.error("can't find start method with obfuscated name");
         }
      }
      if(startMethod==null){
         try{
            startMethod= ClassUtil.getLookup().findVirtual(
                    Goal.class,
                    "start",
                    MethodType.methodType(void.class)
            );
         }catch (Throwable throwable){
            LogUtil.error("can't find start method with deobfuscated name");
         }
      }
      if(startMethod!=null){
         try{
            startMethod.invoke(goal);
            return;
         }catch (Throwable throwable){
            LogUtil.error("can't invoke start method");
         }
      }
      goal.start();
//      try {
//         Method m = Goal.class.getDeclaredMethod("m_8056_");
//         m.setAccessible(true);
//         m.invoke(goal);
//         return;
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
//      try {
//         Method m = Goal.class.getDeclaredMethod("start");
//         m.setAccessible(true);
//         m.invoke(goal);
//      } catch (Exception e) {
//         e.printStackTrace();
//      }

   }

   public static Goal findGoal(Mob mob, Class goalClass) {
      for(WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
         if (goalClass.isInstance(wrapped.getGoal())) {
            return (Goal)goalClass.cast(wrapped.getGoal());
         }
      }

      return null;
   }

   public boolean tryToDigDown() {
      if (this.getSearchArea() != BlockPos.ZERO && this.verticalCollisionBelow) {
         double x = (double)Math.abs(this.getSearchArea().getX()) - Math.abs(this.getX());
         double z = (double)Math.abs(this.getSearchArea().getZ()) - Math.abs(this.getZ());
         return (double)this.getSearchArea().getY() < this.getY() && Math.abs(x) < (double)6.0F && Math.abs(z) < (double)6.0F;
      } else {
         return false;
      }
   }

   public void relocateExitPoint() {
      RandomSource randomSource = RandomSource.create();
      if ((double)this.getSearchArea().getY() > this.getY() && (double)Math.abs(this.getSearchArea().getX()) - Math.abs(this.getX()) < (double)6.0F && (double)Math.abs(this.getSearchArea().getZ()) - Math.abs(this.getZ()) < (double)6.0F && (double)Math.abs(this.getSearchArea().getY()) - Math.abs(this.getY()) > (double)4.0F) {
         int f = (int)Math.abs((double)Math.abs(this.getSearchArea().getY()) - Math.abs(this.getY()));
         int x = randomSource.nextInt(-f, f);
         int z = randomSource.nextInt(-f, f);
         this.setSearchArea(new BlockPos(this.getSearchArea().getX() + x, this.getSearchArea().getY(), this.getSearchArea().getZ() + z));
      }

   }

   public double setInflation() {
      return (double)1.5F;
   }

   public AABB getMiningHitbox() {
      if (this.getSearchArea() != BlockPos.ZERO && this.getTarget() == null) {
         if ((double)this.getSearchArea().getY() < this.getY()) {
            return this.getBoundingBox().inflate(this.setInflation(), (double)0.0F, this.setInflation()).move((double)0.0F, (double)-1.0F, (double)0.0F);
         } else {
            return (double)this.getSearchArea().getY() > this.getY() ? this.getBoundingBox().inflate(this.setInflation(), (double)0.0F, this.setInflation()).move((double)0.0F, (double)1.0F, (double)0.0F) : this.getBoundingBox().inflate(this.setInflation(), (double)0.0F, this.setInflation());
         }
      } else {
         return this.getBoundingBox().inflate(this.setInflation(), (double)0.0F, this.setInflation()).move((double)0.0F, (double)1.0F, (double)0.0F);
      }
   }

   protected void grief(AABB aabb) {
      boolean flag = false;

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.is(Utilities.biomass)) {
            flag = this.level().setBlock(blockpos, ((Block)Sblocks.MEMBRANE_BLOCK.get()).defaultBlockState(), 3) || flag;
            this.breakCounter = 0;
         } else if (blockstate.getDestroySpeed(this.level(), blockpos) < (float)this.getDestroySpeed() && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            flag = this.level().destroyBlock(blockpos, false, this) || flag;
            this.breakCounter = 0;
         }
      }

   }

   public void ActivateAdaptation() {
   }
   public void setCrushingTick(int ticks) {
      this.crushingTick = ticks;
   }
   public void tick() {
      super.tick();
      tickCustomLifeCycle();
      if(this.crushingTick>0&&!this.level.isClientSide) {
         boolean willPlaySound=this.crushingTick--%10==0;
         DamageSource source=getCustomDamage(this);
         for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class,
                 this.bb.inflate(6.0),
                 liv -> liv.isAlive()&&!SporeJudge.isSporeEntity(liv) &&
                         !(liv instanceof Player p && EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(p)))) {
            if(willPlaySound) {
               SporeAttackUtil.INSTANCE.playSound(this.level,null,this.getX(),this.getY(),this.getZ(),(SoundEvent)Ssounds.SIEGER_BITE.get(),this.getSoundSource(),1.0f,1.0f);
            }
            SporeAttackUtil.INSTANCE.dealDamage(living,this,source,2.0f);
         }
      }
      if (this.tickCount % 1200 == 0) {
         this.setRooted(this.getTarget() == null && (double)this.getHealth() <= (double)this.getMaxHealth() * 0.3 && this.onGround());
         if (this.isRooted()) {
            this.setKills(this.getKills() + 1);
         }
      }

      if (this.isRooted()) {
         this.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
      }

      if (this.getRandom().nextInt(300) == 0 && this.getSearchArea() != BlockPos.ZERO) {
         this.relocateExitPoint();
      }

      if (this.breakCounter < 80) {
         ++this.breakCounter;
      } else if (this.getLastDamageSource() == this.damageSources().cactus() || this.getLastDamageSource() == this.damageSources().inWall() || this.horizontalCollision || this.tryToDigDown()) {
         this.grief(this.getMiningHitbox());
      }

      if (this.stun > 0 && this.onGround()) {
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var2;
            --this.stun;
            this.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
            double x0 = this.getX() - ((double)this.random.nextFloat() - 0.1) * 1.2;
            double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * (double)1.25F * (double)5.0F;
            double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 1.2;
            serverLevel.sendParticles((SimpleParticleType)Sparticles.BLOOD_PARTICLE.get(), x0, y0, z0, 4, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         }
      }

      if (this.getHealth() < this.getMaxHealth() && !this.hasEffect(MobEffects.REGENERATION) && this.getKills() > 0) {
         int level = this.getHealth() < this.getMaxHealth() / 2.0F ? 1 : 0;
         this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, level + this.calculateHealing()));
         this.setKills(this.getKills() - 1);
      }

   }

   private int calculateHealing() {
      AttributeInstance toxic = this.getAttribute((Attribute)SAttributes.REJUVENATION.get());
      if (toxic != null) {
         double level = toxic.getValue();
         return level < (double)1.0F ? 0 : (int)level;
      } else {
         return 0;
      }
   }

   public int getDestroySpeed() {
      return (Integer)SConfig.SERVER.calamity_bd.get();
   }

   public float amountOfDamage(float value) {
      float extra = 0.0F;
      AttributeInstance penetration = this.getAttribute((Attribute)SAttributes.LACERATION.get());
      if (penetration != null) {
         double e = penetration.getValue();
         if (e >= (double)1.0F) {
            extra = (float)(e * (double)0.1F);
         }
      }

      AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
      return attack == null ? value : (float)(attack.getValue() * (double)(0.2F + extra));
   }

   public String getChunkId() {
      UUID ownerId = this.getUUID();
      return "calamity_" + ownerId + "_";
   }

   public boolean shouldLoadChunk() {
      return (Boolean)SConfig.SERVER.calamity_chunk.get() && this.getSearchArea() != BlockPos.ZERO;
   }
   public void heal(float amount) {
      healSelf(amount);
   }
   public int chunkLifeTicks() {
      return 600;
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.CALAMITY;
   }

   @Override
   public void tickDeath() {
      this.die(this.lastDamageSource!=null ? this.lastDamageSource : this.damageSources().cactus());
   }

   public void die(DamageSource source) {
      if(this.getHealth()>0.0f){
         return;
      }
      Level var3 = this.level();
      if (var3 instanceof ServerLevel serverLevel) {
         double x0 = this.getX() - ((double)this.random.nextFloat() - 0.1) * 3.2;
         double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * (double)3.25F * (double)5.0F;
         double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 3.2;
         serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 4, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
      }

      this.playSound((SoundEvent)Ssounds.CALAMITY_DEATH.get());
      super.die(source);
      this.summonBiomass();
      this.discard();
   }

   private void SummonMound(Entity entity) {
      Mound mound = new Mound((EntityType)Sentities.MOUND.get(), entity.level());
      mound.moveTo(entity.getX(), entity.getY(), entity.getZ());
      mound.setMaxAge(4);
      entity.level().addFreshEntity(mound);
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.CALAMITY_DAMAGE.get();
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      this.setDefaultAdaptation(serverLevelAccessor);
      this.setMutationColor();
      return super.finalizeSpawn(serverLevelAccessor, p_21435_, p_21436_, p_21437_, p_21438_);
   }

   public void setDefaultAdaptation(ServerLevelAccessor level) {
      if (level instanceof ServerLevel serverLevel) {
         SporeSavedData data = SporeSavedData.getDataLocation(serverLevel);
         if (data != null && data.getAmountOfHiveminds() >= (Integer)SConfig.SERVER.proto_spawn_world_mod.get()) {
            this.ActivateAdaptation();
         }
      }

   }

   public boolean addEffect(MobEffectInstance instance, @Nullable Entity entity) {
      return instance.getEffect().getCategory() == MobEffectCategory.HARMFUL && instance.getAmplifier() < 1 ? false : super.addEffect(instance, entity);
   }

   public List<ItemStack> getDroppedItems(int val) {
      List<ItemStack> drops = new ArrayList();
      if (this.getDropList() != null && !this.getDropList().isEmpty()) {
         for(String str : this.getDropList()) {
            String[] parts = str.split("\\|");
            if (parts.length >= 4) {
               ResourceLocation itemId = new ResourceLocation(parts[0]);
               Item item = (Item)ForgeRegistries.ITEMS.getValue(itemId);
               if (item != null) {
                  ItemStack stack = new ItemStack(item);
                  int minCount = Integer.parseUnsignedInt(parts[2]);
                  int maxCount = Integer.parseUnsignedInt(parts[3]);
                  int chancePercent = Integer.parseUnsignedInt(parts[1]) + val * 10;
                  int quantity;
                  if (minCount == maxCount) {
                     quantity = val > 0 ? this.random.nextInt(maxCount, maxCount + val) : maxCount;
                  } else if (minCount >= 1 && maxCount >= 1) {
                     float scale = 1.0F + 0.15F * (float)val;
                     int adjustedMax = (int)((float)maxCount * scale);
                     quantity = this.random.nextInt(minCount, adjustedMax + 1);
                  } else {
                     quantity = 1;
                  }

                  if (this.random.nextFloat() < (float)chancePercent / 100.0F) {
                     stack.setCount(quantity);
                     drops.add(stack);
                  }
               }
            }
         }

         return drops;
      } else {
         return drops;
      }
   }

   public List<HitboxesForParts> parts() {
      return List.of();
   }

   public boolean getAdaptation() {
      return false;
   }

   public void dropCustomDeathLoot(DamageSource source, int val, boolean bool) {
      if (!this.level().isClientSide()) {
         List<ItemStack> loot = this.getDroppedItems(val);
         List<HitboxesForParts> partList = this.parts();
         if (!partList.isEmpty() && !loot.isEmpty()) {
            int partCount = partList.size();
            List<List<ItemStack>> distributedLoot = new ArrayList();

            for(int i = 0; i < partCount; ++i) {
               distributedLoot.add(new ArrayList());
            }

            for(ItemStack stack : loot) {
               int baseAmount = stack.getCount() / partCount;
               int remainder = stack.getCount() % partCount;

               for(int i = 0; i < partCount; ++i) {
                  int amount = baseAmount + (i < remainder ? 1 : 0);
                  if (amount > 0) {
                     ((List)distributedLoot.get(i)).add(stack.copyWithCount(amount));
                  }
               }
            }

            this.summonCorpsePart(partCount, distributedLoot, partList);
         }
      }
   }

   public void summonCorpsePart(int partCount, List distributedLoot, List partList) {
      for(int i = 0; i < partCount; ++i) {
         CorpseEntity partEntity = new CorpseEntity((EntityType)Sentities.CORPSE_PIECE.get(), this.level());

         for(ItemStack stack : (List<ItemStack>)distributedLoot.get(i)) {
            partEntity.addToInventory(stack);
         }

         partEntity.setColor(this.getMutationColor());
         partEntity.moveTo(this.position());
         partEntity.setDeltaMovement(new Vec3((this.random.nextDouble() - this.random.nextDouble()) * 0.9, this.random.nextDouble() * 0.6 + 0.3, (this.random.nextDouble() - this.random.nextDouble()) * 0.9));
         partEntity.setOwnerAda(this.getAdaptation());
         partEntity.setCorpseType(((HitboxesForParts)partList.get(i)).getID());
         this.level().addFreshEntity(partEntity);
      }

   }

   public HitboxesForParts calculateChance(HitboxesForParts part, float val) {
      return Math.random() < (double)val ? part : null;
   }

   private void summonBiomass() {
      if (!this.level().isClientSide) {
         AABB aabb = this.getBoundingBox().inflate((double)1.0F);

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockState blockState = this.level().getBlockState(blockpos);
            if (blockState.isAir() && Math.random() < (double)0.1F) {
               BlockState state = (BlockState)states.get(this.random.nextInt(states.size()));
               FallingBlockEntity.fall(this.level(), blockpos, state);
            }
         }

      }
   }

   static {
      KILLS = SynchedEntityData.defineId(Calamity.class, EntityDataSerializers.INT);
      MUTATION = SynchedEntityData.defineId(Calamity.class, EntityDataSerializers.INT);
      SEARCH_AREA = SynchedEntityData.defineId(Calamity.class, EntityDataSerializers.BLOCK_POS);
      ROOTED = SynchedEntityData.defineId(Calamity.class, EntityDataSerializers.BOOLEAN);
      states = List.of(((Block)Sblocks.BIOMASS_BLOCK.get()).defaultBlockState(), ((Block)Sblocks.ROOTED_BIOMASS.get()).defaultBlockState(), ((Block)Sblocks.CALCIFIED_BIOMASS_BLOCK.get()).defaultBlockState(), ((Block)Sblocks.SICKEN_BIOMASS_BLOCK.get()).defaultBlockState(), ((Block)Sblocks.REMAINS.get()).defaultBlockState());
   }

   @Override
   public LivingEntity entity() {
      return this;
   }
   @Override
   public boolean isProtoOrCalamity(){
      return true;
   }

   public static class GoToLocation extends Goal {
      public final Calamity infected;
      public final double speed;
      public int tryTicks;

      public GoToLocation(Calamity infected1, double speed) {
         this.infected = infected1;
         this.speed = speed;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      protected void moveMobToBlock() {
         this.infected.getNavigation().moveTo((double)((float)this.infected.getSearchArea().getX()) + (double)0.5F, (double)(this.infected.getSearchArea().getY() + 1), (double)((float)this.infected.getSearchArea().getZ()) + (double)0.5F, (double)1.0F);
      }

      public boolean canUse() {
         if (this.infected.getSearchArea() != BlockPos.ZERO && this.infected.getSearchArea() != null && this.infected.getTarget() == null) {
            return this.infected.getSearchArea().distToCenterSqr(this.infected.position()) > (double)4.0F;
         } else {
            return false;
         }
      }

      public void start() {
         this.moveMobToBlock();
         this.tryTicks = 0;
         super.start();
      }

      public boolean canContinueToUse() {
         return this.infected.getTarget() == null;
      }

      public void tick() {
         super.tick();
         ++this.tryTicks;
         if (this.infected.getSearchArea() != BlockPos.ZERO && this.shouldRecalculatePath()) {
            this.moveMobToBlock();
         }

         if (this.infected.getSearchArea() != BlockPos.ZERO && this.infected.getSearchArea().distToCenterSqr(this.infected.position()) < (double)80.0F) {
            this.infected.setSearchArea(BlockPos.ZERO);
            this.infected.SummonMound(this.infected);
            ChunkPos chunk = this.infected.chunkPosition();
            UUID ownerId = this.infected.getUUID();
            String id = "calamity_" + ownerId + "_" + chunk.toString();
            ChunkLoaderHelper.removeRequest(id);
         }

      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 40 == 0;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }
   }
}
