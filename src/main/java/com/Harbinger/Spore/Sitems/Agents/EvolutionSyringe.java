package com.Harbinger.Spore.Sitems.Agents;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.Harbinger.Spore.Sitems.BaseItem2;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EvolutionSyringe extends BaseItem2 {
   public EvolutionSyringe() {
      super((new Properties()).stacksTo(1));
   }

   public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List tooltipComponents, TooltipFlag p_41424_) {
      super.appendHoverText(p_41421_, p_41422_, tooltipComponents, p_41424_);
      tooltipComponents.add(Component.literal("CREATIVE ONLY").withStyle(ChatFormatting.RED));
   }

   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity living, InteractionHand hand) {
      if (living instanceof Infected infected) {
         if (infected instanceof EvolvingInfected) {
            if (infected instanceof EvolvedInfected) {
               infected.setEvoPoints(infected.getEvoPoints() + (Integer)SConfig.SERVER.min_kills_hyper.get());
            } else {
               infected.setEvoPoints(infected.getEvoPoints() + (Integer)SConfig.SERVER.min_kills.get());
            }

            infected.setEvolution((Integer)SConfig.SERVER.evolution_age_human.get());
            return InteractionResult.SUCCESS;
         }
      }

      if (living instanceof Mound mound) {
         mound.setAge(mound.getAge() + 1);
         return InteractionResult.SUCCESS;
      } else {
         if (living instanceof Calamity calamity) {
            if (!calamity.getAdaptation()) {
               calamity.ActivateAdaptation();
               return InteractionResult.SUCCESS;
            }
         }

         if (living instanceof Scamper scamper) {
            scamper.setAge((Integer)SConfig.SERVER.scamper_age.get());
            return InteractionResult.SUCCESS;
         } else {
            return super.interactLivingEntity(stack, player, living, hand);
         }
      }
   }
}
