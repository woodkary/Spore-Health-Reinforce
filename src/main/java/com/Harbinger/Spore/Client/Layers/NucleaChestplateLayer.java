package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.Models.NuckelaveArmorModel;
import com.Harbinger.Spore.Client.Models.NuckelaveModel;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Nuclealave;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.client.ForgeHooksClient;

public class NucleaChestplateLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Nuclealave> {
   private static final Map ARMOR_LOCATION_CACHE = Maps.newHashMap();
   private final TextureAtlas armorTrimAtlas;
   public final NuckelaveArmorModel Nucklemodel;
   public final List helmetModels = new ArrayList();
   public final List chestModels = new ArrayList();
   public final List pantsModels = new ArrayList();
   public final List bootsModels = new ArrayList();
   private static final ResourceLocation BLOOD_LAYER1 = new ResourceLocation("spore", "textures/overlay/blood_overlay.png");
   private static final ResourceLocation BLOOD_LAYER2 = new ResourceLocation("spore", "textures/overlay/blood_overlay_2.png");

   public NucleaChestplateLayer(RenderLayerParent p_117346_, EntityModelSet set, ModelManager manager) {
      super(p_117346_);
      this.Nucklemodel = new NuckelaveArmorModel(set.bakeLayer(NuckelaveArmorModel.LAYER_LOCATION));
      this.armorTrimAtlas = manager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
      this.helmetModels.add(((NuckelaveModel)this.getParentModel()).HeadWear);
      this.chestModels.add(((NuckelaveModel)this.getParentModel()).LeftArmWear);
      this.chestModels.add(((NuckelaveModel)this.getParentModel()).RightArmWear);
      this.pantsModels.add(((NuckelaveModel)this.getParentModel()).BackRightLegWear);
      this.pantsModels.add(((NuckelaveModel)this.getParentModel()).FrontRightLegWear);
      this.bootsModels.add(((NuckelaveModel)this.getParentModel()).BackLeftFootWear);
      this.bootsModels.add(((NuckelaveModel)this.getParentModel()).FrontRightFootWear);
   }

