package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Recipes.SurgeryRecipe;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
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
import net.minecraft.world.level.ItemLike;

public class SurgeryRecipeScreen extends AbstractContainerScreen<SurgeryRecipeMenu> implements TutorialMenuMethods {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/gui/surgery_table_recipe_gui.png");
   private final List tagItems = Utilities.getItemsFromTag("spore", "stitches");
   private int currentTagIndex = 0;
   private int tickCounter = 0;
   private int currentItemIndex = 0;
   private Button leftButton;
   private Button rightButton;
   private final List recipes;
   public static final ResourceLocation UID = new ResourceLocation("spore", "surgery");

   public SurgeryRecipeScreen(SurgeryRecipeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.imageWidth = 176;
      this.imageHeight = 84;
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         this.recipes = new ArrayList();
      } else {
         this.recipes = level.getRecipeManager().getAllRecipesFor(SurgeryRecipe.SurgeryRecipeType.INSTANCE);
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
      SurgeryRecipe recipe = (SurgeryRecipe)this.recipes.get(this.currentItemIndex);
      if (recipe != null) {
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(0)), this.leftPos + 7, this.topPos + 8);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(1)), this.leftPos + 7, this.topPos + 26);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(2)), this.leftPos + 7, this.topPos + 44);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(3)), this.leftPos + 7, this.topPos + 62);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(4)), this.leftPos + 25, this.topPos + 8);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(5)), this.leftPos + 25, this.topPos + 26);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(6)), this.leftPos + 25, this.topPos + 44);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(7)), this.leftPos + 25, this.topPos + 62);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(8)), this.leftPos + 43, this.topPos + 8);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(9)), this.leftPos + 43, this.topPos + 26);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(10)), this.leftPos + 43, this.topPos + 44);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(11)), this.leftPos + 43, this.topPos + 62);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(12)), this.leftPos + 61, this.topPos + 8);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(13)), this.leftPos + 61, this.topPos + 26);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(14)), this.leftPos + 61, this.topPos + 44);
         this.renderFakeItem(this.font, guiGraphics, this.getItemStackFromIngredient((Ingredient)recipe.getIngredients().get(15)), this.leftPos + 61, this.topPos + 62);
         this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)this.tagItems.get(this.currentTagIndex)), this.leftPos + 97, this.topPos + 8);
         ItemStack stack = recipe.getResultItem((RegistryAccess)null);
         if (stack.getItem() instanceof SporeWeaponData || stack.getItem() instanceof SporeArmorData) {
            this.renderFakeItem(this.font, guiGraphics, this.currentTagIndex % 2 == 0 ? ItemStack.EMPTY : new ItemStack((ItemLike)Sitems.HARDENING_AGENT.get()), this.leftPos + 115, this.topPos + 8);
            this.renderFakeItem(this.font, guiGraphics, this.currentTagIndex % 2 == 0 ? ItemStack.EMPTY : new ItemStack((ItemLike)Sitems.SHARPENING_AGENT.get()), this.leftPos + 133, this.topPos + 8);
            this.renderFakeItem(this.font, guiGraphics, this.currentTagIndex % 2 == 0 ? ItemStack.EMPTY : new ItemStack((ItemLike)Sitems.INTEGRATING_AGENT.get()), this.leftPos + 151, this.topPos + 8);
         }

         this.renderFakeItem(this.font, guiGraphics, stack, this.leftPos + 124, this.topPos + 53);
      }
   }

   private ItemStack getItemStackFromIngredient(Ingredient ingredient) {
      ItemStack[] itemStacks = ingredient.getItems();
      return itemStacks.length > 0 ? itemStacks[0] : ItemStack.EMPTY;
   }

   protected void containerTick() {
      super.containerTick();
      if (!this.tagItems.isEmpty()) {
         ++this.tickCounter;
         if (this.tickCounter % 40 == 0) {
            this.currentTagIndex = (this.currentTagIndex + 1) % this.tagItems.size();
         }
      }

   }
}
