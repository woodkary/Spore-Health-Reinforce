package com.Harbinger.Spore.Client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TintedBufferSource implements MultiBufferSource {
   private final MultiBufferSource original;
   private final float r;
   private final float g;
   private final float b;
   private final float a;

   public TintedBufferSource(MultiBufferSource original, float r, float g, float b, float a) {
      this.original = original;
      this.r = r;
      this.g = g;
      this.b = b;
      this.a = a;
   }

   public VertexConsumer getBuffer(RenderType renderType) {
      VertexConsumer base = this.original.getBuffer(renderType);
      return new TintedVertexConsumer(base, this.r, this.g, this.b, this.a);
   }

   public static class TintedVertexConsumer implements VertexConsumer {
      private final VertexConsumer base;
      private final float r;
      private final float g;
      private final float b;
      private final float a;

      public TintedVertexConsumer(VertexConsumer base, float r, float g, float b, float a) {
         this.base = base;
         this.r = r;
         this.g = g;
         this.b = b;
         this.a = a;
      }

      public VertexConsumer vertex(double x, double y, double z) {
         return this.base.vertex(x, y, z);
      }

      public VertexConsumer color(int red, int green, int blue, int alpha) {
         return this.base.color((int)((float)red * this.r), (int)((float)green * this.g), (int)((float)blue * this.b), (int)((float)alpha * this.a));
      }

      public VertexConsumer uv(float u, float v) {
         return this.base.uv(u, v);
      }

      public VertexConsumer overlayCoords(int u, int v) {
         return this.base.overlayCoords(u, v);
      }

      public VertexConsumer uv2(int u, int v) {
         return this.base.uv2(u, v);
      }

      public VertexConsumer normal(float x, float y, float z) {
         return this.base.normal(x, y, z);
      }

      public void endVertex() {
         this.base.endVertex();
      }

      public void defaultColor(int red, int green, int blue, int alpha) {
         this.base.defaultColor((int)((float)red * this.r), (int)((float)green * this.g), (int)((float)blue * this.b), (int)((float)alpha * this.a));
      }

      public void unsetDefaultColor() {
         this.base.unsetDefaultColor();
      }
   }
}
