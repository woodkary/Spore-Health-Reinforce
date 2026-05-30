package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.DualSpitterModel;
import com.Harbinger.Spore.Client.Models.SniperSpitterModel;
import com.Harbinger.Spore.Client.Models.SpitterModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Spitter;
import com.Harbinger.Spore.Sentities.Variants.SpitterVariants;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpitterRenderer extends BaseInfectedRenderer<Spitter> {
   private final EntityModel defaultModel = this.getModel();
   private final EntityModel dualModel;
   private final EntityModel sniperModel;
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(SpitterVariants.class), (p_114874_) -> {
      p_114874_.put(SpitterVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/spitter.png"));
      p_114874_.put(SpitterVariants.EXPLOSIVE, new ResourceLocation("spore", "textures/entity/exploding_spitter.png"));
      p_114874_.put(SpitterVariants.BILE, new ResourceLocation("spore", "textures/entity/spitter_bile.png"));
      p_114874_.put(SpitterVariants.DUAL, new ResourceLocation("spore", "textures/entity/spitter_dual.png"));
      p_114874_.put(SpitterVariants.SNIPER, new ResourceLocation("spore", "textures/entity/sniper_spitter.png"));
   });
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/spitter.png");

   public SpitterRenderer(EntityRendererProvider.Context context) {
      super(context, new SpitterModel(context.bakeLayer(SpitterModel.LAYER_LOCATION)), 0.5F);
      this.dualModel = new DualSpitterModel(context.bakeLayer(DualSpitterModel.LAYER_LOCATION));
      this.sniperModel = new SniperSpitterModel(context.bakeLayer(SniperSpitterModel.LAYER_LOCATION));
   }

   protected EntityModel entityModel(int val) {
      EntityModel var10000;
      switch (val) {
         case 3 -> var10000 = this.dualModel;
         case 4 -> var10000 = this.sniperModel;
         default -> var10000 = this.defaultModel;
      }

      return var10000;
   }

   public ResourceLocation getTextureLocation(Spitter entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Spitter type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = this.entityModel(type.getTypeVariant());
      super.render(type, value1, value2, stack, bufferSource, light);
   }
}
