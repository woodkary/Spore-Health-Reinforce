package com.Harbinger.Spore.Client.MusicManager;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SporeMusicPlayer {
   private static final SoundManager SoundManager = Minecraft.getInstance().getSoundManager();
   private static SporeMusicInstance currentMusic;
   private static SoundEvent oldMusic;
   private static SoundEvent battleMusic;
   private static int battleMusicTicks;
   private static int worldUpdateDelay;
   private static final RandomSource random = RandomSource.create();
   private static final List DEFAULT_PLAYLIST;
   private static final List POST_PLAYLIST;

   public static void tickMusic() {
      if (currentMusic != null) {
         currentMusic.tick();
      }

      if (worldUpdateDelay > 0) {
         --worldUpdateDelay;
      }

      if (battleMusicTicks > 0) {
         --battleMusicTicks;
         if (battleMusic != null && oldMusic != battleMusic) {
            playMusic(battleMusic);
         }

         if (battleMusicTicks == 60 && oldMusic == battleMusic) {
            stopMusic();
         }
      }

   }

   private static void playRandomDefault() {
      List<SoundEvent> music = new ArrayList(DEFAULT_PLAYLIST);
      if (oldMusic != null) {
         music.remove(oldMusic);
      }

      SoundEvent pick = (SoundEvent)music.get(random.nextInt(music.size()));
      playMusic(pick);
   }

   private static void playRandomPost() {
      List<SoundEvent> music = new ArrayList(POST_PLAYLIST);
      if (oldMusic != null) {
         music.remove(oldMusic);
      }

      SoundEvent pick = (SoundEvent)music.get(random.nextInt(music.size()));
      playMusic(pick);
   }

   private static void playMusic(SoundEvent music) {
      Minecraft mc = Minecraft.getInstance();
      if (!(mc.screen instanceof TitleScreen)) {
         if (currentMusic == null || !SoundManager.isActive(currentMusic) || !music.equals(oldMusic)) {
            stopMusic();
            currentMusic = new SporeMusicInstance(music);
            currentMusic.fadeIn();
            SoundManager.play(currentMusic);
            oldMusic = music;
         }
      }
   }

   private static void stopMusic() {
      if (currentMusic != null && !currentMusic.isStopped()) {
         currentMusic.fadeOut();
      }

   }

   public static void handlePacket(boolean pro, int id, boolean inCombat) {
      if (pro && id == 3) {
         if (currentMusic != null) {
            currentMusic.fadeOut();
         }

         playMusic((SoundEvent)Ssounds.SOMETHING_ONCE_GREAT.get());
      } else if (inCombat && id >= 0 && (Boolean)SConfig.SERVER.encounter_songs.get()) {
         SongVariantsPerEntity variants = SongVariantsPerEntity.getVariant(id);
         if (battleMusicTicks <= 0 || currentMusic != null && currentMusic.getTickCount() >= variants.getDuration() - 10) {
            SoundEvent event = variants.getName();
            if (currentMusic != null) {
               currentMusic.fadeOut();
            }

            battleMusic = event;
            playMusic(event);
         }

         battleMusicTicks = 200;
      } else {
         battleMusicTicks = 0;
         battleMusic = null;
         if (currentMusic == null || !SoundManager.isActive(currentMusic)) {
            if (pro) {
               playRandomPost();
            } else {
               playRandomDefault();
            }
         }

      }
   }

   static {
      DEFAULT_PLAYLIST = List.of((SoundEvent)Ssounds.BICENTENNIAL.get(), (SoundEvent)Ssounds.CYCLE_OF_EVOLUTION.get(), (SoundEvent)Ssounds.DESOLATION.get(), (SoundEvent)Ssounds.FALL_OF_MAN.get(), (SoundEvent)Ssounds.MANMADE_HORRORS.get(), (SoundEvent)Ssounds.MYCONOCLAST.get(), (SoundEvent)Ssounds.NOURISHMENT.get(), (SoundEvent)Ssounds.PROJECT_REGENESIS.get(), (SoundEvent)Ssounds.RECLAIMATION.get(), (SoundEvent)Ssounds.RESTLESS_REACH.get(), (SoundEvent)Ssounds.ROADS_ONCE_TRAVELLED.get(), (SoundEvent)Ssounds.SLEEPLESS_DREAMING.get(), (SoundEvent)Ssounds.START_ANEW.get(), (SoundEvent)Ssounds.THE_SOIL_TALKS.get(), (SoundEvent)Ssounds.THEY_AWAKEN.get(), (SoundEvent)Ssounds.THEY_GROW_BELOW.get(), (SoundEvent)Ssounds.MYCONOCLAST.get());
      POST_PLAYLIST = List.of((SoundEvent)Ssounds.BROKEN_REFLECTION.get(), (SoundEvent)Ssounds.DECAY.get(), (SoundEvent)Ssounds.ENDLESS_FEAST.get(), (SoundEvent)Ssounds.MYCONAUT.get(), (SoundEvent)Ssounds.NATURAL_OCCURANCE.get(), (SoundEvent)Ssounds.NEUROGENESIS.get(), (SoundEvent)Ssounds.PROTOTYPE.get(), (SoundEvent)Ssounds.REPURPOSED.get(), (SoundEvent)Ssounds.ROT.get(), (SoundEvent)Ssounds.SPORE_BURST_SONG.get(), (SoundEvent)Ssounds.SYNAPTIC_RELAPSE.get(), (SoundEvent)Ssounds.THEY_LISTEN.get(), (SoundEvent)Ssounds.WHAT_WE_BECOME.get(), (SoundEvent)Ssounds.WHISPERS.get(), (SoundEvent)Ssounds.MENTAL_MUTILATION.get());
   }

   public static enum SongVariantsPerEntity {
      CALAMITY(0, (SoundEvent)Ssounds.MYCOPHOBIA.get(), 2720),
      VANGUARD(1, (SoundEvent)Ssounds.BANE_OF_SETTLEMENT.get(), 1640),
      VIGIL(2, (SoundEvent)Ssounds.VIRULENT_VIGIL.get(), 2880),
      PROTO(3, (SoundEvent)Ssounds.SOMETHING_ONCE_GREAT.get(), 6000);

      private static final SongVariantsPerEntity[] BY_ID = (SongVariantsPerEntity[])Arrays.stream(values()).sorted(Comparator.comparingInt(SongVariantsPerEntity::getId)).toArray((x$0) -> new SongVariantsPerEntity[x$0]);
      private final int id;
      private final SoundEvent name;
      private final int duration;

      private SongVariantsPerEntity(int id, SoundEvent name, int duration) {
         this.id = id;
         this.name = name;
         this.duration = duration;
      }

      public SoundEvent getName() {
         return this.name;
      }

      public int getId() {
         return this.id;
      }

      public int getDuration() {
         return this.duration;
      }

      public static SongVariantsPerEntity byId(int id) {
         return BY_ID[id % BY_ID.length];
      }

      public static SongVariantsPerEntity getVariant(int var) {
         return byId(var & 255);
      }

      // $FF: synthetic method
      private static SongVariantsPerEntity[] $values() {
         return new SongVariantsPerEntity[]{CALAMITY, VANGUARD, VIGIL, PROTO};
      }
   }
}
