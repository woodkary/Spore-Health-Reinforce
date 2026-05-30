package com.Harbinger.Spore.ExtremelySusThings.CustomJsonReader;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class SporeMobConversionData {
   private static final Map<EntityType<?>, EntityType<?>> ENTITY_CONVERSIONS = new HashMap<>();
   private static final Map<TagKey<EntityType<?>>, EntityType<?>> ENTITY_TAG_CONVERSIONS = new HashMap<>();

   public static void clear() {
      ENTITY_CONVERSIONS.clear();
      ENTITY_TAG_CONVERSIONS.clear();
   }

   public static void addBlock(EntityType<?> from, EntityType<?> to) {
      ENTITY_CONVERSIONS.put(from, to);
   }

   public static void addTag(TagKey<EntityType<?>> tag, EntityType<?> to) {
      ENTITY_TAG_CONVERSIONS.put(tag, to);
   }

   public static EntityType<?> getResult(EntityType<?> block) {
      EntityType<?> direct = ENTITY_CONVERSIONS.get(block);
      if (direct != null) {
         return direct;
      } else {
         for(Map.Entry<TagKey<EntityType<?>>, EntityType<?>> entry : ENTITY_TAG_CONVERSIONS.entrySet()) {
            if (block.is(entry.getKey())) {
               return entry.getValue();
            }
         }

         return null;
      }
   }
}
