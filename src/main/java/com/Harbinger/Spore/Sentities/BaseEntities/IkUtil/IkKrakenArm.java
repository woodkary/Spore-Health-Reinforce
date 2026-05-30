package com.Harbinger.Spore.Sentities.BaseEntities.IkUtil;

import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class IkKrakenArm extends IkKrakenLeg {
   protected final boolean rightArm;
   protected @Nullable LivingEntity target;
   private final Vec3 RightVec = new Vec3((double)4.0F, (double)0.0F, (double)-16.0F);
   private final Vec3 LeftVec = new Vec3((double)4.0F, (double)0.0F, (double)16.0F);
   private final Vec3 RightMidVec = new Vec3((double)4.0F, (double)3.5F, (double)-5.0F);
   private final Vec3 LeftMidVec = new Vec3((double)4.0F, (double)3.5F, (double)5.0F);
   private final Vec3 RightMidVec2 = new Vec3((double)7.0F, (double)1.5F, (double)-5.0F);
   private final Vec3 LeftMidVec2 = new Vec3((double)7.0F, (double)1.5F, (double)5.0F);
   private final Vec3 MouthPosition = new Vec3((double)0.0F, (double)1.5F, (double)0.0F);
   private final Vec3 RightWaterVec = new Vec3((double)8.0F, (double)4.5F, (double)-6.0F);
   private final Vec3 LeftWaterVec = new Vec3((double)8.0F, (double)4.5F, (double)6.0F);
   private static final Vector3f nullVec = new Vector3f(0.0F);

   public IkKrakenArm(Grakensenker owner, int amount, Vec3 defaultBodyOffset, Vec3 defaultLimbOffset, float maxDistance, boolean rightArm) {
      super(owner, amount, defaultBodyOffset, defaultLimbOffset, maxDistance, false);
      this.rightArm = rightArm;
   }

   public float getWiggleAmplitude() {
      return 0.01F;
   }

   public float getWiggleSpeed() {
      return 0.5F;
   }

   public void refreshLegStandingPoint() {
      int hitValues = this.rightArm ? this.owner.getRightArmDelay() : this.owner.getLeftArmDelay();
      boolean full = this.rightArm ? this.owner.isRightArmFull() : this.owner.isLeftArmFull();
      Vector3f vector3f = this.rightArm ? this.owner.getRightArm() : this.owner.getLeftArm();
      if (this.owner.level().isClientSide) {
         this.sitPosition = vector3f != nullVec && hitValues <= 0 ? (new Vec3(vector3f)).add((double)0.0F, (double)1.0F, (double)0.0F) : this.getLegBasePos();
         this.sitPosition = full ? this.getMouthPosition() : this.sitPosition;
         this.lastSitPosition = this.sitPosition;
      } else {
         this.sitPosition = this.target != null && hitValues <= 0 ? this.target.position().add((double)0.0F, (double)1.0F, (double)0.0F) : this.getLegBasePos();
         this.sitPosition = full ? this.getMouthPosition() : this.sitPosition;
         this.lastSitPosition = this.sitPosition;
         if (this.owner.tickCount % 10 == 0 && hitValues <= 0 && !full) {
            this.setTarget();
         }
      }

   }

   protected void moveTipTowards(Vec3 value) {
      int tip = this.entities.length - 1;
      Vec3 currentPos = this.entities[tip];
      int val = this.entities.length - 1;
      Vec3 newPos = currentPos.lerp(value, (double)0.35F);
      this.entities[val] = newPos;
   }

   public void setTarget() {
      Optional<LivingEntity> targetOp = this.findAndSetTarget();
      if (targetOp.isPresent()) {
         this.target = (LivingEntity)targetOp.get();
      } else {
         this.target = null;
      }

   }

   public Optional findAndSetTarget() {
      Level level = this.owner.level();
      Vec3 pivot = this.applyYaw(this.rightArm ? this.RightVec : this.LeftVec);
      AABB searchBox = (new AABB(this.owner.getX() - (double)16.0F, this.owner.getY(), this.owner.getZ() - (double)16.0F, this.owner.getX() + (double)16.0F, this.owner.getY() + (double)this.owner.getExtendedHeight() + (double)4.0F, this.owner.getZ() + (double)16.0F)).move(pivot);
      return level.getEntitiesOfClass(LivingEntity.class, searchBox, (e) -> e.isAlive() && e != this.owner && e.getVehicle() != this.owner && Utilities.TARGET_SELECTOR.Test(e) && TargetingConditions.forCombat().test(this.owner, e)).stream().findFirst();
   }

   public Vec3 getMidSecPivot() {
      Vec3 pivot = this.applyYaw(this.rightArm ? this.RightMidVec : this.LeftMidVec);
      return this.owner.position().add(pivot).add((double)0.0F, (double)this.owner.getExtendedHeight(), (double)0.0F);
   }

   public Vec3 getMidSecPivot2() {
      Vec3 pivot = this.applyYaw(this.rightArm ? this.RightMidVec2 : this.LeftMidVec2);
      return this.owner.position().add(pivot).add((double)0.0F, (double)this.owner.getExtendedHeight(), (double)0.0F);
   }

   public Vec3 getMidSecPivot3() {
      Vec3 pivot = this.applyYaw(this.rightArm ? this.RightWaterVec : this.LeftWaterVec);
      return this.owner.position().add(pivot).add((double)0.0F, (double)this.owner.getExtendedHeight(), (double)0.0F);
   }

   public Vec3 getMouthPosition() {
      Vec3 pivot = this.applyYaw(this.MouthPosition);
      return this.owner.position().add(pivot).add((double)0.0F, (double)this.owner.getExtendedHeight(), (double)0.0F);
   }

   protected void moveMidSegmentTowards(int index, Vec3 target) {
      if (this.target == null && !this.owner.isInDeepWater()) {
         Vec3 currentPos = this.entities[index];
         Vec3 newPos = currentPos.lerp(target, (double)0.2F);
         this.entities[index] = newPos;
      }
   }

   protected void moveMidSegmentWaterTowards(int index, Vec3 target) {
      if (this.target == null && this.owner.isInDeepWater()) {
         Vec3 currentPos = this.entities[index];
         Vec3 newPos = currentPos.lerp(target, (double)0.45F);
         this.entities[index] = newPos;
      }

   }

   public void applyIK() {
      super.applyIK();
      if (!this.owner.isInDeepWater()) {
         this.moveMidSegmentTowards(this.entities.length / 4, this.getMidSecPivot());
         this.moveMidSegmentTowards(this.entities.length / 2, this.getMidSecPivot2());
         this.moveMidSegmentWaterTowards(this.entities.length / 4, this.getMidSecPivot3());
      }

      float x = (float)this.entities[this.entities.length - 1].x();
      float y = (float)this.entities[this.entities.length - 1].y();
      float z = (float)this.entities[this.entities.length - 1].z();
      Vector3f vector3f = new Vector3f(x, y, z);
      if (this.rightArm) {
         this.owner.setRightArm(vector3f);
      } else {
         this.owner.setLeftArm(vector3f);
      }

   }
}
