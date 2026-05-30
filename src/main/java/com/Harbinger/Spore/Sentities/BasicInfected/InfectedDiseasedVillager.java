package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Utility.Reaper;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class InfectedDiseasedVillager extends InfectedVillager {
   public InfectedDiseasedVillager(EntityType type, Level level) {
      super(type, level);
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
         living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
      }

      return super.doHurtTarget(entity);
   }

   public void Evolve(Infected livingEntity, List value, ScamperVariants variants) {
      if (this.getLinked()) {
         Reaper reaper = new Reaper((EntityType)Sentities.REAPER.get(), this.level());
         reaper.setBiomass(this.getKills() + this.getEvoPoints());
         reaper.setCustomName(this.getCustomName());
         reaper.moveTo(this.getX(), this.getY(), this.getZ());
         this.level().addFreshEntity(reaper);
         this.discard();
         Level var6 = this.level();
         if (var6 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var6;
            double x0 = livingEntity.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
            double y0 = livingEntity.getY() + ((double)this.random.nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
            double z0 = livingEntity.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         }
      } else {
         super.Evolve(livingEntity, value, variants);
      }

   }
}
