package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfectedPillagerCaptainModel;
import com.Harbinger.Spore.Client.Models.InfectedPillagerModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPillager;
import com.Harbinger.Spore.Sentities.Variants.InfPillagerSkins;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfectedPillagerRenderer extends BaseInfectedRenderer<InfectedPillager> {
   private final InfectedPillagerModel defaultModel = (InfectedPillagerModel)this.getModel();
   private final InfectedPillagerCaptainModel captainModel;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/inf_pillager.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/inf_pillager.png");
   private static final ResourceLocation CAPTAIN = new ResourceLocation("spore", "textures/entity/inf_pillager_captain.png");

   public InfectedPillagerRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedPillagerModel(context.bakeLayer(InfectedPillagerModel.LAYER_LOCATION)), 0.5F);
      this.captainModel = new InfectedPillagerCaptainModel(context.bakeLayer(InfectedPillagerCaptainModel.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(InfectedPillager entity) {
      return entity.getVariant() == InfPillagerSkins.CAPTAIN ? CAPTAIN : TEXTURE;
   }

   public void render(InfectedPillager type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = (EntityModel)(type.getVariant() == InfPillagerSkins.CAPTAIN ? this.captainModel : this.defaultModel);
      super.render(type, value1, value2, stack, bufferSource, light);
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
