package com.Harbinger.Spore.Client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SpecialEffects {
   private static Ring buildRing(Vec3 center, Vec3 direction, float radius, float rotationOffset) {
      Ring ring = new Ring();
      Vec3 up = Math.abs(direction.y) > 0.99 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
      Vec3 right = direction.cross(up).normalize();
      Vec3 forward = right.cross(direction).normalize();

      for(int i = 0; i < 8; ++i) {
         float angle = (float)i * ((float)Math.PI * 2F) / 8.0F + rotationOffset;
         float x = Mth.cos(angle);
         float y = Mth.sin(angle);
         Vec3 offset = right.scale((double)(x * radius)).add(forward.scale((double)(y * radius)));
         ring.vertices[i] = new Vector3f((float)(center.x + offset.x), (float)(center.y + offset.y), (float)(center.z + offset.z));
         ring.normals[i] = (new Vector3f((float)offset.x, (float)offset.y, (float)offset.z)).normalize();
         ring.uvs[i] = new Vector2f((float)i / 8.0F, 0.0F);
      }

      return ring;
   }

   public static void renderFunnel(Matrix4f matrix4f, Matrix3f matrix3f, int light, MultiBufferSource buffer, Vec3[] segments, float partial, int packedColor, float sizeA, ResourceLocation location) {
      if (segments != null && segments.length >= 2) {
         Ring previousRing = null;
         VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(location));

         for(int i = 1; i < segments.length; ++i) {
            Vec3 from = segments[i - 1];
            Vec3 to = segments[i];
            Vec3 dir = to.subtract(from).normalize();
            float size = calculateSize(i, segments.length, sizeA);
            float segmentProgress = (float)i / (float)segments.length;
            float rotation = partial + segmentProgress * 4.0F;
            Ring currentRing = buildRing(to, dir, size, rotation);
            if (previousRing != null) {
               stitchRings(previousRing, currentRing, consumer, matrix4f, matrix3f, packedColor, light, OverlayTexture.NO_OVERLAY);
            }

            previousRing = currentRing;
         }

      }
   }

   private static void stitchRings(Ring a, Ring b, VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f, int color, int light, int overlay) {
      for(int i = 0; i < 8; ++i) {
         int next = (i + 1) % 8;
         float vA = 0.0F;
         float vB = 1.0F;
         consumer.vertex(matrix4f, a.vertices[i].x, a.vertices[i].y, a.vertices[i].z).color(color).uv(a.uvs[i].x, vA).overlayCoords(overlay).uv2(light).normal(matrix3f, a.normals[i].x(), a.normals[i].y(), a.normals[i].z()).endVertex();
         consumer.vertex(matrix4f, a.vertices[next].x, a.vertices[next].y, a.vertices[next].z).color(color).uv(a.uvs[next].x, vA).overlayCoords(overlay).uv2(light).normal(matrix3f, a.normals[next].x(), a.normals[next].y(), a.normals[next].z()).endVertex();
         consumer.vertex(matrix4f, b.vertices[next].x, b.vertices[next].y, b.vertices[next].z).color(color).uv(b.uvs[next].x, vB).overlayCoords(overlay).uv2(light).normal(matrix3f, b.normals[next].x(), b.normals[next].y(), b.normals[next].z()).endVertex();
         consumer.vertex(matrix4f, b.vertices[i].x, b.vertices[i].y, b.vertices[i].z).color(color).uv(b.uvs[i].x, vB).overlayCoords(overlay).uv2(light).normal(matrix3f, b.normals[i].x(), b.normals[i].y(), b.normals[i].z()).endVertex();
      }

   }

   private static float calculateSize(int segmentIndex, int totalSegments, float inflation) {
      float progress = Mth.clamp((float)segmentIndex / (float)(totalSegments - 1), 0.0F, 1.0F);
      float startSize = 0.5F;
      float endSize = 3.0F;
      return (startSize + (endSize - startSize) * progress + 0.3F * Mth.sin(progress * (float)Math.PI)) * inflation;
   }

   public static class Ring {
      Vector3f[] vertices = new Vector3f[8];
      Vector3f[] normals = new Vector3f[8];
      Vector2f[] uvs = new Vector2f[8];
   }
}
