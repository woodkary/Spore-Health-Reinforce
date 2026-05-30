package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Seffects;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class Biomass extends BaseItem2 {
   public Biomass() {
      super((new Properties()).food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F).meat().effect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 100, 0), 0.5F).build()));
   }

   public void appendHoverText(ItemStack itemStack, @Nullable Level level, List components, TooltipFlag tooltipFlag) {
      if (Screen.hasShiftDown()) {
         components.add(Component.translatable("item.line.shift").withStyle(ChatFormatting.DARK_RED));
      } else {
         components.add(Component.translatable("item.line.normal").withStyle(ChatFormatting.GOLD));
      }

      super.appendHoverText(itemStack, level, components, tooltipFlag);
   }
}
