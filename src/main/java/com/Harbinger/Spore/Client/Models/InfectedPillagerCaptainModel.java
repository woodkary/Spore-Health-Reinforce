package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPillager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
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
import net.minecraft.world.entity.HumanoidArm;

public class InfectedPillagerCaptainModel extends InfectedPillagerModel implements ArmedModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "infectedpillagercaptainmodel"), "main");
   private final ModelPart head;
   private final ModelPart nose;
   private final ModelPart jaw;
   private final ModelPart Banner;
   private final ModelPart body;
   private final ModelPart bodywear;
   private final ModelPart RightArm;
   private final ModelPart RightForArm;
   private final ModelPart LeftArm;
   private final ModelPart LeftForArm;
   private final ModelPart RightLeg;
   private final ModelPart rightForLeg;
   private final ModelPart LeftLeg;
   private final ModelPart leftForLeg;

   public InfectedPillagerCaptainModel(ModelPart root) {
      super(root);
      this.head = root.getChild("head");
      this.nose = this.head.getChild("nose");
      this.jaw = this.head.getChild("jaw");
      this.Banner = this.head.getChild("Banner");
      this.body = root.getChild("body");
      this.bodywear = root.getChild("bodywear");
      this.RightArm = root.getChild("RightArm");
      this.RightForArm = this.RightArm.getChild("RightForArm");
      this.LeftArm = root.getChild("LeftArm");
      this.LeftForArm = this.LeftArm.getChild("LeftForArm");
      this.RightLeg = root.getChild("RightLeg");
      this.rightForLeg = this.RightLeg.getChild("rightForLeg");
      this.LeftLeg = root.getChild("LeftLeg");
      this.leftForLeg = this.LeftLeg.getChild("leftForLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 13).addBox(-3.5F, -2.0F, -4.5F, 7.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(20, 16).addBox(-4.0F, -10.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 24).addBox(-4.0F, -2.0F, 1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 23).addBox(-3.5F, -2.0F, -4.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(52, 0).addBox(3.5F, -2.0F, -4.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(22, 32).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -1.0F));
      head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 41).addBox(-4.0F, -1.0F, -6.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(-0.05F)), PartPose.offset(0.0F, -1.0F, 1.0F));
      PartDefinition Banner = head.addOrReplaceChild("Banner", CubeListBuilder.create().texOffs(120, 84).addBox(-1.0F, -35.0F, -1.0F, 2.0F, 35.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offset(0.0F, -6.3F, 3.5F));
      Banner.addOrReplaceChild("Fabric_r1", CubeListBuilder.create().texOffs(0, 64).addBox(-20.0F, -10.0F, -0.5F, 40.0F, 20.0F, 1.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, -16.4F, -0.8F, 0.0F, 0.0F, 1.5708F));
      Banner.addOrReplaceChild("PoleTop_r1", CubeListBuilder.create().texOffs(0, 106).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 20.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(-10.0F, -35.4F, 0.0F, 0.0F, 0.0F, 1.5708F));
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 32).addBox(-4.0F, 9.0F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -2.5F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.0436F, 0.0F, 0.0F));
      body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0F, -7.0F, -3.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, -0.25F, 0.0436F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("bodywear", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -3.25F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.25F, 0.0436F, 0.0F, 0.0F));
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(40, 52).addBox(-2.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
      RightArm.addOrReplaceChild("RightForArm", CubeListBuilder.create().texOffs(28, 52).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(50, 29).addBox(-1.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition LeftForArm = LeftArm.addOrReplaceChild("LeftForArm", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
      LeftForArm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 49).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(0, 49).addBox(-1.75F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
      RightLeg.addOrReplaceChild("rightForLeg", CubeListBuilder.create().texOffs(44, 41).addBox(-1.75F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(44, 13).addBox(-1.25F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
      LeftLeg.addOrReplaceChild("leftForLeg", CubeListBuilder.create().texOffs(28, 41).addBox(-1.25F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(InfectedPillager entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.isChargingCrossbow()) {
         this.RightArm.xRot = -89.5F + headPitch / 28.647888F;
         this.LeftArm.xRot = -89.5F + headPitch / 28.647888F;
         this.LeftArm.yRot = 88.5F;
      } else if (entity.swinging && !entity.isChargingCrossbow()) {
         this.RightArm.xRot = -89.5F + headPitch / 28.647888F;
         this.LeftArm.xRot = -89.5F + headPitch / 28.647888F;
      } else if ((!(limbSwingAmount > -0.15F) || !(limbSwingAmount < 0.15F)) && !entity.isChargingCrossbow()) {
         this.RightArm.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.LeftArm.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
         this.RightArm.zRot = 0.0F;
         this.LeftArm.zRot = 0.0F;
      } else {
         this.RightArm.zRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.LeftArm.zRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.LeftArm.yRot = 0.0F;
      }

      this.LeftLeg.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
      this.RightLeg.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
      if (this.LeftLeg.xRot < 0.0F) {
         this.LeftLeg.getChild("leftForLeg").xRot = -this.LeftLeg.xRot;
      }

      if (this.RightLeg.xRot < 0.0F) {
         this.RightLeg.getChild("rightForLeg").xRot = -this.RightLeg.xRot;
      }

      this.head.yRot = netHeadYaw / (180F / (float)Math.PI);
      this.head.xRot = headPitch / 28.647888F;
      this.head.getChild("jaw").xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.bodywear.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.RightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.RightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   private ModelPart getArm(HumanoidArm p_102923_) {
      return p_102923_ == HumanoidArm.LEFT ? this.LeftArm : this.RightArm;
   }

   public void translateToHand(HumanoidArm arm, PoseStack stack) {
      this.getArm(arm).translateAndRotate(stack);
   }
}
