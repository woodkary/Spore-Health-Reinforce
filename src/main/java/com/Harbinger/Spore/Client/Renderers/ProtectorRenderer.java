package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BulwarkProtectorModel;
import com.Harbinger.Spore.Client.Models.CollectorProtectorModel;
import com.Harbinger.Spore.Client.Models.MossProtectorModel;
import com.Harbinger.Spore.Client.Models.ProtectorModel;
import com.Harbinger.Spore.Client.Models.StuddedProtectorModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Client.Special.ProtectorBits;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Protector;
import com.Harbinger.Spore.Sentities.Variants.ProtectorVariants;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;

@OnlyIn(Dist.CLIENT)
public class ProtectorRenderer extends BaseInfectedRenderer<Protector> {
   private final EntityModel defaultModel = this.getModel();
   private final EntityModel studded;
   private final EntityModel collector;
   private final EntityModel moss;
   private final EntityModel bulk;
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(ProtectorVariants.class), (p_114874_) -> {
      p_114874_.put(ProtectorVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/protector.png"));
      p_114874_.put(ProtectorVariants.STUBBED, new ResourceLocation("spore", "textures/entity/studded_protector.png"));
      p_114874_.put(ProtectorVariants.COLLECTOR, new ResourceLocation("spore", "textures/entity/collector_protector.png"));
      p_114874_.put(ProtectorVariants.MOSS, new ResourceLocation("spore", "textures/entity/moss_protector.png"));
      p_114874_.put(ProtectorVariants.BULK, new ResourceLocation("spore", "textures/entity/bulka_protector.png"));
   });
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/protector.png");

   public ProtectorRenderer(EntityRendererProvider.Context context) {
      super(context, new ProtectorModel(context.bakeLayer(ProtectorModel.LAYER_LOCATION), false), 0.5F);
      this.addLayer(new ProtectorArmorRenderer(this, context.getModelManager()));
      this.addLayer(new PearlsLayer(this, context.getItemInHandRenderer()));
      this.studded = new StuddedProtectorModel(context.bakeLayer(StuddedProtectorModel.LAYER_LOCATION), false);
      this.collector = new CollectorProtectorModel(context.bakeLayer(CollectorProtectorModel.LAYER_LOCATION), false);
      this.moss = new MossProtectorModel(context.bakeLayer(MossProtectorModel.LAYER_LOCATION), false);
      this.bulk = new BulwarkProtectorModel(context.bakeLayer(BulwarkProtectorModel.LAYER_LOCATION), false);
   }

