package com.Harbinger.Spore.sEvents;

import com.Harbinger.Spore.Sentities.Calamities.Hohlfresser;
import com.Harbinger.Spore.Sentities.Utility.NukeEntity;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "spore",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class ForgeEvents {
   @SubscribeEvent
   public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null) {
         float maxShakeDistance = 25.0F;
         float maxShakeIntensity = 2.0F;
         List<NukeEntity> nukes = player.level().getEntitiesOfClass(NukeEntity.class, player.getBoundingBox().inflate((double)maxShakeDistance));
         List<Hohlfresser> worm = player.level().getEntitiesOfClass(Hohlfresser.class, player.getBoundingBox().inflate((double)maxShakeDistance));

         for(NukeEntity nuke : nukes) {
            double distance = (double)player.distanceTo(nuke);
            shakeCamera(distance, maxShakeDistance, maxShakeIntensity, event);
         }

         for(Hohlfresser worm1 : worm) {
            if (worm1.isUnderground() && worm1.isMoving() && worm1.isInWall(worm1)) {
               double distance = (double)player.distanceTo(worm1);
               shakeCamera(distance, maxShakeDistance, 4.0F, event);
            }
         }
      }

   }

   private static void shakeCamera(double distance, float maxShakeDistance, float maxShakeIntensity, ViewportEvent.ComputeCameraAngles event) {
      if (distance < (double)maxShakeDistance) {
         RandomSource randomSource = RandomSource.create();
         float intensity = (1.0F - (float)(distance / (double)maxShakeDistance)) * maxShakeIntensity;
         float shakeX = (randomSource.nextFloat() - 0.5F) * intensity;
         float shakeY = (randomSource.nextFloat() - 0.5F) * intensity;
         event.setYaw(event.getYaw() + shakeX);
         event.setPitch(event.getPitch() + shakeY);
      }

   }
}
