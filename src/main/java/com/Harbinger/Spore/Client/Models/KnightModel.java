package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Knight;
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

public class KnightModel extends EntityModel<Knight> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "knightmodel"), "main");
   private final ModelPart body;

   public KnightModel(ModelPart root) {
      this.body = root.getChild("body");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 30).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 34).addBox(-3.5F, 4.5F, -2.0F, 7.0F, 6.0F, 4.0F, new CubeDeformation(-0.3F)).texOffs(22, 39).addBox(-3.5F, 10.0F, -2.0F, 7.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 63).addBox(3.0F, -3.0F, -3.0F, 2.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 63).addBox(5.0F, -7.0F, -3.0F, 2.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(17, 57).addBox(3.0F, 9.0F, -2.5F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(27, 55).addBox(-2.0F, -0.25F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(19, 66).addBox(-2.0F, 7.75F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(-6, 81).addBox(1.0F, -15.0F, -5.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(-6, 81).addBox(4.0F, -20.0F, -5.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -0.2182F));
      body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(-6, 81).addBox(-3.0F, -25.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.2182F, 0.0F, 0.2182F));
      body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(-6, 81).addBox(-2.0F, -21.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(-6, 81).addBox(-6.0F, -18.0F, -4.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(-6, 81).addBox(-12.0F, -28.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(-6, 81).addBox(-12.0F, -30.0F, -9.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, -0.2182F, 0.0F, 0.2182F));
      PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(24, 0).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(32, 22).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));
      head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(40, 41).addBox(-3.5F, 0.0F, -3.75F, 7.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));
      head.addOrReplaceChild("mushroom2", CubeListBuilder.create().texOffs(0, 3).addBox(0.0F, -4.0F, -6.0F, 0.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(0, 3).addBox(-2.0F, -2.0F, -6.0F, 0.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(0, 22).addBox(-0.5F, -1.0F, -3.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -8.0F, 0.0F, 0.0F, 0.48F, 0.0F));
      PartDefinition RightArm = body.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(0, 54).addBox(-2.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition RightForArm = RightArm.addOrReplaceChild("RightForArm", CubeListBuilder.create().texOffs(52, 27).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      RightForArm.addOrReplaceChild("fingers", CubeListBuilder.create().texOffs(12, 55).addBox(-2.0F, -1.0F, 1.0F, 4.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(12, 55).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 6.0F, 0.0F));
      RightForArm.addOrReplaceChild("tumb", CubeListBuilder.create().texOffs(58, -3).addBox(0.0F, -1.0F, -3.0F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, -1.0F));
      PartDefinition RightLeg = body.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(42, 47).addBox(-1.75F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
      RightLeg.addOrReplaceChild("rightForLeg", CubeListBuilder.create().texOffs(14, 45).addBox(-1.75F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LeftLeg = body.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(28, 45).addBox(-1.25F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
      LeftLeg.addOrReplaceChild("leftForLeg", CubeListBuilder.create().texOffs(0, 44).addBox(-1.25F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition mushroom = body.addOrReplaceChild("mushroom", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, 2.0F));
      PartDefinition tendril = body.addOrReplaceChild("tendril", CubeListBuilder.create(), PartPose.offset(5.0F, 6.0F, 2.0F));
      PartDefinition tendril2 = body.addOrReplaceChild("tendril2", CubeListBuilder.create(), PartPose.offset(5.0F, 6.0F, -2.0F));
      PartDefinition tendril3 = body.addOrReplaceChild("tendril3", CubeListBuilder.create(), PartPose.offset(7.0F, 0.0F, -1.0F));
      PartDefinition tendril4 = body.addOrReplaceChild("tendril4", CubeListBuilder.create(), PartPose.offset(7.0F, -3.0F, 1.0F));
      PartDefinition flower = body.addOrReplaceChild("flower", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, -5.0F, -1.0F, 0.3054F, 0.0F, 0.4363F));
      flower.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(10, 75).addBox(-6.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(10, 75).addBox(0.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(10, 75).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(10, 75).addBox(-4.0F, 0.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 128);
   }

   public void setupAnim(Knight entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.isAggressive()) {
         this.body.getChild("head").getChild("jaw").xRot = Mth.sin(ageInTicks / 6.0F) / 10.0F;
         this.body.getChild("RightArm").xRot = 25.0F + Mth.cos(limbSwing * 0.2F) * limbSwingAmount;
         this.body.getChild("RightArm").getChild("RightForArm").xRot = -88.5F;
         this.body.zRot = Mth.cos(limbSwing / 4.0F) / 10.0F;
         if (entity.swinging) {
            float f = 0.0F;
            ++f;
            this.body.getChild("RightArm").xRot = -90.0F + f;
            this.body.getChild("RightArm").getChild("RightForArm").xRot = 0.0F;
         }

         if (entity.attackAnim > 0.0F) {
            float f1 = 1.0F - Mth.abs(10.0F - 2.0F * entity.attackAnim) / 10.0F;
            this.body.getChild("RightArm").xRot = Mth.sin(f1) * 2.0F;
         }
      } else if (limbSwingAmount > -0.15F && limbSwingAmount < 0.15F) {
         this.body.zRot = 0.0F;
         this.body.getChild("RightArm").zRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.body.getChild("RightArm").getChild("RightForArm").getChild("fingers").zRot = Mth.sin(ageInTicks / 4.0F) / 8.0F;
         this.body.getChild("RightArm").getChild("RightForArm").getChild("tumb").xRot = -Mth.sin(ageInTicks / 4.0F) / 8.0F;
         this.body.getChild("RightArm").getChild("RightForArm").xRot = 0.0F;
      } else {
         this.body.getChild("RightArm").xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.body.getChild("RightArm").zRot = 0.0F;
      }

      this.body.getChild("LeftLeg").xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
      this.body.getChild("RightLeg").xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
      if (this.body.getChild("LeftLeg").xRot < 0.0F) {
         this.body.getChild("LeftLeg").getChild("leftForLeg").xRot = -2.0F * this.body.getChild("LeftLeg").xRot;
      }

      if (this.body.getChild("RightLeg").xRot < 0.0F) {
         this.body.getChild("RightLeg").getChild("rightForLeg").xRot = -2.0F * this.body.getChild("RightLeg").xRot;
      }

      this.body.getChild("head").getChild("jaw").xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
