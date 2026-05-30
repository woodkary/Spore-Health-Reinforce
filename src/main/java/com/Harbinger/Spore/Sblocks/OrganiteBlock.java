package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class OrganiteBlock extends Block {
   public OrganiteBlock() {
      super(Properties.of().strength(6.0F, 4.0F).sound(SoundType.SLIME_BLOCK));
   }

   public void tick(BlockState state, ServerLevel level, BlockPos blockPos, RandomSource randomSource) {
      super.tick(state, level, blockPos, randomSource);
      AABB searchbox = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)35.0F, (double)35.0F, (double)35.0F);

      for(Entity entity1 : level.getEntitiesOfClass(LivingEntity.class, searchbox)) {
         if (entity1 instanceof LivingEntity entity) {
            if (!entity.hasEffect((MobEffect)Seffects.MYCELIUM.get()) && !(entity instanceof Infected) && !(entity instanceof UtilityEntity) && !((List)SConfig.SERVER.blacklist.get()).contains(entity.getEncodeId()) && !Utilities.helmetList().contains(entity.getItemBySlot(EquipmentSlot.HEAD).getItem())) {
               entity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 0));
               entity.addEffect(new MobEffectInstance((MobEffect)Seffects.MARKER.get(), 400, 0));
            }
         }
      }

      level.scheduleTick(blockPos, this, 100);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos pos, BlockState oldState, boolean moving) {
      super.onPlace(blockstate, level, pos, oldState, moving);
      level.scheduleTick(pos, this, 100);
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      RandomSource random = RandomSource.create();

      for(String str : (List<String>)SConfig.DATAGEN.organite_loot.get()) {
         String[] string = str.split("\\|");
         Item item1 = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string[0]));
         if (item1 != null) {
            ItemStack itemStack = new ItemStack(item1);
            int minimalV = Integer.parseUnsignedInt(string[2]);
            int maxV = Integer.parseUnsignedInt(string[3]);
            int m;
            if (minimalV == maxV) {
               m = maxV;
            } else {
               try {
                  m = random.nextInt(minimalV, maxV);
               } catch (Exception var17) {
                  m = 1;
               }
            }

            if (itemStack != ItemStack.EMPTY && Math.random() < (double)((float)Integer.parseUnsignedInt(string[1]) / 100.0F)) {
               itemStack.setCount(m);
               ItemEntity item = new ItemEntity(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), itemStack);
               item.setPickUpDelay(10);
               level.addFreshEntity(item);
            }
         }
      }

      return super.onDestroyedByPlayer(state, level, pos, player, false, fluid);
   }
}
