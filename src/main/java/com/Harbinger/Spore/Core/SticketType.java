package com.Harbinger.Spore.Core;

import java.util.Comparator;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public class SticketType {
   public static final TicketType SPORE_CHUNK_LOADER = TicketType.create("sporechunk_loader", Comparator.comparingLong(ChunkPos::toLong));

   public static void init() {
   }
}
