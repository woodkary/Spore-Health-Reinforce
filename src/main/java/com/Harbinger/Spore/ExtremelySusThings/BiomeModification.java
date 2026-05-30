package com.Harbinger.Spore.ExtremelySusThings;

import com.Harbinger.Spore.Spore;
import com.Harbinger.Spore.Core.SConfig;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.Tags.Biomes;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.common.world.BiomeModifier.Phase;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class BiomeModification implements BiomeModifier {
   private static final RegistryObject SERIALIZER;

   public void modify(Holder biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
      if (phase == Phase.ADD) {
         if (!this.isBiomeBlacklisted(biome)) {
            int biomeModifier = biome.is(Biomes.IS_MUSHROOM) ? 20 : 0;

            for(String allowedBiome : (List<String>)SConfig.SERVER.dimension_parameters.get()) {
               ResourceLocation biomeLocation = new ResourceLocation(allowedBiome);
               TagKey<Biome> biomeTag = TagKey.create(Registries.BIOME, biomeLocation);
               if (biome.is(biomeTag) || biome.is(biomeLocation)) {
                  this.addSpawns(builder, biomeModifier);
                  break;
               }
            }

         }
      }
   }

   private void addSpawns(ModifiableBiomeInfo.BiomeInfo.Builder builder, int modifier) {
      for(String entry : (List<String>)SConfig.SERVER.spawns.get()) {
         String[] parts = entry.split("\\|");
         if (parts.length != 4) {
            Spore.LOGGER.warn("Invalid spawn config entry: {}", entry);
         } else {
            ResourceLocation entityId = new ResourceLocation(parts[0]);
            EntityType<?> entityType = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(entityId);
            if (entityType == null) {
               Spore.LOGGER.warn("Unknown entity type: {}", parts[0]);
            } else {
               try {
                  int weight = Integer.parseUnsignedInt(parts[1]) + modifier;
                  int minCount = Integer.parseUnsignedInt(parts[2]);
                  int maxCount = Integer.parseUnsignedInt(parts[3]);
                  builder.getMobSpawnSettings().getSpawner(entityType.getCategory()).add(new MobSpawnSettings.SpawnerData(entityType, weight, minCount, maxCount));
               } catch (NumberFormatException e) {
                  Spore.LOGGER.error("Invalid spawn config number format in: {}", entry, e);
               }
            }
         }
      }

   }

   private boolean isBiomeBlacklisted(Holder biome) {
      for(String blacklisted : (List<String>)SConfig.SERVER.dimension_blacklist.get()) {
         ResourceLocation blacklistedLocation = new ResourceLocation(blacklisted);
         TagKey<Biome> blacklistTag = TagKey.create(Registries.BIOME, blacklistedLocation);
         if (biome.is(blacklistTag) || biome.is(blacklistedLocation)) {
            return true;
         }
      }

      return false;
   }

   public Codec codec() {
      return (Codec)SERIALIZER.get();
   }

   public static Codec makeCodec() {
      return Codec.unit(BiomeModification::new);
   }

   static {
      SERIALIZER = RegistryObject.create(new ResourceLocation("spore", "inf_spawns"), Keys.BIOME_MODIFIER_SERIALIZERS, "spore");
   }
}
