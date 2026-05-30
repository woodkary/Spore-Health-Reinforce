package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.ArmorParts.FleshHorseArmorBit;
import com.Harbinger.Spore.Client.ArmorParts.HorseArmorBit;
import com.Harbinger.Spore.Client.ArmorParts.LivingHorseArmorBit;
import com.Harbinger.Spore.Client.ArmorParts.PlatedHorseArmorBit;
import com.Harbinger.Spore.Core.Sitems;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.Item;

public class CustomHorseArmorLayer<T extends AbstractHorse, M extends HorseModel<T>> extends RenderLayer<T, M> {
   private final HorseHandlerModel origin;
   public static final HorseArmorBit FLESH_ARMOR_BIT;
   public static final HorseArmorBit PLATED_ARMOR_BIT;
   public static final HorseArmorBit LIVING_ARMOR_BIT;
   public static final List<HorseArmorBit> HORSE_ARMOR_LIST;

   public CustomHorseArmorLayer(RenderLayerParent<T, M> parent, ModelPart root) {
      super(parent);
      this.origin = new HorseHandlerModel(root);
   }

   public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      ((HorseModel)this.getParentModel()).copyPropertiesTo(this.origin);
      this.origin.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
      this.origin.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      this.handleArmorPartsRendering(entity, poseStack, light, buffer, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
   }

   protected void handleArmorPartsRendering(AbstractHorse entity, PoseStack poseStack, int light, MultiBufferSource buffer, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      List<HorseArmorBit> parts = HORSE_ARMOR_LIST;
      if (!parts.isEmpty()) {
         for(HorseArmorBit bit : parts) {
            bit.tickMovement(entity, poseStack, this.origin, light, buffer);
            bit.getModel().setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks + (float)entity.tickCount, netHeadYaw, headPitch);
         }

      }
   }

   static {
      FLESH_ARMOR_BIT = new FleshHorseArmorBit((Item)Sitems.FLESH_HORSE_ARMOR.get());
      PLATED_ARMOR_BIT = new PlatedHorseArmorBit((Item)Sitems.PLATED_HORSE_ARMOR.get());
      LIVING_ARMOR_BIT = new LivingHorseArmorBit((Item)Sitems.LIVING_HORSE_ARMOR.get());
      HORSE_ARMOR_LIST = new ArrayList<>() {
         {
            this.add(CustomHorseArmorLayer.FLESH_ARMOR_BIT);
            this.add(CustomHorseArmorLayer.PLATED_ARMOR_BIT);
            this.add(CustomHorseArmorLayer.LIVING_ARMOR_BIT);
         }
      };
   }

   public static class HorseHandlerModel extends HorseModel {
      private final ModelPart rightFrontLeg;
      private final ModelPart leftFrontLeg;
      private final ModelPart rightHindLeg;
      private final ModelPart leftHindLeg;

      public HorseHandlerModel(ModelPart root) {
         super(root);
         this.rightFrontLeg = root.getChild("right_front_leg");
         this.leftFrontLeg = root.getChild("left_front_leg");
         this.rightHindLeg = root.getChild("right_hind_leg");
         this.leftHindLeg = root.getChild("left_hind_leg");
      }

      public ModelPart getHead() {
         return this.headParts;
      }

      public ModelPart getBody() {
         return this.body;
      }

      public ModelPart getRightFrontLeg() {
         return this.rightFrontLeg;
      }

      public ModelPart getLeftFrontLeg() {
         return this.leftFrontLeg;
      }

      public ModelPart getRightHindLeg() {
         return this.rightHindLeg;
      }

      public ModelPart getLeftHindLeg() {
         return this.leftHindLeg;
      }
   }
}
