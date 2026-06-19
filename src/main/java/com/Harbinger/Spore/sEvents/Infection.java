package com.Harbinger.Spore.sEvents;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.CustomJsonReader.SporeMobConversionData;
import com.Harbinger.Spore.Sentities.Signal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BasicInfected.Bairn;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPillager;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPlayer;
import com.Harbinger.Spore.Sentities.Hyper.Hvindicator;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.Sentities.Utility.GastGeber;
import com.Harbinger.Spore.Sentities.Utility.InfestedConstruct;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import com.Harbinger.Spore.Sentities.Variants.BairnSkins;
import com.Harbinger.Spore.Sentities.Variants.InfPillagerSkins;
import com.Harbinger.Spore.Sitems.BaseWeapons.DeathRewardingWeapon;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber
public class Infection {
   public static void setItemBySlot(Player player, EquipmentSlot slot, Mob entity) {
      entity.setItemSlot(slot, player.getItemBySlot(slot));
      entity.setDropChance(slot, 0.0F);
   }

   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      LivingEntity entity = event.getEntity();
      if (entity != null && !entity.level().isClientSide) {
         Level level = entity.level();
         double x = entity.getX();
         double y = entity.getY();
         double z = entity.getZ();
         Entity var10 = event.getSource().getEntity();
         if (var10 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)var10;
            ItemStack stack = livingEntity.getMainHandItem();
            Item var12 = stack.getItem();
            if (var12 instanceof DeathRewardingWeapon) {
               DeathRewardingWeapon weapon = (DeathRewardingWeapon)var12;
               weapon.computeAfterEffect(entity, livingEntity, stack);
            }
         }

         if (entity instanceof Infected) {
            Infected infected = (Infected)entity;
            if ((Boolean)SConfig.SERVER.scent_spawn.get() && infected.getTicksFrozen() <= 0 && level instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)level;
               if (Math.random() < (double)((float)(Integer)SConfig.SERVER.scent_spawn_chance.get() / 100.0F)) {
                  AABB area = infected.getBoundingBox().inflate((double)16.0F);
                  List<ScentEntity> scents = level.getEntitiesOfClass(ScentEntity.class, area);
                  if (scents.size() < (Integer)SConfig.SERVER.scent_cap.get()) {
                     ScentEntity scent = new ScentEntity((EntityType)Sentities.SCENT.get(), serverLevel);
                     scent.setOvercharged(infected.getLinked());
                     scent.moveTo(x, y + (double)4.0F, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
                     level.addFreshEntity(scent);
                  }
               }
            }
         }

         if (entity instanceof EvolvedInfected) {
            EvolvedInfected evolved = (EvolvedInfected)entity;
            if (Math.random() < 0.2) {
               AreaEffectCloud cloud = new AreaEffectCloud(level, x, y, z);
               cloud.setRadius(2.5F);
               cloud.setRadiusOnUse(-0.5F);
               cloud.setWaitTime(10);
               cloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 40, 0));
               cloud.setDuration(cloud.getDuration() / 2);
               cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
               level.addFreshEntity(cloud);
               AABB alertBox = evolved.getBoundingBox().inflate((double)30.0F);

