package com.Harbinger.Spore.Core;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class SAttributes {
   public static final DeferredRegister<Attribute> ATTRIBUTES;
   public static final RegistryObject<Attribute> TOXICITY;
   public static final RegistryObject<Attribute> REJUVENATION;
   public static final RegistryObject<Attribute> LOCALIZATION;
   public static final RegistryObject<Attribute> LACERATION;
   public static final RegistryObject<Attribute> CORROSIVES;
   public static final RegistryObject<Attribute> BALLISTIC;
   public static final RegistryObject<Attribute> GRINDING;

   public static void register(IEventBus eventBus) {
      ATTRIBUTES.register(eventBus);
   }

   private static String constructLang(String value) {
      return "attribute.name.spore." + value;
   }

   static {
      ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, "spore");
      TOXICITY = ATTRIBUTES.register("toxicity", () -> (new RangedAttribute(constructLang("toxicity"), (double)0.0F, (double)0.0F, (double)64.0F)).setSyncable(true));
      REJUVENATION = ATTRIBUTES.register("rejuvenation", () -> (new RangedAttribute(constructLang("rejuvenation"), (double)0.0F, (double)0.0F, (double)64.0F)).setSyncable(true));
      LOCALIZATION = ATTRIBUTES.register("localization", () -> (new RangedAttribute(constructLang("localization"), (double)0.0F, (double)0.0F, (double)64.0F)).setSyncable(true));
      LACERATION = ATTRIBUTES.register("laceration", () -> (new RangedAttribute(constructLang("laceration"), (double)0.0F, (double)0.0F, (double)64.0F)).setSyncable(true));
      CORROSIVES = ATTRIBUTES.register("corrosives", () -> (new RangedAttribute(constructLang("corrosives"), (double)0.0F, (double)0.0F, (double)64.0F)).setSyncable(true));
      BALLISTIC = ATTRIBUTES.register("ballistic", () -> (new RangedAttribute(constructLang("ballistic"), (double)0.0F, (double)0.0F, (double)64.0F)).setSyncable(true));
      GRINDING = ATTRIBUTES.register("grinding", () -> (new RangedAttribute(constructLang("grinding"), (double)0.0F, (double)0.0F, (double)64.0F)).setSyncable(true));
   }
}
