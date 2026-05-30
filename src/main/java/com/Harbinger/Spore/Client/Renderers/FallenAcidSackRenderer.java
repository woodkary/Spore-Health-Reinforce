package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Sentities.Projectile.FallenAcidSack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallenAcidSackRenderer extends EntityRenderer<FallenAcidSack> {
   private static final ItemStack itemStack;
   private static final ResourceLocation TEXTURE;

   public FallenAcidSackRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public void render(FallenAcidSack entity, float value2, float value, PoseStack stack, MultiBufferSource source, int p_116116_) {
      ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
      Level level = entity.level();
      BlockPos pos = entity.blockPosition();
      stack.pushPose();
      stack.scale(1.5F, 1.5F, 1.5F);
      stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(value, entity.yRotO, entity.getYRot()) - 90.0F));
      stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(value, entity.xRotO, entity.getXRot()) + 90.0F));
      itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, this.getLight(level, pos), OverlayTexture.NO_OVERLAY, stack, source, level, 1);
      stack.popPose();
      super.render(entity, value2, value, stack, source, p_116116_);
   }

   public ResourceLocation getTextureLocation(FallenAcidSack t) {
      return TEXTURE;
   }

   private int getLight(Level level, BlockPos pos) {
      int a = level.getBrightness(LightLayer.BLOCK, pos);
      int b = level.getBrightness(LightLayer.SKY, pos);
      return LightTexture.pack(a, b);
   }

   static {
      itemStack = new ItemStack((ItemLike)Sitems.ACIDIC_SACK.get());
      TEXTURE = new ResourceLocation("spore:textures/entity/empty.png");
   }
}
