package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.EDBiomassModel;
import com.Harbinger.Spore.Sentities.Projectile.DrownedFleshBomb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedFleshBombRenderer extends EntityRenderer<DrownedFleshBomb> {
   public static final ResourceLocation BASIC_ROUND = new ResourceLocation("spore", "textures/entity/db_texture.png");
   private final EDBiomassModel model = new EDBiomassModel();

   public DrownedFleshBombRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public void render(DrownedFleshBomb entity, float value2, float value, PoseStack stack, MultiBufferSource source, int p_116116_) {
      stack.pushPose();
      stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(value, entity.yRotO, entity.getYRot()) - 90.0F));
      stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(value, entity.xRotO, entity.getXRot()) + 90.0F));
      VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(source, this.model.renderType(this.getTextureLocation(entity)), false, false);
      this.model.setupAnim(entity, 0.0F, 0.0F, (float)entity.tickCount, 0.0F, 0.0F);
      this.model.renderToBuffer(stack, vertexconsumer, p_116116_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      stack.popPose();
      super.render(entity, value2, value, stack, source, p_116116_);
   }

   public ResourceLocation getTextureLocation(DrownedFleshBomb t) {
      return BASIC_ROUND;
   }
}
