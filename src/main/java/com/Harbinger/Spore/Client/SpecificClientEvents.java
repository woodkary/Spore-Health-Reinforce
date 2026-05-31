package com.Harbinger.Spore.Client;

import com.Harbinger.Spore.Client.AnimationTrackers.AssassinReloadAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.AssassinShootAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.BileBlasterReloadAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.BileBlasterShootAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.MistMakerSawAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.MistMakerShootAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.PCIAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.SGAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.SGReloadAnimationTracker;
import com.Harbinger.Spore.Client.ArmorParts.ComplexHandModelItem;
import com.Harbinger.Spore.Client.MusicManager.MenuMusicPlayer;
import com.Harbinger.Spore.Client.MusicManager.SporeMusicPlayer;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import com.Harbinger.Spore.ExtremelySusThings.Package.SporeGunFirePacket;
import com.Harbinger.Spore.Sitems.CustomModelArmorData;
import com.Harbinger.Spore.Sitems.Guns.AbstractSporeGun;
import com.Harbinger.Spore.Sitems.Guns.AcidicAssasin;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "spore",
   value = {Dist.CLIENT}
)
public class SpecificClientEvents {
   public static final ResourceLocation ASSASIN_SCOPE = new ResourceLocation("spore", "textures/gui/icons/assassin_scope.png");
   public static final ResourceLocation BILE_OVERLAY = new ResourceLocation("spore", "textures/gui/icons/bile_overlay.png");
   public static final ResourceLocation CORROSION_OVERLAY = new ResourceLocation("spore", "textures/gui/icons/corrosion_overlay.png");
   public static final ResourceLocation MADNESS_OVERLAY = new ResourceLocation("spore", "textures/gui/icons/madness_overlay.png");
   public static final ResourceLocation MYCELIUM_INFECTION_OVERLAY = new ResourceLocation("spore", "textures/gui/icons/mycelium_infection_overlay.png");

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      handleGunTrigger();
      SporeEntityHeeaafastthManager.INSTANCE.tick();
      EntityHeealuthManager.INSTANCE.tick();
      if (event.phase == Phase.END) {
         PCIAnimationTracker.tickAll();
         SGAnimationTracker.tickAll();
         SGReloadAnimationTracker.tickAll();
         MistMakerSawAnimationTracker.tickAll();
         MistMakerShootAnimationTracker.tickAll();
         BileBlasterShootAnimationTracker.tickAll();
         BileBlasterReloadAnimationTracker.tickAll();
         AssassinShootAnimationTracker.tickAll();
         AssassinReloadAnimationTracker.tickAll();
         Minecraft mc = Minecraft.getInstance();
         if ((Boolean)SConfig.SERVER.disable_vanilla.get()) {
            Minecraft.getInstance().getMusicManager().stopPlaying();
         }

         if (!(Boolean)SConfig.SERVER.disable_system.get()) {
            SporeMusicPlayer.tickMusic();
         }

         if (mc.screen instanceof TitleScreen && (Boolean)SConfig.SERVER.menu_song.get()) {
            MenuMusicPlayer.tick();
         }
      }

   }

   @SubscribeEvent
   public static void onRenderHand(RenderHandEvent event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null && mc.level != null) {
         ItemStack stack = player.getItemInHand(event.getHand());

         for(ComplexHandModelItem handModelItem : ArmorModelList.ITEM_RENDERING_BITS) {
            Item var7 = stack.getItem();
            if (var7 instanceof CustomModelArmorData) {
               CustomModelArmorData armorData = (CustomModelArmorData)var7;
               if (event.getHand().equals(handModelItem.slot) && stack.getItem().equals(handModelItem.item)) {
                  handModelItem.renderCustomHand(player, stack, event.getPartialTick(), event.getPackedLight(), event.getMultiBufferSource(), event.getPoseStack(), armorData.getTextureLocation());
               }
            }
         }

      }
   }

   public static void handleGunTrigger() {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null) {
         if (mc.options.keyAttack.isDown()) {
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            Item var4 = stack.getItem();
            if (var4 instanceof AbstractSporeGun) {
               AbstractSporeGun gun = (AbstractSporeGun)var4;
               int shootDelay = getInt(stack, "ShootDelay");
               int reloadDelay = getInt(stack, "ReloadDelay");
               if (shootDelay <= 0 && reloadDelay <= 0 && !player.getCooldowns().isOnCooldown(gun)) {
                  SporePacketHandler.sendToServer(new SporeGunFirePacket(player.getId()));
               }
            }
         }

      }
   }

   private static int getInt(ItemStack stack, String key) {
      return stack.getTag() != null && stack.getTag().contains(key) ? stack.getTag().getInt(key) : 0;
   }

   @SubscribeEvent
   public static void onFovUpdate(ViewportEvent.ComputeFov event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null) {
         ItemStack stack = player.getMainHandItem();
         if (stack.getItem() instanceof AcidicAssasin && player.isShiftKeyDown()) {
            float zoomMultiplier = 0.4F;
            event.setFOV(event.getFOV() * (double)zoomMultiplier);
         }

      }
   }

   @SubscribeEvent
   public static void onRenderOverlay(RenderGuiEvent.Pre event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null) {
         GuiGraphics guiGraphics = event.getGuiGraphics();
         int screenWidth = guiGraphics.guiWidth();
         int screenHeight = guiGraphics.guiHeight();
         ItemStack stack = player.getMainHandItem();
         if (stack.getItem() instanceof AcidicAssasin && player.isShiftKeyDown()) {
            renderOverlay(event, screenWidth, screenHeight, ASSASIN_SCOPE, false, 0);
         }

         MobEffectInstance biled = player.getEffect((MobEffect)Seffects.BILED.get());
         MobEffectInstance corroded = player.getEffect((MobEffect)Seffects.CORROSION.get());
         MobEffectInstance madness = player.getEffect((MobEffect)Seffects.MADNESS.get());
         MobEffectInstance mycelium = player.getEffect((MobEffect)Seffects.MYCELIUM.get());
         if (biled != null) {
            renderOverlay(event, screenWidth, screenHeight, BILE_OVERLAY, true, biled.getDuration());
         }

         if (corroded != null) {
            renderOverlay(event, screenWidth, screenHeight, CORROSION_OVERLAY, true, corroded.getDuration());
         }

         if (madness != null) {
            renderOverlay(event, screenWidth, screenHeight, MADNESS_OVERLAY, true, madness.getDuration());
         }

         if (mycelium != null) {
            renderOverlay(event, screenWidth, screenHeight, MYCELIUM_INFECTION_OVERLAY, true, mycelium.getDuration());
         }

      }
   }

   protected static void renderOverlay(RenderGuiEvent.Pre event, int w, int h, ResourceLocation location, boolean fade, int i) {
      float alpha = 1.0F;
      if (fade && i <= 100) {
         alpha = (float)i * 0.01F;
      }

      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      event.getGuiGraphics().blit(location, 0, 0, 0.0F, 0.0F, w, h, w, h);
      RenderSystem.depthMask(true);
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
   }
}
