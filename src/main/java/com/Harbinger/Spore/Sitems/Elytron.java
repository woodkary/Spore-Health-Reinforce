package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeBaseArmor;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.gameevent.GameEvent;

public class Elytron extends SporeBaseArmor implements CustomModelArmorData {
   private final ResourceLocation TEXTURE = new ResourceLocation("spore:textures/armor/elytron.png");

   public Elytron(Type type) {
      super(type, new int[]{0, 0, (Integer)SConfig.SERVER.ely_durability.get(), 0}, new int[]{0, 0, (Integer)SConfig.SERVER.ely_protection.get(), 0}, (float)(Integer)SConfig.SERVER.ely_toughness.get(), (float)(Integer)SConfig.SERVER.ely_knockback_resistance.get() / 10.0F, (SoundEvent)Ssounds.INFECTED_GEAR_EQUIP.get(), "Elytron");
   }

   public ResourceLocation getTextureLocation() {
      return this.TEXTURE;
   }

   public static class InfectedElytron extends Elytron {
      public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
         return "spore:textures/entity/empty.png";
      }

      public InfectedElytron() {
         super(Type.CHESTPLATE);
      }

      public boolean isValidRepairItem(ItemStack itemstack, ItemStack repairitem) {
         return Objects.equals(Sitems.BIOMASS.get(), repairitem.getItem());
      }

      public static boolean isFlyEnabled(ItemStack stack) {
         return stack.getDamageValue() < stack.getMaxDamage() - 10;
      }

      public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
         return isFlyEnabled(stack);
      }

      public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
         if (!entity.level().isClientSide) {
            int nextFlightTick = flightTicks + 1;
            if (nextFlightTick % 10 == 0) {
               if (nextFlightTick % 20 == 0) {
                  stack.hurtAndBreak(1, entity, (e) -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
                  if (entity instanceof Player) {
                     Player player = (Player)entity;
                     player.causeFoodExhaustion(0.1F);
                  }
               }

               entity.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
         }

         return true;
      }

      public int getEnchantmentValue() {
         return 2;
      }
   }
}
