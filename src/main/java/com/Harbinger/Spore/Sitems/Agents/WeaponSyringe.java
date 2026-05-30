package com.Harbinger.Spore.Sitems.Agents;

import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WeaponSyringe extends AbstractSyringe {
   private final SporeToolsMutations mutations;

   public WeaponSyringe(SporeToolsMutations mutations) {
      this.mutations = mutations;
   }

   public int getColor() {
      return this.mutations.getColor();
   }

   public void useSyringe(ItemStack stack, LivingEntity living) {
      switch (this.mutations) {
         case VAMPIRIC:
            living.heal(4.0F);
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
            break;
         case CALCIFIED:
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
            break;
         case BEZERK:
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0));
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
            break;
         case TOXIC:
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 0));
            break;
         case ROTTEN:
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 0));
      }

      stack.shrink(1);
      this.addMycelium(living);
   }

   public SporeToolsMutations getMutations() {
      return this.mutations;
   }

   public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickAction, Player player) {
      ItemStack itemStack = slot.getItem();
      Item var7 = itemStack.getItem();
      if (var7 instanceof SporeWeaponData weaponData) {
         if (clickAction == ClickAction.SECONDARY) {
            player.playNotifySound((SoundEvent)Ssounds.SYRINGE_INJECT.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            weaponData.setVariant(this.mutations, itemStack);
            stack.shrink(1);
            return true;
         }
      }

      return false;
   }

   public void appendHoverText(ItemStack stack, @Nullable Level p_41422_, List components, TooltipFlag p_41424_) {
      super.appendHoverText(stack, p_41422_, components, p_41424_);
      String var10001 = Component.translatable("spore.item.mutation").getString();
      components.add(Component.literal(var10001 + Component.translatable(this.mutations.getName()).getString()));
   }
}
