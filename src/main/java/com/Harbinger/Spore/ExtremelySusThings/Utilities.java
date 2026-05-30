package com.Harbinger.Spore.ExtremelySusThings;

import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class Utilities {
   public static final Predicate<Entity> TARGET_SELECTOR_PREDICATE = (entity) -> {
      if (!(entity instanceof Infected) && !(entity instanceof UtilityEntity) && !(entity instanceof TrueCalamity)) {
         if ((entity instanceof AbstractFish || entity instanceof Animal) && !(Boolean)SConfig.SERVER.at_an.get()) {
            return false;
         } else if (!((List)SConfig.SERVER.blacklist.get()).isEmpty()) {
            for(String string : (List<String>)SConfig.SERVER.blacklist.get()) {
               if (string.endsWith(":") && entity.getEncodeId() != null) {
                  String[] mod = string.split(":");
                  String[] iterations = entity.getEncodeId().split(":");
                  if (Objects.equals(mod[0], iterations[0])) {
                     return false;
                  }
               }
            }

            return !((List)SConfig.SERVER.blacklist.get()).contains(entity.getEncodeId());
         } else {
            return true;
         }
      } else {
         return false;
      }
   };
   public static BooleanCache TARGET_SELECTOR;
   public static final TagKey biomass;

   public static void explodeCircle(ServerLevel level, Entity owner, BlockPos pos, double range, float damage, double blockHardness, Predicate<Entity> predicate) {
      explodeCircle(level, owner, pos, range, damage, ParticleTypes.EXPLOSION_EMITTER, false, blockHardness, predicate);
   }

   public static void explodeCircle(ServerLevel level, Entity owner, BlockPos pos, double range, float damage, ParticleOptions particleTypes, boolean dropItems, double blockHardness, Predicate<Entity> predicate) {
      for(int i = 0; (double)i <= (double)2.0F * range; ++i) {
         for(int j = 0; (double)j <= (double)2.0F * range; ++j) {
            for(int k = 0; (double)k <= (double)2.0F * range; ++k) {
               double distance = (double)Mth.sqrt((float)(((double)i - range) * ((double)i - range) + ((double)j - range) * ((double)j - range) + ((double)k - range) * ((double)k - range)));
               if ((Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) && distance < range + (double)0.5F) {
                  BlockPos blockpos = pos.offset(i - (int)range, j - (int)range, k - (int)range);
                  RandomSource source = RandomSource.create();
                  BlockState state = level.getBlockState(blockpos);
                  if ((double)state.getDestroySpeed(level, blockpos) <= blockHardness && state.getDestroySpeed(level, blockpos) >= 0.0F && ForgeEventFactory.getMobGriefingEvent(level, owner)) {
                     level.removeBlock(blockpos, dropItems);
                     if (Math.random() < 0.3) {
                        float value = source.nextFloat() * 0.05F;
                        level.sendParticles(particleTypes, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1, (double)value, (double)0.0F, (double)value, (double)1.0F);
                     }
                  }
               }
            }
         }
      }

      AABB searchbox = AABB.ofSize(new Vec3((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), range * (double)2.0F, range * (double)2.0F, range * (double)2.0F);

      for(Entity entity : level.getEntities(owner, searchbox, predicate)) {
         entity.hurt(level.damageSources().mobAttack((LivingEntity)owner), damage);
      }

      level.playSound((Player)null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS);
   }

   public static void convertBlocks(ServerLevel level, Entity owner, BlockPos pos, double range, BlockState state) {
      for(int i = 0; (double)i <= (double)2.0F * range; ++i) {
         for(int j = 0; (double)j <= (double)2.0F * range; ++j) {
            for(int k = 0; (double)k <= (double)2.0F * range; ++k) {
               double distance = (double)Mth.sqrt((float)(((double)i - range) * ((double)i - range) + ((double)j - range) * ((double)j - range) + ((double)k - range) * ((double)k - range)));
               if ((Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) && distance < range + (double)0.5F) {
                  BlockPos blockpos = pos.offset(i - (int)range, j - (int)range, k - (int)range);
                  if (ForgeEventFactory.getMobGriefingEvent(level, owner) && Math.random() < (double)0.2F && level.getBlockState(blockpos).isAir() && level.getBlockState(blockpos.below()).isSolidRender(level, blockpos)) {
                     level.setBlock(blockpos, state, 3);
                  }
               }
            }
         }
      }

   }

   public static List<Item> helmetList() {
      List<Item> values = new ArrayList<>();

      for(String string : (List<String>)SConfig.SERVER.gas_masks.get()) {
         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
         if (item != null) {
            values.add(item);
         }
      }

      return values;
   }

   public static Vec3 generatePositionAway(Vec3 origin, double distance) {
      Random random = new Random();
      double theta = random.nextDouble() * (double)2.0F * Math.PI;
      double phi = Math.acos((double)2.0F * random.nextDouble() - (double)1.0F);
      double offsetX = Math.sin(phi) * Math.cos(theta) * distance;
      double offsetY = Math.sin(phi) * Math.sin(theta) * distance;
      double offsetZ = Math.cos(phi) * distance;
      return new Vec3(origin.x + offsetX, origin.y + offsetY, origin.z + offsetZ);
   }

   public static List<Item> getItemsFromTag(String namespace, String tagName) {
      TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(namespace, tagName));
      return BuiltInRegistries.ITEM.getTag(tagKey).map((holderSet) -> holderSet.stream().map(Holder::value).collect(Collectors.toList())).orElse(List.of());
   }

   public static void doCustomModifiersAfterEffects(LivingEntity attacker, LivingEntity victim) {
      if (attacker != null && victim != null) {
         AttributeInstance corrosion = attacker.getAttribute((Attribute)SAttributes.CORROSIVES.get());
         if (corrosion != null && corrosion.getValue() >= (double)1.0F) {
            int level = (int)corrosion.getValue() - 1;
            victim.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 300, level), attacker);
         }

         AttributeInstance toxic = attacker.getAttribute((Attribute)SAttributes.TOXICITY.get());
         if (toxic != null && toxic.getValue() >= (double)1.0F) {
            int level = (int)toxic.getValue() - 1;
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 400, level), attacker);
         }

         AttributeInstance local = attacker.getAttribute((Attribute)SAttributes.LOCALIZATION.get());
         if (local != null && local.getValue() >= (double)1.0F) {
            int level = (int)local.getValue() - 1;
            victim.addEffect(new MobEffectInstance((MobEffect)Seffects.MARKER.get(), 600, level), attacker);
         }

         AttributeInstance grind = attacker.getAttribute((Attribute)SAttributes.GRINDING.get());
         if (grind != null && grind.getValue() >= (double)1.0F) {
            double level = grind.getValue();
            victim.getArmorSlots().forEach((itemStack) -> itemStack.setDamageValue(itemStack.getDamageValue() + (int)((double)10.0F * level)));
         }

      }
   }

   public static int mixColors(Map<Integer, Float> colorsAndWeights) {
      float totalWeight = 0.0F;
      float r = 0.0F;
      float g = 0.0F;
      float b = 0.0F;

      for(Map.Entry<Integer, Float> entry : colorsAndWeights.entrySet()) {
         int color = entry.getKey();
         float weight = entry.getValue();
         r += (float)(color >> 16 & 255) * weight;
         g += (float)(color >> 8 & 255) * weight;
         b += (float)(color & 255) * weight;
         totalWeight += weight;
      }

      if (totalWeight == 0.0F) {
         return 16777215;
      } else {
         r /= totalWeight;
         g /= totalWeight;
         b /= totalWeight;
         int finalColor = (int)r << 16 | (int)g << 8 | (int)b;
         return finalColor;
      }
   }

   static {
      TARGET_SELECTOR = new BooleanCache(8, TARGET_SELECTOR_PREDICATE);
      biomass = ((ITagManager)Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())).getTag(BlockTags.create(new ResourceLocation("spore:biomass_to_membrane"))).getKey();
   }
}
