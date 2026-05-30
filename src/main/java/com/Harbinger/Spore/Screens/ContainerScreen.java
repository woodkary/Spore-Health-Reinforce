package com.Harbinger.Spore.Screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ContainerScreen extends AbstractContainerScreen<ContainerMenu> {
   private static final ResourceLocation texture = new ResourceLocation("spore:textures/gui/bio_haz_gui.png");

   public ContainerScreen(ContainerMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.imageWidth = 176;
      this.imageHeight = 166;
   }

   public void render(GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(ms);
      super.render(ms, mouseX, mouseY, partialTicks);
      this.renderTooltip(ms, mouseX, mouseY);
   }

   public boolean keyPressed(int key, int b, int c) {
      if (key == 256) {
         this.minecraft.player.closeContainer();
         return true;
      } else {
         return super.keyPressed(key, b, c);
      }
   }

   public void containerTick() {
      super.containerTick();
   }

   protected void renderBg(GuiGraphics graphics, float p_97788_, int mouseX, int mouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderTexture(0, texture);
      graphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      RenderSystem.disableBlend();
   }
}
