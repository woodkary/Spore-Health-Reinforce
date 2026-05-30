package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BileRound;
import com.Harbinger.Spore.Client.Models.JollyPresentBombs;
import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.time.LocalDate;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FleshBombRenderer extends EntityRenderer<FleshBomb> {
   public static final ResourceLocation BASIC_ROUND = new ResourceLocation("spore", "textures/entity/basicround.png");
   public static final ResourceLocation FLAMMABLE_ROUND = new ResourceLocation("spore", "textures/entity/fireround.png");
   public static final ResourceLocation BILE_ROUND = new ResourceLocation("spore", "textures/entity/bileround.png");
   public static final ResourceLocation ACID_ROUND = new ResourceLocation("spore", "textures/entity/acidround.png");
   public static final ResourceLocation NUCLEAR_ROUND = new ResourceLocation("spore", "textures/entity/nuclear_round.png");
   public static final ResourceLocation BASIC_JOLLY_ROUND = new ResourceLocation("spore", "textures/entity/jolly_howitzer_round.png");
   public static final ResourceLocation FLAMMABLE_JOLLY_ROUND = new ResourceLocation("spore", "textures/entity/jolly_fire_howitzer_round.png");
   public static final ResourceLocation BILE_JOLLY_ROUND = new ResourceLocation("spore", "textures/entity/jolly_bile_howitzer_round.png");
   public static final ResourceLocation ACID_JOLLY_ROUND = new ResourceLocation("spore", "textures/entity/jolly_acid_howitzer_round.png");
   public static final ResourceLocation NUCLEAR_JOLLY_ROUND = new ResourceLocation("spore", "textures/entity/jolly_nuke_howitzer_round.png");
   protected final LocalDate localdate = LocalDate.now();
   protected final int j;
   private final BileRound model;
   private final JollyPresentBombs presentBombs;

   public FleshBombRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.j = this.localdate.getMonth().getValue();
      this.model = new BileRound();
      this.presentBombs = new JollyPresentBombs();
   }

   public void render(FleshBomb entity, float value2, float value, PoseStack stack, MultiBufferSource source, int p_116116_) {
      EntityModel<FleshBomb> entityModel = (EntityModel<FleshBomb>)(this.isJollyTime() ? this.presentBombs : this.model);
      stack.pushPose();
      stack.mulPose(Axis.ZN.rotationDegrees(180.0F));
      stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(value, entity.yRotO, entity.getYRot()) - 90.0F));
      stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(value, entity.xRotO, entity.getXRot()) + 90.0F));
      int scaling = entity.getCarrier() ? 2 : 1;
      stack.scale((float)scaling, (float)scaling, (float)scaling);
      VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(source, this.model.renderType(this.getTextureLocation(entity)), false, false);
      entityModel.setupAnim(entity, 0.0F, 0.0F, (float)entity.tickCount, 0.0F, 0.0F);
      entityModel.renderToBuffer(stack, vertexconsumer, p_116116_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      stack.popPose();
      super.render(entity, value2, value, stack, source, p_116116_);
   }

   boolean isJollyTime() {
      return this.j == 12;
   }

   public ResourceLocation getTextureLocation(FleshBomb bileProjectile) {
      if (bileProjectile.getBombType() == 1) {
         return this.isJollyTime() ? FLAMMABLE_JOLLY_ROUND : FLAMMABLE_ROUND;
      } else if (bileProjectile.getBombType() == 2) {
         return this.isJollyTime() ? BILE_JOLLY_ROUND : BILE_ROUND;
      } else if (bileProjectile.getBombType() == 3) {
         return this.isJollyTime() ? ACID_JOLLY_ROUND : ACID_ROUND;
      } else if (bileProjectile.getBombType() == 4) {
         return this.isJollyTime() ? NUCLEAR_JOLLY_ROUND : NUCLEAR_ROUND;
      } else {
         return this.isJollyTime() ? BASIC_JOLLY_ROUND : BASIC_ROUND;
      }
   }
}
