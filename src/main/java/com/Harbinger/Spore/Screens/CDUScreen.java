package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sitems;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class CDUScreen extends AbstractContainerScreen<CDUMenu> implements TutorialMenuMethods {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/gui/cdu_gui.png");
   private final List blockMap;
   private int tickCounter = 0;
   private int currentItemIndex = 0;

   public CDUScreen(CDUMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.imageWidth = 176;
      this.imageHeight = 84;
      this.blockMap = this.fabricateBlocks();
   }

   protected void init() {
      super.init();
      this.inventoryLabelY = 10000;
      this.titleLabelY = 10000;
   }

   private List fabricateBlocks() {
      List<StoreDouble> blocks = new ArrayList();

      for(String str : (List<String>)SConfig.DATAGEN.block_cleaning.get()) {
         String[] string = str.split("\\|");
         Block blockCon1 = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[0]));
         Block blockCon2 = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[1]));
         if (blockCon1 != null && blockCon2 != null) {
            blocks.add(new StoreDouble(blockCon1, blockCon2));
         }
      }

      return blocks;
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
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.CDU.get()), this.leftPos + 79, this.topPos + 44);
      this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.ICE_CANISTER.get()), this.leftPos + 61, this.topPos + 62);
      if (!this.blockMap.isEmpty()) {
         this.renderFakeItem(this.font, guiGraphics, new ItemStack(((StoreDouble)this.blockMap.get(this.currentItemIndex)).value1), this.leftPos + 34, this.topPos + 44);
         this.renderFakeItem(this.font, guiGraphics, new ItemStack(((StoreDouble)this.blockMap.get(this.currentItemIndex)).value2), this.leftPos + 124, this.topPos + 44);
      }

   }

   protected void containerTick() {
      super.containerTick();
      if (!this.blockMap.isEmpty()) {
         ++this.tickCounter;
         if (this.tickCounter % 40 == 0) {
            this.currentItemIndex = (this.currentItemIndex + 1) % this.blockMap.size();
         }
      }

   }

   static record StoreDouble(Block value1, Block value2) {
   }
}
