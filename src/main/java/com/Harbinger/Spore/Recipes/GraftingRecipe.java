package com.Harbinger.Spore.Recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GraftingRecipe implements Recipe<SimpleContainer> {
   private final NonNullList<Ingredient> inputItems;
   private final ItemStack output;
   private final ResourceLocation id;

   public GraftingRecipe(NonNullList<Ingredient> inputItems, ItemStack output, ResourceLocation id) {
      this.inputItems = inputItems;
      this.output = output;
      this.id = id;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.inputItems;
   }

   public boolean matches(SimpleContainer simpleContainer, Level level) {
      if (!level.isClientSide() && simpleContainer.getContainerSize() >= 24) {
         for(int i = 21; i < 24; ++i) {
            if (!((Ingredient)this.inputItems.get(i)).test(simpleContainer.getItem(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public ItemStack assemble(SimpleContainer simpleContainer, RegistryAccess registryAccess) {
      return this.output == null ? ItemStack.EMPTY : this.output.copy();
   }

   public boolean canCraftInDimensions(int width, int height) {
      return true;
   }

   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return this.output.copy();
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return GraftingRecipeSerializer.INSTANCE;
   }

   public RecipeType<?> getType() {
      return GraftingRecipeType.INSTANCE;
   }

   public static class GraftingRecipeType implements RecipeType<GraftingRecipe> {
      public static final GraftingRecipeType INSTANCE = new GraftingRecipeType();
      public static final String ID = "grafting";

      private GraftingRecipeType() {
      }
   }

   public static class GraftingRecipeSerializer implements RecipeSerializer<GraftingRecipe> {
      public static final GraftingRecipeSerializer INSTANCE = new GraftingRecipeSerializer();
      public static final ResourceLocation ID = new ResourceLocation("spore", "grafting");

      public GraftingRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
         ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "output"));
         JsonArray ingredients = GsonHelper.getAsJsonArray(jsonObject, "ingredients");
         NonNullList<Ingredient> inputs = NonNullList.withSize(24, Ingredient.EMPTY);

         for(int i = 0; i < ingredients.size(); ++i) {
            JsonObject ingredientJson = ingredients.get(i).getAsJsonObject();
            int slot = GsonHelper.getAsInt(ingredientJson, "slot", i + 21);
            if (slot >= 0 && slot < inputs.size()) {
               inputs.set(slot, Ingredient.fromJson(ingredientJson));
            }
         }

         return new GraftingRecipe(inputs, output, resourceLocation);
      }

      public @Nullable GraftingRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
         NonNullList<Ingredient> inputs = NonNullList.withSize(friendlyByteBuf.readInt(), Ingredient.EMPTY);
         inputs.replaceAll((ignored) -> Ingredient.fromNetwork(friendlyByteBuf));
         ItemStack output = friendlyByteBuf.readItem();
         return new GraftingRecipe(inputs, output, resourceLocation);
      }

      public void toNetwork(FriendlyByteBuf friendlyByteBuf, GraftingRecipe surgeryRecipe) {
         friendlyByteBuf.writeInt(surgeryRecipe.inputItems.size());

         for(Ingredient ingredient : surgeryRecipe.getIngredients()) {
            ingredient.toNetwork(friendlyByteBuf);
         }

         friendlyByteBuf.writeItemStack(surgeryRecipe.getResultItem((RegistryAccess)null), false);
      }
   }
}
