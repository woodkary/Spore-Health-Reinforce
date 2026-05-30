package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
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

public class ScamperModel extends EntityModel<Scamper> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "scampermodel"), "main");
   private final ModelPart body;
   private final ModelPart LeftLeg;
   private final ModelPart RightLeg;
   private final ModelPart LeftArm;
   private final ModelPart RightArm;
   private final ModelPart head;
   private final ModelPart mound1;
   private final ModelPart mound2;
   private final ModelPart mound3;

   public ScamperModel(ModelPart root) {
      this.body = root.getChild("body");
      this.LeftLeg = root.getChild("LeftLeg");
      this.RightLeg = root.getChild("RightLeg");
      this.LeftArm = root.getChild("LeftArm");
      this.RightArm = root.getChild("RightArm");
      this.head = root.getChild("head");
      this.mound1 = root.getChild("mound1");
      this.mound2 = root.getChild("mound2");
      this.mound3 = root.getChild("mound3");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 42).addBox(-4.0F, 8.0F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)).texOffs(31, 12).addBox(-1.0F, 4.0F, 0.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 2.0F));
      body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(57, 61).addBox(-4.0F, -16.0F, -1.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.0F, -2.0F, -0.1309F, 0.0F, 0.0F));
      body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(32, 13).addBox(2.0F, -17.0F, 1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.0F, -2.0F, 0.0F, 0.2618F, 0.0F));
      body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(31, 13).addBox(-2.0F, -23.0F, 6.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.0F, -2.0F, 0.2233F, -0.2129F, -0.0479F));
      PartDefinition spine = body.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(42, 20).addBox(-2.0F, -4.5F, -4.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.1309F, 0.0F, 0.0F));
      spine.addOrReplaceChild("back", CubeListBuilder.create().texOffs(62, 8).addBox(0.0F, -4.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(1.0F, 0.0F, -2.0F, 0.0F, 0.2618F, 0.0F));
      spine.addOrReplaceChild("back2", CubeListBuilder.create().texOffs(62, 0).addBox(-1.0F, -4.0F, -2.25F, 2.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.0F, 0.0F, -2.0F, 0.0F, -0.2618F, 0.0F));
      PartDefinition spine2 = body.addOrReplaceChild("spine2", CubeListBuilder.create().texOffs(0, 23).addBox(-4.0F, -5.5F, -4.5F, 8.0F, 6.0F, 5.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, 4.0F, 1.25F, 0.2618F, 0.0F, 0.0F));
      spine2.addOrReplaceChild("back3", CubeListBuilder.create().texOffs(58, 16).addBox(0.0F, -4.0F, -1.75F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -1.0F, -2.0F, 0.0F, 0.2618F, 0.0F));
      spine2.addOrReplaceChild("back4", CubeListBuilder.create().texOffs(48, 12).addBox(-2.0F, -4.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, -2.0F, 0.0F, -0.2618F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(12, 53).addBox(-1.25F, 0.0F, -1.75F, 3.0F, 6.0F, 3.5F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 2.0F));
      LeftLeg.addOrReplaceChild("leftForLeg", CubeListBuilder.create().texOffs(48, 49).addBox(-1.25F, 0.0F, -1.75F, 3.0F, 6.0F, 3.5F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(26, 53).addBox(-1.75F, 0.0F, -1.75F, 3.0F, 6.0F, 3.5F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 2.0F));
      RightLeg.addOrReplaceChild("rightForLeg", CubeListBuilder.create().texOffs(52, 39).addBox(-1.75F, 0.0F, -1.75F, 3.0F, 6.0F, 3.5F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(52, 59).addBox(-1.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 4.0F, 0.0F));
      PartDefinition LeftForArm = LeftArm.addOrReplaceChild("LeftForArm", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
      LeftForArm.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 57).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(60, 24).addBox(-2.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 4.0F, 0.0F));
      RightArm.addOrReplaceChild("RightForArm", CubeListBuilder.create().texOffs(40, 59).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(42, 6).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(74, 1).addBox(-4.0F, -1.92F, -4.0F, 8.0F, 4.0F, 5.0F, new CubeDeformation(0.04F)).texOffs(23, -1).addBox(-4.25F, -3.0F, -4.25F, 8.5F, 1.0F, 4.25F, new CubeDeformation(0.04F)), PartPose.offset(0.0F, 4.5F, -2.5F));
      head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(57, 61).addBox(-4.0F, -26.0F, -1.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.5F, 2.5F, 0.0F, 0.0F, -0.1309F));
      head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(58, 61).addBox(-4.0F, -24.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.5F, 2.5F, -0.0873F, 0.0F, 0.1309F));
      head.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(31, 12).addBox(-1.0F, -26.0F, 2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.5F, 2.5F, 0.0436F, 0.0F, 0.0F));
      PartDefinition flower2 = head.addOrReplaceChild("flower2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, -6.5F, -0.5F, 0.238F, -0.3148F, -0.0403F));
      flower2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(35, 68).addBox(-4.0406F, 0.0F, 0.0F, 7.0711F, 0.0F, 6.061F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(35, 68).addBox(0.0F, 0.0F, -3.0305F, 6.061F, 0.0F, 6.061F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5051F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(35, 68).addBox(-4.0406F, 0.0F, -6.061F, 7.0711F, 0.0F, 6.061F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(35, 68).addBox(-6.061F, 0.0F, -3.0305F, 6.061F, 0.0F, 6.061F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5051F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      PartDefinition flower = head.addOrReplaceChild("flower", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, -6.0F, 0.5F, 0.6655F, -0.2266F, 0.3196F));
      flower.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(56, 61).addBox(-5.52F, 0.0F, 0.0F, 9.66F, 0.0F, 8.28F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(56, 61).addBox(0.0F, 0.0F, -4.14F, 8.28F, 0.0F, 8.28F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.69F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(56, 61).addBox(-5.52F, 0.0F, -8.28F, 9.66F, 0.0F, 8.28F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(56, 61).addBox(-8.28F, 0.0F, -4.14F, 8.28F, 0.0F, 8.28F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.69F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      PartDefinition flower3 = head.addOrReplaceChild("flower3", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5F, -6.5F, 0.5F, -0.9553F, -0.2449F, 0.2404F));
      flower3.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(35, 68).addBox(-4.0406F, 0.0F, 0.0F, 7.0711F, 0.0F, 6.061F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower3.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(35, 68).addBox(0.0F, 0.0F, -3.0305F, 6.061F, 0.0F, 6.061F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5051F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower3.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(35, 68).addBox(-4.0406F, 0.0F, -6.061F, 7.0711F, 0.0F, 6.061F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower3.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(35, 68).addBox(-6.061F, 0.0F, -3.0305F, 6.061F, 0.0F, 6.061F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5051F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      PartDefinition mound1 = partdefinition.addOrReplaceChild("mound1", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 6.0F, 2.0F, -0.3491F, 0.0F, 0.3491F));
      PartDefinition body2 = mound1.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 74).addBox(-2.0F, -6.0F, -15.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 66).addBox(-1.5F, -7.0F, -14.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 13.0F));
      body2.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(12, 80).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -13.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition mound2 = partdefinition.addOrReplaceChild("mound2", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 9.0F, 2.0F, -0.4363F, 0.0F, -0.48F));
      PartDefinition body3 = mound2.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(0, 74).addBox(-2.0F, -6.0F, -15.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 66).addBox(-1.5F, -7.0F, -14.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 13.0F));
      body3.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(12, 80).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -13.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition mound3 = partdefinition.addOrReplaceChild("mound3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 10.25F, 2.0F, 0.9599F, 0.48F, 0.0F));
      PartDefinition body4 = mound3.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(0, 74).addBox(-2.0F, -6.0F, -15.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 66).addBox(-1.5F, -7.0F, -14.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 13.0F));
      body4.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(12, 80).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -13.0F, 0.0F, -0.7854F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void animateMound(ModelPart part, float value) {
      part.xScale = 1.0F + value;
      part.zScale = 1.0F + value;
      part.yScale = 1.0F - value;
   }

   public void setupAnim(Scamper entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.isAggressive()) {
         if (entity.swinging) {
            this.RightArm.xRot = -89.5F;
            this.LeftArm.xRot = -89.5F;
         } else {
            this.RightArm.xRot = -90.0F - Mth.sin(ageInTicks / 4.0F) / 7.0F;
            this.LeftArm.xRot = -90.0F + Mth.sin(ageInTicks / 4.0F) / 7.0F;
         }
      } else if (limbSwingAmount > -0.05F && limbSwingAmount < 0.15F) {
         this.RightArm.xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.LeftArm.xRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
      } else {
         this.RightArm.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.LeftArm.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
         this.RightArm.zRot = 0.0F;
         this.LeftArm.zRot = 0.0F;
      }

      this.head.yRot = netHeadYaw / (180F / (float)Math.PI);
      this.head.xRot = headPitch / 28.647888F;
      this.LeftLeg.xRot = Mth.cos(limbSwing) * limbSwingAmount;
      this.RightLeg.xRot = Mth.cos(limbSwing) * -limbSwingAmount;
      if (this.LeftLeg.xRot < 0.0F) {
         this.LeftLeg.getChild("leftForLeg").xRot = -this.LeftLeg.xRot;
      }

      if (this.RightLeg.xRot < 0.0F) {
         this.RightLeg.getChild("rightForLeg").xRot = -this.RightLeg.xRot;
      }

      this.animateMound(this.mound1, Mth.sin(ageInTicks / 8.0F) / 9.0F);
      this.animateMound(this.mound2, Mth.sin(ageInTicks / 8.0F) / 8.0F);
      this.animateMound(this.mound3, Mth.sin(ageInTicks / 8.0F) / 7.0F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.RightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.RightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.mound1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.mound2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.mound3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
