package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Busser;
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

public class RangedBusserModel extends EntityModel<Busser> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "rangedbussermodel"), "main");
   private final ModelPart busser;
   private final ModelPart head;
   private final ModelPart jaw;
   private final ModelPart mouth_tendrils;
   private final ModelPart headtendril1;
   private final ModelPart headtendril2;
   private final ModelPart headtendril3;
   private final ModelPart RightArm;
   private final ModelPart RightForArm;
   private final ModelPart LeftArm;
   private final ModelPart LeftForArm;
   private final ModelPart Tail1;
   private final ModelPart Tail2;
   private final ModelPart Tail3;
   private final ModelPart Tail4;
   private final ModelPart Claws1;
   private final ModelPart Claws2;
   private final ModelPart ShooterSmall;
   private final ModelPart ShooterSmall2;
   private final ModelPart ShooterBig;

   public RangedBusserModel(ModelPart root) {
      this.busser = root.getChild("busser");
      this.head = this.busser.getChild("body").getChild("head");
      this.jaw = this.head.getChild("jaw");
      this.mouth_tendrils = this.head.getChild("HeadStingers").getChild("MouthStinger");
      this.headtendril1 = this.head.getChild("HeadStingers").getChild("HeadStinger1");
      this.headtendril2 = this.head.getChild("HeadStingers").getChild("HeadStinger2");
      this.headtendril3 = this.head.getChild("HeadStingers").getChild("HeadStinger3");
      this.RightArm = this.busser.getChild("body").getChild("RightArm");
      this.RightForArm = this.RightArm.getChild("RightForArm");
      this.LeftArm = this.busser.getChild("body").getChild("LeftArm");
      this.LeftForArm = this.LeftArm.getChild("LeftForArm");
      this.Tail1 = this.busser.getChild("Tail");
      this.Tail2 = this.Tail1.getChild("Tail2");
      this.Tail3 = this.Tail2.getChild("Tail3");
      this.Tail4 = this.Tail3.getChild("Tail4");
      this.Claws1 = this.Tail1.getChild("claws");
      this.Claws2 = this.Tail1.getChild("claws2");
      this.ShooterSmall = this.Tail3.getChild("SubHole2");
      this.ShooterSmall2 = this.Tail4.getChild("StingerHole").getChild("SubHole1");
      this.ShooterBig = this.Tail4.getChild("StingerHole").getChild("MainHole");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition busser = partdefinition.addOrReplaceChild("busser", CubeListBuilder.create(), PartPose.offset(0.0F, 25.0F, -4.0F));
      PartDefinition body = busser.addOrReplaceChild("body", CubeListBuilder.create().texOffs(50, 19).addBox(-3.0F, -2.0F, -1.75F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -13.0F, 2.0F));
      body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 49).addBox(0.0F, -1.0F, 0.0F, 0.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.2182F, 0.0F, 0.0F));
      PartDefinition spine = body.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(28, 42).addBox(-2.0F, -2.5F, -4.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(-0.5F)).texOffs(22, 26).addBox(-3.0F, -4.5F, -5.2F, 6.0F, 3.0F, 6.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -4.0F, 2.0F, 0.1309F, 0.0F, 0.0F));
      spine.addOrReplaceChild("back", CubeListBuilder.create().texOffs(8, 56).addBox(0.0F, -4.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(1.0F, 0.0F, -2.0F, 0.0F, 0.2618F, 0.0F));
      spine.addOrReplaceChild("back2", CubeListBuilder.create().texOffs(54, 46).addBox(-1.0F, -4.0F, -2.25F, 2.0F, 6.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.0F, 0.0F, -2.0F, 0.0F, -0.2618F, 0.0F));
      PartDefinition spine2 = body.addOrReplaceChild("spine2", CubeListBuilder.create().texOffs(22, 14).addBox(-4.0F, -5.5F, -5.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.0F, -8.0F, 1.25F, 0.2618F, 0.0F, 0.0F));
      spine2.addOrReplaceChild("back3", CubeListBuilder.create().texOffs(44, 10).addBox(0.0F, -4.0F, -2.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -1.0F, -2.0F, 0.0F, 0.2618F, 0.0F));
      spine2.addOrReplaceChild("back4", CubeListBuilder.create().texOffs(0, 44).addBox(-3.0F, -4.0F, -2.25F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, -2.0F, 0.0F, -0.2618F, 0.0F));
      PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(-0.01F)).texOffs(18, 35).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(38, 37).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, -5.0F));
      PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));
      jaw.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(40, 26).addBox(-3.5F, 1.0F, -3.75F, 7.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      PartDefinition flower2 = head.addOrReplaceChild("flower2", CubeListBuilder.create(), PartPose.offsetAndRotation(1.9696F, -6.2209F, -0.8251F, 0.6981F, 0.0F, 0.4363F));
      flower2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(74, 6).addBox(-6.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(74, 6).addBox(0.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      PartDefinition flower3 = head.addOrReplaceChild("flower3", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.25F, -5.75F, 1.0F, -0.1309F, 0.0F, -0.5672F));
      flower3.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(74, 6).addBox(-6.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(74, 6).addBox(0.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      PartDefinition flower4 = head.addOrReplaceChild("flower4", CubeListBuilder.create(), PartPose.offsetAndRotation(1.75F, -6.75F, 0.0F, 0.0F, 0.0F, 0.0F));
      flower4.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(74, 6).addBox(-6.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower4.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(74, 6).addBox(0.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower4.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower4.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      PartDefinition HeadStingers = head.addOrReplaceChild("HeadStingers", CubeListBuilder.create(), PartPose.offset(-1.1804F, -8.3262F, -1.3431F));
      PartDefinition HeadStinger1 = HeadStingers.addOrReplaceChild("HeadStinger1", CubeListBuilder.create().texOffs(77, 20).addBox(-1.6337F, -0.4636F, -0.8437F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.829F, 0.0F));
      HeadStinger1.addOrReplaceChild("EastWall_r1", CubeListBuilder.create().texOffs(2, -1).addBox(-1.0F, -2.9962F, -1.4128F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6163F, 1.5364F, -0.0937F, 0.0873F, 0.0F, 0.4363F));
      HeadStinger1.addOrReplaceChild("NorthWall_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -1.0F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1337F, 0.83F, -1.5918F, 0.0873F, 0.0F, 0.0F));
      HeadStinger1.addOrReplaceChild("SouthWall_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1337F, 0.83F, 1.5825F, -0.0873F, 0.2182F, 0.0F));
      HeadStinger1.addOrReplaceChild("WestWall_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -2.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0958F, 1.0816F, -0.0501F, 0.0011F, 0.0608F, -0.2747F));
      PartDefinition MainStinger2 = HeadStinger1.addOrReplaceChild("MainStinger2", CubeListBuilder.create().texOffs(79, 17).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.1396F, 1.5364F, -0.1478F, 0.0F, 0.2182F, 0.0F));
      MainStinger2.addOrReplaceChild("StingerPlane_r1", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.3F, 0.0F, 0.0F, 2.3562F, 0.0F));
      MainStinger2.addOrReplaceChild("StingerPlane_r2", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -3.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(81, 18).addBox(-0.5F, -1.8F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      PartDefinition HeadStinger2 = HeadStingers.addOrReplaceChild("HeadStinger2", CubeListBuilder.create().texOffs(77, 20).addBox(-1.6337F, -0.4636F, -0.8437F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6695F, -0.0613F, 0.38F, -2.2175F, 1.5316F, -1.7129F));
      HeadStinger2.addOrReplaceChild("EastWall_r2", CubeListBuilder.create().texOffs(2, -1).addBox(-1.0F, -2.9962F, -1.4128F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6163F, 1.5364F, -0.0937F, 0.0873F, 0.0F, 0.4363F));
      HeadStinger2.addOrReplaceChild("NorthWall_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -1.0F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1337F, 0.83F, -1.5918F, 0.0873F, 0.0F, 0.0F));
      HeadStinger2.addOrReplaceChild("SouthWall_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1337F, 0.83F, 1.5825F, -0.0873F, 0.2182F, 0.0F));
      HeadStinger2.addOrReplaceChild("WestWall_r2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -2.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0958F, 1.0816F, -0.0501F, 0.0011F, 0.0608F, -0.2747F));
      PartDefinition MainStinger3 = HeadStinger2.addOrReplaceChild("MainStinger3", CubeListBuilder.create().texOffs(79, 17).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.1396F, 1.5364F, -0.1478F, 0.0F, 0.2182F, 0.0F));
      MainStinger3.addOrReplaceChild("StingerPlane_r3", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.3F, 0.0F, 0.0F, 2.3562F, 0.0F));
      MainStinger3.addOrReplaceChild("StingerPlane_r4", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -3.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(81, 18).addBox(-0.5F, -1.8F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      PartDefinition HeadStinger3 = HeadStingers.addOrReplaceChild("HeadStinger3", CubeListBuilder.create().texOffs(77, 20).addBox(-1.6337F, -0.4636F, -0.8437F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5591F, 0.1887F, 3.2927F, -0.46F, 0.3698F, 0.0684F));
      HeadStinger3.addOrReplaceChild("EastWall_r3", CubeListBuilder.create().texOffs(2, -1).addBox(-1.0F, -2.9962F, -1.4128F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6163F, 1.5364F, -0.0937F, 0.0873F, 0.0F, 0.4363F));
      HeadStinger3.addOrReplaceChild("NorthWall_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -1.0F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1337F, 0.83F, -1.5918F, 0.0873F, 0.0F, 0.0F));
      HeadStinger3.addOrReplaceChild("SouthWall_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1337F, 0.83F, 1.5825F, -0.0873F, 0.2182F, 0.0F));
      HeadStinger3.addOrReplaceChild("WestWall_r3", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -2.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0958F, 1.0816F, -0.0501F, 0.0011F, 0.0608F, -0.2747F));
      PartDefinition MainStinger4 = HeadStinger3.addOrReplaceChild("MainStinger4", CubeListBuilder.create().texOffs(79, 17).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.1396F, 1.5364F, -0.1478F, 0.0F, 0.2182F, 0.0F));
      MainStinger4.addOrReplaceChild("StingerPlane_r5", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.3F, 0.0F, 0.0F, 2.3562F, 0.0F));
      MainStinger4.addOrReplaceChild("StingerPlane_r6", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -3.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(81, 18).addBox(-0.5F, -1.8F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      PartDefinition MouthStinger = HeadStingers.addOrReplaceChild("MouthStinger", CubeListBuilder.create().texOffs(76, 20).addBox(-2.6337F, -0.4636F, -0.8437F, 5.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2663F, 6.8389F, 0.7376F, 1.9274F, -0.0982F, 0.068F));
      MouthStinger.addOrReplaceChild("EastWall_r4", CubeListBuilder.create().texOffs(2, -1).addBox(-1.5F, -2.9962F, -1.4128F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6163F, 1.5364F, -0.0937F, 0.0873F, 0.0F, 0.4363F));
      MouthStinger.addOrReplaceChild("NorthWall_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -2.0F, 0.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1337F, 0.83F, -1.5918F, 0.0873F, 0.0F, 0.0F));
      MouthStinger.addOrReplaceChild("SouthWall_r4", CubeListBuilder.create().texOffs(0, 1).addBox(-3.5F, -2.0F, -1.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1337F, 0.83F, 1.5825F, -0.0873F, 0.2182F, 0.0F));
      MouthStinger.addOrReplaceChild("WestWall_r4", CubeListBuilder.create().texOffs(1, 1).mirror().addBox(-1.0F, -2.5F, -1.25F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0958F, 1.0816F, -0.0501F, 0.0011F, 0.0608F, -0.2747F));
      PartDefinition MouthStinger1 = MouthStinger.addOrReplaceChild("MouthStinger1", CubeListBuilder.create().texOffs(79, 17).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.1396F, 1.5364F, -0.1478F, 0.0479F, 0.2129F, 0.2233F));
      MouthStinger1.addOrReplaceChild("StingerPlane_r7", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.3F, 0.0F, 0.0F, 2.3562F, 0.0F));
      MouthStinger1.addOrReplaceChild("StingerPlane_r8", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -3.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(81, 18).addBox(-0.5F, -1.8F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      PartDefinition MouthStinger2 = MouthStinger.addOrReplaceChild("MouthStinger2", CubeListBuilder.create().texOffs(79, 17).addBox(-0.5766F, -1.9667F, -1.2129F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.3978F, -0.0188F, -0.4447F, -0.4005F, -0.614F, -0.305F));
      MouthStinger2.addOrReplaceChild("StingerPlane_r9", CubeListBuilder.create().texOffs(73, 22).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4234F, -3.7667F, -0.2129F, 0.0F, 2.3562F, 0.0F));
      MouthStinger2.addOrReplaceChild("StingerPlane_r10", CubeListBuilder.create().texOffs(73, 22).addBox(-0.5F, -3.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(81, 18).addBox(-0.5F, -1.8F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4234F, -1.4667F, -0.2129F, 0.0F, 0.7854F, 0.0F));
      PartDefinition MouthStinger3 = MouthStinger.addOrReplaceChild("MouthStinger3", CubeListBuilder.create().texOffs(79, 17).addBox(-0.5766F, -1.9667F, -1.2129F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-1.1213F, 0.0456F, 0.159F, 0.0964F, 0.32F, -0.1463F));
      MouthStinger3.addOrReplaceChild("StingerPlane_r11", CubeListBuilder.create().texOffs(73, 22).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4234F, -3.7667F, -0.2129F, 0.0F, 2.3562F, 0.0F));
      MouthStinger3.addOrReplaceChild("StingerPlane_r12", CubeListBuilder.create().texOffs(73, 22).addBox(-0.5F, -3.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(81, 18).addBox(-0.5F, -1.8F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4234F, -1.4667F, -0.2129F, 0.0F, 0.7854F, 0.0F));
      PartDefinition flower = body.addOrReplaceChild("flower", CubeListBuilder.create(), PartPose.offsetAndRotation(1.75F, -9.75F, 0.0F, -1.1345F, -0.3491F, 0.1309F));
      flower.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(74, 6).addBox(-6.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(74, 6).addBox(0.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      PartDefinition RightArm = body.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(57, 9).addBox(-2.0F, -1.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(58, 32).addBox(-1.5F, 2.0F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 29).addBox(-1.0F, -1.0F, 1.0F, 0.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -10.0F, -2.0F));
      PartDefinition RightForArm = RightArm.addOrReplaceChild("RightForArm", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offset(-0.5F, 6.75F, 0.0F));
      RightForArm.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(46, 36).addBox(0.01F, -5.0F, -0.25F, 0.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 3.25F, 1.0F, -0.2182F, 0.0F, 0.0F));
      RightForArm.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(22, 14).addBox(0.0F, 0.0F, 0.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
      PartDefinition RightHand = RightForArm.addOrReplaceChild("RightHand", CubeListBuilder.create().texOffs(34, 60).addBox(0.0F, -0.25F, -0.75F, 1.0F, 7.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(-1.0F, 5.75F, 0.0F));
      RightHand.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(30, 60).addBox(0.0F, 0.0F, -1.75F, 1.0F, 7.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F));
      RightHand.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(8, 53).addBox(0.0F, 0.0F, -0.25F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));
      RightHand.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 3).addBox(0.5F, -4.0F, -5.0F, 0.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
      PartDefinition LeftArm = body.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(59, 40).addBox(-1.0F, -1.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(20, 60).addBox(-0.5F, 2.0F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(14, 34).addBox(1.0F, -1.0F, 1.0F, 0.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -10.0F, -2.0F));
      PartDefinition LeftForArm = LeftArm.addOrReplaceChild("LeftForArm", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.5F, 6.75F, 0.0F));
      LeftForArm.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(42, 46).addBox(1.01F, -5.0F, -0.25F, 0.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 3.25F, 1.0F, -0.2182F, 0.0F, 0.0F));
      LeftForArm.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(22, 26).addBox(0.0F, 0.0F, 0.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));
      PartDefinition LeftHand = LeftForArm.addOrReplaceChild("LeftHand", CubeListBuilder.create().texOffs(42, 60).addBox(0.0F, -0.25F, -0.75F, 1.0F, 7.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, 5.75F, 0.0F));
      LeftHand.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(38, 60).addBox(0.0F, 0.0F, -1.75F, 1.0F, 7.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F));
      LeftHand.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(46, 60).addBox(0.0F, 0.0F, -0.25F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));
      LeftHand.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(0, 14).addBox(0.5F, -4.0F, -5.0F, 0.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
      PartDefinition Tail = busser.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, 0.0F, -1.75F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.0F, 2.0F, 0.2618F, 0.0F, 0.0F));
      PartDefinition Tail2 = Tail.addOrReplaceChild("Tail2", CubeListBuilder.create().texOffs(52, 0).addBox(-2.5F, 0.0F, -1.25F, 5.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      PartDefinition Tail3 = Tail2.addOrReplaceChild("Tail3", CubeListBuilder.create().texOffs(52, 0).addBox(-2.5F, 0.0F, -1.25F, 5.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.6981F, 0.0F, 0.0F));
      PartDefinition SubHole2 = Tail3.addOrReplaceChild("SubHole2", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, 3.6155F, 0.4664F, -0.1309F, 0.0F, -0.2618F));
      SubHole2.addOrReplaceChild("Center_r1", CubeListBuilder.create().texOffs(74, 20).mirror().addBox(-1.0F, 0.0F, -2.0F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 0.6345F, 0.5336F, 0.0F, 0.0F, 0.8029F));
      SubHole2.addOrReplaceChild("BackWall_r1", CubeListBuilder.create().texOffs(5, 2).mirror().addBox(-1.5F, -1.5F, -0.25F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0651F, 0.406F, 1.0012F, 0.2996F, -0.3249F, 0.8729F));
      SubHole2.addOrReplaceChild("FrontWall_r1", CubeListBuilder.create().texOffs(5, 2).mirror().addBox(-1.5F, -2.5F, -0.5F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.9329F, -0.2966F, -1.0562F, -0.2506F, 0.0873F, 0.828F));
      SubHole2.addOrReplaceChild("ExternalWall_r1", CubeListBuilder.create().texOffs(6, 1).mirror().addBox(-0.5F, -2.5F, -1.5F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5598F, -0.7299F, 0.2836F, 0.0215F, -0.2139F, 0.9375F));
      PartDefinition SubHole1Stinger2 = SubHole2.addOrReplaceChild("SubHole1Stinger2", CubeListBuilder.create().texOffs(79, 17).mirror().addBox(-1.0F, -3.6F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(-1.8713F, 2.6041F, 0.0938F, 0.0F, 0.6109F, 0.8029F));
      SubHole1Stinger2.addOrReplaceChild("StingerPlane_r13", CubeListBuilder.create().texOffs(79, 22).mirror().addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, 0.0F, 2.3562F, 0.0F));
      SubHole1Stinger2.addOrReplaceChild("StingerPlane_r14", CubeListBuilder.create().texOffs(81, 22).mirror().addBox(-0.5F, 1.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(81, 18).mirror().addBox(-0.5F, -0.2F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.1F, 0.0F, 0.0F, 0.7854F, 0.0F));
      PartDefinition Tail4 = Tail3.addOrReplaceChild("Tail4", CubeListBuilder.create().texOffs(52, 0).addBox(-1.5F, 0.0F, -1.25F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 6.0F, 0.0F, 0.2618F, 0.0F, 0.0F));
      PartDefinition StingerHole = Tail4.addOrReplaceChild("StingerHole", CubeListBuilder.create().texOffs(-4, 1).addBox(-1.5F, 6.0F, -1.75F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));
      StingerHole.addOrReplaceChild("BottomSegment_r1", CubeListBuilder.create().texOffs(-1, 4).addBox(-2.0F, -5.5F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.49F, 7.5524F, -1.4501F, -0.1309F, 0.0F, 0.0F));
      StingerHole.addOrReplaceChild("TopSegment_r1", CubeListBuilder.create().texOffs(-1, 4).addBox(-1.99F, -5.5F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 7.5524F, 1.9501F, 0.1309F, 0.0F, 0.0F));
      PartDefinition MainHole = StingerHole.addOrReplaceChild("MainHole", CubeListBuilder.create().texOffs(77, 20).addBox(-3.25F, 2.0F, -0.75F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.25F, 7.2898F, 0.4109F));
      MainHole.addOrReplaceChild("EastWall_r5", CubeListBuilder.create().texOffs(2, -1).addBox(-1.0F, -2.0038F, -1.4128F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, -0.4363F));
      MainHole.addOrReplaceChild("NorthWall_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -1.0F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, 0.7064F, -1.4981F, -0.0873F, 0.0F, 0.0F));
      MainHole.addOrReplaceChild("SouthWall_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, 0.7064F, 1.6762F, 0.0873F, 0.2182F, 0.0F));
      MainHole.addOrReplaceChild("WestWall_r5", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -2.5F, -2.5F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.7121F, 0.4549F, 0.0436F, -0.0011F, 0.0608F, 0.2747F));
      PartDefinition SideStingers = MainHole.addOrReplaceChild("SideStingers", CubeListBuilder.create(), PartPose.offset(-2.0F, 1.0F, 0.4281F));
      SideStingers.addOrReplaceChild("Stinger_r1", CubeListBuilder.create().texOffs(105, 5).mirror().addBox(-0.5F, -2.0F, -1.75F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.4193F, 0.8413F, -0.5389F));
      SideStingers.addOrReplaceChild("Stinger_r2", CubeListBuilder.create().texOffs(105, -3).addBox(-0.25F, -2.0F, -1.5F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 1.0F, -0.4281F, 2.8245F, -0.7798F, -2.7049F));
      PartDefinition MainStinger = MainHole.addOrReplaceChild("MainStinger", CubeListBuilder.create().texOffs(79, 17).addBox(-1.0F, 1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-1.7559F, 0.0F, -0.0541F, 0.0F, 0.2182F, 0.0F));
      MainStinger.addOrReplaceChild("StingerPlane_r15", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.3F, 0.0F, 0.0F, 2.3562F, 0.0F));
      MainStinger.addOrReplaceChild("StingerPlane_r16", CubeListBuilder.create().texOffs(89, 22).addBox(-0.5F, 1.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(81, 18).addBox(-0.5F, -0.2F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      PartDefinition SubHole1 = StingerHole.addOrReplaceChild("SubHole1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      SubHole1.addOrReplaceChild("Center_r2", CubeListBuilder.create().texOffs(74, 20).addBox(-2.0F, 0.0F, -1.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 5.25F, 0.0F, 0.0F, 0.0F, -0.8029F));
      SubHole1.addOrReplaceChild("FrontWall_r2", CubeListBuilder.create().texOffs(5, 2).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0671F, 4.3189F, -0.4103F, -0.4932F, 0.1357F, -1.1254F));
      SubHole1.addOrReplaceChild("BackWall_r2", CubeListBuilder.create().texOffs(5, 2).addBox(-2.0F, -2.5F, -0.5F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.399F, 4.035F, 1.3463F, 0.2868F, 0.5085F, -0.7077F));
      SubHole1.addOrReplaceChild("ExternalWall_r2", CubeListBuilder.create().texOffs(5, 0).addBox(-0.5F, -2.5F, -1.5F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5598F, 3.8856F, 0.25F, 0.014F, 0.2665F, -0.8121F));
      PartDefinition SubHole1Stinger = SubHole1.addOrReplaceChild("SubHole1Stinger", CubeListBuilder.create().texOffs(79, 17).addBox(-1.0F, -3.6F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(4.8714F, 7.2196F, 0.4397F, 0.0F, 0.6109F, -0.8029F));
      SubHole1Stinger.addOrReplaceChild("StingerPlane_r17", CubeListBuilder.create().texOffs(79, 22).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, 0.0F, 2.3562F, 0.0F));
      SubHole1Stinger.addOrReplaceChild("StingerPlane_r18", CubeListBuilder.create().texOffs(81, 22).addBox(-0.5F, 1.3F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(81, 18).addBox(-0.5F, -0.2F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.1F, 0.0F, 0.0F, 0.7854F, 0.0F));
      Tail.addOrReplaceChild("claws", CubeListBuilder.create().texOffs(96, -6).addBox(0.0F, 0.0F, -4.0F, 0.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 6.0F, 0.0F, 0.0F, 0.0F, -0.2618F));
      Tail.addOrReplaceChild("claws2", CubeListBuilder.create().texOffs(114, -7).addBox(0.0F, 0.0F, -4.0F, 0.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.2618F));
      PartDefinition flower5 = Tail.addOrReplaceChild("flower5", CubeListBuilder.create(), PartPose.offsetAndRotation(1.75F, 2.25F, 0.0F, -1.309F, -0.3491F, 0.0F));
      flower5.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(74, 6).addBox(-6.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower5.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(74, 6).addBox(0.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower5.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower5.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void ScaleUpArm(ModelPart part) {
      part.xScale = 1.65F;
      part.yScale = 1.65F;
      part.zScale = 1.65F;
   }

   public void setupAnim(Busser entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.head.xRot = headPitch / 28.647888F;
      this.jaw.xRot = Mth.sin(ageInTicks / 8.0F) / 6.0F;
      this.headtendril1.yScale = 1.0F + Mth.sin(ageInTicks / 8.0F) / 10.0F;
      this.headtendril2.yScale = 1.0F + Mth.cos(ageInTicks / 8.0F) / 7.0F;
      this.headtendril3.yScale = 1.0F + Mth.sin(ageInTicks / 8.0F) / 9.0F;
      this.mouth_tendrils.zScale = 1.0F + Mth.cos(ageInTicks / 6.0F) / 7.0F;
      this.ScaleUpArm(this.RightArm);
      this.ScaleUpArm(this.LeftArm);
      this.Claws1.xRot = Mth.sin(ageInTicks / 4.0F) / 3.0F;
      this.Claws2.xRot = Mth.sin(ageInTicks / 4.0F) / 2.0F;
      this.ShooterSmall.zRot = Mth.sin(ageInTicks / 6.0F) / 6.0F;
      this.ShooterSmall2.zRot = Mth.cos(ageInTicks / 6.0F) / 6.0F;
      this.ShooterBig.yScale = 1.5F + Mth.sin(ageInTicks / 7.0F) / 5.0F;
      if (!entity.getBlockStateOn().isAir()) {
         this.Tail1.yRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.Tail2.yRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.Tail3.yRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.Tail4.yRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
         this.Tail1.xRot = 0.4F;
         this.Tail2.xRot = 0.4F;
         this.Tail3.xRot = 0.4F;
         this.Tail4.xRot = 0.4F;
         this.RightArm.zRot = Mth.sin(ageInTicks / 5.0F) / 6.0F;
         this.LeftArm.zRot = -Mth.sin(ageInTicks / 5.0F) / 6.0F;
         this.RightForArm.zRot = this.RightArm.zRot < 0.0F ? Mth.sin(ageInTicks / 3.0F) / 7.0F : 0.0F;
         this.LeftForArm.zRot = this.LeftArm.zRot < 0.0F ? Mth.sin(ageInTicks / 3.0F) / 7.0F : 0.0F;
      } else {
         this.Tail1.yRot = 0.0F;
         this.Tail2.yRot = 0.0F;
         this.Tail3.yRot = 0.0F;
         this.Tail4.yRot = 0.0F;
         this.RightArm.zRot = 1.1F + Mth.sin(ageInTicks / 3.0F);
         this.LeftArm.zRot = -1.1F - Mth.sin(ageInTicks / 3.0F);
         this.RightForArm.zRot = this.RightArm.zRot > 0.0F ? Mth.sin(ageInTicks / 3.0F) / 2.0F : 0.0F;
         this.LeftForArm.zRot = this.LeftArm.zRot > 0.0F ? Mth.sin(ageInTicks / 3.0F) / 2.0F : 0.0F;
         if (entity.isVehicle()) {
            this.Tail1.xRot = 0.0F;
            this.Tail2.xRot = 0.0F;
            this.Tail3.xRot = 0.0F;
         } else {
            this.Tail1.xRot = -0.3F + Mth.sin(ageInTicks / 3.0F) / 6.0F;
            this.Tail2.xRot = -0.3F + Mth.sin(ageInTicks / 3.0F) / 6.0F;
            this.Tail3.xRot = -0.3F + Mth.sin(ageInTicks / 3.0F) / 6.0F;
         }

         this.Tail4.xRot = -0.3F + Mth.sin(ageInTicks / 3.0F) / 6.0F;
      }

   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.busser.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
