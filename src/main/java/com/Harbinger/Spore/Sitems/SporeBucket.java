package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Sitems;
import java.util.function.Supplier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class SporeBucket extends BucketItem {
   public SporeBucket(Supplier supplier, Properties builder) {
      super(supplier, builder);
      Sitems.TECHNOLOGICAL_ITEMS.add(this);
   }
}
