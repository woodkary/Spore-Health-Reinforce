package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Client.ClientModEvents;
import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Core.Ssounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BiologicalReagent extends BaseItem {
   private final AcceptedTypes type;
   public static final TagKey ALL_TYPES = ItemTags.create(new ResourceLocation("spore", "enchantable_items"));
   public static final TagKey WEAPON_TYPES = ItemTags.create(new ResourceLocation("spore", "enchantable_weapon_items"));
   public static final TagKey ARMOR_TYPES_TYPES = ItemTags.create(new ResourceLocation("spore", "enchantable_armor_items"));

   public BiologicalReagent(AcceptedTypes types) {
      super(new Properties());
      this.type = types;
   }

   public AcceptedTypes getType() {
      return this.type;
   }

   public boolean isFoil(ItemStack p_41453_) {
      return true;
   }

   public static List curses() {
      List<Enchantment> enchantments = new ArrayList();
      enchantments.add((Enchantment)Senchantments.UNWAVERING_NATURE.get());
      enchantments.add((Enchantment)Senchantments.MUTAGENIC_REACTANT.get());
      return enchantments;
   }

   public Enchantment getAppliedEnchantment() {
      return null;
   }

   public boolean testSlotCompat(ItemStack stack) {
      if (this.type == AcceptedTypes.ALL_TYPES) {
         return stack.is(ALL_TYPES);
      } else if (this.type == AcceptedTypes.WEAPON_TYPES) {
         return stack.is(WEAPON_TYPES);
      } else {
         return this.type == AcceptedTypes.ARMOR_TYPES ? stack.is(ARMOR_TYPES_TYPES) : false;
      }
   }

   private double chance() {
      return 0.2;
   }

   public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
      ItemStack stack = slot.getItem();
      if (this.testSlotCompat(stack) && EnchantmentHelper.getTagEnchantmentLevel(this.getAppliedEnchantment(), stack) == 0 && this.getAppliedEnchantment() != null && clickAction == ClickAction.SECONDARY) {
         player.playNotifySound((SoundEvent)Ssounds.REAGENT.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
         stack.enchant(this.getAppliedEnchantment(), this.getAppliedEnchantment().getMaxLevel());
         itemStack.setCount(itemStack.getCount() - 1);
         if (Math.random() < this.chance()) {
            Enchantment curse = (Enchantment)curses().get(player.getRandom().nextInt(curses().size()));
            if (stack.getEnchantmentLevel(curse) == 0) {
               stack.enchant(curse, curse.getMaxLevel());
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void appendHoverText(ItemStack stack, @Nullable Level level, List list, TooltipFlag tooltipFlag) {
      list.add(Component.translatable(this.type.getId()).withStyle(ChatFormatting.GOLD));
      list.add(Component.translatable("item.reagent.line1"));
      list.add(Component.translatable(this.getAppliedEnchantment().getDescriptionId()));
      list.add(Component.translatable("item.reagent.line2").withStyle(ChatFormatting.BLACK));
      list.add(Component.translatable("universal_shift_rightclick").withStyle(ChatFormatting.YELLOW));
   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      if (player.isShiftKeyDown() && level.isClientSide) {
         ClientModEvents.openInjectionScreen(player);
      }

      ItemStack stack = player.getItemInHand(hand);
      return InteractionResultHolder.success(stack);
   }

   public static enum AcceptedTypes {
      ALL_TYPES("spore.name.reagent_type1"),
      WEAPON_TYPES("spore.name.reagent_type2"),
      ARMOR_TYPES("spore.name.reagent_type3");

      private final String id;

      private AcceptedTypes(String ids) {
         this.id = ids;
      }

      public String getId() {
         return this.id;
      }

      // $FF: synthetic method
      private static AcceptedTypes[] $values() {
         return new AcceptedTypes[]{ALL_TYPES, WEAPON_TYPES, ARMOR_TYPES};
      }
   }
}
