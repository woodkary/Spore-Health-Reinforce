package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.Projectile.SyringeProjectile;
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

public class SyringeProjectileModel extends EntityModel<SyringeProjectile> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "syringeprojectile"), "main");
   private final ModelPart syringe;
   private final ModelPart color;

   public SyringeProjectileModel() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.syringe = root.getChild("syringe");
      this.color = this.syringe.getChild("color");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition syringe = partdefinition.addOrReplaceChild("syringe", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -3.88F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)).texOffs(8, 8).addBox(-0.5F, -7.25F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.3F)).texOffs(12, 2).addBox(-0.5F, -0.75F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.3F)).texOffs(0, 6).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(8, 0).addBox(-1.0F, 2.98F, -1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      syringe.addOrReplaceChild("color", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, -3.85F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 16, 16);
   }

   public void setupAnim(SyringeProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.syringe.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
