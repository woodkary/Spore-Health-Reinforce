package com.Harbinger.Spore.Client.Models;

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
import net.minecraft.world.entity.Entity;

public class TentacleSegmentModel2 extends EntityModel<Entity> implements TentacledModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "tentaclesegmentmodel"), "main");
   private final ModelPart body;
   private final ModelPart Biomass;
   private final ModelPart Biomass2;

   public TentacleSegmentModel2() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.body = root.getChild("body");
      this.Biomass = root.getChild("Biomass");
      this.Biomass2 = root.getChild("Biomass2");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, -16.0F, -1.5F, 3.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      body.addOrReplaceChild("Fungus_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 1.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -11.55F, -1.45F, 0.3325F, -0.5755F, 0.0523F));
      PartDefinition Biomass = partdefinition.addOrReplaceChild("Biomass", CubeListBuilder.create(), PartPose.offset(-0.4597F, 21.9866F, -0.332F));
      Biomass.addOrReplaceChild("Biomass4_r1", CubeListBuilder.create().texOffs(12, 22).addBox(-3.5F, -1.5F, -1.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.677F, 1.8005F, 0.2635F, -0.1987F, 0.4755F, -0.1473F));
      Biomass.addOrReplaceChild("Biomass3_r1", CubeListBuilder.create().texOffs(12, 22).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5599F, -1.0913F, 0.0862F, 0.3699F, -0.7124F, -0.2154F));
      Biomass.addOrReplaceChild("Biomass2_r1", CubeListBuilder.create().texOffs(8, 20).addBox(-4.0F, -2.0F, -2.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2846F, -0.1518F, -0.6794F, 0.0F, -0.5236F, -0.3491F));
      PartDefinition Biomass2 = partdefinition.addOrReplaceChild("Biomass2", CubeListBuilder.create(), PartPose.offset(-1.4597F, 9.9866F, -0.082F));
      Biomass2.addOrReplaceChild("Biomass3_r2", CubeListBuilder.create().texOffs(12, 22).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5599F, -4.0913F, -0.9138F, 0.3699F, -0.7124F, -0.2154F));
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.animateTumor(this.Biomass, Mth.sin(ageInTicks / 8.0F) / 6.0F);
      this.animateTumor(this.Biomass2, Mth.cos(ageInTicks / 8.0F) / 7.0F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.Biomass.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.Biomass2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
