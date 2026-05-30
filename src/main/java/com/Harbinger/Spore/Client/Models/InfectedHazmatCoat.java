package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.BasicInfected.InfectedHazmat;
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

public class InfectedHazmatCoat extends EntityModel<InfectedHazmat> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "inf_science"), "main");
   private final ModelPart inf_whiteguy;
   private final ModelPart Head;
   private final ModelPart RightArm;
   private final ModelPart LeftArm;
   private final ModelPart Card;
   private final ModelPart RightLeg;
   private final ModelPart LeftLeg;
   private final ModelPart RightforLeg;
   private final ModelPart LeftforLeg;
   private final ModelPart Tumor;

   public InfectedHazmatCoat(ModelPart root) {
      this.inf_whiteguy = root.getChild("inf_whiteguy");
      this.Head = this.inf_whiteguy.getChild("TorsoBase").getChild("TorsoTop").getChild("Head");
      this.RightArm = this.inf_whiteguy.getChild("TorsoBase").getChild("TorsoTop").getChild("Arms").getChild("ArmRight");
      this.LeftArm = this.inf_whiteguy.getChild("TorsoBase").getChild("TorsoTop").getChild("Arms").getChild("ArmLeft");
      this.Card = this.inf_whiteguy.getChild("TorsoBase").getChild("TorsoTop").getChild("KeyCard");
      this.RightLeg = this.inf_whiteguy.getChild("Legs").getChild("LegRight");
      this.LeftLeg = this.inf_whiteguy.getChild("Legs").getChild("LegLeft");
      this.RightforLeg = this.RightLeg.getChild("RightLegBase");
      this.LeftforLeg = this.LeftLeg.getChild("LeftLegBase");
      this.Tumor = this.RightLeg.getChild("RightLegTumors");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition inf_whiteguy = partdefinition.addOrReplaceChild("inf_whiteguy", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition TorsoBase = inf_whiteguy.addOrReplaceChild("TorsoBase", CubeListBuilder.create().texOffs(28, 12).addBox(-4.0F, -6.1F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(20, 22).addBox(-4.0F, -5.9F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -11.9F, 0.0F));
      TorsoBase.addOrReplaceChild("Fungus_r1", CubeListBuilder.create().texOffs(-7, 55).addBox(3.5F, 19.0F, 0.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6824F, -22.9221F, 3.6064F, -0.0252F, 0.6149F, 0.1144F));
      PartDefinition TorsoTop = TorsoBase.addOrReplaceChild("TorsoTop", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -6.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 28).addBox(-4.0F, -6.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -6.1F, 0.0F));
      PartDefinition Arms = TorsoTop.addOrReplaceChild("Arms", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.0F));
      PartDefinition ArmLeft = Arms.addOrReplaceChild("ArmLeft", CubeListBuilder.create().texOffs(48, 18).addBox(0.0F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(48, 45).addBox(0.0F, -1.0F, -1.999F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(4.0F, -23.0F, 0.0F));
      ArmLeft.addOrReplaceChild("ArmLeftBase", CubeListBuilder.create().texOffs(40, 28).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 5.0F, 0.0F));
      PartDefinition ArmRight = Arms.addOrReplaceChild("ArmRight", CubeListBuilder.create().texOffs(40, 28).addBox(-4.0F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 38).addBox(-4.0F, -1.0F, -2.001F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-4.0F, -23.0F, 0.0F));
      ArmRight.addOrReplaceChild("ArmRightBase", CubeListBuilder.create().texOffs(48, 18).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(32, 0).addBox(-2.0F, 0.2F, -1.99F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-2.0F, 5.0F, 0.0F));
      PartDefinition Head = TorsoTop.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));
      PartDefinition Mask = Head.addOrReplaceChild("Mask", CubeListBuilder.create().texOffs(48, 10).addBox(-4.0F, -27.0F, -4.0F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition WTFwouldyoucalltheseLeft = Mask.addOrReplaceChild("WTFwouldyoucalltheseLeft", CubeListBuilder.create().texOffs(0, 21).addBox(-1.05F, -0.25F, -1.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -25.5F, -2.0F));
      WTFwouldyoucalltheseLeft.addOrReplaceChild("topthing_r1", CubeListBuilder.create().texOffs(0, 21).addBox(0.0F, -0.5F, -1.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.05F, -1.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      PartDefinition WTFwouldyoucalltheseRight = Mask.addOrReplaceChild("WTFwouldyoucalltheseRight", CubeListBuilder.create().texOffs(0, 21).addBox(1.05F, -0.25F, -1.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -25.5F, -2.0F));
      WTFwouldyoucalltheseRight.addOrReplaceChild("topthing_r2", CubeListBuilder.create().texOffs(0, 21).addBox(0.0F, -0.5F, -1.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.05F, -1.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      PartDefinition HeadFungies = Head.addOrReplaceChild("HeadFungies", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      HeadFungies.addOrReplaceChild("Fungus_r2", CubeListBuilder.create().texOffs(-7, 62).addBox(-6.0F, 2.0F, 0.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, -5.0F, -1.0F, 0.3927F, 0.0F, 0.0F));
      HeadFungies.addOrReplaceChild("Fungus_r3", CubeListBuilder.create().texOffs(-7, 55).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6824F, -1.8221F, 3.6064F, -0.0252F, 0.6149F, 0.1144F));
      HeadFungies.addOrReplaceChild("Fungus_r4", CubeListBuilder.create().texOffs(-7, 48).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -6.1481F, -3.7716F, 0.1302F, 0.1744F, -0.0077F));
      PartDefinition KeyCard = TorsoTop.addOrReplaceChild("KeyCard", CubeListBuilder.create().texOffs(24, 0).addBox(-1.5F, 4.7062F, -0.5067F, 3.0F, 4.0F, 1.0F, new CubeDeformation(-0.4F)), PartPose.offset(0.0F, -6.2062F, -2.2533F));
      KeyCard.addOrReplaceChild("CordRight_r1", CubeListBuilder.create().texOffs(16, 38).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(-1.5F, 0.2062F, 0.0033F, 0.0F, 0.0F, -0.2182F));
      KeyCard.addOrReplaceChild("CordLeft_r1", CubeListBuilder.create().texOffs(16, 38).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(1.5F, 0.2062F, 0.0033F, 0.0F, 0.0F, 0.2182F));
      TorsoBase.addOrReplaceChild("TorsoBaseCoatTail", CubeListBuilder.create().texOffs(52, 38).addBox(-4.0F, 0.0F, -0.075F, 8.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.2F, 2.175F, 0.0175F, 0.0F, 0.0F));
      PartDefinition Legs = inf_whiteguy.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition LegLeft = Legs.addOrReplaceChild("LegLeft", CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(24, 32).addBox(-2.0F, 0.4F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(2.0F, -12.0F, 0.0F));
      LegLeft.addOrReplaceChild("LeftLegBase", CubeListBuilder.create().texOffs(64, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LegRight = Legs.addOrReplaceChild("LegRight", CubeListBuilder.create().texOffs(32, 49).addBox(-2.0F, 0.0F, -1.25F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(16, 53).addBox(-2.0F, 0.0F, -1.25F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(-2.0F, -12.0F, 0.0F));
      LegRight.addOrReplaceChild("RightLegBase", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(33, 51).addBox(-1.25F, 2.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition RightLegTumors = LegRight.addOrReplaceChild("RightLegTumors", CubeListBuilder.create(), PartPose.offset(-0.5F, 5.5F, -0.5F));
      RightLegTumors.addOrReplaceChild("Tumor_r1", CubeListBuilder.create().texOffs(56, 28).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3054F, -0.6545F));
      RightLegTumors.addOrReplaceChild("Tumor_r2", CubeListBuilder.create().texOffs(56, 28).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, -0.3491F, 0.4363F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(InfectedHazmat entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (limbSwingAmount > -0.15F && limbSwingAmount < 0.15F) {
         this.Card.zRot = 0.0F;
         this.RightArm.xRot = -0.7F + Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.LeftArm.xRot = -0.7F - Mth.sin(ageInTicks / 8.0F) / 10.0F;
      } else {
         this.RightArm.xRot = -90.0F - Mth.sin(ageInTicks / 4.0F) / 7.0F;
         this.LeftArm.xRot = -90.0F + Mth.sin(ageInTicks / 4.0F) / 7.0F;
         this.Card.zRot = Mth.sin(ageInTicks / 7.0F) / 7.0F;
      }

      this.LeftLeg.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
      this.RightLeg.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
      if (this.LeftLeg.xRot < 0.0F) {
         this.LeftforLeg.xRot = -this.LeftLeg.xRot;
      }

      if (this.RightLeg.xRot < 0.0F) {
         this.RightforLeg.xRot = -this.RightLeg.xRot;
      }

      this.Tumor.xScale = 1.0F + Mth.sin(ageInTicks / 10.0F) / 10.0F;
      this.Tumor.yScale = 1.0F + Mth.sin(ageInTicks / 10.0F) / 10.0F;
      this.Tumor.zScale = 1.0F + Mth.sin(ageInTicks / 10.0F) / 10.0F;
      this.Head.yRot = netHeadYaw / (180F / (float)Math.PI);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.inf_whiteguy.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
