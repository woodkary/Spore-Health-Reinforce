package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Recipes.GraftingRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class GraftingRecipeScreen extends AbstractContainerScreen<GraftingRecipeMenu> implements TutorialMenuMethods {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/gui/grafting_recipe_gui.png");
   private int currentItemIndex = 0;
   private Button leftButton;
   private Button rightButton;
   private final List recipes;
   public static final ResourceLocation UID = new ResourceLocation("spore", "grafting");

   public GraftingRecipeScreen(GraftingRecipeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.imageWidth = 176;
      this.imageHeight = 84;
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         this.recipes = new ArrayList();
      } else {
         this.recipes = level.getRecipeManager().getAllRecipesFor(GraftingRecipe.GraftingRecipeType.INSTANCE);
      }

   }

   protected void init() {
      super.init();
      this.inventoryLabelY = 10000;
      this.titleLabelY = 10000;
      int buttonY = this.topPos - 20;
      int buttonX = this.leftPos + 88;
      this.leftButton = (Button)this.addRenderableWidget(Button.builder(Component.literal("<"), (button) -> this.changeRecipe(-1)).bounds(buttonX - 10, buttonY, 20, 20).build());
      this.rightButton = (Button)this.addRenderableWidget(Button.builder(Component.literal(">"), (button) -> this.changeRecipe(1)).bounds(buttonX + 10, buttonY, 20, 20).build());
   }

   private void changeRecipe(int direction) {
      if (!this.recipes.isEmpty()) {
         this.currentItemIndex = (this.currentItemIndex + direction) % this.recipes.size();
         if (this.currentItemIndex < 0) {
            this.currentItemIndex += this.recipes.size();
         }
      }

   }

   protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderTexture(0, TEXTURE);
      guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      RenderSystem.disableBlend();
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
      this.renderBackground(guiGraphics);
      super.render(guiGraphics, mouseX, mouseY, delta);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
      GraftingRecipe recipe = (GraftingRecipe)this.recipes.get(this.currentItemIndex);
      if (recipe != null) {
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(21)), this.leftPos + 25, this.topPos + 8);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(22)), this.leftPos + 25, this.topPos + 35);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(23)), this.leftPos + 25, this.topPos + 62);
         ItemStack stack = recipe.getResultItem((RegistryAccess)null);
         this.renderFakeItem(this.font, guiGraphics, stack, this.leftPos + 88, this.topPos + 35);
      }
   }

   private ItemStack getItemStackFromIngredient(Ingredient ingredient) {
      ItemStack[] itemStacks = ingredient.getItems();
      return itemStacks.length > 0 ? itemStacks[0] : ItemStack.EMPTY;
   }
}
