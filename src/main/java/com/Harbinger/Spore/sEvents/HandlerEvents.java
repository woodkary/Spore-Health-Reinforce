package com.Harbinger.Spore.sEvents;

import com.Harbinger.Spore.Core.*;
import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.Harbinger.Spore.Spore;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.ExtremelySusThings.ChunkLoadRequest;
import com.Harbinger.Spore.ExtremelySusThings.ChunkLoaderHelper;
import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.ExtremelySusThings.Package.SongInitializingPacket;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.Harbinger.Spore.SBlockEntities.LivingStructureBlocks;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.ChunkLoaderMob;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedDrowned;
import com.Harbinger.Spore.Sentities.Calamities.Gazenbrecher;
import com.Harbinger.Spore.Sentities.Calamities.Hinderburg;
import com.Harbinger.Spore.Sentities.Calamities.Hohlfresser;
import com.Harbinger.Spore.Sentities.Calamities.Sieger;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Naiad;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Protector;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.Harbinger.Spore.Sentities.Organoids.Brauerei;
import com.Harbinger.Spore.Sentities.Organoids.Delusionare;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.Sentities.Organoids.Umarmer;
import com.Harbinger.Spore.Sentities.Organoids.Vigil;
import com.Harbinger.Spore.Sentities.Organoids.Womb;
import com.Harbinger.Spore.Sentities.Projectile.AcidBall;
import com.Harbinger.Spore.Sentities.Projectile.BileProjectile;
import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import com.Harbinger.Spore.Sentities.Projectile.StingerProjectile;
import com.Harbinger.Spore.Sentities.Projectile.Vomit;
import com.Harbinger.Spore.Sentities.Projectile.VomitUsurperBall;
import com.Harbinger.Spore.Sentities.Utility.CorpseEntity;
import com.Harbinger.Spore.Sentities.Utility.GastGeber;
import com.Harbinger.Spore.Sentities.Utility.Illusion;
import com.Harbinger.Spore.Sentities.Utility.InfestedConstruct;
import com.Harbinger.Spore.Sentities.Utility.NukeEntity;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import com.Harbinger.Spore.Sentities.Utility.Specter;
import com.Harbinger.Spore.Sentities.Utility.Vanguard;
import com.Harbinger.Spore.Sitems.PCI;
import com.Harbinger.Spore.Sitems.SporeHorseArmor;
import com.Harbinger.Spore.Sitems.BaseWeapons.DamagePiercingModifier;
import com.Harbinger.Spore.Sitems.BaseWeapons.LootModifierWeapon;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeBaseArmor;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsBaseItem;
import com.Harbinger.Spore.Sitems.Guns.AbstractSporeGun;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import java.util.*;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(
   modid = "spore"
)
public class HandlerEvents {
   private static int tickCounter = 0;
   private static final int CHECK_INTERVAL = 1200;
   private static int val;
   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         ChunkLoaderHelper.tick();
      }
      if (event.phase == Phase.END) {
         ++tickCounter;
         if (tickCounter >= 1200) {
            MinecraftServer server = event.getServer();
            if (server != null) {
               for(ServerLevel level : server.getAllLevels()) {
                  cleanUpMobs(level);
               }
            }

            tickCounter = 0;
         }

         int i = 1200 * (Integer)SConfig.SERVER.time_song_trigger.get();
         ++val;
         if (val % i == 0) {
            if (!(Boolean)SConfig.SERVER.ambient_song.get() || (Boolean)SConfig.SERVER.disable_system.get()) {
               val = 0;
               return;
            }

            PlayerList players = event.getServer().getPlayerList();
            if (players.getPlayers().isEmpty()) {
               return;
            }

            for(Player player : players.getPlayers()) {
               if (player instanceof ServerPlayer) {
                  ServerPlayer serverPlayer = (ServerPlayer)player;
                  boolean postProto = !SporeSavedData.getHiveminds().isEmpty();
                  SporePacketHandler.sendToClient(new SongInitializingPacket(-1, false, postProto), serverPlayer);
               }
            }

            val = 0;
         }

      }
   }

   private static void cleanUpMobs(ServerLevel level) {
      List<Infected> infected = new ArrayList();
      List<Projectile> projectileExcess = new ArrayList();
      List<EvolvedInfected> evolved = new ArrayList();
      List<Hyper> hyper = new ArrayList();
      List<Organoid> organoid = new ArrayList();
      List<ScentEntity> scent = new ArrayList();

      for(Entity entity : level.getAllEntities()) {
         if (!((List)SConfig.SERVER.despawn_blacklist.get()).contains(entity.getEncodeId()) && !entity.hasCustomName()) {
            if (entity instanceof Organoid) {
               Organoid o = (Organoid)entity;
               organoid.add(o);
            } else if (entity instanceof EvolvedInfected) {
               EvolvedInfected e = (EvolvedInfected)entity;
               evolved.add(e);
            } else if (entity instanceof Hyper) {
               Hyper h = (Hyper)entity;
               hyper.add(h);
            } else if (entity instanceof ScentEntity) {
               ScentEntity s = (ScentEntity)entity;
               scent.add(s);
            } else if (entity instanceof Infected) {
               Infected i = (Infected)entity;
               infected.add(i);
            } else if (entity instanceof AcidBall) {
               AcidBall i = (AcidBall)entity;
               projectileExcess.add(i);
            } else if (entity instanceof BileProjectile) {
               BileProjectile i = (BileProjectile)entity;
               projectileExcess.add(i);
            } else if (entity instanceof StingerProjectile) {
               StingerProjectile i = (StingerProjectile)entity;
               projectileExcess.add(i);
            } else if (entity instanceof Vomit) {
               Vomit i = (Vomit)entity;
               projectileExcess.add(i);
            } else if (entity instanceof FleshBomb) {
               FleshBomb i = (FleshBomb)entity;
               projectileExcess.add(i);
            } else if (entity instanceof VomitUsurperBall) {
               VomitUsurperBall i = (VomitUsurperBall)entity;
               projectileExcess.add(i);
            }
         }
      }

      despawnExcess(level, infected, (Integer)SConfig.SERVER.max_infected_cap.get());
      despawnExcess(level, evolved, (Integer)SConfig.SERVER.max_evolved_cap.get());
      despawnExcess(level, hyper, (Integer)SConfig.SERVER.max_hyper_cap.get());
      despawnExcess(level, organoid, (Integer)SConfig.SERVER.max_organoid_cap.get());
      despawnExcess(level, scent, (Integer)SConfig.SERVER.max_scent_cap.get());
      despawnExcess(level, projectileExcess, 100);
   }

   private static <T extends Entity> void despawnExcess(ServerLevel level, List<T> entities, int cap) {
      if (entities.size() > cap) {
         int toRemove = entities.size() - cap;
         int despawns = 0;
         List<ServerPlayer> players = level.getPlayers((p) -> true);
         if (players.isEmpty()) {
            for(int i = 0; i < toRemove; ++i) {
               Entity entity = entities.get(i);
               entity.discard();
               ++despawns;
            }
         } else {
            entities.sort(Comparator.comparingDouble((Entity entity) -> {
               Player nearest = level.getNearestPlayer(entity, (double)-1.0F);
               return nearest != null ? entity.distanceToSqr(nearest) : Double.MAX_VALUE;
            }).reversed());

            for(int i = 0; i < toRemove; ++i) {
               Entity entity = entities.get(i);
               entity.discard();
               ++despawns;
            }
         }

         System.out.println("Despawned " + despawns + " mobs in level: " + level.dimension().location());
      }
   }

   @SubscribeEvent
   public static void onWorldLoad(LevelEvent.Load event) {
      LevelAccessor var2 = event.getLevel();
      if (var2 instanceof ServerLevel level) {
         SporeSavedData data = SporeSavedData.get(level);

         for(ChunkLoadRequest request : data.getRequests()) {
            ChunkLoaderHelper.ACTIVE_REQUESTS.put(request.getRequestID(), request);

            for(ChunkPos pos : request.getChunkPositionsToLoad()) {
               ChunkLoaderHelper.forceChunk(level, pos);
            }
         }
      }

   }
   @SubscribeEvent
   public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event){
      EntityHeealuthManager.INSTANCE.setPlayerAlliive(event.getEntity());
   }


   @SubscribeEvent
   public static void onLivingSpawned(EntityJoinLevelEvent event) {
      if (event != null && event.getEntity() != null) {
         Entity var2 = event.getEntity();
         if (var2 instanceof Protector) {
            Protector protector = (Protector)var2;
            SporeSavedData.addProtector(protector);
         }

         var2 = event.getEntity();
         if (var2 instanceof Proto) {
            Proto proto = (Proto)var2;
            if (event.getLevel() instanceof ServerLevel) {
               SporeSavedData.addProto(proto);
            }
         }

         var2 = event.getEntity();
         if (var2 instanceof PathfinderMob) {
            PathfinderMob mob = (PathfinderMob)var2;

            for(String string : (List<String>)SConfig.SERVER.attack.get()) {
               if (string.endsWith(":")) {
                  String[] mod = string.split(":");
                  String[] iterations = mob.getEncodeId().split(":");
                  if (Objects.equals(mod[0], iterations[0])) {
                     mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal(mob, Infected.class, false));
                     mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal(mob, Calamity.class, false));
                     mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal(mob, Organoid.class, false));
                  }
               } else if (((List)SConfig.SERVER.attack.get()).contains(mob.getEncodeId())) {
                  mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal(mob, Infected.class, false));
                  mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal(mob, Calamity.class, false));
                  mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal(mob, Organoid.class, false));
               }
            }

            for(String string : (List<String>)SConfig.SERVER.flee.get()) {
               if (string.endsWith(":")) {
                  String[] mod = string.split(":");
                  String[] iterations = mob.getEncodeId().split(":");
                  if (Objects.equals(mod[0], iterations[0])) {
                     mob.goalSelector.addGoal(4, new AvoidEntityGoal(mob, Infected.class, 6.0F, (double)1.0F, 0.9));
                     mob.goalSelector.addGoal(4, new AvoidEntityGoal(mob, UtilityEntity.class, 8.0F, (double)1.0F, 0.9));
                  }
               } else if (((List)SConfig.SERVER.flee.get()).contains(mob.getEncodeId())) {
                  mob.goalSelector.addGoal(4, new AvoidEntityGoal(mob, Infected.class, 6.0F, (double)1.0F, 0.9));
                  mob.goalSelector.addGoal(4, new AvoidEntityGoal(mob, UtilityEntity.class, 8.0F, (double)1.0F, 0.9));
               }
            }
         }
      }

   }
   private static boolean forceKillEntity(Entity entity, Player player) {
      if (entity == null) {
         return false;
      }
      if (entity instanceof LivingEntity livingEntity) {
         if (SporeJudge.isSporeEntity(livingEntity)) {
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(livingEntity,0.0f);
            return true;
         }
         DamageSource source = livingEntity.damageSources().cactus();
         EntityHeealuthManager.INSTANCE.hurt(livingEntity, Float.POSITIVE_INFINITY, source);
         EntityHeealuthManager.INSTANCE.killEntity(livingEntity, source);
         return true;
      }

      entity.remove(Entity.RemovalReason.DISCARDED);
      return true;
   }
   @SubscribeEvent
   public static void Command(RegisterCommandsEvent event) {
      CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
      dispatcher.register(Commands.literal("spore:force_kill")
              .requires(source -> source.hasPermission(2))
              .then(Commands.argument("targets", EntityArgument.entities())
                      .executes(ctx -> {
                         Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
                         Player player = ctx.getSource().getEntity() instanceof Player p ? p : null;
                         targets=new ArrayList<>(targets);
                         targets.remove(player);
                         int killed = 0;
                         for (Entity entity : targets) {
                            if (forceKillEntity(entity, player)) {
                               killed++;
                            }
                         }
                         int total = targets.size();
                         int finalKilled = killed;
                         ctx.getSource().sendSuccess(
                                 () -> Component.literal("force_kill 执行完成: " + finalKilled + "/" + total),
                                 true
                         );
                         return killed;
                      }))
      );
      dispatcher.register(Commands.literal("spore:force_remove")
              .requires(source -> source.hasPermission(2))
              .then(Commands.argument("targets", EntityArgument.entities())
                      .executes(ctx -> {
                         Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
                         Player player = ctx.getSource().getEntity() instanceof Player p ? p : null;
                         targets=new ArrayList<>(targets);
                         targets.remove(player);
                         int killed = 0;
                         for (Entity entity : targets) {
                            if (SimpleRemoveUtil.INSTANCE.remove(entity, Entity.RemovalReason.CHANGED_DIMENSION)) {
                               killed++;
                            }
                         }
                         int total = targets.size();
                         int finalKilled = killed;
                         ctx.getSource().sendSuccess(
                                 () -> Component.literal("force_remove 执行完成: " + finalKilled + "/" + total),
                                 true
                         );
                         return killed;
                      }))
      );
      dispatcher.register(Commands.literal("spore:force_remove_all")
              .requires(source -> source.hasPermission(2))
                      .executes(ctx -> {
                         Collection<? extends Entity> targets = SimpleRemoveUtil.INSTANCE.getAllEntities(ctx.getSource().getEntity().level,(entity)->entity instanceof Player);
                         int killed = 0;
                         for (Entity entity : targets) {
                            if (SimpleRemoveUtil.INSTANCE.remove(entity, Entity.RemovalReason.CHANGED_DIMENSION)) {
                               killed++;
                            }
                         }
                         int total = targets.size();
                         int finalKilled = killed;
                         ctx.getSource().sendSuccess(
                                 () -> Component.literal("force_remove_all 执行完成: " + finalKilled + "/" + total),
                                 true
                         );
                         return killed;
                      }));
      dispatcher.register(Commands.literal("spore:enable_light")
              .requires(source -> source.hasPermission(2))
              .then(Commands.argument("light", BoolArgumentType.bool())
              .executes(ctx->{
                 boolean value=BoolArgumentType.getBool(ctx,"light");
                 SporeSavedData.get(ctx.getSource().getLevel()).setCasingLightAllowed(value);
                 ctx.getSource().sendSuccess(
                         () -> Component.literal("proto casing light has "+(value?"enabled":"disabled")+"."),
                         true
                 );
                 return 1;
              })));
      dispatcher.register(Commands.literal("spore:set_area").executes((arguments) -> {
         ServerLevel world = ((CommandSourceStack)arguments.getSource()).getLevel();
         int x = (int)((CommandSourceStack)arguments.getSource()).getPosition().x();
         int y = (int)((CommandSourceStack)arguments.getSource()).getPosition().y();
         int z = (int)((CommandSourceStack)arguments.getSource()).getPosition().z();
         Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
         if (entity == null) {
            entity = FakePlayerFactory.getMinecraft(world);
         }

         if (entity != null) {
            BlockPos pos = new BlockPos(x, y, z);
            AABB hitbox = entity.getBoundingBox().inflate((double)20.0F);

            for(Entity entity1 : entity.level().getEntities(entity, hitbox)) {
               if (entity1 instanceof Infected) {
                  Infected infected = (Infected)entity1;
                  infected.setSearchPos(pos);
               } else if (entity1 instanceof Calamity) {
                  Calamity calamity = (Calamity)entity1;
                  calamity.setSearchArea(pos);
               }
            }
         }

         return 1;
      }).requires((s) -> s.hasPermission(1)));
      dispatcher.register(Commands.literal("spore:nuke_the_land").executes((arguments) -> {
         ServerLevel world = ((CommandSourceStack)arguments.getSource()).getLevel();
         int x = (int)((CommandSourceStack)arguments.getSource()).getPosition().x();
         int y = (int)((CommandSourceStack)arguments.getSource()).getPosition().y();
         int z = (int)((CommandSourceStack)arguments.getSource()).getPosition().z();
         NukeEntity nukeEntity = new NukeEntity((EntityType)Sentities.NUKE.get(), world);
         nukeEntity.setInitRange(1.0F);
         nukeEntity.setRange((float)((Double)SConfig.SERVER.nuke_range.get() * (double)1.0F));
         nukeEntity.setInitDuration(0);
         nukeEntity.setDuration((Integer)SConfig.SERVER.nuke_time.get());
         nukeEntity.setDamage((float)((Double)SConfig.SERVER.nuke_damage.get() * (double)1.0F));
         nukeEntity.setPos((double)x, (double)y, (double)z);
         world.addFreshEntity(nukeEntity);
         return 1;
      }).requires((s) -> s.hasPermission(1)));
      dispatcher.register(Commands.literal("spore:corpse").executes((arguments) -> {
         ServerLevel world = ((CommandSourceStack)arguments.getSource()).getLevel();
         RandomSource randomSource = RandomSource.create();
         int x = (int)((CommandSourceStack)arguments.getSource()).getPosition().x();
         int y = (int)((CommandSourceStack)arguments.getSource()).getPosition().y();
         int z = (int)((CommandSourceStack)arguments.getSource()).getPosition().z();
         CorpseEntity corpseEntity = new CorpseEntity((EntityType)Sentities.CORPSE_PIECE.get(), world);
         corpseEntity.setCorpseType(randomSource.nextInt(HitboxesForParts.values().length));
         corpseEntity.setOwnerAda(true);
         corpseEntity.setPos((double)x, (double)y, (double)z);
         world.addFreshEntity(corpseEntity);
         return 1;
      }).requires((s) -> s.hasPermission(1)));
      dispatcher.register(Commands.literal("spore:erase_the_fungus").executes((arguments) -> {
         ServerLevel serverLevel = ((CommandSourceStack)arguments.getSource()).getLevel();

         for(Entity entity : serverLevel.getAllEntities()) {
            if (entity instanceof LivingEntity living) {
               if (living instanceof Infected || living instanceof UtilityEntity) {
                  living.discard();
               }
            }
         }

         return 1;
      }).requires((s) -> s.hasPermission(1)));
      dispatcher.register(Commands.literal("spore:feed").executes((arguments) -> {
         ServerLevel world = ((CommandSourceStack)arguments.getSource()).getLevel();
         Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
         if (entity == null) {
            entity = FakePlayerFactory.getMinecraft(world);
         }

         if (entity != null) {
            AABB hitbox = entity.getBoundingBox().inflate((double)20.0F);

            for(Entity entity1 : entity.level().getEntities(entity, hitbox)) {
               if (entity1 instanceof Infected) {
                  Infected infected = (Infected)entity1;
                  infected.setKills(infected.getKills() + 1);
                  infected.setEvoPoints(infected.getEvoPoints() + 1);
               } else if (entity1 instanceof Calamity) {
                  Calamity calamity = (Calamity)entity1;
                  calamity.setKills(calamity.getKills() + 1);
               }
            }
         }

         return 1;
      }).requires((s) -> s.hasPermission(1)));
      dispatcher.register(Commands.literal("spore:evolve").executes((arguments) -> {
         ServerLevel world = ((CommandSourceStack)arguments.getSource()).getLevel();
         Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
         if (entity == null) {
            entity = FakePlayerFactory.getMinecraft(world);
         }

         if (entity != null) {
            AABB hitbox = entity.getBoundingBox().inflate((double)20.0F);

            for(Entity entity1 : entity.level().getEntities(entity, hitbox)) {
               if (entity1 instanceof Infected) {
                  Infected infected = (Infected)entity1;
                  infected.setEvolution((Integer)SConfig.SERVER.evolution_age_human.get());
                  if (entity1 instanceof Scamper) {
                     Scamper scamper = (Scamper)entity1;
                     scamper.setAge((Integer)SConfig.SERVER.scamper_age.get());
                  } else if (infected instanceof EvolvedInfected) {
                     EvolvedInfected evolvedInfected = (EvolvedInfected)infected;
                     evolvedInfected.setEvoPoints((Integer)SConfig.SERVER.min_kills_hyper.get());
                  } else {
                     infected.setEvoPoints((Integer)SConfig.SERVER.min_kills.get());
                  }
               } else if (entity1 instanceof Mound) {
                  Mound mound = (Mound)entity1;
                  mound.setAge(mound.getAge() + 1);
               } else if (entity1 instanceof Calamity) {
                  Calamity calamity = (Calamity)entity1;
                  calamity.ActivateAdaptation();
               }
            }
         }

         return 1;
      }).requires((s) -> s.hasPermission(1)));
      dispatcher.register(Commands.literal("spore:get_data").executes((arguments) -> {
         ServerLevel world = ((CommandSourceStack)arguments.getSource()).getLevel();
         Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
         if (entity instanceof Player player) {
            SporeSavedData data = SporeSavedData.getDataLocation(world);
            int numberofprotos = data.getAmountOfHiveminds();
            player.displayClientMessage(Component.literal("........................................"), false);
            player.displayClientMessage(Component.literal("There are " + numberofprotos + " proto hiveminds in this dimension"), false);

            for(ChunkLoadRequest request : data.getRequests()) {
               String id = request.getRequestID();
               long getDefaultTicks = request.getTickAmount();
               long ticks = request.getTicksUntilExpiration();
               player.displayClientMessage(Component.literal("Loaded chunk " + id + " " + ticks + "/" + getDefaultTicks), false);
            }
         }

         return 1;
      }).requires((s) -> s.hasPermission(1)));
      dispatcher.register(Commands.literal("spore:check_entity").executes((arguments) -> {
         ServerLevel world = ((CommandSourceStack)arguments.getSource()).getLevel();
         Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
         if (entity == null) {
            entity = FakePlayerFactory.getMinecraft(world);
         }

         if (entity instanceof Player player) {
            if (!player.level().isClientSide) {
               AABB hitbox = entity.getBoundingBox().inflate((double)5.0F);

               for(Entity entity1 : entity.level().getEntities(entity, hitbox)) {
                  if (entity1 instanceof CorpseEntity) {
                     CorpseEntity corpseEntity = (CorpseEntity)entity1;
                     player.displayClientMessage(Component.literal("isAdapted ? " + corpseEntity.getOwnerAda()), false);
                     player.displayClientMessage(Component.literal("ID ? " + corpseEntity.getCorpseType()), false);
                     player.displayClientMessage(Component.literal("Timer ? " + corpseEntity.getTimer()), false);

                     for(int i = 0; i < corpseEntity.getInventory().getContainerSize(); ++i) {
                        ItemStack stack = corpseEntity.getInventory().getItem(i);
                        if (stack != ItemStack.EMPTY) {
                           player.displayClientMessage(Component.literal("ID ? " + stack.getItem().asItem().getDescription()), false);
                        }
                     }
                  }

                  if (entity1 instanceof Infected) {
                     Infected infected = (Infected)entity1;
                     String var61 = infected.getEncodeId();
                     player.displayClientMessage(Component.literal("Entity " + var61 + " " + infected.getCustomName()), false);
                     float var62 = infected.getHealth();
                     player.displayClientMessage(Component.literal("Current Health " + var62 + "/" + infected.getMaxHealth()), false);
                     player.displayClientMessage(Component.literal("Kills " + infected.getKills()), false);
                     player.displayClientMessage(Component.literal("Evolution Points " + infected.getEvoPoints()), false);
                     player.displayClientMessage(Component.literal("Position to be Searched " + infected.getSearchPos()), false);
                     player.displayClientMessage(Component.literal("Buffs " + infected.getActiveEffects()), false);
                     int var63 = infected.getEvolutionCoolDown();
                     player.displayClientMessage(Component.literal("Seconds until evolution: " + var63 + "/" + SConfig.SERVER.evolution_age_human.get()), false);
                     var63 = infected.getHunger();
                     player.displayClientMessage(Component.literal("Seconds until starvation: " + var63 + "/" + SConfig.SERVER.hunger.get()), false);
                     player.displayClientMessage(Component.literal("Is Linked ? " + infected.getLinked()), false);
                     player.displayClientMessage(Component.literal("Target ? " + infected.getTarget()), false);
                     player.displayClientMessage(Component.literal("Partner ? " + infected.getFollowPartner()), false);
                     if (infected instanceof Scamper) {
                        Scamper scamper = (Scamper)infected;
                        var63 = scamper.getAge();
                        player.displayClientMessage(Component.literal("Time before overtake ? " + var63 + "/" + SConfig.SERVER.scamper_age.get()), false);
                     }

                     if (infected instanceof Hyper) {
                        Hyper scamper = (Hyper)infected;
                        player.displayClientMessage(Component.literal("get nest location ? " + scamper.getNestLocation()), false);
                     }

                     if (infected instanceof Naiad) {
                        Naiad scamper = (Naiad)infected;
                        player.displayClientMessage(Component.literal("get nest location ? " + scamper.getTerritory()), false);
                     }

                     if (infected instanceof GastGeber) {
                        GastGeber geber = (GastGeber)infected;
                        player.displayClientMessage(Component.literal("RootTimer ? " + geber.getTimeRooted()), false);
                        player.displayClientMessage(Component.literal("Aggression ? " + geber.getAggression()), false);
                        player.displayClientMessage(Component.literal("Spread ? " + geber.getSpreadInterval()), false);
                     }

                     player.displayClientMessage(Component.literal("-------------------------"), false);
                  } else if (entity1 instanceof Calamity) {
                     Calamity calamity = (Calamity)entity1;
                     String var56 = calamity.getEncodeId();
                     player.displayClientMessage(Component.literal("Entity " + var56 + " " + calamity.getCustomName()), false);
                     float var57 = calamity.getHealth();
                     player.displayClientMessage(Component.literal("Current Health " + var57 + "/" + calamity.getMaxHealth()), false);
                     player.displayClientMessage(Component.literal("Kills " + calamity.getKills()), false);
                     player.displayClientMessage(Component.literal("Position to be Searched " + calamity.getSearchArea()), false);
                     player.displayClientMessage(Component.literal("Buffs " + calamity.getActiveEffects()), false);
                     player.displayClientMessage(Component.literal("Target ? " + calamity.getTarget()), false);
                     player.displayClientMessage(Component.literal("Mutation Color ? " + calamity.getMutationColor()), false);
                     if (calamity instanceof Sieger) {
                        Sieger sieger = (Sieger)calamity;
                        var57 = sieger.getTailHp();
                        player.displayClientMessage(Component.literal("Tail health " + var57 + "/" + sieger.getMaxTailHp()), false);
                     }

                     if (calamity instanceof Gazenbrecher) {
                        Gazenbrecher sieger = (Gazenbrecher)calamity;
                        var57 = sieger.getTongueHp();
                        player.displayClientMessage(Component.literal("Tongue health " + var57 + "/" + sieger.getMaxTongueHp()), false);
                        boolean var60 = sieger.isAdaptedToFire();
                        player.displayClientMessage(Component.literal("Is adapted to fire " + var60 + " fire points" + sieger.getAdaptationCount()), false);
                     }

                     if (calamity instanceof Hinderburg) {
                        Hinderburg sieger = (Hinderburg)calamity;
                        player.displayClientMessage(Component.literal("Is armed " + sieger.isArmed()), false);
                     }

                     if (calamity instanceof Hohlfresser) {
                        Hohlfresser sieger = (Hohlfresser)calamity;
                        player.displayClientMessage(Component.literal("Underground " + sieger.isUnderground()), false);
                        player.displayClientMessage(Component.literal("Ores ? " + sieger.getOres()), false);
                     }

                     player.displayClientMessage(Component.literal("-------------------------"), false);
                  } else if (entity1 instanceof Mound) {
                     Mound mound = (Mound)entity1;
                     String var52 = mound.getEncodeId();
                     player.displayClientMessage(Component.literal("Entity " + var52 + " " + mound.getCustomName()), false);
                     float var53 = mound.getHealth();
                     player.displayClientMessage(Component.literal("Current Health " + var53 + "/" + mound.getMaxHealth()), false);
                     player.displayClientMessage(Component.literal("Is Linked ? " + mound.getLinked()), false);
                     player.displayClientMessage(Component.literal("Age " + mound.getAge()), false);
                     int var54 = mound.getAgeCounter();
                     player.displayClientMessage(Component.literal("Seconds until growth " + var54 + "/" + SConfig.SERVER.mound_age.get()), false);
                     var54 = mound.getCounter();
                     player.displayClientMessage(Component.literal("Seconds until puff " + var54 + "/" + mound.getMaxCounter()), false);
                     player.displayClientMessage(Component.literal("Buffs " + mound.getActiveEffects()), false);
                     player.displayClientMessage(Component.literal("-------------------------"), false);
                  } else if (entity1 instanceof Proto) {
                     Proto proto = (Proto)entity1;
                     String var50 = proto.getEncodeId();
                     player.displayClientMessage(Component.literal("Entity " + var50 + " " + proto.getCustomName()), false);
                     float var51 = proto.getHealth();
                     player.displayClientMessage(Component.literal("Current Health " + var51 + "/" + proto.getMaxHealth()), false);
                     player.displayClientMessage(Component.literal("Current Target " + proto.getTarget()), false);
                     player.displayClientMessage(Component.literal("Buffs " + proto.getActiveEffects()), false);
                     player.displayClientMessage(Component.literal("Mobs under control " + proto.getHosts()), false);
                     player.displayClientMessage(Component.literal("Biomass " + proto.getBiomass()), false);

                     for(int i = 0; i < proto.getWeights().length; ++i) {
                        player.displayClientMessage(Component.literal("Neuron_" + i + " " + proto.getWeightsValue(i)), false);
                     }

                     for(String s : proto.team_1) {
                        player.displayClientMessage(Component.literal("TEAM_1 " + s), false);
                     }

                     for(String s : proto.team_2) {
                        player.displayClientMessage(Component.literal("TEAM_2 " + s), false);
                     }

                     for(String s : proto.team_3) {
                        player.displayClientMessage(Component.literal("TEAM_3 " + s), false);
                     }

                     for(String s : proto.team_4) {
                        player.displayClientMessage(Component.literal("TEAM_4 " + s), false);
                     }

                     for(String s : proto.team_5) {
                        player.displayClientMessage(Component.literal("Beloved mobs " + s), false);
                     }

                     player.displayClientMessage(Component.literal("-------------------------"), false);
                  } else if (!(entity1 instanceof Womb)) {
                     if (entity1 instanceof Vigil) {
                        Vigil vigil = (Vigil)entity1;
                        String var43 = vigil.getEncodeId();
                        player.displayClientMessage(Component.literal("Entity " + var43 + " " + vigil.getCustomName()), false);
                        player.displayClientMessage(Component.literal("Current Health " + vigil.getHealth()), false);
                        player.displayClientMessage(Component.literal("Buffs " + vigil.getActiveEffects()), false);
                        player.displayClientMessage(Component.literal("State " + vigil.getTrigger()), false);
                        player.displayClientMessage(Component.literal("Horde size " + vigil.getWaveSize()), false);
                        player.displayClientMessage(Component.literal("Time until it leaves " + vigil.getTimer() + "/6000"), false);
                        player.displayClientMessage(Component.literal("-------------------------"), false);
                     } else if (entity1 instanceof Umarmer) {
                        Umarmer umarmer = (Umarmer)entity1;
                        String var44 = umarmer.getEncodeId();
                        player.displayClientMessage(Component.literal("Entity " + var44 + " " + umarmer.getCustomName()), false);
                        player.displayClientMessage(Component.literal("Current Health " + umarmer.getHealth()), false);
                        player.displayClientMessage(Component.literal("Buffs " + umarmer.getActiveEffects()), false);
                        player.displayClientMessage(Component.literal("Shielded? " + umarmer.isShielding()), false);
                        player.displayClientMessage(Component.literal("Pins? " + umarmer.isPinned()), false);
                        player.displayClientMessage(Component.literal("Time until it leaves " + umarmer.getTimer() + "/2400"), false);
                        player.displayClientMessage(Component.literal("-------------------------"), false);
                     } else if (entity1 instanceof Brauerei) {
                        Brauerei brauerei = (Brauerei)entity1;
                        String var45 = brauerei.getEncodeId();
                        player.displayClientMessage(Component.literal("Entity " + var45 + " " + brauerei.getCustomName()), false);
                        player.displayClientMessage(Component.literal("Current Health " + brauerei.getHealth()), false);
                        player.displayClientMessage(Component.literal("Buffs " + brauerei.getActiveEffects()), false);
                        player.displayClientMessage(Component.literal("Time until it leaves " + brauerei.getTimer() + "/300"), false);
                        player.displayClientMessage(Component.literal("-------------------------"), false);
                     } else if (entity1 instanceof Delusionare) {
                        Delusionare delusionare = (Delusionare)entity1;
                        String var46 = delusionare.getEncodeId();
                        player.displayClientMessage(Component.literal("Entity " + var46 + " " + delusionare.getCustomName()), false);
                        player.displayClientMessage(Component.literal("Current Health " + delusionare.getHealth()), false);
                        player.displayClientMessage(Component.literal("Buffs " + delusionare.getActiveEffects()), false);
                        player.displayClientMessage(Component.literal("Target ? " + delusionare.getTarget()), false);
                        int var47 = delusionare.getSpellById();
                        player.displayClientMessage(Component.literal("Magic state " + var47 + " casting " + delusionare.isCasting()), false);
                        player.displayClientMessage(Component.literal("-------------------------"), false);
                     } else if (entity1 instanceof Specter) {
                        Specter specter = (Specter)entity1;
                        String var48 = specter.getEncodeId();
                        player.displayClientMessage(Component.literal("Entity " + var48 + " " + specter.getCustomName()), false);
                        player.displayClientMessage(Component.literal("Current Health " + specter.getHealth()), false);
                        player.displayClientMessage(Component.literal("Buffs " + specter.getActiveEffects()), false);
                        player.displayClientMessage(Component.literal("Target ? " + specter.getTarget()), false);
                        player.displayClientMessage(Component.literal("Target Pos " + specter.getTargetPos()), false);
                        player.displayClientMessage(Component.literal("Stomach " + specter.getStomach()), false);
                        player.displayClientMessage(Component.literal("Biomass " + specter.getBiomass()), false);
                        player.displayClientMessage(Component.literal("-------------------------"), false);
                     } else if (entity1 instanceof InfestedConstruct) {
                        InfestedConstruct construct = (InfestedConstruct)entity1;
                        String var49 = construct.getEncodeId();
                        player.displayClientMessage(Component.literal("Entity " + var49 + " " + construct.getCustomName()), false);
                        player.displayClientMessage(Component.literal("Current Health " + construct.getHealth()), false);
                        player.displayClientMessage(Component.literal("Buffs " + construct.getActiveEffects()), false);
                        player.displayClientMessage(Component.literal("Target ? " + construct.getTarget()), false);
                        player.displayClientMessage(Component.literal("Machine hp " + construct.getMachineHealth()), false);
                        player.displayClientMessage(Component.literal("Metal " + construct.getMetalReserve()), false);
                        player.displayClientMessage(Component.literal("-------------------------"), false);
                     }
                  } else {
                     Womb reformator = (Womb)entity1;
                     String var10001 = reformator.getEncodeId();
                     player.displayClientMessage(Component.literal("Entity " + var10001 + " " + reformator.getCustomName()), false);
                     player.displayClientMessage(Component.literal("Current Health " + reformator.getHealth()), false);
                     player.displayClientMessage(Component.literal("Stored Location " + reformator.getLocation()), false);
                     player.displayClientMessage(Component.literal("Buffs " + reformator.getActiveEffects()), false);
                     player.displayClientMessage(Component.literal("Biomass " + reformator.getBiomass()), false);
                     player.displayClientMessage(Component.literal("State " + reformator.getVariant().getValue()), false);

                     for(String s : reformator.getAttributeIDs()) {
                        player.displayClientMessage(Component.translatable(s), false);
                     }

                     player.displayClientMessage(Component.literal("-------------------------"), false);
                  }
               }
            }
         }

         return 1;
      }).requires((s) -> s.hasPermission(1)));
      dispatcher.register(Commands.literal("spore:check_block_entity").executes((arguments) -> {
         ServerLevel world = ((CommandSourceStack)arguments.getSource()).getLevel();
         Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
         if (entity == null) {
            entity = FakePlayerFactory.getMinecraft(world);
         }

         if (entity != null) {
            AABB aabb = entity.getBoundingBox().inflate((double)5.0F);

            for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
               BlockEntity blockEntity = entity.level().getBlockEntity(blockpos);
               if (entity instanceof Player) {
                  Player player = (Player)entity;
                  if (!player.level().isClientSide) {
                     if (blockEntity instanceof LivingStructureBlocks) {
                        LivingStructureBlocks structureBlocks = (LivingStructureBlocks)blockEntity;
                        player.displayClientMessage(Component.literal("Structure block with " + structureBlocks.getKills() + " kills"), false);
                     } else if (blockEntity instanceof CDUBlockEntity) {
                        CDUBlockEntity block = (CDUBlockEntity)blockEntity;
                        player.displayClientMessage(Component.literal("Fuel " + block.fuel), false);
                     }
                  }
               }
            }
         }

         return 1;
      }).requires((s) -> s.hasPermission(1)));
   }

   @SubscribeEvent
   public static void SpawnPlacement(SpawnPlacementRegisterEvent event) {
      for(Object entry : Sentities.SPORE_ENTITIES.getEntries()) {
         RegistryObject<?> type = (RegistryObject<?>)entry;
         EntityType<?> entityType = (EntityType<?>)type.get();
         if (!blacklist().contains(entityType)) {
            try {
               event.register(entityType, Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Infected::checkMonsterInfectedRules, Operation.AND);
            } catch (Exception e) {
               ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
               Spore.LOGGER.warn("Could not apply custom placement {}: {}", id, e.getMessage());
            }
         }
      }

   }

   private static List<EntityType<?>> blacklist() {
      List<EntityType<?>> values = new ArrayList();
      values.add((EntityType)Sentities.PLAGUED.get());
      values.add((EntityType)Sentities.LACERATOR.get());
      values.add((EntityType)Sentities.BIOBLOOB.get());
      values.add((EntityType)Sentities.SAUGLING.get());
      return values;
   }

   @SubscribeEvent
   public static void Effects(TickEvent.PlayerTickEvent event) {
      Player var2 = event.player;
      if (var2 instanceof ServerPlayer player) {
         if (player.hasEffect((MobEffect)Seffects.CORROSION.get()) && player.tickCount % 60 == 0) {
            player.getInventory().hurtArmor(SdamageTypes.acid(player), 0.5F, Inventory.ALL_ARMOR_SLOTS);
         }

         if (player.hasEffect((MobEffect)Seffects.SYMBIOSIS.get()) && player.tickCount % 200 == 0) {
            int size = player.getInventory().getContainerSize();

            for(int i = 0; i <= size; ++i) {
               ItemStack itemStack = player.getInventory().getItem(i);
               if (EnchantmentHelper.getTagEnchantmentLevel((Enchantment)Senchantments.SYMBIOTIC_RECONSTITUTION.get(), itemStack) != 0 && itemStack.isDamaged()) {
                  Item var7 = itemStack.getItem();
                  if (var7 instanceof SporeToolsBaseItem) {
                     SporeToolsBaseItem base = (SporeToolsBaseItem)var7;
                     base.healTool(itemStack, 2);
                  } else {
                     var7 = itemStack.getItem();
                     if (var7 instanceof SporeArmorData) {
                        SporeArmorData base = (SporeArmorData)var7;
                        base.healTool(itemStack, 2);
                     } else {
                        int l = itemStack.getDamageValue() - 2;
                        itemStack.setDamageValue(l);
                     }
                  }
               }
            }
         }
      }

   }

   @SubscribeEvent
   public static void drops(LootingLevelEvent event) {
      if (event.getDamageSource() != null) {
         Entity entity = event.getDamageSource().getDirectEntity();
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            Item var4 = living.getMainHandItem().getItem();
            if (var4 instanceof LootModifierWeapon) {
               LootModifierWeapon lootModifierWeapon = (LootModifierWeapon)var4;
               event.setLootingLevel(lootModifierWeapon.getLootingLevel());
            }
         }

      }
   }

   @SubscribeEvent
   public static void FishingAnInfectedDrowned(ItemFishedEvent event) {
      if (event != null && Math.random() < 0.05 && event.getHookEntity().isOpenWaterFishing()) {
         InfectedDrowned infectedDrowned = new InfectedDrowned((EntityType)Sentities.INF_DROWNED.get(), event.getEntity().level());
         infectedDrowned.moveTo(event.getHookEntity().getX(), event.getHookEntity().getY(), event.getHookEntity().getZ());
         infectedDrowned.setKills(1);
         infectedDrowned.setTarget(event.getEntity());
         event.getEntity().level().addFreshEntity(infectedDrowned);
      }

   }

   @SubscribeEvent
   public static void ExplosiveBite(LivingEntityUseItemEvent.Finish event) {
      if (event != null && !event.getEntity().level().isClientSide) {
         Item item = event.getItem().getItem();
         if (item == Sitems.ROASTED_TUMOR.get() && Math.random() < 0.2) {
            LivingEntity entity = event.getEntity();
            entity.level().explode((Entity)null, entity.getX(), entity.getY(), entity.getZ(), 0.5F, ExplosionInteraction.NONE);
         }

         if (item == Sitems.MILKY_SACK.get()) {
            LivingEntity entity = event.getEntity();
            List<MobEffectInstance> effectsToRemove = new ArrayList();
            entity.getActiveEffects().forEach((mobEffectInstance) -> {
               if (!mobEffectInstance.getEffect().isBeneficial()) {
                  effectsToRemove.add(mobEffectInstance);
               }

            });
            effectsToRemove.forEach((mobEffectInstance) -> entity.removeEffect(mobEffectInstance.getEffect()));
         }
      }

   }

   @SubscribeEvent
   public static void LoadCalamity(EntityEvent.EnteringSection event) {
      Entity entity = event.getEntity();
      if (entity instanceof ChunkLoaderMob mob) {
         Level var4 = entity.level();
         if (var4 instanceof ServerLevel serverLevel) {
            SectionPos OldChunk = event.getOldPos();
            SectionPos NewChunk = event.getNewPos();
            if (mob.shouldLoadChunk() && event.didChunkChange() && OldChunk != NewChunk) {
               ChunkPos chunk = NewChunk.chunk();
               String var10000 = mob.getChunkId();
               String id = var10000 + chunk.toString();
               ChunkLoadRequest request = new ChunkLoadRequest(serverLevel.dimension(), new ChunkPos[]{chunk}, 0, id, (long)mob.chunkLifeTicks(), entity.getUUID());
               ChunkLoaderHelper.addRequest(request);
            }
         }
      }

   }

   @SubscribeEvent
   public static void FallProt(LivingFallEvent event) {
      if (event.getEntity().getItemBySlot(EquipmentSlot.FEET).getItem() == Sitems.INF_UP_BOOTS.get()) {
         event.setDistance(event.getDistance() - 25.0F);
      }

   }

   @SubscribeEvent
   public static void ProtectFromEffect(MobEffectEvent.Applicable event) {
      LivingEntity living = event.getEntity();
      MobEffectInstance instance = event.getEffectInstance();
      MobEffect mobEffect = event.getEffectInstance().getEffect();
      if (living != null) {
         if (mobEffect == Seffects.MYCELIUM.get() && Utilities.helmetList().contains(living.getItemBySlot(EquipmentSlot.HEAD).getItem())) {
            event.setResult(Result.DENY);
         }

         if ((Boolean)SConfig.SERVER.faw_target.get() && event.getEntity().getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("fromanotherworld:things")))) {
            if (mobEffect == Seffects.MARKER.get()) {
               event.setResult(Result.DENY);
            }
         } else if ((Boolean)SConfig.SERVER.skulk_target.get() && event.getEntity().getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("sculkhorde:sculk_entity"))) && mobEffect == Seffects.MARKER.get()) {
            event.setResult(Result.DENY);
         }

         if (living.getItemBySlot(EquipmentSlot.HEAD).getItem() == Sitems.INF_UP_HELMET.get() && mobEffect == Seffects.MADNESS.get() && instance.getAmplifier() < 1) {
            event.setResult(Result.DENY);
         }
      }

   }

   @SubscribeEvent
   public static void DiscardProto(EntityLeaveLevelEvent event) {
      Entity var2 = event.getEntity();
      if (var2 instanceof Protector protector) {
         SporeSavedData.removeProtector(protector);
      }

      var2 = event.getEntity();
      if (var2 instanceof Proto proto) {
         if (event.getLevel() instanceof ServerLevel) {
            SporeSavedData.removeProto(proto);
         }
      }

   }

   @SubscribeEvent
   public static void onProjectileImpact(ProjectileImpactEvent event) {
      if (event.getProjectile() instanceof Snowball && event.getRayTraceResult().getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
         Entity entity = ((EntityHitResult)event.getRayTraceResult()).getEntity();
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (living.canFreeze()) {
               living.setTicksFrozen(living.getTicksFrozen() + 100);
            }
         }
      }

   }

   @SubscribeEvent
   public static void NoSleep(PlayerSleepInBedEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (player.hasEffect((MobEffect)Seffects.UNEASY.get())) {
            player.displayClientMessage(Component.translatable("uneasy.message"), true);
            event.setResult(BedSleepingProblem.OTHER_PROBLEM);
         }
      }

   }

   @SubscribeEvent
   public static void DefenseBypass(LivingDamageEvent event) {
      Entity living = event.getSource().getEntity();
      if (living instanceof Player player) {
         if (event.getEntity().getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            ItemStack weapon = player.getMainHandItem();
            Item var5 = weapon.getItem();
            if (var5 instanceof PCI) {
               PCI pci = (PCI)var5;
               if (pci.getCharge(weapon) > 0 && !player.getCooldowns().isOnCooldown(pci)) {
                  int damageMod = (Integer)SConfig.SERVER.pci_damage_multiplier.get();
                  int charge = pci.getCharge(weapon);
                  LivingEntity target = event.getEntity();
                  boolean freeze = event.getEntity().getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES);
                  float targetHealth = freeze ? target.getHealth() / (float)damageMod : target.getHealth();
                  int freezeDamage = (float)charge >= targetHealth ? (int)targetHealth : charge;
                  float newDamage = event.getAmount() + (float)(freeze ? freezeDamage * damageMod : freezeDamage);
                  event.setAmount(newDamage);
                  pci.setCharge(weapon, charge - freezeDamage);
                  target.setTicksFrozen(Math.max(target.getTicksFrozen(), 600));
                  target.addEffect(new MobEffectInstance((MobEffect)Seffects.FROSTBITE.get(), 2400, 4));
                  player.getCooldowns().addCooldown(pci, (int)Math.ceil((double)(targetHealth / 5.0F)) * 20);
                  pci.playSound(player);
               }
            }
         }
      }

      LivingEntity attacker = event.getEntity();
      if (attacker instanceof Infected victim) {
         if (!(victim instanceof Protector)) {
            LivingEntity var10000;
            if (living instanceof LivingEntity) {
               LivingEntity e = (LivingEntity)living;
               var10000 = e;
            } else {
               var10000 = null;
            }

            attacker = var10000;
            List<Protector> protectorList = SporeSavedData.protectorList();
            if (!protectorList.isEmpty() && attacker != null) {
               for(Protector protector1 : protectorList) {
                  double d0 = (double)protector1.distanceTo(attacker);
                  if (protector1.isAlive() && d0 < (double)64.0F && !attacker.isSpectator() && Utilities.TARGET_SELECTOR.Test(attacker)) {
                     protector1.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));
                     protector1.setTarget(attacker);
                  }
               }
            }
         }
      }

      if (living instanceof ArmorPersentageBypass bypass) {
         float original_damage = event.getAmount();
         float recalculatedDamage = bypass.amountOfDamage(original_damage);
         if (recalculatedDamage >= 0.0F && original_damage < recalculatedDamage) {
            event.setAmount(recalculatedDamage);
         }
      }

      if (living instanceof LivingEntity livingEntity) {
         Item heldItem = livingEntity.getMainHandItem().getItem();
         if (heldItem instanceof DamagePiercingModifier piercingModifier) {
            float originalDamage = event.getAmount();
            float recalculatedDamage = piercingModifier.getMinimalDamage(originalDamage);
            if (recalculatedDamage >= 0.0F && originalDamage < recalculatedDamage) {
               event.setAmount(recalculatedDamage);
            }
         }
      }

      if (living instanceof Infected || living instanceof UtilityEntity && !(living instanceof Illusion)) {
         LivingEntity livingEntity = event.getEntity();
         MobEffectInstance mobEffectInstance = livingEntity.getEffect((MobEffect)Seffects.MADNESS.get());
         if (mobEffectInstance != null) {
            int level = mobEffectInstance.getAmplifier();
            int duration = mobEffectInstance.getDuration() + 1200;
            boolean jumpLevel = duration < 12000;
            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MADNESS.get(), jumpLevel ? duration : duration - 12000, jumpLevel ? level : level + 1));
         }
      }

      attacker = event.getEntity();
      if (attacker instanceof Player player) {
         float totalDamageModification = 0.0F;

         for(ItemStack stack : player.getArmorSlots()) {
            Item smartMob = stack.getItem();
            if (smartMob instanceof SporeBaseArmor armor) {
               totalDamageModification += armor.calculateAdditionalDamage(event.getSource(), stack, event.getAmount());
            }
         }

         event.setAmount(event.getAmount() + totalDamageModification);
      }

      Entity var27 = event.getSource().getEntity();
      if (var27 instanceof ServerPlayer serverPlayer) {
         int i = 0;

         for(ItemStack stack : serverPlayer.getInventory().armor) {
            Item smartMob = stack.getItem();
            if (smartMob instanceof SporeBaseArmor baseArmor) {
               if (baseArmor.getVariant(stack) == SporeArmorMutations.CHARRED) {
                  i += 2;
               }
            }
         }

         if (i > 0) {
            event.getEntity().setSecondsOnFire(i);
         }
      }

      var27 = event.getSource().getEntity();
      if (var27 instanceof Mob hivemindAttacker) {
         CompoundTag data = hivemindAttacker.getPersistentData();
         if (data.contains("hivemind")) {
            int summonerUUID = data.getInt("hivemind");
            Level level = hivemindAttacker.level();
            Entity summoner = level.getEntity(summonerUUID);
            if (summoner instanceof Proto) {
               Proto smartMob = (Proto)summoner;
               int decision = data.getInt("decision");
               int member = data.getInt("member");
               smartMob.praisedForDecision(decision, member);
            }
         }
      }

      LivingEntity var31 = event.getEntity();
      if (var31 instanceof Mob creature) {
         CompoundTag data = creature.getPersistentData();
         if (data.contains("hivemind")) {
            int summonerUUID = data.getInt("hivemind");
            Level level = creature.level();
            Entity summoner = level.getEntity(summonerUUID);
            if (summoner instanceof Proto) {
               Proto smartMob = (Proto)summoner;
               int decision = data.getInt("decision");
               int member = data.getInt("member");
               smartMob.punishForDecision(decision, member);
            }
         }
      }

   }

   @SubscribeEvent
   public static void TickEvents(LivingEvent.LivingTickEvent event) {
      LivingEntity tickingEntity = event.getEntity();
      if (tickingEntity instanceof Player player) {
         MobEffectInstance madnessEffect = player.getEffect((MobEffect)Seffects.MADNESS.get());
         if (madnessEffect != null && madnessEffect.getDuration() == 1) {
            int level = madnessEffect.getAmplifier();
            if (level > 0) {
               madnessEffect.update(new MobEffectInstance((MobEffect)Seffects.MADNESS.get(), 12000, level - 1));
            }
         }

         if (player.tickCount % 400 == 0 && player.level().isClientSide) {
            AABB aabb = player.getBoundingBox().inflate((double)5.0F);
            List<BlockPos> list = new ArrayList();

            for(BlockPos blockPos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
               if (player.level().getBlockState(blockPos).is(BlockTags.create(new ResourceLocation("spore:fungal_blocks")))) {
                  list.add(blockPos);
               }
            }

            if (list.size() > 4) {
               player.playSound((SoundEvent)Ssounds.AREA_AMBIENT.get());
            }
         }
      }

      tickingEntity = event.getEntity();
      if (tickingEntity instanceof Mob mob) {
         LivingEntity var12 = mob.getTarget();
         if (var12 instanceof ServerPlayer player) {
            if (mob.tickCount % 20 == 0) {
               if (mob instanceof Calamity) {
                  SporePacketHandler.sendToClient(new SongInitializingPacket(0, true, true), player);
               }

               if (mob instanceof Vanguard) {
                  SporePacketHandler.sendToClient(new SongInitializingPacket(1, true, true), player);
               }

               if (mob instanceof Vigil) {
                  SporePacketHandler.sendToClient(new SongInitializingPacket(2, true, true), player);
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void horseArmorTick(LivingEvent.LivingTickEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof AbstractHorse horse) {
         ItemStack armor = horse.getItemBySlot(EquipmentSlot.CHEST);
         Item var4 = armor.getItem();
         if (var4 instanceof SporeHorseArmor armorItem) {
            armorItem.onHorseArmorTick(armor, horse.level(), horse);
         }

      }
   }

   @SubscribeEvent
   public static void onAttack(AttackEntityEvent event) {
      Player player = event.getEntity();
      if (player.getMainHandItem().getItem() instanceof AbstractSporeGun) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
      Player player = event.getEntity();
      if (player.getMainHandItem().getItem() instanceof AbstractSporeGun) {
         event.setCanceled(true);
      }

   }
}
