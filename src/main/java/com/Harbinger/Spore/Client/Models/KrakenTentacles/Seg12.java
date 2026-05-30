package com.Harbinger.Spore.Client.Models.KrakenTentacles;

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
import net.minecraft.world.entity.Entity;

public class Seg12 extends EntityModel<Entity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "seg12"), "main");
   private final ModelPart BaseSegment;
   private final ModelPart CalcifiedCorpses;
   private final ModelPart CalcifiedCorpse1;
   private final ModelPart CalcifiedCorpse2;
   private final ModelPart CalcifiedThorns;
   private final ModelPart CalcifiedThorn1;
   private final ModelPart CalcifiedThorn2;
   private final ModelPart CalcifiedThorn3;
   private final ModelPart CalcifiedThorn4;
   private final ModelPart CalcifiedThorn5;
   private final ModelPart CalcifiedThorn6;
   private final ModelPart CalcifiedThorn7;

   public Seg12() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.BaseSegment = root.getChild("BaseSegment");
      this.CalcifiedCorpses = root.getChild("CalcifiedCorpses");
      this.CalcifiedCorpse1 = this.CalcifiedCorpses.getChild("CalcifiedCorpse1");
      this.CalcifiedCorpse2 = this.CalcifiedCorpses.getChild("CalcifiedCorpse2");
      this.CalcifiedThorns = root.getChild("CalcifiedThorns");
      this.CalcifiedThorn1 = this.CalcifiedThorns.getChild("CalcifiedThorn1");
      this.CalcifiedThorn2 = this.CalcifiedThorns.getChild("CalcifiedThorn2");
      this.CalcifiedThorn3 = this.CalcifiedThorns.getChild("CalcifiedThorn3");
      this.CalcifiedThorn4 = this.CalcifiedThorns.getChild("CalcifiedThorn4");
      this.CalcifiedThorn5 = this.CalcifiedThorns.getChild("CalcifiedThorn5");
      this.CalcifiedThorn6 = this.CalcifiedThorns.getChild("CalcifiedThorn6");
      this.CalcifiedThorn7 = this.CalcifiedThorns.getChild("CalcifiedThorn7");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("BaseSegment", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -16.0F, -4.5F, 9.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition CalcifiedCorpses = partdefinition.addOrReplaceChild("CalcifiedCorpses", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -2.0944F, 0.0F));
      PartDefinition CalcifiedCorpse1 = CalcifiedCorpses.addOrReplaceChild("CalcifiedCorpse1", CubeListBuilder.create(), PartPose.offsetAndRotation(22.0F, -8.2857F, -1.0714F, -1.0468F, -0.0378F, -1.4617F));
      CalcifiedCorpse1.addOrReplaceChild("Leg_r1", CubeListBuilder.create().texOffs(40, 53).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -5.7143F, -18.2286F, -2.4342F, 0.6419F, -0.6109F));
      CalcifiedCorpse1.addOrReplaceChild("Leg_r2", CubeListBuilder.create().texOffs(40, 53).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.7143F, -18.9286F, -2.1827F, -0.0991F, 0.3538F));
      CalcifiedCorpse1.addOrReplaceChild("Arm_r1", CubeListBuilder.create().texOffs(40, 51).addBox(-3.0F, -1.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -13.6143F, -11.2286F, -0.4451F, -0.4345F, 0.6254F));
      CalcifiedCorpse1.addOrReplaceChild("Arm_r2", CubeListBuilder.create().texOffs(40, 51).addBox(0.0F, -1.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -12.7143F, -13.9286F, -2.2153F, -0.28F, -0.3932F));
      CalcifiedCorpse1.addOrReplaceChild("TorsoBase_r1", CubeListBuilder.create().texOffs(44, 51).addBox(-1.5F, 0.0F, -3.5F, 3.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.7143F, -13.9286F, -1.5708F, 0.5672F, -1.5708F));
      CalcifiedCorpse1.addOrReplaceChild("TorsoTop_r1", CubeListBuilder.create().texOffs(32, 46).addBox(-2.0F, -3.5F, -4.0F, 4.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -12.2143F, -13.9286F, 0.0F, 1.5708F, 0.0F));
      CalcifiedCorpse1.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(32, 49).addBox(-3.0F, -6.5F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -11.9143F, -18.2286F, -1.5037F, 0.9647F, -1.4632F));
      PartDefinition CalcifiedCorpse2 = CalcifiedCorpses.addOrReplaceChild("CalcifiedCorpse2", CubeListBuilder.create().texOffs(40, 51).addBox(3.3F, -5.6143F, -17.5286F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, -8.2857F, -0.0714F, 1.5708F, -0.7418F, -1.5708F));
      CalcifiedCorpse2.addOrReplaceChild("Leg_r3", CubeListBuilder.create().texOffs(40, 53).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4F, 5.6857F, -15.9286F, -1.3168F, 0.7765F, -2.0048F));
      CalcifiedCorpse2.addOrReplaceChild("Arm_r3", CubeListBuilder.create().texOffs(40, 51).addBox(-3.0F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6F, -5.1143F, -17.2286F, -1.7516F, 0.4252F, -0.6813F));
      CalcifiedCorpse2.addOrReplaceChild("TorsoBase_r2", CubeListBuilder.create().texOffs(44, 51).addBox(-1.5F, 0.0F, -3.5F, 3.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -0.6143F, -16.4286F, -2.7301F, 1.4787F, -2.3198F));
      CalcifiedCorpse2.addOrReplaceChild("TorsoTop_r2", CubeListBuilder.create().texOffs(32, 46).addBox(-2.0F, -3.5F, -4.0F, 4.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -3.1143F, -16.6286F, 1.3788F, 1.4835F, 1.5708F));
      PartDefinition CalcifiedThorns = partdefinition.addOrReplaceChild("CalcifiedThorns", CubeListBuilder.create(), PartPose.offset(21.7516F, 23.5056F, 16.1064F));
      PartDefinition CalcifiedThorn1 = CalcifiedThorns.addOrReplaceChild("CalcifiedThorn1", CubeListBuilder.create().texOffs(33, 55).addBox(-0.9423F, -0.3335F, -0.9959F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.8F, -8.3F, -15.0F, 3.0916F, -0.0049F, 0.7809F));
      CalcifiedThorn1.addOrReplaceChild("Plane2_r1", CubeListBuilder.create().texOffs(62, 44).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3423F, 6.6665F, -0.2959F, 0.0F, 0.7854F, 0.0F));
      CalcifiedThorn1.addOrReplaceChild("Plane1_r1", CubeListBuilder.create().texOffs(62, 50).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3577F, 6.6665F, -0.2959F, 0.0F, -0.7854F, 0.0F));
      PartDefinition CalcifiedThorn2 = CalcifiedThorns.addOrReplaceChild("CalcifiedThorn2", CubeListBuilder.create().texOffs(33, 55).addBox(-0.9423F, -0.3335F, -0.9958F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.8F, -8.3F, -19.0F, -2.6522F, -0.1746F, 1.0876F));
      CalcifiedThorn2.addOrReplaceChild("Plane3_r1", CubeListBuilder.create().texOffs(62, 48).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3423F, 6.6665F, -0.2958F, 0.0F, 0.7854F, 0.0F));
      CalcifiedThorn2.addOrReplaceChild("Plane2_r2", CubeListBuilder.create().texOffs(62, 51).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3577F, 6.6665F, -0.2958F, 0.0F, -0.7854F, 0.0F));
      PartDefinition CalcifiedThorn3 = CalcifiedThorns.addOrReplaceChild("CalcifiedThorn3", CubeListBuilder.create().texOffs(33, 55).addBox(-0.9423F, -0.3335F, -0.9958F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.5F, -5.3F, -12.0F, 2.3032F, -0.3745F, 0.9965F));
      CalcifiedThorn3.addOrReplaceChild("Plane4_r1", CubeListBuilder.create().texOffs(62, 50).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3423F, 6.6665F, -0.2958F, 0.0F, 0.7854F, 0.0F));
      CalcifiedThorn3.addOrReplaceChild("Plane3_r2", CubeListBuilder.create().texOffs(62, 51).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3577F, 6.6665F, -0.2958F, 0.0F, -0.7854F, 0.0F));
      PartDefinition CalcifiedThorn4 = CalcifiedThorns.addOrReplaceChild("CalcifiedThorn4", CubeListBuilder.create().texOffs(33, 55).addBox(-0.9423F, -0.3335F, -0.9958F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-24.5F, -9.3F, -12.0F, 1.6189F, -0.6927F, 1.2291F));
      CalcifiedThorn4.addOrReplaceChild("Plane5_r1", CubeListBuilder.create().texOffs(62, 51).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3423F, 6.6665F, -0.2958F, 0.0F, 0.7854F, 0.0F));
      CalcifiedThorn4.addOrReplaceChild("Plane4_r2", CubeListBuilder.create().texOffs(62, 52).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3577F, 6.6665F, -0.2958F, 0.0F, -0.7854F, 0.0F));
      PartDefinition CalcifiedThorn5 = CalcifiedThorns.addOrReplaceChild("CalcifiedThorn5", CubeListBuilder.create().texOffs(33, 55).addBox(-0.9423F, -0.3335F, -0.9959F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-28.0F, -7.8F, -16.7F, -0.2242F, 0.3277F, 2.3346F));
      CalcifiedThorn5.addOrReplaceChild("Plane6_r1", CubeListBuilder.create().texOffs(62, 44).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3423F, 6.6665F, -0.2958F, 0.0F, 0.7854F, 0.0F));
      CalcifiedThorn5.addOrReplaceChild("Plane5_r2", CubeListBuilder.create().texOffs(62, 51).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3577F, 6.6665F, -0.2958F, 0.0F, -0.7854F, 0.0F));
      PartDefinition CalcifiedThorn6 = CalcifiedThorns.addOrReplaceChild("CalcifiedThorn6", CubeListBuilder.create().texOffs(33, 55).addBox(-0.9423F, -0.3335F, -0.9958F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-24.0F, -10.8F, -21.7F, -0.6272F, 0.6108F, 2.0274F));
      CalcifiedThorn6.addOrReplaceChild("Plane7_r1", CubeListBuilder.create().texOffs(62, 53).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3423F, 6.6665F, -0.2958F, 0.0F, 0.7854F, 0.0F));
      CalcifiedThorn6.addOrReplaceChild("Plane6_r2", CubeListBuilder.create().texOffs(62, 57).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3577F, 6.6665F, -0.2958F, 0.0F, -0.7854F, 0.0F));
      PartDefinition CalcifiedThorn7 = CalcifiedThorns.addOrReplaceChild("CalcifiedThorn7", CubeListBuilder.create().texOffs(33, 55).addBox(-0.9423F, -0.3335F, -0.9958F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.3F, -3.1F, -20.8F, -1.4255F, 0.8767F, 1.5413F));
      CalcifiedThorn7.addOrReplaceChild("Plane8_r1", CubeListBuilder.create().texOffs(62, 45).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3423F, 6.6665F, -0.2958F, 0.0F, 0.7854F, 0.0F));
      CalcifiedThorn7.addOrReplaceChild("Plane7_r2", CubeListBuilder.create().texOffs(62, 51).addBox(0.0F, -2.0F, -0.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3577F, 6.6665F, -0.2958F, 0.0F, -0.7854F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
      this.BaseSegment.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.CalcifiedCorpses.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.CalcifiedThorns.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
   }
}
