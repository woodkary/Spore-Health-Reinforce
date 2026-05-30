package com.Harbinger.Spore.Recipes;

import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WombRecipe implements Recipe<EntityContainer> {
   private final ResourceLocation id;
   private final List<Pair> entityPairs;
   private final String attribute;
   private final ResourceLocation icon;

   public WombRecipe(ResourceLocation id, List<Pair> entityPairs, String attribute, ResourceLocation icon) {
      this.id = id;
      this.entityPairs = entityPairs;
      this.attribute = attribute;
      this.icon = icon;
   }

   public List<Pair> getEntityPairs() {
      return this.entityPairs;
   }

   public String getAttribute() {
      return this.attribute;
   }

   public ResourceLocation getIcon() {
      return this.icon;
   }

   public boolean matches(EntityContainer entityContainer, Level level) {
      if (level.isClientSide) {
         return false;
      } else {
         EntityType<?> entityType = entityContainer.entity().getType();

         for(Pair pair : this.entityPairs) {
            EntityType<?> expectedType = EntityType.byString(pair.entityId()).orElse(null);
            if (expectedType != null) {
               Entity var8 = entityContainer.entity();
               if (var8 instanceof VariantKeeper) {
                  VariantKeeper keeper = (VariantKeeper)var8;
                  if (keeper.getTypeVariant() == pair.type() && entityType.equals(expectedType)) {
                     return true;
                  }
               } else if (entityType.equals(expectedType)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public ItemStack assemble(EntityContainer entityContainer, RegistryAccess registryAccess) {
      return ItemStack.EMPTY;
   }

   public boolean canCraftInDimensions(int i, int i1) {
      return true;
   }

   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return ItemStack.EMPTY;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return WombRecipeSerializer.INSTANCE;
   }

   public RecipeType<?> getType() {
      return WombRecipeType.INSTANCE;
   }

   public static record Pair(String entityId, int type) {
   }

   public static class WombRecipeType implements RecipeType<WombRecipe> {
      public static final WombRecipeType INSTANCE = new WombRecipeType();
      public static final String ID = "assimilation";

      private WombRecipeType() {
      }
   }

   public static class WombRecipeSerializer implements RecipeSerializer<WombRecipe> {
      public static final WombRecipeSerializer INSTANCE = new WombRecipeSerializer();
      public static final ResourceLocation ID = new ResourceLocation("spore", "assimilation");

      public WombRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
         List<Pair> entityPairs = new ArrayList();
         JsonArray entities = GsonHelper.getAsJsonArray(jsonObject, "entities");

         for(int i = 0; i < entities.size(); ++i) {
            JsonObject obj = entities.get(i).getAsJsonObject();
            String entityId = GsonHelper.getAsString(obj, "entity");
            int type = GsonHelper.getAsInt(obj, "entity_type");
            entityPairs.add(new Pair(entityId, type));
         }

         String attribute = GsonHelper.getAsString(jsonObject, "attribute");
         ResourceLocation icon = new ResourceLocation(GsonHelper.getAsString(jsonObject, "icon"));
         return new WombRecipe(resourceLocation, entityPairs, attribute, icon);
      }

      public @Nullable WombRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
         int size = buf.readInt();
         List<Pair> entityPairs = new ArrayList();

         for(int i = 0; i < size; ++i) {
            String entityId = buf.readUtf();
            int type = buf.readInt();
            entityPairs.add(new Pair(entityId, type));
         }

         String attribute = buf.readUtf();
         ResourceLocation icon = buf.readResourceLocation();
         return new WombRecipe(resourceLocation, entityPairs, attribute, icon);
      }

      public void toNetwork(FriendlyByteBuf buf, WombRecipe recipe) {
         buf.writeInt(recipe.entityPairs.size());

         for(Pair pair : recipe.entityPairs) {
            buf.writeUtf(pair.entityId());
            buf.writeInt(pair.type());
         }

         buf.writeUtf(recipe.attribute);
         buf.writeResourceLocation(recipe.icon);
      }
   }
}
