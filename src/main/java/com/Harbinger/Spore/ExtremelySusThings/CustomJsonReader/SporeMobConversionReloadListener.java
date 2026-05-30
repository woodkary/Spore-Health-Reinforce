package com.Harbinger.Spore.ExtremelySusThings.CustomJsonReader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class SporeMobConversionReloadListener extends SimpleJsonResourceReloadListener {
   public SporeMobConversionReloadListener() {
      super(new Gson(), "spore_mob_conversion");
   }

   protected void apply(Map<ResourceLocation, JsonElement> jsons, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
      SporeMobConversionData.clear();

      for(JsonElement element : jsons.values()) {
         JsonObject obj = element.getAsJsonObject();

         for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            ResourceLocation targetId = ResourceLocation.tryParse(entry.getValue().getAsString());
            EntityType<?> target = (EntityType)BuiltInRegistries.ENTITY_TYPE.get(targetId);
            if (key.startsWith("#")) {
               ResourceLocation tagId = ResourceLocation.tryParse(key.substring(1));
               TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagId);
               SporeMobConversionData.addTag(tag, target);
            } else {
               ResourceLocation fromId = ResourceLocation.tryParse(key);
               EntityType<?> from = (EntityType)BuiltInRegistries.ENTITY_TYPE.get(fromId);
               SporeMobConversionData.addBlock(from, target);
            }
         }
      }

   }
}
