package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
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

public class JollyPresentBombs extends EntityModel<FleshBomb> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "jollypresentbombs"), "main");
   private final ModelPart RegularPresent;

   public JollyPresentBombs() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.RegularPresent = root.getChild("RegularPresent");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition RegularPresent = partdefinition.addOrReplaceChild("RegularPresent", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -0.3349F, -9.2188F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(0, 40).addBox(-2.0F, -2.3349F, -3.2188F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 8.3349F, 1.2188F));
      RegularPresent.addOrReplaceChild("SmallRibbon_r1", CubeListBuilder.create().texOffs(51, 37).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.8349F, -0.2188F, -0.0436F, -0.3927F, 0.0F));
      RegularPresent.addOrReplaceChild("SmallRibbon_r2", CubeListBuilder.create().texOffs(44, 37).addBox(0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.8349F, -0.2188F, -0.0436F, 0.3927F, 0.0F));
      RegularPresent.addOrReplaceChild("Ribbon_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.3349F, -1.2188F, 0.0F, 0.0F, 0.3927F));
      RegularPresent.addOrReplaceChild("Ribbon_r2", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, -1.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.3349F, -1.2188F, 0.0F, 0.0F, -0.3927F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void setupAnim(FleshBomb entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
      this.RegularPresent.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
   }
}
