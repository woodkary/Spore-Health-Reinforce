package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.BaseEntities.IkUtil.IkLeviLeg;
import com.Harbinger.Spore.Sentities.Calamities.Leviathan;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.fluids.FluidType;

public class LeviathanMultipart extends LivingEntity implements TrueCalamity, ColdWeakness {
   private double prevHeight = (double)0.0F;
   private int headEntityId = -1;
   private final IkLeviLeg[] legs;
   private static final EntityDataAccessor CHILD_UUID;
   private static final EntityDataAccessor PARENT_UUID;
   private static final EntityDataAccessor COLOR;
   private static final EntityDataAccessor PARENT_ID;
   private static final EntityDataAccessor ADAPTED;
   private static final EntityDataAccessor IS_TAIL;

   public LeviathanMultipart(EntityType p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
      IkLeviLeg frontRightLeg = new IkLeviLeg(this, 4, LEG_POSITIONS.FRONT_RIGHT_TENTACLE.bodySet, LEG_POSITIONS.FRONT_RIGHT_TENTACLE.offset, 4.0F);
      IkLeviLeg frontLeftLeg = new IkLeviLeg(this, 4, LEG_POSITIONS.FRONT_LEFT_TENTACLE.bodySet, LEG_POSITIONS.FRONT_LEFT_TENTACLE.offset, 4.0F);
      IkLeviLeg backRightLeg = new IkLeviLeg(this, 4, LEG_POSITIONS.BACK_RIGHT_TENTACLE.bodySet, LEG_POSITIONS.BACK_RIGHT_TENTACLE.offset, 2.0F);
      IkLeviLeg backLeftLeg = new IkLeviLeg(this, 4, LEG_POSITIONS.BACK_LEFT_TENTACLE.bodySet, LEG_POSITIONS.BACK_LEFT_TENTACLE.offset, 2.0F);
      this.legs = new IkLeviLeg[]{frontLeftLeg, frontRightLeg, backLeftLeg, backRightLeg};
   }

   public IkLeviLeg[] getLegs() {
      return this.legs;
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.CALAMITY;
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.CALAMITY_DAMAGE.get();
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(CHILD_UUID, Optional.empty());
      this.entityData.define(PARENT_UUID, Optional.empty());
      this.entityData.define(COLOR, 0);
      this.entityData.define(PARENT_ID, -1);
      this.entityData.define(ADAPTED, false);
      this.entityData.define(IS_TAIL, false);
   }

   public boolean isAdapted() {
      return (Boolean)this.entityData.get(ADAPTED);
   }

   public Entity getChild() {
      UUID id = this.getChildId();
      if (id != null) {
         Level var3 = this.level();
         if (var3 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var3;
            return serverLevel.getEntity(id);
         }
      }

      return null;
   }

   public int getParentIntId() {
      return (Integer)this.entityData.get(PARENT_ID);
   }

   public void tick() {
      super.tick();

      for(IkLeviLeg leg : this.legs) {
         leg.refreshLegStandingPoint();
         leg.applyIK();
      }

      if (this.tickCount > 1) {
         Entity parent = this.getParentSafe();
         if (!this.level().isClientSide) {
            if (parent != null && !parent.isRemoved()) {
               label45: {
                  if (parent instanceof Leviathan) {
                     Leviathan leviathan = (Leviathan)parent;
                     if (!Objects.equals(leviathan.getChildId(), this.uuid)) {
                        break label45;
                     }
                  }

                  if (parent instanceof LivingEntity) {
                     LivingEntity living = (LivingEntity)parent;
                     this.hurtTime = living.hurtTime;
                     this.deathTime = living.deathTime;
                  }

                  return;
               }
            }

            this.remove(RemovalReason.DISCARDED);
         }
      }

   }

