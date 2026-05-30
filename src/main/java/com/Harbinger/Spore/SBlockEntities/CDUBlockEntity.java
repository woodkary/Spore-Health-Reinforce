package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.ExtremelySusThings.CustomJsonReader.SporeCduConversionData;
import com.Harbinger.Spore.Sblocks.CDUBlock;
import com.Harbinger.Spore.Screens.CDUMenu;
import com.Harbinger.Spore.Sentities.Utility.InfectionTendril;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class CDUBlockEntity extends BlockEntity implements MenuProvider, AnimatedEntity {
   public static final TagKey fungalItems = ItemTags.create(new ResourceLocation("spore:weapons"));
   public final int maxFuel;
   public int fuel;
   private final List<StoreDouble> blockMap;
   private final int side;
   private int ticks;

   public CDUBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SblockEntities.CDU.get(), pos, state);
      this.maxFuel = (Integer)SConfig.DATAGEN.cryo_time.get();
      this.blockMap = this.fabricateBlocks();
      this.side = this.setSide(state);
   }

   public int getTicks() {
      return this.ticks;
   }

   public int getSide() {
      return this.side;
   }

   public boolean infested() {
      if (this.level == null) {
         return false;
      } else {
         BlockState state = this.level.getBlockState(this.worldPosition);
         return state.is((Block)Sblocks.CDU.get()) ? (Boolean)state.getValue(CDUBlock.LIT) : false;
      }
   }

   public boolean isRunning() {
      return this.fuel > 0;
   }

   private int setSide(BlockState state) {
      Property var3 = state.getBlock().getStateDefinition().getProperty("facing");
      if (var3 instanceof DirectionProperty directionProperty) {
         return ((Direction)state.getValue(directionProperty)).get3DDataValue();
      } else {
         return 2;
      }
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, CDUBlockEntity cduBlockEntity) {
      ++cduBlockEntity.ticks;
   }

   private List<StoreDouble> fabricateBlocks() {
      List<StoreDouble> blocks = new ArrayList();

      for(String str : (List<String>)SConfig.DATAGEN.block_cleaning.get()) {
         String[] string = str.split("\\|");
         Block blockCon1 = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[0]));
         Block blockCon2 = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[1]));
         if (blockCon1 != null && blockCon2 != null) {
            blocks.add(new StoreDouble(blockCon1, blockCon2));
         }
      }

      return blocks;
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt("fuel", this.getFuel());
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.setFuel(tag.getInt("fuel"));
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithFullMetadata();
   }

   public void setFuel(int i) {
      this.fuel = i;
   }

   public int getFuel() {
      return this.fuel;
   }

   public void cleanInfection(BlockPos blockPos) {
      int range = 2 * (Integer)SConfig.DATAGEN.cryo_range.get();
      AABB aabb = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)range, (double)range, (double)range);
      List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, aabb);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState state = this.level.getBlockState(blockpos);
         if (state.is(TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("spore", "removable_foliage"))) && Math.random() < 0.2) {
            this.level.removeBlock(blockpos, false);
         }

         if (state == ((Block)Sblocks.REMAINS.get()).defaultBlockState()) {
            this.level.setBlock(blockpos, ((Block)Sblocks.FROZEN_REMAINS.get()).defaultBlockState(), 3);
         }

         if (Math.random() < 0.2 && !this.blockMap.isEmpty()) {
            for(StoreDouble storeDouble : this.blockMap) {
               if (storeDouble.value1 == state.getBlock()) {
                  this.level.setBlock(blockpos, storeDouble.value2.defaultBlockState(), 3);
               }
            }

            this.convertFromJson(this.level, state, blockpos);
         }

         if (Math.random() < 0.1) {
            if (state.is(Utilities.biomass) || state.is((Block)Sblocks.MEMBRANE_BLOCK.get())) {
               this.level.setBlock(blockpos, ((Block)Sblocks.FROST_BURNED_BIOMASS.get()).defaultBlockState(), 3);
            }

            if (state == ((LiquidBlock)Sblocks.BILE.get()).defaultBlockState()) {
               this.level.setBlock(blockpos, ((Block)Sblocks.CRUSTED_BILE.get()).defaultBlockState(), 3);
            }
         }

         if (Math.random() < 0.001 && (Boolean)SConfig.DATAGEN.cryo_snow.get()) {
            BlockState blockState1 = this.level.getBlockState(blockpos.above());
            if (state.isSolidRender(this.level, blockPos) && blockState1.isAir()) {
               RandomSource randomSource = RandomSource.create();
               int layer = randomSource.nextInt(1, 4);
               BlockState snowState = Blocks.SNOW.defaultBlockState();
               Property var13 = snowState.getBlock().getStateDefinition().getProperty("layers");
               if (var13 instanceof IntegerProperty) {
                  IntegerProperty property = (IntegerProperty)var13;
                  this.level.setBlock(blockpos.above(), (BlockState)snowState.setValue(property, layer), 3);
               }
            }
         }
      }

      for(Entity entity : entities) {
         if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
               MobEffectInstance instance = livingEntity.getEffect((MobEffect)Seffects.FROSTBITE.get());
               int intensity = instance == null ? 0 : instance.getAmplifier() + 1;
               livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.FROSTBITE.get(), 1200, intensity));
            }
         }

         if (entity instanceof Player player) {
            boolean be = false;

            for(ItemStack stack : player.getArmorSlots()) {
               if (stack.is(fungalItems)) {
                  be = true;
                  break;
               }
            }

            if (be) {
               MobEffectInstance instance = player.getEffect((MobEffect)Seffects.FROSTBITE.get());
               int intensity = instance == null ? 0 : instance.getAmplifier() + 1;
               player.addEffect(new MobEffectInstance((MobEffect)Seffects.FROSTBITE.get(), 600, intensity));
            }
         }

         if (entity instanceof ScentEntity || entity instanceof InfectionTendril) {
            entity.discard();
         }
      }

   }

   void convertFromJson(Level level, BlockState blockstate, BlockPos blockpos) {
      Block targetBlock = SporeCduConversionData.getResult(blockstate.getBlock());
      if (targetBlock != null) {
         BlockState _bs = targetBlock.defaultBlockState();
         UnmodifiableIterator var6 = blockstate.getValues().entrySet().iterator();

         while(var6.hasNext()) {
            Map.Entry<Property<?>, Comparable<?>> entry = (Map.Entry)var6.next();
            Property property = _bs.getBlock().getStateDefinition().getProperty(((Property)entry.getKey()).getName());
            if (property != null) {
               try {
                  _bs = (BlockState)_bs.setValue(property, (Comparable)entry.getValue());
               } catch (Exception var10) {
               }
            }
         }

         level.setBlock(blockpos, _bs, 3);
      }
   }

   public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, CDUBlockEntity e) {
      if (CDUBlock.isCDUUsable(blockPos, e.level) && e.getFuel() > 0 && !level.isClientSide) {
         --e.fuel;
         if (e.getFuel() % 200 == 0) {
            e.cleanInfection(blockPos);
         }

         if (e.getFuel() % 80 == 0) {
            level.playSound((Player)null, blockPos, (SoundEvent)Ssounds.CDU_AMBIENT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   public Component getDisplayName() {
      return Component.translatable("block.spore.cdu");
   }

   public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      return new CDUMenu(i, inventory);
   }

   static record StoreDouble(Block value1, Block value2) {
   }
}
