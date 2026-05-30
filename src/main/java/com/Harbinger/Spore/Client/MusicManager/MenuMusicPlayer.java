package com.Harbinger.Spore.Client.MusicManager;

import com.Harbinger.Spore.Core.Ssounds;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MenuMusicPlayer {
   private static SoundInstance currentMusic;
   private static final List MENU_TRACKS;
   private static final RandomSource random;

   public static void tick() {
      Minecraft mc = Minecraft.getInstance();
      if (currentMusic == null || !mc.getSoundManager().isActive(currentMusic)) {
         SoundEvent track = (SoundEvent)MENU_TRACKS.get(random.nextInt(MENU_TRACKS.size()));
         currentMusic = SimpleSoundInstance.forMusic(track);
         mc.getSoundManager().play(currentMusic);
      }
   }

   static {
      MENU_TRACKS = List.of((SoundEvent)Ssounds.ONCE_HERE.get());
      random = RandomSource.create();
   }
}
