package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.CDUModel;
import com.Harbinger.Spore.Client.Models.CDUModelInfested;
import com.Harbinger.Spore.Client.Special.BaseBlockEntityRenderer;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CduRenderer extends BaseBlockEntityRenderer<CDUBlockEntity> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/block/cdu.png");
   private static final ResourceLocation TEXTURE2 = new ResourceLocation("spore", "textures/block/infested_cdu.png");
   private final CDUModelInfested infestedCDU = new CDUModelInfested();

   public CduRenderer() {
      super(new CDUModel());
   }

   public ResourceLocation getTexture(CDUBlockEntity block) {
      return block.infested() ? TEXTURE2 : TEXTURE;
   }

   public void render(@NotNull CDUBlockEntity blockEntity, float partialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
      if (this.unRenderBlock(blockEntity)) {
         pPoseStack.pushPose();
         float f = (float)blockEntity.getTicks() + partialTicks;
         VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityCutout(this.getTexture(blockEntity)));
         this.setModelScale(pPoseStack, blockEntity);
         if (blockEntity.infested()) {
            this.infestedCDU.setupAnim(blockEntity, f);
            this.infestedCDU.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
         } else {
            this.model.setupAnim(blockEntity, f);
            this.model.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
         }

         pPoseStack.popPose();
      }
   }

   public void setModelScale(PoseStack pPoseStack, CDUBlockEntity block) {
      int e = block.getSide();
      this.setModelScale(pPoseStack, block, e);
   }
}
