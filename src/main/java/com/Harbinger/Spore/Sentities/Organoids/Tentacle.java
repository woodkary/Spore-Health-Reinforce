package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.IkLegWithHitbox;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

public class Tentacle extends UtilityEntity {
   private final TentaclePart[] completeArray;
   private final IkLegWithHitbox rightFrontHitbox;
   private final IkLegWithHitbox leftFrontHitbox;
   private final IkLegWithHitbox rightBackHitbox;
   private final IkLegWithHitbox leftBackHitbox;
   private final IkLegWithHitbox[] IkLegs;

   public Tentacle(EntityType type, Level level) {
      super(type, level);
      TentaclePart part0 = new TentaclePart(this, "segment0", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part1 = new TentaclePart(this, "segment1", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part2 = new TentaclePart(this, "segment2", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part3 = new TentaclePart(this, "segment3", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part4 = new TentaclePart(this, "segment4", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part5 = new TentaclePart(this, "segment5", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part6 = new TentaclePart(this, "segment6", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part7 = new TentaclePart(this, "segment7", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part8 = new TentaclePart(this, "segment8", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part9 = new TentaclePart(this, "segment9", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part10 = new TentaclePart(this, "segment10", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      TentaclePart part11 = new TentaclePart(this, "segment11", new EntityDimensions(0.5F, 0.5F, false), 1.0F);
      this.completeArray = new TentaclePart[]{part0, part1, part2, part3, part4, part5, part6, part7, part8, part9, part10, part11};
      this.rightFrontHitbox = new IkLegWithHitbox(this, new TentaclePart[]{part0, part1, part2}, LEGS.RIGHT_FRONT.getBodySet(), LEGS.RIGHT_FRONT.getOffset(), 8.0F, 1.0F, 2.0F, 0.5F);
      this.leftFrontHitbox = new IkLegWithHitbox(this, new TentaclePart[]{part3, part4, part5}, LEGS.LEFT_FRONT.getBodySet(), LEGS.LEFT_FRONT.getOffset(), 8.0F, 1.0F, 2.0F, 0.5F);
      this.rightBackHitbox = new IkLegWithHitbox(this, new TentaclePart[]{part6, part7, part8}, LEGS.RIGHT_BACK.getBodySet(), LEGS.RIGHT_BACK.getOffset(), 8.0F, 1.0F, 2.0F, 0.5F);
      this.leftBackHitbox = new IkLegWithHitbox(this, new TentaclePart[]{part9, part10, part11}, LEGS.LEFT_BACK.getBodySet(), LEGS.LEFT_BACK.getOffset(), 8.0F, 1.0F, 2.0F, 0.5F);
      this.IkLegs = new IkLegWithHitbox[]{this.rightFrontHitbox, this.leftFrontHitbox, this.rightBackHitbox, this.leftBackHitbox};
      int baseId = ENTITY_COUNTER.getAndAdd(this.completeArray.length + 1);
      this.setId(baseId);
      this.setMaxUpStep(1.0F);
   }

   public void setId(int entityId) {
      super.setId(entityId);

      for(int i = 0; i < this.completeArray.length; ++i) {
         this.completeArray[i].setId(entityId + i + 1);
      }

   }

   public PartEntity[] getRightSegments() {
      return this.rightFrontHitbox.getEntities();
   }

   public PartEntity[] getLeftSegments() {
      return this.leftFrontHitbox.getEntities();
   }

   public PartEntity[] getRightBackSegments() {
      return this.rightBackHitbox.getEntities();
   }

   public PartEntity[] getLeftBackSegments() {
      return this.leftBackHitbox.getEntities();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.mound_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.mound_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_DAMAGE, (double)2.0F).add(Attributes.MOVEMENT_SPEED, 0.3);
   }

   public boolean isMultipartEntity() {
      return true;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
      super.recreateFromPacket(clientboundAddEntityPacket);

      for(int i = 0; i < this.completeArray.length; ++i) {
         this.completeArray[i].setId(i + clientboundAddEntityPacket.getId());
      }

   }

   public PartEntity[] getParts() {
      return this.completeArray;
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      super.registerGoals();
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, (double)1.0F, true));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, (double)1.0F));
   }

   public void aiStep() {
      super.aiStep();
      if (this.tickCount % 20 == 0) {
         for(IkLegWithHitbox leg : this.IkLegs) {
            leg.refreshLegStandingPoint();
         }
      }

      for(IkLegWithHitbox leg : this.IkLegs) {
         leg.applyIK();
      }

   }

   private void spawnParticlesInPlaces(Vec3 vec3, IkLegWithHitbox legs, boolean fire) {
      if (fire) {
         Vec3 positionOnBody = legs.getDefaultBodyOffset();
         this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, positionOnBody.x, positionOnBody.y, positionOnBody.z, (double)0.0F, 0.2, (double)0.0F);
      } else if (vec3 != null) {
         this.level().addParticle(ParticleTypes.FLAME, vec3.x, vec3.y, vec3.z, (double)0.0F, 0.2, (double)0.0F);
      }

   }

   public boolean hurt(TentaclePart tentaclePart, DamageSource source, float amount) {
      return this.hurt(source, amount * 0.25F);
   }

   public static enum LEGS {
      RIGHT_FRONT(new Vec3((double)0.5F, (double)1.5F, (double)-0.5F), new Vec3((double)3.0F, (double)-0.5F, (double)-3.0F)),
      LEFT_FRONT(new Vec3((double)0.5F, (double)1.5F, (double)0.5F), new Vec3((double)3.0F, (double)-0.5F, (double)3.0F)),
      RIGHT_BACK(new Vec3((double)-0.5F, (double)1.5F, (double)-0.5F), new Vec3((double)-3.0F, (double)-0.5F, (double)-3.0F)),
      LEFT_BACK(new Vec3((double)-0.5F, (double)1.5F, (double)0.5F), new Vec3((double)-3.0F, (double)-0.5F, (double)3.0F));

      private final Vec3 bodySet;
      private final Vec3 offset;

      private LEGS(Vec3 bodySet, Vec3 offset) {
         this.bodySet = bodySet;
         this.offset = offset;
      }

      public Vec3 getOffset() {
         return this.offset;
      }

      public Vec3 getBodySet() {
         return this.bodySet;
      }

      // $FF: synthetic method
      private static LEGS[] $values() {
         return new LEGS[]{RIGHT_FRONT, LEFT_FRONT, RIGHT_BACK, LEFT_BACK};
      }
   }
}
