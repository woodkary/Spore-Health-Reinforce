package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Compat.l2Hostility.ASMHurtKillerAuraTrait;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.traits.legendary.KillerAuraTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FallenMultipartEntity extends UtilityEntity implements Enemy, ColdWeakness,ICustomLifeCycleEntity {
   public FallenMultipartEntity(EntityType type, Level level) {
      super(type, level);
      initCustom();
   }
   @Override
   public void onRemovedFromWorld() {
      onRemoved();
   }
   public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      LazyOptional<T> opt=super.getCapability(cap, side);
      if(opt.resolve().isEmpty()||
              !ModList.get().isLoaded("l2hostility")){
         return opt;
      }
      if(opt.resolve().get() instanceof MobTraitCap traitCap) {
         traitCap.traits.keySet().forEach(trait -> {
            if(trait.getClass()== KillerAuraTrait.class){
               KlassPointerUtil.INSTANCE.replaceClass(trait, ASMHurtKillerAuraTrait.killerAuraTraitClass,"",0,0.0f);
            }
         });
      }
      return opt;
   }
   public void tick() {
      super.tick();
      tickCustomLifeCycle();
      if (this.random.nextInt(200) == 0 && this.onGround()) {
         AABB aabb = this.getBoundingBox().inflate((double)1.0F);

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockState blockState = this.level().getBlockState(blockpos);
            BlockState above = this.level().getBlockState(blockpos.above());
            if (!this.level().isClientSide() && blockState.isSolidRender(this.level(), blockpos) && above.isAir() && Math.random() < 0.1) {
               if (Math.random() < (double)0.5F) {
                  this.level().setBlock(blockpos.above(), ((Block)Sblocks.GROWTHS_BIG.get()).defaultBlockState(), 3);
               } else {
                  this.level().setBlock(blockpos.above(), ((Block)Sblocks.GROWTHS_SMALL.get()).defaultBlockState(), 3);
               }
            }
         }
      }

   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.HYPER;
   }

   @Override
   public LivingEntity entity() {
      return this;
   }
   @Override
   public boolean isProtoOrCalamity(){
      return false;
   }
   @Override
   public void actuallyHurt(DamageSource source, float amount) {
      actualHurt(source, amount);
   }
   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      addSaveData(tag);
   }
   @Override
   public void heal(float amount) {
      healSelf(amount);
   }
   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      readSaveData(tag);
   }
}