   public Vec3 tickMultipartPosition(int headId, Vec3 parentPos, float parentXRot, float parentYRot, float ourYRot, boolean doHeight) {
      double spacing = (double)(1.5F * this.getBbWidth());
      Vec3 buttOffset = this.calcOffsetVec((float)(-spacing), parentXRot, parentYRot);
      Vec3 targetPos = parentPos.add(buttOffset);
      Vec3 currentPos = this.position();
      Vec3 smoothedPos = currentPos.lerp(targetPos, (double)0.25F);
      Vec3 dir = this.position().subtract(targetPos);
      if (dir.length() > (double)5.0F) {
         this.setPos(targetPos);
      }

      double yOffset = (double)0.0F;
      if (doHeight) {
         double hgt = this.getLowPartHeight(targetPos.x, targetPos.y, targetPos.z) + this.getHighPartHeight(targetPos.x, targetPos.y, targetPos.z);
         if (Math.abs(hgt - this.prevHeight) > (double)0.2F) {
            this.prevHeight = hgt;
         }

         yOffset = Mth.clamp((double)this.getScale() * this.prevHeight, (double)-0.6F, (double)0.6F);
      }

      double dx = parentPos.x - smoothedPos.x;
      double dz = parentPos.z - smoothedPos.z;
      double horizontalDist = Math.sqrt(dx * dx + dz * dz);
      float targetYaw = (float)(Mth.atan2(dz, dx) * (double)(180F / (float)Math.PI)) - 90.0F;
      float smoothedYaw = this.limitAngle(this.getYRot(), targetYaw, 7.5F);
      float targetPitch = (float)(-Mth.atan2(yOffset, horizontalDist) * (double)(180F / (float)Math.PI));
      float smoothedPitch = this.limitAngle(this.getXRot(), targetPitch, 5.0F);
      double distanceToParent = this.position().distanceTo(parentPos);
      boolean disablePhysics = distanceToParent > (double)5.0F;
      Entity entity = this.getParentSafe();
      if (entity != null) {
         this.setNoGravity(disablePhysics || entity.isNoGravity());
         this.noPhysics = disablePhysics || entity.noPhysics;
         if (disablePhysics) {
            this.teleportTo(entity.getX(), entity.getY(), entity.getZ());
         }

         this.setDeltaMovement(disablePhysics ? entity.getDeltaMovement().multiply((double)1.0F, (double)0.0F, (double)1.0F) : entity.getDeltaMovement());
      }

      this.moveTo(smoothedPos.x, this.onGround() ? this.position().y : smoothedPos.y, smoothedPos.z, smoothedYaw, smoothedPitch);
      this.setYRot(smoothedYaw);
      this.setXRot(smoothedPitch);
      this.yHeadRot = smoothedYaw;
      this.headEntityId = headId;
      return smoothedPos;
   }

   private Vec3 calcOffsetVec(float offsetZ, float xRot, float yRot) {
      return (new Vec3((double)0.0F, (double)0.0F, (double)offsetZ)).xRot(xRot * ((float)Math.PI / 180F)).yRot(-yRot * ((float)Math.PI / 180F));
   }

