package com.Harbinger.Spore.Sitems.Agents;

import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorMutations;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ArmorSyringe extends AbstractSyringe implements ArmorMutationSyringe {
    private final SporeArmorMutations mutations;
    public ArmorSyringe(SporeArmorMutations mutations) {
        this.mutations = mutations;
    }
    @Override
    public int getColor() {
        return mutations.getColor();
    }

    @Override
    public void useSyringe(ItemStack stack, LivingEntity living) {
        if (mutations == SporeArmorMutations.REINFORCED) {
            living.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
        } else if (mutations == SporeArmorMutations.SKELETAL) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
        } else if (mutations == SporeArmorMutations.DROWNED) {
            living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0));
        } else if (mutations == SporeArmorMutations.CHARRED) {
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
        }
        stack.shrink(1);
        addMycelium(living);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickAction, Player player) {
        ItemStack itemStack = slot.getItem();
        if (itemStack.getItem() instanceof SporeArmorData weaponData && clickAction == ClickAction.SECONDARY){
            player.playNotifySound(Ssounds.SYRINGE_INJECT.get(), SoundSource.AMBIENT,1F,1F);
            weaponData.setVariant(mutations,itemStack);
            stack.shrink(1);
            return true;
        }
        return false;
    }

    @Override
    public SporeArmorMutations getMutations(){return mutations;}
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level p_41422_, List<Component> components, TooltipFlag p_41424_) {
        super.appendHoverText(stack, p_41422_, components, p_41424_);
        components.add(Component.literal(Component.translatable("spore.item.mutation").getString()+Component.translatable(mutations.getName()).getString()));
    }
}
