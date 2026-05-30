package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Recipes.GraftingRecipe;
import com.Harbinger.Spore.Recipes.SurgeryRecipe;
import com.Harbinger.Spore.Screens.SurgeryMenu;
import com.Harbinger.Spore.Sitems.Agents.MutationAgents;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SurgeryTableBlockEntity extends BlockEntity implements MenuProvider {
   public final ItemStackHandler itemHandler = new ItemStackHandler(25);
   public final TagKey stringLikeItem = ItemTags.create(new ResourceLocation("spore:stitches"));
   public static final int STRING_SLOT = 16;
   public static final int AGENT_SLOT_1 = 17;
   public static final int AGENT_SLOT_2 = 18;
   public static final int AGENT_SLOT_3 = 19;
   public static final int OUTPUT_SLOT = 20;
   public static final int GRATING_ITEM_ONE = 21;
   public static final int GRATING_INGREDIENT = 22;
   public static final int GRATING_ITEM_TWO = 23;
   public static final int GRATING_OUTPUT = 24;
   private LazyOptional lazyItemHandler = LazyOptional.empty();
   public final ContainerData data = new SimpleContainerData(25);

   public SurgeryTableBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)SblockEntities.SURGERY_TABLE_ENTITY.get(), p_155229_, p_155230_);
   }

   public Component getDisplayName() {
      return Component.translatable("spore.surgery_table_menu");
   }

   public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory inventory, Player player) {
      return new SurgeryMenu(pContainerId, inventory, this, this.data);
   }

   public @NotNull LazyOptional getCapability(@NotNull Capability cap, @Nullable Direction side) {
      return cap == ForgeCapabilities.ITEM_HANDLER ? this.lazyItemHandler.cast() : super.getCapability(cap, side);
   }

   public void drops() {
      for(int i = 0; i < this.itemHandler.getSlots(); ++i) {
         ItemStack stack = this.itemHandler.getStackInSlot(i);
         if (!stack.isEmpty()) {
            Containers.dropItemStack(this.level, (double)this.worldPosition.getX(), (double)this.worldPosition.getY(), (double)this.worldPosition.getZ(), stack);
         }
      }

   }

   public void onLoad() {
      super.onLoad();
      this.lazyItemHandler = LazyOptional.of(() -> this.itemHandler);
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.lazyItemHandler.invalidate();
   }

   public Optional getCurrentRecipe() {
      SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());

      for(int i = 0; i < 16; ++i) {
         inventory.setItem(i, this.itemHandler.getStackInSlot(i));
      }

      return this.level != null ? this.level.getRecipeManager().getRecipeFor(SurgeryRecipe.SurgeryRecipeType.INSTANCE, inventory, this.level) : null;
   }

   public void consumeItems() {
      Optional<SurgeryRecipe> match = this.getCurrentRecipe();
      match.ifPresent((recipe) -> {
         for(int i = 0; i < 16; ++i) {
            ItemStack stack = this.itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
               this.itemHandler.extractItem(i, 1, false);
            }
         }

         this.itemHandler.extractItem(16, 1, false);
      });
      if (this.level != null) {
         this.level.playLocalSound(this.getBlockPos(), (SoundEvent)Ssounds.SURGERY.get(), SoundSource.BLOCKS, 1.0F, 1.0F, true);
      }

   }

   public Optional getCurrentGraftingRecipe() {
      SimpleContainer inventory = new SimpleContainer(25);

      for(int i = 21; i < 24; ++i) {
         inventory.setItem(i, this.itemHandler.getStackInSlot(i));
      }

      return this.level != null ? this.level.getRecipeManager().getRecipeFor(GraftingRecipe.GraftingRecipeType.INSTANCE, inventory, this.level) : null;
   }

   public void consumeItemsGrafting() {
      Optional<GraftingRecipe> match = this.getCurrentGraftingRecipe();
      match.ifPresent((recipe) -> {
         for(int i = 21; i < 24; ++i) {
            ItemStack stack = this.itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
               this.itemHandler.extractItem(i, 1, false);
            }
         }

      });
      if (this.level != null) {
         this.level.playLocalSound(this.getBlockPos(), (SoundEvent)Ssounds.SURGERY.get(), SoundSource.BLOCKS, 1.0F, 1.0F, true);
      }

   }

   public void assembleWeapon(Player player, ItemStack stack) {
      int mutation = 15;
      int[] e = new int[]{17, 18, 19};
      List<MutationAgents> mutagens = new ArrayList();

      for(int i : e) {
         ItemStack itemStack = this.itemHandler.getStackInSlot(i);
         Item var12 = itemStack.getItem();
         if (var12 instanceof MutationAgents mutationAgents) {
            mutagens.add(mutationAgents);
            itemStack.shrink(1);
         }
      }

      if (stack.getItem() instanceof SporeWeaponData || stack.getItem() instanceof SporeArmorData) {
         for(MutationAgents mutagen : mutagens) {
            mutagen.mutateWeapon(stack);
            mutation += mutagen.getMutationChance();
         }

         if (Math.random() < (double)mutation * 0.01) {
            Item var17 = stack.getItem();
            if (var17 instanceof SporeWeaponData) {
               SporeWeaponData item = (SporeWeaponData)var17;
               item.setVariant(SporeToolsMutations.byId(player.getRandom().nextInt(SporeToolsMutations.values().length)), stack);
            }
         }

         if (Math.random() < (double)mutation * 0.01) {
            Item var18 = stack.getItem();
            if (var18 instanceof SporeArmorData) {
               SporeArmorData item = (SporeArmorData)var18;
               item.setVariant(SporeArmorMutations.byId(player.getRandom().nextInt(SporeArmorMutations.values().length)), stack);
            }
         }

         stack.setDamageValue(this.itemHandler.getStackInSlot(19) == ItemStack.EMPTY ? player.getRandom().nextInt(stack.getMaxDamage()) : 0);
      }

   }

   public ItemStack assembleGraft(ItemStack stack) {
      ItemStack firstItem = this.itemHandler.getStackInSlot(21);
      ItemStack secondItem = this.itemHandler.getStackInSlot(23);
      Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(firstItem);
      enchants.putAll(EnchantmentHelper.getEnchantments(secondItem));
      Item item = stack.getItem();
      if (item instanceof SporeWeaponData weaponData) {
         int extra_durability;
         double extra_damage;
         int mutation;
         int luck;
         label54: {
            luck = 0;
            extra_durability = 0;
            extra_damage = (double)0.0F;
            mutation = 0;
            Item var13 = firstItem.getItem();
            if (var13 instanceof SporeWeaponData firstWdata) {
               var13 = secondItem.getItem();
               if (var13 instanceof SporeWeaponData secondWData) {
                  luck = (firstWdata.getLuck(firstItem) + secondWData.getLuck(secondItem)) / 2;
                  extra_durability = (firstWdata.getMaxAdditionalDurability(firstItem) + secondWData.getMaxAdditionalDurability(secondItem)) / 2;
                  extra_damage = (firstWdata.getAdditionalDamage(firstItem) + secondWData.getAdditionalDamage(secondItem)) / (double)2.0F;
                  mutation = Math.random() < (double)0.5F ? firstWdata.getTypeVariant(firstItem) : secondWData.getTypeVariant(secondItem);
                  break label54;
               }
            }

            Item var14 = firstItem.getItem();
            if (var14 instanceof SporeWeaponData firstWdata) {
               luck = firstWdata.getLuck(firstItem);
               extra_durability = firstWdata.getMaxAdditionalDurability(firstItem);
               extra_damage = firstWdata.getAdditionalDamage(firstItem);
               mutation = firstWdata.getTypeVariant(firstItem);
            }

            var14 = secondItem.getItem();
            if (var14 instanceof SporeWeaponData secondWData) {
               luck = secondWData.getLuck(secondItem);
               extra_durability = secondWData.getMaxAdditionalDurability(secondItem);
               extra_damage = secondWData.getAdditionalDamage(secondItem);
               mutation = secondWData.getTypeVariant(secondItem);
            }
         }

         weaponData.setLuck(luck, stack);
         weaponData.setMaxAdditionalDurability(extra_durability, stack);
         weaponData.setAdditionalDamage(extra_damage, stack);
         weaponData.setVariant(SporeToolsMutations.byId(mutation), stack);
      }

      item = stack.getItem();
      if (item instanceof SporeArmorData armorData) {
         int extra_durability;
         double extra_protection;
         double agent_toughness;
         int mutation;
         int luck;
         label55: {
            luck = 0;
            extra_durability = 0;
            extra_protection = (double)0.0F;
            agent_toughness = (double)0.0F;
            mutation = 0;
            Item var15 = firstItem.getItem();
            if (var15 instanceof SporeArmorData firstWdata) {
               var15 = secondItem.getItem();
               if (var15 instanceof SporeArmorData secondWData) {
                  luck = (firstWdata.getLuck(firstItem) + secondWData.getLuck(secondItem)) / 2;
                  extra_durability = (firstWdata.getMaxAdditionalDurability(firstItem) + secondWData.getMaxAdditionalDurability(secondItem)) / 2;
                  extra_protection = (firstWdata.getAdditionalProtection(firstItem) + secondWData.getAdditionalProtection(secondItem)) / (double)2.0F;
                  agent_toughness = (firstWdata.getAdditionalToughness(firstItem) + secondWData.getAdditionalToughness(secondItem)) / (double)2.0F;
                  mutation = Math.random() < (double)0.5F ? firstWdata.getTypeVariant(firstItem) : secondWData.getTypeVariant(secondItem);
                  break label55;
               }
            }

            Item var16 = firstItem.getItem();
            if (var16 instanceof SporeArmorData firstWdata) {
               luck = firstWdata.getLuck(firstItem);
               extra_durability = firstWdata.getMaxAdditionalDurability(firstItem);
               extra_protection = firstWdata.getAdditionalProtection(firstItem);
               agent_toughness = firstWdata.getAdditionalToughness(firstItem);
               mutation = firstWdata.getTypeVariant(firstItem);
            }

            var16 = secondItem.getItem();
            if (var16 instanceof SporeArmorData secondWData) {
               luck = secondWData.getLuck(secondItem);
               extra_durability = secondWData.getMaxAdditionalDurability(secondItem);
               extra_protection = secondWData.getAdditionalProtection(secondItem);
               agent_toughness = secondWData.getAdditionalToughness(secondItem);
               mutation = secondWData.getTypeVariant(secondItem);
            }
         }

         armorData.setLuck(luck, stack);
         armorData.setMaxAdditionalDurability(extra_durability, stack);
         armorData.setAdditionalProtection(extra_protection, stack);
         armorData.setAdditionalToughness(agent_toughness, stack);
         armorData.setVariant(SporeArmorMutations.byId(mutation), stack);
      }

      Objects.requireNonNull(stack);
      enchants.forEach(stack::enchant);
      return stack;
   }

   public boolean canInsertIntoOutputSlot(ItemStack stack, int slot) {
      ItemStack outputStack = this.itemHandler.getStackInSlot(slot);
      return outputStack.isEmpty();
   }

   public void updateOutputSlot() {
      if (this.itemHandler.getStackInSlot(16) == ItemStack.EMPTY) {
         this.itemHandler.setStackInSlot(20, ItemStack.EMPTY);
      } else {
         Optional<SurgeryRecipe> match = this.getCurrentRecipe();
         if (match.isPresent()) {
            ItemStack stack = ((SurgeryRecipe)match.get()).getResultItem((RegistryAccess)null);
            if (this.canInsertIntoOutputSlot(stack, 20)) {
               this.itemHandler.insertItem(20, stack.copy(), false);
            }
         } else {
            this.itemHandler.setStackInSlot(20, ItemStack.EMPTY);
         }

      }
   }

   public void updateSecondOutputSlot() {
      Optional<GraftingRecipe> match = this.getCurrentGraftingRecipe();
      if (match.isPresent()) {
         ItemStack stack = ((GraftingRecipe)match.get()).getResultItem((RegistryAccess)null);
         if (this.canInsertIntoOutputSlot(stack, 24)) {
            this.itemHandler.insertItem(24, stack.copy(), false);
         }
      } else {
         this.itemHandler.setStackInSlot(24, ItemStack.EMPTY);
      }

   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, SurgeryTableBlockEntity entity) {
      entity.updateOutputSlot();
      entity.updateSecondOutputSlot();
   }
}
