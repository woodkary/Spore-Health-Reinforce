package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.Utility.NukeEntity;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;

public class FleshBomb extends AbstractArrow {
   private static final EntityDataAccessor DAMAGE;
   private static final EntityDataAccessor BOMB_TYPE;
   private static final EntityDataAccessor EXPLOSION;
   private static final EntityDataAccessor CARRIER;
   private Predicate livingEntityPredicate = (entityx) -> true;
   private Vec3 target;

   public FleshBomb(Level level, LivingEntity entity, float damage, BombType type, int range) {
      super((EntityType)Sentities.FLESH_BOMB.get(), level);
      this.setBombType(type.getValue());
      this.setExplosion(range);
      this.setDamage(damage);
      this.setOwner(entity);
   }

   public void setTarget(Entity entity) {
      this.target = entity.position();
   }

   public FleshBomb(EntityType fleshBombEntityType, Level level) {
      super(fleshBombEntityType, level);
   }

   public FleshBomb(PlayMessages.SpawnEntity spawnEntity, Level level) {
      super((EntityType)Sentities.FLESH_BOMB.get(), level);
   }

   public void setLivingEntityPredicate(Predicate value) {
      this.livingEntityPredicate = value;
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DAMAGE, 2.0F);
      this.entityData.define(BOMB_TYPE, 0);
      this.entityData.define(EXPLOSION, 5);
      this.entityData.define(CARRIER, false);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setDamage(tag.getFloat("damage"));
      this.setBombType(tag.getInt("bomb_type"));
      this.setExplosion(tag.getInt("explosion"));
      this.setCarrier(tag.getBoolean("carrier"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", this.getDamage());
      tag.putInt("bomb_type", this.getBombType());
      tag.putInt("explosion", this.getExplosion());
      tag.putBoolean("carrier", this.getCarrier());
   }

   public float getDamage() {
      return (Float)this.entityData.get(DAMAGE);
   }

   public void setDamage(float value) {
      this.entityData.set(DAMAGE, value);
   }

   public int getBombType() {
      return (Integer)this.entityData.get(BOMB_TYPE);
   }

   public void setBombType(int value) {
      this.entityData.set(BOMB_TYPE, value);
   }

   public int getExplosion() {
      return (Integer)this.entityData.get(EXPLOSION);
   }

   public void setExplosion(int value) {
      this.entityData.set(EXPLOSION, value);
   }

   public boolean getCarrier() {
      return (Boolean)this.entityData.get(CARRIER);
   }

   public void setCarrier(boolean value) {
      this.entityData.set(CARRIER, value);
   }

   protected boolean canHitEntity(Entity entity) {
      boolean var10000;
      if (entity instanceof LivingEntity living) {
         if (this.livingEntityPredicate.test(living)) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   protected void onHitEntity(EntityHitResult result) {
      Entity var3 = result.getEntity();
      if (var3 instanceof LivingEntity living) {
         Level var4 = this.level();
         if (var4 instanceof ServerLevel serverLevel) {
            Utilities.explodeCircle(serverLevel, this.getOwner(), result.getEntity().getOnPos(), (double)this.getExplosion(), this.getDamage(), (double)8.0F, (entity) -> {
               boolean var10000;
               if (entity instanceof LivingEntity livingEntity) {
                  if (this.livingEntityPredicate.test(livingEntity)) {
                     var10000 = true;
                     return var10000;
                  }
               }

               var10000 = false;
               return var10000;
            });
            if (this.getBombType() == 1) {
               living.setSecondsOnFire(20);
               Utilities.convertBlocks(serverLevel, this.getOwner(), result.getEntity().getOnPos(), (double)this.getExplosion(), Blocks.FIRE.defaultBlockState());
            }

            if (this.getBombType() == 2) {
               Utilities.convertBlocks(serverLevel, this.getOwner(), result.getEntity().getOnPos(), (double)this.getExplosion(), ((LiquidBlock)Sblocks.BILE.get()).defaultBlockState());
            }

            if (this.getBombType() == 4) {
               NukeEntity nukeEntity = new NukeEntity((EntityType)Sentities.NUKE.get(), this.level());
               nukeEntity.setInitRange(1.0F);
               nukeEntity.setRange((float)((Double)SConfig.SERVER.nuke_range.get() * (double)1.0F));
               nukeEntity.setInitDuration(0);
               nukeEntity.setDuration((Integer)SConfig.SERVER.nuke_time.get());
               nukeEntity.setDamage((float)((Double)SConfig.SERVER.nuke_damage.get() * (double)1.0F));
               nukeEntity.livingEntityPredicate = this.livingEntityPredicate;
               nukeEntity.setPos(living.getX(), living.getY(), living.getZ());
               this.level().addFreshEntity(nukeEntity);
            }
         }
      }

      this.discard();
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 70 == 0) {
         this.playSound((SoundEvent)Ssounds.FALLING_BOMB.get());
      }

      if (this.getBombType() == 1) {
         for(int i = 0; i < 360; ++i) {
            if (i % 40 == 0) {
               this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), Math.cos((double)i) * (double)0.25F, (double)0.25F, Math.sin((double)i) * (double)0.25F);
            }
         }
      }

      this.aimForTarget();
   }

   private float calculate(Entity entity, Vec3 entity1) {
      float f = (float)(entity.getX() - entity1.x);
      float f2 = (float)(entity.getZ() - entity1.z);
      return Mth.sqrt(f * f + f2 * f2);
   }

