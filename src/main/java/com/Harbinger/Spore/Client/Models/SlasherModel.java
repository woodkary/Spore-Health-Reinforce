package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Slasher;
import com.Harbinger.Spore.Sentities.Variants.SlasherVariants;
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

public class SlasherModel extends EntityModel<Slasher> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "slashermodel"), "main");
   private final ModelPart body;
   private final ModelPart bodywear;
   private final ModelPart LeftArm;
   private final ModelPart RightLeg;
   private final ModelPart LeftLeg;
   private final ModelPart HeadJoint;
   private final ModelPart Marm;

   public SlasherModel(ModelPart root) {
      this.body = root.getChild("body");
      this.bodywear = root.getChild("bodywear");
      this.LeftArm = root.getChild("LeftArm");
      this.RightLeg = root.getChild("RightLeg");
      this.LeftLeg = root.getChild("LeftLeg");
      this.HeadJoint = root.getChild("HeadJoint");
      this.Marm = root.getChild("Marm");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(36, 21).addBox(-4.0F, 9.0F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(59, 41).addBox(0.5F, -3.75F, -2.0F, 4.0F, 3.75F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 75).addBox(-3.0F, -9.0F, -5.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(-7, 87).addBox(-4.0F, -32.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.2618F));
      body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(-7, 87).addBox(-1.0F, -33.0F, -12.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
      body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(-7, 87).addBox(-3.0F, -29.0F, -6.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, -0.1731F, 0.0227F, 0.1289F));
      body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(-7, 87).addBox(-2.0F, -22.0F, 7.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.6109F, 0.0F, -0.1309F));
      body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(-7, 87).addBox(-5.0F, -24.0F, -1.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -0.1309F));
      body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(18, 66).addBox(-16.0F, -32.0F, -4.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.4363F));
      body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(28, 47).addBox(-1.5F, 6.5F, -3.25F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(32, 0).addBox(-4.5F, -1.0F, -4.0F, 8.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -2.0F, 0.0F, 0.0F, 0.0F, 0.7854F));
      body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(36, 12).addBox(-4.0F, -2.5F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.0436F, 0.0F, 0.0F));
      body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(0, 34).addBox(-6.0F, -11.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.1F)).texOffs(28, 34).addBox(-4.0F, -7.0F, -3.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, -0.25F, 0.0436F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("bodywear", CubeListBuilder.create().texOffs(75, 0).addBox(-4.0F, 0.0F, -3.25F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.25F, 0.0436F, 0.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(62, 50).addBox(-1.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition LeftForArm = LeftArm.addOrReplaceChild("LeftForArm", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
      LeftForArm.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(61, 18).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(32, 56).addBox(-1.75F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
      RightLeg.addOrReplaceChild("rightForLeg", CubeListBuilder.create().texOffs(56, 30).addBox(-1.75F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(16, 55).addBox(-1.25F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
      LeftLeg.addOrReplaceChild("leftForLeg", CubeListBuilder.create().texOffs(0, 55).addBox(-1.25F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition HeadJoint = partdefinition.addOrReplaceChild("HeadJoint", CubeListBuilder.create(), PartPose.offset(3.0F, -3.0F, 0.0F));
      PartDefinition head = HeadJoint.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -2.0F, -4.5F, 7.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-4.0F, -10.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(55, 0).addBox(-4.0F, -2.0F, 1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(24, 1).addBox(-3.5F, -2.0F, -4.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(24, 0).addBox(3.5F, -2.0F, -4.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -0.5F, 0.1733F, -0.0298F, 0.3438F));
      head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -1.0F));
      head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 47).addBox(-4.0F, -1.0F, -6.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(-0.05F)), PartPose.offset(0.0F, -1.0F, 1.0F));
      PartDefinition flower2 = HeadJoint.addOrReplaceChild("flower2", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -6.5F, -4.25F, 0.7039F, -0.1949F, -0.1078F));
      flower2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(-8, 87).addBox(0.0F, 0.0F, -3.84F, 7.68F, 0.0F, 7.68F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(-8, 87).addBox(-7.68F, 0.0F, -3.84F, 7.68F, 0.0F, 7.68F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(-8, 87).addBox(-5.12F, 0.0F, -7.68F, 8.96F, 0.0F, 7.68F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.64F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(-8, 87).addBox(-5.12F, 0.0F, 0.0F, 8.96F, 0.0F, 7.68F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.64F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      PartDefinition flower = HeadJoint.addOrReplaceChild("flower", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -9.0F, -2.0F, 0.162F, 0.1468F, -0.7298F));
      flower.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(-6, 87).addBox(-6.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(-6, 87).addBox(-4.0F, 0.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(-6, 87).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      PartDefinition Marm = partdefinition.addOrReplaceChild("Marm", CubeListBuilder.create(), PartPose.offset(-6.0F, -4.0F, -2.0F));
      PartDefinition MarmJoint = Marm.addOrReplaceChild("MarmJoint", CubeListBuilder.create().texOffs(58, 8).addBox(-2.0F, -3.0F, -1.75F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(52, 47).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -1.0F, 0.7854F, 0.0F, 0.0F));
      PartDefinition MArm2 = MarmJoint.addOrReplaceChild("MArm2", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, 1.0F));
      PartDefinition MarmJoint2 = MArm2.addOrReplaceChild("MarmJoint2", CubeListBuilder.create().texOffs(19, 19).addBox(-1.0F, -1.0F, -13.0F, 2.0F, 2.0F, 13.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.0F));
      MarmJoint2.addOrReplaceChild("claw", CubeListBuilder.create().texOffs(0, 0).addBox(0.1F, -9.0F, -16.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -13.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(Slasher entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.attackAnim > 0.0F) {
         float f1 = -1.0F + Mth.abs(10.0F - 2.0F * entity.attackAnim) / 6.5F;
         if (entity.getVariant() == SlasherVariants.PIERCER) {
            f1 = -1.0F + Mth.abs(10.0F - 2.0F * entity.attackAnim) / 6.5F;
         }

         this.Marm.xRot = Mth.sin(f1) * 2.0F;
         this.Marm.getChild("MarmJoint").getChild("MArm2").xRot = -Mth.sin(f1) * 3.0F;
      } else if (limbSwingAmount > -0.15F && limbSwingAmount < 0.15F) {
         this.Marm.xRot = Mth.sin(ageInTicks / 6.0F) / 8.0F;
         this.Marm.getChild("MarmJoint").getChild("MArm2").xRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.Marm.getChild("MarmJoint").getChild("MArm2").getChild("MarmJoint2").getChild("claw").xRot = -Mth.sin(ageInTicks / 6.0F) / 8.0F;
         this.LeftArm.zRot = -Mth.sin(ageInTicks / 6.0F) / 8.0F;
      } else {
         this.LeftArm.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
         this.LeftArm.zRot = 0.0F;
         this.Marm.xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.Marm.getChild("MarmJoint").getChild("MArm2").xRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.Marm.getChild("MarmJoint").getChild("MArm2").getChild("MarmJoint2").getChild("claw").xRot = -Mth.sin(ageInTicks / 6.0F) / 8.0F;
      }

      this.LeftLeg.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
      this.RightLeg.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
      if (this.LeftLeg.xRot < 0.0F) {
         this.LeftLeg.getChild("leftForLeg").xRot = -this.LeftLeg.xRot;
      }

      if (this.RightLeg.xRot < 0.0F) {
         this.RightLeg.getChild("rightForLeg").xRot = -this.RightLeg.xRot;
      }

      this.HeadJoint.getChild("head").getChild("jaw").xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.bodywear.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.RightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.HeadJoint.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.Marm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
