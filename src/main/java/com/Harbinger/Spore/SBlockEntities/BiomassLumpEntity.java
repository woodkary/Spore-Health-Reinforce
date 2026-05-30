package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SblockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BiomassLumpEntity extends LivingStructureBlocks {
   public BiomassLumpEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SblockEntities.BIOMASS_LUMP.get(), pos, state);
   }
}
