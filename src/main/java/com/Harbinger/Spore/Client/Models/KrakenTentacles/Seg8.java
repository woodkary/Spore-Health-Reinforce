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

public class Seg8 extends EntityModel<Entity> implements TentacledModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "seg8"), "main");
   private final ModelPart BaseSegment;
   private final ModelPart Tumor;

   public Seg8() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.BaseSegment = root.getChild("BaseSegment");
      this.Tumor = root.getChild("Tumor");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("BaseSegment", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -16.0F, -4.5F, 9.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition Tumor = partdefinition.addOrReplaceChild("Tumor", CubeListBuilder.create(), PartPose.offset(-0.75F, 16.625F, -0.25F));
      Tumor.addOrReplaceChild("Biomass_r1", CubeListBuilder.create().texOffs(6, 53).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, 6.375F, -3.25F, -0.2697F, -1.2535F, 0.065F));
      Tumor.addOrReplaceChild("Biomass_r2", CubeListBuilder.create().texOffs(4, 52).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.25F, -7.125F, -3.25F, -1.2327F, 0.0886F, 0.2333F));
      Tumor.addOrReplaceChild("Biomass_r3", CubeListBuilder.create().texOffs(4, 52).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, 2.875F, 3.75F, 3.0543F, -0.5236F, -0.48F));
      Tumor.addOrReplaceChild("Biomass_r4", CubeListBuilder.create().texOffs(0, 50).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.25F, -2.125F, 2.75F, -0.858F, -0.7626F, 1.0417F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.animateTumor(this.Tumor, Mth.cos(ageInTicks / 7.0F) / 8.0F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
      this.BaseSegment.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.Tumor.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
   }
}
