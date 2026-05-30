package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
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

public class RootsModel extends EntityModel<Calamity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "rootsmodel"), "main");
   private final ModelPart roots;

   public RootsModel(ModelPart root) {
      this.roots = root.getChild("roots");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition roots = partdefinition.addOrReplaceChild("roots", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition rootBase = roots.addOrReplaceChild("rootBase", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.25F, 0.0F));
      PartDefinition plants = rootBase.addOrReplaceChild("plants", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 11.0F, 0.0F, -0.5236F, 0.0F));
      plants.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants2 = rootBase.addOrReplaceChild("plants2", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, -8.0F, 0.0F, -0.48F, 0.0F));
      plants2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants3 = rootBase.addOrReplaceChild("plants3", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, 0.0F, -6.0F, 0.0F, -0.4363F, 0.0F));
      plants3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition north = roots.addOrReplaceChild("north", CubeListBuilder.create(), PartPose.offset(0.0F, -0.25F, -48.0F));
      north.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 48).addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
      PartDefinition plants4 = north.addOrReplaceChild("plants4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, -6.0F, 0.0F, -0.4363F, 0.0F));
      plants4.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants5 = north.addOrReplaceChild("plants5", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 0.0F, 5.0F, 0.0F, -0.48F, 0.0F));
      plants5.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition south = roots.addOrReplaceChild("south", CubeListBuilder.create().texOffs(96, 48).addBox(-24.0F, 0.0F, -25.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.25F, 49.0F));
      PartDefinition plants6 = south.addOrReplaceChild("plants6", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, -8.0F, 0.0F, -0.8727F, 0.0F));
      plants6.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants7 = south.addOrReplaceChild("plants7", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, 0.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
      plants7.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition west = roots.addOrReplaceChild("west", CubeListBuilder.create().texOffs(0, 96).addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(-48.0F, -0.25F, 0.0F));
      PartDefinition plants8 = west.addOrReplaceChild("plants8", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, 0.0F, -14.0F, 0.0F, -0.48F, 0.0F));
      plants8.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants9 = west.addOrReplaceChild("plants9", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 11.0F, 0.0F, -0.48F, 0.0F));
      plants9.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition east = roots.addOrReplaceChild("east", CubeListBuilder.create(), PartPose.offset(48.0F, -0.25F, 0.0F));
      east.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(96, 0).addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants10 = east.addOrReplaceChild("plants10", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, 6.0F, 0.0F, -0.48F, 0.0F));
      plants10.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants11 = east.addOrReplaceChild("plants11", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -13.0F, 0.0F, -0.4363F, 0.0F));
      plants11.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition north_west = roots.addOrReplaceChild("north_west", CubeListBuilder.create().texOffs(96, 96).addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(-48.0F, -0.25F, -48.0F));
      PartDefinition plants12 = north_west.addOrReplaceChild("plants12", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, 0.0F, 4.0F, 0.0F, -0.4363F, 0.0F));
      plants12.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants13 = north_west.addOrReplaceChild("plants13", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 17.0F, 0.0F, -0.9163F, 0.0F));
      plants13.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition south_west = roots.addOrReplaceChild("south_west", CubeListBuilder.create().texOffs(96, 144).addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(-48.0F, -0.25F, 48.0F));
      PartDefinition plants14 = south_west.addOrReplaceChild("plants14", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, -16.0F, 0.0F, -0.8727F, 0.0F));
      plants14.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -16.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants15 = south_west.addOrReplaceChild("plants15", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      plants15.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition south_east = roots.addOrReplaceChild("south_east", CubeListBuilder.create().texOffs(0, 192).addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(48.0F, -0.25F, 48.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants16 = south_east.addOrReplaceChild("plants16", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, -4.0F, 0.0F, -1.1781F, 0.0F));
      plants16.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition plants17 = south_east.addOrReplaceChild("plants17", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, 0.0F, 14.0F, 0.0F, -0.7854F, 0.0F));
      plants17.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      PartDefinition north_east = roots.addOrReplaceChild("north_east", CubeListBuilder.create().texOffs(0, 144).addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(48.0F, -0.25F, -48.0F));
      PartDefinition plants18 = north_east.addOrReplaceChild("plants18", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, 0.0F, 12.0F, 0.0F, -0.7854F, 0.0F));
      plants18.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, -16).addBox(0.0F, -6.0F, -8.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
      return LayerDefinition.create(meshdefinition, 256, 256);
   }

   public void setupAnim(Calamity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.roots.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
