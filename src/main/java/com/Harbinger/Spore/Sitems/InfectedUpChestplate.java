package com.Harbinger.Spore.Sitems;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class InfectedUpChestplate extends UpgradedInfectedExoskeleton {
    public InfectedUpChestplate() {
        super(Type.CHESTPLATE);
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

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player entity) {
        if (entity.horizontalCollision && entity.isCrouching()) {
            Vec3 initialVec = entity.getDeltaMovement();
            Vec3 climbVec = new Vec3(initialVec.x, 0.2D, initialVec.z);
            entity.setDeltaMovement(climbVec.x * 0.91D, climbVec.y * 0.98D, climbVec.z * 0.91D);
        }
        super.onArmorTick(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            components.add(Component.translatable("item.armor.shift").withStyle(ChatFormatting.DARK_RED));
        } else {
            components.add(Component.translatable("item.armor.normal").withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(itemStack, level, components, tooltipFlag);
    }
}
