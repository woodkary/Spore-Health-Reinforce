package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
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

public class ExperimentDormantLayerModel<T extends Experiment> extends EntityModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "experimentdormantlayer"), "main");
   private final ModelPart body;

   public ExperimentDormantLayerModel() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.body = root.getChild("body");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(-8, 0).addBox(-19.0F, 0.0F, -5.0F, 24.0F, 0.0F, 24.0F, new CubeDeformation(0.0F)).texOffs(0, 32).addBox(-15.0F, -32.0F, 7.0F, 16.0F, 32.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 24.0F, -7.0F));
      body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -32.0F, 0.0F, 16.0F, 32.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 0.0F, 7.0F, 0.0F, 0.7854F, 0.0F));
      body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -32.0F, 0.0F, 16.0F, 32.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 0.0F, 7.0F, 0.0F, -0.7854F, 0.0F));
      body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -32.0F, 0.0F, 16.0F, 32.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 0.0F, 7.0F, 0.0F, 1.5708F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   private void resize(ModelPart part, T entity) {
      float height = entity.getBbHeight() > 2.5F ? entity.getBbHeight() : 1.0F;
      float waist = entity.getBbWidth() > 1.0F ? entity.getBbWidth() : 1.0F;
      part.yScale = height;
      part.xScale = waist;
      part.zScale = waist;
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.resize(this.body, entity);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
