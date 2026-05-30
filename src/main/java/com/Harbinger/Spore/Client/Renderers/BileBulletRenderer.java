package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Sentities.Projectile.GunProjectiles.BileBullet;
import com.Harbinger.Spore.Sentities.Projectile.GunProjectiles.GoreBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BileBulletRenderer extends EntityRenderer<BileBullet> {
   public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("spore", "textures/entity/bile_round.png");
   private final BileRound model = new BileRound();

   public BileBulletRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public void render(BileBullet spear, float p_116112_, float partial, PoseStack stack, MultiBufferSource bufferSource, int light) {
      stack.pushPose();
      stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial, spear.yRotO, spear.getYRot()) - 90.0F));
      stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial, spear.xRotO, spear.getXRot()) + 90.0F));
      int i = spear.getMutationVariant().getColor();
      float r = (float)(i >> 16 & 255) / 255.0F;
      float g = (float)(i >> 8 & 255) / 255.0F;
      float b = (float)(i & 255) / 255.0F;
      VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(spear)));
      this.model.renderToBuffer(stack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
      stack.popPose();
      super.render(spear, p_116112_, partial, stack, bufferSource, light);
   }

   public ResourceLocation getTextureLocation(BileBullet p_116109_) {
      return TEXTURE_LOCATION;
   }

   public static class BileRound extends EntityModel<GoreBullet> {
      public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "bileround"), "main");
      private final ModelPart BileRound;

      public BileRound() {
         ModelPart root = createBodyLayer().bakeRoot();
         this.BileRound = root.getChild("BileRound");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition meshdefinition = new MeshDefinition();
         PartDefinition partdefinition = meshdefinition.getRoot();
         PartDefinition BileRound = partdefinition.addOrReplaceChild("BileRound", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8429F, -0.6857F, -1.2429F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 4).addBox(-1.1429F, -1.1857F, -1.0429F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 8).addBox(-0.6429F, 0.1143F, -0.5429F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.1571F, 22.6857F, -0.1571F));
         BileRound.addOrReplaceChild("bile_drip_r1", CubeListBuilder.create().texOffs(0, 11).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1429F, -0.1857F, -1.2429F, -1.5708F, -1.2741F, 1.5708F));
         BileRound.addOrReplaceChild("bile_drip_r2", CubeListBuilder.create().texOffs(8, 8).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1429F, -0.1857F, 1.1571F, 1.5708F, -1.3439F, -1.5708F));
         BileRound.addOrReplaceChild("bile_drip_r3", CubeListBuilder.create().texOffs(8, 4).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1571F, -0.1857F, -0.0429F, 0.0F, 0.0F, -0.3491F));
         BileRound.addOrReplaceChild("bile_drip_r4", CubeListBuilder.create().texOffs(8, 0).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2429F, -0.1857F, -0.0429F, 0.0F, 0.0F, 0.1222F));
         return LayerDefinition.create(meshdefinition, 16, 16);
      }

      public void setupAnim(GoreBullet entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
         this.BileRound.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      }
   }
}
