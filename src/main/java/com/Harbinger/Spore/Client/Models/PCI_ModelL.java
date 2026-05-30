package com.Harbinger.Spore.Client.Models;

import com.Harbinger.Spore.Client.AnimationTrackers.PCIAnimationTracker;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PCI_ModelL extends EntityModel<LivingEntity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "pci_convertedl"), "main");
   public final ModelPart PCIBODY;
   private final ModelPart PCI;
   private final ModelPart body;
   private final ModelPart needle;
   private final ModelPart vial;
   private final ModelPart Main;
   private final ModelPart straps;

   public PCI_ModelL() {
      ModelPart root = createBodyLayer().bakeRoot();
      this.PCIBODY = root.getChild("PCIBODY");
      this.PCI = this.PCIBODY.getChild("PCI");
      this.body = this.PCI.getChild("body");
      this.needle = this.PCI.getChild("needle");
      this.vial = this.PCI.getChild("vial");
      this.Main = this.vial.getChild("Main");
      this.straps = this.PCI.getChild("straps");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition PCIBODY = partdefinition.addOrReplaceChild("PCIBODY", CubeListBuilder.create(), PartPose.offset(0.0F, 6.0F, 1.0F));
      PartDefinition PCI = PCIBODY.addOrReplaceChild("PCI", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 10.0F, -1.0F, 0.0F, -1.5708F, 1.5708F));
      PCI.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, 1.0F, 4.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition needle = PCI.addOrReplaceChild("needle", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, -2.5F, -24.0F, 0.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      needle.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -13.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.25F, 0.0F, 0.0F, 0.0F, -0.7854F));
      needle.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(30, 15).addBox(-1.0F, -1.0F, -7.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.5F, 1.0F, 0.0F, 0.0F, -0.7854F));
      PartDefinition vial = PCI.addOrReplaceChild("vial", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      vial.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 36).addBox(-2.0F, -2.0F, 1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -3.0F, 10.0F, -0.3927F, 0.0F, 0.0F));
      vial.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(24, 35).addBox(-2.0F, -2.0F, 1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -3.0F, 6.0F, -0.3927F, 0.0F, 0.0F));
      vial.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(16, 32).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 2.0F, -0.7854F, 0.0F, 0.0F));
      vial.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(24, 32).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.5F, 13.5F, 0.0F, 0.0F, 0.7854F));
      PartDefinition Main = vial.addOrReplaceChild("Main", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      Main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(32, 8).addBox(-2.0F, -8.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 2.0F, -0.7854F, 0.0F, 0.0F));
      Main.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(20, 36).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -2.65F, 2.35F, -0.7854F, 0.0F, 0.0F));
      PCI.addOrReplaceChild("straps", CubeListBuilder.create().texOffs(30, 25).addBox(-3.5F, -3.25F, 4.25F, 5.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 32).addBox(-3.5F, -3.25F, 8.25F, 5.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity instanceof Player player) {
         float anim = PCIAnimationTracker.getProgress(player, 0.0F);
         this.needle.zScale = 1.0F + anim;
      }

   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.PCIBODY.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
