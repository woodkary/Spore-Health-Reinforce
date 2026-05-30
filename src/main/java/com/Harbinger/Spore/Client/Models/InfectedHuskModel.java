package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.BasicInfected.InfectedHusk;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
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

public class InfectedHuskModel extends EntityModel<InfectedHusk> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "infectedhuskmodel"), "main");
   private final ModelPart body;
   private int i;

   public InfectedHuskModel(ModelPart root) {
      this.body = root.getChild("body");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(22, 21).addBox(-4.0F, 8.0F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 2.0F, 2.0F));
      PartDefinition spine = body.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -4.5F, -4.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.1309F, 0.0F, 0.0F));
      spine.addOrReplaceChild("back", CubeListBuilder.create().texOffs(10, 43).addBox(0.0F, -4.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(1.0F, 0.0F, -2.0F, 0.0F, 0.2618F, 0.0F));
      spine.addOrReplaceChild("back2", CubeListBuilder.create().texOffs(42, 10).addBox(-2.0F, -4.0F, -2.25F, 3.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.0F, 0.0F, -2.0F, 0.0F, -0.2618F, 0.0F));
      PartDefinition spine2 = body.addOrReplaceChild("spine2", CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, -5.5F, -4.5F, 8.0F, 6.0F, 5.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 4.0F, 1.25F, 0.2618F, 0.0F, 0.0F));
      spine2.addOrReplaceChild("back3", CubeListBuilder.create().texOffs(38, 29).addBox(0.0F, -4.0F, -1.75F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -1.0F, -2.0F, 0.0F, 0.2618F, 0.0F));
      spine2.addOrReplaceChild("back4", CubeListBuilder.create().texOffs(32, 6).addBox(-2.0F, -4.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, -2.0F, 0.0F, -0.2618F, 0.0F));
      body.addOrReplaceChild("tendril", CubeListBuilder.create().texOffs(48, 48).addBox(0.0F, -5.0F, -2.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 4.0F, 2.0F));
      body.addOrReplaceChild("tendril2", CubeListBuilder.create().texOffs(48, 48).addBox(0.0F, -5.0F, -2.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 2.0F, 1.0F));
      PartDefinition LeftLeg = body.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(0, 35).addBox(-1.25F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 10.0F, 0.0F));
      LeftLeg.addOrReplaceChild("leftForLeg", CubeListBuilder.create().texOffs(14, 33).addBox(-1.25F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition RightLeg = body.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(38, 39).addBox(-1.75F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 10.0F, 0.0F));
      RightLeg.addOrReplaceChild("rightForLeg", CubeListBuilder.create().texOffs(28, 33).addBox(-1.75F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LeftArm = body.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(0, 48).addBox(-1.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 2.0F, -2.0F));
      PartDefinition LeftForArm = LeftArm.addOrReplaceChild("LeftForArm", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
      LeftForArm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 43).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition RightArm = body.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 2.0F, -2.0F));
      RightArm.addOrReplaceChild("RightForArm", CubeListBuilder.create().texOffs(46, 18).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(24, 0).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(21, 14).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -3.0F));
      head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(18, 27).addBox(-3.5F, 0.0F, -3.75F, 7.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void setupAnim(InfectedHusk entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.isAggressive()) {
         this.body.getChild("head").getChild("jaw").xRot = Mth.sin(ageInTicks / 6.0F) / 10.0F + 0.4F;
         this.body.xRot = 0.2F;
         this.body.getChild("RightArm").xRot = -90.0F - Mth.sin(ageInTicks / 4.0F) / 7.0F;
         this.body.getChild("LeftArm").xRot = -90.0F + Mth.sin(ageInTicks / 4.0F) / 7.0F;
         this.body.zRot = Mth.cos(limbSwing / 2.0F) / 10.0F;
      } else if (limbSwingAmount > -0.15F && limbSwingAmount < 0.15F) {
         this.body.zRot = 0.0F;
         this.body.xRot = 0.0F;
         this.body.getChild("RightArm").zRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.body.getChild("LeftArm").zRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
      } else {
         this.body.getChild("RightArm").xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.body.getChild("LeftArm").xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
         this.body.getChild("RightArm").zRot = 0.0F;
         this.body.getChild("LeftArm").zRot = 0.0F;
      }

      this.body.getChild("LeftLeg").xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
      this.body.getChild("RightLeg").xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
      if (this.body.getChild("LeftLeg").xRot < 0.0F) {
         this.body.getChild("LeftLeg").getChild("leftForLeg").xRot = -this.body.getChild("LeftLeg").xRot;
      }

      if (this.body.getChild("RightLeg").xRot < 0.0F) {
         this.body.getChild("RightLeg").getChild("rightForLeg").xRot = -this.body.getChild("RightLeg").xRot;
      }

      this.body.getChild("head").yRot = netHeadYaw / (180F / (float)Math.PI);
      this.body.getChild("head").xRot = headPitch / 28.647888F;
      this.body.getChild("head").getChild("jaw").xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
      this.body.getChild("tendril").xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
      this.body.getChild("tendril2").xRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
      this.body.getChild("tendril").zRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
      this.body.getChild("tendril2").zRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
