package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.SBlockEntities.BiomassLumpEntity;
import com.Harbinger.Spore.SBlockEntities.LivingStructureBlocks;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BiomassLump extends Block implements EntityBlock {
   public BiomassLump() {
      super(Properties.of().strength(2.0F, 2.0F).sound(SoundType.SLIME_BLOCK).randomTicks().noOcclusion().noCollission());
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos pos, BlockState oldState, boolean moving) {
      super.onPlace(blockstate, level, pos, oldState, moving);
      level.scheduleTick(pos, this, 40);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos blockPos, RandomSource source) {
      BlockEntity entity = level.getBlockEntity(blockPos);
      level.scheduleTick(blockPos, this, 40);
      if (entity instanceof LivingStructureBlocks structureBlocks) {
         AABB searchbox = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)33.0F, (double)33.0F, (double)33.0F);

         for(Entity entity1 : level.getEntitiesOfClass(Infected.class, searchbox)) {
            if (entity1 instanceof Infected infected) {
               if (infected.getKills() > 1 && structureBlocks.getKills() <= (Integer)SConfig.DATAGEN.biomass_lump_kills.get()) {
                  infected.setSearchPos(blockPos);
               }
            }
         }

         if (structureBlocks.getKills() >= (Integer)SConfig.DATAGEN.biomass_lump_kills.get()) {
            level.destroyBlock(blockPos, false);
            RandomSource random = RandomSource.create();
            if (Math.random() < 0.4) {
               StructureTemplate template = level.getStructureManager().getOrCreate(new ResourceLocation("spore", "biomass_tower"));
               BlockPos pos = new BlockPos(blockPos.getX() - 3, blockPos.getY() - 2, blockPos.getZ() - 3);
               template.placeInWorld(level, pos, pos, (new StructurePlaceSettings()).setIgnoreEntities(true), random, 3);
            } else if (Math.random() < 0.4) {
               StructureTemplate template = level.getStructureManager().getOrCreate(new ResourceLocation("spore", "biomass_tower_tall"));
               BlockPos pos = new BlockPos(blockPos.getX() - 3, blockPos.getY() - 1, blockPos.getZ() - 3);
               template.placeInWorld(level, pos, pos, (new StructurePlaceSettings()).setIgnoreEntities(true), random, 3);
            } else {
               StructureTemplate template = level.getStructureManager().getOrCreate(new ResourceLocation("spore", "biomass_tower_small"));
               BlockPos pos = new BlockPos(blockPos.getX() - 2, blockPos.getY() - 1, blockPos.getZ() - 2);
               template.placeInWorld(level, pos, pos, (new StructurePlaceSettings()).setIgnoreEntities(true), random, 3);
            }
         }
      }

   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity livingEntity) {
      BlockEntity entity = level.getBlockEntity(pos);
      if (livingEntity instanceof Infected infected) {
         if (infected.getKills() > 1 && entity.getPersistentData().getInt("kills") <= (Integer)SConfig.DATAGEN.biomass_lump_kills.get() && entity instanceof LivingStructureBlocks structureBlocks) {
            infected.setKills(infected.getKills() - 1);
            structureBlocks.addKills();
         }
      }

      super.entityInside(state, level, pos, livingEntity);
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new BiomassLumpEntity(pos, state);
   }
}
