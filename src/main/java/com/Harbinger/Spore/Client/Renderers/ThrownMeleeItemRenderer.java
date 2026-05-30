package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Sentities.Projectile.ThrownItemProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownMeleeItemRenderer extends EntityRenderer<ThrownItemProjectile> {
   private final ItemRenderer itemRenderer;

   public ThrownMeleeItemRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.itemRenderer = context.getItemRenderer();
   }

   public void render(ThrownItemProjectile entity, float value1, float value2, PoseStack stack, MultiBufferSource source, int lightValue) {
      if (!(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < (double)12.25F)) {
         stack.pushPose();
         stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(value2, entity.yRotO, entity.getYRot()) - 270.0F));
         stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(value2, entity.xRotO, entity.getXRot()) + 90.0F));
         stack.scale(1.2F, 1.2F, 1.2F);
         this.itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, lightValue, OverlayTexture.NO_OVERLAY, stack, source, entity.level(), entity.getId());
         stack.popPose();
      }

      super.render(entity, value1, value2, stack, source, lightValue);
   }

   public ResourceLocation getTextureLocation(ThrownItemProjectile t) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
