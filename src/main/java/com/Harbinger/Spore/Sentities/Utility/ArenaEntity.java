package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Organoids.Usurper;
import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

public class ArenaEntity extends UtilityEntity {
   public static final EntityDataAccessor BORROW;
   public static final EntityDataAccessor EMERGE;
   public static final EntityDataAccessor WAVE_SIZE;
   public static final EntityDataAccessor WAVE_LEVEL;
   public static final EntityDataAccessor SPECIAL_SPAWNS;
   public static final EntityDataAccessor START;
   private List<Entity> waveHosts = new ArrayList<>();
   public List<FleshBomb.BombType> bombTypes = new ArrayList<>() {
      {
         this.add(FleshBomb.BombType.BASIC);
         this.add(FleshBomb.BombType.FLAME);
         this.add(FleshBomb.BombType.BILE);
         this.add(FleshBomb.BombType.ACID);
      }
   };
   public List<Enchantment> enchantmentList = new ArrayList<>() {
      {
         this.add((Enchantment)Senchantments.CRYOGENIC_ASPECT.get());
         this.add((Enchantment)Senchantments.SYMBIOTIC_RECONSTITUTION.get());
         this.add((Enchantment)Senchantments.CORROSIVE_POTENCY.get());
         this.add((Enchantment)Senchantments.GASTRIC_SPEWAGE.get());
      }
   };

   public ArenaEntity(EntityType type, Level level) {
      super(type, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BORROW, 0);
      this.entityData.define(EMERGE, 0);
      this.entityData.define(WAVE_SIZE, 0);
      this.entityData.define(WAVE_LEVEL, 0);
      this.entityData.define(SPECIAL_SPAWNS, 0);
      this.entityData.define(START, false);
   }

   public void readAdditionalSaveData(CompoundTag compoundTag) {
      super.readAdditionalSaveData(compoundTag);
      this.setWaveSize(compoundTag.getInt("size"));
      this.setWaveLevel(compoundTag.getInt("level"));
      this.setAmountOfSpecialSpawns(compoundTag.getInt("special"));
      this.startWave(compoundTag.getBoolean("start"));
   }

   public void addAdditionalSaveData(CompoundTag compoundTag) {
      super.addAdditionalSaveData(compoundTag);
      compoundTag.putInt("size", this.getWaveSize());
      compoundTag.putInt("level", this.getWaveLevel());
      compoundTag.putInt("special", this.getSpecialSpawns());
      compoundTag.putBoolean("start", this.isWaveActive());
   }

   public void setWaveSize(int size) {
      this.entityData.set(WAVE_SIZE, size);
   }

   public void setWaveLevel(int level) {
      this.entityData.set(WAVE_LEVEL, level);
   }

   public void setAmountOfSpecialSpawns(int amount) {
      this.entityData.set(SPECIAL_SPAWNS, amount);
   }

   public int getWaveSize() {
      return (Integer)this.entityData.get(WAVE_SIZE);
   }

   public int getWaveLevel() {
      return (Integer)this.entityData.get(WAVE_LEVEL);
   }

   public int getSpecialSpawns() {
      return (Integer)this.entityData.get(SPECIAL_SPAWNS);
   }

   public void startWave(boolean value) {
      this.entityData.set(START, value);
   }

