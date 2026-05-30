package com.Harbinger.Spore.Client.Models.KrakenTentacles;

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
import net.minecraft.world.entity.Entity;

public class Seg6 extends EntityModel<Entity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "seg6"), "main");
   private final ModelPart BaseSegment;
   private final ModelPart Corpses;
   private final ModelPart Corpse1;

   public Seg6() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.BaseSegment = root.getChild("BaseSegment");
      this.Corpses = root.getChild("Corpses");
      this.Corpse1 = this.Corpses.getChild("Corpse1");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("BaseSegment", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -16.0F, -4.5F, 9.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition Corpses = partdefinition.addOrReplaceChild("Corpses", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition Corpse1 = Corpses.addOrReplaceChild("Corpse1", CubeListBuilder.create(), PartPose.offsetAndRotation(2.8F, -0.2857F, -2.3714F, 0.0F, -1.0559F, 0.0F));
      Corpse1.addOrReplaceChild("Arm_r1", CubeListBuilder.create().texOffs(42, 86).addBox(0.0F, -1.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6F, -6.7143F, 0.0714F, 0.7743F, -0.2223F, -0.0735F));
      Corpse1.addOrReplaceChild("TorsoTop_r1", CubeListBuilder.create().texOffs(36, 81).addBox(-2.0F, -3.5F, -4.0F, 4.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.2143F, -0.4286F, 1.5359F, 1.5184F, 1.5708F));
      Corpse1.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(32, 65).addBox(-3.0F, -6.5F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.6143F, 0.5714F, -1.5708F, 1.3265F, -1.5708F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
      this.BaseSegment.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      this.Corpses.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
   }
}
