package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.Calamities.Sieger;
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

public class SiegerArrowModel extends EntityModel<Sieger> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "siegerarrormodel"), "main");
   private final ModelPart Arrows;

   public SiegerArrowModel(ModelPart root) {
      this.Arrows = root.getChild("Arrows");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Arrows = partdefinition.addOrReplaceChild("Arrows", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      Arrows.addOrReplaceChild("Arrow1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -54.5F, -5.0F, -1.2898F, 0.4478F, 1.3734F));
      Arrows.addOrReplaceChild("Arrow2", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, -46.5F, 11.0F, -1.3077F, -0.0874F, 1.9938F));
      Arrows.addOrReplaceChild("Arrow3", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -46.5F, 2.0F, -1.4927F, 0.5804F, 1.2238F));
      Arrows.addOrReplaceChild("Arrow4", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -48.5F, -18.0F, -1.5034F, -0.2467F, 1.1644F));
      Arrows.addOrReplaceChild("Arrow5", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -41.5F, 4.0F, -1.1865F, 0.298F, 2.2389F));
      Arrows.addOrReplaceChild("Arrow6", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -51.5F, -12.0F, -1.1267F, -0.281F, 1.8115F));
      Arrows.addOrReplaceChild("Arrow7", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -51.5F, -31.0F, -0.4044F, -0.0984F, 1.4568F));
      Arrows.addOrReplaceChild("Arrow8", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -40.75F, -52.0F, -0.9208F, -1.0566F, 2.2679F));
      Arrows.addOrReplaceChild("Arrow9", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(-5, 0).addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, -40.75F, -14.0F, -1.2635F, -0.1796F, 2.7959F));
      Arrows.addOrReplaceChild("Arrow10", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(0, 0).mirror().addBox(7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(-5, 0).mirror().addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-16.0F, -31.75F, 3.0F, 1.2635F, -0.1796F, -2.7959F));
      Arrows.addOrReplaceChild("Arrow11", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(0, 0).mirror().addBox(7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(-5, 0).mirror().addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-16.0F, -27.75F, -14.0F, 1.4362F, 0.3201F, 2.7409F));
      Arrows.addOrReplaceChild("Arrow12", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(0, 0).mirror().addBox(7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(-5, 0).mirror().addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-16.0F, -37.75F, -27.0F, 2.3497F, 0.3372F, -2.8959F));
      Arrows.addOrReplaceChild("Arrow13", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(0, 0).mirror().addBox(7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(-5, 0).mirror().addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, -32.75F, 26.0F, -1.4844F, -0.2327F, -1.6531F));
      Arrows.addOrReplaceChild("Arrow14", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(0, 0).mirror().addBox(7.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(-5, 0).mirror().addBox(-8.0F, 0.0F, -2.5F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -39.75F, 15.0F, -0.7411F, -0.5268F, -1.7972F));
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public void setupAnim(Sieger entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.Arrows.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
