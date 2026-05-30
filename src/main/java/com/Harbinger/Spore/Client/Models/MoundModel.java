package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.Organoids.Mound;
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

public class MoundModel extends EntityModel<Mound> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "moundmodel"), "main");
   private final ModelPart body;
   private final ModelPart tendrils;
   private final ModelPart small_tendrils;
   private final ModelPart bb_main;

   public MoundModel(ModelPart root) {
      this.body = root.getChild("body");
      this.tendrils = root.getChild("tendrils");
      this.small_tendrils = root.getChild("small_tendrils");
      this.bb_main = root.getChild("bb_main");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(12, 14).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition tendrils = partdefinition.addOrReplaceChild("tendrils", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
      PartDefinition tendril = tendrils.addOrReplaceChild("tendril", CubeListBuilder.create(), PartPose.offset(-3.0F, 0.0F, 0.0F));
      tendril.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(50, 0).addBox(-6.0F, -13.0F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));
      PartDefinition tendril2 = tendrils.addOrReplaceChild("tendril2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.5F));
      tendril2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(50, 6).addBox(0.0F, -13.0F, -7.5F, 0.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));
      PartDefinition tendril3 = tendrils.addOrReplaceChild("tendril3", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, 0.0F));
      tendril3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(50, 26).addBox(-1.0F, -13.0F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));
      PartDefinition tendril4 = tendrils.addOrReplaceChild("tendril4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 3.0F));
      tendril4.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(50, 32).addBox(0.0F, -13.0F, -0.5F, 0.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, -0.1745F, 0.0F, 0.0F));
      PartDefinition small_tendrils = partdefinition.addOrReplaceChild("small_tendrils", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition tendril5 = small_tendrils.addOrReplaceChild("tendril5", CubeListBuilder.create(), PartPose.offset(-3.0F, 0.0F, 0.0F));
      tendril5.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(36, 0).addBox(-6.0F, -13.0F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));
      PartDefinition tendril6 = small_tendrils.addOrReplaceChild("tendril6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.5F));
      tendril6.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(36, 6).addBox(0.0F, -13.0F, -7.5F, 0.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));
      PartDefinition tendril7 = small_tendrils.addOrReplaceChild("tendril7", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, 0.0F));
      tendril7.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(36, 26).addBox(-1.0F, -13.0F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));
      PartDefinition tendril8 = small_tendrils.addOrReplaceChild("tendril8", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 3.0F));
      tendril8.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(36, 32).addBox(0.0F, -13.0F, -0.5F, 0.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, -0.1745F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void setupAnim(Mound entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.body.xScale = 1.0F + Mth.cos(ageInTicks / 9.0F) / 6.0F;
      this.body.zScale = 1.0F + Mth.cos(ageInTicks / 9.0F) / 6.0F;
      this.body.yScale = 1.0F - Mth.cos(ageInTicks / 9.0F) / 6.0F;
      this.small_tendrils.visible = entity.getAge() >= 3;
      this.tendrils.visible = entity.getAge() >= 4;
      this.bb_main.visible = entity.onGround();
      if (this.small_tendrils.visible) {
         this.small_tendrils.getChild("tendril5").zRot = -Mth.cos(ageInTicks / 9.0F) / 6.0F;
         this.small_tendrils.getChild("tendril7").zRot = Mth.cos(ageInTicks / 9.0F) / 6.0F;
         this.small_tendrils.getChild("tendril6").xRot = Mth.cos(ageInTicks / 9.0F) / 6.0F;
         this.small_tendrils.getChild("tendril8").xRot = -Mth.cos(ageInTicks / 9.0F) / 6.0F;
      }

      if (this.tendrils.visible) {
         this.tendrils.getChild("tendril").zRot = -Mth.cos(ageInTicks / 9.0F) / 6.0F;
         this.tendrils.getChild("tendril3").zRot = Mth.cos(ageInTicks / 9.0F) / 6.0F;
         this.tendrils.getChild("tendril2").xRot = Mth.cos(ageInTicks / 9.0F) / 6.0F;
         this.tendrils.getChild("tendril4").xRot = -Mth.cos(ageInTicks / 9.0F) / 6.0F;
      }

   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.tendrils.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.small_tendrils.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
