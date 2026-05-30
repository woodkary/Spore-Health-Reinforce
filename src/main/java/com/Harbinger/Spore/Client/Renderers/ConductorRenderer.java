package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.ConductorModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Conductor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConductorRenderer extends BaseInfectedRenderer<Conductor> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/conductor.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/conductor.png");
   private final EntityModel<Entity> chains = new BoltBit();
   private Conductor.AmbientSparks attackSpark = null;

   public ConductorRenderer(EntityRendererProvider.Context context) {
      super(context, new ConductorModel(context.bakeLayer(ConductorModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new ElectricalOverlayLayer(this, context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(Conductor entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Conductor type, float value1, float partial, PoseStack stack, MultiBufferSource bufferSource, int light) {
      super.render(type, value1, partial, stack, bufferSource, light);
      Vec3 entityPos = type.getPosition(partial);
      if (!type.getSparks().isEmpty()) {
         stack.pushPose();
         stack.translate(-entityPos.x, -entityPos.y, -entityPos.z);

         for(Conductor.AmbientSparks sparks : type.getSparks()) {
            this.renderChain(sparks.getConnections(), stack, light, bufferSource, false);
         }

         int targetId = type.getAttackedId();
         Entity e = type.level().getEntity(targetId);
         if (e != null && type.getBeamTicks() >= 19) {
            Vec3 vec3 = Utilities.generatePositionAway(type.getPosition(partial), (double)4.0F);
            this.attackSpark = new Conductor.AmbientSparks(vec3, e, type, 20);
         }

         if (this.attackSpark != null && this.attackSpark.life < this.attackSpark.maxLife) {
            this.attackSpark.TickSpark();
            this.renderChain(this.attackSpark.getConnections(), stack, light, bufferSource, true);
         } else {
            this.attackSpark = null;
         }

         stack.popPose();
      }
   }

   private void renderChain(List<Vec3> entities, PoseStack stack, int light, MultiBufferSource buffer, boolean extra) {
      if (entities != null && entities.size() >= 2) {
         Vec3 origin = null;

         for(Vec3 currentPos : entities) {
            this.renderConnection(origin, currentPos, light, stack, buffer, extra);
            origin = currentPos;
         }

      }
   }

   private void renderConnection(Vec3 from, Vec3 to, int light, PoseStack stack, MultiBufferSource buffer, boolean extra) {
      if (from != null && to != null) {
         Vec3 direction = to.subtract(from);
         float length = (float)direction.length();
         if (!(length < 1.0E-4F)) {
            float thickness = extra ? 2.0F : 1.0F;
            direction = direction.normalize();
            float yaw = (float)Math.atan2(direction.x, direction.z);
            float pitch = (float)(-Math.asin(direction.y));
            stack.pushPose();
            stack.translate(from.x, from.y, from.z);
            stack.mulPose(Axis.YP.rotation(yaw));
            stack.mulPose(Axis.XP.rotation(pitch));
            stack.pushPose();
            stack.mulPose(Axis.XP.rotationDegrees(90.0F));
            stack.translate(0.0F, -length / 2.0F, 0.0F);
            stack.scale(thickness, length * 1.05F, thickness);
            VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
            this.renderBit(stack, light, consumer);
            stack.popPose();
            stack.popPose();
         }
      }
   }

   public void renderBit(PoseStack stack, int light, VertexConsumer consumer) {
      this.chains.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static class ElectricalOverlayLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Conductor> {
      private final ConductorModel model;
      private static final ResourceLocation POWER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper_armor.png");

      public ElectricalOverlayLayer(RenderLayerParent renderer, EntityModelSet modelSet) {
         super(renderer);
         this.model = new ConductorModel(modelSet.bakeLayer(ConductorModel.LAYER_LOCATION));
      }

      public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Conductor livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
         float charge = livingEntity.getCharge();
         if (!(charge <= 0.0F)) {
            float f = (float)livingEntity.tickCount + partialTicks;
            ((ConductorModel)this.getParentModel()).copyPropertiesTo(this.model);
            this.model.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTicks);
            this.model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            ModelPart part = this.model.MainBrain;
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.energySwirl(POWER_LOCATION, (float)livingEntity.tickCount * 0.01F % 1.0F, f * 0.01F % 1.0F));
            part.render(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, -8355712.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }

   private static class BoltBit extends EntityModel<Entity> {
      public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("spore", "boltbit"), "main");
      private final ModelPart bone;

      public BoltBit() {
         ModelPart root = createBodyLayer().bakeRoot();
         this.bone = root.getChild("bone");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition meshdefinition = new MeshDefinition();
         PartDefinition partdefinition = meshdefinition.getRoot();
         partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -16.0F, -0.5F, 1.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
         return LayerDefinition.create(meshdefinition, 32, 32);
      }

      public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float alpha) {
         this.bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, alpha);
      }
   }
}
