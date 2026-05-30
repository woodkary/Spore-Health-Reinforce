package com.Harbinger.Spore.ExtremelySusThings.Package;

import com.Harbinger.Spore.SBlockEntities.SurgeryTableBlockEntity;
import com.Harbinger.Spore.Screens.SurgeryMenu;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class OpenSurgeryScreenPacket {
   private final BlockPos pos;
   private final int id;

   public OpenSurgeryScreenPacket(BlockPos pos, int playerId) {
      this.pos = pos;
      this.id = playerId;
   }

   public OpenSurgeryScreenPacket(FriendlyByteBuf friendlyByteBuf) {
      this.pos = friendlyByteBuf.readBlockPos();
      this.id = friendlyByteBuf.readInt();
   }

   public static void encode(OpenSurgeryScreenPacket msg, FriendlyByteBuf buf) {
      buf.writeBlockPos(msg.pos);
      buf.writeInt(msg.id);
   }

   public static void handle(OpenSurgeryScreenPacket msg, Supplier ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer player = ((NetworkEvent.Context)ctx.get()).getSender();
         if (player != null) {
            Entity truePlayer = player.level().getEntity(msg.id);
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (be instanceof SurgeryTableBlockEntity) {
               final SurgeryTableBlockEntity table = (SurgeryTableBlockEntity)be;
               if (truePlayer instanceof ServerPlayer) {
                  ServerPlayer trueP = (ServerPlayer)truePlayer;
                  NetworkHooks.openScreen(trueP, new MenuProvider() {
                     public Component getDisplayName() {
                        return Component.translatable("block.spore.surgery_table");
                     }

                     public AbstractContainerMenu createMenu(int id, Inventory inv, Player ply) {
                        return new SurgeryMenu(id, inv, table, table.data);
                     }
                  }, (buf) -> buf.writeBlockPos(msg.pos));
               }
            }
         }

      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