   private void aimForTarget() {
      if (this.target != null && this.getDeltaMovement().y < (double)0.0F) {
         Vec3 vec3 = this.getDeltaMovement();
         Vec3 vec31 = new Vec3(this.target.x - this.getX(), (double)0.0F, this.target.z - this.getZ());
         if (vec31.lengthSqr() > 1.0E-7) {
            vec31 = vec31.normalize().scale(0.05);
         }

         if (this.target != null && this.calculate(this, this.target) < 3.5F) {
            this.setDeltaMovement(new Vec3(vec31.x, vec3.y, vec31.z));
         } else {
            this.setDeltaMovement(vec3.add(vec31.x, (double)0.0F, vec31.z));
         }
      }

   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.GENERIC_EXPLODE;
   }

   protected void onHitBlock(BlockHitResult result) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel serverLevel) {
         Utilities.explodeCircle(serverLevel, this.getOwner(), result.getBlockPos(), (double)this.getExplosion(), this.getDamage(), (double)(Integer)SConfig.SERVER.calamity_bd.get(), (entity) -> {
            boolean var10000;
            if (entity instanceof LivingEntity livingEntity) {
               if (this.livingEntityPredicate.test(livingEntity)) {
                  var10000 = true;
                  return var10000;
               }
            }

            var10000 = false;
            return var10000;
         });
         if (this.getBombType() == 1) {
            Utilities.convertBlocks(serverLevel, this.getOwner(), result.getBlockPos(), (double)this.getExplosion(), Blocks.FIRE.defaultBlockState());
         }

         if (this.getBombType() == 2) {
            Utilities.convertBlocks(serverLevel, this.getOwner(), result.getBlockPos(), (double)this.getExplosion(), ((LiquidBlock)Sblocks.BILE.get()).defaultBlockState());
         }

         if (this.getBombType() == 3) {
            this.summonAcid(this.getX(), this.getY() - (double)(this.getExplosion() - 2), this.getZ(), this.getExplosion());
         }

         if (this.getBombType() == 4) {
            NukeEntity nukeEntity = new NukeEntity((EntityType)Sentities.NUKE.get(), this.level());
            nukeEntity.setInitRange(1.0F);
            nukeEntity.setRange((float)((Double)SConfig.SERVER.nuke_range.get() * (double)1.0F));
            nukeEntity.setInitDuration(0);
            nukeEntity.setDuration((Integer)SConfig.SERVER.nuke_time.get());
            nukeEntity.setDamage((float)((Double)SConfig.SERVER.nuke_damage.get() * (double)1.0F));
            nukeEntity.livingEntityPredicate = this.livingEntityPredicate;
            nukeEntity.setPos((double)result.getBlockPos().getX(), (double)(result.getBlockPos().getY() - this.getExplosion() + 1), (double)result.getBlockPos().getZ());
            this.level().addFreshEntity(nukeEntity);
         }

         if (this.getCarrier()) {
            this.SummonInfected(serverLevel);
         }

         this.playSound(SoundEvents.GENERIC_EXPLODE);
      }

      this.discard();
   }

   private void summonAcid(double x, double y, double z, int range) {
      AreaEffectCloud cloud = new AreaEffectCloud(this.level(), x, y, z);
      cloud.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 300, 1));
      cloud.setRadius((float)range);
      this.level().addFreshEntity(cloud);
   }

   public void onSyncedDataUpdated(EntityDataAccessor accessor) {
      super.onSyncedDataUpdated(accessor);
      if (accessor.equals(CARRIER)) {
         this.refreshDimensions();
      }

   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.getCarrier() ? super.getDimensions(pose).scale(2.0F) : super.getDimensions(pose);
   }

   private void SummonInfected(ServerLevel serverLevel) {
      List<? extends String> values = (List)SConfig.SERVER.howit_summmons.get();
      int randomIndex = this.random.nextInt(values.size());
      ResourceLocation randomElement1 = new ResourceLocation((String)values.get(randomIndex));
      EntityType<?> randomElement = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(randomElement1);
      Mob waveentity = (Mob)randomElement.create(this.level());
      if (waveentity != null) {
         waveentity.setPos(this.getX(), this.getY(), this.getZ());
         waveentity.finalizeSpawn(serverLevel, this.level().getCurrentDifficultyAt(new BlockPos((int)this.getX(), (int)this.getY(), (int)this.getZ())), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
         serverLevel.addFreshEntity(waveentity);
      }

   }

   static {
      DAMAGE = SynchedEntityData.defineId(FleshBomb.class, EntityDataSerializers.FLOAT);
      BOMB_TYPE = SynchedEntityData.defineId(FleshBomb.class, EntityDataSerializers.INT);
      EXPLOSION = SynchedEntityData.defineId(FleshBomb.class, EntityDataSerializers.INT);
      CARRIER = SynchedEntityData.defineId(FleshBomb.class, EntityDataSerializers.BOOLEAN);
   }

   public static enum BombType {
      BASIC(0),
      FLAME(1),
      BILE(2),
      ACID(3),
      NUCLEAR(4);

      private final int value;

      private BombType(int value1) {
         this.value = value1;
      }

      public int getValue() {
         return this.value;
      }

      // $FF: synthetic method
      private static BombType[] $values() {
         return new BombType[]{BASIC, FLAME, BILE, ACID, NUCLEAR};
      }
   }
}
