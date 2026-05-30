package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Slasher;
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

public class SmasherSlasherModel extends EntityModel<Slasher> implements TentacledModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "smasherslashermodel"), "main");
   private final ModelPart smash;
   private final ModelPart body;
   private final ModelPart LeftArm;
   private final ModelPart LeftForArm;
   private final ModelPart RightLeg;
   private final ModelPart rightForLeg;
   private final ModelPart LeftLeg;
   private final ModelPart leftForLeg;
   private final ModelPart HeadJoint;
   private final ModelPart head;
   private final ModelPart nose;
   private final ModelPart jaw;
   private final ModelPart Marm;
   private final ModelPart MarmJoint;
   private final ModelPart MArm2;
   private final ModelPart MarmJoint2;
   private final ModelPart clump;
   private final ModelPart tumors;
   private final ModelPart spikes;
   private final ModelPart thorn2;
   private final ModelPart thorn3;
   private final ModelPart thorn4;
   private final ModelPart thorn5;
   private final ModelPart thorn6;
   private final ModelPart thorn7;
   private final ModelPart thorn8;
   private final ModelPart thorn9;
   private final ModelPart thorn10;
   private final ModelPart thorn11;
   private final ModelPart thorn12;

   public SmasherSlasherModel(ModelPart root) {
      this.smash = root.getChild("smash");
      this.body = this.smash.getChild("body");
      this.LeftArm = this.smash.getChild("LeftArm");
      this.LeftForArm = this.LeftArm.getChild("LeftForArm");
      this.RightLeg = this.smash.getChild("RightLeg");
      this.rightForLeg = this.RightLeg.getChild("rightForLeg");
      this.LeftLeg = this.smash.getChild("LeftLeg");
      this.leftForLeg = this.LeftLeg.getChild("leftForLeg");
      this.HeadJoint = this.smash.getChild("HeadJoint");
      this.head = this.HeadJoint.getChild("head");
      this.nose = this.head.getChild("nose");
      this.jaw = this.head.getChild("jaw");
      this.Marm = this.smash.getChild("Marm");
      this.MarmJoint = this.Marm.getChild("MarmJoint");
      this.MArm2 = this.MarmJoint.getChild("MArm2");
      this.MarmJoint2 = this.MArm2.getChild("MarmJoint2");
      this.clump = this.MarmJoint2.getChild("clump");
      this.tumors = this.clump.getChild("tumors");
      this.spikes = this.clump.getChild("spikes");
      this.thorn2 = this.spikes.getChild("thorn2");
      this.thorn3 = this.spikes.getChild("thorn3");
      this.thorn4 = this.spikes.getChild("thorn4");
      this.thorn5 = this.spikes.getChild("thorn5");
      this.thorn6 = this.spikes.getChild("thorn6");
      this.thorn7 = this.spikes.getChild("thorn7");
      this.thorn8 = this.spikes.getChild("thorn8");
      this.thorn9 = this.spikes.getChild("thorn9");
      this.thorn10 = this.spikes.getChild("thorn10");
      this.thorn11 = this.spikes.getChild("thorn11");
      this.thorn12 = this.spikes.getChild("thorn12");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition smash = partdefinition.addOrReplaceChild("smash", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition body = smash.addOrReplaceChild("body", CubeListBuilder.create().texOffs(36, 21).addBox(-4.0F, 9.0F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(59, 41).addBox(-0.5F, -4.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 75).addBox(-3.0F, -9.0F, -5.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, 0.0F));
      body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(36, 12).addBox(-4.0F, -2.5F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.0436F, 0.0F, 0.0F));
      body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(18, 66).addBox(-16.0F, -32.0F, -4.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.4363F));
      body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(-7, 87).addBox(-5.0F, -24.0F, -1.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -0.1309F));
      body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(-7, 87).addBox(-2.0F, -22.0F, 7.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.6109F, 0.0F, -0.1309F));
      body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(-7, 87).addBox(-3.0F, -29.0F, -6.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, -0.1631F, 0.0227F, 0.1289F));
      body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(-7, 87).addBox(-1.0F, -33.0F, -12.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, -0.3054F, 0.0F, 0.0F));
      body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(-7, 87).addBox(-4.0F, -32.0F, -4.0F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.2618F));
      body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(28, 47).addBox(-1.5F, 6.5F, -3.25F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(32, 0).addBox(-4.5F, -1.0F, -4.0F, 8.0F, 5.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.5F, -2.0F, 0.0F, 0.0F, 0.0F, 0.7854F));
      body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(0, 34).addBox(-6.0F, -11.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.1F)).texOffs(28, 34).addBox(-4.0F, -7.0F, -3.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, -0.25F, 0.0436F, 0.0F, 0.0F));
      smash.addOrReplaceChild("bodywear", CubeListBuilder.create().texOffs(75, 0).addBox(-4.0F, 0.0F, -3.25F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -24.0F, -0.25F, 0.0436F, 0.0F, 0.0F));
      PartDefinition LeftArm = smash.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(62, 50).addBox(-1.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -22.0F, 0.0F));
      PartDefinition LeftForArm = LeftArm.addOrReplaceChild("LeftForArm", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
      LeftForArm.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(61, 18).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
      PartDefinition RightLeg = smash.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(32, 56).addBox(-1.75F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, -12.0F, 0.0F));
      RightLeg.addOrReplaceChild("rightForLeg", CubeListBuilder.create().texOffs(56, 30).addBox(-1.75F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 6.0F, 0.0F));
      PartDefinition LeftLeg = smash.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(16, 55).addBox(-1.25F, 0.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, -12.0F, 0.0F));
      LeftLeg.addOrReplaceChild("leftForLeg", CubeListBuilder.create().texOffs(0, 55).addBox(-1.25F, 1.0F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 5.0F, 0.0F));
      PartDefinition HeadJoint = smash.addOrReplaceChild("HeadJoint", CubeListBuilder.create(), PartPose.offset(3.0F, -27.0F, 0.0F));
      PartDefinition head = HeadJoint.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -2.0F, -4.5F, 7.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-4.0F, -10.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(55, 0).addBox(-4.0F, -2.0F, 1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(24, 1).addBox(-3.5F, -2.0F, -4.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(24, 0).addBox(3.5F, -2.0F, -4.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -0.5F, 0.1733F, -0.0298F, 0.3438F));
      head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -1.0F));
      head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 47).addBox(-4.0F, -1.0F, -6.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(-0.05F)), PartPose.offset(0.0F, -1.0F, 1.0F));
      PartDefinition flower2 = HeadJoint.addOrReplaceChild("flower2", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -6.5F, -4.25F, 0.7039F, -0.1949F, -0.1078F));
      flower2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(-8, 87).addBox(-5.0F, 0.0F, -8.0F, 9.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(-8, 87).addBox(-5.0F, 0.0F, 0.0F, 9.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(-8, 87).addBox(-8.0F, 0.0F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(-8, 87).addBox(0.0F, 0.0F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      PartDefinition flower = HeadJoint.addOrReplaceChild("flower", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -9.0F, -2.0F, 0.1608F, 0.1468F, -0.7298F));
      flower.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(-6, 87).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      flower.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(-6, 87).addBox(-4.0F, 0.0F, -6.0F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(-6, 87).addBox(-6.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      PartDefinition Marm = smash.addOrReplaceChild("Marm", CubeListBuilder.create(), PartPose.offset(-6.0F, -28.0F, -2.0F));
      PartDefinition MarmJoint = Marm.addOrReplaceChild("MarmJoint", CubeListBuilder.create().texOffs(58, 8).addBox(-2.0F, -3.0F, -1.75F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(50, 47).addBox(-1.5F, -14.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -1.0F, 0.7854F, 0.0F, 0.0F));
      PartDefinition MArm2 = MarmJoint.addOrReplaceChild("MArm2", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, 1.0F));
      PartDefinition MarmJoint2 = MArm2.addOrReplaceChild("MarmJoint2", CubeListBuilder.create().texOffs(17, 18).addBox(-1.5F, -1.5F, -13.0F, 3.0F, 3.0F, 13.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.0F));
      PartDefinition clump = MarmJoint2.addOrReplaceChild("clump", CubeListBuilder.create().texOffs(0, 16).addBox(-2.5F, -2.5F, -5.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -13.0F));
      PartDefinition tumors = clump.addOrReplaceChild("tumors", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -2.5F));
      tumors.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(21, 75).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 1.0F, -0.5672F, -1.2217F, -0.5236F));
      tumors.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(21, 75).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 1.0F, 1.0F, 0.6545F, -0.6109F, -0.5672F));
      tumors.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(20, 74).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.9599F, 0.3054F, 0.5236F));
      tumors.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(22, 76).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.5672F, -0.48F, 0.6545F));
      PartDefinition spikes = clump.addOrReplaceChild("spikes", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition thorn2 = spikes.addOrReplaceChild("thorn2", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -2.5F));
      thorn2.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn2.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn3 = spikes.addOrReplaceChild("thorn3", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.0F, -2.5F, 0.48F, 0.3054F, -0.5236F));
      thorn3.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn3.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn4 = spikes.addOrReplaceChild("thorn4", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, -2.5F, 0.5672F, -0.8727F, 1.7453F));
      thorn4.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn4.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn5 = spikes.addOrReplaceChild("thorn5", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -5.5F, 1.309F, -0.4363F, 0.48F));
      thorn5.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn5.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn6 = spikes.addOrReplaceChild("thorn6", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, -0.3054F, 0.3927F, 2.618F));
      thorn6.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn6.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn7 = spikes.addOrReplaceChild("thorn7", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -2.5F, -0.2618F, -0.6545F, -1.9635F));
      thorn7.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn7.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn8 = spikes.addOrReplaceChild("thorn8", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -2.5F, 0.6109F, -0.4363F, -1.7453F));
      thorn8.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn8.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn9 = spikes.addOrReplaceChild("thorn9", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2182F, 0.0F, 0.9163F));
      thorn9.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn9.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn10 = spikes.addOrReplaceChild("thorn10", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.5F, 1.9635F, 0.3054F, -0.2618F));
      thorn10.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn10.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn11 = spikes.addOrReplaceChild("thorn11", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.5F, 1.0472F, 0.0F, -0.3054F));
      thorn11.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn11.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition thorn12 = spikes.addOrReplaceChild("thorn12", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.0F, -3.5F, 0.6981F, 0.1745F, 2.1817F));
      thorn12.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
      thorn12.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(3, 17).addBox(-0.5F, -6.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void animateSpikes(ModelPart part, float val) {
      part.yScale = 1.0F + Mth.sin(val / 6.0F) / 6.0F;
   }

   public void setupAnim(Slasher entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.attackAnim > 0.0F) {
         float f1 = -1.0F + Mth.abs(10.0F - 2.0F * entity.attackAnim) / 6.5F;
         this.Marm.xRot = Mth.sin(f1) * 2.0F;
         this.MArm2.xRot = -Mth.sin(f1) * 3.0F;
      } else if (limbSwingAmount > -0.15F && limbSwingAmount < 0.15F) {
         this.Marm.xRot = Mth.sin(ageInTicks / 6.0F) / 8.0F;
         this.MArm2.xRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.MarmJoint2.xRot = -Mth.sin(ageInTicks / 6.0F) / 8.0F;
         this.LeftArm.zRot = -Mth.sin(ageInTicks / 6.0F) / 8.0F;
      } else {
         this.LeftArm.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
         this.LeftArm.zRot = 0.0F;
         this.Marm.xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.MArm2.xRot = -Mth.sin(ageInTicks / 8.0F) / 10.0F;
         this.MarmJoint2.xRot = -Mth.sin(ageInTicks / 6.0F) / 8.0F;
      }

      this.LeftLeg.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
      this.RightLeg.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
      this.leftForLeg.xRot = this.LeftLeg.xRot < 0.0F ? -this.LeftLeg.xRot : 0.0F;
      this.rightForLeg.xRot = this.RightLeg.xRot < 0.0F ? -this.RightLeg.xRot : 0.0F;
      this.jaw.xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
      this.animateTumor(this.tumors, Mth.cos(ageInTicks / 7.0F) / 5.0F);
      this.animateSpikes(this.thorn2, ageInTicks);
      this.animateSpikes(this.thorn3, -ageInTicks);
      this.animateSpikes(this.thorn4, ageInTicks);
      this.animateSpikes(this.thorn5, -ageInTicks);
      this.animateSpikes(this.thorn6, ageInTicks);
      this.animateSpikes(this.thorn7, -ageInTicks);
      this.animateSpikes(this.thorn8, ageInTicks);
      this.animateSpikes(this.thorn9, -ageInTicks);
      this.animateSpikes(this.thorn10, ageInTicks);
      this.animateSpikes(this.thorn11, -ageInTicks);
      this.animateSpikes(this.thorn12, ageInTicks);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.smash.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
