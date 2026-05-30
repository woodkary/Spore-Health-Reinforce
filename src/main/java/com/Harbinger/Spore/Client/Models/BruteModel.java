package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Brute;
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

public class BruteModel extends EntityModel<Brute> implements ArmedModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "brutemodel"), "main");
   private final ModelPart Brute;
   private final ModelPart Legs;
   private final ModelPart LeftLeg;
   private final ModelPart LeftLegSeg2;
   private final ModelPart RightLeg;
   private final ModelPart RightLegSeg2;
   private final ModelPart Arms;
   private final ModelPart LeftArm;
   private final ModelPart LeftArmSeg2;
   private final ModelPart LeftArmFungus;
   private final ModelPart LeftFist;
   private final ModelPart Fist;
   private final ModelPart RightArm;
   private final ModelPart RightArmSeg3;
   private final ModelPart RightFist;
   private final ModelPart Fist2;
   private final ModelPart FistFungus;
   private final ModelPart MiddleChestFungus;
   private final ModelPart crown2;
   private final ModelPart crown3;
   private final ModelPart crown6;
   private final ModelPart Neck;
   private final ModelPart Head;
   private final ModelPart Jaw;
   private final ModelPart crown1;
   private final ModelPart crown4;
   private final ModelPart crown5;
   private final ModelPart FungalBloom;

   public BruteModel(ModelPart root) {
      this.Brute = root.getChild("Brute");
      this.Legs = this.Brute.getChild("Legs");
      this.LeftLeg = this.Legs.getChild("LeftLeg");
      this.LeftLegSeg2 = this.LeftLeg.getChild("LeftLegSeg2");
      this.RightLeg = this.Legs.getChild("RightLeg");
      this.RightLegSeg2 = this.RightLeg.getChild("RightLegSeg2");
      this.Arms = this.Brute.getChild("Arms");
      this.LeftArm = this.Arms.getChild("LeftArm");
      this.LeftArmSeg2 = this.LeftArm.getChild("LeftArmSeg2");
      this.LeftArmFungus = this.LeftArmSeg2.getChild("LeftArmFungus");
      this.LeftFist = this.LeftArmSeg2.getChild("LeftFist");
      this.Fist = this.LeftFist.getChild("Fist");
      this.RightArm = this.Arms.getChild("RightArm");
      this.RightArmSeg3 = this.RightArm.getChild("RightArmSeg3");
      this.RightFist = this.RightArmSeg3.getChild("RightFist");
      this.Fist2 = this.RightFist.getChild("Fist2");
      this.FistFungus = this.Fist2.getChild("FistFungus");
      this.MiddleChestFungus = this.RightArm.getChild("MiddleChestFungus");
      this.crown2 = this.Arms.getChild("crown2");
      this.crown3 = this.Arms.getChild("crown3");
      this.crown6 = this.Arms.getChild("crown6");
      this.Neck = this.Brute.getChild("Neck");
      this.Head = this.Neck.getChild("Head");
      this.Jaw = this.Head.getChild("Jaw");
      this.crown1 = this.Head.getChild("crown1");
      this.crown4 = this.Head.getChild("crown4");
      this.crown5 = this.Head.getChild("crown5");
      this.FungalBloom = this.Brute.getChild("FungalBloom");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Brute = partdefinition.addOrReplaceChild("Brute", CubeListBuilder.create(), PartPose.offset(0.0F, 13.0F, 2.0F));
      Brute.addOrReplaceChild("UpperTorso_r1", CubeListBuilder.create().texOffs(0, 64).addBox(-6.0F, -4.0F, -12.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5357F, 2.4379F, -0.6981F, 0.0F, 0.0F));
      Brute.addOrReplaceChild("Tail_r1", CubeListBuilder.create().texOffs(97, 54).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.8359F, 8.784F, -0.0785F, 0.0F, 0.0F));
      Brute.addOrReplaceChild("Base_r1", CubeListBuilder.create().texOffs(64, 52).addBox(-5.0F, -3.0F, -1.5F, 10.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5357F, 2.4379F, -0.3054F, 0.0F, 0.0F));
      PartDefinition Legs = Brute.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 6.0F));
      PartDefinition LeftLeg = Legs.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(64, 114).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1935F, -0.5904F, 0.0559F, 0.6484F, 0.2106F, -0.0573F));
      LeftLeg.addOrReplaceChild("LeftLegSeg2", CubeListBuilder.create().texOffs(24, 114).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.6545F, 0.0F, 0.0F));
      PartDefinition RightLeg = Legs.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(64, 114).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1935F, -0.5904F, 0.0559F, 0.6484F, -0.2106F, 0.0573F));
      RightLeg.addOrReplaceChild("RightLegSeg2", CubeListBuilder.create().texOffs(24, 114).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.6545F, 0.0F, 0.0F));
      PartDefinition Arms = Brute.addOrReplaceChild("Arms", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -5.0F));
      Arms.addOrReplaceChild("RightShoulder_r1", CubeListBuilder.create().texOffs(0, 112).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 112).addBox(5.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -1.9452F, 1.8213F, -0.3054F, 0.0F, 0.0F));
      PartDefinition LeftArm = Arms.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(48, 80).addBox(-2.0F, -3.5F, -2.5F, 8.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.625F, 0.6495F, 1.0F, 0.18F, 0.3007F, 0.5509F));
      PartDefinition LeftArmSeg2 = LeftArm.addOrReplaceChild("LeftArmSeg2", CubeListBuilder.create().texOffs(92, 0).addBox(-2.0F, -1.5F, -2.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4253F, 0.0092F, 0.0F, 0.0F, 0.0F, -0.7418F));
      PartDefinition LeftArmFungus = LeftArmSeg2.addOrReplaceChild("LeftArmFungus", CubeListBuilder.create(), PartPose.offset(1.8904F, 0.502F, 0.6525F));
      LeftArmFungus.addOrReplaceChild("Fungus_r1", CubeListBuilder.create().texOffs(-7, 0).addBox(-3.5F, 1.0F, -7.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4407F, 2.7892F, 3.1475F, -0.1797F, -0.4891F, 0.195F));
      LeftArmFungus.addOrReplaceChild("Fungus_r2", CubeListBuilder.create().texOffs(-7, 7).addBox(-3.5F, 1.0F, -2.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5593F, -0.2108F, 0.8975F, 0.1783F, -0.2977F, -0.0909F));
      PartDefinition LeftFist = LeftArmSeg2.addOrReplaceChild("LeftFist", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));
      LeftFist.addOrReplaceChild("Fist", CubeListBuilder.create().texOffs(0, 84).addBox(-3.0F, -2.0F, -3.0F, 7.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4882F, -1.3918F, 0.5F, 0.0F, 0.0F, 0.2182F));
      PartDefinition RightArm = Arms.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(48, 80).mirror().addBox(-6.0F, -3.5F, -2.5F, 8.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.625F, 0.6495F, 1.0F, 0.18F, -0.3007F, -0.5509F));
      PartDefinition RightArmSeg3 = RightArm.addOrReplaceChild("RightArmSeg3", CubeListBuilder.create().texOffs(92, 0).addBox(-4.0F, -1.5F, -2.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4253F, 0.0092F, 0.0F, 0.0F, 0.0F, 0.7418F));
      PartDefinition RightFist = RightArmSeg3.addOrReplaceChild("RightFist", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));
      PartDefinition Fist2 = RightFist.addOrReplaceChild("Fist2", CubeListBuilder.create().texOffs(0, 84).addBox(-4.0F, -2.0F, -3.0F, 7.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4882F, -1.3918F, 0.5F, 0.0F, 0.0F, -0.2182F));
      PartDefinition FistFungus = Fist2.addOrReplaceChild("FistFungus", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));
      FistFungus.addOrReplaceChild("Fungus_r3", CubeListBuilder.create().texOffs(-7, 0).addBox(-1.5F, 1.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0384F, -10.817F, 2.05F, 0.3325F, 0.5154F, 0.0523F));
      FistFungus.addOrReplaceChild("Fungus_r4", CubeListBuilder.create().texOffs(-7, 7).addBox(-8.5F, 1.0F, -7.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0384F, -8.317F, 6.05F, -0.1797F, -0.4891F, 0.195F));
      PartDefinition MiddleChestFungus = RightArm.addOrReplaceChild("MiddleChestFungus", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.4343F, -1.4887F, 3.6525F, 0.0F, 0.0F, 1.6144F));
      MiddleChestFungus.addOrReplaceChild("Fungus_r5", CubeListBuilder.create().texOffs(-7, 14).addBox(-3.5F, 0.0F, -8.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3702F, 2.2607F, -2.0283F, 0.038F, 0.7157F, -0.187F));
      MiddleChestFungus.addOrReplaceChild("Fungus_r6", CubeListBuilder.create().texOffs(-7, 7).addBox(-0.5F, 0.0F, -2.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4407F, 1.7892F, -3.1025F, 0.1783F, -0.2977F, -0.0909F));
      PartDefinition crown2 = Arms.addOrReplaceChild("crown2", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.7521F, -3.1863F, 3.6527F, -0.2906F, -0.0993F, -0.2383F));
      crown2.addOrReplaceChild("Petal4_r1", CubeListBuilder.create().texOffs(-7, 7).addBox(-5.0F, -1.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8861F, -0.5245F, 0.0399F, 0.1745F, 0.0F, -0.4363F));
      crown2.addOrReplaceChild("Petal3_r1", CubeListBuilder.create().texOffs(-7, 0).addBox(-1.0F, -2.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6315F, 1.4073F, 0.0399F, 0.263F, 0.1603F, 0.1693F));
      crown2.addOrReplaceChild("Petal2_r1", CubeListBuilder.create().texOffs(-7, 14).addBox(-3.0F, 0.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1139F, -0.5245F, -3.9601F, -0.4363F, 0.0F, 0.0F));
      crown2.addOrReplaceChild("Petal1_r1", CubeListBuilder.create().texOffs(-7, 7).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2969F, 0.3356F, 0.5792F, 0.4726F, -0.2047F, -0.0163F));
      PartDefinition crown3 = Arms.addOrReplaceChild("crown3", CubeListBuilder.create(), PartPose.offsetAndRotation(5.5201F, -3.6171F, 0.92F, 0.2589F, -0.2264F, 0.2434F));
      crown3.addOrReplaceChild("Petal5_r1", CubeListBuilder.create().texOffs(-7, 0).addBox(-2.0F, -1.0F, -3.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8861F, -0.5245F, -0.0399F, -0.1745F, 0.0F, 0.4363F));
      crown3.addOrReplaceChild("Petal4_r2", CubeListBuilder.create().texOffs(-7, 14).addBox(-6.0F, -2.0F, -3.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6315F, 1.4073F, -0.0399F, -0.263F, 0.1603F, -0.1693F));
      crown3.addOrReplaceChild("Petal3_r2", CubeListBuilder.create().texOffs(-7, 7).addBox(-4.0F, 0.0F, -3.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1139F, -0.5245F, 3.9601F, 0.4363F, 0.0F, 0.0F));
      crown3.addOrReplaceChild("Petal2_r2", CubeListBuilder.create().texOffs(-7, 0).addBox(-3.5F, 0.0F, -5.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2969F, 0.3356F, -0.5792F, -0.4726F, -0.2047F, 0.1036F));
      PartDefinition crown6 = Arms.addOrReplaceChild("crown6", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.5765F, 6.6135F, 0.7692F, -0.6729F, 1.0E-4F, 0.2258F));
      crown6.addOrReplaceChild("Petal6_r1", CubeListBuilder.create().texOffs(-7, 0).addBox(-2.0F, 1.0F, -3.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8861F, 0.5245F, -0.0399F, 0.1745F, 0.0F, -0.4363F));
      crown6.addOrReplaceChild("Petal5_r2", CubeListBuilder.create().texOffs(-7, 14).addBox(-6.0F, 2.0F, -3.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6315F, -1.4073F, -0.0399F, 0.263F, 0.1603F, 0.1693F));
      crown6.addOrReplaceChild("Petal4_r3", CubeListBuilder.create().texOffs(-7, 7).addBox(-4.0F, 0.0F, -3.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1139F, 0.5245F, 3.9601F, -0.4363F, 0.0F, 0.0F));
      crown6.addOrReplaceChild("Petal3_r3", CubeListBuilder.create().texOffs(-7, 0).addBox(-3.5F, 0.0F, -5.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2969F, -0.3356F, -0.5792F, 0.4726F, -0.2047F, -0.1036F));
      PartDefinition Neck = Brute.addOrReplaceChild("Neck", CubeListBuilder.create().texOffs(112, 108).addBox(-3.0F, -4.0F, -1.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, -8.0F));
      PartDefinition Head = Neck.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(48, 64).addBox(-4.0F, -6.0F, -7.5F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(68, 93).addBox(-1.0F, 1.0F, -9.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -1.0F));
      PartDefinition Jaw = Head.addOrReplaceChild("Jaw", CubeListBuilder.create().texOffs(78, 91).addBox(-4.0F, 1.0F, -7.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(28, 84).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 2.0F, -0.5F));
      Jaw.addOrReplaceChild("Tooth_r1", CubeListBuilder.create().texOffs(30, 82).addBox(0.0F, -1.0F, -3.0F, 0.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 1.0F, -4.0F, -0.0757F, 0.2048F, -0.3568F));
      Jaw.addOrReplaceChild("Tooth_r2", CubeListBuilder.create().texOffs(32, 84).addBox(0.0F, -1.0F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 1.0F, -4.0F, 0.0F, -0.2182F, 0.0F));
      Jaw.addOrReplaceChild("Tooth_r3", CubeListBuilder.create().texOffs(28, 88).addBox(-4.0F, -1.0F, 0.0F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -6.0F, 0.0F, -0.1745F, 0.0F));
      PartDefinition crown1 = Head.addOrReplaceChild("crown1", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.63F, -5.0293F, -5.1129F, 0.3291F, -0.2439F, -0.5362F));
      crown1.addOrReplaceChild("Petal4_r4", CubeListBuilder.create().texOffs(-7, 7).addBox(-5.0F, -1.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8861F, -0.5245F, 0.0399F, 0.1745F, 0.0F, -0.4363F));
      crown1.addOrReplaceChild("Petal3_r4", CubeListBuilder.create().texOffs(-7, 0).addBox(-1.0F, -2.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6315F, 1.4073F, 0.0399F, 0.263F, 0.1603F, 0.1693F));
      crown1.addOrReplaceChild("Petal2_r3", CubeListBuilder.create().texOffs(-7, 14).addBox(-3.0F, 0.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1139F, -0.5245F, -3.9601F, -0.4363F, 0.0F, 0.0F));
      crown1.addOrReplaceChild("Petal1_r2", CubeListBuilder.create().texOffs(-7, 7).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2969F, 0.3356F, 0.5792F, 0.4726F, -0.2047F, -0.1036F));
      PartDefinition crown4 = Head.addOrReplaceChild("crown4", CubeListBuilder.create(), PartPose.offsetAndRotation(2.2475F, -3.6823F, -5.3111F, 0.5823F, -0.8025F, 0.4237F));
      crown4.addOrReplaceChild("Petal5_r3", CubeListBuilder.create().texOffs(-7, 7).mirror().addBox(-2.0F, -1.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.8861F, -0.5245F, 0.0399F, 0.1745F, 0.0F, 0.4363F));
      crown4.addOrReplaceChild("Petal4_r5", CubeListBuilder.create().texOffs(-7, 0).mirror().addBox(-6.0F, -2.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.6315F, 1.4073F, 0.0399F, 0.263F, -0.1603F, -0.1693F));
      crown4.addOrReplaceChild("Petal3_r5", CubeListBuilder.create().texOffs(-7, 14).mirror().addBox(-4.0F, 0.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.1139F, -0.5245F, -3.9601F, -0.4363F, 0.0F, 0.0F));
      crown4.addOrReplaceChild("Petal2_r4", CubeListBuilder.create().texOffs(-7, 7).mirror().addBox(-3.5F, 0.0F, -1.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.2969F, 0.3356F, 0.5792F, 0.4726F, 0.2047F, 0.1036F));
      PartDefinition crown5 = Head.addOrReplaceChild("crown5", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.7117F, -3.1016F, -4.6982F, 0.712F, 0.3536F, -0.4548F));
      crown5.addOrReplaceChild("Petal6_r2", CubeListBuilder.create().texOffs(-7, 7).addBox(-5.0F, -1.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8861F, -0.5245F, 0.0399F, 0.1745F, 0.0F, -0.4363F));
      crown5.addOrReplaceChild("Petal5_r4", CubeListBuilder.create().texOffs(-7, 0).addBox(-1.0F, -2.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6315F, 1.4073F, 0.0399F, 0.263F, 0.1603F, 0.1693F));
      crown5.addOrReplaceChild("Petal4_r6", CubeListBuilder.create().texOffs(-7, 14).addBox(-3.0F, 0.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1139F, -0.5245F, -3.9601F, -0.4363F, 0.0F, 0.0F));
      crown5.addOrReplaceChild("Petal3_r6", CubeListBuilder.create().texOffs(-7, 7).addBox(-3.5F, 0.0F, -1.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2969F, 0.3356F, 0.5792F, 0.4726F, -0.2047F, -0.1036F));
      PartDefinition FungalBloom = Brute.addOrReplaceChild("FungalBloom", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1532F, -2.7802F, 3.0507F, -0.6127F, -0.3644F, 0.2455F));
      FungalBloom.addOrReplaceChild("Plane2_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-11.8501F, -13.8001F, -7.6583F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2533F, 1.5803F, -7.8924F, 0.0F, 0.7854F, 0.0F));
      FungalBloom.addOrReplaceChild("Plane1_r1", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-0.8501F, -13.8001F, 5.3417F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(9.2533F, 1.5803F, -7.8924F, 0.0F, -0.7854F, 0.0F));
      FungalBloom.addOrReplaceChild("Npetal_r1", CubeListBuilder.create().texOffs(0, 15).addBox(-16.8501F, -0.8001F, -17.6583F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5033F, 1.5803F, -0.3924F, -0.3927F, 0.0F, 0.0F));
      FungalBloom.addOrReplaceChild("Spetal_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-16.8501F, -0.8001F, 2.3417F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5033F, 1.5803F, -0.3924F, 0.3927F, 0.0F, 0.0F));
      FungalBloom.addOrReplaceChild("Wpetal_r1", CubeListBuilder.create().texOffs(-1, 46).addBox(1.1499F, -0.8001F, 0.3417F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5033F, 1.5803F, -8.3924F, 0.0F, 0.0F, -0.3927F));
      FungalBloom.addOrReplaceChild("Epetal_r1", CubeListBuilder.create().texOffs(0, 30).addBox(-17.8501F, -0.8001F, 0.3417F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5033F, 1.5803F, -8.3924F, 0.0F, 0.0F, 0.3927F));
      return LayerDefinition.create(meshdefinition, 256, 256);
   }

   public void setupAnim(Brute entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.Head.yRot = netHeadYaw / (180F / (float)Math.PI);
      this.Jaw.xRot = 0.4F + Mth.sin(ageInTicks / 5.0F) / 3.0F;
      if (!(limbSwingAmount > -0.15F) || !(limbSwingAmount < 0.15F)) {
         float moveValue = Mth.cos(limbSwing * 0.3F) * 0.8F * limbSwingAmount;
         this.LeftLeg.yRot = this.LeftLeg.getInitialPose().yRot + moveValue * 1.6F;
         this.RightLeg.yRot = this.RightLeg.getInitialPose().yRot + moveValue * 1.6F;
         this.LeftLeg.xRot = this.LeftLeg.getInitialPose().xRot + moveValue;
         this.RightLeg.xRot = this.RightLeg.getInitialPose().xRot - moveValue;
         this.LeftArm.yRot = this.RightArm.getInitialPose().yRot + moveValue;
         this.RightArm.yRot = this.RightArm.getInitialPose().yRot + moveValue;
         this.RightFist.zRot = this.RightFist.getInitialPose().zRot + moveValue;
         this.LeftFist.zRot = this.LeftFist.getInitialPose().zRot + moveValue;
      }

   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.Brute.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
      poseStack.translate(0.0F, -1.3F, 0.6F);
      poseStack.scale(1.3F, 1.3F, 1.3F);
      this.Brute.translateAndRotate(poseStack);
   }
}
