package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.Effect.Biled;
import com.Harbinger.Spore.Effect.Corrosion;
import com.Harbinger.Spore.Effect.FrostBite;
import com.Harbinger.Spore.Effect.HealingInhibition;
import com.Harbinger.Spore.Effect.Madness;
import com.Harbinger.Spore.Effect.Marker;
import com.Harbinger.Spore.Effect.Mycelium;
import com.Harbinger.Spore.Effect.Starvation;
import com.Harbinger.Spore.Effect.Symbiosis;
import com.Harbinger.Spore.Effect.Uneasy;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Seffects {
   public static final DeferredRegister<MobEffect> MOB_EFFECTS;
   public static final RegistryObject<MobEffect> MYCELIUM;
   public static final RegistryObject<MobEffect> MADNESS;
   public static final RegistryObject<MobEffect> STARVATION;
   public static final RegistryObject<MobEffect> UNEASY;
   public static final RegistryObject<MobEffect> MARKER;
   public static final RegistryObject<MobEffect> CORROSION;
   public static final RegistryObject<MobEffect> FROSTBITE;
   public static final RegistryObject<MobEffect> BILED;
   public static final RegistryObject<MobEffect> SYMBIOSIS;
   public static final RegistryObject<MobEffect> HEALING_INHIBITION;

   public static void register(IEventBus eventBus) {
      MOB_EFFECTS.register(eventBus);
   }

   static {
      MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "spore");
      MYCELIUM = MOB_EFFECTS.register("mycelium_ef", Mycelium::new);
      MADNESS = MOB_EFFECTS.register("madness", Madness::new);
      STARVATION = MOB_EFFECTS.register("starvation", Starvation::new);
      UNEASY = MOB_EFFECTS.register("uneasy", Uneasy::new);
      MARKER = MOB_EFFECTS.register("marker", () -> (new Marker()).addAttributeModifier(Attributes.FOLLOW_RANGE, "91AEAA56-376B-4498-935B-2F7F68070635", (double)0.5F, Operation.MULTIPLY_TOTAL));
      CORROSION = MOB_EFFECTS.register("corrosion", () -> (new Corrosion()).addAttributeModifier(Attributes.ARMOR, "91AEAA56-376B-4498-935B-2F7F68070635", (double)-0.1F, Operation.MULTIPLY_TOTAL));
      FROSTBITE = MOB_EFFECTS.register("frostbite", () -> (new FrostBite()).addAttributeModifier(Attributes.MOVEMENT_SPEED, "6ee43a05-b6c8-4abf-8c1d-6e36007724e0", (double)-0.1F, Operation.MULTIPLY_TOTAL));
      BILED = MOB_EFFECTS.register("biled", () -> (new Biled()).addAttributeModifier(Attributes.MOVEMENT_SPEED, "4be9c86d-ff59-4eed-b3e4-a07d72a241af", (double)-0.2F, Operation.MULTIPLY_TOTAL).addAttributeModifier(Attributes.ATTACK_SPEED, "325ec79e-11cc-499b-9557-9f168c4e7ce6", (double)-0.2F, Operation.MULTIPLY_TOTAL).addAttributeModifier(Attributes.ATTACK_DAMAGE, "8bd4f8a7-3677-42bc-8e63-129ab2330906", (double)-0.2F, Operation.MULTIPLY_TOTAL));
      SYMBIOSIS = MOB_EFFECTS.register("symbiosis", () -> (new Symbiosis()).addAttributeModifier(Attributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", (double)0.2F, Operation.MULTIPLY_TOTAL).addAttributeModifier(Attributes.ATTACK_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", (double)0.2F, Operation.MULTIPLY_TOTAL).addAttributeModifier(Attributes.ATTACK_DAMAGE, "91AEAA56-376B-4498-935B-2F7F68070635", (double)4.0F, Operation.ADDITION).addAttributeModifier(Attributes.MAX_HEALTH, "91AEAA56-376B-4498-935B-2F7F68070635", (double)6.0F, Operation.ADDITION));
      HEALING_INHIBITION = MOB_EFFECTS.register("healing_inhibition", HealingInhibition::new);
   }
}
