package com.Harbinger.Spore.ExtremelySusThings;

import com.Harbinger.Spore.Core.SConfig;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.world.ModifiableStructureInfo;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class StructureModification implements StructureModifier {
   private static final RegistryObject SERIALIZER;

   public void modify(Holder structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder) {
      if (structure.is(TagKey.create(Registries.STRUCTURE, new ResourceLocation("spore", "laboratories"))) && !((List)SConfig.SERVER.structure_spawns.get()).isEmpty()) {
         for(String str : (List<String>)SConfig.SERVER.structure_spawns.get()) {
            String[] string = str.split("\\|");
            EntityType<?> entity = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(string[0]));
            if (entity != null) {
               builder.getStructureSettings().getOrAddSpawnOverrides(entity.getCategory()).addSpawn(new MobSpawnSettings.SpawnerData(entity, Integer.parseUnsignedInt(string[1]), Integer.parseUnsignedInt(string[2]), Integer.parseUnsignedInt(string[3])));
            }
         }
      }

   }

   public static Codec makeCodec() {
      return Codec.unit(StructureModification::new);
   }

   public Codec codec() {
      return (Codec)SERIALIZER.get();
   }

   static {
      SERIALIZER = RegistryObject.create(new ResourceLocation("spore", "spore_structure_spawns"), Keys.STRUCTURE_MODIFIER_SERIALIZERS, "spore");
   }
}
