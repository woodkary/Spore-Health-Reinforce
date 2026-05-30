package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BurstUsurperModel;
import com.Harbinger.Spore.Client.Models.SprayUsurperModel;
import com.Harbinger.Spore.Client.Models.UsurperModel;
import com.Harbinger.Spore.Sentities.Organoids.Usurper;
import com.Harbinger.Spore.Sentities.Variants.UsurperVariants;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UsurperRenderer extends OrganoidMobRenderer<Usurper> {
   private final EntityModel base = this.getModel();
   private final EntityModel burst;
   private final EntityModel spray;
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(UsurperVariants.class), (p_114874_) -> {
      p_114874_.put(UsurperVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/usurper.png"));
      p_114874_.put(UsurperVariants.BURST, new ResourceLocation("spore", "textures/entity/burst_usurper.png"));
      p_114874_.put(UsurperVariants.SPRAY, new ResourceLocation("spore", "textures/entity/spray_usurper.png"));
   });

   public UsurperRenderer(EntityRendererProvider.Context context) {
      super(context, new UsurperModel(context.bakeLayer(UsurperModel.LAYER_LOCATION)), 1.0F);
      this.burst = new BurstUsurperModel(context.bakeLayer(BurstUsurperModel.LAYER_LOCATION));
      this.spray = new SprayUsurperModel(context.bakeLayer(SprayUsurperModel.LAYER_LOCATION));
   }

   protected EntityModel getUsurperModel(Usurper entity) {
      switch (entity.getVariant()) {
         case BURST -> {
            return this.burst;
         }
         case SPRAY -> {
            return this.spray;
         }
         default -> {
            return this.base;
         }
      }
   }

   public ResourceLocation getTextureLocation(Usurper entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public void render(Usurper type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int value3) {
      this.model = this.getUsurperModel(type);
      super.render(type, value1, value2, stack, bufferSource, value3);
   }
}
