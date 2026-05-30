package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.Calamities.Hohlfresser;
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

public class WormTailModel extends EntityModel<Hohlfresser> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "wormstailmodel"), "main");
   private final ModelPart body;

   public WormTailModel(ModelPart root) {
      this.body = root.getChild("body");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(8, 8).addBox(15.0F, -24.0F, -11.0F, 26.0F, 24.0F, 24.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-16.0F, -32.0F, -15.0F, 32.0F, 32.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-47.0F, -21.0F, -8.0F, 51.0F, 23.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(57.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2182F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(Hohlfresser entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
