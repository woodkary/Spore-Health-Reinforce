package com.Harbinger.Spore.Sitems.Agents;

import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
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

public class ArmorSyringe extends AbstractSyringe {
   private final SporeArmorMutations mutations;

   public ArmorSyringe(SporeArmorMutations mutations) {
      this.mutations = mutations;
   }

   public int getColor() {
      return this.mutations.getColor();
   }

   public void useSyringe(ItemStack stack, LivingEntity living) {
      switch (this.mutations) {
         case REINFORCED -> living.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
         case SKELETAL -> living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
         case DROWNED -> living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0));
         case CHARRED -> living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
      }

      stack.shrink(1);
      this.addMycelium(living);
   }

   public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickAction, Player player) {
      ItemStack itemStack = slot.getItem();
      Item var7 = itemStack.getItem();
      if (var7 instanceof SporeArmorData weaponData) {
         if (clickAction == ClickAction.SECONDARY) {
            player.playNotifySound((SoundEvent)Ssounds.SYRINGE_INJECT.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            weaponData.setVariant(this.mutations, itemStack);
            stack.shrink(1);
            return true;
         }
      }

      return false;
   }

   public SporeArmorMutations getMutations() {
      return this.mutations;
   }

   public void appendHoverText(ItemStack stack, @Nullable Level p_41422_, List components, TooltipFlag p_41424_) {
      super.appendHoverText(stack, p_41422_, components, p_41424_);
      String var10001 = Component.translatable("spore.item.mutation").getString();
      components.add(Component.literal(var10001 + Component.translatable(this.mutations.getName()).getString()));
   }
}
