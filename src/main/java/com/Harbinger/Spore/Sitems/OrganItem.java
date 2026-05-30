package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.ExtremelySusThings.ClientAdvancementTracker;
import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import com.Harbinger.Spore.ExtremelySusThings.Package.RequestAdvancementPacket;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OrganItem extends BaseItem {
   private final String info;
   private final String advancementIds;

   public OrganItem(String value, String advancementId) {
      super(new Properties());
      this.info = value;
      this.advancementIds = advancementId;
   }

   public String getAdvancementIds() {
      return this.advancementIds;
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List list, TooltipFlag tooltipFlag) {
      super.appendHoverText(stack, level, list, tooltipFlag);
      if (this.info != null && this.advancementIds != null) {
         if (level != null && level.isClientSide) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity instanceof Player) {
               Player player = (Player)entity;
               if (ClientAdvancementTracker.hasAdvancement(this.advancementIds)) {
                  list.add(Component.translatable(this.info).withStyle(ChatFormatting.GOLD));
               } else {
                  list.add(Component.translatable("spore.scanner.organ.default").withStyle(ChatFormatting.RED));
               }

               SporePacketHandler.sendToServer(new RequestAdvancementPacket(this.advancementIds, player.getId()));
            }
         } else {
            list.add(Component.translatable("spore.scanner.organ.default").withStyle(ChatFormatting.RED));
         }

      }
   }
}
