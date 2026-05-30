package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Package.SongInitializingPacket;
import com.Harbinger.Spore.SBlockEntities.HiveSpawnBlockEntity;
import com.Harbinger.Spore.SBlockEntities.LivingStructureBlocks;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HiveSpawn extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED;

   public HiveSpawn() {
      super(Properties.of().strength(4.0F, 4.0F).sound(SoundType.SLIME_BLOCK).randomTicks().noOcclusion().noCollission());
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, Boolean.FALSE));
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new HiveSpawnBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState blockState, boolean value) {
      super.onPlace(state, level, pos, blockState, value);
      level.scheduleTick(pos, this, 80);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos blockPos, RandomSource random) {
      BlockEntity entity = level.getBlockEntity(blockPos);
      level.scheduleTick(blockPos, this, 80);
      if (entity instanceof LivingStructureBlocks structureBlocks) {
         AABB searchbox = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)50.0F, (double)50.0F, (double)50.0F);

         for(Entity entity1 : level.getEntitiesOfClass(Infected.class, searchbox)) {
            if (entity1 instanceof Infected infected) {
               if (infected.getKills() > 1 && structureBlocks.getKills() <= (Integer)SConfig.DATAGEN.hive_spawn_kills.get()) {
                  infected.setSearchPos(blockPos);
               }
            }
         }

         if (structureBlocks.getKills() >= (Integer)SConfig.DATAGEN.hive_spawn_kills.get() && this.checkForOtherMinds(blockPos, level)) {
            level.removeBlock(blockPos, true);
            Proto proto = (Proto)((EntityType)Sentities.PROTO.get()).create(level);
            if (proto != null) {
               proto.tickEmerging();
               proto.setPos((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
               proto.loadChunks();
               if ((Boolean)SConfig.SERVER.teleport_hive.get()) {
                  Proto.teleportToSurface(level, proto);
               }

               level.addFreshEntity(proto);
            }

            for(ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
               player.playNotifySound((SoundEvent)Ssounds.REBIRTH.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
               player.displayClientMessage(Component.translatable("hivemind_summon_message"), false);
               if (SporeSavedData.getHiveminds().size() == 1) {
                  SporePacketHandler.sendToClient(new SongInitializingPacket(3, false, true), player);
               }
            }
         } else if (structureBlocks.getKills() >= (Integer)SConfig.DATAGEN.hive_spawn_kills.get() && !this.checkForOtherMinds(blockPos, level)) {
            StructureTemplate template = level.getStructureManager().getOrCreate(new ResourceLocation("spore", "mega_biomass_tower"));
            BlockPos pos = new BlockPos(blockPos.getX() - 4, blockPos.getY() - 3, blockPos.getZ() - 4);
            template.placeInWorld(level, pos, pos, (new StructurePlaceSettings()).setIgnoreEntities(true), random, 3);
         }
      }

   }

   boolean checkForOtherMinds(BlockPos blockPos, Level level) {
      int e = (Integer)SConfig.DATAGEN.hive_generate.get();
      AABB searchbox = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)e, (double)e, (double)e);

      for(Entity entity1 : level.getEntitiesOfClass(Proto.class, searchbox)) {
         if (entity1 instanceof Proto) {
            return false;
         }
      }

      return true;
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      if (blockEntity instanceof LivingStructureBlocks structureBlocks) {
         if (entity instanceof Infected infected) {
            if (infected.getKills() > 1) {
               infected.setKills(infected.getKills() - 1);
               structureBlocks.addKills();
            }
         }
      }

      super.entityInside(state, level, pos, entity);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder stateBuilder) {
      super.createBlockStateDefinition(stateBuilder);
      stateBuilder.add(new Property[]{WATERLOGGED});
   }

   public PushReaction getPistonPushReaction(BlockState p_153494_) {
      return PushReaction.PUSH_ONLY;
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   @javax.annotation.Nullable
   public BlockEntityTicker getTicker(Level level, BlockState p_153274_, BlockEntityType type) {
      return createBrainTicker(level, type, (BlockEntityType)SblockEntities.HIVE_SPAWN.get());
   }

   @javax.annotation.Nullable
   protected static BlockEntityTicker createBrainTicker(Level level, BlockEntityType type, BlockEntityType p_151990_) {
      return level.isClientSide ? createTickerHelper(type, p_151990_, HiveSpawnBlockEntity::clientTick) : null;
   }

   static {
      WATERLOGGED = BlockStateProperties.WATERLOGGED;
   }
}
