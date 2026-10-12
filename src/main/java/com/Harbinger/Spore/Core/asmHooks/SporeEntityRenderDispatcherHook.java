package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Client-only implementation, initialized by the common bootstrap only on the client. */
public final class SporeEntityRenderDispatcherHook implements ISporeEntityRenderDispatcherHook {
    public static final ISporeEntityRenderDispatcherHook INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            ISporeEntityRenderDispatcherHook.class,
            SporeEntityRenderDispatcherHook.class
    );

    static {
        if (!INSTANCE.getClass().isHidden()) {
            throw new IllegalStateException("EntityRenderDispatcher hook must be a hidden class");
        }
    }

    @Override
    public void render(EntityRenderDispatcher dispatcher, Entity entity, double x, double y, double z,
                       float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) {
            return;
        }
        EntityRenderer<? super Entity> renderer = null;
        try {
            renderer = dispatcher.getRenderer(entity);
            Vec3 offset = renderer.getRenderOffset(entity, partialTicks);
            PoseStack.Pose previousPose = poseStack.last();
            poseStack.pushPose();
            try {
                poseStack.translate(x + offset.x(), y + offset.y(), z + offset.z());
                renderEntity(renderer, entity, yaw, partialTicks, poseStack, buffers, packedLight);
                if (entity.displayFireAnimation()) {
                    dispatcher.renderFlame(poseStack, buffers, entity);
                }
                poseStack.translate(-offset.x(), -offset.y(), -offset.z());
                if (dispatcher.options.entityShadows().get() && dispatcher.shouldRenderShadow
                        && renderer.shadowRadius > 0.0F && !entity.isInvisible()) {
                    float strength = (float) ((1.0D - dispatcher.distanceToSqr(
                            entity.getX(), entity.getY(), entity.getZ()) / 256.0D) * renderer.shadowStrength);
                    if (strength > 0.0F) {
                        EntityRenderDispatcher.renderShadow(poseStack, buffers, entity, strength, partialTicks,
                                dispatcher.level, Math.min(renderer.shadowRadius, 32.0F));
                    }
                }
                if (dispatcher.renderHitBoxes && !entity.isInvisible()
                        && !Minecraft.getInstance().showOnlyReducedInfo()) {
                    EntityRenderDispatcher.renderHitbox(poseStack, buffers.getBuffer(RenderType.lines()), entity, partialTicks);
                }
            } finally {
                restorePoseStack(poseStack, previousPose);
            }
        } catch (Throwable throwable) {
            CrashReport report = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory entityCategory = report.addCategory("Entity being rendered");
            entity.fillCrashReportCategory(entityCategory);
            CrashReportCategory details = report.addCategory("Renderer details");
            details.setDetail("Assigned renderer", renderer);
            details.setDetail("Location", CrashReportCategory.formatLocation(dispatcher.level, x, y, z));
            details.setDetail("Rotation", yaw);
            details.setDetail("Delta", partialTicks);
            throw new ReportedException(report);
        }
    }

    private void renderEntity(EntityRenderer<? super Entity> renderer, Entity entity, float yaw, float partialTicks,
                              PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        if (entity instanceof LivingEntity living && isAnimationDeath(living)) {
            // Read before entering the scope: reads inside it deliberately return zero.
            int effectiveDeathTime = CustomDeathTimeManager.INSTANCE.deathTimeGetFieldHook(living, living.deathTime);
            if (effectiveDeathTime > 0) {
                renderDeath(renderer, living, effectiveDeathTime, partialTicks, poseStack, buffers, packedLight);
                return;
            }
        }
        renderer.render(entity, yaw, partialTicks, poseStack, buffers, packedLight);
    }

    private boolean isAnimationDeath(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player)) {
                return true;
            }
            if (EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(player)) {
                return false;
            }
        }
        if (SporeJudge.isSporeEntity(entity)) {
            return false;
        }
        return EntityHeealuthManager.INSTANCE.isDeeadfOrDyaging(entity, entity.isDeadOrDying());
    }

    private void renderDeath(EntityRenderer<? super Entity> renderer, LivingEntity entity, int effectiveDeathTime,
                             float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        float yRot = entity.yRot;
        float yRotO = entity.yRotO;
        float headRot = entity.yHeadRot;
        float headRotO = entity.yHeadRotO;
        float bodyRot = entity.yBodyRot;
        float bodyRotO = entity.yBodyRotO;
        int deathTime = entity.deathTime;
        int hurtTime = entity.hurtTime;
        PoseStack.Pose previousPose = poseStack.last();
        CustomDeathTimeManager.INSTANCE.enterSpecialRender(entity);
        try {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getViewYRot(partialTicks)));
            entity.yRot = 0.0F;
            entity.yRotO = 0.0F;
            entity.yHeadRot = 0.0F;
            entity.yHeadRotO = 0.0F;
            entity.yBodyRot = 0.0F;
            entity.yBodyRotO = 0.0F;
            entity.deathTime = 0;
            entity.hurtTime = 20;
            poseStack.mulPose(Axis.ZN.rotationDegrees(deathAngle(effectiveDeathTime, partialTicks)));
            renderer.render(entity, 0.0F, partialTicks, poseStack, buffers, packedLight);
        } finally {
            try {
                entity.yRot = yRot;
                entity.yRotO = yRotO;
                entity.yHeadRot = headRot;
                entity.yHeadRotO = headRotO;
                entity.yBodyRot = bodyRot;
                entity.yBodyRotO = bodyRotO;
                entity.deathTime = deathTime;
                entity.hurtTime = hurtTime;
            } finally {
                try {
                    CustomDeathTimeManager.INSTANCE.exitSpecialRender(entity);
                } finally {
                    restorePoseStack(poseStack, previousPose);
                }
            }
        }
    }

    private float deathAngle(int deathTime, float partialTicks) {
        float interpolation = Float.isFinite(partialTicks) ? Mth.clamp(partialTicks, 0.0F, 1.0F) : 0.0F;
        float progress = ((Math.min(deathTime, 20) + interpolation - 1.0F) / 20.0F) * 1.6F;
        return Mth.clamp(Mth.sqrt(Math.max(0.0F, progress)), 0.0F, 1.0F) * 90.0F;
    }

    private void restorePoseStack(PoseStack poseStack, PoseStack.Pose previousPose) {
        // Also unwind pushes left by a renderer (or vanilla flame/hitbox drawing) that throws.
        while (poseStack.last() != previousPose && !poseStack.clear()) {
            poseStack.popPose();
        }
    }
}
