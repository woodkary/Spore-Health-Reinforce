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
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class SporeConversionReloadListener extends SimpleJsonResourceReloadListener {
   public SporeConversionReloadListener() {
      super(new Gson(), "spore_block_conversion");
   }

   protected void apply(Map<ResourceLocation, JsonElement> jsons, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
      SporeConversionData.clear();

      for(JsonElement element : jsons.values()) {
         JsonObject obj = element.getAsJsonObject();

         for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            ResourceLocation targetId = ResourceLocation.tryParse(entry.getValue().getAsString());
            Block target = (Block)BuiltInRegistries.BLOCK.get(targetId);
            if (key.startsWith("#")) {
               ResourceLocation tagId = ResourceLocation.tryParse(key.substring(1));
               TagKey<Block> tag = TagKey.create(Registries.BLOCK, tagId);
               SporeConversionData.addTag(tag, target);
            } else {
               ResourceLocation fromId = ResourceLocation.tryParse(key);
               Block from = (Block)BuiltInRegistries.BLOCK.get(fromId);
               SporeConversionData.addBlock(from, target);
            }
         }
      }

   }
}
