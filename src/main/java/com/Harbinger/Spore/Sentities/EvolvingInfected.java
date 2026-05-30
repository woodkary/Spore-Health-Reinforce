package com.Harbinger.Spore.Sentities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public interface EvolvingInfected {
   default void tickEvolution(Infected infected, List value, ScamperVariants variants) {
      if (infected.tickCount % 20 == 0 && infected.getEvoPoints() >= (Integer)SConfig.SERVER.min_kills.get()) {
         if (infected.getEvolutionCoolDown() >= (Integer)SConfig.SERVER.evolution_age_human.get()) {
            this.Evolve(infected, value, variants);
         } else if (!infected.hasEffect((MobEffect)Seffects.FROSTBITE.get())) {
            infected.setEvolution(infected.getEvolutionCoolDown() + 1);
         }
      }

   }

   default void tickHyperEvolution(EvolvedInfected infected) {
      if (infected.tickCount % 20 == 0 && infected.getEvoPoints() >= (Integer)SConfig.SERVER.min_kills_hyper.get()) {
         if (infected.getEvolutionCoolDown() >= (Integer)SConfig.SERVER.evolution_age_human.get()) {
            this.HyperEvolve(infected);
         } else if (!infected.hasEffect((MobEffect)Seffects.FROSTBITE.get())) {
            infected.setEvolution(infected.getEvolutionCoolDown() + 1);
         }
      }

   }

   default void HyperEvolve(LivingEntity living) {
      Level var3 = living.level();
      if (var3 instanceof ServerLevel serverLevel) {
         double x0 = living.getX() - ((double)living.getRandom().nextFloat() - 0.1) * 0.1;
         double y0 = living.getY() + ((double)living.getRandom().nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
         double z0 = living.getZ() + ((double)living.getRandom().nextFloat() - 0.1) * 0.1;
         serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         this.ringPlayers(living);
      }

   }

   default void ringPlayers(LivingEntity living) {
      for(Entity entity : living.level().getEntities(living, living.getBoundingBox().inflate((double)64.0F), (entityx) -> entityx instanceof Player)) {
         if (entity instanceof Player player) {
            player.playNotifySound((SoundEvent)Ssounds.HYPER_EVOLVE.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
         }
      }

   }

   default void Evolve(Infected livingEntity, List value, ScamperVariants variants) {
      if (livingEntity != null && value != null) {
         Level level = livingEntity.level();
         if (level instanceof ServerLevel) {
            ServerLevel world = (ServerLevel)level;
            level = livingEntity.level();
            RandomSource random = RandomSource.create();
            if (Math.random() < 0.9) {
               Random rand = new Random();

               for(int i = 0; i < 1; ++i) {
                  int randomIndex = rand.nextInt(value.size());
                  ResourceLocation randomElement1 = new ResourceLocation((String)value.get(randomIndex));
                  EntityType<?> randomElement = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(randomElement1);
                  Entity waveentity = randomElement.create(level);
                  waveentity.setPos(livingEntity.getX(), livingEntity.getY() + (double)0.5F, livingEntity.getZ());
                  waveentity.setCustomName(livingEntity.getCustomName());
                  if (waveentity instanceof LivingEntity) {
                     LivingEntity entity = (LivingEntity)waveentity;

                     for(MobEffectInstance mobeffectinstance : livingEntity.getActiveEffects()) {
                        entity.addEffect(new MobEffectInstance(mobeffectinstance));
                     }
                  }

                  if (waveentity instanceof Infected) {
                     Infected infected = (Infected)waveentity;
                     infected.setKills(livingEntity.getKills());
                     infected.setEvoPoints(livingEntity.getEvoPoints());
                     infected.setSearchPos(livingEntity.getSearchPos());
                     infected.setLinked(livingEntity.getLinked());
                     infected.finalizeSpawn(world, livingEntity.level().getCurrentDifficultyAt(new BlockPos((int)livingEntity.getX(), (int)livingEntity.getY(), (int)livingEntity.getZ())), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
                  }

                  level.addFreshEntity(waveentity);
                  livingEntity.discard();
               }
            } else {
               Scamper scamper = new Scamper((EntityType)Sentities.SCAMPER.get(), level);
               scamper.setVariant(variants);
               scamper.setPos(livingEntity.getX(), livingEntity.getY() + (double)0.5F, livingEntity.getZ());
               scamper.setCustomName(livingEntity.getCustomName());
               scamper.setKills(livingEntity.getKills());
               scamper.setEvoPoints(livingEntity.getEvoPoints());
               scamper.setLinked(livingEntity.getLinked());
               scamper.setSearchPos(livingEntity.getSearchPos());

               for(MobEffectInstance mobeffectinstance : livingEntity.getActiveEffects()) {
                  scamper.addEffect(new MobEffectInstance(mobeffectinstance));
               }

               level.addFreshEntity(scamper);
               livingEntity.discard();
            }

            if (level instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)level;
               double x0 = livingEntity.getX() - ((double)random.nextFloat() - 0.1) * 0.1;
               double y0 = livingEntity.getY() + ((double)random.nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
               double z0 = livingEntity.getZ() + ((double)random.nextFloat() - 0.1) * 0.1;
               serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
            }
         }
      }

   }
}
