package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.Calamities.Hohlfresser;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
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

public class HohlMultipart extends LivingEntity implements TrueCalamity, ColdWeakness,ICustomLifeCycleEntity, ICalamityMultipart {
   private double prevHeight = (double)0.0F;
   private int headEntityId = -1;
   private static final EntityDataAccessor CHILD_UUID;
   private static final EntityDataAccessor PARENT_UUID;
   private static final EntityDataAccessor SIZE;
   private static final EntityDataAccessor VARIANT;
   private static final EntityDataAccessor COLOR;
   private static final EntityDataAccessor IS_TAIL;
   private static final EntityDataAccessor PARENT_ID;
   private static final EntityDataAccessor ADAPTED;
   private float spin = 0.0F;

   public HohlMultipart(EntityType p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
      this.setMaxUpStep(1.5F);
      initCustom();
   }
   @Override
   public void onRemovedFromWorld() {
      onRemoved();
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
      this.entityData.define(SIZE, 1.0F);
      this.entityData.define(VARIANT, 0);
      this.entityData.define(COLOR, 0);
      this.entityData.define(IS_TAIL, false);
      this.entityData.define(PARENT_ID, -1);
      this.entityData.define(ADAPTED, false);
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
   @Override
   public boolean isProtoOrCalamity(){
      return true;
   }
   @Override
   public void actuallyHurt(DamageSource source, float damage) {

   }
   public void tick() {
      super.tick();
      this.isInsidePortal = false;
      if (this.tickCount > 1) {
         Entity parent = this.getParentSafe();
         if (!this.level().isClientSide) {
            label36: {
               if (parent != null && !parent.isRemoved()) {
                  label33: {
                     if (parent instanceof Hohlfresser hohlfresser) {
                         if (!Objects.equals(hohlfresser.getChildId(), this.uuid)) {
                           break label33;
                        }
                     }

                     if (!(parent.distanceTo(this) > 25.0F)) {
                        if (parent instanceof LivingEntity living) {
                            this.hurtTime = living.hurtTime;
                           this.deathTime = living.deathTime;
                        }
                        break label36;
                     }
                  }
               }

               this.remove(RemovalReason.DISCARDED);
            }
         }
      }

      if (this.tickCount % 100 == 0) {
         this.refreshDimensions();
      }

      if (this.tickCount % 30 == 0 && this.getSegmentVariant() == SegmentVariants.MELEE && !this.isTail()) {
         this.dealMeleeDamageAround();
      }

   }

   public float getSpin() {
      float speed = (float)Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
      this.spin += speed * 2.5E-4F * (float)this.tickCount;
      return this.spin;
   }

   public Vec3 tickMultipartPosition(int headId, Vec3 parentPos, float parentXRot, float parentYRot, float ourYRot, boolean doHeight) {
      double spacing = (double)(1.5F * this.getBbWidth());
      Vec3 buttOffset = this.calcOffsetVec((float)(-spacing), parentXRot, parentYRot);
      Vec3 targetPos = parentPos.add(buttOffset);
      Vec3 currentPos = this.position();
      Vec3 smoothedPos = currentPos.lerp(targetPos, (double)0.25F);
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
   @Override
   public void heal(float amount) {
      Calamity calamity = this.getCalamityHead();
      if(calamity != null){
         calamity.healSelf(amount);
      }
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
   @Nullable
   public Entity getHeadEntity() {
      return this.level().getEntity(this.headEntityId);
   }

   @Nullable
   @Override
   public Calamity getCalamityHead() {
      Entity head = this.getHeadEntity();
      if (head instanceof Hohlfresser hohlfresser) {
         return hohlfresser;
      }

      Entity current = this.getHohlParentEntity();
      for(int i = 0; i < 32 && current != null; ++i) {
         if (current instanceof Hohlfresser hohlfresser) {
            return hohlfresser;
         }

         if (!(current instanceof HohlMultipart currentPart)) {
            return null;
         }

         head = currentPart.getHeadEntity();
         if (head instanceof Hohlfresser hohlfresser) {
            return hohlfresser;
         }

         Entity next = currentPart.getHohlParentEntity();
         if (next == current) {
            return null;
         }
         current = next;
      }

      return null;
   }

   @Nullable
   private Entity getHohlParentEntity() {
      Entity parent = this.getParentSafe();
      if (parent != null) {
         return parent;
      }

      int parentId = this.getParentIntId();
      return parentId >= 0 ? this.level().getEntity(parentId) : null;
   }

   public boolean hurt(DamageSource source, float damage) {
      if (!this.isTail() && this.getSegmentVariant() == SegmentVariants.ORGAN) {
         damage *= 2.5F;
      }

      this.hurtMarked = true;
      this.hurtTime = 20;
      return this.hurtHeadId(source, damage);
   }

   public void dealMeleeDamageAround() {
      AABB aabb = this.getBoundingBox().inflate((double)1.5F);
      List<Entity> entities = this.level().getEntities(this, aabb, (entityx) -> {
         boolean var10000;
         if (entityx instanceof LivingEntity living) {
            if (Utilities.TARGET_SELECTOR.Test(living)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      });
      float damage = (float)((Double)SConfig.SERVER.hohl_damage.get() * (Double)SConfig.SERVER.global_damage.get() / (double)2.0F);

      for(Entity entity : entities) {
         DamageSource source = this.level().damageSources().mobAttack(this);
         entity.hurt(source, damage);
         if(entity instanceof LivingEntity living&&!SporeJudge.isSporeEntity(living)&&!(living instanceof Player p&& EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(p))) {
            SporeAttackUtil.INSTANCE.dealDamage(living, source, damage);
         }
      }

   }

   public boolean hurtHeadId(DamageSource source, float damage) {
      Calamity calamity = this.getCalamityHead();
      if (calamity != null) {
         return calamity.hurt(source, damage);
      }

      return true;
   }

   public Entity getParentSafe() {
      UUID id = this.getParentId();
      if (id != null) {
         Level var3 = this.level();
         if (var3 instanceof ServerLevel serverLevel) {
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
      Entity parent = this.getParentSafe();
      if (parent instanceof LivingEntity livingParent) {
         MobEffectInstance existing = livingParent.getEffect(instance.getEffect());
         if (existing == null || existing.getDuration() < instance.getDuration() - 5) {
            livingParent.addEffect(new MobEffectInstance(instance));
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

      tag.putFloat("size", (Float)this.entityData.get(SIZE));
      tag.putInt("variant", (Integer)this.entityData.get(VARIANT));
      tag.putInt("color", (Integer)this.entityData.get(COLOR));
      tag.putBoolean("tail", (Boolean)this.entityData.get(IS_TAIL));
      tag.putBoolean("adapted", (Boolean)this.entityData.get(ADAPTED));
      addSaveData(tag);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.hasUUID("ParentUUID")) {
         this.setParentId(tag.getUUID("ParentUUID"));
      }

      if (tag.hasUUID("ChildUUID")) {
         this.setChildId(tag.getUUID("ChildUUID"));
      }

      this.entityData.set(SIZE, tag.getFloat("size"));
      this.entityData.set(VARIANT, tag.getInt("variant"));
      this.entityData.set(COLOR, tag.getInt("color"));
      this.entityData.set(IS_TAIL, tag.getBoolean("tail"));
      this.entityData.set(ADAPTED, tag.getBoolean("adapted"));
      readSaveData(tag);
   }

   public void setAdapted(boolean val) {
      this.entityData.set(ADAPTED, val);
   }

   public void setSize(float val) {
      this.entityData.set(SIZE, val);
   }

   public void setVariant(int val) {
      this.entityData.set(VARIANT, val);
   }

   public void setColor(int val) {
      this.entityData.set(COLOR, val);
   }

   public void setIsTail(boolean val) {
      this.entityData.set(IS_TAIL, val);
   }

   public boolean isAdapted() {
      return (Boolean)this.entityData.get(ADAPTED);
   }

   public float getSize() {
      return (Float)this.entityData.get(SIZE);
   }

   public int getVariant() {
      return (Integer)this.entityData.get(VARIANT);
   }

   public int getColor() {
      return (Integer)this.entityData.get(COLOR);
   }

   public boolean isTail() {
      return (Boolean)this.entityData.get(IS_TAIL);
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

   public EntityDimensions getDimensions(Pose p_21047_) {
      return super.getDimensions(p_21047_).scale(this.getSize());
   }

   private void setVariant(SegmentVariants variant) {
      this.entityData.set(VARIANT, variant.getId() & 255);
   }

   public void setVariant() {
      SegmentVariants variant = (SegmentVariants)Util.getRandom(SegmentVariants.values(), this.random);
      this.setVariant(variant);
   }

   public SegmentVariants getSegmentVariant() {
      return SegmentVariants.byId((Integer)this.entityData.get(VARIANT) & 255);
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.CALAMITY;
   }

   static {
      CHILD_UUID = SynchedEntityData.defineId(HohlMultipart.class, EntityDataSerializers.OPTIONAL_UUID);
      PARENT_UUID = SynchedEntityData.defineId(HohlMultipart.class, EntityDataSerializers.OPTIONAL_UUID);
      SIZE = SynchedEntityData.defineId(HohlMultipart.class, EntityDataSerializers.FLOAT);
      VARIANT = SynchedEntityData.defineId(HohlMultipart.class, EntityDataSerializers.INT);
      COLOR = SynchedEntityData.defineId(HohlMultipart.class, EntityDataSerializers.INT);
      IS_TAIL = SynchedEntityData.defineId(HohlMultipart.class, EntityDataSerializers.BOOLEAN);
      PARENT_ID = SynchedEntityData.defineId(HohlMultipart.class, EntityDataSerializers.INT);
      ADAPTED = SynchedEntityData.defineId(HohlMultipart.class, EntityDataSerializers.BOOLEAN);
   }

   @Override
   public LivingEntity entity() {
      return this;
   }

   public static enum SegmentVariants {
      DEFAULT(0),
      MELEE(1),
      ORGAN(2);

      private static final SegmentVariants[] BY_ID = (SegmentVariants[])Arrays.stream(values()).sorted(Comparator.comparingInt(SegmentVariants::getId)).toArray((x$0) -> new SegmentVariants[x$0]);
      private final int id;

      private SegmentVariants(int id) {
         this.id = id;
      }

      public int getId() {
         return this.id;
      }

      public static SegmentVariants byId(int id) {
         return BY_ID[id % BY_ID.length];
      }

      // $FF: synthetic method
      private static SegmentVariants[] $values() {
         return new SegmentVariants[]{DEFAULT, MELEE, ORGAN};
      }
   }
}
