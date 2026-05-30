package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Naiad;
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

public class TridentNaiadCharge extends EntityModel<Naiad> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "tridentnaiadcharge"), "main");
   private final ModelPart bodySpin;
   private final ModelPart chargeBody;

   public TridentNaiadCharge() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.bodySpin = root.getChild("bodySpin");
      this.chargeBody = this.bodySpin.getChild("chargeBody");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition bodySpin = partdefinition.addOrReplaceChild("bodySpin", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.5F, 0.0F, 1.0472F, 0.0F, 0.0F));
      bodySpin.addOrReplaceChild("chargeBody", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -10.5F, -10.0F, 16.0F, 31.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -4.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void setupAnim(Naiad entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.chargeBody.yRot = ageInTicks * 0.1F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
      this.bodySpin.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
   }
}
