package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import com.Harbinger.Spore.ExtremelySusThings.Package.OpenSurgeryScreenPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public class GraftingScreen extends AbstractContainerScreen<GraftingMenu> {
   private InvisibleButton invisibleButton;
   private InvisibleButton invisibleButton2;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/gui/grafting_gui.png");

   public GraftingScreen(GraftingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.imageWidth = 176;
      this.imageHeight = 166;
   }

   protected void init() {
      super.init();
      this.inventoryLabelY = 10000;
      this.titleLabelY = 10000;
      int buttonX = this.width / 2 - 50;
      int buttonY = this.height / 2 - 20;
      if (!ModList.get().isLoaded("jei")) {
         this.invisibleButton2 = (InvisibleButton)this.addRenderableWidget(new InvisibleButton(buttonX + 50, buttonY, 20, 20, Component.literal(""), (button) -> {
            Entity patt1762$temp = Minecraft.getInstance().cameraEntity;
            if (patt1762$temp instanceof Player player) {
               GraftingRecipeMenu menu1 = new GraftingRecipeMenu(1, player.getInventory());
               Minecraft.getInstance().setScreen(new GraftingRecipeScreen(menu1, player.getInventory(), Component.translatable("block.spore.surgery_table")));
            }

         }, (btn) -> Component.literal("Go To Recipes")));
      }

      this.invisibleButton = (InvisibleButton)this.addRenderableWidget(new InvisibleButton(buttonX + 110, buttonY - 7, 20, 20, Component.literal(""), (button) -> {
         Entity patt2428$temp = Minecraft.getInstance().cameraEntity;
         if (patt2428$temp instanceof Player player) {
            BlockPos pos = ((GraftingMenu)this.menu).blockEntity.getBlockPos();
            SporePacketHandler.sendToServer(new OpenSurgeryScreenPacket(pos, player.getId()));
         }

      }, (btn) -> Component.literal("Go To Recipes")));
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
   }
}
