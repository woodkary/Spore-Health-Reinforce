package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingPlant extends GenericFoliageBlock {
   public static final BooleanProperty HANGING;
   protected static final VoxelShape AABB;
   protected static final VoxelShape HANGING_AABB;

   public HangingPlant() {
      super(Properties.of().strength(0.0F, 0.0F).noCollission().sound(SoundType.CROP).randomTicks());
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(HANGING, Boolean.FALSE)).setValue(WATERLOGGED, Boolean.FALSE));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext p_153467_) {
      FluidState fluidstate = p_153467_.getLevel().getFluidState(p_153467_.getClickedPos());

      for(Direction direction : p_153467_.getNearestLookingDirections()) {
         if (direction.getAxis() == Axis.Y) {
            BlockState blockstate = (BlockState)this.defaultBlockState().setValue(HANGING, direction == Direction.UP);
            if (blockstate.canSurvive(p_153467_.getLevel(), p_153467_.getClickedPos())) {
               return (BlockState)blockstate.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
            }
         }
      }

      return null;
   }

   public VoxelShape getShape(BlockState p_153474_, BlockGetter p_153475_, BlockPos p_153476_, CollisionContext p_153477_) {
      return (Boolean)p_153474_.getValue(HANGING) ? HANGING_AABB : AABB;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder p_153490_) {
      p_153490_.add(new Property[]{HANGING, WATERLOGGED});
   }

   public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
      Direction direction = getConnectedDirection(state).getOpposite();
      BlockState blockState = levelReader.getBlockState(pos.relative(direction));
      return blockState.canOcclude() || blockState.getBlock() == Sblocks.HANGING_FUNGAL_STEM.get();
   }

   protected static Direction getConnectedDirection(BlockState state) {
      return (Boolean)state.getValue(HANGING) ? Direction.DOWN : Direction.UP;
   }

   public PushReaction getPistonPushReaction(BlockState p_153494_) {
      return PushReaction.DESTROY;
   }

   public BlockState updateShape(BlockState p_153483_, Direction p_153484_, BlockState p_153485_, LevelAccessor p_153486_, BlockPos p_153487_, BlockPos p_153488_) {
      if ((Boolean)p_153483_.getValue(WATERLOGGED)) {
         p_153486_.scheduleTick(p_153487_, Fluids.WATER, Fluids.WATER.getTickDelay(p_153486_));
      }

      return getConnectedDirection(p_153483_).getOpposite() == p_153484_ && !p_153483_.canSurvive(p_153486_, p_153487_) ? Blocks.AIR.defaultBlockState() : super.updateShape(p_153483_, p_153484_, p_153485_, p_153486_, p_153487_, p_153488_);
   }

   public FluidState getFluidState(BlockState p_153492_) {
      return (Boolean)p_153492_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_153492_);
   }

   public boolean isPathfindable(BlockState p_153469_, BlockGetter p_153470_, BlockPos p_153471_, PathComputationType p_153472_) {
      return true;
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (entity instanceof LivingEntity living) {
         if (Utilities.TARGET_SELECTOR.Test(living)) {
            entity.makeStuckInBlock(state, new Vec3((double)0.8F, (double)0.75F, (double)0.8F));
            if (!level.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
               double d0 = Math.abs(entity.getX() - entity.xOld);
               double d1 = Math.abs(entity.getZ() - entity.zOld);
               if (d0 >= (double)0.003F || d1 >= (double)0.003F) {
                  entity.hurt(entity.damageSources().cactus(), 1.0F);
               }
            }
         }
      }

   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos blockpos, RandomSource randomSource) {
      if (Math.random() < 0.1) {
         BlockState block2 = ((Block)Sblocks.BLOOM_GG.get()).defaultBlockState();
         Property var7 = state.getBlock().getStateDefinition().getProperty("hanging");
         if (var7 instanceof BooleanProperty) {
            BooleanProperty property = (BooleanProperty)var7;
            if ((Boolean)state.getValue(property)) {
               level.setBlock(blockpos, (BlockState)block2.setValue(property, true), 3);
            } else {
               level.setBlock(blockpos, block2, 3);
            }
         }
      }

      super.randomTick(state, level, blockpos, randomSource);
   }

   static {
      HANGING = BlockStateProperties.HANGING;
      AABB = Shapes.or(box((double)4.0F, (double)0.0F, (double)4.0F, (double)12.0F, (double)6.0F, (double)12.0F), new VoxelShape[0]);
      HANGING_AABB = Shapes.or(box((double)4.0F, (double)4.0F, (double)4.0F, (double)12.0F, (double)10.0F, (double)12.0F), box((double)6.0F, (double)10.0F, (double)6.0F, (double)10.0F, (double)16.0F, (double)10.0F));
   }
}
