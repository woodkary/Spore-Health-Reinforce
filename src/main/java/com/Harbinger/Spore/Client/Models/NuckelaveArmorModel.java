package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Nuclealave;
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

public class NuckelaveArmorModel extends EntityModel<Nuclealave> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "nuckelavearmormodel"), "main");
   public final ModelPart ChestPlate;

   public NuckelaveArmorModel(ModelPart root) {
      this.ChestPlate = root.getChild("ChestPlate");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ChestPlate = partdefinition.addOrReplaceChild("ChestPlate", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      ChestPlate.addOrReplaceChild("TorsoBase_r1", CubeListBuilder.create().texOffs(16, 16).addBox(-4.5F, -6.0F, -2.5F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.5F, -27.1452F, -10.0653F, 0.3054F, 0.0F, 0.0F));
      ChestPlate.addOrReplaceChild("TorsoBase_r2", CubeListBuilder.create().texOffs(17, 20).addBox(-3.0F, -5.0F, -2.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, -22.8605F, -8.8398F, 0.3927F, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void setupAnim(Nuclealave entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.ChestPlate.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
