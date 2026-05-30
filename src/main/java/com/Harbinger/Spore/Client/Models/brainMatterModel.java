package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Client.Special.BlockEntityModel;
import com.Harbinger.Spore.SBlockEntities.BrainRemnantBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class brainMatterModel extends BlockEntityModel<BrainRemnantBlockEntity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "brainmattermodel"), "main");
   private final ModelPart lobes;
   private final ModelPart lobes2;
   private final ModelPart lobes3;
   private final ModelPart lobes4;
   private final ModelPart lobes5;
   private final ModelPart lobes6;
   private final ModelPart lobes7;
   private final ModelPart lobes8;

   public brainMatterModel() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.lobes = root.getChild("lobes");
      this.lobes2 = root.getChild("lobes2");
      this.lobes3 = root.getChild("lobes3");
      this.lobes4 = root.getChild("lobes4");
      this.lobes5 = root.getChild("lobes5");
      this.lobes6 = root.getChild("lobes6");
      this.lobes7 = root.getChild("lobes7");
      this.lobes8 = root.getChild("lobes8");
   }

   public void setupAnim(BrainRemnantBlockEntity entity, float ageInTicks) {
      this.lobes.yScale = 1.0F + Mth.cos(ageInTicks / 9.0F) / 11.0F;
      this.lobes.xScale = 1.0F - Mth.cos(ageInTicks / 9.0F) / 11.0F;
      this.lobes.zScale = 1.0F + Mth.cos(ageInTicks / 9.0F) / 11.0F;
      this.lobes2.yScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes2.xScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes2.zScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes3.yScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes3.xScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes3.zScale = 1.0F - Mth.cos(ageInTicks / 11.0F) / 9.0F;
      this.lobes4.yScale = 1.0F + Mth.cos(ageInTicks / 11.0F) / 8.0F;
      this.lobes4.xScale = 1.0F - Mth.cos(ageInTicks / 11.0F) / 8.0F;
      this.lobes4.zScale = 1.0F + Mth.cos(ageInTicks / 11.0F) / 8.0F;
      this.lobes5.yScale = 1.0F + Mth.cos(ageInTicks / 9.0F) / 11.0F;
      this.lobes5.xScale = 1.0F + Mth.cos(ageInTicks / 9.0F) / 11.0F;
      this.lobes5.zScale = 1.0F - Mth.cos(ageInTicks / 9.0F) / 11.0F;
      this.lobes6.yScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes6.xScale = 1.0F - Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes6.zScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes7.yScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes7.xScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 9.0F;
      this.lobes7.zScale = 1.0F - Mth.cos(ageInTicks / 11.0F) / 9.0F;
      this.lobes8.yScale = 1.0F + Mth.cos(ageInTicks / 11.0F) / 8.0F;
      this.lobes8.xScale = 1.0F - Mth.cos(ageInTicks / 11.0F) / 8.0F;
      this.lobes8.zScale = 1.0F + Mth.cos(ageInTicks / 11.0F) / 8.0F;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("lobes", CubeListBuilder.create().texOffs(48, 0).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      partdefinition.addOrReplaceChild("lobes2", CubeListBuilder.create().texOffs(0, 48).addBox(0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      partdefinition.addOrReplaceChild("lobes3", CubeListBuilder.create().texOffs(24, 40).addBox(-8.0F, 0.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      partdefinition.addOrReplaceChild("lobes4", CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, 0.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      partdefinition.addOrReplaceChild("lobes5", CubeListBuilder.create().texOffs(24, 24).addBox(0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      partdefinition.addOrReplaceChild("lobes6", CubeListBuilder.create().texOffs(24, 8).addBox(-8.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      partdefinition.addOrReplaceChild("lobes7", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, -8.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      partdefinition.addOrReplaceChild("lobes8", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.lobes.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.lobes2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.lobes3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.lobes4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.lobes5.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.lobes6.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.lobes7.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.lobes8.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
