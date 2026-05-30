package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.Projectile.HarpoonProjectile;
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

public class HarpoonModel extends EntityModel<HarpoonProjectile> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "harpoonmodel"), "main");
   private final ModelPart harpoon;

   public HarpoonModel() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.harpoon = root.getChild("harpoon");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition harpoon = partdefinition.addOrReplaceChild("harpoon", CubeListBuilder.create().texOffs(975, 345).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 19.0F, new CubeDeformation(0.0F)).texOffs(1009, 378).addBox(-1.0F, -1.0F, -7.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(1010, 364).addBox(0.0F, -4.0F, -7.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(1011, 364).addBox(-4.0F, 0.0F, -7.0F, 4.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(1011, 364).mirror().addBox(0.0F, 0.0F, -7.0F, 4.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -1.5708F, 0.0F, 0.0F));
      harpoon.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(1006, 422).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(1000, 377).addBox(-2.0F, 0.0F, -18.5F, 4.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.0F, 0.7854F));
      harpoon.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(1006, 422).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(1012, 377).addBox(-2.0F, 0.0F, -18.5F, 4.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.0F, -0.7854F));
      harpoon.addOrReplaceChild("backPlane_r1", CubeListBuilder.create().texOffs(1010, 364).addBox(0.0F, -2.0F, -4.5F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -2.5F, 0.0F, 0.0F, -3.1416F));
      return LayerDefinition.create(meshdefinition, 1028, 1028);
   }

   public void setupAnim(HarpoonProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
      this.harpoon.y = this.harpoon.getInitialPose().y - 4.0F;
      this.harpoon.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
   }
}
