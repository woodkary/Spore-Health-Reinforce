package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.ForlornHowlerModel;
import com.Harbinger.Spore.Client.Models.HowlerModel;
import com.Harbinger.Spore.Client.Models.SculkHowlerModel;
import com.Harbinger.Spore.Client.Models.SwarmerHowlerModel;
import com.Harbinger.Spore.Client.Models.bansheeHowlerModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Howler;
import com.Harbinger.Spore.Sentities.Variants.HowlerVariants;
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
public class HowlerRenderer extends BaseInfectedRenderer<Howler> {
   private final EntityModel defaultModel = this.getModel();
   private final EntityModel banshee;
   private final EntityModel sculk;
   private final EntityModel forlow;
   private final EntityModel swarm;
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/howler.png");
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(HowlerVariants.class), (p_114874_) -> {
      p_114874_.put(HowlerVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/howler.png"));
      p_114874_.put(HowlerVariants.BANSHEE, new ResourceLocation("spore", "textures/entity/freaky_howler.png"));
      p_114874_.put(HowlerVariants.SONIC, new ResourceLocation("spore", "textures/entity/sculkhowler.png"));
      p_114874_.put(HowlerVariants.FORLORN, new ResourceLocation("spore", "textures/entity/forlorn_howler.png"));
      p_114874_.put(HowlerVariants.SWARMER, new ResourceLocation("spore", "textures/entity/swarmer_howler.png"));
   });

   public HowlerRenderer(EntityRendererProvider.Context context) {
      super(context, new HowlerModel(context.bakeLayer(HowlerModel.LAYER_LOCATION)), 0.5F);
      this.banshee = new bansheeHowlerModel(context.bakeLayer(bansheeHowlerModel.LAYER_LOCATION));
      this.sculk = new SculkHowlerModel(context.bakeLayer(SculkHowlerModel.LAYER_LOCATION));
      this.forlow = new ForlornHowlerModel(context.bakeLayer(ForlornHowlerModel.LAYER_LOCATION));
      this.swarm = new SwarmerHowlerModel(context.bakeLayer(SwarmerHowlerModel.LAYER_LOCATION));
   }

   public ResourceLocation getTextureLocation(Howler entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public EntityModel getModel(HowlerVariants variants) {
      EntityModel var10000;
      switch (variants) {
         case BANSHEE -> var10000 = this.banshee;
         case SONIC -> var10000 = this.sculk;
         case FORLORN -> var10000 = this.forlow;
         case SWARMER -> var10000 = this.swarm;
         default -> var10000 = this.defaultModel;
      }

      return var10000;
   }

   public void render(Howler type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = this.getModel(type.getVariant());
      super.render(type, value1, value2, stack, bufferSource, light);
   }
}
