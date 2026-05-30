package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.GrabberSlasherModel;
import com.Harbinger.Spore.Client.Models.ScrewerSlasherModel;
import com.Harbinger.Spore.Client.Models.SlasherModel;
import com.Harbinger.Spore.Client.Models.SmasherSlasherModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Slasher;
import com.Harbinger.Spore.Sentities.Variants.SlasherVariants;
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
public class SlasherRenderer extends BaseInfectedRenderer<Slasher> {
   private final EntityModel defaultModel;
   private final EntityModel smasher;
   private final GrabberSlasherModel grabber;
   private final ScrewerSlasherModel screwer;
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(SlasherVariants.class), (p_114874_) -> {
      p_114874_.put(SlasherVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/slasher.png"));
      p_114874_.put(SlasherVariants.PIERCER, new ResourceLocation("spore", "textures/entity/piercer.png"));
      p_114874_.put(SlasherVariants.SMASHER, new ResourceLocation("spore", "textures/entity/smasher_slasher.png"));
      p_114874_.put(SlasherVariants.GRABBER, new ResourceLocation("spore", "textures/entity/grabber.png"));
      p_114874_.put(SlasherVariants.SCREW, new ResourceLocation("spore", "textures/entity/screwer.png"));
   });
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/slasher.png");

   public SlasherRenderer(EntityRendererProvider.Context context) {
      super(context, new SlasherModel(context.bakeLayer(SlasherModel.LAYER_LOCATION)), 0.5F);
      this.defaultModel = this.model;
      this.smasher = new SmasherSlasherModel(context.bakeLayer(SmasherSlasherModel.LAYER_LOCATION));
      this.grabber = new GrabberSlasherModel(context.bakeLayer(GrabberSlasherModel.LAYER_LOCATION));
      this.screwer = new ScrewerSlasherModel(context.bakeLayer(ScrewerSlasherModel.LAYER_LOCATION));
   }

   private EntityModel getDefaultModel(int i) {
      Object var10000;
      switch (i) {
         case 2 -> var10000 = this.smasher;
         case 3 -> var10000 = this.grabber;
         case 4 -> var10000 = this.screwer;
         default -> var10000 = this.defaultModel;
      }

      return (EntityModel)var10000;
   }

   public void render(Slasher type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = this.getDefaultModel(type.getTypeVariant());
      super.render(type, value1, value2, stack, bufferSource, light);
   }

   public ResourceLocation getTextureLocation(Slasher entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
