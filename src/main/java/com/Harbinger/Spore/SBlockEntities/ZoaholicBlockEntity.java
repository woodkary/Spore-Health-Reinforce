package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Screens.ZoaholicMenu;
import com.Harbinger.Spore.Sentities.Signal;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ZoaholicBlockEntity extends BlockEntity implements AnimatedEntity, MenuProvider {
   private int ticks;
   private boolean hasBrain = false;
   private boolean hasHeart = false;
   private int amountOfInnards = 0;
   private int biomass = 0;
   private int processing = 0;
   private int side;

   public ZoaholicBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SblockEntities.ZOAHOLIC.get(), pos, state);
      this.side = this.setSide(state);
   }

   private int setSide(BlockState state) {
      Property var3 = state.getBlock().getStateDefinition().getProperty("facing");
      if (var3 instanceof DirectionProperty directionProperty) {
         return ((Direction)state.getValue(directionProperty)).get3DDataValue();
      } else {
         return 2;
      }
   }

   public int getAmountOfInnards() {
      return this.amountOfInnards;
   }

   public void setAmountOfInnards(int amountOfInnards) {
      this.amountOfInnards = amountOfInnards;
   }

   public boolean HasBrain() {
      return this.hasBrain;
   }

   public void setBrain(boolean hasBrain) {
      this.hasBrain = hasBrain;
   }

   public boolean HasHeart() {
      return this.hasHeart;
   }

   public void setHasHeart(boolean hasHeart) {
      this.hasHeart = hasHeart;
   }

   public boolean hasEnoughInnards() {
      return this.getAmountOfInnards() >= 2;
   }

   public int getBiomass() {
      return this.biomass;
   }

   public void setBiomass(int biomass) {
      this.biomass = biomass;
   }

   public void addBiomass(int value) {
      this.setBiomass(this.getBiomass() + value);
   }

   public int getProcessing() {
      return this.processing;
   }

   public void setProcessing(int processing) {
      this.processing = processing;
   }

   public void setSide(int i) {
      this.side = i;
   }

   public int getSide() {
      return this.side;
   }

   protected void saveAdditional(CompoundTag tag) {
      tag.putInt("innards", this.getAmountOfInnards());
      tag.putInt("biomass", this.getBiomass());
      tag.putBoolean("brain", this.HasBrain());
      tag.putBoolean("heart", this.HasHeart());
      tag.putInt("side", this.getSide());
      super.saveAdditional(tag);
   }

   public void load(CompoundTag tag) {
      this.setAmountOfInnards(tag.getInt("innards"));
      this.setBiomass(tag.getInt("biomass"));
      this.setBrain(tag.getBoolean("brain"));
      this.setHasHeart(tag.getBoolean("heart"));
      this.setSide(this.getSide());
      super.load(tag);
   }

   public void lowerBiomass() {
      this.setBiomass(this.getBiomass() - 1);
   }

   public int getTicks() {
      return this.ticks;
   }

   public boolean isActive() {
      return this.HasHeart() && this.HasBrain() && this.hasEnoughInnards() && this.getBiomass() > 1;
   }

   public void tickBlockEntity() {
      if (this.ticks < 360) {
         ++this.ticks;
      } else {
         this.ticks = 0;
      }

   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithFullMetadata();
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, ZoaholicBlockEntity e) {
      if (e.isActive()) {
         e.lowerBiomass();
         if (e.getProcessing() > 0) {
            e.setProcessing(e.getProcessing() - 1);
            if (e.getProcessing() == 198) {
               level.playSound((Player)null, pos, (SoundEvent)Ssounds.PRINTING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            if (e.getProcessing() == 1) {
               e.writeDocument(level, pos);
            }
         }

         if (e.getBiomass() % 60 == 0) {
            e.spreadMadness(level, pos);
            level.playSound((Player)null, pos, (SoundEvent)Ssounds.HEART_BEAT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, ZoaholicBlockEntity e) {
      e.tickBlockEntity();
   }

   protected @Nullable LivingEntity getAnomaly(Level level) {
      RandomSource source = RandomSource.create();
      List<LivingEntity> entities = new ArrayList();
      if (level instanceof ServerLevel serverLevel) {
         for(Entity entity : serverLevel.getAllEntities()) {
            if (entity instanceof LivingEntity living) {
               if (living instanceof Proto || living instanceof Calamity) {
                  entities.add(living);
               }
            }
         }
      }

      if (entities.isEmpty()) {
         return null;
      } else {
         int size = entities.size();
         return (LivingEntity)entities.get(source.nextInt(size));
      }
   }

   public void writeDocument(Level level, BlockPos blockPos) {
      LivingEntity livingEntity = this.getAnomaly(level);
      RandomSource randomSource = RandomSource.create();
      ItemStack stack = new ItemStack(Items.PAPER);
      if (livingEntity != null) {
         if (Math.random() < (double)0.2F) {
            this.alertAnomaly(blockPos, livingEntity);
            stack.setHoverName(Component.translatable("zoaholic.line_5"));
         } else {
            int x = livingEntity.getBlockX() + randomSource.nextInt(-50, 50);
            int z = livingEntity.getBlockZ() + randomSource.nextInt(-50, 50);
            String component = Component.translatable("zoaholic.line_3").getString();
            stack.setHoverName(Component.literal(component + " X:" + x + " Z:" + z));
         }
      } else {
         stack.setHoverName(Component.translatable("zoaholic.line_4"));
      }

      ItemEntity item = new ItemEntity(level, (double)blockPos.getX(), (double)blockPos.getY() + (double)0.5F, (double)blockPos.getZ(), stack);
      level.addFreshEntity(item);
   }

   private void alertAnomaly(BlockPos pos, LivingEntity livingEntity) {
      if (livingEntity instanceof Proto proto) {
         proto.setSignal(new Signal(true, pos));
      } else if (livingEntity instanceof Calamity calamity) {
         calamity.setSearchArea(pos);
      }

   }

   public void spreadMadness(Level level, BlockPos blockPos) {
      if (Math.random() < 0.1) {
         AABB aabb = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)16.0F, (double)16.0F, (double)16.0F);

         for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, aabb, (e) -> ((List)SConfig.SERVER.proto_sapient_target.get()).contains(e.getEncodeId()) || e instanceof Player)) {
            entity.addEffect(new MobEffectInstance((MobEffect)Seffects.MADNESS.get(), 2400, 0, false, false));
         }
      }

   }

   public Component getDisplayName() {
      return Component.translatable("block.spore.zoaholic");
   }

   public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      return new ZoaholicMenu(i, inventory);
   }
}