   public ResourceLocation getTextureLocation(Protector entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public EntityModel getVariantModel(ProtectorVariants protectorVariants) {
      switch (protectorVariants) {
         case BULK -> {
            return this.bulk;
         }
         case STUBBED -> {
            return this.studded;
         }
         case COLLECTOR -> {
            return this.collector;
         }
         case MOSS -> {
            return this.moss;
         }
         case DEFAULT -> {
            return this.defaultModel;
         }
         default -> {
            return this.defaultModel;
         }
      }
   }

   protected void scale(Protector livingEntity, PoseStack poseStack, float partialTickTime) {
      float type = livingEntity.getVariant() == ProtectorVariants.BULK ? 1.2F : 1.0F;
      poseStack.scale(type, type, type);
      super.scale(livingEntity, poseStack, partialTickTime);
   }

   public void render(Protector type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = this.getVariantModel(type.getVariant());
      super.render(type, value1, value2, stack, bufferSource, light);
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   private static class PearlsLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Protector> {
      private final ItemInHandRenderer itemInHandRenderer;

      public PearlsLayer(RenderLayerParent parent, ItemInHandRenderer itemInHandRenderer) {
         super(parent);
         this.itemInHandRenderer = itemInHandRenderer;
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Protector t, float v, float v1, float v2, float v3, float v4, float v5) {
         if (t.getPearls() > 0) {
            EntityModel var12 = this.getParentModel();
            if (var12 instanceof ProtectorBits) {
               ProtectorBits bits = (ProtectorBits)var12;
               ItemStack stack = new ItemStack(Items.ENDER_PEARL);
               poseStack.pushPose();

               for(ModelPart part : bits.EnderPearlArm()) {
                  part.translateAndRotate(poseStack);
               }

               poseStack.translate((double)0.0F, (double)0.75F, (double)0.0F);
               poseStack.scale(0.5F, 0.5F, 0.5F);
               poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
               poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
               this.itemInHandRenderer.renderItem(t, stack, ItemDisplayContext.FIXED, true, poseStack, multiBufferSource, i);
               poseStack.popPose();
            }
         }

      }
   }

   private static class ProtectorArmorRenderer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Protector> {
      private static final Map ARMOR_LOCATION_CACHE = Maps.newHashMap();
      private final TextureAtlas armorTrimAtlas;
      private static final ResourceLocation BLOOD_LAYER1 = new ResourceLocation("spore", "textures/overlay/blood_overlay.png");

      public ProtectorArmorRenderer(RenderLayerParent modelRenderLayerParent, ModelManager manager) {
         super(modelRenderLayerParent);
         this.armorTrimAtlas = manager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Protector t, float v, float v1, float v2, float v3, float v4, float v5) {
         EntityModel var12 = this.getParentModel();
         if (var12 instanceof ProtectorBits bits) {
            this.renderArmorPart(t, bits, EquipmentSlot.HEAD, bits.Helmet(), poseStack, multiBufferSource, i);
            this.renderArmorPart(t, bits, EquipmentSlot.FEET, bits.Feet(), poseStack, multiBufferSource, i);
         }

      }

      private void renderArmorPart(Protector entity, ProtectorBits bits, EquipmentSlot slot, List parts, PoseStack stack, MultiBufferSource bufferSource, int packedLight) {
         ItemStack itemStack = entity.getItemBySlot(slot);
         boolean flag = itemStack.hasFoil();
         Item var11 = itemStack.getItem();
         if (var11 instanceof ArmorItem armorItem) {
            if (armorItem instanceof DyeableLeatherItem) {
               int i = ((DyeableLeatherItem)armorItem).getColor(itemStack);
               float f = (float)(i >> 16 & 255) / 255.0F;
               float f1 = (float)(i >> 8 & 255) / 255.0F;
               float f2 = (float)(i & 255) / 255.0F;
               this.renderArmor(parts, bits, stack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, f, f1, f2, 1.0F, this.getArmorResource(entity, itemStack, slot, (String)null), flag);
            } else {
               this.renderArmor(parts, bits, stack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, itemStack, slot, (String)null), flag);
            }

            ArmorTrim.getTrim(entity.level().registryAccess(), itemStack).ifPresent((p_289638_) -> this.renderTrim(armorItem.getMaterial(), bits, stack, bufferSource, packedLight, p_289638_, parts));
         }

      }

      private void renderArmor(List parts, ProtectorBits bits, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float r, float g, float b, float alpha, ResourceLocation location, boolean glint) {
         VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(location));
         ModelPart root = bits.root();
         root.getAllParts().forEach((modelPart) -> this.setInvisible(modelPart, parts));
         root.render(stack, consumer, packedLight, packedOverlay, r, g, b, alpha);
         if (glint) {
            root.render(stack, bufferSource.getBuffer(RenderType.entityGlint()), packedLight, packedOverlay, r, g, b, alpha);
         }

         this.renderBloodLayer(root, stack, bufferSource, packedLight);
      }

      private void setInvisible(ModelPart part, List parts) {
         part.skipDraw = !parts.contains(part);
      }

      private void renderTrim(ArmorMaterial armorMaterialHolder, ProtectorBits bits, PoseStack stack, MultiBufferSource source, int light, ArmorTrim armorTrim, List parts) {
         TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(armorTrim.outerTexture(armorMaterialHolder));
         VertexConsumer vertexconsumer = textureatlassprite.wrap(source.getBuffer(Sheets.armorTrimsSheet()));
         ModelPart root = bits.root();
         root.getAllParts().forEach((modelPart) -> this.setInvisible(modelPart, parts));
         root.render(stack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }

      public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
         ArmorItem item = (ArmorItem)stack.getItem();
         String texture = item.getMaterial().getName();
         String domain = "minecraft";
         int idx = texture.indexOf(58);
         if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
         }

         String s1 = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, slot == EquipmentSlot.LEGS ? 2 : 1, type == null ? "" : String.format(Locale.ROOT, "_%s", type));
         s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
         ResourceLocation resourcelocation = (ResourceLocation)ARMOR_LOCATION_CACHE.get(s1);
         if (resourcelocation == null) {
            resourcelocation = new ResourceLocation(s1);
            ARMOR_LOCATION_CACHE.put(s1, resourcelocation);
         }

         return resourcelocation;
      }

      private void renderBloodLayer(ModelPart part, PoseStack stack, MultiBufferSource bufferSource, int packedLight) {
         VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(BLOOD_LAYER1));
         part.render(stack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
      }
   }
}