   public boolean isInvulnerableTo(DamageSource source) {
      return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL);
   }

   public boolean isOpaqueBlockAt(double x, double y, double z) {
      if (this.noPhysics) {
         return false;
      } else {
         Vec3 pos = new Vec3(x, y, z);
         AABB box = AABB.ofSize(pos, (double)1.0F, 1.0E-6, (double)1.0F);
         return this.level().getBlockStates(box).filter(Predicate.not(BlockBehaviour.BlockStateBase::isAir)).anyMatch((state) -> state.isSuffocating(this.level(), BlockPos.containing(pos)) && Shapes.joinIsNotEmpty(state.getCollisionShape(this.level(), BlockPos.containing(pos)).move(pos.x, pos.y, pos.z), Shapes.create(box), BooleanOp.AND));
      }
   }

   public double getLowPartHeight(double x, double y, double z) {
      if (this.isFluidAt(x, y, z)) {
         return (double)0.0F;
      } else {
         double dy;
         for(dy = (double)0.0F; dy > (double)-3.0F && !this.isOpaqueBlockAt(x, y + dy, z); dy -= 0.2) {
         }

         return dy;
      }
   }

   public double getHighPartHeight(double x, double y, double z) {
      if (this.isFluidAt(x, y, z)) {
         return (double)0.0F;
      } else {
         double dy;
         for(dy = (double)0.0F; dy <= (double)3.0F && this.isOpaqueBlockAt(x, y + dy, z); dy += 0.2) {
         }

         return dy;
      }
   }

   public boolean canBeSeenAsEnemy() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean isFluidAt(double x, double y, double z) {
      if (this.noPhysics) {
         return false;
      } else {
         return !this.level().getFluidState(BlockPos.containing(x, y, z)).isEmpty();
      }
   }

   public float limitAngle(float source, float target, float maxChange) {
      float delta = Mth.wrapDegrees(target - source);
      delta = Mth.clamp(delta, -maxChange, maxChange);
      float result = source + delta;
      return Mth.wrapDegrees(result);
   }

   public boolean hurt(DamageSource source, float damage) {
      this.hurtMarked = true;
      this.hurtTime = 20;
      return this.hurtHeadId(source, damage);
   }

   public boolean hurtHeadId(DamageSource source, float damage) {
      if (this.headEntityId != -1) {
         Entity e = this.level().getEntity(this.headEntityId);
         if (e instanceof LivingEntity) {
            return e.hurt(source, damage);
         }
      }

      return true;
   }

   public Entity getParentSafe() {
      UUID id = this.getParentId();
      if (id != null) {
         Level var3 = this.level();
         if (var3 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var3;
            Entity parent = serverLevel.getEntity(id);
            if (parent == null) {
               return null;
            }

            this.entityData.set(PARENT_ID, parent.getId());
            return parent;
         }
      }

      return null;
   }

   public void setParent(Entity entity) {
      this.setParentId(entity.getUUID());
   }

   public UUID getParentId() {
      return (UUID)((Optional)this.entityData.get(PARENT_UUID)).orElse((Object)null);
   }

   public void setParentId(@Nullable UUID uniqueId) {
      this.entityData.set(PARENT_UUID, Optional.ofNullable(uniqueId));
   }

   public UUID getChildId() {
      return (UUID)((Optional)this.entityData.get(CHILD_UUID)).orElse((Object)null);
   }

   public void setChildId(@Nullable UUID uniqueId) {
      this.entityData.set(CHILD_UUID, Optional.ofNullable(uniqueId));
   }

   public boolean shouldShowName() {
      return false;
   }

   protected void onEffectAdded(MobEffectInstance instance, @Nullable Entity source) {
      super.onEffectAdded(instance, source);
      if (instance.getEffect().isBeneficial() || instance.getAmplifier() >= 2) {
         Entity parent = this.getParentSafe();
         if (parent instanceof LivingEntity) {
            LivingEntity livingParent = (LivingEntity)parent;
            MobEffectInstance existing = livingParent.getEffect(instance.getEffect());
            if (existing == null || existing.getDuration() < instance.getDuration() - 5) {
               livingParent.addEffect(new MobEffectInstance(instance));
            }

         }
      }
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      Entity parent = this.getParentSafe();
      InteractionResult var10000;
      if (parent instanceof LivingEntity living) {
         var10000 = living.interact(player, hand);
      } else {
         var10000 = super.interact(player, hand);
      }

      return var10000;
   }

   public Iterable getArmorSlots() {
      return List.of();
   }

   public ItemStack getItemBySlot(EquipmentSlot slot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
   }

   public HumanoidArm getMainArm() {
      return HumanoidArm.RIGHT;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      if (this.getParentId() != null) {
         tag.putUUID("ParentUUID", this.getParentId());
      }

      if (this.getChildId() != null) {
         tag.putUUID("ChildUUID", this.getChildId());
      }

      tag.putInt("color", (Integer)this.entityData.get(COLOR));
      tag.putBoolean("adapted", (Boolean)this.entityData.get(ADAPTED));
      tag.putBoolean("tail", (Boolean)this.entityData.get(IS_TAIL));

      for(int e = 0; e < this.legs.length; ++e) {
         this.legs[e].writeVariants(tag, e);
      }

   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.hasUUID("ParentUUID")) {
         this.setParentId(tag.getUUID("ParentUUID"));
      }

      if (tag.hasUUID("ChildUUID")) {
         this.setChildId(tag.getUUID("ChildUUID"));
      }

      this.entityData.set(COLOR, tag.getInt("color"));
      this.entityData.set(IS_TAIL, tag.getBoolean("adapted"));
      this.entityData.set(ADAPTED, tag.getBoolean("tail"));

      for(int e = 0; e < this.legs.length; ++e) {
         this.legs[e].readVariants(tag, e);
      }

   }

   public void setAdapted(boolean val) {
      this.entityData.set(ADAPTED, val);
   }

   public void setColor(int val) {
      this.entityData.set(COLOR, val);
   }

   public int getColor() {
      return (Integer)this.entityData.get(COLOR);
   }

   public boolean isTail() {
      return (Boolean)this.entityData.get(IS_TAIL);
   }

   public void setTail(boolean v) {
      this.entityData.set(IS_TAIL, v);
   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      return false;
   }

   public int chemicalRange() {
      return 0;
   }

   public List buffs() {
      return List.of();
   }

   public List debuffs() {
      return List.of();
   }

   static {
      CHILD_UUID = SynchedEntityData.defineId(LeviathanMultipart.class, EntityDataSerializers.OPTIONAL_UUID);
      PARENT_UUID = SynchedEntityData.defineId(LeviathanMultipart.class, EntityDataSerializers.OPTIONAL_UUID);
      COLOR = SynchedEntityData.defineId(LeviathanMultipart.class, EntityDataSerializers.INT);
      PARENT_ID = SynchedEntityData.defineId(LeviathanMultipart.class, EntityDataSerializers.INT);
      ADAPTED = SynchedEntityData.defineId(LeviathanMultipart.class, EntityDataSerializers.BOOLEAN);
      IS_TAIL = SynchedEntityData.defineId(LeviathanMultipart.class, EntityDataSerializers.BOOLEAN);
   }

   static enum LEG_POSITIONS {
      BACK_LEFT_TENTACLE(new Vec3((double)-1.5F, (double)0.5F, (double)0.25F), new Vec3((double)-2.5F, (double)-1.0F, (double)4.0F)),
      BACK_RIGHT_TENTACLE(new Vec3((double)-1.5F, (double)0.5F, (double)-0.25F), new Vec3((double)-2.5F, (double)-1.0F, (double)-4.0F)),
      FRONT_LEFT_TENTACLE(new Vec3((double)0.5F, (double)0.5F, (double)0.75F), new Vec3((double)-1.5F, (double)-1.0F, (double)6.0F)),
      FRONT_RIGHT_TENTACLE(new Vec3((double)0.5F, (double)0.5F, (double)-0.75F), new Vec3((double)-1.5F, (double)-1.0F, (double)-6.0F));

      private final Vec3 bodySet;
      private final Vec3 offset;

      private LEG_POSITIONS(Vec3 bodySet, Vec3 offset) {
         this.bodySet = bodySet;
         this.offset = offset;
      }

      // $FF: synthetic method
      private static LEG_POSITIONS[] $values() {
         return new LEG_POSITIONS[]{BACK_LEFT_TENTACLE, BACK_RIGHT_TENTACLE, FRONT_LEFT_TENTACLE, FRONT_RIGHT_TENTACLE};
      }
   }
}
