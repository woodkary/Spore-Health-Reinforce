package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class IncubatorScreen extends AbstractContainerScreen<IncubatorMenu> implements TutorialMenuMethods {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/gui/incubator_gui.png");
   private final List tagItems;
   private int tickCounter = 0;
   private int currentItemIndex = 0;

   public IncubatorScreen(IncubatorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.imageWidth = 176;
      this.imageHeight = 84;
      this.tagItems = Utilities.getItemsFromTag("spore", "weapons");
   }

   protected void init() {
      super.init();
      this.inventoryLabelY = 10000;
      this.titleLabelY = 10000;
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
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.BIOMASS.get()), this.leftPos + 106, this.topPos + 8);
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.INCUBATOR.get()), this.leftPos + 79, this.topPos + 35);
      if (!this.tagItems.isEmpty()) {
         ItemStack stack = new ItemStack((ItemLike)this.tagItems.get(this.currentItemIndex));
         int damage = stack.getMaxDamage();
         this.renderFakeDamagedItem(this.font, guiGraphics, stack, damage / 2, this.leftPos + 34, this.topPos + 35);
         this.renderFakeDamagedItem(this.font, guiGraphics, stack, damage / 8, this.leftPos + 133, this.topPos + 35);
      }

   }

   protected void containerTick() {
      super.containerTick();
      if (!this.tagItems.isEmpty()) {
         ++this.tickCounter;
         if (this.tickCounter % 40 == 0) {
            this.currentItemIndex = (this.currentItemIndex + 1) % this.tagItems.size();
         }
      }

   }
}
