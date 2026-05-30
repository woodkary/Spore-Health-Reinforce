package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Client.Special.BlockEntityModel;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
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

public class CDUModelInfested extends BlockEntityModel<CDUBlockEntity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "cdumodelinfested"), "main");
   private final ModelPart cdu;
   private final ModelPart ventWall;
   private final ModelPart ventfan;
   private final ModelPart antenna;
   private final ModelPart canister;
   private final ModelPart CanisterPump;
   private final ModelPart pipe;
   private final ModelPart lesser;
   private final ModelPart AC;
   private final ModelPart Tendril1;
   private final ModelPart Seg2Tendril1;
   private final ModelPart Seg3Tendril1;
   private final ModelPart Tendril2;
   private final ModelPart Seg2Tendril2;
   private final ModelPart Seg3Tendril2;
   private final ModelPart Tendril3;
   private final ModelPart Seg2Tendril3;
   private final ModelPart Seg3Tendril3;
   private final ModelPart Tendril4;
   private final ModelPart Seg2Tendril4;
   private final ModelPart Seg3Tendril4;

   public CDUModelInfested() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.cdu = root.getChild("cdu");
      this.ventWall = this.cdu.getChild("ventWall");
      this.ventfan = this.ventWall.getChild("ventfan");
      this.antenna = this.ventWall.getChild("antenna");
      this.canister = this.cdu.getChild("canister");
      this.CanisterPump = this.canister.getChild("CanisterPump");
      this.pipe = this.canister.getChild("pipe");
      this.lesser = this.pipe.getChild("lesser");
      this.AC = this.cdu.getChild("AC");
      this.Tendril1 = this.cdu.getChild("Tendril1");
      this.Seg2Tendril1 = this.Tendril1.getChild("Seg2Tendril1");
      this.Seg3Tendril1 = this.Seg2Tendril1.getChild("Seg3Tendril1");
      this.Tendril2 = this.cdu.getChild("Tendril2");
      this.Seg2Tendril2 = this.Tendril2.getChild("Seg2Tendril2");
      this.Seg3Tendril2 = this.Seg2Tendril2.getChild("Seg3Tendril2");
      this.Tendril3 = this.cdu.getChild("Tendril3");
      this.Seg2Tendril3 = this.Tendril3.getChild("Seg2Tendril3");
      this.Seg3Tendril3 = this.Seg2Tendril3.getChild("Seg3Tendril3");
      this.Tendril4 = this.cdu.getChild("Tendril4");
      this.Seg2Tendril4 = this.Tendril4.getChild("Seg2Tendril4");
      this.Seg3Tendril4 = this.Seg2Tendril4.getChild("Seg3Tendril4");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition cdu = partdefinition.addOrReplaceChild("cdu", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 6.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
      cdu.addOrReplaceChild("bolt_r1", CubeListBuilder.create().texOffs(76, 31).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, 5.5F, -6.5F, 0.0F, 0.7854F, 0.0F));
      PartDefinition ventWall = cdu.addOrReplaceChild("ventWall", CubeListBuilder.create().texOffs(40, 34).addBox(-1.8056F, -6.5F, -6.5F, 2.0F, 13.0F, 13.0F, new CubeDeformation(0.0F)).texOffs(46, 18).addBox(0.1944F, -6.5F, -6.5F, 2.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)).texOffs(0, 53).addBox(0.1944F, 3.5F, -6.5F, 2.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)).texOffs(70, 39).addBox(0.1944F, -3.5F, -6.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(70, 49).addBox(0.1944F, -3.5F, 3.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.8056F, -0.5F, 1.5F));
      ventWall.addOrReplaceChild("bolt_r2", CubeListBuilder.create().texOffs(80, 47).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1944F, -7.0F, -5.0F, 0.0F, 0.7854F, 0.0F));
      ventWall.addOrReplaceChild("bolt_r3", CubeListBuilder.create().texOffs(80, 44).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1944F, -7.0F, 5.0F, 0.0F, 0.7854F, 0.0F));
      ventWall.addOrReplaceChild("ventfan", CubeListBuilder.create().texOffs(80, 14).addBox(-1.25F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 69).addBox(0.25F, -3.5F, -3.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(1.4444F, 0.0F, 0.0F));
      PartDefinition antenna = ventWall.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(66, 78).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1944F, -7.5F, 5.0F, 0.5967F, 0.4015F, 0.0157F));
      antenna.addOrReplaceChild("plane_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, 0.0F, -8.0F, 7.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.5F, 0.0F, -0.48F, 0.0F, 0.0F));
      PartDefinition canister = cdu.addOrReplaceChild("canister", CubeListBuilder.create().texOffs(60, 60).addBox(-8.0F, 5.0F, -8.0F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(28, 76).addBox(-8.0F, -3.0F, -5.5F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      canister.addOrReplaceChild("brace2_r1", CubeListBuilder.create().texOffs(34, 76).addBox(0.0F, -8.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 5.0F, -4.5F, 0.1309F, 0.0F, 0.3054F));
      canister.addOrReplaceChild("basse_r1", CubeListBuilder.create().texOffs(70, 34).addBox(-2.0F, -7.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(64, 0).addBox(-2.5F, -6.0F, -2.5F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 3.0F, -4.5F, -0.088F, 0.1852F, 0.1151F));
      canister.addOrReplaceChild("CanisterPump", CubeListBuilder.create().texOffs(30, 60).addBox(-3.0F, -0.5F, -3.5F, 8.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -0.5F, -4.5F, 0.0F, 0.0F, 0.2182F));
      PartDefinition pipe = canister.addOrReplaceChild("pipe", CubeListBuilder.create().texOffs(80, 39).addBox(-4.75F, -7.75F, -4.75F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(40, 76).addBox(0.0F, -8.5F, 3.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      pipe.addOrReplaceChild("maybe_r1", CubeListBuilder.create().texOffs(30, 53).addBox(1.0F, -0.5F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(58, 78).addBox(3.0F, -0.5F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(50, 78).addBox(0.0F, -0.5F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(64, 14).addBox(0.0F, 0.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.25F, -7.75F, -3.75F, 0.0F, 0.3927F, 0.3054F));
      pipe.addOrReplaceChild("ACPipeBolt_r1", CubeListBuilder.create().texOffs(76, 18).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.25F, -6.0F, 4.0F, 0.0F, 0.7854F, 0.0F));
      pipe.addOrReplaceChild("ACPipe_r1", CubeListBuilder.create().texOffs(54, 68).addBox(-1.0F, -2.0F, -7.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -6.0F, 3.5F, 0.3054F, 0.0F, 0.0F));
      pipe.addOrReplaceChild("boltup_r1", CubeListBuilder.create().texOffs(76, 27).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(76, 23).addBox(-1.5F, 0.5F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.75F, -5.25F, -3.75F, 0.0F, 0.7854F, 0.0F));
      pipe.addOrReplaceChild("lesser", CubeListBuilder.create().texOffs(70, 78).addBox(1.0F, -6.0F, -5.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(74, 68).addBox(1.0F, -2.0F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      cdu.addOrReplaceChild("AC", CubeListBuilder.create().texOffs(0, 34).addBox(-8.0F, -5.0F, 0.0F, 12.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(30, 68).addBox(-8.0F, -7.0F, 2.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(74, 74).addBox(1.0F, 3.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(80, 50).addBox(3.0F, 3.0F, -4.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(14, 69).addBox(-8.1F, -3.0F, 0.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition Tendril1 = cdu.addOrReplaceChild("Tendril1", CubeListBuilder.create().texOffs(0, 117).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.6163F, 9.1943F, -1.2108F, -0.0233F, 0.2608F, -0.4394F));
      PartDefinition Seg2Tendril1 = Tendril1.addOrReplaceChild("Seg2Tendril1", CubeListBuilder.create().texOffs(8, 116).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0137F, -8.9634F, -0.0145F, 0.0F, 0.0F, 1.0472F));
      Seg2Tendril1.addOrReplaceChild("Seg3Tendril1", CubeListBuilder.create().texOffs(16, 117).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0816F, -8.3197F, -0.0142F, -0.5631F, 0.0184F, 1.0905F));
      PartDefinition Tendril2 = cdu.addOrReplaceChild("Tendril2", CubeListBuilder.create().texOffs(0, 117).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.6163F, 8.1943F, 7.7892F, -0.3569F, 0.2316F, 0.1452F));
      PartDefinition Seg2Tendril2 = Tendril2.addOrReplaceChild("Seg2Tendril2", CubeListBuilder.create().texOffs(8, 116).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0137F, -8.9634F, -0.0145F, 0.6981F, 0.0F, 0.0436F));
      Seg2Tendril2.addOrReplaceChild("Seg3Tendril2", CubeListBuilder.create().texOffs(16, 117).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0816F, -8.3197F, -0.0142F, 1.309F, 0.0F, 0.0F));
      PartDefinition Tendril3 = cdu.addOrReplaceChild("Tendril3", CubeListBuilder.create().texOffs(0, 117).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1163F, 7.1943F, -1.2108F, 0.0F, 0.0F, 0.48F));
      PartDefinition Seg2Tendril3 = Tendril3.addOrReplaceChild("Seg2Tendril3", CubeListBuilder.create().texOffs(8, 116).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0137F, -8.9634F, -0.0145F, 0.48F, 0.0F, -0.5236F));
      Seg2Tendril3.addOrReplaceChild("Seg3Tendril3", CubeListBuilder.create().texOffs(16, 117).addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0816F, -8.3197F, -0.0142F, 0.0F, 0.0F, -1.5272F));
      PartDefinition Tendril4 = cdu.addOrReplaceChild("Tendril4", CubeListBuilder.create().texOffs(0, 117).mirror().addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.6163F, 9.1943F, -1.2108F, -0.1496F, 0.5052F, 0.1705F));
      PartDefinition Seg2Tendril4 = Tendril4.addOrReplaceChild("Seg2Tendril4", CubeListBuilder.create().texOffs(8, 116).mirror().addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(-0.0137F, -8.9634F, -0.0145F, 0.0F, 0.0F, -0.3491F));
      Seg2Tendril4.addOrReplaceChild("Seg3Tendril4", CubeListBuilder.create().texOffs(16, 117).mirror().addBox(-0.5F, -9.25F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.0816F, -8.3197F, -0.0142F, -0.0832F, -0.0184F, -1.0905F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.cdu.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   void animateTendrilsPulsate(ModelPart part, float val) {
      part.xScale = 1.0F + val;
      part.yScale = 1.0F + val;
      part.zScale = 1.0F + val;
   }

   public void setupAnim(CDUBlockEntity entity, float ageInTicks) {
      this.animateTendrilsPulsate(this.Tendril1, (float)(Math.sin((double)(ageInTicks / 16.0F)) / (double)12.0F));
      this.animateTendrilsPulsate(this.Tendril2, (float)(Math.cos((double)(ageInTicks / 17.0F)) / (double)13.0F));
      this.animateTendrilsPulsate(this.Tendril3, (float)(Math.sin((double)(ageInTicks / 19.0F)) / (double)15.0F));
      this.animateTendrilsPulsate(this.Tendril4, (float)(Math.cos((double)(ageInTicks / 18.0F)) / (double)14.0F));
   }
}
