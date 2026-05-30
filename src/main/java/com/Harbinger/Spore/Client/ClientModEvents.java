package com.Harbinger.Spore.Client;

import com.Harbinger.Spore.Spore;
import com.Harbinger.Spore.Client.Layers.CustomArmorLayer;
import com.Harbinger.Spore.Client.Layers.CustomHorseArmorLayer;
import com.Harbinger.Spore.Client.Models.ArenaTendrilModel;
import com.Harbinger.Spore.Client.Models.BairnModel;
import com.Harbinger.Spore.Client.Models.BileRound;
import com.Harbinger.Spore.Client.Models.BiobloobModel;
import com.Harbinger.Spore.Client.Models.BloaterModel;
import com.Harbinger.Spore.Client.Models.BrainTentacleModel;
import com.Harbinger.Spore.Client.Models.BraionmilBabe;
import com.Harbinger.Spore.Client.Models.BraionmilModel;
import com.Harbinger.Spore.Client.Models.BraureiModel;
import com.Harbinger.Spore.Client.Models.BrokenIronGolemModel;
import com.Harbinger.Spore.Client.Models.BrotkatzeModel;
import com.Harbinger.Spore.Client.Models.BruteModel;
import com.Harbinger.Spore.Client.Models.BulletModel;
import com.Harbinger.Spore.Client.Models.BulwarkProtectorModel;
import com.Harbinger.Spore.Client.Models.BurstUsurperModel;
import com.Harbinger.Spore.Client.Models.BusserModel;
import com.Harbinger.Spore.Client.Models.ChemistModel;
import com.Harbinger.Spore.Client.Models.CollectorProtectorModel;
import com.Harbinger.Spore.Client.Models.ConductorModel;
import com.Harbinger.Spore.Client.Models.DelusionerEnchanterModel;
import com.Harbinger.Spore.Client.Models.DelusionerModel;
import com.Harbinger.Spore.Client.Models.DetasheHyperClaw;
import com.Harbinger.Spore.Client.Models.DrownedScamperModel;
import com.Harbinger.Spore.Client.Models.DualSpitterModel;
import com.Harbinger.Spore.Client.Models.ElytrumModel;
import com.Harbinger.Spore.Client.Models.ExperimentDormantLayerModel;
import com.Harbinger.Spore.Client.Models.ExplodingBusserModel;
import com.Harbinger.Spore.Client.Models.ForlornHowlerModel;
import com.Harbinger.Spore.Client.Models.GasMaskModel;
import com.Harbinger.Spore.Client.Models.GastgeberModel;
import com.Harbinger.Spore.Client.Models.GazenbrecherModel;
import com.Harbinger.Spore.Client.Models.GorgonSpookyModel;
import com.Harbinger.Spore.Client.Models.GrabberSlasherModel;
import com.Harbinger.Spore.Client.Models.GrakensenkerModel;
import com.Harbinger.Spore.Client.Models.GrieferModel;
import com.Harbinger.Spore.Client.Models.GroberfubModel;
import com.Harbinger.Spore.Client.Models.HarbingerModel;
import com.Harbinger.Spore.Client.Models.HevokerModel;
import com.Harbinger.Spore.Client.Models.HevokerModelDead;
import com.Harbinger.Spore.Client.Models.HindenXmaslightsModel;
import com.Harbinger.Spore.Client.Models.HindieModel;
import com.Harbinger.Spore.Client.Models.HivetumorModel;
import com.Harbinger.Spore.Client.Models.HohlfresserSeg1Model;
import com.Harbinger.Spore.Client.Models.HohlfresserSeg2Model;
import com.Harbinger.Spore.Client.Models.HohlfresserSeg3Model;
import com.Harbinger.Spore.Client.Models.HowitzerModel;
import com.Harbinger.Spore.Client.Models.HowlerModel;
import com.Harbinger.Spore.Client.Models.IchorGargoyleModel;
import com.Harbinger.Spore.Client.Models.IncubatorModel;
import com.Harbinger.Spore.Client.Models.InebriaterModel;
import com.Harbinger.Spore.Client.Models.InfEvoClawModel;
import com.Harbinger.Spore.Client.Models.InfectedDrownModel;
import com.Harbinger.Spore.Client.Models.InfectedEvokerModel;
import com.Harbinger.Spore.Client.Models.InfectedHazmatCoat;
import com.Harbinger.Spore.Client.Models.InfectedHazmatModel;
import com.Harbinger.Spore.Client.Models.InfectedHazmatWithTank;
import com.Harbinger.Spore.Client.Models.InfectedHuskModel;
import com.Harbinger.Spore.Client.Models.InfectedModel;
import com.Harbinger.Spore.Client.Models.InfectedPillagerCaptainModel;
import com.Harbinger.Spore.Client.Models.InfectedPillagerModel;
import com.Harbinger.Spore.Client.Models.InfectedPlayerModel;
import com.Harbinger.Spore.Client.Models.InfectedSpearModel;
import com.Harbinger.Spore.Client.Models.InfectedTechnoModel;
import com.Harbinger.Spore.Client.Models.InfectedVillagerModel;
import com.Harbinger.Spore.Client.Models.InfectedVindicatorModel;
import com.Harbinger.Spore.Client.Models.InfectedWandererModel;
import com.Harbinger.Spore.Client.Models.InfectedWitchModel;
import com.Harbinger.Spore.Client.Models.InfectedZombieVillager;
import com.Harbinger.Spore.Client.Models.InfestedContructModel;
import com.Harbinger.Spore.Client.Models.InquisitorModel;
import com.Harbinger.Spore.Client.Models.JagdhundModel;
import com.Harbinger.Spore.Client.Models.KnightModel;
import com.Harbinger.Spore.Client.Models.LaceratorModel;
import com.Harbinger.Spore.Client.Models.LeaperModel;
import com.Harbinger.Spore.Client.Models.LeftArmModel;
import com.Harbinger.Spore.Client.Models.LickerModel;
import com.Harbinger.Spore.Client.Models.MephiticModel;
import com.Harbinger.Spore.Client.Models.MossProtectorModel;
import com.Harbinger.Spore.Client.Models.MoundModel;
import com.Harbinger.Spore.Client.Models.NaiadModel;
import com.Harbinger.Spore.Client.Models.NaiadTritonModel;
import com.Harbinger.Spore.Client.Models.NuckelaveArmorModel;
import com.Harbinger.Spore.Client.Models.NuckelaveModel;
import com.Harbinger.Spore.Client.Models.OgreModel;
import com.Harbinger.Spore.Client.Models.OutpostWatcherModel;
import com.Harbinger.Spore.Client.Models.PCI_Model;
import com.Harbinger.Spore.Client.Models.PCI_ModelL;
import com.Harbinger.Spore.Client.Models.PlaguedModel;
import com.Harbinger.Spore.Client.Models.ProtectorModel;
import com.Harbinger.Spore.Client.Models.ProtoChritsmasHat;
import com.Harbinger.Spore.Client.Models.ProtoHivemindModel;
import com.Harbinger.Spore.Client.Models.ProtoRedesign;
import com.Harbinger.Spore.Client.Models.RangedBusserModel;
import com.Harbinger.Spore.Client.Models.RavenousJawModel;
import com.Harbinger.Spore.Client.Models.ReaperModel;
import com.Harbinger.Spore.Client.Models.ReconstructedMindModel;
import com.Harbinger.Spore.Client.Models.RightArmModel;
import com.Harbinger.Spore.Client.Models.RootsModel;
import com.Harbinger.Spore.Client.Models.SantaModel;
import com.Harbinger.Spore.Client.Models.SauglingModel;
import com.Harbinger.Spore.Client.Models.ScamperModel;
import com.Harbinger.Spore.Client.Models.ScamperVillagerModel;
import com.Harbinger.Spore.Client.Models.ScavengerModel;
import com.Harbinger.Spore.Client.Models.ScrewerSlasherModel;
import com.Harbinger.Spore.Client.Models.SculkHowlerModel;
import com.Harbinger.Spore.Client.Models.SegmentBase;
import com.Harbinger.Spore.Client.Models.SickleModel;
import com.Harbinger.Spore.Client.Models.SiegerArrowModel;
import com.Harbinger.Spore.Client.Models.SiegerModel;
import com.Harbinger.Spore.Client.Models.SiegerTailModel;
import com.Harbinger.Spore.Client.Models.SlasherModel;
import com.Harbinger.Spore.Client.Models.SmasherSlasherModel;
import com.Harbinger.Spore.Client.Models.SniperSpitterModel;
import com.Harbinger.Spore.Client.Models.SpecterModel;
import com.Harbinger.Spore.Client.Models.SpitterModel;
import com.Harbinger.Spore.Client.Models.SprayUsurperModel;
import com.Harbinger.Spore.Client.Models.StahlmorderModel;
import com.Harbinger.Spore.Client.Models.StalkerModel;
import com.Harbinger.Spore.Client.Models.StingerModel;
import com.Harbinger.Spore.Client.Models.StuddedProtectorModel;
import com.Harbinger.Spore.Client.Models.SwarmerHowlerModel;
import com.Harbinger.Spore.Client.Models.SyringeGunModel;
import com.Harbinger.Spore.Client.Models.SyringeGunModelArm;
import com.Harbinger.Spore.Client.Models.SyringeProjectileModel;
import com.Harbinger.Spore.Client.Models.TentacleSegmentModel;
import com.Harbinger.Spore.Client.Models.TentacleSegmentModel2;
import com.Harbinger.Spore.Client.Models.TentacleSegmentModel3;
import com.Harbinger.Spore.Client.Models.ThornModel;
import com.Harbinger.Spore.Client.Models.TransporterPhayresModel;
import com.Harbinger.Spore.Client.Models.TridentNaiadCharge;
import com.Harbinger.Spore.Client.Models.TumoralNukeModel;
import com.Harbinger.Spore.Client.Models.UmarmerModel;
import com.Harbinger.Spore.Client.Models.UsurperModel;
import com.Harbinger.Spore.Client.Models.VanguardModel;
import com.Harbinger.Spore.Client.Models.VigilModel;
import com.Harbinger.Spore.Client.Models.VigilSignModel;
import com.Harbinger.Spore.Client.Models.VolatileModel;
import com.Harbinger.Spore.Client.Models.WendigoModel;
import com.Harbinger.Spore.Client.Models.WombModel;
import com.Harbinger.Spore.Client.Models.WombModelStageII;
import com.Harbinger.Spore.Client.Models.WombModelStageIII;
import com.Harbinger.Spore.Client.Models.WormSegmentModel;
import com.Harbinger.Spore.Client.Models.WormTailModel;
import com.Harbinger.Spore.Client.Models.ZoaholicModel;
import com.Harbinger.Spore.Client.Models.bansheeHowlerModel;
import com.Harbinger.Spore.Client.Models.bloomingGargoyleModel;
import com.Harbinger.Spore.Client.Models.bomberGargoyleModel;
import com.Harbinger.Spore.Client.Models.brainMatterModel;
import com.Harbinger.Spore.Client.Models.gargoyleModel;
import com.Harbinger.Spore.Client.Models.gorgonModel;
import com.Harbinger.Spore.Client.Models.hVindicatorModel;
import com.Harbinger.Spore.Client.Models.hohlfresserHeadModel;
import com.Harbinger.Spore.Client.Models.hohlfresserTailModel;
import com.Harbinger.Spore.Client.Models.lacedThornsModel;
import com.Harbinger.Spore.Client.Models.livingArmorMkModel;
import com.Harbinger.Spore.Client.Models.ringerVigilModel;
import com.Harbinger.Spore.Client.Models.valkyrieGargoyleModel;
import com.Harbinger.Spore.Client.Models.verwahrungModel;
import com.Harbinger.Spore.Client.Models.NukeParts.BombFunnelModel;
import com.Harbinger.Spore.Client.Models.NukeParts.FireDiskModel;
import com.Harbinger.Spore.Client.Models.NukeParts.MushroomExplosionTop;
import com.Harbinger.Spore.Client.Renderers.AcidBulletRenderer;
import com.Harbinger.Spore.Client.Renderers.BairnRenderer;
import com.Harbinger.Spore.Client.Renderers.BileBulletRenderer;
import com.Harbinger.Spore.Client.Renderers.BiobloobRenderer;
import com.Harbinger.Spore.Client.Renderers.BiomassReconfiguratorRenderer;
import com.Harbinger.Spore.Client.Renderers.BloaterRenderer;
import com.Harbinger.Spore.Client.Renderers.BrainRemnantsRenderer;
import com.Harbinger.Spore.Client.Renderers.BraioRenderer;
import com.Harbinger.Spore.Client.Renderers.BraureiRenderer;
import com.Harbinger.Spore.Client.Renderers.BrotkatzeRenderer;
import com.Harbinger.Spore.Client.Renderers.BruteRenderer;
import com.Harbinger.Spore.Client.Renderers.BulletRender;
import com.Harbinger.Spore.Client.Renderers.BusserRenderer;
import com.Harbinger.Spore.Client.Renderers.CduRenderer;
import com.Harbinger.Spore.Client.Renderers.ChemistRenderer;
import com.Harbinger.Spore.Client.Renderers.ClawRenderer;
import com.Harbinger.Spore.Client.Renderers.ConductorRenderer;
import com.Harbinger.Spore.Client.Renderers.CorpseRenderer;
import com.Harbinger.Spore.Client.Renderers.DelusionareRenderer;
import com.Harbinger.Spore.Client.Renderers.DrownedFleshBombRenderer;
import com.Harbinger.Spore.Client.Renderers.FallenAcidSackRenderer;
import com.Harbinger.Spore.Client.Renderers.FleshBombRenderer;
import com.Harbinger.Spore.Client.Renderers.GargoyleRenderer;
import com.Harbinger.Spore.Client.Renderers.GastGaverRenderer;
import com.Harbinger.Spore.Client.Renderers.GazenRenderer;
import com.Harbinger.Spore.Client.Renderers.GoreBulletRenderer;
import com.Harbinger.Spore.Client.Renderers.GorgonRenderer;
import com.Harbinger.Spore.Client.Renderers.GrieferRenderer;
import com.Harbinger.Spore.Client.Renderers.GroberRenderer;
import com.Harbinger.Spore.Client.Renderers.HarpoonRenderer;
import com.Harbinger.Spore.Client.Renderers.HevokerRenderer;
import com.Harbinger.Spore.Client.Renderers.HindieRenderer;
import com.Harbinger.Spore.Client.Renderers.HiveTumorRenderer;
import com.Harbinger.Spore.Client.Renderers.HohlRenderer;
import com.Harbinger.Spore.Client.Renderers.HohlSegRenderer;
import com.Harbinger.Spore.Client.Renderers.HowdiArmRenderer;
import com.Harbinger.Spore.Client.Renderers.HowitzerRenderer;
import com.Harbinger.Spore.Client.Renderers.HowlerRenderer;
import com.Harbinger.Spore.Client.Renderers.HyperClawRenderer;
import com.Harbinger.Spore.Client.Renderers.HyperVindicatorRenderer;
import com.Harbinger.Spore.Client.Renderers.IllusionRenderer;
import com.Harbinger.Spore.Client.Renderers.IncubatorRenderer;
import com.Harbinger.Spore.Client.Renderers.InebriatorRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedDiseasedVillagerRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedDrownRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedEvokerRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedHazmatRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedHumanRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedHuskRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedPillagerRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedPlayerRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedVillagerRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedVindicatorRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedWandererRenderer;
import com.Harbinger.Spore.Client.Renderers.InfectedWitchRenderer;
import com.Harbinger.Spore.Client.Renderers.InfestedConstructRenderer;
import com.Harbinger.Spore.Client.Renderers.InquisitorRenderer;
import com.Harbinger.Spore.Client.Renderers.JagdhundRenderer;
import com.Harbinger.Spore.Client.Renderers.KnifeRenderer;
import com.Harbinger.Spore.Client.Renderers.KnightRenderer;
import com.Harbinger.Spore.Client.Renderers.KrakenRenderer;
import com.Harbinger.Spore.Client.Renderers.LaceratorRenderer;
import com.Harbinger.Spore.Client.Renderers.LeaperRenderer;
import com.Harbinger.Spore.Client.Renderers.LeviathanRenderer;
import com.Harbinger.Spore.Client.Renderers.LeviathanSegRenderer;
import com.Harbinger.Spore.Client.Renderers.LickerRenderer;
import com.Harbinger.Spore.Client.Renderers.MephticRenderer;
import com.Harbinger.Spore.Client.Renderers.MoundRenderer;
import com.Harbinger.Spore.Client.Renderers.NaiadRenderer;
import com.Harbinger.Spore.Client.Renderers.NucleaRenderer;
import com.Harbinger.Spore.Client.Renderers.NuclearBombRenderer;
import com.Harbinger.Spore.Client.Renderers.OgreRenderer;
import com.Harbinger.Spore.Client.Renderers.OutpostWatcherRenderer;
import com.Harbinger.Spore.Client.Renderers.OvergrownSpawnerRenderer;
import com.Harbinger.Spore.Client.Renderers.PlaguedRenderer;
import com.Harbinger.Spore.Client.Renderers.ProtectorRenderer;
import com.Harbinger.Spore.Client.Renderers.ProtoRenderer;
import com.Harbinger.Spore.Client.Renderers.RaidTendrilRenderer;
import com.Harbinger.Spore.Client.Renderers.ReaperRenderer;
import com.Harbinger.Spore.Client.Renderers.ReconMindRenderer;
import com.Harbinger.Spore.Client.Renderers.SauglingRenderer;
import com.Harbinger.Spore.Client.Renderers.ScamperHumanRenderer;
import com.Harbinger.Spore.Client.Renderers.ScavengerRenderer;
import com.Harbinger.Spore.Client.Renderers.ScentEntityRenderer;
import com.Harbinger.Spore.Client.Renderers.SickleRenderer;
import com.Harbinger.Spore.Client.Renderers.SiegerRenderer;
import com.Harbinger.Spore.Client.Renderers.SiegerTailRenderer;
import com.Harbinger.Spore.Client.Renderers.SlasherRenderer;
import com.Harbinger.Spore.Client.Renderers.SpearRenderer;
import com.Harbinger.Spore.Client.Renderers.SpecterRenderer;
import com.Harbinger.Spore.Client.Renderers.SpitterRenderer;
import com.Harbinger.Spore.Client.Renderers.StahlArmRenderer;
import com.Harbinger.Spore.Client.Renderers.StalhRenderer;
import com.Harbinger.Spore.Client.Renderers.StalkerRenderer;
import com.Harbinger.Spore.Client.Renderers.StingerRenderer;
import com.Harbinger.Spore.Client.Renderers.SyringeRenderer;
import com.Harbinger.Spore.Client.Renderers.TendrilRenderer;
import com.Harbinger.Spore.Client.Renderers.TentacleRenderer;
import com.Harbinger.Spore.Client.Renderers.ThornRenderer;
import com.Harbinger.Spore.Client.Renderers.ThrownBlockRenderer;
import com.Harbinger.Spore.Client.Renderers.ThrownBoomerangRenderer;
import com.Harbinger.Spore.Client.Renderers.ThrownMeleeItemRenderer;
import com.Harbinger.Spore.Client.Renderers.TumoroidNukeRenderer;
import com.Harbinger.Spore.Client.Renderers.UmarmedRenderer;
import com.Harbinger.Spore.Client.Renderers.UsurperRenderer;
import com.Harbinger.Spore.Client.Renderers.UsurperVomitRenderer;
import com.Harbinger.Spore.Client.Renderers.VanguardRenderer;
import com.Harbinger.Spore.Client.Renderers.VervaRenderer;
import com.Harbinger.Spore.Client.Renderers.VigilRenderer;
import com.Harbinger.Spore.Client.Renderers.VolatileRenderer;
import com.Harbinger.Spore.Client.Renderers.VomitRenderer;
import com.Harbinger.Spore.Client.Renderers.WaveRenderer;
import com.Harbinger.Spore.Client.Renderers.WendigoRenderer;
import com.Harbinger.Spore.Client.Renderers.ZoaholicRenderer;
import com.Harbinger.Spore.Core.SMenu;
import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Particles.AcidBulletParticle;
import com.Harbinger.Spore.Particles.AcidParticle;
import com.Harbinger.Spore.Particles.BashParticle;
import com.Harbinger.Spore.Particles.BileBulletParticle;
import com.Harbinger.Spore.Particles.BloodParticle;
import com.Harbinger.Spore.Particles.GoreBulletParticle;
import com.Harbinger.Spore.Particles.SlashParticle;
import com.Harbinger.Spore.Particles.SporeParticle;
import com.Harbinger.Spore.Particles.VomitParticle;
import com.Harbinger.Spore.Screens.AssimilationScreen;
import com.Harbinger.Spore.Screens.CDUScreen;
import com.Harbinger.Spore.Screens.CabinetScreen;
import com.Harbinger.Spore.Screens.ContainerScreen;
import com.Harbinger.Spore.Screens.GraftingRecipeScreen;
import com.Harbinger.Spore.Screens.GraftingScreen;
import com.Harbinger.Spore.Screens.IncubatorScreen;
import com.Harbinger.Spore.Screens.InjectionRecipeMenu;
import com.Harbinger.Spore.Screens.InjectionRecipeScreen;
import com.Harbinger.Spore.Screens.InjectionScreen;
import com.Harbinger.Spore.Screens.SurgeryRecipeScreen;
import com.Harbinger.Spore.Screens.SurgeryScreen;
import com.Harbinger.Spore.Screens.ZoaholicScreen;
import com.Harbinger.Spore.Sitems.Agents.AbstractSyringe;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import com.Harbinger.Spore.sEvents.SItemProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(
   modid = "spore",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class ClientModEvents {
   private ClientModEvents() {
   }

   @SubscribeEvent
   public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
      event.registerLayerDefinition(InfectedModel.LAYER_LOCATION, InfectedModel::createBodyLayer);
      event.registerLayerDefinition(InfectedHuskModel.LAYER_LOCATION, InfectedHuskModel::createBodyLayer);
      event.registerLayerDefinition(KnightModel.LAYER_LOCATION, KnightModel::createBodyLayer);
      event.registerLayerDefinition(GrieferModel.LAYER_LOCATION, GrieferModel::createBodyLayer);
      event.registerLayerDefinition(BraionmilModel.LAYER_LOCATION, BraionmilModel::createBodyLayer);
      event.registerLayerDefinition(InfectedVillagerModel.LAYER_LOCATION, InfectedVillagerModel::createBodyLayer);
      event.registerLayerDefinition(InfectedWitchModel.LAYER_LOCATION, InfectedWitchModel::createBodyLayer);
      event.registerLayerDefinition(LeaperModel.LAYER_LOCATION, LeaperModel::createBodyLayer);
      event.registerLayerDefinition(SlasherModel.LAYER_LOCATION, SlasherModel::createBodyLayer);
      event.registerLayerDefinition(SpitterModel.LAYER_LOCATION, SpitterModel::createBodyLayer);
      event.registerLayerDefinition(InfectedPillagerModel.LAYER_LOCATION, InfectedPillagerModel::createBodyLayer);
      event.registerLayerDefinition(InfectedVindicatorModel.LAYER_LOCATION, InfectedVindicatorModel::createBodyLayer);
      event.registerLayerDefinition(InfEvoClawModel.LAYER_LOCATION, InfEvoClawModel::createBodyLayer);
      event.registerLayerDefinition(InfectedSpearModel.LAYER_LOCATION, InfectedSpearModel::createBodyLayer);
      event.registerLayerDefinition(InfectedEvokerModel.LAYER_LOCATION, InfectedEvokerModel::createBodyLayer);
      event.registerLayerDefinition(HowlerModel.LAYER_LOCATION, HowlerModel::createBodyLayer);
      event.registerLayerDefinition(InfectedWandererModel.LAYER_LOCATION, InfectedWandererModel::createBodyLayer);
      event.registerLayerDefinition(StalkerModel.LAYER_LOCATION, StalkerModel::createBodyLayer);
      event.registerLayerDefinition(BruteModel.LAYER_LOCATION, BruteModel::createBodyLayer);
      event.registerLayerDefinition(BusserModel.LAYER_LOCATION, BusserModel::createBodyLayer);
      event.registerLayerDefinition(UsurperModel.LAYER_LOCATION, UsurperModel::createBodyLayer);
      event.registerLayerDefinition(ExplodingBusserModel.LAYER_LOCATION, ExplodingBusserModel::createBodyLayer);
      event.registerLayerDefinition(InfectedDrownModel.LAYER_LOCATION, InfectedDrownModel::createBodyLayer);
      event.registerLayerDefinition(InfectedPlayerModel.LAYER_LOCATION, InfectedPlayerModel::createBodyLayer);
      event.registerLayerDefinition(ScamperModel.LAYER_LOCATION, ScamperModel::createBodyLayer);
      event.registerLayerDefinition(UmarmerModel.LAYER_LOCATION, UmarmerModel::createBodyLayer);
      event.registerLayerDefinition(InfectedHazmatModel.LAYER_LOCATION, InfectedHazmatModel::createBodyLayer);
      event.registerLayerDefinition(InfectedHazmatWithTank.LAYER_LOCATION, InfectedHazmatWithTank::createBodyLayer);
      event.registerLayerDefinition(MoundModel.LAYER_LOCATION, MoundModel::createBodyLayer);
      event.registerLayerDefinition(BraionmilBabe.LAYER_LOCATION, BraionmilBabe::createBodyLayer);
      event.registerLayerDefinition(InfectedHazmatCoat.LAYER_LOCATION, InfectedHazmatCoat::createBodyLayer);
      event.registerLayerDefinition(VolatileModel.LAYER_LOCATION, VolatileModel::createBodyLayer);
      event.registerLayerDefinition(WombModel.LAYER_LOCATION, WombModel::createBodyLayer);
      event.registerLayerDefinition(VigilModel.LAYER_LOCATION, VigilModel::createBodyLayer);
      event.registerLayerDefinition(VigilSignModel.LAYER_LOCATION, VigilSignModel::createBodyLayer);
      event.registerLayerDefinition(GasMaskModel.LAYER_LOCATION, GasMaskModel::createBodyLayer);
      event.registerLayerDefinition(BileRound.LAYER_LOCATION, BileRound::createBodyLayer);
      event.registerLayerDefinition(ProtoHivemindModel.LAYER_LOCATION, ProtoHivemindModel::createBodyLayer);
      event.registerLayerDefinition(SiegerModel.LAYER_LOCATION, SiegerModel::createBodyLayer);
      event.registerLayerDefinition(GazenbrecherModel.LAYER_LOCATION, GazenbrecherModel::createBodyLayer);
      event.registerLayerDefinition(HowitzerModel.LAYER_LOCATION, HowitzerModel::createBodyLayer);
      event.registerLayerDefinition(SiegerTailModel.LAYER_LOCATION, SiegerTailModel::createBodyLayer);
      event.registerLayerDefinition(LickerModel.LAYER_LOCATION, LickerModel::createBodyLayer);
      event.registerLayerDefinition(HindieModel.LAYER_LOCATION, HindieModel::createBodyLayer);
      event.registerLayerDefinition(LaceratorModel.LAYER_LOCATION, LaceratorModel::createBodyLayer);
      event.registerLayerDefinition(SantaModel.LAYER_LOCATION, SantaModel::createBodyLayer);
      event.registerLayerDefinition(HindenXmaslightsModel.LAYER_LOCATION, HindenXmaslightsModel::createBodyLayer);
      event.registerLayerDefinition(TumoralNukeModel.LAYER_LOCATION, TumoralNukeModel::createBodyLayer);
      event.registerLayerDefinition(RightArmModel.LAYER_LOCATION, RightArmModel::createBodyLayer);
      event.registerLayerDefinition(LeftArmModel.LAYER_LOCATION, LeftArmModel::createBodyLayer);
      event.registerLayerDefinition(WendigoModel.LAYER_LOCATION, WendigoModel::createBodyLayer);
      event.registerLayerDefinition(InquisitorModel.LAYER_LOCATION, InquisitorModel::createBodyLayer);
      event.registerLayerDefinition(BrotkatzeModel.LAYER_LOCATION, BrotkatzeModel::createBodyLayer);
      event.registerLayerDefinition(BulletModel.LAYER_LOCATION, BulletModel::createBodyLayer);
      event.registerLayerDefinition(ThornModel.LAYER_LOCATION, ThornModel::createBodyLayer);
      event.registerLayerDefinition(PlaguedModel.LAYER_LOCATION, PlaguedModel::createBodyLayer);
      event.registerLayerDefinition(RangedBusserModel.LAYER_LOCATION, RangedBusserModel::createBodyLayer);
      event.registerLayerDefinition(StingerModel.LAYER_LOCATION, StingerModel::createBodyLayer);
      event.registerLayerDefinition(BraureiModel.LAYER_LOCATION, BraureiModel::createBodyLayer);
      event.registerLayerDefinition(ProtoRedesign.LAYER_LOCATION, ProtoRedesign::createBodyLayer);
      event.registerLayerDefinition(SiegerArrowModel.LAYER_LOCATION, SiegerArrowModel::createBodyLayer);
      event.registerLayerDefinition(DelusionerModel.LAYER_LOCATION, DelusionerModel::createBodyLayer);
      event.registerLayerDefinition(JagdhundModel.LAYER_LOCATION, JagdhundModel::createBodyLayer);
      event.registerLayerDefinition(verwahrungModel.LAYER_LOCATION, verwahrungModel::createBodyLayer);
      event.registerLayerDefinition(RootsModel.LAYER_LOCATION, RootsModel::createBodyLayer);
      event.registerLayerDefinition(GastgeberModel.LAYER_LOCATION, GastgeberModel::createBodyLayer);
      event.registerLayerDefinition(SpecterModel.LAYER_LOCATION, SpecterModel::createBodyLayer);
      event.registerLayerDefinition(InfestedContructModel.LAYER_LOCATION, InfestedContructModel::createBodyLayer);
      event.registerLayerDefinition(BrokenIronGolemModel.LAYER_LOCATION, BrokenIronGolemModel::createBodyLayer);
      event.registerLayerDefinition(brainMatterModel.LAYER_LOCATION, brainMatterModel::createBodyLayer);
      event.registerLayerDefinition(ZoaholicModel.LAYER_LOCATION, ZoaholicModel::createBodyLayer);
      event.registerLayerDefinition(IncubatorModel.LAYER_LOCATION, IncubatorModel::createBodyLayer);
      event.registerLayerDefinition(OutpostWatcherModel.LAYER_LOCATION, OutpostWatcherModel::createBodyLayer);
      event.registerLayerDefinition(BrainTentacleModel.LAYER_LOCATION, BrainTentacleModel::createBodyLayer);
      event.registerLayerDefinition(ArenaTendrilModel.LAYER_LOCATION, ArenaTendrilModel::createBodyLayer);
      event.registerLayerDefinition(OgreModel.LAYER_LOCATION, OgreModel::createBodyLayer);
      event.registerLayerDefinition(BloaterModel.LAYER_LOCATION, BloaterModel::createBodyLayer);
      event.registerLayerDefinition(ScavengerModel.LAYER_LOCATION, ScavengerModel::createBodyLayer);
      event.registerLayerDefinition(ProtoChritsmasHat.LAYER_LOCATION, ProtoChritsmasHat::createBodyLayer);
      event.registerLayerDefinition(NuckelaveModel.LAYER_LOCATION, NuckelaveModel::createBodyLayer);
      event.registerLayerDefinition(NuckelaveArmorModel.LAYER_LOCATION, NuckelaveArmorModel::createBodyLayer);
      event.registerLayerDefinition(WombModelStageIII.LAYER_LOCATION, WombModelStageIII::createBodyLayer);
      event.registerLayerDefinition(WombModelStageII.LAYER_LOCATION, WombModelStageII::createBodyLayer);
      event.registerLayerDefinition(ScamperVillagerModel.LAYER_LOCATION, ScamperVillagerModel::createBodyLayer);
      event.registerLayerDefinition(DrownedScamperModel.LAYER_LOCATION, DrownedScamperModel::createBodyLayer);
      event.registerLayerDefinition(HevokerModel.LAYER_LOCATION, HevokerModel::createBodyLayer);
      event.registerLayerDefinition(HevokerModelDead.LAYER_LOCATION, HevokerModelDead::createBodyLayer);
      event.registerLayerDefinition(DetasheHyperClaw.LAYER_LOCATION, DetasheHyperClaw::createBodyLayer);
      event.registerLayerDefinition(hVindicatorModel.LAYER_LOCATION, hVindicatorModel::createBodyLayer);
      event.registerLayerDefinition(ReconstructedMindModel.LAYER_LOCATION, ReconstructedMindModel::createBodyLayer);
      event.registerLayerDefinition(hohlfresserHeadModel.LAYER_LOCATION, hohlfresserHeadModel::createBodyLayer);
      event.registerLayerDefinition(WormSegmentModel.LAYER_LOCATION, WormSegmentModel::createBodyLayer);
      event.registerLayerDefinition(WormTailModel.LAYER_LOCATION, WormTailModel::createBodyLayer);
      event.registerLayerDefinition(ExperimentDormantLayerModel.LAYER_LOCATION, ExperimentDormantLayerModel::createBodyLayer);
      event.registerLayerDefinition(BiobloobModel.LAYER_LOCATION, BiobloobModel::createBodyLayer);
      event.registerLayerDefinition(ElytrumModel.LAYER_LOCATION, ElytrumModel::createBodyLayer);
      event.registerLayerDefinition(PCI_Model.LAYER_LOCATION, PCI_Model::createBodyLayer);
      event.registerLayerDefinition(PCI_ModelL.LAYER_LOCATION, PCI_ModelL::createBodyLayer);
      event.registerLayerDefinition(livingArmorMkModel.LAYER_LOCATION, livingArmorMkModel::createBodyLayer);
      event.registerLayerDefinition(ProtectorModel.LAYER_LOCATION, ProtectorModel::createBodyLayer);
      event.registerLayerDefinition(InebriaterModel.LAYER_LOCATION, InebriaterModel::createBodyLayer);
      event.registerLayerDefinition(SauglingModel.LAYER_LOCATION, SauglingModel::createBodyLayer);
      event.registerLayerDefinition(SmasherSlasherModel.LAYER_LOCATION, SmasherSlasherModel::createBodyLayer);
      event.registerLayerDefinition(HohlfresserSeg1Model.LAYER_LOCATION, HohlfresserSeg1Model::createBodyLayer);
      event.registerLayerDefinition(HohlfresserSeg2Model.LAYER_LOCATION, HohlfresserSeg2Model::createBodyLayer);
      event.registerLayerDefinition(HohlfresserSeg3Model.LAYER_LOCATION, HohlfresserSeg3Model::createBodyLayer);
      event.registerLayerDefinition(hohlfresserTailModel.LAYER_LOCATION, hohlfresserTailModel::createBodyLayer);
      event.registerLayerDefinition(bansheeHowlerModel.LAYER_LOCATION, bansheeHowlerModel::createBodyLayer);
      event.registerLayerDefinition(InfectedTechnoModel.LAYER_LOCATION, InfectedTechnoModel::createBodyLayer);
      event.registerLayerDefinition(DualSpitterModel.LAYER_LOCATION, DualSpitterModel::createBodyLayer);
      event.registerLayerDefinition(SniperSpitterModel.LAYER_LOCATION, SniperSpitterModel::createBodyLayer);
      event.registerLayerDefinition(GrabberSlasherModel.LAYER_LOCATION, GrabberSlasherModel::createBodyLayer);
      event.registerLayerDefinition(RavenousJawModel.LAYER_LOCATION, RavenousJawModel::createBodyLayer);
      event.registerLayerDefinition(lacedThornsModel.LAYER_LOCATION, lacedThornsModel::createBodyLayer);
      event.registerLayerDefinition(MephiticModel.LAYER_LOCATION, MephiticModel::createBodyLayer);
      event.registerLayerDefinition(InfectedZombieVillager.LAYER_LOCATION, InfectedZombieVillager::createBodyLayer);
      event.registerLayerDefinition(SculkHowlerModel.LAYER_LOCATION, SculkHowlerModel::createBodyLayer);
      event.registerLayerDefinition(SyringeGunModel.LAYER_LOCATION, SyringeGunModel::createBodyLayer);
      event.registerLayerDefinition(SyringeGunModelArm.LAYER_LOCATION, SyringeGunModelArm::createBodyLayer);
      event.registerLayerDefinition(SyringeProjectileModel.LAYER_LOCATION, SyringeProjectileModel::createBodyLayer);
      event.registerLayerDefinition(SegmentBase.LAYER_LOCATION, SegmentBase::createBodyLayer);
      event.registerLayerDefinition(TentacleSegmentModel.LAYER_LOCATION, TentacleSegmentModel::createBodyLayer);
      event.registerLayerDefinition(TentacleSegmentModel2.LAYER_LOCATION, TentacleSegmentModel2::createBodyLayer);
      event.registerLayerDefinition(TentacleSegmentModel3.LAYER_LOCATION, TentacleSegmentModel3::createBodyLayer);
      event.registerLayerDefinition(DelusionerEnchanterModel.LAYER_LOCATION, DelusionerEnchanterModel::createBodyLayer);
      event.registerLayerDefinition(SprayUsurperModel.LAYER_LOCATION, SprayUsurperModel::createBodyLayer);
      event.registerLayerDefinition(BurstUsurperModel.LAYER_LOCATION, BurstUsurperModel::createBodyLayer);
      event.registerLayerDefinition(NaiadModel.LAYER_LOCATION, NaiadModel::createBodyLayer);
      event.registerLayerDefinition(NaiadTritonModel.LAYER_LOCATION, NaiadTritonModel::createBodyLayer);
      event.registerLayerDefinition(ChemistModel.LAYER_LOCATION, ChemistModel::createBodyLayer);
      event.registerLayerDefinition(TridentNaiadCharge.LAYER_LOCATION, TridentNaiadCharge::createBodyLayer);
      event.registerLayerDefinition(VanguardModel.LAYER_LOCATION, VanguardModel::createBodyLayer);
      event.registerLayerDefinition(InfectedPillagerCaptainModel.LAYER_LOCATION, InfectedPillagerCaptainModel::createBodyLayer);
      event.registerLayerDefinition(BairnModel.LAYER_LOCATION, BairnModel::createBodyLayer);
      event.registerLayerDefinition(GrakensenkerModel.LAYER_LOCATION, GrakensenkerModel::createBodyLayer);
      event.registerLayerDefinition(StahlmorderModel.LAYER_LOCATION, StahlmorderModel::createBodyLayer);
      event.registerLayerDefinition(HivetumorModel.LAYER_LOCATION, HivetumorModel::createBodyLayer);
      event.registerLayerDefinition(GroberfubModel.LAYER_LOCATION, GroberfubModel::createBodyLayer);
      event.registerLayerDefinition(HarbingerModel.LAYER_LOCATION, HarbingerModel::createBodyLayer);
      event.registerLayerDefinition(ConductorModel.LAYER_LOCATION, ConductorModel::createBodyLayer);
      event.registerLayerDefinition(gargoyleModel.LAYER_LOCATION, gargoyleModel::createBodyLayer);
      event.registerLayerDefinition(ReaperModel.LAYER_LOCATION, ReaperModel::createBodyLayer);
      event.registerLayerDefinition(ForlornHowlerModel.LAYER_LOCATION, ForlornHowlerModel::createBodyLayer);
      event.registerLayerDefinition(SwarmerHowlerModel.LAYER_LOCATION, SwarmerHowlerModel::createBodyLayer);
      event.registerLayerDefinition(ringerVigilModel.LAYER_LOCATION, ringerVigilModel::createBodyLayer);
      event.registerLayerDefinition(ScrewerSlasherModel.LAYER_LOCATION, ScrewerSlasherModel::createBodyLayer);
      event.registerLayerDefinition(TransporterPhayresModel.LAYER_LOCATION, TransporterPhayresModel::createBodyLayer);
      event.registerLayerDefinition(StuddedProtectorModel.LAYER_LOCATION, StuddedProtectorModel::createBodyLayer);
      event.registerLayerDefinition(CollectorProtectorModel.LAYER_LOCATION, CollectorProtectorModel::createBodyLayer);
      event.registerLayerDefinition(MossProtectorModel.LAYER_LOCATION, MossProtectorModel::createBodyLayer);
      event.registerLayerDefinition(BulwarkProtectorModel.LAYER_LOCATION, BulwarkProtectorModel::createBodyLayer);
      event.registerLayerDefinition(gorgonModel.LAYER_LOCATION, gorgonModel::createBodyLayer);
      event.registerLayerDefinition(IchorGargoyleModel.LAYER_LOCATION, IchorGargoyleModel::createBodyLayer);
      event.registerLayerDefinition(bloomingGargoyleModel.LAYER_LOCATION, bloomingGargoyleModel::createBodyLayer);
      event.registerLayerDefinition(bomberGargoyleModel.LAYER_LOCATION, bomberGargoyleModel::createBodyLayer);
      event.registerLayerDefinition(valkyrieGargoyleModel.LAYER_LOCATION, valkyrieGargoyleModel::createBodyLayer);
      event.registerLayerDefinition(GorgonSpookyModel.LAYER_LOCATION, GorgonSpookyModel::createBodyLayer);
      event.registerLayerDefinition(BombFunnelModel.LAYER_LOCATION, BombFunnelModel::createBodyLayer);
      event.registerLayerDefinition(MushroomExplosionTop.LAYER_LOCATION, MushroomExplosionTop::createBodyLayer);
      event.registerLayerDefinition(FireDiskModel.LAYER_LOCATION, FireDiskModel::createBodyLayer);
      event.registerLayerDefinition(SickleModel.LAYER_LOCATION, SickleModel::createBodyLayer);
   }

   @SubscribeEvent
   public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)Sentities.INF_HUMAN.get(), InfectedHumanRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_HUSK.get(), InfectedHuskRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.KNIGHT.get(), KnightRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.GRIEFER.get(), GrieferRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BRAIOMIL.get(), BraioRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_VILLAGER.get(), InfectedVillagerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_WITCH.get(), InfectedWitchRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.LEAPER.get(), LeaperRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SLASHER.get(), SlasherRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SPITTER.get(), SpitterRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_PILLAGER.get(), InfectedPillagerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_VINDICATOR.get(), InfectedVindicatorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.CLAW.get(), ClawRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THROWN_SPEAR.get(), SpearRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_EVOKER.get(), InfectedEvokerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HOWLER.get(), HowlerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_WANDERER.get(), InfectedWandererRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.STALKER.get(), StalkerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BRUTE.get(), BruteRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BUSSER.get(), BusserRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.VOLATILE.get(), VolatileRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_DROWNED.get(), InfectedDrownRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_HAZMAT.get(), InfectedHazmatRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_PLAYER.get(), InfectedPlayerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.LACERATOR.get(), LaceratorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THORN.get(), ThornRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SCAMPER.get(), ScamperHumanRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.MOUND.get(), MoundRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.RECONSTRUCTOR.get(), BiomassReconfiguratorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.PROTO.get(), ProtoRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.VIGIL.get(), VigilRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.USURPER.get(), UsurperRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.UMARMED.get(), UmarmedRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SIEGER.get(), SiegerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.GAZENBREACHER.get(), GazenRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HINDENBURG.get(), HindieRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SIEGER_TAIL.get(), SiegerTailRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.LICKER.get(), LickerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HOWIT_ARM.get(), HowdiArmRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.TUMOROID_NUKE.get(), TumoroidNukeRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.VERVA.get(), VervaRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.WENDIGO.get(), WendigoRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INQUISITOR.get(), InquisitorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BROTKATZE.get(), BrotkatzeRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.OGRE.get(), OgreRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_CONSTRUCT.get(), InfestedConstructRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.JAGD.get(), JagdhundRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.PLAGUED.get(), PlaguedRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.WAVE.get(), WaveRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.ILLUSION.get(), IllusionRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.GASTGABER.get(), GastGaverRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SPECTER.get(), SpecterRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HOWITZER.get(), HowitzerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BLOATER.get(), BloaterRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SCAVENGER.get(), ScavengerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.NUCLEA.get(), NucleaRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HEVOKER.get(), HevokerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HVINDICATOR.get(), HyperVindicatorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INF_DISEASED_VILLAGER.get(), InfectedDiseasedVillagerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.CONDUCTOR.get(), ConductorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.GARGOYLE.get(), GargoyleRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.REAPER.get(), ReaperRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.ACID_BALL.get(), ThrownItemRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.VOMIT_BALL.get(), VomitRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SPIT.get(), BulletRender::new);
      event.registerEntityRenderer((EntityType)Sentities.BILE.get(), ThrownItemRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.ACID.get(), ThrownItemRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THROWN_TOOL.get(), ThrownMeleeItemRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THROWN_TUMOR.get(), ThrownItemRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.FLESH_BOMB.get(), FleshBombRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THROWN_BLOCK.get(), ThrownBlockRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SCENT.get(), ScentEntityRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.TENDRIL.get(), TendrilRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.STINGER.get(), StingerRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BRAUREI.get(), BraureiRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.DELUSIONARE.get(), DelusionareRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.ARENA_TENDRIL.get(), RaidTendrilRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HEVOKER_ARM.get(), HyperClawRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.NUKE.get(), NuclearBombRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.TENTACLE.get(), TentacleRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HOHLFRESSER.get(), HohlRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HOHLFRESSER_SEG.get(), HohlSegRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BIOBLOOB.get(), BiobloobRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THROWN_KNIFE.get(), KnifeRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THROWN_SICKEL.get(), SickleRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THROWN_BOOMERANG.get(), ThrownBoomerangRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.PROTECTOR.get(), ProtectorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.INEBRIATER.get(), InebriatorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.SAUGLING.get(), SauglingRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.CORPSE_PIECE.get(), CorpseRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.MEPHETIC.get(), MephticRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.THROWN_SYRINGE.get(), SyringeRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.USURPER_VOMIT_BALL.get(), UsurperVomitRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.NAIAD.get(), NaiadRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.CHEMIST.get(), ChemistRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.VANGUARD.get(), VanguardRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BAIRN.get(), BairnRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.KRAKEN.get(), KrakenRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.STALH.get(), StalhRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.STAHL_ARM.get(), StahlArmRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HIVETUMOR.get(), HiveTumorRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.LEVIATHAN.get(), LeviathanRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.LEVIATHAN_SEG.get(), LeviathanSegRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.DROWNED_FLESH_BOMB.get(), DrownedFleshBombRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.FALLEN_ACID_BULB.get(), FallenAcidSackRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.GROBER.get(), GroberRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.GORGON.get(), GorgonRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.HARPOON.get(), HarpoonRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.GORE_BULLET.get(), GoreBulletRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.ASSASSIN_BULLET.get(), AcidBulletRenderer::new);
      event.registerEntityRenderer((EntityType)Sentities.BILE_BULLET.get(), BileBulletRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)SblockEntities.OVERGROWN_SPAWNER.get(), new OvergrownSpawnerRenderer());
      event.registerBlockEntityRenderer((BlockEntityType)SblockEntities.BRAIN_REMNANTS.get(), new BrainRemnantsRenderer());
      event.registerBlockEntityRenderer((BlockEntityType)SblockEntities.ZOAHOLIC.get(), new ZoaholicRenderer());
      event.registerBlockEntityRenderer((BlockEntityType)SblockEntities.INCUBATOR.get(), new IncubatorRenderer());
      event.registerBlockEntityRenderer((BlockEntityType)SblockEntities.OUTPOST_WATCHER.get(), new OutpostWatcherRenderer());
      event.registerBlockEntityRenderer((BlockEntityType)SblockEntities.HIVE_SPAWN.get(), new ReconMindRenderer());
      event.registerBlockEntityRenderer((BlockEntityType)SblockEntities.CDU.get(), new CduRenderer());
   }

   @SubscribeEvent
   public static void clientSetup(FMLClientSetupEvent event) {
      SItemProperties.addCustomItemProperties();
      event.enqueueWork(() -> {
         MenuScreens.register(SMenu.CONTAINER.get(), ContainerScreen::new);
         MenuScreens.register(SMenu.SURGERY_MENU.get(), SurgeryScreen::new);
         MenuScreens.register(SMenu.INJECTION_MENU.get(), InjectionScreen::new);
         MenuScreens.register(SMenu.INCUBATOR_MENU.get(), IncubatorScreen::new);
         MenuScreens.register(SMenu.ZOAHOLIC_MENU.get(), ZoaholicScreen::new);
         MenuScreens.register(SMenu.SURGERY_RECIPE_MENU.get(), SurgeryRecipeScreen::new);
         MenuScreens.register(SMenu.CDU_MENU.get(), CDUScreen::new);
         MenuScreens.register(SMenu.ASSIMILATION_MENU.get(), AssimilationScreen::new);
         MenuScreens.register(SMenu.CABINET_MENU.get(), CabinetScreen::new);
         MenuScreens.register(SMenu.GRAFTING_MENU.get(), GraftingScreen::new);
         MenuScreens.register(SMenu.GRAFTING_RECIPE_MENU.get(), GraftingRecipeScreen::new);
         MenuScreens.register(SMenu.INJECTION_RECIPE_MENU.get(), InjectionRecipeScreen::new);
      });
   }

   @SubscribeEvent
   public static void addLayers(EntityRenderersEvent.AddLayers event) {
      event.getSkins().forEach((name) -> {
         LivingEntityRenderer patt31602$temp = event.getSkin(name);
         if (patt31602$temp instanceof PlayerRenderer renderer) {
            renderer.addLayer(new CustomArmorLayer(renderer));
         }

      });
      LivingEntityRenderer var2 = event.getRenderer(EntityType.ARMOR_STAND);
      if (var2 instanceof ArmorStandRenderer renderer) {
         renderer.addLayer(new CustomArmorLayer(renderer));
      }

      tryToAddArmorToType(event);
      var2 = event.getRenderer(EntityType.HORSE);
      if (var2 instanceof HorseRenderer renderer) {
         ModelPart root = event.getEntityModels().bakeLayer(ModelLayers.HORSE);
         renderer.addLayer(new CustomHorseArmorLayer(renderer, root));
      }

   }

   private static void tryToAddArmorToType(EntityRenderersEvent.AddLayers event) {
      for(EntityType type : ForgeRegistries.ENTITY_TYPES.getValues()) {
         if (type != null) {
            try {
               LivingEntityRenderer var6 = event.getRenderer(type);
               if (var6 instanceof HumanoidMobRenderer) {
                  HumanoidMobRenderer renderer = (HumanoidMobRenderer)var6;
                  renderer.addLayer(new CustomArmorLayer(renderer));
               }
            } catch (Exception e) {
               ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
               Spore.LOGGER.warn("Could not apply custom armor to entity type {}: {}", id, e.getMessage());
            }
         }
      }

   }

   @SubscribeEvent
   public static void registerParticle(RegisterParticleProvidersEvent event) {
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.SPORE_PARTICLE.get(), SporeParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.ACID_PARTICLE.get(), AcidParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.BLOOD_PARTICLE.get(), BloodParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.SPORE_SLASH.get(), SlashParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.SPORE_IMPACT.get(), BashParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.VOMIT.get(), VomitParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.VOMIT_BONE.get(), VomitParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.VOMIT_ORES.get(), VomitParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.ACID_BULLET.get(), AcidBulletParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.GORE_BULLET.get(), GoreBulletParticle.Provider::new);
      Minecraft.getInstance().particleEngine.register((ParticleType)Sparticles.BILE_BULLET.get(), BileBulletParticle.Provider::new);
   }

   @SubscribeEvent
   public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
      for(Item item : Sitems.TINTABLE_ITEMS) {
         if (item instanceof SporeWeaponData data) {
            event.register((itemStack, tintIndex) -> tintIndex == 0 ? data.getVariant(itemStack).getColor() : -1, new ItemLike[]{item});
         }

         if (item instanceof SporeArmorData data) {
            event.register((itemStack, tintIndex) -> tintIndex == 0 ? data.getVariant(itemStack).getColor() : -1, new ItemLike[]{item});
         }

         if (item instanceof AbstractSyringe data) {
            event.register((itemStack, tintIndex) -> tintIndex == 0 ? data.getColor() : -1, new ItemLike[]{item});
         }
      }

   }

   public static void openInjectionScreen(Player player) {
      InjectionRecipeMenu menu = new InjectionRecipeMenu(1, player.getInventory());
      Minecraft.getInstance().setScreen(new InjectionRecipeScreen(menu, player.getInventory(), Component.literal("")));
   }
}
