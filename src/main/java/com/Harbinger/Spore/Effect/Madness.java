package com.Harbinger.Spore.Effect;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Utility.Illusion;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Madness extends MobEffect {
   public Madness() {
      super(MobEffectCategory.HARMFUL, 419435);
   }

   public void applyEffectTick(LivingEntity entity, int intense) {
      if (Math.random() < (Double)SConfig.SERVER.chance_hallucination_spawn.get() * 0.01) {
         Level var4 = entity.level();
         if (var4 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var4;
            if (intense > 1 && intense < 4) {
               this.SummonIllusion(entity, serverLevel, false, entity.getId());
            }

            if (intense >= 4) {
               this.SummonIllusion(entity, serverLevel, true, entity.getId());
            }
         }
      }

      if (Math.random() < 0.1) {
         this.playClientSounds(entity);
      }

      if (Math.random() < 0.1 && intense > 0 && entity instanceof ServerPlayer player) {
         this.feelingWatched(player);
      }

   }

   public void feelingWatched(ServerPlayer player) {
      player.displayClientMessage(Component.translatable("vigil.message"), true);
   }

   public void SummonIllusion(LivingEntity entity, ServerLevel serverLevel, boolean value, int targetId) {
      int x = entity.getRandom().nextInt(-6, 6);
      int z = entity.getRandom().nextInt(-6, 6);
      Illusion illusion = new Illusion((EntityType)Sentities.ILLUSION.get(), serverLevel);
      illusion.setSeeAble(false);
      illusion.setAdvanced(value);
      illusion.setTargetId(targetId);
      DifficultyInstance difficultyInstance = serverLevel.getCurrentDifficultyAt(entity.blockPosition());
      illusion.moveTo(entity.getX() + (double)x, entity.getY(), entity.getZ() + (double)z);
      illusion.finalizeSpawn(serverLevel, difficultyInstance, MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
      serverLevel.addFreshEntity(illusion);
   }

   public void playClientSounds(LivingEntity entity) {
      entity.playSound((SoundEvent)Ssounds.MADNESS.get());
   }

   public boolean isDurationEffectTick(int duration, int intensity) {
      if (this == Seffects.MADNESS.get()) {
         return duration % 80 == 0;
      } else {
         return false;
      }
   }

   public List getCurativeItems() {
      ArrayList<ItemStack> ret = new ArrayList();
      ret.add(ItemStack.EMPTY);
      return ret;
   }
}
