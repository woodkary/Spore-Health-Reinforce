package com.Harbinger.Spore.Sentities.AI.CalamitiesAI;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sblocks.CDUBlock;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public class SporeBurstSupport extends Goal {
   private final Calamity calamity;

   public SporeBurstSupport(Calamity calamity) {
      this.calamity = calamity;
   }

   public boolean canUse() {
      return this.calamity.isAlive() && this.calamity.getRandom().nextInt(300) == 0 && this.calamity.getTarget() != null && this.calamity.distanceToSqr(this.calamity.getTarget()) < (double)200.0F;
   }

   public void start() {
      this.calamity.setStun(60);
      Calamity var2 = this.calamity;
      if (var2 instanceof TrueCalamity trueCalamity) {
         this.calamity.playSound((SoundEvent)Ssounds.SPORE_BURST.get());
         AABB boundingBox = this.calamity.getBoundingBox().inflate((double)trueCalamity.chemicalRange());
         this.sporeBurst(trueCalamity.buffs(), trueCalamity.debuffs(), boundingBox);
         this.killCDUs(boundingBox);
      }

      super.start();
   }

   private void killCDUs(AABB aabb) {
      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         if (this.calamity.level().getBlockState(blockpos).is((Block)Sblocks.CDU.get())) {
            CDUBlock.replaceCDU(blockpos, this.calamity.level());
         }
      }

   }

   private void sporeBurst(List<String> buffs, List<String> debuffs, AABB boundingBox) {
      this.calamity.playAmbientSound();

      for(Entity entity : this.calamity.level().getEntities(this.calamity, boundingBox)) {
         if (entity instanceof LivingEntity living) {
            if (this.calamity.TARGET_SELECTOR.test(living) && !Utilities.helmetList().contains(living.getItemBySlot(EquipmentSlot.HEAD).getItem())) {
               this.applyEffects(living, debuffs);
            }

            if (living instanceof UtilityEntity || living instanceof Infected) {
               this.applyEffects(living, buffs);
               if (living instanceof Infected) {
                  Infected infected = (Infected)living;
                  infected.setKills(infected.getKills() + this.calamity.getRandom().nextInt(4));
               }
            }
         }
      }

   }

   private void applyEffects(LivingEntity living, List<String> effects) {
      if (!effects.isEmpty()) {
         for(String str : effects) {
            String[] string = str.split("\\|");
            MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string[0]));
            if (effect != null && !living.hasEffect(effect)) {
               living.addEffect(new MobEffectInstance(effect, Integer.parseUnsignedInt(string[1]), Integer.parseUnsignedInt(string[2])));
            }
         }
      }

   }
}
