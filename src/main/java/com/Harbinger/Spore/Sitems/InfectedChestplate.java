package com.Harbinger.Spore.Sitems;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class InfectedChestplate extends InfectedExoskeleton {
   public InfectedChestplate() {
      super(Type.CHESTPLATE);
   }

   public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
      return "spore:textures/entity/empty.png";
   }

   public void appendHoverText(ItemStack itemStack, @Nullable Level level, List components, TooltipFlag tooltipFlag) {
      if (Screen.hasShiftDown()) {
         components.add(Component.translatable("item.armor.shift").withStyle(ChatFormatting.DARK_RED));
      } else {
         components.add(Component.translatable("item.armor.normal").withStyle(ChatFormatting.GOLD));
      }

      super.appendHoverText(itemStack, level, components, tooltipFlag);
   }

   public void onArmorTick(ItemStack stack, Level level, Player player) {
      super.onArmorTick(stack, level, player);
      if (player.horizontalCollision && player.isCrouching()) {
         Vec3 initialVec = player.getDeltaMovement();
         Vec3 climbVec = new Vec3(initialVec.x, 0.2, initialVec.z);
         player.setDeltaMovement(climbVec.x * 0.91, climbVec.y * 0.98, climbVec.z * 0.91);
      }

   }
}
