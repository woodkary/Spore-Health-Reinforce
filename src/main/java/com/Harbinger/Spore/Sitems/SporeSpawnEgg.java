package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Sitems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import org.jetbrains.annotations.Nullable;

public class SporeSpawnEgg extends ForgeSpawnEggItem {
   private final SpawnEggType type;

   public SporeSpawnEgg(Supplier type, int backgroundColor, SpawnEggType type1) {
      super(type, backgroundColor, -1, new Properties());
      this.type = type1;
      Sitems.BIOLOGICAL_ITEMS.add(this);
   }

   public void appendHoverText(ItemStack stack, @Nullable Level level, List components, TooltipFlag flag) {
      components.add(this.type.getName());
      super.appendHoverText(stack, level, components, flag);
   }
}
