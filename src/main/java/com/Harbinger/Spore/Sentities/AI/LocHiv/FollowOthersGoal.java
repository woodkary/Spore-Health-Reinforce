package com.Harbinger.Spore.Sentities.AI.LocHiv;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.google.common.base.Predicate;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

public class FollowOthersGoal extends Goal {
   private static final int SEARCH_INTERVAL = 20;
   private static final int FOLLOW_DISTANCE = 32;
   private static final double STOP_FOLLOWING_DISTANCE = (double)3.0F;
   private static final double TELEPORT_DISTANCE = (double)64.0F;
   private final Infected infected;
   private final Class desiredPartner;
   private final Predicate partnerTargeting;
   private int searchCooldown = 0;
   private int stuckCounter = 0;
   private LivingEntity lastTarget;

   public FollowOthersGoal(Infected infected, Class desiredPartner, @Nullable Predicate predicate) {
      this.infected = infected;
      this.desiredPartner = desiredPartner;
      this.partnerTargeting = predicate;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      if (--this.searchCooldown > 0) {
         return this.infected.getFollowPartner() != null;
      } else {
         this.searchCooldown = 20;
         LivingEntity currentPartner = this.infected.getFollowPartner();
         if (this.isValidPartner(currentPartner)) {
            return true;
         } else {
            if (currentPartner != null) {
               this.infected.setFollowPartner((LivingEntity)null);
            }

            LivingEntity newPartner = this.findNearestPartner();
            if (newPartner != null) {
               this.infected.setFollowPartner(newPartner);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   public boolean canContinueToUse() {
      LivingEntity partner = this.infected.getFollowPartner();
      if (partner != null && partner.isAlive()) {
         if (this.infected.distanceToSqr(partner) > (double)4096.0F) {
            this.infected.setFollowPartner((LivingEntity)null);
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public void start() {
      super.start();
      this.stuckCounter = 0;
      this.lastTarget = null;
   }

   public void stop() {
      super.stop();
      this.infected.getNavigation().stop();
      this.stuckCounter = 0;
      this.lastTarget = null;
   }

   public void tick() {
      LivingEntity partner = this.infected.getFollowPartner();
      if (partner != null) {
         double distanceToPartner = this.infected.distanceToSqr(partner);
         if (distanceToPartner <= (double)9.0F) {
            this.infected.getNavigation().stop();
         } else if (distanceToPartner > (double)4096.0F) {
            this.infected.teleportTo(partner.getX(), partner.getY(), partner.getZ());
         } else {
            PathNavigation navigation = this.infected.getNavigation();
            if (this.lastTarget != partner || this.shouldRepath()) {
               navigation.moveTo(partner, (double)1.0F);
               this.lastTarget = partner;
               this.stuckCounter = 0;
            }

            if (navigation.isDone()) {
               ++this.stuckCounter;
               if (this.stuckCounter > 10) {
                  navigation.stop();
                  navigation.moveTo(partner, (double)1.0F);
                  this.stuckCounter = 0;
               }
            }

            this.infected.getLookControl().setLookAt(partner, 30.0F, 30.0F);
         }
      }
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Nullable
   private LivingEntity findNearestPartner() {
      List<? extends LivingEntity> candidates = this.infected.level().getEntitiesOfClass(this.desiredPartner, this.infected.getBoundingBox().inflate((double)32.0F), this::isValidPartner);
      if (candidates.isEmpty()) {
         return null;
      } else {
         LivingEntity nearest = null;
         double nearestDistance = Double.MAX_VALUE;
         double infectedX = this.infected.getX();
         double infectedY = this.infected.getY();
         double infectedZ = this.infected.getZ();

         for(LivingEntity candidate : candidates) {
            if (candidate != this.infected) {
               double dx = candidate.getX() - infectedX;
               double dy = candidate.getY() - infectedY;
               double dz = candidate.getZ() - infectedZ;
               double distanceSqr = dx * dx + dy * dy + dz * dz;
               if (distanceSqr < nearestDistance) {
                  nearestDistance = distanceSqr;
                  nearest = candidate;
               }
            }
         }

         return nearest;
      }
   }

   private boolean isValidPartner(LivingEntity entity) {
      if (entity != null && entity.isAlive()) {
         return this.partnerTargeting == null || this.partnerTargeting.apply(entity);
      } else {
         return false;
      }
   }

   private boolean shouldRepath() {
      return this.infected.tickCount % 20 == 0;
   }
}
