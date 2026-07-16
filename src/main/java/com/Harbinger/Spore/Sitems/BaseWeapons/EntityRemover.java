package com.Harbinger.Spore.Sitems.BaseWeapons;

import com.Harbinger.Spore.Core.utils.BossEventUtil;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;
import java.util.function.Predicate;

public final class EntityRemover extends Item implements Predicate<Entity>,IBlockableSwordItem {
    private final UUID BONUS_DAMAGE_MODIFIER_UUID = UUID.fromString("035e66d6-5a74-402f-b64c-e61432ec39ba");
    private final UUID BONUS_REACH_MODIFIER_UUID = UUID.fromString("d8c35ba5-f440-4335-92b2-3c8b1b703706");
    private final UUID BONUS_RECHARGE_MODIFIER_UUID = UUID.fromString("6dee499d-60f9-4f91-9ae9-fa62f285cc24");
    public EntityRemover() {
        super(new Item.Properties().durability(1024).rarity(Rarity.EPIC));
    }
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if(slot == EquipmentSlot.MAINHAND){
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            return builder.put(Attributes.ATTACK_DAMAGE,new AttributeModifier(BONUS_DAMAGE_MODIFIER_UUID,"Tool modifier",4.0, AttributeModifier.Operation.ADDITION))
                    .put(Attributes.ATTACK_SPEED, new AttributeModifier(BONUS_RECHARGE_MODIFIER_UUID, "Tool modifier", -2.4, AttributeModifier.Operation.ADDITION))
                    .put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BONUS_REACH_MODIFIER_UUID, "Tool modifier",3, AttributeModifier.Operation.ADDITION))
                    .build();
        }
        return ImmutableMultimap.of();
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity living, int p_41415_) {
        if (!(living instanceof Player player)) {
            return;
        }
        for (Entity entity : SimpleRemoveUtil.INSTANCE.getAllEntities(level, this)) {
            double x=entity.getX();
            double y=entity.getY();
            double z=entity.getZ();
            SporeAttackUtil.INSTANCE.playSound(level,null, x,y,z,
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.crit(entity);
            if(SimpleRemoveUtil.INSTANCE.remove(entity, Entity.RemovalReason.CHANGED_DIMENSION)){
                BossEventUtil.INSTANCE.disableBossEvent(entity);
            }
        }
        SimpleRemoveUtil.INSTANCE.resetRenderData(level);
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        replaceRender(player, stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player)){
            return false;
        }
        Entity target= SporeAttackUtil.INSTANCE.getTargetedEntity(player,player.getEntityReach());
        if(target!=null){
            SimpleRemoveUtil.INSTANCE.remove(target, Entity.RemovalReason.CHANGED_DIMENSION);
        }
        return false;
    }

    @Override
    public int getDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean test(Entity entity) {
        return entity instanceof Player;
    }
}
