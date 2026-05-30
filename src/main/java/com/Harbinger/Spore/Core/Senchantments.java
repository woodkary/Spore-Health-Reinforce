package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.Senchantments.CorrosivePotency;
import com.Harbinger.Spore.Senchantments.CorrosiveThorns;
import com.Harbinger.Spore.Senchantments.CryogenicAspect;
import com.Harbinger.Spore.Senchantments.GastricSpewage;
import com.Harbinger.Spore.Senchantments.MutagenicReactant;
import com.Harbinger.Spore.Senchantments.SymbioticReconstitution;
import com.Harbinger.Spore.Senchantments.UnwaveringNature;
import com.Harbinger.Spore.Senchantments.VoraciousMaw;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Senchantments {
   public static final DeferredRegister ENCHANTMENTS;
   public static final EnchantmentCategory FUNGAL_ITEMS;
   public static final RegistryObject SYMBIOTIC_RECONSTITUTION;
   public static final RegistryObject CRYOGENIC_ASPECT;
   public static final RegistryObject GASTRIC_SPEWAGE;
   public static final RegistryObject CORROSIVE_POTENCY;
   public static final RegistryObject SERRATED_THORNS;
   public static final RegistryObject VORACIOUS_MAW;
   public static final RegistryObject UNWAVERING_NATURE;
   public static final RegistryObject MUTAGENIC_REACTANT;

   public static void register(IEventBus eventBus) {
      ENCHANTMENTS.register(eventBus);
   }

   static {
      ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, "spore");
      FUNGAL_ITEMS = EnchantmentCategory.create("fungal_items", Item::canBeDepleted);
      SYMBIOTIC_RECONSTITUTION = ENCHANTMENTS.register("symbiotic_reconstitution", () -> new SymbioticReconstitution(new EquipmentSlot[0]));
      CRYOGENIC_ASPECT = ENCHANTMENTS.register("cryogenic_aspect", () -> new CryogenicAspect(new EquipmentSlot[0]));
      GASTRIC_SPEWAGE = ENCHANTMENTS.register("gastric_spewage", () -> new GastricSpewage(new EquipmentSlot[0]));
      CORROSIVE_POTENCY = ENCHANTMENTS.register("corrosive_potency", () -> new CorrosivePotency(new EquipmentSlot[0]));
      SERRATED_THORNS = ENCHANTMENTS.register("serrated_thorns", CorrosiveThorns::new);
      VORACIOUS_MAW = ENCHANTMENTS.register("voracious_maw", () -> new VoraciousMaw(new EquipmentSlot[0]));
      UNWAVERING_NATURE = ENCHANTMENTS.register("unwavering_nature", () -> new UnwaveringNature(new EquipmentSlot[0]));
      MUTAGENIC_REACTANT = ENCHANTMENTS.register("mutagenic_reactant", () -> new MutagenicReactant(new EquipmentSlot[0]));
   }
}
