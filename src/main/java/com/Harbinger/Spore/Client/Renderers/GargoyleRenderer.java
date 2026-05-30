package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.IchorGargoyleModel;
import com.Harbinger.Spore.Client.Models.bloomingGargoyleModel;
import com.Harbinger.Spore.Client.Models.bomberGargoyleModel;
import com.Harbinger.Spore.Client.Models.gargoyleModel;
import com.Harbinger.Spore.Client.Models.valkyrieGargoyleModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Client.Special.GargoyleBits;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Gargoyl;
import com.Harbinger.Spore.Sentities.Variants.GargoyleVariants;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class GargoyleRenderer extends BaseInfectedRenderer<Gargoyl> {
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(GargoyleVariants.class), (p_114874_) -> {
      p_114874_.put(GargoyleVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/gargoyle.png"));
      p_114874_.put(GargoyleVariants.ICHOR, new ResourceLocation("spore", "textures/entity/bile_gargoyle.png"));
      p_114874_.put(GargoyleVariants.BLOOMING, new ResourceLocation("spore", "textures/entity/blooming_gargoyle.png"));
      p_114874_.put(GargoyleVariants.BOMBER, new ResourceLocation("spore", "textures/entity/bomber_gargoyle.png"));
      p_114874_.put(GargoyleVariants.VALKYRIE, new ResourceLocation("spore", "textures/entity/valk_gargoyle.png"));
   });
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/gargoyle.png");
   private final EntityModel defaultModel = this.getModel();
   private final EntityModel ichor;
   private final EntityModel blooming;
   private final EntityModel bomber;
   private final EntityModel valk;

   public GargoyleRenderer(EntityRendererProvider.Context context) {
      super(context, new gargoyleModel(context.bakeLayer(gargoyleModel.LAYER_LOCATION), false), 0.5F);
      this.ichor = new IchorGargoyleModel(context.bakeLayer(IchorGargoyleModel.LAYER_LOCATION), false);
      this.blooming = new bloomingGargoyleModel(context.bakeLayer(bloomingGargoyleModel.LAYER_LOCATION), false);
      this.bomber = new bomberGargoyleModel(context.bakeLayer(bomberGargoyleModel.LAYER_LOCATION), false);
      this.valk = new valkyrieGargoyleModel(context.bakeLayer(valkyrieGargoyleModel.LAYER_LOCATION), false);
      this.addLayer(new ProtectorArmorRenderer(this, context.getModelManager()));
   }

   public ResourceLocation getTextureLocation(Gargoyl entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   protected @Nullable RenderType getRenderType(Gargoyl livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
      return super.getRenderType(livingEntity, bodyVisible, livingEntity.getVariant() == GargoyleVariants.ICHOR, glowing);
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   protected void scale(Gargoyl livingEntity, PoseStack poseStack, float partialTickTime) {
      float val = livingEntity.getVariant() == GargoyleVariants.VALKYRIE ? 1.2F : 1.0F;
      poseStack.scale(val, val, val);
      super.scale(livingEntity, poseStack, partialTickTime);
   }

   public EntityModel getVariantModel(GargoyleVariants gargoyleVariants) {
      switch (gargoyleVariants) {
         case ICHOR -> {
            return this.ichor;
         }
         case BLOOMING -> {
            return this.blooming;
         }
         case BOMBER -> {
            return this.bomber;
         }
         case VALKYRIE -> {
            return this.valk;
         }
         case DEFAULT -> {
            return this.defaultModel;
         }
         default -> {
            return this.defaultModel;
         }
      }
   }

   public void render(Gargoyl type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = this.getVariantModel(type.getVariant());
      super.render(type, value1, value2, stack, bufferSource, light);
   }

   private static class ProtectorArmorRenderer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Gargoyl> {
      private final TextureAtlas armorTrimAtlas;
      private static final Map ARMOR_LOCATION_CACHE = Maps.newHashMap();
      private static final ResourceLocation BLOOD_LAYER1 = new ResourceLocation("spore", "textures/overlay/blood_overlay.png");

      public ProtectorArmorRenderer(RenderLayerParent modelRenderLayerParent, ModelManager manager) {
         super(modelRenderLayerParent);
         this.armorTrimAtlas = manager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Gargoyl t, float v, float v1, float v2, float v3, float v4, float v5) {
         EntityModel var12 = this.getParentModel();
         if (var12 instanceof GargoyleBits gargoyleBits) {
            this.renderArmorPart(t, gargoyleBits, EquipmentSlot.HEAD, gargoyleBits.Helmet(), poseStack, multiBufferSource, i);
         }

      }

      private void renderArmorPart(Gargoyl entity, GargoyleBits bits, EquipmentSlot slot, List arm, PoseStack stack, MultiBufferSource bufferSource, int packedLight) {
         ItemStack itemStack = entity.getItemBySlot(slot);
         boolean flag = itemStack.hasFoil();
         Item var11 = itemStack.getItem();
         if (var11 instanceof ArmorItem armorItem) {
            if (armorItem instanceof DyeableLeatherItem) {
               int i = ((DyeableLeatherItem)armorItem).getColor(itemStack);
               float f = (float)(i >> 16 & 255) / 255.0F;
               float f1 = (float)(i >> 8 & 255) / 255.0F;
               float f2 = (float)(i & 255) / 255.0F;
               this.renderArmor(arm, bits, stack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, f, f1, f2, 1.0F, this.getArmorResource(entity, itemStack, slot, (String)null), flag);
            } else {
               this.renderArmor(arm, bits, stack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, itemStack, slot, (String)null), flag);
            }

            ArmorTrim.getTrim(entity.level().registryAccess(), itemStack).ifPresent((p_289638_) -> this.renderTrim(armorItem.getMaterial(), bits, stack, bufferSource, packedLight, p_289638_, arm, flag));
         }

      }

      private void renderArmor(List parts, GargoyleBits bits, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float r, float g, float b, float alpha, ResourceLocation location, boolean glint) {
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

      private void renderTrim(ArmorMaterial material, GargoyleBits bits, PoseStack stack, MultiBufferSource source, int light, ArmorTrim armorTrim, List parts, boolean flag) {
         TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(flag ? armorTrim.innerTexture(material) : armorTrim.outerTexture(material));
         VertexConsumer vertexconsumer = textureatlassprite.wrap(source.getBuffer(Sheets.armorTrimsSheet()));
         ModelPart root = bits.root();
         root.getAllParts().forEach((modelPart) -> this.setInvisible(modelPart, parts));
         root.render(stack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }

      public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, @javax.annotation.Nullable String type) {
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
