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
import net.minecraft.util.Mth;

public class BileRound extends EntityModel<FleshBomb> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "bileround"), "main");
   private final ModelPart BileMissile;
   private final ModelPart Tumor;
   private final ModelPart Tendrils;

   public BileRound() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.BileMissile = root.getChild("BileMissile");
      this.Tumor = this.BileMissile.getChild("Tail").getChild("Biomass");
      this.Tendrils = this.BileMissile.getChild("Tail").getChild("Tendrils");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition BileMissile = partdefinition.addOrReplaceChild("BileMissile", CubeListBuilder.create().texOffs(0, 0).addBox(-5.7859F, -7.8591F, -5.8501F, 12.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.2141F, 15.8591F, -0.1499F));
      BileMissile.addOrReplaceChild("Bile_r1", CubeListBuilder.create().texOffs(36, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.9698F, -2.0944F, 3.4616F, 0.2877F, -0.5467F, -0.5175F));
      BileMissile.addOrReplaceChild("Bile_r2", CubeListBuilder.create().texOffs(36, 0).addBox(-3.0F, -1.0F, -7.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.7859F, 0.1409F, 0.1499F, -0.2877F, -0.5467F, 0.5175F));
      PartDefinition Tail = BileMissile.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offset(0.2141F, -7.8591F, 0.1499F));
      PartDefinition Biomass = Tail.addOrReplaceChild("Biomass", CubeListBuilder.create(), PartPose.offset(1.4335F, 3.0F, -3.3512F));
      Biomass.addOrReplaceChild("Biomass_r1", CubeListBuilder.create().texOffs(0, 28).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.9926F, -2.3258F, 0.612F, 0.3699F, 0.7124F, 0.2154F));
      Biomass.addOrReplaceChild("Biomass_r2", CubeListBuilder.create().texOffs(2, 30).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8902F, -2.4341F, 6.7892F, -0.1987F, -0.4755F, 0.1473F));
      Biomass.addOrReplaceChild("Biomass_r3", CubeListBuilder.create().texOffs(1, 29).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2826F, -1.3864F, -0.1536F, 0.0F, 0.5236F, 0.3491F));
      Biomass.addOrReplaceChild("Biomass_r4", CubeListBuilder.create().texOffs(0, 28).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2467F, -2.5148F, 7.8847F, -0.1801F, -0.3913F, 0.0164F));
      PartDefinition Tendrils = Tail.addOrReplaceChild("Tendrils", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition root_3 = Tendrils.addOrReplaceChild("root_3", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, -8.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8403F, 0.6943F, 1.558F, 0.0F, -0.5672F, 0.0F));
      PartDefinition r3seg2 = root_3.addOrReplaceChild("r3seg2", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0138F, -8.4634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      r3seg2.addOrReplaceChild("r3seg3", CubeListBuilder.create().texOffs(44, 28).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0815F, -8.5697F, -0.0142F));
      PartDefinition root_2 = Tendrils.addOrReplaceChild("root_2", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, -10.5F, -1.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0041F, 0.6943F, -1.4346F, 0.0F, 0.5672F, 0.0F));
      PartDefinition r2seg2 = root_2.addOrReplaceChild("r2seg2", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0138F, -10.4634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      r2seg2.addOrReplaceChild("r2seg3", CubeListBuilder.create().texOffs(44, 28).addBox(-0.5F, -11.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0815F, -10.5697F, -0.0142F));
      PartDefinition root_1 = Tendrils.addOrReplaceChild("root_1", CubeListBuilder.create().texOffs(28, 28).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9959F, 0.6943F, 1.5654F));
      PartDefinition r1seg2 = root_1.addOrReplaceChild("r1seg2", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0138F, -6.4634F, -0.0145F, 0.0F, 0.0F, 0.0F));
      r1seg2.addOrReplaceChild("r1seg3", CubeListBuilder.create().texOffs(44, 28).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0815F, -6.5697F, -0.0142F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   private void animateTumor(ModelPart part, float value) {
      part.xScale = 1.0F + Mth.cos(value / 6.0F) / 6.0F;
      part.yScale = 1.0F + Mth.cos(value / 6.0F) / 6.0F;
      part.zScale = 1.0F + Mth.cos(value / 6.0F) / 6.0F;
   }

   private void animateTendril(ModelPart part, float value) {
      part.xRot = Mth.sin(value / 9.0F) / 2.0F;
      part.yRot = Mth.sin(value / 9.0F) / 3.0F;
   }

   public void setupAnim(FleshBomb entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.animateTumor(this.Tumor, ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_3"), ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_3").getChild("r3seg2"), ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_3").getChild("r3seg2").getChild("r3seg3"), ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_2"), ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_2").getChild("r2seg2"), ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_2").getChild("r2seg2").getChild("r2seg3"), ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_1"), ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_1").getChild("r1seg2"), ageInTicks);
      this.animateTendril(this.Tendrils.getChild("root_1").getChild("r1seg2").getChild("r1seg3"), ageInTicks);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.BileMissile.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
