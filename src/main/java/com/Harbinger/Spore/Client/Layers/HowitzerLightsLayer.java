package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.Models.HowitzerModel;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.Calamities.Howitzer;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HowitzerLightsLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Howitzer> {
   private int currentTexture;
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(activeLights.class), (p_114874_) -> {
      p_114874_.put(activeLights.RED, new ResourceLocation("spore", "textures/entity/howitzer_lights/red.png"));
      p_114874_.put(activeLights.BLUE, new ResourceLocation("spore", "textures/entity/howitzer_lights/blue.png"));
      p_114874_.put(activeLights.GREEN, new ResourceLocation("spore", "textures/entity/howitzer_lights/green.png"));
      p_114874_.put(activeLights.YELLOW, new ResourceLocation("spore", "textures/entity/howitzer_lights/yellow.png"));
   });

   public HowitzerLightsLayer(RenderLayerParent p_117346_) {
      super(p_117346_);
   }

   public void render(PoseStack stack, MultiBufferSource bufferSource, int p_117351_, Howitzer type, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
      if ((Boolean)SConfig.SERVER.costumes.get()) {
         LocalDate localdate = LocalDate.now();
         int j = localdate.get(ChronoField.MONTH_OF_YEAR);
         if (j == 12 || (Boolean)SConfig.SERVER.costumes_active.get()) {
            stack.pushPose();
            this.renderActiveLight(bufferSource, stack);
            if (type.tickCount % 40 == 0) {
               this.currentTexture = this.currentTexture == activeLights.values().length ? 0 : this.currentTexture + 1;
            }

            stack.popPose();
         }
      }

   }

   public void renderActiveLight(MultiBufferSource bufferSource, PoseStack stack) {
      VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.eyes((ResourceLocation)TEXTURE.get(activeLights.byId(this.currentTexture & 255))));
      ((HowitzerModel)this.getParentModel()).renderToBuffer(stack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   static enum activeLights {
      RED(0),
      BLUE(1),
      GREEN(2),
      YELLOW(3);

      private final int type;
      private static final activeLights[] BY_ID = (activeLights[])Arrays.stream(values()).sorted(Comparator.comparingInt(activeLights::getType)).toArray((x$0) -> new activeLights[x$0]);

      private activeLights(int type) {
         this.type = type;
      }

      public int getType() {
         return this.type;
      }

      public static activeLights byId(int id) {
         return BY_ID[id % BY_ID.length];
      }

      // $FF: synthetic method
      private static activeLights[] $values() {
         return new activeLights[]{RED, BLUE, GREEN, YELLOW};
      }
   }
}
