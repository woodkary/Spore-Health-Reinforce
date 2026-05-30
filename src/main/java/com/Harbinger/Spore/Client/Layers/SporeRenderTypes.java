package com.Harbinger.Spore.Client.Layers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

public class SporeRenderTypes extends RenderType {
   public SporeRenderTypes(String p_173178_, VertexFormat p_173179_, Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
      super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
   }

   public static RenderType glowingTranslucent(ResourceLocation texture) {
      return RenderType.create("glowing_translucent", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 512, true, true, CompositeState.builder().setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).setTextureState(new TextureStateShard(texture, true, false)).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setCullState(RenderType.NO_CULL).setOverlayState(RenderType.NO_OVERLAY).setLightmapState(RenderType.LIGHTMAP).createCompositeState(true));
   }

   public static RenderType energySwirlStatic(ResourceLocation texture, float val1, float val2) {
      return create("energy_swirl_static", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, false, true, CompositeState.builder().setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER).setTextureState(new TextureStateShard(texture, false, false)).setTexturingState(new OffsetTexturingStateShard(val1, val2)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setOverlayState(RenderType.NO_OVERLAY).setLightmapState(LIGHTMAP).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
   }
}
