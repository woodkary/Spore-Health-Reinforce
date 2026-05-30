package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.Utility.InfestedConstruct;
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

public class BrokenIronGolemModel extends EntityModel<InfestedConstruct> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "broken_iron_golem"), "main");
   private final ModelPart Destoryedgolem;

   public BrokenIronGolemModel(ModelPart root) {
      this.Destoryedgolem = root.getChild("Destoryedgolem");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Destoryedgolem = partdefinition.addOrReplaceChild("Destoryedgolem", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, -7.0F));
      PartDefinition body = Destoryedgolem.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, -1.0F, 1.5002F, -0.1448F, 0.0056F));
      body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 70).addBox(-4.5F, -0.5F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, 10.5F, -1.25F, -0.3927F, 0.0F, 0.0F));
      PartDefinition head = Destoryedgolem.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -1.5F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -10.0F, -1.3259F, 0.4677F, 0.1122F));
      head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.75F, 0.3762F, 0.1153F, -0.2835F));
      Destoryedgolem.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 21).addBox(-2.0F, -15.0F, -3.0F, 4.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -2.5F, 0.0F, -1.3863F, -0.0127F, 1.5981F));
      Destoryedgolem.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 58).addBox(-2.0F, -15.0F, -3.0F, 4.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -8.5F, 5.0F, 0.0F, 0.0F, -1.0472F));
      Destoryedgolem.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -3.0F, 16.0F, 1.4832F, 0.0869F, -0.0076F));
      PartDefinition left_leg = Destoryedgolem.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(9.0F, -4.0F, 14.0F, 0.9717F, 1.0084F, -0.5238F));
      left_leg.addOrReplaceChild("left_leg_r1", CubeListBuilder.create().texOffs(60, 0).addBox(-3.0F, -8.0F, -2.5F, 6.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6552F, 5.2347F, -1.5571F, 0.1329F, 0.173F, 0.023F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(InfestedConstruct entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.Destoryedgolem.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
