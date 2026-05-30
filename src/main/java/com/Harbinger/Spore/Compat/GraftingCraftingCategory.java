package com.Harbinger.Spore.Compat;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Recipes.GraftingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class GraftingCraftingCategory implements IRecipeCategory<GraftingRecipe> {
   public static final ResourceLocation UID = new ResourceLocation("spore", "grafting");
   public static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/gui/grafting_gui.png");
   public static final RecipeType<GraftingRecipe> GRAFTING_TYPE;
   private final IDrawable background;
   private final IDrawable icon;

   public GraftingCraftingCategory(IGuiHelper helper) {
      this.background = helper.drawableBuilder(TEXTURE, 0, 0, 176, 82).setTextureSize(176, 166).build();
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack((ItemLike)Sblocks.SURGERY_TABLE.get()));
   }

   public RecipeType<GraftingRecipe> getRecipeType() {
      return GRAFTING_TYPE;
   }

   public Component getTitle() {
      return Component.translatable("block.spore.surgery_table");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, GraftingRecipe recipe, IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.INPUT, 25, 8).addIngredients((Ingredient)recipe.getIngredients().get(21));
      builder.addSlot(RecipeIngredientRole.INPUT, 25, 35).addIngredients((Ingredient)recipe.getIngredients().get(22));
      builder.addSlot(RecipeIngredientRole.INPUT, 25, 62).addIngredients((Ingredient)recipe.getIngredients().get(23));
      ItemStack stack = recipe.getResultItem((RegistryAccess)null);
      builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 35).addItemStack(stack);
   }

   static {
      GRAFTING_TYPE = new RecipeType<>(UID, GraftingRecipe.class);
   }
}
