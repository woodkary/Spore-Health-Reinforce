package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Objects;

public final class InfectedElytron extends Elytron {
    public InfectedElytron() {
        super(Type.CHESTPLATE);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "spore:textures/entity/empty.png";
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemstack, ItemStack repairitem) {
        return Objects.equals(Sitems.BIOMASS.get(), repairitem.getItem());
    }

    public static boolean isFlyEnabled(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage() - 10;
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return isFlyEnabled(stack);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide) {
            int nextFlightTick = flightTicks + 1;
            if (nextFlightTick % 10 == 0) {
                if (nextFlightTick % 20 == 0) {
                    stack.hurtAndBreak(1, entity, new EquipmentSlotBreakConsumer(EquipmentSlot.CHEST));
                    if (entity instanceof Player player) {
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
