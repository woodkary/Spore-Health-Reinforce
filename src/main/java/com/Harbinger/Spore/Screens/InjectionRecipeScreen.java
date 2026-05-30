package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Recipes.InjectionRecipe;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sitems.BiologicalReagent;
import com.Harbinger.Spore.Sitems.Agents.ArmorSyringe;
import com.Harbinger.Spore.Sitems.Agents.WeaponSyringe;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;

public class InjectionRecipeScreen extends AbstractContainerScreen<InjectionRecipeMenu> implements TutorialMenuMethods {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/gui/injection_recipe_gui.png");
   private final List recipes;
   private int tickCounter = 0;
   private int currentItemIndex = 0;
   private Button leftButton;
   private Button rightButton;
   private Button leftDownButton;
   private Button rightDownButton;
   private int getCurrentWeaponIndex = 0;
   private int getCurrentArmorIndex = 0;
   private int getCurrentItemIndex = 0;
   private int getCurrentReagentIndex = 0;
   private final List weaponItems;
   private final List armorItems;
   private final List allItems;
   private final List reagents;
   ClientLevel level;

   public InjectionRecipeScreen(InjectionRecipeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.level = Minecraft.getInstance().level;
      this.weaponItems = Utilities.getItemsFromTag("spore", "enchantable_weapon_items");
      this.armorItems = Utilities.getItemsFromTag("spore", "enchantable_armor_items");
      this.allItems = Utilities.getItemsFromTag("spore", "enchantable_items");
      this.reagents = Utilities.getItemsFromTag("spore", "reagents");
      this.imageWidth = 176;
      this.imageHeight = 166;
      if (this.level == null) {
         this.recipes = new ArrayList();
      } else {
         this.recipes = this.level.getRecipeManager().getAllRecipesFor(InjectionRecipe.InjectionRecipeType.INSTANCE);
      }

   }

   private void changeRecipe(int direction) {
      if (!this.recipes.isEmpty()) {
         this.currentItemIndex = (this.currentItemIndex + direction) % this.recipes.size();
         this.getCurrentWeaponIndex = 0;
         this.getCurrentArmorIndex = 0;
         if (this.currentItemIndex < 0) {
            this.currentItemIndex += this.recipes.size();
         }
      }

   }

   private void changeReagent(int direction) {
      if (!this.recipes.isEmpty()) {
         this.getCurrentReagentIndex = (this.getCurrentReagentIndex + direction) % this.reagents.size();
         this.getCurrentWeaponIndex = 0;
         this.getCurrentArmorIndex = 0;
         if (this.getCurrentReagentIndex < 0) {
            this.getCurrentReagentIndex += this.reagents.size();
         }
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
      this.leftDownButton = (Button)this.addRenderableWidget(Button.builder(Component.literal("<"), (button) -> this.changeReagent(-1)).bounds(buttonX - 10, buttonY + 110, 20, 20).build());
      this.rightDownButton = (Button)this.addRenderableWidget(Button.builder(Component.literal(">"), (button) -> this.changeReagent(1)).bounds(buttonX + 10, buttonY + 110, 20, 20).build());
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
      int x = this.leftPos + 34;
      int y = this.topPos + 70;
      this.renderTooltip(guiGraphics, mouseX, mouseY);
      InjectionRecipe recipe = (InjectionRecipe)this.recipes.get(this.currentItemIndex);
      if (recipe != null) {
         ItemStack stack = recipe.getResultItem((RegistryAccess)null);
         this.renderFakeItem(this.font, guiGraphics, new ItemStack((ItemLike)Sitems.SYRINGE.get()), this.leftPos + 97, this.topPos + 17);
         this.renderFakeItem(this.font, guiGraphics, stack.copy(), this.leftPos + 97, this.topPos + 53);
         this.renderName(guiGraphics, stack.getHoverName(), 90, 15);
         ItemStack weapon = new ItemStack((ItemLike)this.weaponItems.get(this.getCurrentWeaponIndex));
         ItemStack armor = new ItemStack((ItemLike)this.armorItems.get(this.getCurrentArmorIndex));
         ItemStack allItems = new ItemStack((ItemLike)this.allItems.get(this.getCurrentItemIndex));
         ItemStack reagent = new ItemStack((ItemLike)this.reagents.get(this.getCurrentReagentIndex));
         ItemStack mutatedTool = this.getCurrentMutantConstruct(stack.copy(), weapon.copy(), armor.copy());
         if (!mutatedTool.equals(ItemStack.EMPTY)) {
            this.renderFakeItem(this.font, guiGraphics, mutatedTool.copy(), this.leftPos + 133, this.topPos + 53);
         }

         if (stack.getItem() instanceof WeaponSyringe) {
            this.renderFakeItem(this.font, guiGraphics, weapon.copy(), this.leftPos + 133, this.topPos + 17);
         }

         if (stack.getItem() instanceof ArmorSyringe) {
            this.renderFakeItem(this.font, guiGraphics, armor.copy(), this.leftPos + 133, this.topPos + 17);
         }

         this.renderName(guiGraphics, reagent.getHoverName(), 75, 85);
         this.renderFakeItem(this.font, guiGraphics, reagent.copy(), this.leftPos + 43, this.topPos + 89);
         ItemStack compatTool = this.getCurrentReagentItem(reagent, weapon, armor, allItems);
         this.renderFakeItem(this.font, guiGraphics, compatTool.copy(), this.leftPos + 43, this.topPos + 125);
         ItemStack enchantedItem = this.getEnchantedType(reagent, compatTool);
         this.renderFakeItem(this.font, guiGraphics, enchantedItem.copy(), this.leftPos + 79, this.topPos + 125);
         Enchantment enchantment = this.Enchantment(reagent.copy());
         this.renderName(guiGraphics, enchantment.getFullname(1), 90, 155);
         if (this.level != null) {
            int variant = recipe.getEntityType();
            ResourceLocation location = new ResourceLocation(recipe.getEntityId());
            Entity entity = ((EntityType)ForgeRegistries.ENTITY_TYPES.getValue(location)).create(this.level);
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               if (living instanceof VariantKeeper) {
                  VariantKeeper keeper = (VariantKeeper)living;
                  keeper.setVariant(variant);
                  this.renderEntityInInventoryFollowsAngle(guiGraphics, x, y, 20, 0.0F, 0.0F, living);
               } else {
                  this.renderEntityInInventoryFollowsAngle(guiGraphics, x, y, 20, 0.0F, 0.0F, living);
               }
            }
         }

      }
   }

