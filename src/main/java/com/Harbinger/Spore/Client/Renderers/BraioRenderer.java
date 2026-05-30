package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BraionmilBabe;
import com.Harbinger.Spore.Client.Models.BraionmilModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Braionmil;
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
public class BraioRenderer extends BaseInfectedRenderer<Braionmil> {
   private final EntityModel normalBraio = this.getModel();
   private final EntityModel babeBraio;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/baio.png");
   private static final ResourceLocation BABE_TEXTURE = new ResourceLocation("spore", "textures/entity/braio_babe.png");
   private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/baio.png");

   public BraioRenderer(EntityRendererProvider.Context context) {
      super(context, new BraionmilModel(context.bakeLayer(BraionmilModel.LAYER_LOCATION)), 0.5F);
      this.babeBraio = new BraionmilBabe(context.bakeLayer(BraionmilBabe.LAYER_LOCATION));
   }

   protected boolean isBabe(Braionmil braionmil) {
      return Objects.equals(braionmil.getCustomName(), Component.literal("Babe"));
   }

   public ResourceLocation getTextureLocation(Braionmil entity) {
      return this.isBabe(entity) ? BABE_TEXTURE : TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYE_TEXTURE;
   }

   public void render(Braionmil braionmil, float p_115456_, float p_115457_, PoseStack stack, MultiBufferSource bufferSource, int p_115460_) {
      this.model = this.isBabe(braionmil) ? this.babeBraio : this.normalBraio;
      super.render(braionmil, p_115456_, p_115457_, stack, bufferSource, p_115460_);
   }
}
