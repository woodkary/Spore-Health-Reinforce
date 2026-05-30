package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.CustomArmorLayer;
import com.Harbinger.Spore.Client.Models.HarbingerModel;
import com.Harbinger.Spore.Client.Models.InfectedPlayerModel;
import com.Harbinger.Spore.Client.Models.InfectedTechnoModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPlayer;
import com.Harbinger.Spore.Sentities.Variants.InfPlayerSkins;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfectedPlayerRenderer extends BaseInfectedRenderer<InfectedPlayer> {
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/inf_player.png");
   private final HumanoidModel mainModel = (HumanoidModel)this.getModel();
   private final HumanoidModel technoSkin;
   private final HumanoidModel madnessModel;
   private final HumanoidModel harbyModel;
   public static final Map MAIN_TEXTURES = (Map)Util.make(Maps.newEnumMap(InfPlayerSkins.class), (p_114874_) -> {
      p_114874_.put(InfPlayerSkins.STEVE, new ResourceLocation("spore", "textures/entity/player/inf_player_steve.png"));
      p_114874_.put(InfPlayerSkins.ALEX, new ResourceLocation("spore", "textures/entity/player/inf_player_alex.png"));
      p_114874_.put(InfPlayerSkins.EFE, new ResourceLocation("spore", "textures/entity/player/inf_player_efe.png"));
      p_114874_.put(InfPlayerSkins.MAKENA, new ResourceLocation("spore", "textures/entity/player/inf_player_makena.png"));
      p_114874_.put(InfPlayerSkins.SUNNY, new ResourceLocation("spore", "textures/entity/player/inf_player_sunny.png"));
      p_114874_.put(InfPlayerSkins.ZURI, new ResourceLocation("spore", "textures/entity/player/inf_player_zuri.png"));
      p_114874_.put(InfPlayerSkins.ARI, new ResourceLocation("spore", "textures/entity/player/inf_player_ari.png"));
      p_114874_.put(InfPlayerSkins.KAI, new ResourceLocation("spore", "textures/entity/player/inf_player_kai.png"));
      p_114874_.put(InfPlayerSkins.NO0R, new ResourceLocation("spore", "textures/entity/player/inf_player_noor.png"));
   });
   public static final Map MADNESS_TEXTURES = (Map)Util.make(Maps.newEnumMap(InfPlayerSkins.class), (p_114874_) -> {
      p_114874_.put(InfPlayerSkins.STEVE, new ResourceLocation("minecraft:textures/entity/player/wide/steve.png"));
      p_114874_.put(InfPlayerSkins.ALEX, new ResourceLocation("minecraft:textures/entity/player/wide/alex.png"));
      p_114874_.put(InfPlayerSkins.EFE, new ResourceLocation("minecraft:textures/entity/player/wide/efe.png"));
      p_114874_.put(InfPlayerSkins.MAKENA, new ResourceLocation("minecraft:textures/entity/player/wide/makena.png"));
      p_114874_.put(InfPlayerSkins.SUNNY, new ResourceLocation("minecraft:textures/entity/player/wide/sunny.png"));
      p_114874_.put(InfPlayerSkins.ZURI, new ResourceLocation("minecraft:textures/entity/player/wide/zuri.png"));
      p_114874_.put(InfPlayerSkins.ARI, new ResourceLocation("minecraft:textures/entity/player/wide/ari.png"));
      p_114874_.put(InfPlayerSkins.KAI, new ResourceLocation("minecraft:textures/entity/player/wide/kai.png"));
      p_114874_.put(InfPlayerSkins.NO0R, new ResourceLocation("minecraft:textures/entity/player/wide/noor.png"));
   });
   public static final Map SPECIAL_SKINS = new HashMap() {
      {
         this.put(Component.literal("Technoblade"), new ResourceLocation("spore", "textures/entity/player/techno_skin.png"));
         this.put(Component.literal("CODATOWER"), new ResourceLocation("spore", "textures/entity/player/inf_coda_skin.png"));
         this.put(Component.literal("Flash62724"), new ResourceLocation("spore", "textures/entity/player/inf_player_slasher.png"));
         this.put(Component.literal("TVGuy"), new ResourceLocation("spore", "textures/entity/player/inf_player_blura.png"));
         this.put(Component.literal("mrlambert6"), new ResourceLocation("spore", "textures/entity/player/inf_player_lambert.png"));
         this.put(Component.literal("NexouuZ"), new ResourceLocation("spore", "textures/entity/player/inf_player_nexouuz.png"));
         this.put(Component.literal("SyrCrypt"), new ResourceLocation("spore", "textures/entity/player/inf_player_syrcrypt.png"));
         this.put(Component.literal("KaratFeng"), new ResourceLocation("spore", "textures/entity/player/inf_karat_skin.png"));
         this.put(Component.literal("BigXplosion"), new ResourceLocation("spore", "textures/entity/player/inf_explosion_skin.png"));
         this.put(Component.literal("Toasteroni"), new ResourceLocation("spore", "textures/entity/player/inf_player_toast.png"));
         this.put(Component.literal("Dr_Pilot_MOO"), new ResourceLocation("spore", "textures/entity/player/dr_pilot_moo.png"));
         this.put(Component.literal("UnmeiHa"), new ResourceLocation("spore", "textures/entity/player/inf_player_nunny.png"));
         this.put(Component.literal("AllToAshes"), new ResourceLocation("spore", "textures/entity/player/inf_player_alltoashes.png"));
         this.put(Component.literal("0dna"), new ResourceLocation("spore", "textures/entity/player/inf_player_0dna.png"));
         this.put(Component.literal("PedroHenrry"), new ResourceLocation("spore", "textures/entity/player/inf_player_pedro.png"));
         this.put(Component.literal("minisketchy0919"), new ResourceLocation("spore", "textures/entity/player/inf_player_minisketchy0919.png"));
         this.put(Component.literal("yile_ouo"), new ResourceLocation("spore", "textures/entity/player/yile_ouo.png"));
         this.put(Component.literal("TheCaramelGuy"), new ResourceLocation("spore", "textures/entity/player/thecaramelguy.png"));
         this.put(Component.literal("ThatGardener"), new ResourceLocation("spore", "textures/entity/player/gardener.png"));
         this.put(Component.literal("The_Harbinger69"), new ResourceLocation("spore", "textures/entity/player/harby.png"));
         this.put(Component.literal("hammbug"), new ResourceLocation("spore", "textures/entity/player/hammbug.png"));
         this.put(Component.literal("DivnejFelix"), new ResourceLocation("spore", "textures/entity/player/penguin.png"));
      }
   };

   public InfectedPlayerRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedPlayerModel(context.bakeLayer(InfectedPlayerModel.LAYER_LOCATION)), 0.5F);
      this.madnessModel = new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER));
      this.technoSkin = new InfectedTechnoModel(context.bakeLayer(InfectedTechnoModel.LAYER_LOCATION));
      this.harbyModel = new HarbingerModel(context.bakeLayer(HarbingerModel.LAYER_LOCATION));
      this.addLayer(new HumanoidArmorLayer(this, new HumanoidArmorModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
      this.addLayer(new CustomArmorLayer(this));
   }

   public ResourceLocation getTextureLocation(InfectedPlayer infectedPlayer) {
      if (this.isTheViewerMad(infectedPlayer)) {
         return (ResourceLocation)MADNESS_TEXTURES.get(infectedPlayer.getVariant());
      } else {
         Component component = infectedPlayer.getName();
         ResourceLocation location = (ResourceLocation)SPECIAL_SKINS.get(component);
         return location != null ? location : (ResourceLocation)MAIN_TEXTURES.get(infectedPlayer.getVariant());
      }
   }

   public boolean isTheViewerMad(InfectedPlayer infectedPlayer) {
      Entity var3 = Minecraft.getInstance().cameraEntity;
      if (!(var3 instanceof Player player)) {
         return false;
      } else {
         MobEffectInstance instance = player.getEffect((MobEffect)Seffects.MADNESS.get());
         return instance != null && instance.getAmplifier() > 0 && player.distanceTo(infectedPlayer) > 30.0F;
      }
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(InfectedPlayer type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = this.isTheViewerMad(type) ? this.madnessModel : (Objects.equals(type.getCustomName(), Component.literal("Technoblade")) ? this.technoSkin : (Objects.equals(type.getCustomName(), Component.literal("The_Harbinger69")) ? this.harbyModel : this.mainModel));
      super.render(type, value1, value2, stack, bufferSource, light);
   }
}
