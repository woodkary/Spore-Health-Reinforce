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

public class BrainTentacleModel extends BlockEntityModel<BrainRemnantBlockEntity> implements TentacledModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "braintentacles"), "main");
   private final ModelPart body;
   private final ModelPart Tendril1;
   private final ModelPart Seg2Tendril1;
   private final ModelPart Seg3Tendril1;
   private final ModelPart Tendril2;
   private final ModelPart Seg2Tendril2;
   private final ModelPart Seg3Tendril2;
   private final ModelPart Tendril4;
   private final ModelPart Seg2Tendril4;
   private final ModelPart Seg3Tendril4;
   private final ModelPart Tendril5;
   private final ModelPart Seg2Tendril5;
   private final ModelPart Seg3Tendril5;
   private final ModelPart Tendril6;
   private final ModelPart Seg2Tendril6;
   private final ModelPart Seg3Tendril6;
   private final ModelPart Tendril7;
   private final ModelPart Seg2Tendril7;
   private final ModelPart Seg3Tendril7;
   private final ModelPart Tendril8;
   private final ModelPart Seg2Tendril8;
   private final ModelPart Seg3Tendril8;
   private final ModelPart Tendril3;
   private final ModelPart Seg2Tendril3;
   private final ModelPart Seg3Tendril3;

   public BrainTentacleModel() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.body = root.getChild("body");
      this.Tendril1 = this.body.getChild("Tendril1");
      this.Seg2Tendril1 = this.Tendril1.getChild("Seg2Tendril1");
      this.Seg3Tendril1 = this.Seg2Tendril1.getChild("Seg3Tendril1");
      this.Tendril2 = this.body.getChild("Tendril2");
      this.Seg2Tendril2 = this.Tendril2.getChild("Seg2Tendril2");
      this.Seg3Tendril2 = this.Seg2Tendril2.getChild("Seg3Tendril2");
      this.Tendril4 = this.body.getChild("Tendril4");
      this.Seg2Tendril4 = this.Tendril4.getChild("Seg2Tendril4");
      this.Seg3Tendril4 = this.Seg2Tendril4.getChild("Seg3Tendril4");
      this.Tendril5 = this.body.getChild("Tendril5");
      this.Seg2Tendril5 = this.Tendril5.getChild("Seg2Tendril5");
      this.Seg3Tendril5 = this.Seg2Tendril5.getChild("Seg3Tendril5");
      this.Tendril6 = this.body.getChild("Tendril6");
      this.Seg2Tendril6 = this.Tendril6.getChild("Seg2Tendril6");
      this.Seg3Tendril6 = this.Seg2Tendril6.getChild("Seg3Tendril6");
      this.Tendril7 = this.body.getChild("Tendril7");
      this.Seg2Tendril7 = this.Tendril7.getChild("Seg2Tendril7");
      this.Seg3Tendril7 = this.Seg2Tendril7.getChild("Seg3Tendril7");
      this.Tendril8 = this.body.getChild("Tendril8");
      this.Seg2Tendril8 = this.Tendril8.getChild("Seg2Tendril8");
      this.Seg3Tendril8 = this.Seg2Tendril8.getChild("Seg3Tendril8");
      this.Tendril3 = this.body.getChild("Tendril3");
      this.Seg2Tendril3 = this.Tendril3.getChild("Seg2Tendril3");
      this.Seg3Tendril3 = this.Seg2Tendril3.getChild("Seg3Tendril3");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));
      PartDefinition Tendril1 = body.addOrReplaceChild("Tendril1", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3837F, -0.8057F, 0.2892F, -1.5708F, 0.0F, 0.0F));
      PartDefinition Seg2Tendril1 = Tendril1.addOrReplaceChild("Seg2Tendril1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0138F, -8.9634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      Seg2Tendril1.addOrReplaceChild("Seg3Tendril1", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0815F, -8.3197F, -0.0142F));
      PartDefinition Tendril2 = body.addOrReplaceChild("Tendril2", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3837F, -0.8057F, 0.2892F, 0.0F, -1.5708F, -1.5708F));
      PartDefinition Seg2Tendril2 = Tendril2.addOrReplaceChild("Seg2Tendril2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0138F, -8.9634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      Seg2Tendril2.addOrReplaceChild("Seg3Tendril2", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0815F, -8.3197F, -0.0142F));
      PartDefinition Tendril4 = body.addOrReplaceChild("Tendril4", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3837F, -0.8057F, 0.2892F, 1.5708F, 0.0F, 3.1416F));
      PartDefinition Seg2Tendril4 = Tendril4.addOrReplaceChild("Seg2Tendril4", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0138F, -8.9634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      Seg2Tendril4.addOrReplaceChild("Seg3Tendril4", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0815F, -8.3197F, -0.0142F));
      PartDefinition Tendril5 = body.addOrReplaceChild("Tendril5", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3837F, -0.8057F, 0.2892F, 1.5708F, -0.7854F, 3.1416F));
      PartDefinition Seg2Tendril5 = Tendril5.addOrReplaceChild("Seg2Tendril5", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0138F, -8.9634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      Seg2Tendril5.addOrReplaceChild("Seg3Tendril5", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0815F, -8.3197F, -0.0142F));
      PartDefinition Tendril6 = body.addOrReplaceChild("Tendril6", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3837F, -0.8057F, 0.2892F, 1.5708F, 0.7854F, 3.1416F));
      PartDefinition Seg2Tendril6 = Tendril6.addOrReplaceChild("Seg2Tendril6", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0138F, -8.9634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      Seg2Tendril6.addOrReplaceChild("Seg3Tendril6", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0815F, -8.3197F, -0.0142F));
      PartDefinition Tendril7 = body.addOrReplaceChild("Tendril7", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3837F, -0.8057F, 0.2892F, -1.5708F, -0.7854F, 0.0F));
      PartDefinition Seg2Tendril7 = Tendril7.addOrReplaceChild("Seg2Tendril7", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0138F, -8.9634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      Seg2Tendril7.addOrReplaceChild("Seg3Tendril7", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0815F, -8.3197F, -0.0142F));
      PartDefinition Tendril8 = body.addOrReplaceChild("Tendril8", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3837F, -0.8057F, 0.2892F, -1.5708F, 0.6981F, 0.0F));
      PartDefinition Seg2Tendril8 = Tendril8.addOrReplaceChild("Seg2Tendril8", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0138F, -8.9634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      Seg2Tendril8.addOrReplaceChild("Seg3Tendril8", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0815F, -8.3197F, -0.0142F));
      PartDefinition Tendril3 = body.addOrReplaceChild("Tendril3", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3837F, -0.8057F, 0.2892F, 0.0F, 1.5708F, 1.5708F));
      PartDefinition Seg2Tendril3 = Tendril3.addOrReplaceChild("Seg2Tendril3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0138F, -8.9634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      Seg2Tendril3.addOrReplaceChild("Seg3Tendril3", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0815F, -8.3197F, -0.0142F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   public void setupAnim(BrainRemnantBlockEntity entity, float ageInTicks) {
      this.animateTentacleX(this.Tendril1, Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleY(this.Tendril1, Mth.cos(ageInTicks / 2.0F));
      this.animateTentacleX(this.Seg2Tendril1, Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Seg3Tendril1, Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Tendril4, Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleY(this.Tendril4, Mth.sin(ageInTicks / 3.0F));
      this.animateTentacleX(this.Seg2Tendril4, Mth.cos(ageInTicks / 2.0F));
      this.animateTentacleX(this.Seg3Tendril4, Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Tendril2, -Mth.cos(ageInTicks / 4.0F));
      this.animateTentacleX(this.Seg2Tendril2, -Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Seg3Tendril2, -Mth.cos(ageInTicks / 2.0F));
      this.animateTentacleX(this.Tendril3, -Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Seg2Tendril3, -Mth.cos(ageInTicks / 2.0F));
      this.animateTentacleX(this.Seg3Tendril3, -Mth.cos(ageInTicks / 4.0F));
      this.animateTentacleX(this.Tendril5, Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleY(this.Tendril5, Mth.sin(ageInTicks / 2.0F));
      this.animateTentacleX(this.Seg2Tendril5, -Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Seg3Tendril5, Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Tendril6, -Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Seg2Tendril6, Mth.cos(ageInTicks / 2.0F));
      this.animateTentacleX(this.Seg3Tendril6, Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Tendril7, -Mth.cos(ageInTicks / 4.0F));
      this.animateTentacleY(this.Tendril7, -Mth.cos(ageInTicks / 4.0F));
      this.animateTentacleX(this.Seg2Tendril7, -Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Seg3Tendril7, Mth.cos(ageInTicks / 2.0F));
      this.animateTentacleX(this.Tendril8, -Mth.cos(ageInTicks / 3.0F));
      this.animateTentacleX(this.Seg2Tendril8, Mth.cos(ageInTicks / 2.0F));
      this.animateTentacleX(this.Seg3Tendril8, -Mth.cos(ageInTicks / 4.0F));
   }
}