   public boolean isWaveActive() {
      return (Boolean)this.entityData.get(START);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)1.0F).add(Attributes.MOVEMENT_SPEED, 0.1).add(Attributes.ATTACK_DAMAGE, (double)1.0F).add(Attributes.ARMOR, (double)1.0F).add(Attributes.FOLLOW_RANGE, (double)8.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public boolean isEmerging() {
      return (Integer)this.entityData.get(EMERGE) > 0;
   }

   public void tickEmerging() {
      int emerging = (Integer)this.entityData.get(EMERGE);
      if (emerging > 60) {
         this.recalculateHosts();
         emerging = -1;
      }

      this.entityData.set(EMERGE, emerging + 1);
   }

   public boolean isBurrowing() {
      return (Integer)this.entityData.get(BORROW) > 0;
   }

   public void tickBurrowing() {
      int burrowing = (Integer)this.entityData.get(BORROW);
      if (burrowing > 60) {
         burrowing = -1;
         if (this.isWaveActive()) {
            this.dropLoot();
         }

         this.discard();
      }

      this.entityData.set(BORROW, burrowing + 1);
   }

   public int getEmerge() {
      return (Integer)this.entityData.get(EMERGE);
   }

   public int getBorrow() {
      return (Integer)this.entityData.get(BORROW);
   }

   public boolean hurt(DamageSource source, float p_21017_) {
      return source.is(DamageTypes.FELL_OUT_OF_WORLD) ? super.hurt(source, p_21017_) : false;
   }

   public void recalculateHosts() {
      this.waveHosts.clear();
      this.waveHosts = this.level().getEntities(this, this.getBoundingBox().inflate((double)16.0F), (entityx) -> entityx instanceof LivingEntity && !(entityx instanceof UtilityEntity) && !(entityx instanceof Infected));
      if (!this.waveHosts.isEmpty()) {
         if (!this.isWaveActive()) {
            this.compareEntity(this.waveHosts);
         }

         for(Entity entity : this.waveHosts) {
            if (entity.getY() > this.getY() + (double)3.0F && Math.random() < (double)0.1F) {
               this.summonUsurper();
            }
         }
      }

   }

   public boolean removeWhenFarAway(double p_21542_) {
      return false;
   }

   public void compareEntity(List<Entity> entities) {
      for(Entity entity : entities) {
         if (entity instanceof LivingEntity living) {
            for(int i = 0; i < living.getArmorValue(); ++i) {
               if (i % 5 == 0) {
                  this.setWaveSize(this.getWaveSize() + 1);
               }

               if (i % 7 == 0) {
                  this.setWaveLevel(this.getWaveLevel() + 1);
               }
            }

            for(int i = 0; (float)i < living.getMaxHealth(); ++i) {
               if (i % 4 == 0) {
                  this.setWaveSize(this.getWaveSize() + 1);
               }

               if (i % 50 == 0) {
                  this.setWaveLevel(this.getWaveLevel() + 1);
               }
            }

            if (living.hasEffect((MobEffect)Seffects.SYMBIOSIS.get())) {
               this.setAmountOfSpecialSpawns(this.getSpecialSpawns() + 1);
            }

            if (living instanceof InventoryCarrier carrier) {
               int l = 0;
               int f = 0;

               for(int e = 0; e < carrier.getInventory().getContainerSize(); ++e) {
                  ItemStack stack = carrier.getInventory().getItem(e);
                  if (stack.getItem().isEdible()) {
                     l += stack.getCount();
                  }

                  if (l % 32 == 0) {
                     this.setAmountOfSpecialSpawns(this.getSpecialSpawns() + 1);
                  }
               }

               for(int e = 0; e < carrier.getInventory().getContainerSize(); ++e) {
                  ItemStack stack = carrier.getInventory().getItem(e);

                  for(Enchantment enchantment : this.enchantmentList) {
                     if (stack.getEnchantmentLevel(enchantment) > 0) {
                        f += stack.getCount();
                     }
                  }

                  if (f % 3 == 0) {
                     this.setAmountOfSpecialSpawns(this.getSpecialSpawns() + 1);
                  }
               }
            }

            this.startWave(true);
         }
      }

   }

   public void summonVerva(boolean special, List mob) {
      int X = this.random.nextInt(-16, 16);
      int Z = this.random.nextInt(-16, 16);
      String creature = (String)mob.get(this.random.nextInt(mob.size()));
      Verwa verva = new Verwa((EntityType)Sentities.VERVA.get(), this.level());
      verva.randomTeleport(this.getX() + (double)X, this.getY(), this.getZ() + (double)Z, false);
      verva.setStoredMob(creature);
      verva.tickEmerging();
      this.level().addFreshEntity(verva);
      if (special) {
         this.setAmountOfSpecialSpawns(this.getSpecialSpawns() - 1);
      } else {
         this.setWaveSize(this.getWaveSize() - 1);
      }

      if (this.getWaveLevel() >= 2 && Math.random() < (double)((float)(this.getWaveLevel() - 1) * 0.05F)) {
         this.summonBomb();
      }

   }

   public void summonUsurper() {
      int X = this.random.nextInt(-16, 16);
      int Z = this.random.nextInt(-16, 16);
      Usurper verva = new Usurper((EntityType)Sentities.USURPER.get(), this.level());
      verva.randomTeleport(this.getX() + (double)X, this.getY(), this.getZ() + (double)Z, false);
      verva.tickEmerging();
      this.level().addFreshEntity(verva);
   }

   public void summonBomb() {
      int X = this.random.nextInt(-32, 32);
      int Z = this.random.nextInt(-32, 32);
      FleshBomb.BombType type = (FleshBomb.BombType)this.bombTypes.get(this.random.nextInt(this.bombTypes.size()));
      FleshBomb verva = new FleshBomb(this.level(), this, 10.0F, type, this.random.nextInt(2, 5));
      verva.setLivingEntityPredicate(Utilities.TARGET_SELECTOR_PREDICATE);
      verva.moveTo(this.getX() + (double)X, this.getY() + (double)100.0F, this.getZ() + (double)Z);
      this.level().addFreshEntity(verva);
   }

   public Map getWaveSpawns() {
      Map<Integer, List<? extends String>> values = new HashMap();
      values.put(0, (List)SConfig.DATAGEN.raid_level_1.get());
      values.put(1, (List)SConfig.DATAGEN.raid_level_2.get());
      values.put(2, (List)SConfig.DATAGEN.raid_level_3.get());
      return values;
   }

   public void calculateSummons() {
      int e = this.getWaveSize() > 3 ? this.random.nextInt(4) : this.getWaveSize();
      int wave = Math.min(this.getWaveLevel(), 2);
      if (this.getWaveSize() <= 0 && this.checkForInfected() && this.isWaveActive()) {
         this.tickBurrowing();
      } else {
         for(int i = 0; i < e; ++i) {
            boolean special = this.getSpecialSpawns() > 0 && Math.random() < (double)0.1F;
            this.summonVerva(special, special ? (List)SConfig.DATAGEN.special.get() : (List)this.getWaveSpawns().get(wave));
         }

         this.playSound(SoundEvents.BELL_RESONATE);
      }
   }

   public boolean checkForInfected() {
      AABB aabb = this.getBoundingBox().inflate((double)8.0F);
      List<Entity> list = this.level().getEntities(this, aabb, (entity) -> (entity instanceof Infected || entity instanceof UtilityEntity) && !(entity instanceof ArenaEntity));
      return list.size() < 4;
   }

   public void tick() {
      super.tick();
      if (this.isBurrowing()) {
         this.tickBurrowing();
      }

      if (this.isEmerging()) {
         this.tickEmerging();
      }

      if (this.tickCount % 300 == 0) {
         this.calculateSummons();
      }

      if (this.tickCount % 40 == 0) {
         this.recalculateHosts();
      }

   }

   public void dropLoot() {
      for(String string : (List<String>)SConfig.DATAGEN.drops.get()) {
         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
         if (item != null) {
            int i = this.getWaveLevel() > 0 ? this.random.nextInt(this.getWaveLevel(), 3 * this.getWaveLevel()) : 1;
            if (Math.random() < (double)(0.2F * (float)Math.min(1, this.getWaveLevel()))) {
               ItemStack itemStack = new ItemStack(item);
               itemStack.setCount(Math.min(itemStack.getMaxStackSize(), i));
               ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack);
               this.level().addFreshEntity(itemEntity);
            }
         }
      }

   }

   public boolean canBeSeenAsEnemy() {
      return false;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean isPushedByFluid(FluidType type) {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
   }

   public boolean isInvulnerable() {
      return true;
   }

   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (player.getItemInHand(hand).getItem() == Sitems.VIGIL_EYE.get()) {
         this.discard();
         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract(player, hand);
      }
   }

   static {
      BORROW = SynchedEntityData.defineId(ArenaEntity.class, EntityDataSerializers.INT);
      EMERGE = SynchedEntityData.defineId(ArenaEntity.class, EntityDataSerializers.INT);
      WAVE_SIZE = SynchedEntityData.defineId(ArenaEntity.class, EntityDataSerializers.INT);
      WAVE_LEVEL = SynchedEntityData.defineId(ArenaEntity.class, EntityDataSerializers.INT);
      SPECIAL_SPAWNS = SynchedEntityData.defineId(ArenaEntity.class, EntityDataSerializers.INT);
      START = SynchedEntityData.defineId(ArenaEntity.class, EntityDataSerializers.BOOLEAN);
   }
}
