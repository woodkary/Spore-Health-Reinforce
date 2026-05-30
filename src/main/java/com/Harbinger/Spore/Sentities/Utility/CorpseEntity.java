package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.Harbinger.Spore.Sitems.Reaver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CorpseEntity extends Entity {
   private static final EntityDataAccessor TYPE;
   private static final EntityDataAccessor OWNER_ADA;
   private static final EntityDataAccessor COLOR;
   private static final EntityDataAccessor TIMER;
   private static final EntityDataAccessor INFLATION;
   private final SimpleContainer inventory = new SimpleContainer(20);

   public CorpseEntity(EntityType p_19870_, Level p_19871_) {
      super(p_19870_, p_19871_);
   }

   protected void defineSynchedData() {
      this.entityData.define(TYPE, 0);
      this.entityData.define(OWNER_ADA, false);
      this.entityData.define(COLOR, 0);
      this.entityData.define(TIMER, 0);
      this.entityData.define(INFLATION, 1.0F);
   }

   public SimpleContainer getInventory() {
      return this.inventory;
   }

   public void addToInventory(ItemStack stack) {
      this.inventory.addItem(stack);
   }

   protected void readAdditionalSaveData(CompoundTag compoundTag) {
      this.setCorpseType(compoundTag.getInt("corpse_type"));
      this.setColor(compoundTag.getInt("color"));
      this.entityData.set(TIMER, compoundTag.getInt("timer"));
      this.setOwnerAda(compoundTag.getBoolean("owner_ada"));
      this.setInflation(compoundTag.getFloat("inflation"));
      ListTag listtag = compoundTag.getList("Items", 10);

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         int j = (compoundtag.getByte("Slot") & 255) % this.inventory.getContainerSize();
         this.inventory.setItem(j, ItemStack.of(compoundtag));
      }

   }

   protected void addAdditionalSaveData(CompoundTag compoundTag) {
      compoundTag.putInt("corpse_type", this.getCorpseType());
      compoundTag.putInt("color", this.getColor());
      compoundTag.putInt("timer", (Integer)this.entityData.get(TIMER));
      compoundTag.putBoolean("owner_ada", this.getOwnerAda());
      compoundTag.putFloat("inflation", this.getInflation());
      ListTag listtag = new ListTag();

      for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
         ItemStack itemstack = this.inventory.getItem(i);
         if (!itemstack.isEmpty()) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putByte("Slot", (byte)i);
            listtag.add(itemstack.save(compoundtag));
         }
      }

      compoundTag.put("Items", listtag);
   }

   public void setCorpseType(int e) {
      this.entityData.set(TYPE, e);
   }

   public int getCorpseType() {
      return (Integer)this.entityData.get(TYPE);
   }

   public void setColor(int e) {
      this.entityData.set(COLOR, e);
   }

   public int getColor() {
      return (Integer)this.entityData.get(COLOR);
   }

   public void setOwnerAda(boolean e) {
      this.entityData.set(OWNER_ADA, e);
   }

   public boolean getOwnerAda() {
      return (Boolean)this.entityData.get(OWNER_ADA);
   }

   public int getTimer() {
      return (Integer)this.entityData.get(TIMER);
   }

   public void setInflation(float e) {
      this.entityData.set(INFLATION, e);
   }

   public float getInflation() {
      return (Float)this.entityData.get(INFLATION);
   }

   public InteractionResult interactAt(Player player, Vec3 hitVec, InteractionHand hand) {
      if (!this.level().isClientSide && player.getItemInHand(hand).getItem() instanceof Reaver && Math.random() < (double)0.3F) {
         this.summonItem(HitboxesForParts.byId(this.getCorpseType()).getCalamityType().getStack());
         this.playSound((SoundEvent)Ssounds.REAVER_REAVE.get());
      }

      this.createLoot();
      return InteractionResult.SUCCESS;
   }

   public boolean hurt(DamageSource p_19946_, float p_19947_) {
      this.createLoot();
      return super.hurt(p_19946_, p_19947_);
   }

   private void createLoot() {
      if (!this.level().isClientSide) {
         for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) {
               this.summonItem(stack);
               this.inventory.setItem(i, ItemStack.EMPTY);
               break;
            }
         }

         if (this.inventory.isEmpty()) {
            this.summonItem(HitboxesForParts.byId(this.getCorpseType()).getCalamityType().getStack());
            this.discard();
         }
      }

   }

   public boolean isPickable() {
      return true;
   }

   public void summonItem(ItemStack stack) {
      ItemEntity entity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
      this.level().addFreshEntity(entity);
   }

   public boolean mayInteract(Level p_146843_, BlockPos p_146844_) {
      return true;
   }

   public void tick() {
      super.tick();
      if (!this.isNoGravity()) {
         this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, -0.05, (double)0.0F));
      }

      this.move(MoverType.SELF, this.getDeltaMovement());
      if (!this.onGround() && this.getDeltaMovement().lengthSqr() > 1.0E-4) {
         Vec3 motion = this.getDeltaMovement();
         double horizSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
         this.setYRot((float)(Mth.atan2(motion.x, motion.z) * (180D / Math.PI)));
         this.setXRot((float)(Mth.atan2(motion.y, horizSpeed) * (180D / Math.PI)));
         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
      }

      if (this.onGround()) {
         Vec3 motion = this.getDeltaMovement();
         if (motion.lengthSqr() > 0.01) {
            this.setDeltaMovement(motion.multiply((double)0.5F, (double)-0.5F, (double)0.5F));
         } else {
            this.setDeltaMovement(Vec3.ZERO);
         }
      }

      if (this.tickCount % 20 == 0) {
         this.tickTimer();
      }

   }

   private void tickTimer() {
      int time = (Integer)this.entityData.get(TIMER);
      if (time < 300) {
         ++time;
         this.entityData.set(TIMER, time);
      } else {
         this.summonBiomass();
      }

   }

   private void summonBiomass() {
      if (!this.level().isClientSide) {
         if (Math.random() < 0.1) {
            Mound mound = new Mound((EntityType)Sentities.MOUND.get(), this.level());
            mound.moveTo(this.position());
            mound.tickEmerging();
            this.level().addFreshEntity(mound);
         } else {
            AABB aabb = this.getBoundingBox().inflate((double)1.0F);

            for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
               BlockState blockState = this.level().getBlockState(blockpos);
               if (blockState.isAir() && Math.random() < (double)0.2F) {
                  FallingBlockEntity.fall(this.level(), blockpos, ((Block)Sblocks.REMAINS.get()).defaultBlockState());
               }
            }
         }

         this.discard();
      }
   }

   public boolean isNoGravity() {
      return false;
   }

   public boolean shouldBeSaved() {
      return true;
   }

   public HitboxesForParts getVariant() {
      return HitboxesForParts.byId(this.getCorpseType() & 255);
   }

   public void onSyncedDataUpdated(EntityDataAccessor key) {
      super.onSyncedDataUpdated(key);
      if (TYPE.equals(key)) {
         this.refreshDimensions();
         this.setBoundingBox(this.getDimensions(this.getPose()).makeBoundingBox(this.position()));
      }

   }

   public EntityDimensions getDimensions(Pose pose) {
      HitboxesForParts parts = this.getVariant();
      return EntityDimensions.scalable(parts.getWidth(), parts.getHeight()).scale(this.getInflation());
   }

   static {
      TYPE = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.INT);
      OWNER_ADA = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.BOOLEAN);
      COLOR = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.INT);
      TIMER = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.INT);
      INFLATION = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
   }
}
