package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Client.Animations.ScavengerAnimations;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scavenger;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
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
import org.joml.Vector3f;

public class ScavengerModel extends HierarchicalModel<Scavenger> implements TentacledModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "scavengermodel"), "main");
   private final ModelPart scavenger;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final ModelPart rightForLeg;
   private final ModelPart leftForLeg;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart tail1;
   private final ModelPart tail2;
   private final ModelPart tail3;
   private final ModelPart tail4;
   private final ModelPart tail5;
   private final ModelPart Tumor1;
   private final ModelPart Tumor2;
   private final ModelPart Tumor3;
   private final ModelPart Neck1;
   private final ModelPart Neck2;
   private final ModelPart Head;
   private final ModelPart Tumor4;
   private final ModelPart Jaw;
   private final ModelPart Fang1;
   private final ModelPart Fang2;
   private final ModelPart Fang3;
   private final ModelPart Fang4;
   private final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

   public ScavengerModel(ModelPart root) {
      this.scavenger = root.getChild("scavenger");
      this.rightLeg = this.scavenger.getChild("Legs").getChild("RightLeg");
      this.leftLeg = this.scavenger.getChild("Legs").getChild("LeftLeg");
      this.rightForLeg = this.rightLeg.getChild("RightLegSeg2");
      this.leftForLeg = this.leftLeg.getChild("LeftLegSeg2");
      this.rightWing = this.scavenger.getChild("LowerTorso").getChild("UpperTorso").getChild("Arms").getChild("Right");
      this.leftWing = this.scavenger.getChild("LowerTorso").getChild("UpperTorso").getChild("Arms").getChild("Left");
      this.tail1 = this.scavenger.getChild("LowerTorso").getChild("Tail");
      this.tail2 = this.tail1.getChild("Tail2");
      this.tail3 = this.tail2.getChild("Tail3");
      this.tail4 = this.tail3.getChild("Tail4");
      this.tail5 = this.tail4.getChild("Tail5");
      this.Tumor1 = this.scavenger.getChild("LowerTorso").getChild("TailTumors");
      this.Tumor2 = this.tail2.getChild("Tail2Tumors");
      this.Tumor3 = this.rightWing.getChild("RightSeg1").getChild("RightSeg2").getChild("RightSeg3").getChild("RightTumors");
      this.Neck1 = this.scavenger.getChild("LowerTorso").getChild("UpperTorso").getChild("Neck");
      this.Neck2 = this.Neck1.getChild("Neck2");
      this.Head = this.Neck2.getChild("Neck3").getChild("Head");
      this.Tumor4 = this.Head.getChild("Tumors");
      this.Jaw = this.Head.getChild("Jaw");
      this.Fang1 = this.Head.getChild("UpperRightFang");
      this.Fang2 = this.Head.getChild("UpperLeftFang");
      this.Fang3 = this.Jaw.getChild("LowerRightFang");
      this.Fang4 = this.Jaw.getChild("LowerLeftFang");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition scavenger = partdefinition.addOrReplaceChild("scavenger", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition LowerTorso = scavenger.addOrReplaceChild("LowerTorso", CubeListBuilder.create().texOffs(30, 14).addBox(-4.0F, -7.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.3986F, 3.9074F, 1.2654F, 0.0F, 0.0F));
      PartDefinition UpperTorso = LowerTorso.addOrReplaceChild("UpperTorso", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -3.0F, 10.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
      UpperTorso.addOrReplaceChild("HunchPlaneBack_r1", CubeListBuilder.create().texOffs(98, 36).addBox(0.0F, -2.5F, 0.5F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(58, 0).addBox(-4.0F, -0.5F, -2.5F, 8.0F, 8.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, -2.7686F, 4.5095F, -0.2618F, 0.0F, 0.0F));
      UpperTorso.addOrReplaceChild("HunchPlaneFront_r1", CubeListBuilder.create().texOffs(42, 98).addBox(-0.01F, -1.0F, 0.0F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 14).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 6.0F, 5.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, -7.8616F, 2.0F, 0.6109F, 0.0F, 0.0F));
      PartDefinition Neck = UpperTorso.addOrReplaceChild("Neck", CubeListBuilder.create().texOffs(62, 81).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(106, 53).addBox(0.0F, -4.0F, 2.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -0.25F, 0.5672F, 0.0F, 0.0F));
      PartDefinition Neck2 = Neck.addOrReplaceChild("Neck2", CubeListBuilder.create().texOffs(62, 81).mirror().addBox(-2.0F, -4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.25F)).mirror(false).texOffs(106, 46).addBox(-0.01F, -3.25F, 1.5F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.4363F, 0.0F, 0.0F));
      PartDefinition Neck3 = Neck2.addOrReplaceChild("Neck3", CubeListBuilder.create().texOffs(86, 92).addBox(-1.5F, -4.5F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(16, 84).addBox(0.0F, -4.5F, 1.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, -0.8727F, 0.0F, 0.0F));
      PartDefinition Head = Neck3.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(48, 19).addBox(-4.0F, -6.0F, -2.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.25F, 0.0F));
      PartDefinition Jaw = Head.addOrReplaceChild("Jaw", CubeListBuilder.create().texOffs(0, 76).addBox(-4.0F, -6.0F, -2.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -2.0F, 0.4363F, 0.0F, 0.0F));
      PartDefinition LowerRightFang = Jaw.addOrReplaceChild("LowerRightFang", CubeListBuilder.create().texOffs(104, 0).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6029F, -4.8324F, -1.514F, 0.2517F, 0.2494F, -0.0804F));
      LowerRightFang.addOrReplaceChild("Plane_r1", CubeListBuilder.create().texOffs(99, 50).mirror().addBox(0.0F, -2.0F, -3.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 1.5F, 0.0F, 0.0F, 3.1416F));
      PartDefinition LowerLeftFang = Jaw.addOrReplaceChild("LowerLeftFang", CubeListBuilder.create().texOffs(104, 0).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6029F, -4.8324F, -1.514F, 0.2517F, -0.2494F, 0.0804F));
      LowerLeftFang.addOrReplaceChild("Plane_r2", CubeListBuilder.create().texOffs(99, 50).mirror().addBox(0.0F, -2.0F, -3.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 1.5F, 0.0F, 0.0F, 3.1416F));
      PartDefinition UpperRightFang = Head.addOrReplaceChild("UpperRightFang", CubeListBuilder.create().texOffs(104, 0).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.3007F, -0.5463F, 0.3155F, -0.2494F, -0.0804F));
      UpperRightFang.addOrReplaceChild("Plane_r3", CubeListBuilder.create().texOffs(99, 50).mirror().addBox(0.0F, -3.0F, -1.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, -1.5F, -2.9671F, 0.0F, 0.0F));
      PartDefinition UpperLeftFang = Head.addOrReplaceChild("UpperLeftFang", CubeListBuilder.create().texOffs(104, 0).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -4.3007F, -0.5463F, 0.3155F, 0.2494F, 0.0804F));
      UpperLeftFang.addOrReplaceChild("Plane_r4", CubeListBuilder.create().texOffs(99, 50).mirror().addBox(0.0F, -3.0F, -1.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, -1.5F, -2.9671F, 0.0F, 0.0F));
      PartDefinition Tumors = Head.addOrReplaceChild("Tumors", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.2209F, -0.3616F, 2.8002F, -0.3054F, -0.6981F, 0.0F));
      Tumors.addOrReplaceChild("Tumor_r1", CubeListBuilder.create().texOffs(0, 84).addBox(1.5F, -1.5F, -2.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.5569F, 3.0453F, 2.3457F, 0.3312F, 0.6681F, -0.7794F));
      Tumors.addOrReplaceChild("Tumor_r2", CubeListBuilder.create().texOffs(2, 85).addBox(-1.75F, -1.5F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3875F, -0.383F, -0.3346F, 0.6625F, 0.0949F, 0.6994F));
      Tumors.addOrReplaceChild("Tumor_r3", CubeListBuilder.create().texOffs(0, 84).addBox(-1.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8667F, -1.3456F, 0.831F, 0.9163F, 0.5236F, 0.0F));
      PartDefinition HeadCrown = Head.addOrReplaceChild("HeadCrown", CubeListBuilder.create(), PartPose.offsetAndRotation(4.088F, 0.1653F, 1.7805F, 0.0F, 0.0F, 1.8762F));
      HeadCrown.addOrReplaceChild("Petal4_r1", CubeListBuilder.create().texOffs(-7, 44).addBox(-2.0F, -1.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8861F, -0.5245F, 0.0399F, 0.1745F, 0.0F, 0.4363F));
      HeadCrown.addOrReplaceChild("Petal3_r1", CubeListBuilder.create().texOffs(-7, 37).addBox(-6.0F, -2.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6315F, 1.4073F, 0.0399F, 0.263F, -0.1603F, -0.1693F));
      HeadCrown.addOrReplaceChild("Petal2_r1", CubeListBuilder.create().texOffs(-7, 37).addBox(-4.0F, 0.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1139F, -0.5245F, -3.9601F, -0.4363F, 0.0F, 0.0F));
      HeadCrown.addOrReplaceChild("Petal1_r1", CubeListBuilder.create().texOffs(-7, 44).addBox(-4.0F, 0.0F, -5.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1139F, -0.5245F, 4.0399F, 0.3838F, 0.2129F, -0.4821F));
      PartDefinition Arms = UpperTorso.addOrReplaceChild("Arms", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 1.0F));
      PartDefinition Left = Arms.addOrReplaceChild("Left", CubeListBuilder.create().texOffs(0, 92).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3491F));
      PartDefinition LeftSeg1 = Left.addOrReplaceChild("LeftSeg1", CubeListBuilder.create().texOffs(0, 64).addBox(-1.0F, -1.5F, -1.5F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(56, 42).addBox(-4.0F, 1.5F, 0.25F, 13.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, -0.1309F, 0.0F));
      PartDefinition LeftArmFungus = LeftSeg1.addOrReplaceChild("LeftArmFungus", CubeListBuilder.create(), PartPose.offsetAndRotation(1.9373F, -1.864F, 1.745F, 0.0F, 0.0F, 1.3526F));
      LeftArmFungus.addOrReplaceChild("Fungus_r1", CubeListBuilder.create().texOffs(-7, 44).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5593F, -4.2108F, -1.1025F, 0.3325F, 0.5154F, 0.0523F));
      LeftArmFungus.addOrReplaceChild("Fungus_r2", CubeListBuilder.create().texOffs(-7, 37).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5593F, -2.2108F, -2.1025F, -0.2664F, -0.4504F, 0.3859F));
      LeftArmFungus.addOrReplaceChild("Fungus_r3", CubeListBuilder.create().texOffs(-7, 51).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.322F, 0.6324F, 0.3074F, 0.0333F, 0.6219F, 0.3593F));
      LeftArmFungus.addOrReplaceChild("Fungus_r4", CubeListBuilder.create().texOffs(-7, 51).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5593F, 2.7892F, -2.1025F, 0.0037F, -0.2977F, -0.0909F));
      PartDefinition LeftSeg2 = LeftSeg1.addOrReplaceChild("LeftSeg2", CubeListBuilder.create().texOffs(96, 0).addBox(0.0F, -1.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(82, 9).mirror().addBox(-6.0F, -1.0F, 0.5F, 7.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(8.0839F, 0.0F, -0.1515F, 0.0F, 0.0F, -0.7854F));
      PartDefinition LeftSeg3 = LeftSeg2.addOrReplaceChild("LeftSeg3", CubeListBuilder.create().texOffs(20, 78).addBox(-0.75F, -0.25F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)).texOffs(0, 58).mirror().addBox(-5.75F, 0.75F, 0.0F, 14.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 9.0F, 0.0F, 0.0F, 0.0F, 0.6109F));
      PartDefinition LeftTipFungus = LeftSeg3.addOrReplaceChild("LeftTipFungus", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      LeftTipFungus.addOrReplaceChild("Plane_r5", CubeListBuilder.create().texOffs(0, 30).addBox(0.0F, -3.5F, -3.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 0.5F, 0.5F, 0.7854F, 0.0F, -0.4363F));
      PartDefinition LeftWingTalons = LeftSeg3.addOrReplaceChild("LeftWingTalons", CubeListBuilder.create(), PartPose.offset(7.4161F, -2.0507F, 0.8552F));
      LeftWingTalons.addOrReplaceChild("Plane_r6", CubeListBuilder.create().texOffs(99, 50).mirror().addBox(0.0F, -1.0F, -2.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0567F, 2.9968F, -1.2271F, -1.2075F, -0.0174F, -1.5814F));
      LeftWingTalons.addOrReplaceChild("Plane_r7", CubeListBuilder.create().texOffs(99, 50).mirror().addBox(0.0F, -1.0F, -2.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.204F, 3.3802F, -1.1384F, -2.8236F, -0.0829F, 2.0346F));
      LeftWingTalons.addOrReplaceChild("Plane_r8", CubeListBuilder.create().texOffs(99, 50).mirror().addBox(0.0F, -1.0F, -1.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.2515F, 2.2044F, -0.3967F, -2.8235F, -0.5492F, 0.8264F));
      PartDefinition Right = Arms.addOrReplaceChild("Right", CubeListBuilder.create().texOffs(0, 92).mirror().addBox(-2.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));
      PartDefinition RightSeg1 = Right.addOrReplaceChild("RightSeg1", CubeListBuilder.create().texOffs(0, 64).mirror().addBox(-9.0F, -1.5F, -1.5F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(56, 42).mirror().addBox(-9.0F, 1.5F, 0.25F, 13.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, 0.1309F, 0.0F));
      PartDefinition RightSeg2 = RightSeg1.addOrReplaceChild("RightSeg2", CubeListBuilder.create().texOffs(96, 0).mirror().addBox(-2.0F, -1.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(82, 9).addBox(-1.0F, -1.0F, 0.5F, 7.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0839F, 0.0F, -0.1515F, 0.0F, 0.0F, 0.7854F));
      PartDefinition RightFungus = RightSeg2.addOrReplaceChild("RightFungus", CubeListBuilder.create(), PartPose.offset(-2.9754F, 6.5594F, 1.8966F));
      RightFungus.addOrReplaceChild("Fungus_r5", CubeListBuilder.create().texOffs(-7, 51).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5593F, -4.2108F, -1.1025F, 0.3325F, 0.5154F, 0.0523F));
      RightFungus.addOrReplaceChild("Fungus_r6", CubeListBuilder.create().texOffs(-7, 37).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5593F, 2.7892F, -2.1025F, 0.0037F, -0.2977F, -0.0909F));
      PartDefinition RightSeg3 = RightSeg2.addOrReplaceChild("RightSeg3", CubeListBuilder.create().texOffs(20, 78).mirror().addBox(-8.25F, -0.25F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false).texOffs(0, 58).addBox(-8.25F, 0.75F, 0.0F, 14.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.0F, 0.0F, 0.0F, 0.0F, -0.6109F));
      PartDefinition RightTumors = RightSeg3.addOrReplaceChild("RightTumors", CubeListBuilder.create(), PartPose.offset(-1.0F, 2.0F, 0.0F));
      RightTumors.addOrReplaceChild("Tumor_r4", CubeListBuilder.create().texOffs(0, 84).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8231F, -0.7281F, -0.4329F, 0.1805F, 0.6838F, 0.452F));
      RightTumors.addOrReplaceChild("Tumor_r5", CubeListBuilder.create().texOffs(1, 86).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 0.2182F, -0.3491F));
      PartDefinition RightWingTalons = RightSeg3.addOrReplaceChild("RightWingTalons", CubeListBuilder.create(), PartPose.offsetAndRotation(-8.579F, 0.8498F, 0.2383F, -2.7489F, 0.0F, 0.0F));
      RightWingTalons.addOrReplaceChild("Plane_r9", CubeListBuilder.create().texOffs(99, 50).addBox(0.0F, -1.0F, -2.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1063F, 0.1755F, 0.5051F, -0.553F, 0.0174F, 1.5814F));
      RightWingTalons.addOrReplaceChild("Plane_r10", CubeListBuilder.create().texOffs(99, 50).addBox(0.0F, -1.0F, -2.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3669F, 0.559F, 0.5938F, 3.0669F, 0.0829F, -2.0346F));
      RightWingTalons.addOrReplaceChild("Plane_r11", CubeListBuilder.create().texOffs(99, 50).addBox(0.0F, -1.0F, -1.5F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9114F, -0.6169F, 1.3356F, 2.9361F, 0.5492F, -0.8264F));
      PartDefinition TorsoFlower = UpperTorso.addOrReplaceChild("TorsoFlower", CubeListBuilder.create(), PartPose.offsetAndRotation(1.317F, -3.7718F, -2.615F, 1.609F, -0.1526F, -0.5298F));
      TorsoFlower.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(-7, 37).addBox(-7.0F, 0.0F, -3.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      TorsoFlower.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(-7, 51).addBox(0.0F, 0.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      TorsoFlower.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(-7, 37).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      TorsoFlower.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(-7, 44).addBox(-4.0F, 0.0F, -7.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      PartDefinition UpperTorsoFungus = UpperTorso.addOrReplaceChild("UpperTorsoFungus", CubeListBuilder.create(), PartPose.offset(-4.0593F, -0.4406F, 4.745F));
      UpperTorsoFungus.addOrReplaceChild("Fungus_r7", CubeListBuilder.create().texOffs(-7, 44).addBox(-3.5F, 1.25F, -3.25F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.8685F, -4.5866F, -1.9219F, 0.7634F, 0.1482F, 0.7943F));
      UpperTorsoFungus.addOrReplaceChild("Fungus_r8", CubeListBuilder.create().texOffs(-7, 51).addBox(-3.5F, 0.0F, -2.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5593F, -2.2108F, -2.1025F, -0.2664F, -0.4504F, 0.3859F));
      UpperTorsoFungus.addOrReplaceChild("Fungus_r9", CubeListBuilder.create().texOffs(-7, 51).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5575F, 2.8681F, -0.7511F, -0.3667F, 0.5152F, -0.3599F));
      UpperTorsoFungus.addOrReplaceChild("Fungus_r10", CubeListBuilder.create().texOffs(-7, 44).addBox(-4.5F, -1.0F, -1.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5593F, 2.7892F, -2.1025F, -0.3719F, -0.2779F, -0.4546F));
      PartDefinition TailTumors = LowerTorso.addOrReplaceChild("TailTumors", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.1618F, 0.1664F, 1.4628F, 0.0F, 0.0F, 0.0F));
      TailTumors.addOrReplaceChild("Tumor_r6", CubeListBuilder.create().texOffs(0, 84).addBox(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2559F, 2.4151F, -2.5344F, -0.2516F, 0.9236F, -0.8615F));
      TailTumors.addOrReplaceChild("Tumor_r7", CubeListBuilder.create().texOffs(0, 84).addBox(3.5F, -3.5F, -1.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.5569F, 3.0453F, 2.3457F, 0.3312F, 0.6681F, -0.7794F));
      TailTumors.addOrReplaceChild("Tumor_r8", CubeListBuilder.create().texOffs(0, 84).addBox(-1.0F, -2.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8667F, -1.3456F, 0.831F, 0.9163F, 0.5236F, 0.0F));
      PartDefinition Tail = LowerTorso.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(48, 90).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.5F, 0.6545F, 0.0F, 0.0F));
      PartDefinition Tail2 = Tail.addOrReplaceChild("Tail2", CubeListBuilder.create().texOffs(60, 90).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.5672F, 0.0F, 0.0F));
      PartDefinition Tail2Tumors = Tail2.addOrReplaceChild("Tail2Tumors", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      Tail2Tumors.addOrReplaceChild("Tumor_r9", CubeListBuilder.create().texOffs(2, 86).addBox(-2.0F, -2.5F, -4.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(1.4723F, 4.3913F, 0.1382F, -0.7804F, 0.1267F, -0.4738F));
      Tail2Tumors.addOrReplaceChild("Tumor_r10", CubeListBuilder.create().texOffs(2, 86).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(1.4723F, 4.3913F, 0.1382F, -0.6743F, -1.0812F, 0.1796F));
      Tail2Tumors.addOrReplaceChild("Tumor_r11", CubeListBuilder.create().texOffs(2, 86).addBox(-0.5F, -0.75F, -0.75F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.5F, 5.5F, 0.5F, 0.7681F, -0.7749F, -0.1378F));
      PartDefinition Tail3 = Tail2.addOrReplaceChild("Tail3", CubeListBuilder.create().texOffs(98, 92).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -1.0908F, 0.0F, 0.0F));
      PartDefinition Tail4 = Tail3.addOrReplaceChild("Tail4", CubeListBuilder.create().texOffs(0, 100).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 5.4237F, -0.0535F, -0.9599F, 0.0F, 0.0F));
      PartDefinition Tail5 = Tail4.addOrReplaceChild("Tail5", CubeListBuilder.create().texOffs(104, 74).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.0472F, 0.0F, 0.0F));
      Tail5.addOrReplaceChild("TailTalon_r1", CubeListBuilder.create().texOffs(22, 98).addBox(-2.5F, -5.0F, 0.0F, 5.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, 0.0F, 0.7854F, 0.0F));
      Tail5.addOrReplaceChild("TailTalon_r2", CubeListBuilder.create().texOffs(32, 98).mirror().addBox(-2.5F, -3.5F, 0.0F, 5.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition Legs = scavenger.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition LeftLeg = Legs.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(92, 74).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -11.0F, 3.0F, -0.9599F, 0.0F, 0.0F));
      PartDefinition LeftLegSeg2 = LeftLeg.addOrReplaceChild("LeftLegSeg2", CubeListBuilder.create().texOffs(82, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LeftLegFoot = LeftLegSeg2.addOrReplaceChild("LeftLegFoot", CubeListBuilder.create().texOffs(100, 31).addBox(-1.0F, -1.0F, 0.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 1.0F, 5.0F));
      LeftLegFoot.addOrReplaceChild("FrontTalon3_r1", CubeListBuilder.create().texOffs(100, 24).addBox(0.4617F, -1.692F, -2.384F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 4.25F, -0.75F, 0.7968F, -0.268F, -0.4031F));
      LeftLegFoot.addOrReplaceChild("FrontTalon2_r1", CubeListBuilder.create().texOffs(100, 24).addBox(0.0F, -1.7164F, -2.2737F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 4.25F, -0.75F, 0.7418F, 0.0F, 0.0F));
      LeftLegFoot.addOrReplaceChild("FrontTalon1_r1", CubeListBuilder.create().texOffs(100, 24).addBox(-0.4617F, -1.692F, -2.384F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 4.25F, -0.75F, 0.7968F, 0.268F, 0.4031F));
      LeftLegFoot.addOrReplaceChild("BackTalon_r1", CubeListBuilder.create().texOffs(98, 49).addBox(0.0F, -4.9526F, -2.7891F, 0.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 2.0F, -0.3491F, 0.0F, 0.0F));
      LeftLegFoot.addOrReplaceChild("FrontFoot_r1", CubeListBuilder.create().texOffs(96, 12).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.5F, 2.0F, 1.0F, -0.6109F, 0.0F, 0.0F));
      PartDefinition RightLeg = Legs.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(92, 74).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -11.0F, 3.0F, -0.9599F, 0.0F, 0.0F));
      PartDefinition RightLegSeg2 = RightLeg.addOrReplaceChild("RightLegSeg2", CubeListBuilder.create().texOffs(82, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition RightLegFoot = RightLegSeg2.addOrReplaceChild("RightLegFoot", CubeListBuilder.create().texOffs(100, 31).addBox(-2.0F, -1.0F, 0.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 1.0F, 5.0F));
      RightLegFoot.addOrReplaceChild("FrontTalon3_r2", CubeListBuilder.create().texOffs(100, 24).addBox(-0.4617F, -1.692F, -2.384F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 4.25F, -0.75F, 0.7968F, 0.268F, 0.4031F));
      RightLegFoot.addOrReplaceChild("FrontTalon2_r2", CubeListBuilder.create().texOffs(100, 24).addBox(0.0F, -1.7164F, -2.2737F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 4.25F, -0.75F, 0.7418F, 0.0F, 0.0F));
      RightLegFoot.addOrReplaceChild("FrontTalon1_r2", CubeListBuilder.create().texOffs(100, 24).addBox(0.4617F, -1.692F, -2.384F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 4.25F, -0.75F, 0.7968F, -0.268F, -0.4031F));
      RightLegFoot.addOrReplaceChild("BackTalon_r2", CubeListBuilder.create().texOffs(98, 49).addBox(0.0F, -4.9526F, -2.7891F, 0.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 2.0F, -0.3491F, 0.0F, 0.0F));
      RightLegFoot.addOrReplaceChild("FrontFoot_r2", CubeListBuilder.create().texOffs(96, 12).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-0.5F, 2.0F, 1.0F, -0.6109F, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   private void animateState(AnimationDefinition definition, float value, float multiplies, boolean active) {
      if (active) {
         long movement = (long)(value * 30.0F * multiplies);
         KeyframeAnimations.animate(this, definition, movement, 1.0F, this.ANIMATION_VECTOR_CACHE);
      }

   }

   public void setupAnim(Scavenger entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      float movementValue = Mth.cos(limbSwing * 0.6F) * 0.6F * limbSwingAmount;
      float idleMovementValue = Mth.cos(ageInTicks / 6.0F) / 6.0F;
      float headSpin = netHeadYaw / (180F / (float)Math.PI) / 3.0F;
      if (!entity.onGround() && !entity.isInFluidType()) {
         this.animateState(ScavengerAnimations.FLIGHT, ageInTicks, 1.0F, !entity.onGround());
         this.leftLeg.xRot = idleMovementValue;
         this.rightLeg.xRot = -idleMovementValue;
      } else {
         this.leftLeg.xRot = movementValue;
         this.rightLeg.xRot = -movementValue;
         this.animateTentacleZ(this.rightWing, -1.3F - idleMovementValue);
         this.animateTentacleZ(this.leftWing, 1.3F + idleMovementValue);
      }

      this.animateTumor(this.Tumor1, Mth.cos(ageInTicks / 7.0F) / 7.0F);
      this.animateTumor(this.Tumor2, Mth.sin(ageInTicks / 6.0F) / 7.0F);
      this.animateTumor(this.Tumor3, Mth.sin(ageInTicks / 7.0F) / 6.0F);
      this.animateTumor(this.Tumor4, Mth.cos(ageInTicks / 6.0F) / 7.0F);
      this.animateTentacleX(this.tail1, Mth.cos(ageInTicks / 7.0F) / 5.0F);
      this.animateTentacleX(this.tail2, Mth.cos(ageInTicks / 7.0F) / 4.0F);
      this.animateTentacleX(this.tail3, Mth.cos(ageInTicks / 8.0F) / 5.0F);
      this.animateTentacleX(this.tail4, Mth.cos(ageInTicks / 7.0F) / 4.0F);
      this.animateTentacleX(this.tail5, Mth.cos(ageInTicks / 6.0F) / 5.0F);
      this.leftForLeg.xRot = this.leftLeg.xRot < 0.0F ? -this.leftLeg.xRot : 0.0F;
      this.rightForLeg.xRot = this.rightLeg.xRot < 0.0F ? -this.rightLeg.xRot : 0.0F;
      this.Neck1.zRot = headSpin;
      this.Neck2.zRot = headSpin;
      this.Head.zRot = headSpin;
      this.animateTentacleX(this.Jaw, -idleMovementValue);
      this.animateTentacleX(this.Fang1, idleMovementValue);
      this.animateTentacleX(this.Fang2, idleMovementValue);
      this.animateTentacleX(this.Fang3, -idleMovementValue);
      this.animateTentacleX(this.Fang4, -idleMovementValue);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.scavenger.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   public ModelPart root() {
      return this.scavenger;
   }
}
