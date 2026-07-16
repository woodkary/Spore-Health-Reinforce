package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Sitems.BaseWeapons.IBlockableSwordItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SwordBlockingItemInHandRenderer extends ItemInHandRenderer {
    public static final Class<? extends ItemInHandRenderer> renderClass= (Class<? extends ItemInHandRenderer>) BytecodeUtil.resolveHiddenClassOrSelf(
            SwordBlockingItemInHandRenderer.class,
            Minecraft.class,
            EntityRenderDispatcher.class,
            ItemRenderer.class
    );
    public SwordBlockingItemInHandRenderer(Minecraft p_234241_, EntityRenderDispatcher p_234242_, ItemRenderer p_234243_) {
        super(p_234241_, p_234242_, p_234243_);
    }
    private boolean isBlocking(Player player) {
        if (player.isUsingItem()) {
            ItemStack usedItem = player.getUseItem();
            return usedItem.getItem() instanceof IBlockableSwordItem;
        }
        return false;

    }
    @Override
    public void renderArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
        if(isBlocking(player)&&hand==player.getUsedItemHand()){
            this.renderInstantBlocking(player, partialTicks, pitch, hand, swingProgress, stack, poseStack, buffer, combinedLight);
            return;
        }
        super.renderArmWithItem(player,partialTicks,pitch,hand,swingProgress,stack,equipProgress,poseStack,buffer,combinedLight);
    }
    private boolean renderInstantBlocking(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
        poseStack.pushPose();

        float attackCooldown = player.getAttackStrengthScale(partialTicks);
        float lowerOffset = 1.0F - attackCooldown;

        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isRightArm = arm == HumanoidArm.RIGHT;
        int sideMultiplier = isRightArm ? 1 : -1;

        poseStack.translate(
                (float) sideMultiplier * 0.56F,
                -0.52F + -0.6F - lowerOffset,
                -0.72F
        );

        if (isRightArm) {
            poseStack.mulPose(Axis.XP.rotation(4.6F));
            poseStack.mulPose(Axis.YP.rotation(0.26000002F));
            poseStack.mulPose(Axis.ZP.rotation(-4.9199996F));
            poseStack.translate(-0.2599999994039536, 0.23000000059604647, 0.6F);
        } else {
            poseStack.mulPose(Axis.XP.rotation(4.6F));
            poseStack.mulPose(Axis.YP.rotation(-0.26000002F));
            poseStack.mulPose(Axis.ZP.rotation(4.9199996F));
            poseStack.translate(0.2599999994039536, 0.23000000059604647, 0.6F);
            poseStack.scale(0.95F, 0.95F, 0.95F);
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                combinedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                player.level(),
                0
        );

        poseStack.popPose();
        return true;
    }

    @Override
    public void renderItem(LivingEntity p_270072_, ItemStack p_270793_, ItemDisplayContext p_270837_, boolean p_270203_, PoseStack p_270974_, MultiBufferSource p_270686_, int p_270103_) {
        super.renderItem(p_270072_, p_270793_, p_270837_, p_270203_, p_270974_, p_270686_, p_270103_);
    }
}
