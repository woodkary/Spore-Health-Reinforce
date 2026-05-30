package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.NucleaChestplateLayer;
import com.Harbinger.Spore.Client.Models.NuckelaveModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Nuclealave;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NucleaRenderer extends BaseInfectedRenderer<Nuclealave> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/nuckelave.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/nuckelave.png");

   public NucleaRenderer(EntityRendererProvider.Context context) {
      super(context, new NuckelaveModel(context.bakeLayer(NuckelaveModel.LAYER_LOCATION), false), 1.0F);
      this.addLayer(new NucleaChestplateLayer(this, context.getModelSet(), context.getModelManager()));
      this.addLayer(new NucleaItemLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(Nuclealave entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   class NucleaItemLayer extends ItemInHandLayer {
      public NucleaItemLayer(RenderLayerParent renderLayerParent, ItemInHandRenderer item) {
         super(renderLayerParent, item);
      }

      public void render(PoseStack stack, MultiBufferSource bufferSource, int light, Nuclealave type, float p_117208_, float p_117209_, float p_117210_, float p_117211_, float p_117212_, float p_117213_) {
         stack.pushPose();
         stack.translate((double)0.0F, -0.55, -0.65);
         super.render(stack, bufferSource, light, type, p_117208_, p_117209_, p_117210_, p_117211_, p_117212_, p_117213_);
         stack.popPose();
      }
   }
}
