package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Spore;
import com.Harbinger.Spore.Sentities.BaseEntities.AdaptableHurtFeedbackState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.Map;

public final class AdaptableHurtRenderTypes extends RenderStateShard {
    private static final ResourceLocation PALETTE = new ResourceLocation(Spore.MODID, "textures/misc/adaptable_hurt_overlay.png");

    private static final OverlayStateShard ADAPTABLE_OVERLAY = new OverlayStateShard(false) {
        @Override
        public void setupRenderState() {
            // Resolve through TextureManager at draw time: SimpleTexture owns upload, reload and disposal.
            RenderSystem.setShaderTexture(1, PALETTE);
        }

        @Override
        public void clearRenderState() {
            RenderSystem.teardownOverlayColor();
        }
    };

    private static final Function<ResourceLocation, RenderType> CUTOUT = Util.memoize(texture -> RenderType.create(
            "adaptable_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP).setOverlayState(ADAPTABLE_OVERLAY)
                    .createCompositeState(true)));

    private static final Function<ResourceLocation, RenderType> TRANSLUCENT = Util.memoize(texture -> RenderType.create(
            "adaptable_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP).setOverlayState(ADAPTABLE_OVERLAY)
                    .createCompositeState(true)));

    private static final Function<ResourceLocation, RenderType> INVISIBLE_VISIBLE = Util.memoize(texture -> RenderType.create(
            "adaptable_invisible_visible", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
            RenderType.CompositeState.builder()
                    // The item shader ignores overlay. Keep its culling, target, blend, depth and sort states.
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(CULL)
                    .setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setLightmapState(LIGHTMAP).setOverlayState(ADAPTABLE_OVERLAY)
                    .createCompositeState(true)));

    private AdaptableHurtRenderTypes() {
        super("adaptable_hurt_materials", () -> {}, () -> {});
    }

    public static RenderType cutoutNoCull(ResourceLocation texture) {
        return CUTOUT.apply(texture);
    }

    public static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.apply(texture);
    }

    public static RenderType invisibleVisible(ResourceLocation texture) {
        return INVISIBLE_VISIBLE.apply(texture);
    }

    /** For custom materials: pair this state with an overlay-aware entity shader and retain their draw states. */
    public static OverlayStateShard overlayState() {
        return ADAPTABLE_OVERLAY;
    }

    /** Only explicitly bound model materials are replaced; all other drawing passes through untouched. */
    public static MultiBufferSource wrapBuffers(MultiBufferSource source, AdaptableHurtFeedbackState.Snapshot feedback,
                                                Map<RenderType, RenderType> materials) {
        Map<RenderType, RenderType> bindings = Map.copyOf(materials);
        int hurtRow = feedback.active() ? switch (feedback.color()) {
            case RED -> 3;
            case GREEN -> 6;
            case PURPLE -> 4;
        } : -1;
        return type -> {
            RenderType material = bindings.get(type);
            if (material == null) {
                return source.getBuffer(type);
            }
            return new AdaptableOverlayVertexConsumer(source.getBuffer(material), hurtRow);
        };
    }
}