   public ItemStack getCurrentMutantConstruct(ItemStack stack, ItemStack weapon, ItemStack armor) {
      Item mutations = stack.getItem();
      if (mutations instanceof WeaponSyringe syringe) {
         SporeToolsMutations toolMutations = syringe.getMutations();
         Item var7 = weapon.getItem();
         if (var7 instanceof SporeWeaponData sporeToolsBaseItem) {
            sporeToolsBaseItem.setVariant(toolMutations, weapon);
            return weapon.copy();
         }
      }

      mutations = stack.getItem();
      if (mutations instanceof ArmorSyringe syringe) {
         SporeArmorMutations armorMutations = syringe.getMutations();
         Item var13 = armor.getItem();
         if (var13 instanceof SporeArmorData sporeToolsBaseItem) {
            sporeToolsBaseItem.setVariant(armorMutations, armor);
            return armor.copy();
         }
      }

      return ItemStack.EMPTY;
   }

   public ItemStack getCurrentReagentItem(ItemStack stack, ItemStack weapon, ItemStack armor, ItemStack both) {
      Item var6 = stack.getItem();
      if (var6 instanceof BiologicalReagent biologicalReagent) {
         if (biologicalReagent.getType() == BiologicalReagent.AcceptedTypes.ALL_TYPES) {
            return both.copy();
         }

         if (biologicalReagent.getType() == BiologicalReagent.AcceptedTypes.WEAPON_TYPES) {
            return weapon.copy();
         }

         if (biologicalReagent.getType() == BiologicalReagent.AcceptedTypes.ARMOR_TYPES) {
            return armor.copy();
         }
      }

      return ItemStack.EMPTY;
   }

   public ItemStack getEnchantedType(ItemStack stack, ItemStack tool) {
      Item var4 = stack.getItem();
      if (var4 instanceof BiologicalReagent biologicalReagent) {
         tool.enchant(biologicalReagent.getAppliedEnchantment(), 1);
         return tool;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public Enchantment Enchantment(ItemStack stack) {
      Item var3 = stack.getItem();
      if (var3 instanceof BiologicalReagent biologicalReagent) {
         return biologicalReagent.getAppliedEnchantment();
      } else {
         return Enchantments.SHARPNESS;
      }
   }

   private void renderName(GuiGraphics guiGraphics, Component name, int x, int y) {
      int iconX = this.leftPos + x;
      int iconY = this.topPos + y;
      int iconWidth = 32;
      int textX = iconX + iconWidth / 2 - this.font.width(name) / 2;
      int textY = iconY - 10;
      guiGraphics.drawString(this.font, name, textX, textY, 16777215, true);
   }

   private void renderEntityInInventoryFollowsAngle(GuiGraphics guiGraphics, int x, int y, int scale, float angleXComponent, float angleYComponent, LivingEntity entity) {
      Quaternionf pose = (new Quaternionf()).rotateZ((float)Math.PI);
      Quaternionf cameraOrientation = (new Quaternionf()).rotateX(angleYComponent * 20.0F * ((float)Math.PI / 180F));
      pose.mul(cameraOrientation);
      float f2 = entity.yBodyRot;
      float f3 = entity.getYRot();
      float f4 = entity.getXRot();
      float f5 = entity.yHeadRotO;
      float f6 = entity.yHeadRot;
      entity.yBodyRot = 180.0F + angleXComponent * 20.0F;
      entity.setYRot(180.0F + angleXComponent * 40.0F);
      entity.setXRot(-angleYComponent * 20.0F);
      entity.yHeadRot = entity.getYRot();
      entity.yHeadRotO = entity.getYRot();
      InventoryScreen.renderEntityInInventory(guiGraphics, x, y, scale, pose, cameraOrientation, entity);
      entity.yBodyRot = f2;
      entity.setYRot(f3);
      entity.setXRot(f4);
      entity.yHeadRotO = f5;
      entity.yHeadRot = f6;
   }

   protected void containerTick() {
      super.containerTick();
      if (!this.recipes.isEmpty()) {
         ++this.tickCounter;
         if (this.tickCounter % 40 == 0) {
            this.getCurrentWeaponIndex = (this.getCurrentWeaponIndex + 1) % this.weaponItems.size();
            this.getCurrentArmorIndex = (this.getCurrentArmorIndex + 1) % this.armorItems.size();
            this.getCurrentItemIndex = (this.getCurrentItemIndex + 1) % this.allItems.size();
         }
      }

   }
}
