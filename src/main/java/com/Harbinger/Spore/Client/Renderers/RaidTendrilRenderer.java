package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.ArenaTendrilModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Utility.ArenaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaidTendrilRenderer extends BaseInfectedRenderer<ArenaEntity> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/raid_tendril.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/raid_tendril.png");

   public RaidTendrilRenderer(EntityRendererProvider.Context context) {
      super(context, new ArenaTendrilModel(context.bakeLayer(ArenaTendrilModel.LAYER_LOCATION)), 1.0F);
   }

   public ResourceLocation getTextureLocation(ArenaEntity entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(ArenaEntity type, float p_115456_, float p_115457_, PoseStack stack, MultiBufferSource p_115459_, int p_115460_) {
      if (type.isBurrowing() || type.isEmerging()) {
         float a = type.getBbHeight();
         float b = 0.0F;
         if (type.isBurrowing()) {
            b = 0.0F - a / 60.0F * (float)type.getBorrow();
         } else if (type.isEmerging()) {
            b = -a + a / 60.0F * (float)type.getEmerge();
         }

         stack.translate((double)0.0F, (double)b, (double)0.0F);
      }

      super.render(type, p_115456_, p_115457_, stack, p_115459_, p_115460_);
   }

   protected boolean isShaking(ArenaEntity type) {
      return type.isBurrowing() || type.isEmerging();
   }
}
