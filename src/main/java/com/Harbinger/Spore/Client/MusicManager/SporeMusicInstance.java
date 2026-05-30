package com.Harbinger.Spore.Client.MusicManager;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class SporeMusicInstance extends AbstractTickableSoundInstance {
   private float targetVolume = 1.0F;
   private float fadeSpeed = 0.01F;
   private int ticks;
   private static final RandomSource random = RandomSource.create();

   public SporeMusicInstance(SoundEvent sound) {
      super(sound, SoundSource.MUSIC, random);
      this.looping = false;
      this.relative = true;
      this.attenuation = Attenuation.NONE;
      this.pitch = 1.0F;
      this.volume = 1.0F;
   }

   public void fadeIn() {
      this.targetVolume = 1.0F;
      this.fadeSpeed = 0.01F;
   }

   public void fadeOut() {
      this.targetVolume = 0.0F;
      this.fadeSpeed = 0.02F;
   }

   public void tick() {
      if (this.volume < this.targetVolume) {
         this.volume = Math.min(this.targetVolume, this.volume + this.fadeSpeed);
      } else if (this.volume > this.targetVolume) {
         this.volume = Math.max(this.targetVolume, this.volume - this.fadeSpeed);
      }

      if (this.volume <= 0.001F && this.targetVolume == 0.0F) {
         this.stop();
      }

      ++this.ticks;
   }

   public int getTickCount() {
      return this.ticks;
   }
}
