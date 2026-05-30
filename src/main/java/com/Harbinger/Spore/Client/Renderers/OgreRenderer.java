package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.OgreModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Hyper.Ogre;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OgreRenderer extends BaseInfectedRenderer<Ogre> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/ogre.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/ogre.png");

   public OgreRenderer(EntityRendererProvider.Context context) {
      super(context, new OgreModel(context.bakeLayer(OgreModel.LAYER_LOCATION)), 1.0F);
   }

   public ResourceLocation getTextureLocation(Ogre entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