               for(Entity e : level.getEntities(evolved, alertBox)) {
                  if (e instanceof Infected) {
                     Infected ally = (Infected)e;
                     ally.setSearchPos(new BlockPos((int)x, (int)y, (int)z));
                  }
               }
            }
         }

         if (entity instanceof Infected) {
            Infected inf = (Infected)entity;
            if (event.getSource().getEntity() != null && inf.getLinked()) {
               double chance = (Double)SConfig.SERVER.proto_calamity.get() / (double)100.0F;
               if (inf instanceof EvolvedInfected && Math.random() < chance) {
                  callProto(inf);
               }

               if (inf instanceof Hyper && Math.random() < chance * (double)2.0F) {
                  callProto(inf);
               }

               if (inf instanceof GastGeber && Math.random() < chance * (double)4.0F) {
                  callProto(inf);
               }
            }
         }

         if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.hasEffect((MobEffect)Seffects.MYCELIUM.get()) && (Boolean)SConfig.SERVER.inf_player.get()) {
               InfectedPlayer infectedPlayer = new InfectedPlayer((EntityType)Sentities.INF_PLAYER.get(), level);

               for(EquipmentSlot slot : EquipmentSlot.values()) {
                  if (slot.getType() == Type.ARMOR || slot.getType() == Type.HAND) {
                     setItemBySlot(player, slot, infectedPlayer);
                  }
               }

               infectedPlayer.setSkin();
               infectedPlayer.moveTo(x, y, z);
               infectedPlayer.setCustomName(player.getName());
               level.addFreshEntity(infectedPlayer);
            }
         }

         if (entity.hasEffect((MobEffect)Seffects.MYCELIUM.get()) && !(entity instanceof Player) && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (tryToMakeChild(entity, serverLevel)) {
               return;
            }

            for(String entry : (List<String>)SConfig.SERVER.inf_human_conv.get()) {
               String[] parts = entry.split("\\|");
               if (parts.length >= 2) {
                  ResourceLocation id = new ResourceLocation(parts[1]);
                  EntityType<?> type = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(id);
                  if (type != null && parts[0].equals(entity.getEncodeId())) {
                     Entity result = type.create(serverLevel);
                     if (result != null) {
                        result.setCustomName(entity.getCustomName());
                        result.setPos(entity.position());
                        if (result instanceof Mob) {
                           Mob mob = (Mob)result;
                           mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
                        }

                        if (result instanceof Infected) {
                           Infected converted = (Infected)result;
                           converted.setOrigin(entity.getEncodeId());
                        }

                        if (entity instanceof Pillager) {
                           Pillager pillager = (Pillager)entity;
                           if (pillager.isPatrolLeader() && result instanceof InfectedPillager) {
                              InfectedPillager infectedPillager = (InfectedPillager)result;
                              infectedPillager.setVariant(InfPillagerSkins.CAPTAIN.getId());
                           }
                        }

                        serverLevel.addFreshEntity(result);
                        entity.discard();
                        break;
                     }
                  }
               }
            }

            EntityType<?> JsonMob = SporeMobConversionData.getResult(entity.getType());
            if (JsonMob != null) {
               Entity result = JsonMob.create(serverLevel);
               if (result != null) {
                  result.setCustomName(entity.getCustomName());
                  result.setPos(entity.position());
                  if (result instanceof Mob) {
                     Mob mob = (Mob)result;
                     mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
                  }

                  if (result instanceof Infected) {
                     Infected converted = (Infected)result;
                     converted.setOrigin(entity.getEncodeId());
                  }

                  serverLevel.addFreshEntity(result);
                  entity.discard();
               }
            }

            if (entity instanceof IronGolem) {
               IronGolem golem = (IronGolem)entity;
               if (Math.random() < (Double)SConfig.SERVER.machine_infestation.get() / (double)100.0F) {
                  InfestedConstruct construct = new InfestedConstruct((EntityType)Sentities.INF_CONSTRUCT.get(), serverLevel);
                  construct.setPos(golem.position());
                  construct.setHealth(10.0F);
                  construct.setMachineHeeauklth(0.0F);
                  construct.setActive(false);
                  construct.setCustomName(golem.getCustomName());
                  serverLevel.addFreshEntity(construct);
                  golem.discard();
               }
            }
         }

         giveRewards(event.getSource().getEntity(), entity);
         awardHivemind(event.getSource().getEntity(), entity);
      }
   }

   public static boolean tryToMakeChild(LivingEntity living, ServerLevel serverLevel) {
      if (!living.isBaby()) {
         return false;
      } else {
         BairnSkins skin = null;
         if (living instanceof Villager) {
            skin = BairnSkins.VILLAGER;
         } else if (living instanceof Husk) {
            skin = BairnSkins.HUSK;
         } else if (living instanceof Drowned) {
            skin = BairnSkins.DROWNED;
         } else if (living instanceof ZombieVillager) {
            skin = BairnSkins.ZOMBIE_VILLAGER;
         } else if (living instanceof Zombie) {
            skin = BairnSkins.ZOMBIE;
         }

         if (skin != null) {
            Bairn bairn = new Bairn((EntityType)Sentities.BAIRN.get(), living.level());
            bairn.setCustomName(living.getCustomName());
            bairn.setPos(living.position());
            bairn.setVariant(skin.getId());
            serverLevel.addFreshEntity(bairn);
            living.discard();
            return true;
         } else {
            return false;
         }
      }
   }

   private static void callProto(Entity entity) {
      if (entity.level() instanceof ServerLevel) {
         List<Proto> protos = SporeSavedData.getHiveminds();
         if (!protos.isEmpty()) {
            for(Proto proto : protos) {
               if (proto.distanceTo(entity) <= (float)(Integer)SConfig.SERVER.proto_range.get()) {
                  proto.setSignal(new Signal(true, BlockPos.containing(entity.position())));
                  break;
               }
            }
         }

      }
   }

   private static void giveRewards(Entity source, LivingEntity victim) {
      if (source instanceof Hvindicator hv) {
         hv.awardSkull(victim);
      }

   }

   private static void awardHivemind(Entity source, LivingEntity victim) {
      if (source instanceof Mob mob) {
         CompoundTag data = mob.getPersistentData();
         if (data.contains("hivemind")) {
            Entity summoner = mob.level().getEntity(data.getInt("hivemind"));
            if (summoner instanceof Proto) {
               Proto proto = (Proto)summoner;
               int decision = data.getInt("decision");
               proto.adjustWeightsForDecision(decision, 0.1);
            }
         }
      }

   }
}
