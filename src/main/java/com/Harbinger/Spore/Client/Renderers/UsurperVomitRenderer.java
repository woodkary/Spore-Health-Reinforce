package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Sentities.Projectile.VomitUsurperBall;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UsurperVomitRenderer extends EntityRenderer<VomitUsurperBall> {
   public static final ResourceLocation LOCATION = new ResourceLocation("");

   public UsurperVomitRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(VomitUsurperBall t) {
      return LOCATION;
   }
}
