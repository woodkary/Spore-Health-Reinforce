package com.Harbinger.Spore.Client.Models;

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
import net.minecraft.world.entity.LivingEntity;

public class fleshArmorModel extends EntityModel<LivingEntity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "flesharmormodel"), "main");
   public final ModelPart headwear;
   public final ModelPart tendril;
   public final ModelPart body;
   public final ModelPart heart;
   public final ModelPart rightLung;
   public final ModelPart leftLung;
   public final ModelPart rightArm;
   public final ModelPart leftArm;
   public final ModelPart rightLeg;
   public final ModelPart leftLeg;
   public final ModelPart leftBoot;
   public final ModelPart rightBoot;
   public final ModelPart pelvis;

   public fleshArmorModel() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.headwear = root.getChild("headwear");
      this.tendril = this.headwear.getChild("tendril");
      this.body = root.getChild("body");
      this.heart = this.body.getChild("heart");
      this.rightLung = this.body.getChild("rightLung");
      this.leftLung = this.body.getChild("leftLung");
      this.rightArm = root.getChild("rightArm");
      this.leftArm = root.getChild("leftArm");
      this.rightLeg = root.getChild("rightLeg");
      this.leftLeg = root.getChild("leftLeg");
      this.leftBoot = root.getChild("leftBoot");
      this.rightBoot = root.getChild("rightBoot");
      this.pelvis = root.getChild("pelvis");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition headwear = partdefinition.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      headwear.addOrReplaceChild("brain_r1", CubeListBuilder.create().texOffs(50, 0).addBox(-3.0F, -4.0F, 0.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, 3.0F, -0.3927F, 0.0F, 0.0F));
      PartDefinition tendril = headwear.addOrReplaceChild("tendril", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, 7.0F));
      tendril.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(50, 36).addBox(1.0F, -1.9281F, -1.611F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.6F, 0.0F, 0.3927F, 0.4363F, 0.0F));
      tendril.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(50, 36).addBox(1.0F, -1.9281F, -1.611F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.6F, -1.0F, 0.3927F, -0.4363F, 0.0F));
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(1.01F)).texOffs(0, 31).addBox(0.0F, -1.0F, 2.75F, 0.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(32, 20).mirror().addBox(-1.0F, -2.0F, 0.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.25F, 1.0F, 3.0F, 0.0F, 0.0F, 0.6109F));
      body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(32, 20).addBox(-2.0F, -2.0F, 0.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.25F, 1.0F, 3.0F, 0.0F, 0.0F, -0.6109F));
      PartDefinition heart = body.addOrReplaceChild("heart", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1F, 3.8F, -2.05F, -0.0263F, -0.0832F, 0.3065F));
      heart.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(18, 60).addBox(-0.8381F, -0.5587F, -0.95F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.0752F, -0.8348F, 0.0F, 0.0F, 0.0F, 1.1781F));
      heart.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(57, 20).addBox(0.8381F, -0.9413F, -1.05F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-1.2273F, -0.5898F, -0.35F, 0.0F, 0.0F, 1.1781F));
      heart.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(36, 56).addBox(-2.1619F, -2.9413F, -1.05F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)), PartPose.offsetAndRotation(-0.8252F, 0.4152F, -1.0F, 0.0F, 0.0F, 1.1781F));
      body.addOrReplaceChild("rightLung", CubeListBuilder.create().texOffs(6, 58).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.01F)).texOffs(12, 59).addBox(-1.5F, 0.5F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 3.5F, -3.0F, 0.0F, 0.1745F, 0.1745F));
      body.addOrReplaceChild("leftLung", CubeListBuilder.create().texOffs(12, 59).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.01F)).texOffs(6, 58).addBox(-0.5F, 0.5F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.15F, 3.5F, -3.0F, 0.0F, -0.1745F, -0.1745F));
      PartDefinition rightArm = partdefinition.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
      rightArm.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(60, 55).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.0F, -2.5F, 0.0F, 3.1416F, 0.0F));
      rightArm.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(60, 55).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.0F, 0.5F, 0.0F, 3.1416F, 0.0F));
      partdefinition.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(16, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)).texOffs(60, 55).addBox(3.0F, -4.0F, -1.5F, 2.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(60, 55).addBox(3.0F, -4.0F, 1.5F, 2.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));
      partdefinition.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(32, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.5F)).texOffs(24, 25).addBox(-0.2F, 1.0F, -3.25F, 0.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.5F)).texOffs(24, 25).addBox(0.0F, 1.0F, -3.25F, 0.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("leftBoot", CubeListBuilder.create().texOffs(32, 43).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("rightBoot", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.01F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 26).addBox(-4.0F, -2.0F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 12.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   private void animateHeart(float age) {
      float val = Mth.sin(age / 6.0F) / 6.0F;
      this.heart.xScale = 1.0F + val;
      this.heart.zScale = 1.0F + val;
      this.heart.yScale = 1.0F - val;
   }

   private void animateLung(ModelPart part, float age) {
      float val = Mth.sin(age / 8.0F) / 4.0F;
      part.xScale = 1.0F + val;
      part.yScale = 1.0F - val;
   }

   public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.animateHeart(ageInTicks);
      this.animateLung(this.leftLung, ageInTicks);
      this.animateLung(this.rightLung, ageInTicks);
      this.tendril.xRot = Mth.cos(ageInTicks / 6.0F) / 6.0F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.leftBoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.rightBoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.pelvis.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
