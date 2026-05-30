package com.Harbinger.Spore.Client.Special;

import com.Harbinger.Spore.Client.Layers.EyeLayer;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class BaseInfectedRenderer<T extends Mob> extends MobRenderer<T, EntityModel<T>> {
   private final EntityRenderDispatcher entityRenderer;

   public BaseInfectedRenderer(EntityRendererProvider.Context context, EntityModel<T> model, float shadow) {
      super(context, model, shadow);
      this.addLayer(new EyeLayer<>(this, this.eyeLayerTexture()));
      this.entityRenderer = context.getEntityRenderDispatcher();
   }

   public abstract ResourceLocation eyeLayerTexture();

   protected boolean isShaking(T type) {
      if (type instanceof Infected infected) {
         return infected.isFreazing();
      } else if (type instanceof Calamity calamity) {
         return calamity.isStunned();
      } else {
         return false;
      }
   }

   public void render(T type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      if (type instanceof Infected infected) {
         if (Objects.equals(infected.getOrigin(), "")) {
            super.render(type, value1, value2, stack, bufferSource, light);
            return;
         }
      }

      if (type instanceof Infected infected) {
         Entity var9 = Minecraft.getInstance().cameraEntity;
         if (var9 instanceof Player player) {
            MobEffectInstance instance = player.getEffect((MobEffect)Seffects.MADNESS.get());
            Entity entityForm = this.getForm(infected);
            if (instance != null && instance.getAmplifier() > 0 && player.distanceTo(infected) > 30.0F && entityForm instanceof LivingEntity living) {
               this.renderIllusions(living, infected, value2, stack, bufferSource, light);
            } else {
               super.render(type, value1, value2, stack, bufferSource, light);
            }

            return;
         }
      }

      super.render(type, value1, value2, stack, bufferSource, light);
   }

   protected float getBob(Infected illusion, float p_115306_) {
      return (float)illusion.tickCount + p_115306_;
   }

   public Entity getForm(Infected infected) {
      ResourceLocation location = new ResourceLocation(infected.getOrigin());
      EntityType<?> entityType = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(location);
      return entityType != null ? entityType.create(infected.level()) : null;
   }

   public void renderIllusions(LivingEntity living, Infected infected, float value2, PoseStack stack, MultiBufferSource source, int light) {
      float f = Mth.rotLerp(value2, infected.yBodyRotO, infected.yBodyRot);
      float f1 = Mth.rotLerp(value2, infected.yHeadRotO, infected.yHeadRot);
      float f2 = f1 - f;
      float f6 = Mth.lerp(value2, infected.xRotO, infected.getXRot());
      float f7 = this.getBob(infected, value2);
      float f8 = 0.0F;
      float f5 = 0.0F;
      if (infected.isAlive()) {
         f8 = infected.walkAnimation.speed(value2);
         f5 = infected.walkAnimation.position(value2);
         if (f8 > 1.0F) {
            f8 = 1.0F;
         }
      }

      if (living != null) {
         stack.pushPose();
         stack.mulPose(Axis.YP.rotationDegrees(-infected.yBodyRot));
         stack.mulPose(Axis.XP.rotationDegrees(180.0F));
         stack.translate((double)0.0F, (double)-1.5F, (double)0.0F);
         EntityRenderer var15 = this.entityRenderer.getRenderer(living);
         if (var15 instanceof MobRenderer) {
            MobRenderer mobRenderer = (MobRenderer)var15;
            EntityModel model = mobRenderer.getModel();
            ResourceLocation texture = mobRenderer.getTextureLocation(living);
            VertexConsumer consumer = source.getBuffer(RenderType.entityCutout(texture));
            model.prepareMobModel(living, f5, f8, value2);
            model.setupAnim(living, f5, f8, f7, f2, f6);
            model.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         }

         stack.popPose();
      }

   }
}
