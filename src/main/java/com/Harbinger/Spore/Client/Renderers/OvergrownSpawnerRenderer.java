package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfestedSpawnerModel;
import com.Harbinger.Spore.Client.Special.BaseBlockEntityRenderer;
import com.Harbinger.Spore.SBlockEntities.OvergrownSpawnerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OvergrownSpawnerRenderer extends BaseBlockEntityRenderer<OvergrownSpawnerEntity> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/block/overgrown_spawner.png");

   public OvergrownSpawnerRenderer() {
      super(new InfestedSpawnerModel());
   }

   public ResourceLocation getTexture(OvergrownSpawnerEntity block) {
      return TEXTURE;
   }
}
