package com.Harbinger.Spore.Recipes;

import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.google.gson.JsonObject;
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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class InjectionRecipe implements Recipe<EntityContainer> {
   private final ResourceLocation id;
   private final String entityId;
   private final int type;
   private final ItemStack result;

   public InjectionRecipe(ResourceLocation id, String entityId, int type, ItemStack result) {
      this.id = id;
      this.entityId = entityId;
      this.type = type;
      this.result = result;
   }

   public String getEntityId() {
      return this.entityId;
   }

   public int getEntityType() {
      return this.type;
   }

   public boolean matches(EntityContainer entityContainer, Level level) {
      if (level.isClientSide) {
         return false;
      } else {
         EntityType<?> entityType = entityContainer.entity().getType();
         EntityType<?> expectedType = EntityType.byString(this.entityId).orElse(null);
         if (expectedType == null) {
            return false;
         } else {
            Entity var6 = entityContainer.entity();
            if (!(var6 instanceof VariantKeeper)) {
               return entityType.equals(expectedType);
            } else {
               VariantKeeper keeper = (VariantKeeper)var6;
               return keeper.getTypeVariant() == this.getEntityType() && entityType.equals(expectedType);
            }
         }
      }
   }

   public ItemStack assemble(EntityContainer entityContainer, RegistryAccess registryAccess) {
      return this.result == null ? ItemStack.EMPTY : this.result.copy();
   }

   public boolean canCraftInDimensions(int i, int i1) {
      return true;
   }

   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return this.result == null ? ItemStack.EMPTY : this.result.copy();
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return InjectionRecipeSerializer.INSTANCE;
   }

   public RecipeType<?> getType() {
      return InjectionRecipeType.INSTANCE;
   }

   public static class InjectionRecipeType implements RecipeType<InjectionRecipe> {
      public static final InjectionRecipeType INSTANCE = new InjectionRecipeType();
      public static final String ID = "injection";

      private InjectionRecipeType() {
      }
   }

   public static class InjectionRecipeSerializer implements RecipeSerializer<InjectionRecipe> {
      public static final InjectionRecipeSerializer INSTANCE = new InjectionRecipeSerializer();
      public static final ResourceLocation ID = new ResourceLocation("spore", "injection");

      public InjectionRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
         ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "output"));
         String entityId = GsonHelper.getAsString(jsonObject, "entity");
         int type = GsonHelper.getAsInt(jsonObject, "entity_type");
         return new InjectionRecipe(resourceLocation, entityId, type, output);
      }

      public @Nullable InjectionRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
         String entityId = buf.readUtf();
         int type = buf.readInt();
         ItemStack result = buf.readItem();
         return new InjectionRecipe(resourceLocation, entityId, type, result);
      }

      public void toNetwork(FriendlyByteBuf friendlyByteBuf, InjectionRecipe injectionRecipe) {
         friendlyByteBuf.writeUtf(injectionRecipe.entityId);
         friendlyByteBuf.writeInt(injectionRecipe.getEntityType());
         friendlyByteBuf.writeItemStack(injectionRecipe.getResultItem((RegistryAccess)null), false);
      }
   }
}
