package com.Harbinger.Spore.Client.AnimationTrackers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SGReloadAnimationTracker {
   private static final Map<UUID,MagazineRotationState> rotationStates = new HashMap<>();

   public static void triggerRotationToChamber(Player player, int chamberIndex, int duration) {
      UUID playerId = player.getUUID();
      MagazineRotationState prevState = (MagazineRotationState)rotationStates.get(playerId);
      float startRot = prevState != null ? prevState.currentRotation : 0.0F;
      float targetRot = (float)chamberIndex * 90.0F;
      MagazineRotationState newState = new MagazineRotationState(startRot, targetRot, duration);
      rotationStates.put(playerId, newState);
   }

   public static float getCurrentRotation(Player player, float partialTicks) {
      MagazineRotationState state = (MagazineRotationState)rotationStates.get(player.getUUID());
      if (state == null) {
         return 0.0F;
      } else if (state.animationTicks <= 0) {
         return state.currentRotation;
      } else {
         float progress = 1.0F - ((float)state.animationTicks - partialTicks) / (float)state.maxAnimationTicks;
         progress = easeInOutCubic(progress);
         float delta = shortestAngleDelta(state.startRotation, state.targetRotation);
         return normalizeAngle(state.startRotation + delta * progress);
      }
   }

   public static void tickAll() {
      rotationStates.forEach((id, state) -> {
         if (state.animationTicks > 0) {
            --state.animationTicks;
            if (state.animationTicks <= 0) {
               state.currentRotation = normalizeAngle(state.targetRotation);
               state.startRotation = state.currentRotation;
               state.targetRotation = state.currentRotation;
            }
         }

      });
   }

   private static float shortestAngleDelta(float from, float to) {
      float delta = normalizeAngle(to) - normalizeAngle(from);
      if (delta > 180.0F) {
         delta -= 360.0F;
      }

      if (delta < -180.0F) {
         delta += 360.0F;
      }

      return delta;
   }

   private static float normalizeAngle(float angle) {
      return (angle % 360.0F + 360.0F) % 360.0F;
   }

   private static float easeInOutCubic(float x) {
      return x < 0.5F ? 4.0F * x * x * x : 1.0F - (float)Math.pow((double)(-2.0F * x + 2.0F), (double)3.0F) / 2.0F;
   }

   public static class MagazineRotationState {
      public float startRotation;
      public float currentRotation;
      public float targetRotation;
      public int animationTicks;
      public int maxAnimationTicks;

      public MagazineRotationState(float startRot, float targetRot, int duration) {
         this.startRotation = SGReloadAnimationTracker.normalizeAngle(startRot);
         this.currentRotation = this.startRotation;
         this.targetRotation = SGReloadAnimationTracker.normalizeAngle(targetRot);
         this.maxAnimationTicks = duration;
         this.animationTicks = duration;
      }
   }
}
