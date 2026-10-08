package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Spore;
import com.Harbinger.Spore.network.AdaptableHurtColor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class ProtoHurtRenderTypes extends RenderStateShard {
    private static final ResourceLocation PALETTE = new ResourceLocation(Spore.MODID, "textures/misc/proto_hurt_overlay.png");

    private static final OverlayStateShard PROTO_OVERLAY = new OverlayStateShard(false) {
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
            "proto_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP).setOverlayState(PROTO_OVERLAY)
                    .createCompositeState(true)));

    private static final Function<ResourceLocation, RenderType> TRANSLUCENT = Util.memoize(texture -> RenderType.create(
            "proto_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP).setOverlayState(PROTO_OVERLAY)
                    .createCompositeState(true)));

    private static final Function<ResourceLocation, RenderType> INVISIBLE_VISIBLE = Util.memoize(texture -> RenderType.create(
            "proto_invisible_visible", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
            RenderType.CompositeState.builder()
                    // The item shader ignores overlay. Keep its culling, target, blend, depth and sort states.
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(CULL)
                    .setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setLightmapState(LIGHTMAP).setOverlayState(PROTO_OVERLAY)
                    .createCompositeState(true)));

    private ProtoHurtRenderTypes() {
        super("proto_hurt_materials", () -> {}, () -> {});
    }

    public static RenderType invisibleVisible(ResourceLocation texture) {
        return INVISIBLE_VISIBLE.apply(texture);
    }

    public static MultiBufferSource wrapBuffers(MultiBufferSource source, ResourceLocation bodyTexture,
                                                ResourceLocation membraneTexture, ResourceLocation hatTexture,
                                                boolean hasFeedback, AdaptableHurtColor color) {
        // Restrict replacement to the existing model passes. Labels, leashes and outlines pass through.
        RenderType body = RenderType.entityCutoutNoCull(bodyTexture);
        RenderType membrane = RenderType.entityTranslucent(membraneTexture);
        RenderType hat = RenderType.entityCutoutNoCull(hatTexture);
        RenderType invisibleVisible = invisibleVisible(bodyTexture);
        int hurtRow = hasFeedback ? switch (color) {
            case RED -> 3;
            case GREEN -> 6;
            case PURPLE -> 4;
        } : -1;
        return type -> {
            RenderType material;
            if (type == body) {
                material = CUTOUT.apply(bodyTexture);
            } else if (type == membrane) {
                material = TRANSLUCENT.apply(membraneTexture);
            } else if (type == hat) {
                material = CUTOUT.apply(hatTexture);
            } else if (type == invisibleVisible) {
                material = invisibleVisible;
            } else {
                return source.getBuffer(type);
            }
            return new ProtoOverlayVertexConsumer(source.getBuffer(material), hurtRow);
        };
    }
}
