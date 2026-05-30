package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.OutpostWatcherModel;
import com.Harbinger.Spore.Client.Special.BaseBlockEntityRenderer;
import com.Harbinger.Spore.SBlockEntities.OutpostWatcherBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutpostWatcherRenderer extends BaseBlockEntityRenderer<OutpostWatcherBlockEntity> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/block/outpost_watcher.png");

   public OutpostWatcherRenderer() {
      super(new OutpostWatcherModel());
   }

   public ResourceLocation getTexture(OutpostWatcherBlockEntity block) {
      return TEXTURE;
   }
}
