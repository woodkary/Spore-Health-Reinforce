package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OvergrownSpawnerEntity extends BlockEntity implements AnimatedEntity {
   public int ticks;
   private int time;
   public final int maxTime = 1200;

   public OvergrownSpawnerEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SblockEntities.OVERGROWN_SPAWNER.get(), pos, state);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt("time", this.getTime());
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.setTime(tag.getInt("time"));
   }

   public int getTime() {
      return this.time;
   }

   public void setTime(int time) {
      this.time = time;
   }

   public void addTime() {
      this.setTime(this.getTime() + 1);
   }

   public int getMaxTime() {
      Objects.requireNonNull(this);
      return 1200;
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, OvergrownSpawnerEntity e) {
      tick(level, pos, state, e);
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, OvergrownSpawnerEntity e) {
      tick(level, pos, state, e);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, OvergrownSpawnerEntity entity) {
      if (entity.ticks <= 360) {
         ++entity.ticks;
      } else {
         entity.ticks = 0;
      }

      int var10000 = entity.getTime();
      Objects.requireNonNull(entity);
      if (var10000 <= 1200) {
         entity.addTime();
      } else {
         entity.setTime(0);
         entity.feed(level, pos);
      }

   }

   private void feed(Level level1, BlockPos blockPos) {
      int range = 2 * (Integer)SConfig.DATAGEN.spawner_range.get();
      AABB aabb = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)range, (double)range, (double)range);
      List<LivingEntity> entities = level1.getEntitiesOfClass(LivingEntity.class, aabb);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockEntity blockEntity = level1.getBlockEntity(blockpos);
         if (blockEntity instanceof LivingStructureBlocks structureBlocks) {
            structureBlocks.addKills();
         }
      }

      for(Entity entity : entities) {
         if (entity instanceof Infected infected) {
            infected.setKills(infected.getKills() + 1);
            infected.setEvolution(infected.getEvoPoints() + 1);
         }

         if (entity instanceof Calamity calamity) {
            calamity.setKills(calamity.getKills() + 1);
         }
      }

   }

   public int getTicks() {
      return this.ticks;
   }
}
