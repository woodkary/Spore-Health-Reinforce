package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Volatile;
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

public class VolatileModel extends EntityModel<Volatile> implements TentacledModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "volatilemodel"), "main");
   private final ModelPart CuntyVolatile;
   private final ModelPart RightLeg;
   private final ModelPart LeftLeg;
   private final ModelPart RightForLeg;
   private final ModelPart LeftForLeg;
   private final ModelPart RightArm;
   private final ModelPart LeftArm;
   private final ModelPart RightForArm;
   private final ModelPart LeftForArm;
   private final ModelPart Head;
   private final ModelPart Jaw;
   private final ModelPart Tumor1;
   private final ModelPart Tumor2;
   private final ModelPart Tumor3;
   private final ModelPart Tumor4;
   private final ModelPart Tumor5;

   public VolatileModel(ModelPart root) {
      this.CuntyVolatile = root.getChild("CuntyVolatile");
      this.RightLeg = this.CuntyVolatile.getChild("Legs").getChild("RightLeg");
      this.LeftLeg = this.CuntyVolatile.getChild("Legs").getChild("LeftLeg");
      this.RightForLeg = this.RightLeg.getChild("rightForLeg");
      this.LeftForLeg = this.LeftLeg.getChild("leftForLeg");
      this.RightArm = this.CuntyVolatile.getChild("hips").getChild("LowerTorso").getChild("UpperTorso").getChild("RightArm");
      this.LeftArm = this.CuntyVolatile.getChild("hips").getChild("LowerTorso").getChild("UpperTorso").getChild("LeftArm");
      this.RightForArm = this.RightArm.getChild("RightForArm");
      this.LeftForArm = this.LeftArm.getChild("LeftForArm");
      this.Head = this.CuntyVolatile.getChild("hips").getChild("LowerTorso").getChild("UpperTorso").getChild("head");
      this.Jaw = this.Head.getChild("jaw");
      this.Tumor1 = this.CuntyVolatile.getChild("hips").getChild("LowerTorso").getChild("tumor5");
      this.Tumor2 = this.CuntyVolatile.getChild("hips").getChild("LowerTorso").getChild("UpperTorso").getChild("Backtumors");
      this.Tumor3 = this.RightForArm.getChild("tumor2");
      this.Tumor4 = this.LeftArm.getChild("tumor4");
      this.Tumor5 = this.LeftForArm.getChild("tumor3");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition CuntyVolatile = partdefinition.addOrReplaceChild("CuntyVolatile", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 2.0F));
      PartDefinition Legs = CuntyVolatile.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, -1.25F));
      PartDefinition RightLeg = Legs.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(98, 24).addBox(-2.75F, 0.0F, -2.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.15F, -15.25F, 0.75F, -0.1331F, 0.0057F, 0.0432F));
      RightLeg.addOrReplaceChild("RightLegClothes_r1", CubeListBuilder.create().texOffs(72, 95).mirror().addBox(-2.5F, -4.5F, -2.5F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.2F)).mirror(false), PartPose.offsetAndRotation(-0.25F, 4.5F, 0.5F, 0.0F, 3.1416F, 0.0F));
      PartDefinition rightlegfungus = RightLeg.addOrReplaceChild("rightlegfungus", CubeListBuilder.create(), PartPose.offset(-2.9093F, 4.4108F, 1.1525F));
      rightlegfungus.addOrReplaceChild("Fungus_r1", CubeListBuilder.create().texOffs(-8, 120).addBox(-2.5F, 1.0F, -3.5F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5593F, -2.2108F, -2.1025F, 0.3325F, 0.5154F, 0.0523F));
      rightlegfungus.addOrReplaceChild("Fungus_r2", CubeListBuilder.create().texOffs(-8, 120).addBox(-3.25F, -5.0F, -4.5F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5593F, 4.7892F, 2.8975F, 0.0333F, 0.6219F, 0.3593F));
      rightlegfungus.addOrReplaceChild("Fungus_r3", CubeListBuilder.create().texOffs(-8, 120).addBox(-4.5F, 1.0F, -2.5F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5593F, -0.2108F, -3.1025F, 0.1783F, -0.2977F, -0.0909F));
      PartDefinition rightForLeg = RightLeg.addOrReplaceChild("rightForLeg", CubeListBuilder.create().texOffs(52, 91).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-0.25F, 6.0F, 0.5F, 0.1745F, 0.0F, 0.0F));
      rightForLeg.addOrReplaceChild("Fungus_r4", CubeListBuilder.create().texOffs(-8, 120).addBox(-4.5F, 1.0F, -7.5F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1F, 2.2F, 4.55F, -0.1797F, -0.4891F, 0.195F));
      PartDefinition LeftLeg = Legs.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(100, 0).addBox(-2.25F, 0.0F, -2.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(72, 95).addBox(-2.25F, 0.0F, -2.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(2.15F, -15.25F, 0.75F, -0.1331F, -0.0057F, -0.0432F));
      LeftLeg.addOrReplaceChild("leftForLeg", CubeListBuilder.create().texOffs(24, 95).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.25F, 6.0F, 0.5F, 0.1745F, 0.0F, 0.0F));
      PartDefinition hips = CuntyVolatile.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(32, 28).addBox(-5.0F, -6.0F, -3.5F, 10.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.25F, -0.0873F, 0.0F, 0.0F));
      PartDefinition LowerTorso = hips.addOrReplaceChild("LowerTorso", CubeListBuilder.create().texOffs(40, 16).addBox(-4.5F, -6.0F, -3.0F, 9.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, 0.1309F, 0.0F, 0.0F));
      PartDefinition UpperTorso = LowerTorso.addOrReplaceChild("UpperTorso", CubeListBuilder.create().texOffs(0, 0).addBox(-6.5F, -8.0F, -4.0F, 13.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -0.25F, 0.3491F, 0.0F, 0.0F));
      UpperTorso.addOrReplaceChild("LeftShoulder_r1", CubeListBuilder.create().texOffs(0, 91).mirror().addBox(2.0F, -3.5F, -4.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, -6.5F, 2.0F, -1.0472F, 0.0F, 0.2182F));
      UpperTorso.addOrReplaceChild("RightShoulder_r1", CubeListBuilder.create().texOffs(0, 91).addBox(-8.0F, -3.5F, -4.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -6.5F, 2.0F, -1.0472F, 0.0F, -0.2182F));
      PartDefinition RightArm = UpperTorso.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(32, 71).addBox(-4.0F, -2.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, -6.5F, 0.75F, -0.3914F, 0.0334F, 0.0807F));
      PartDefinition RightForArm = RightArm.addOrReplaceChild("RightForArm", CubeListBuilder.create().texOffs(44, 105).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 9.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 10.0F, 0.0F, 0.0F, 0.0F, -0.0436F));
      RightForArm.addOrReplaceChild("finger", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(24, 79).addBox(-3.0F, 0.0F, -0.5F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 9.0F, -0.5F));
      RightForArm.addOrReplaceChild("finger2", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(24, 79).addBox(-3.0F, 0.0F, 2.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 9.0F, -1.0F));
      RightForArm.addOrReplaceChild("finger3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, 0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(24, 79).mirror().addBox(-2.0F, 0.0F, 1.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.0F, 9.0F, -1.0F));
      PartDefinition tumor2 = RightForArm.addOrReplaceChild("tumor2", CubeListBuilder.create(), PartPose.offset(-1.0F, 3.0F, 1.25F));
      tumor2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 109).addBox(-11.0F, -26.0F, 0.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 28.0F, 0.0F, 0.0873F, 0.0F, 0.3927F));
      tumor2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(20, 109).addBox(2.0F, -19.0F, 21.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 28.0F, 0.0F, 0.9581F, 0.0283F, -0.1278F));
      PartDefinition LeftArm = UpperTorso.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(52, 74).addBox(-2.0F, -2.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -6.5F, 0.75F, -0.3914F, -0.0334F, -0.0807F));
      PartDefinition LeftForArm = LeftArm.addOrReplaceChild("LeftForArm", CubeListBuilder.create().texOffs(58, 105).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 9.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 0.0F, 0.0436F));
      LeftForArm.addOrReplaceChild("finger4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(24, 79).mirror().addBox(-1.0F, 0.0F, -0.5F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.0F, 9.0F, -0.5F));
      LeftForArm.addOrReplaceChild("finger5", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, 1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(24, 79).mirror().addBox(-1.0F, 0.0F, 2.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.0F, 9.0F, -1.0F));
      LeftForArm.addOrReplaceChild("finger6", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, 0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(24, 79).addBox(-2.0F, 0.0F, 1.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 9.0F, -1.0F));
      PartDefinition tumor3 = LeftForArm.addOrReplaceChild("tumor3", CubeListBuilder.create(), PartPose.offset(1.0F, 3.0F, 1.25F));
      tumor3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(88, 113).addBox(7.0F, -26.0F, -4.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 28.0F, -2.0F, -0.0873F, 0.0F, -0.3927F));
      tumor3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(88, 113).addBox(-6.0F, -19.0F, -25.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 28.0F, -2.0F, -0.9581F, 0.0283F, 0.1278F));
      PartDefinition tumor4 = LeftArm.addOrReplaceChild("tumor4", CubeListBuilder.create(), PartPose.offset(4.5F, 1.0F, 0.25F));
      tumor4.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(100, 81).addBox(-14.0F, -25.0F, -5.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(103, 82).addBox(-12.0F, -26.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 28.0F, -2.0F, -0.0873F, 0.0F, 0.3927F));
      tumor4.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(103, 82).addBox(1.0F, -19.0F, -25.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 28.0F, -2.0F, -0.9581F, -0.0283F, -0.1278F));
      PartDefinition head = UpperTorso.addOrReplaceChild("head", CubeListBuilder.create().texOffs(66, 57).addBox(-4.0F, -2.0833F, -0.0833F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 28).addBox(-4.0F, -10.0833F, -4.0833F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(66, 36).addBox(-4.0F, -2.3333F, -3.8333F, 8.0F, 1.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -7.4167F, -2.9167F, -0.3491F, 0.0F, 0.0F));
      head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(118, 43).addBox(-1.0F, -5.0F, -9.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(70, 24).addBox(0.0F, -3.0F, -9.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, 1.9167F, 2.9167F));
      head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(92, 68).addBox(-4.0F, -3.0F, -7.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(-0.05F)).texOffs(92, 52).addBox(-4.0F, -3.9F, -7.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(-0.05F)), PartPose.offset(0.0F, 1.9167F, 2.9167F));
      PartDefinition headwear = head.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -1.0F, -3.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7204F, -9.2184F, -1.3745F, -0.2965F, -0.0809F, 0.3213F));
      PartDefinition hat2 = headwear.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(64, 63).addBox(-0.1047F, -3.8361F, -3.2052F, 7.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.25F, -1.0F, 2.0F, -0.0524F, 0.0F, 0.0262F));
      PartDefinition hat3 = hat2.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(116, 56).addBox(-0.3053F, -3.4694F, -3.5875F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.1047F, 0.0F, 0.0524F));
      hat3.addOrReplaceChild("hat4", CubeListBuilder.create().texOffs(60, 52).addBox(-0.6663F, -2.5979F, -4.2198F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(1.75F, -2.0F, 2.0F, -0.2094F, 0.0F, 0.1047F));
      PartDefinition flower = head.addOrReplaceChild("flower", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -7.0833F, -3.0833F, 0.6979F, -0.0094F, -0.4374F));
      flower.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(-8, 120).addBox(-0.32F, 0.0F, -3.84F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(-8, 120).addBox(-8.0F, 0.0F, -3.84F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(-8, 120).addBox(-4.16F, 0.0F, -7.68F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.64F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(-8, 120).addBox(-4.16F, 0.0F, 0.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.64F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      PartDefinition flower2 = head.addOrReplaceChild("flower2", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, -6.0833F, -4.0833F, -0.262F, -0.3149F, 0.4789F));
      flower2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(-8, 120).addBox(-0.32F, 0.0F, -3.84F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
      flower2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(-8, 120).addBox(-8.0F, 0.0F, -3.84F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
      flower2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(-8, 120).addBox(-4.16F, 0.0F, -7.68F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.64F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
      flower2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(-8, 120).addBox(-4.16F, 0.0F, 0.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.64F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
      PartDefinition Backtumors = UpperTorso.addOrReplaceChild("Backtumors", CubeListBuilder.create(), PartPose.offset(0.0F, -4.5F, 3.0F));
      Backtumors.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(32, 41).addBox(-12.0F, -27.0F, -4.0F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 28.0F, -2.0F, -0.0873F, 0.0F, 0.3927F));
      Backtumors.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(-8, 120).addBox(-2.0F, -29.0F, -3.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 28.0F, -2.0F, -0.1745F, -0.6109F, -0.0436F));
      Backtumors.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(-8, 120).addBox(-4.0F, -26.0F, -3.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 28.0F, -2.0F, -0.1745F, 0.3927F, 0.0873F));
      Backtumors.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(35, 43).addBox(-1.0F, -21.0F, -25.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 28.0F, -2.0F, -0.9581F, -0.0283F, -0.1278F));
      PartDefinition tumor5 = LowerTorso.addOrReplaceChild("tumor5", CubeListBuilder.create(), PartPose.offset(1.0F, 0.5F, -4.25F));
      tumor5.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 79).mirror().addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.8739F, -7.9401F, -0.519F, -0.035F, 0.2595F, -1.7063F));
      tumor5.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 79).addBox(-4.5F, -29.0F, 0.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.75F, 3.0F, 0.2597F, -0.0338F, 0.1265F));
      tumor5.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(2, 81).addBox(2.0F, -20.0F, -9.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.0F, 3.0F, -0.2618F, 0.0F, -0.3491F));
      tumor5.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(2, 81).addBox(1.0F, -23.0F, -8.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.0F, 3.0F, -0.1309F, 0.0F, -0.2182F));
      PartDefinition LowerTorsoTumor = LowerTorso.addOrReplaceChild("LowerTorsoTumor", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      LowerTorsoTumor.addOrReplaceChild("fungus_r5", CubeListBuilder.create().texOffs(-8, 120).addBox(-5.0F, -4.0F, -11.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -5.5F, -1.25F, 0.2182F, 0.48F, -0.3927F));
      LowerTorsoTumor.addOrReplaceChild("fungus_r6", CubeListBuilder.create().texOffs(-8, 120).addBox(-6.0F, 4.0F, -9.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -5.5F, -1.25F, 0.3054F, 0.1309F, 0.1745F));
      LowerTorsoTumor.addOrReplaceChild("fungus_r7", CubeListBuilder.create().texOffs(-8, 120).addBox(-1.0F, -1.0F, -9.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -5.5F, -1.25F, 0.3054F, 0.3491F, -0.2182F));
      PartDefinition skirt = hips.addOrReplaceChild("skirt", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
      skirt.addOrReplaceChild("SkirtSeg_r1", CubeListBuilder.create().texOffs(104, 12).mirror().addBox(-3.0F, -1.0F, 0.0F, 7.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.75F, -1.0F, 3.5F, 0.1993F, -0.2015F, 0.0886F));
      skirt.addOrReplaceChild("SkirtSeg_r2", CubeListBuilder.create().texOffs(102, 74).addBox(-4.0F, -1.0F, 0.0F, 10.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.25F, -1.0F, 4.0F, 0.1321F, 0.2094F, 0.0434F));
      skirt.addOrReplaceChild("SkirtSeg_r3", CubeListBuilder.create().texOffs(94, 97).addBox(0.0F, -4.0F, -4.0F, 0.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.2853F, 2.0832F, 2.4894F, 0.0732F, 0.0715F, 0.2319F));
      skirt.addOrReplaceChild("SkirtSeg_r4", CubeListBuilder.create().texOffs(73, 111).addBox(0.0F, -1.0F, -3.5F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5553F, -1.7591F, -0.7117F, -0.1029F, -0.1889F, 0.2586F));
      skirt.addOrReplaceChild("SkirtSeg_r5", CubeListBuilder.create().texOffs(92, 95).addBox(0.0F, -1.0F, -4.5F, 0.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5553F, -0.5091F, 0.2117F, -0.0705F, 0.0722F, -0.2771F));
      skirt.addOrReplaceChild("SkirtSeg_r6", CubeListBuilder.create().texOffs(93, 103).addBox(-4.0F, -1.0F, 0.0F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -1.0F, -3.75F, -0.1993F, -0.2015F, -0.0886F));
      skirt.addOrReplaceChild("SkirtSeg_r7", CubeListBuilder.create().texOffs(102, 12).addBox(-5.0F, -1.0F, 0.0F, 9.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.25F, -0.2162F, 0.1242F, -0.0242F));
      return LayerDefinition.create(meshdefinition, 256, 256);
   }

   public void setupAnim(Volatile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.isAggressive()) {
         float value = Mth.sin(ageInTicks / 7.0F) / 8.0F;
         this.animateTentacleX(this.RightArm, -0.9F + value);
         this.animateTentacleX(this.LeftArm, -0.9F - value);
      } else {
         this.animateTentacleX(this.RightArm, Mth.sin(ageInTicks / 8.0F) / 10.0F);
         this.animateTentacleX(this.LeftArm, -Mth.sin(ageInTicks / 8.0F) / 10.0F);
      }

      this.RightLeg.xRot = Mth.cos(limbSwing * 0.8F) * 0.8F * limbSwingAmount;
      this.LeftLeg.xRot = Mth.cos(limbSwing * 0.8F) * -0.8F * limbSwingAmount;
      this.LeftForLeg.xRot = this.LeftLeg.xRot < 0.0F ? -this.LeftLeg.xRot : 0.0F;
      this.RightForLeg.xRot = this.RightLeg.xRot < 0.0F ? -this.RightLeg.xRot : 0.0F;
      this.Head.yRot = netHeadYaw / (180F / (float)Math.PI);
      this.Head.xRot = headPitch / 28.647888F;
      this.Jaw.xRot = Mth.sin(ageInTicks / 8.0F) / 10.0F;
      this.animateTumor(this.Tumor1, Mth.cos(ageInTicks / 8.0F) / 10.0F);
      this.animateTumor(this.Tumor2, -Mth.cos(ageInTicks / 8.0F) / 8.0F);
      this.animateTumor(this.Tumor3, Mth.sin(ageInTicks / 7.0F) / 8.0F);
      this.animateTumor(this.Tumor4, -Mth.sin(ageInTicks / 8.0F) / 8.0F);
      this.animateTumor(this.Tumor5, Mth.cos(ageInTicks / 8.0F) / 7.0F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.CuntyVolatile.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
