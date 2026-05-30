package com.Harbinger.Spore.ExtremelySusThings;

import java.util.HashMap;
import java.util.Map;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientAdvancementTracker {
   private static final Map advancementCache = new HashMap();

   public static void setAdvancement(String advancementId, boolean hasIt) {
      advancementCache.put(advancementId, hasIt);
   }

   public static boolean hasAdvancement(String advancementId) {
      return (Boolean)advancementCache.getOrDefault(advancementId, false);
   }
}
