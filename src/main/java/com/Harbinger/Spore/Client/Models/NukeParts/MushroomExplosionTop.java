package com.Harbinger.Spore.Client.Models.NukeParts;

import com.Harbinger.Spore.Sentities.Utility.NukeEntity;
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

public class MushroomExplosionTop extends EntityModel<NukeEntity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "mushroomexplosiontop"), "main");
   private final ModelPart top;

   public MushroomExplosionTop() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.top = root.getChild("top");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -6.0F, -16.0F, 32.0F, 7.0F, 32.0F, new CubeDeformation(0.0F)).texOffs(0, 39).addBox(-15.0F, -13.0F, -15.0F, 30.0F, 7.0F, 30.0F, new CubeDeformation(0.0F)).texOffs(0, 76).addBox(-13.0F, -16.0F, -13.0F, 26.0F, 3.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(NukeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.top.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
