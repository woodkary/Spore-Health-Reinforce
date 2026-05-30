package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.GroberfubModel;
import com.Harbinger.Spore.Client.Models.GroberfubOmniModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Hyper.Grober;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GroberRenderer extends BaseInfectedRenderer<Grober> {
   protected final EntityModel defaultModel = this.getModel();
   protected final EntityModel omniModel = new GroberfubOmniModel();
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/grober.png");
   private static final ResourceLocation OMNI = new ResourceLocation("spore", "textures/entity/omniman.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/grober.png");

   public GroberRenderer(EntityRendererProvider.Context context) {
      super(context, new GroberfubModel(context.bakeLayer(GroberfubModel.LAYER_LOCATION)), 0.5F);
   }

   public boolean isOmniMan(Grober entity) {
      return Objects.equals(entity.getCustomName(), Component.literal("Omni-Man")) || Objects.equals(entity.getCustomName(), Component.literal("Nolan"));
   }

   public ResourceLocation getTextureLocation(Grober entity) {
      return this.isOmniMan(entity) ? OMNI : TEXTURE;
   }

   public void render(Grober type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = this.isOmniMan(type) ? this.omniModel : this.defaultModel;
      super.render(type, value1, value2, stack, bufferSource, light);
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
