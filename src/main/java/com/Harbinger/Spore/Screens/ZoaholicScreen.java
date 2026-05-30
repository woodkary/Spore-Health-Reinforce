package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.Sitems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ZoaholicScreen extends AbstractContainerScreen<ZoaholicMenu> implements TutorialMenuMethods {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/gui/zoaholic_gui.png");

   public ZoaholicScreen(ZoaholicMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.imageWidth = 176;
      this.imageHeight = 84;
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
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.BIOMASS.get()), this.leftPos + 34, this.topPos + 17);
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.ZOAHOLIC.get()), this.leftPos + 79, this.topPos + 17);
      this.renderFakeItem(this.font, guiGraphics, new ItemStack(Items.PAPER), this.leftPos + 124, this.topPos + 17);
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.CEREBRUM.get()), this.leftPos + 34, this.topPos + 62);
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.INNARDS.get()), this.leftPos + 61, this.topPos + 62);
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.INNARDS.get()), this.leftPos + 97, this.topPos + 62);
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.MUTATED_HEART.get()), this.leftPos + 124, this.topPos + 62);
   }
}
