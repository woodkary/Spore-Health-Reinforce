package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.BasicInfected.InfectedWitch;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
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
import net.minecraft.world.entity.HumanoidArm;

public class InfectedWitchModel extends EntityModel<InfectedWitch> implements ArmedModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "infectedwitchmodel"), "main");
   private final ModelPart body;
   private final ModelPart LeftLeg;
   private final ModelPart RightLeg;
   private final ModelPart TopBody;
   private final ModelPart RightArm;
   private final ModelPart LeftArm;

   public InfectedWitchModel(ModelPart root) {
      this.body = root.getChild("body");
      this.LeftLeg = root.getChild("LeftLeg");
      this.RightLeg = root.getChild("RightLeg");
      this.TopBody = root.getChild("TopBody");
      this.RightArm = root.getChild("RightArm");
      this.LeftArm = root.getChild("LeftArm");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(27, 37).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(68, 0).addBox(-4.0F, 4.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(0, 54).addBox(-1.25F, 0.0F, -4.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 2.0F));
      LeftLeg.addOrReplaceChild("leftForLeg", CubeListBuilder.create().texOffs(52, 44).addBox(-1.25F, 0.0F, -4.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(38, 48).addBox(-1.75F, 0.0F, -4.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 2.0F));
      RightLeg.addOrReplaceChild("rightForLeg", CubeListBuilder.create().texOffs(24, 48).addBox(-1.75F, 0.0F, -4.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition TopBody = partdefinition.addOrReplaceChild("TopBody", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 10.0F, 6.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition head = TopBody.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 26).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(48, 12).addBox(-4.0F, -2.25F, -3.75F, 8.0F, 1.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, -6.0F, -2.0F));
      head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 3).addBox(0.0F, 1.0F, -6.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, -2.0F, 0.0F));
      head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 42).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offset(0.0F, -2.0F, 0.0F));
      PartDefinition headwear = head.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(18, 14).addBox(0.0F, 0.0F, 0.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -10.05F, -5.0F));
      PartDefinition hat2 = headwear.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(32, 26).addBox(0.0F, 0.0F, 0.0F, 7.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.0524F, 0.0F, 0.0262F));
      PartDefinition hat3 = hat2.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(54, 22).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.1047F, 0.0F, 0.0524F));
      hat3.addOrReplaceChild("hat4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(1.75F, -2.0F, 2.0F, -0.2094F, 0.0F, 0.1047F));
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(14, 55).addBox(-2.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 3.0F, 0.0F));
      RightArm.addOrReplaceChild("RightForArm", CubeListBuilder.create().texOffs(52, 54).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-1.0F, 4.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(57, 34).addBox(-1.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 3.0F, 0.0F));
      PartDefinition LeftForArm = LeftArm.addOrReplaceChild("LeftForArm", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
      LeftForArm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition item = LeftForArm.addOrReplaceChild("item", CubeListBuilder.create(), PartPose.offset(0.0F, 5.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(InfectedWitch entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.swinging) {
         this.RightArm.xRot = -90.0F;
         this.LeftArm.xRot = -90.0F;
      } else if (limbSwingAmount > -0.15F && limbSwingAmount < 0.15F) {
         this.RightArm.zRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.LeftArm.zRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
      } else {
         this.RightArm.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.LeftArm.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
         this.RightArm.zRot = 0.0F;
         this.LeftArm.zRot = 0.0F;
      }

      this.LeftLeg.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
      this.RightLeg.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
      if (this.LeftLeg.xRot < 0.0F) {
         this.LeftLeg.getChild("leftForLeg").xRot = -this.LeftLeg.xRot;
      }

      if (this.RightLeg.xRot < 0.0F) {
         this.RightLeg.getChild("rightForLeg").xRot = -this.RightLeg.xRot;
      }

      this.TopBody.getChild("head").yRot = netHeadYaw / (180F / (float)Math.PI);
      this.TopBody.getChild("head").xRot = headPitch / 28.647888F;
      this.TopBody.getChild("head").getChild("jaw").xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.RightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.TopBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.RightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   private ModelPart getArm(HumanoidArm arm) {
      return arm == HumanoidArm.RIGHT ? this.LeftArm : this.RightArm;
   }

   public void translateToHand(HumanoidArm arm, PoseStack stack) {
      this.getArm(arm).translateAndRotate(stack);
   }
}
