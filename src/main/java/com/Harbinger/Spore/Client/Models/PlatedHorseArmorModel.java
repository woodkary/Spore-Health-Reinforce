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
import net.minecraft.world.entity.LivingEntity;

public class PlatedHorseArmorModel extends EntityModel<LivingEntity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "platedhorsearmormodel"), "main");
   public final ModelPart Body;
   public final ModelPart Neck;
   private final ModelPart Head;
   public final ModelPart BackLeftLeg;
   public final ModelPart BackRightLeg;
   public final ModelPart FrontLeftLeg;
   public final ModelPart FrontRightLeg;

   public PlatedHorseArmorModel() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.Body = root.getChild("Body");
      this.Neck = root.getChild("Neck");
      this.Head = this.Neck.getChild("Head");
      this.BackLeftLeg = root.getChild("BackLeftLeg");
      this.BackRightLeg = root.getChild("BackRightLeg");
      this.FrontLeftLeg = root.getChild("FrontLeftLeg");
      this.FrontRightLeg = root.getChild("FrontRightLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -20.0F, 10.0F, 10.0F, 22.0F, new CubeDeformation(0.0F)).texOffs(0, 48).addBox(5.0F, -6.0F, -13.0F, 3.0F, 0.0F, 15.0F, new CubeDeformation(0.0F)).texOffs(36, 32).addBox(5.0F, -7.0F, -14.0F, 2.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(36, 32).addBox(5.0F, -5.0F, -14.0F, 2.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(36, 48).addBox(-8.0F, -6.0F, -13.0F, 3.0F, 0.0F, 15.0F, new CubeDeformation(0.0F)).texOffs(0, 32).addBox(-7.0F, -7.0F, -14.0F, 2.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(0, 32).addBox(-7.0F, -5.0F, -14.0F, 2.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, 9.0F));
      PartDefinition Neck = partdefinition.addOrReplaceChild("Neck", CubeListBuilder.create().texOffs(64, 0).addBox(-2.0F, -11.0F, -3.0F, 4.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(92, 17).addBox(-3.0F, -11.0F, 2.0F, 3.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(90, 17).addBox(0.0F, -11.0F, 2.0F, 3.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, -8.0F));
      PartDefinition Head = Neck.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(64, 19).addBox(-3.0F, -5.0F, -6.0F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(44, 15).addBox(-2.0F, -6.0F, -6.0F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(86, 0).addBox(-1.0F, -7.0F, -6.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(86, 0).addBox(1.0F, -7.0F, -6.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(42, -5).addBox(0.0F, -8.0F, -6.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(46, 3).addBox(-2.0F, -5.0F, -11.0F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 3.0F));
      Head.addOrReplaceChild("UMouth_r1", CubeListBuilder.create().texOffs(0, -9).addBox(0.0F, -9.0038F, -7.9128F, 0.0F, 11.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -10.0F, -0.6981F, 0.0F, 0.0F));
      PartDefinition BackLeftLeg = partdefinition.addOrReplaceChild("BackLeftLeg", CubeListBuilder.create().texOffs(26, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 15).addBox(-2.0F, 2.0F, -3.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(11, 16).addBox(-2.0F, 2.0F, 2.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(0.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(-1.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(1.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 13.0F, 9.0F));
      BackLeftLeg.addOrReplaceChild("Leg1A_r1", CubeListBuilder.create().texOffs(10, 6).addBox(0.0F, -1.0F, -6.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 9.0F, -1.0F, 0.0873F, -0.1309F, -0.0087F));
      BackLeftLeg.addOrReplaceChild("Leg1A_r2", CubeListBuilder.create().texOffs(10, 6).addBox(0.0F, -1.0F, -7.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, 0.0873F, 0.1309F, -0.0087F));
      PartDefinition BackRightLeg = partdefinition.addOrReplaceChild("BackRightLeg", CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, 2.0F, -3.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(11, 16).addBox(-2.0F, 2.0F, 2.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(0.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(-1.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(1.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(26, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 13.0F, 9.0F));
      BackRightLeg.addOrReplaceChild("Leg2A_r1", CubeListBuilder.create().texOffs(10, 6).addBox(0.0F, -1.0F, -6.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 9.0F, -1.0F, 0.0873F, -0.1309F, 0.0087F));
      BackRightLeg.addOrReplaceChild("Leg2A_r2", CubeListBuilder.create().texOffs(10, 6).addBox(0.0F, -1.0F, -6.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, -1.0F, 0.0873F, 0.1309F, -0.0087F));
      PartDefinition FrontLeftLeg = partdefinition.addOrReplaceChild("FrontLeftLeg", CubeListBuilder.create().texOffs(26, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 15).addBox(-2.0F, 2.0F, -3.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(11, 16).addBox(-2.0F, 2.0F, 2.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(0.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(-1.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(1.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 13.0F, -9.0F));
      FrontLeftLeg.addOrReplaceChild("Leg3A_r1", CubeListBuilder.create().texOffs(10, 6).addBox(0.0F, -1.0F, -6.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 9.0F, -1.0F, 0.0873F, -0.1309F, -0.0087F));
      FrontLeftLeg.addOrReplaceChild("Leg3A_r2", CubeListBuilder.create().texOffs(10, 6).addBox(0.0F, -1.0F, -6.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, -1.0F, 0.0873F, 0.1309F, -0.0087F));
      PartDefinition FrontRightLeg = partdefinition.addOrReplaceChild("FrontRightLeg", CubeListBuilder.create().texOffs(26, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 15).addBox(-2.0F, 2.0F, -3.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(11, 16).addBox(-2.0F, 2.0F, 2.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(0.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(-1.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 50).addBox(1.0F, 2.0F, -4.0F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 13.0F, -9.0F));
      FrontRightLeg.addOrReplaceChild("Leg4A_r1", CubeListBuilder.create().texOffs(10, 6).addBox(0.0F, -1.0F, -5.9F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 9.0F, -1.0F, 0.0873F, -0.1309F, -0.0087F));
      FrontRightLeg.addOrReplaceChild("Leg4A_r2", CubeListBuilder.create().texOffs(10, 6).addBox(0.0F, -1.0F, -5.9F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, -1.0F, 0.0873F, 0.1309F, -0.0087F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
      this.Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.Neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.BackLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.BackRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.FrontLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.FrontRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
   }
}
