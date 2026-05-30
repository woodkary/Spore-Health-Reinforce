package com.Harbinger.Spore.Client.Models.KrakenTentacles;

import com.Harbinger.Spore.Client.Models.TentacledModel;
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

public class Seg3 extends EntityModel<Entity> implements TentacledModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "seg3"), "main");
   private final ModelPart BaseSegment;
   private final ModelPart Tumor;
   private final ModelPart FungalBloom;

   public Seg3() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.BaseSegment = root.getChild("BaseSegment");
      this.Tumor = root.getChild("Tumor");
      this.FungalBloom = root.getChild("FungalBloom");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("BaseSegment", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -16.0F, -4.5F, 9.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition Tumor = partdefinition.addOrReplaceChild("Tumor", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.1725F, 17.2469F, 1.1421F, -0.3466F, 1.2751F, -0.9749F));
      Tumor.addOrReplaceChild("Biomass_r1", CubeListBuilder.create().texOffs(6, 53).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -3.875F, 1.5F, -1.9689F, 0.1611F, 0.8053F));
      Tumor.addOrReplaceChild("Biomass_r2", CubeListBuilder.create().texOffs(4, 52).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 1.625F, -3.5F, -0.5741F, -0.0716F, -0.1324F));
      Tumor.addOrReplaceChild("Biomass_r3", CubeListBuilder.create().texOffs(4, 52).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 2.625F, 2.5F, 0.3491F, 0.0F, -0.8727F));
      Tumor.addOrReplaceChild("Biomass_r4", CubeListBuilder.create().texOffs(0, 50).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.375F, -0.5F, 0.1745F, 0.0F, 0.1745F));
      PartDefinition FungalBloom = partdefinition.addOrReplaceChild("FungalBloom", CubeListBuilder.create(), PartPose.offsetAndRotation(3.8468F, 17.2198F, -1.9493F, 0.2013F, 0.004F, 1.5809F));
      FungalBloom.addOrReplaceChild("Plane2_r1", CubeListBuilder.create().texOffs(20, 102).addBox(0.0F, -6.0F, -7.0F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6532F, -6.2198F, -0.0507F, 0.0F, -0.7854F, 0.0F));
      FungalBloom.addOrReplaceChild("Plane1_r1", CubeListBuilder.create().texOffs(20, 102).addBox(0.0F, -6.0F, -7.0F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6532F, -6.2198F, -0.0507F, 0.0F, 0.7854F, 0.0F));
      FungalBloom.addOrReplaceChild("Npetal_r1", CubeListBuilder.create().texOffs(-15, 81).addBox(-16.8501F, -0.8001F, -17.6583F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5033F, 1.5803F, -0.3924F, -0.3927F, 0.0F, 0.0F));
      FungalBloom.addOrReplaceChild("Spetal_r1", CubeListBuilder.create().texOffs(80, 112).addBox(-16.8501F, -0.8001F, 2.3417F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5033F, 1.5803F, -0.3924F, 0.3927F, 0.0F, 0.0F));
      FungalBloom.addOrReplaceChild("Wpetal_r1", CubeListBuilder.create().texOffs(-16, 112).addBox(1.1499F, -0.8001F, 0.3417F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5033F, 1.5803F, -8.3924F, 0.0F, 0.0F, -0.3927F));
      FungalBloom.addOrReplaceChild("Epetal_r1", CubeListBuilder.create().texOffs(-15, 96).addBox(-17.8501F, -0.8001F, 0.3417F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5033F, 1.5803F, -8.3924F, 0.0F, 0.0F, 0.3927F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.animateTumor(this.Tumor, Mth.sin(ageInTicks / 7.0F) / 6.0F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
      this.BaseSegment.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.Tumor.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.FungalBloom.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
   }
}
