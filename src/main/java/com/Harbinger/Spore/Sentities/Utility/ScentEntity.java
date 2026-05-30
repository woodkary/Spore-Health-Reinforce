package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Organoids.Womb;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public class ScentEntity extends UtilityEntity {
   private static final EntityDataAccessor OVERCHARGED;
   private static final EntityDataAccessor DISSIPATE;
   private static final EntityDataAccessor SUMMON;
   public List<String> idList = new ArrayList<>();

   public ScentEntity(EntityType mob, Level level) {
      super(mob, level);
   }

   public boolean isInvulnerable() {
      return true;
   }

   public void tick() {
      if (this.isAlive()) {
         if (this.getDissipate() == 1) {
            this.addToTheList(this);
         }

         this.setDissipate(this.getDissipate() + 1);
         if (this.getDissipate() >= (Integer)SConfig.SERVER.scent_life.get()) {
            this.discard();
         }

         if ((Boolean)SConfig.SERVER.scent_summon.get()) {
            this.setSummon(this.getSummon() + 1);
            if (this.getSummon() >= (Integer)SConfig.SERVER.scent_summon_cooldown.get() && !this.level().isClientSide && (this.getOvercharged() || this.checkForNonInfected(this))) {
               Womb womb = this.getNearbyWombs();
               if (womb != null) {
                  womb.setBiomass(womb.getBiomass() + (Integer)SConfig.SERVER.reconstructor_assimilation.get());
               }

               this.Summon(this);
               this.setSummon(0);
            }
         }
      }

      super.tick();
   }

   private Womb getNearbyWombs() {
      List<Womb> wombs = this.level().getEntitiesOfClass(Womb.class, this.getBoundingBox().inflate((double)16.0F));
      return wombs.isEmpty() ? null : (Womb)wombs.get(this.random.nextInt(wombs.size()));
   }

   boolean checkForNonInfected(Entity entity) {
      AABB boundingBox = entity.getBoundingBox().inflate((double)16.0F);

      for(Entity en : entity.level().getEntities(entity, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
         if (en instanceof LivingEntity livingEntity) {
            if (this.TARGET_SELECTOR.test(livingEntity)) {
               return true;
            }
         }
      }

      return false;
   }

   void addToTheList(Entity entity) {
      AABB boundingBox = entity.getBoundingBox().inflate((double)100.0F);

      for(Entity en : entity.level().getEntities(entity, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
         for(String entry : (List<String>)SConfig.SERVER.inf_human_conv.get()) {
            String[] parts = entry.split("\\|");
            if (parts.length >= 2 && Objects.equals(en.getEncodeId(), parts[0])) {
               this.idList.add(parts[1]);
            }
         }
      }

   }

   public boolean getOvercharged() {
      return (Boolean)this.entityData.get(OVERCHARGED);
   }

   public void setOvercharged(boolean b) {
      this.entityData.set(OVERCHARGED, b);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("overcharged", (Boolean)this.entityData.get(OVERCHARGED));
      tag.putInt("summon", (Integer)this.entityData.get(SUMMON));
      tag.putInt("dissipate", (Integer)this.entityData.get(DISSIPATE));
      ListTag teamTag = new ListTag();

      for(String member : this.idList) {
         teamTag.add(StringTag.valueOf(member));
      }

      tag.put("entities", teamTag);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(OVERCHARGED, tag.getBoolean("overcharged"));
      this.entityData.set(SUMMON, tag.getInt("summon"));
      this.entityData.set(DISSIPATE, tag.getInt("dissipate"));
      this.idList.clear();
      ListTag teamTag = tag.getList("entities", 8);

      for(int l = 0; l < teamTag.size(); ++l) {
         this.idList.add(teamTag.getString(l));
      }

   }

   public void setSummon(int val) {
      this.entityData.set(SUMMON, val);
   }

   public void setDissipate(int val) {
      this.entityData.set(DISSIPATE, val);
   }

   public int getSummon() {
      return (Integer)this.entityData.get(SUMMON);
   }

   public int getDissipate() {
      return (Integer)this.entityData.get(DISSIPATE);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(OVERCHARGED, false);
      this.entityData.define(SUMMON, 0);
      this.entityData.define(DISSIPATE, 0);
   }

   public void setNoGravity(boolean ignored) {
      super.setNoGravity(true);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)1.0F);
   }

   public void aiStep() {
      super.aiStep();
      if (!this.onGround()) {
         this.getDeltaMovement().add((double)0.0F, -0.01, (double)0.0F);
      }

      if ((Boolean)SConfig.SERVER.scent_particles.get()) {
         int i = Mth.floor(this.getX());
         int j = Mth.floor(this.getY());
         int k = Mth.floor(this.getZ());
         Level world = this.level();
         RandomSource randomSource = this.random;
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(int l = 0; l < 14; ++l) {
            blockpos$mutableblockpos.set(i + Mth.nextInt(randomSource, -6, 6), j + Mth.nextInt(randomSource, -6, 6), k + Mth.nextInt(randomSource, -6, 6));
            BlockState blockstate = world.getBlockState(blockpos$mutableblockpos);
            if (!blockstate.isSolidRender(world, blockpos$mutableblockpos)) {
               world.addParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get(), (double)blockpos$mutableblockpos.getX() + randomSource.nextDouble(), (double)blockpos$mutableblockpos.getY() + randomSource.nextDouble(), (double)blockpos$mutableblockpos.getZ() + randomSource.nextDouble(), (double)0.0F, 0.1, (double)0.0F);
               if (this.getOvercharged()) {
                  world.addParticle((ParticleOptions)Sparticles.BLOOD_PARTICLE.get(), (double)blockpos$mutableblockpos.getX() + randomSource.nextDouble(), (double)blockpos$mutableblockpos.getY() + randomSource.nextDouble(), (double)blockpos$mutableblockpos.getZ() + randomSource.nextDouble(), (double)0.0F, 0.1, (double)0.0F);
               }
            }
         }
      }

   }

   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   public void Summon(LivingEntity entity) {
      ServerLevelAccessor world = (ServerLevelAccessor)entity.level();
      Level level = entity.level();
      Random rand = new Random();
      int d = this.random.nextInt(0, 3);
      int r = this.random.nextInt(-12, 12);
      int c = this.random.nextInt(-12, 12);
      List<? extends String> ev = (List)SConfig.SERVER.inf_summon.get();
      boolean bigger = this.idList.size() > ev.size();
      if (world.isEmptyBlock(new BlockPos((int)this.getX() + r, (int)this.getY() + d, (int)this.getZ() + c))) {
         for(int i = 0; i < 1; ++i) {
            int randomIndex = rand.nextInt(bigger ? this.idList.size() : ev.size());
            ResourceLocation randomElement1 = new ResourceLocation(bigger ? (String)this.idList.get(randomIndex) : (String)ev.get(randomIndex));
            EntityType<?> randomElement = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(randomElement1);
            Mob waveentity = (Mob)randomElement.create(level);

            assert waveentity != null;

            waveentity.setPos(entity.getX() + (double)r, entity.getY() + (double)0.5F + (double)d, entity.getZ() + (double)c);
            waveentity.finalizeSpawn(world, level.getCurrentDifficultyAt(new BlockPos((int)entity.getX(), (int)entity.getY(), (int)entity.getZ())), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
            if (this.getOvercharged()) {
               List<? extends String> buffer = (List)SConfig.SERVER.scent_effects_buff.get();
               int randomInt = this.random.nextInt(buffer.size());
               if (waveentity instanceof Infected) {
                  Infected infected = (Infected)waveentity;
                  int k = (Integer)SConfig.SERVER.scent_kills.get();
                  infected.setKills(this.random.nextInt(k, k + 3));
                  infected.setEvoPoints(this.random.nextInt(k, k + 3));
                  infected.setEvolution((Integer)SConfig.SERVER.evolution_age_human.get() / 2);
                  infected.setLinked(true);
               }

               ResourceLocation randomElement2 = new ResourceLocation((String)buffer.get(randomInt));
               MobEffect randomElement3 = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(randomElement2);

               assert randomElement3 != null;

               waveentity.addEffect(new MobEffectInstance(randomElement3, 3600, 0));
            }

            level.addFreshEntity(waveentity);
         }
      }

   }

   public boolean addEffect(MobEffectInstance p_182397_, @Nullable Entity p_182398_) {
      return false;
   }

   static {
      OVERCHARGED = SynchedEntityData.defineId(ScentEntity.class, EntityDataSerializers.BOOLEAN);
      DISSIPATE = SynchedEntityData.defineId(ScentEntity.class, EntityDataSerializers.INT);
      SUMMON = SynchedEntityData.defineId(ScentEntity.class, EntityDataSerializers.INT);
   }
}