   public void render(PoseStack stack, MultiBufferSource bufferSource, int value, Nuclealave type, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
      this.renderArmorBuffer(type, stack, bufferSource, value);
      this.renderToBufferPerArmorPiece(type, stack, bufferSource, value, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void renderToBufferPerArmorPiece(Nuclealave entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.renderArmorPart(entity, EquipmentSlot.HEAD, this.helmetModels, poseStack, bufferSource, packedLight, packedOverlay, red, green, blue, alpha);
      this.renderArmorPart(entity, EquipmentSlot.CHEST, this.chestModels, poseStack, bufferSource, packedLight, packedOverlay, red, green, blue, alpha);
      this.renderArmorPart(entity, EquipmentSlot.LEGS, this.pantsModels, poseStack, bufferSource, packedLight, packedOverlay, red, green, blue, alpha);
      this.renderArmorPart(entity, EquipmentSlot.FEET, this.bootsModels, poseStack, bufferSource, packedLight, packedOverlay, red, green, blue, alpha);
   }

   private void renderArmorPart(Nuclealave entity, EquipmentSlot slot, List parts, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      ItemStack itemStack = entity.getItemBySlot(slot);
      boolean flag = itemStack.hasFoil();
      Item var15 = itemStack.getItem();
      if (var15 instanceof ArmorItem armorItem) {
         if (armorItem instanceof DyeableLeatherItem) {
            int i = ((DyeableLeatherItem)armorItem).getColor(itemStack);
            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            this.renderArmor(parts, stack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, f, f1, f2, 1.0F, this.getArmorResource(entity, itemStack, slot, (String)null), flag, slot);
         } else {
            this.renderArmor(parts, stack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, itemStack, slot, (String)null), flag, slot);
         }

         ArmorTrim.getTrim(entity.level().registryAccess(), itemStack).ifPresent((p_289638_) -> this.renderTrim(armorItem.getMaterial(), stack, bufferSource, packedLight, p_289638_, parts, flag));
      }

   }

   private void renderArmor(List parts, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, ResourceLocation location, boolean glint, EquipmentSlot slot) {
      VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(location));
      ((NuckelaveModel)this.getParentModel()).Nuckelavee.getAllParts().forEach((modelPart) -> this.setInvisible(modelPart, parts));
      ((NuckelaveModel)this.getParentModel()).Nuckelavee.render(stack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
      if (glint) {
         ((NuckelaveModel)this.getParentModel()).Nuckelavee.render(stack, bufferSource.getBuffer(RenderType.entityGlint()), packedLight, packedOverlay, red, green, blue, alpha);
      }

      this.renderBloodLayer(((NuckelaveModel)this.getParentModel()).Nuckelavee, slot, stack, bufferSource, packedLight);
   }

   private void setInvisible(ModelPart part, List parts) {
      part.skipDraw = !parts.contains(part);
   }

   private void renderBloodLayer(ModelPart part, EquipmentSlot slot, PoseStack stack, MultiBufferSource bufferSource, int packedLight) {
      VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(slot == EquipmentSlot.LEGS ? BLOOD_LAYER2 : BLOOD_LAYER1));
      part.render(stack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
   }

   public void renderArmorBuffer(Nuclealave entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      ItemStack itemStack = entity.getItemBySlot(EquipmentSlot.CHEST);
      boolean flag = itemStack.hasFoil();
      Item var8 = itemStack.getItem();
      if (var8 instanceof ArmorItem armorItem) {
         if (armorItem instanceof DyeableLeatherItem) {
            int i = ((DyeableLeatherItem)armorItem).getColor(itemStack);
            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            this.RenderChestplate(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, f, f1, f2, 1.0F, this.getArmorResource(entity, itemStack, EquipmentSlot.CHEST, (String)null), flag);
         } else {
            this.RenderChestplate(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, itemStack, EquipmentSlot.CHEST, (String)null), flag);
         }

         ArmorTrim.getTrim(entity.level().registryAccess(), itemStack).ifPresent((p_289638_) -> this.renderTrim(armorItem.getMaterial(), poseStack, bufferSource, packedLight, p_289638_, flag));
      }

   }

   private void RenderChestplate(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, ResourceLocation location, boolean glint) {
      VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(location));
      this.Nucklemodel.ChestPlate.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
      if (glint) {
         this.Nucklemodel.ChestPlate.render(poseStack, bufferSource.getBuffer(RenderType.entityGlint()), packedLight, packedOverlay, red, green, blue, alpha);
      }

      this.renderBloodLayer(this.Nucklemodel.ChestPlate, EquipmentSlot.CHEST, poseStack, bufferSource, packedLight);
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

   private void renderTrim(ArmorMaterial material, PoseStack stack, MultiBufferSource source, int light, ArmorTrim armorTrim, List parts, boolean flag) {
      TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(flag ? armorTrim.innerTexture(material) : armorTrim.outerTexture(material));
      VertexConsumer vertexconsumer = textureatlassprite.wrap(source.getBuffer(Sheets.armorTrimsSheet()));
      ((NuckelaveModel)this.getParentModel()).Nuckelavee.getAllParts().forEach((modelPart) -> this.setInvisible(modelPart, parts));
      ((NuckelaveModel)this.getParentModel()).Nuckelavee.render(stack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderTrim(ArmorMaterial material, PoseStack stack, MultiBufferSource source, int light, ArmorTrim armorTrim, boolean flag) {
      TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(flag ? armorTrim.innerTexture(material) : armorTrim.outerTexture(material));
      VertexConsumer vertexconsumer = textureatlassprite.wrap(source.getBuffer(Sheets.armorTrimsSheet()));
      this.Nucklemodel.renderToBuffer(stack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }
}
