package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.Screens.AssimilationMenu;
import com.Harbinger.Spore.Screens.CDUMenu;
import com.Harbinger.Spore.Screens.CabinetMenu;
import com.Harbinger.Spore.Screens.ContainerMenu;
import com.Harbinger.Spore.Screens.GraftingMenu;
import com.Harbinger.Spore.Screens.GraftingRecipeMenu;
import com.Harbinger.Spore.Screens.IncubatorMenu;
import com.Harbinger.Spore.Screens.InjectionMenu;
import com.Harbinger.Spore.Screens.InjectionRecipeMenu;
import com.Harbinger.Spore.Screens.SurgeryMenu;
import com.Harbinger.Spore.Screens.SurgeryRecipeMenu;
import com.Harbinger.Spore.Screens.ZoaholicMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SMenu {
   public static final DeferredRegister<MenuType<?>> MENU;
   public static final RegistryObject<MenuType<ContainerMenu>> CONTAINER;
   public static final RegistryObject<MenuType<SurgeryMenu>> SURGERY_MENU;
   public static final RegistryObject<MenuType<InjectionMenu>> INJECTION_MENU;
   public static final RegistryObject<MenuType<InjectionRecipeMenu>> INJECTION_RECIPE_MENU;
   public static final RegistryObject<MenuType<AssimilationMenu>> ASSIMILATION_MENU;
   public static final RegistryObject<MenuType<IncubatorMenu>> INCUBATOR_MENU;
   public static final RegistryObject<MenuType<ZoaholicMenu>> ZOAHOLIC_MENU;
   public static final RegistryObject<MenuType<SurgeryRecipeMenu>> SURGERY_RECIPE_MENU;
   public static final RegistryObject<MenuType<CDUMenu>> CDU_MENU;
   public static final RegistryObject<MenuType<CabinetMenu>> CABINET_MENU;
   public static final RegistryObject<MenuType<GraftingMenu>> GRAFTING_MENU;
   public static final RegistryObject<MenuType<GraftingRecipeMenu>> GRAFTING_RECIPE_MENU;

   public static void register(IEventBus eventBus) {
      MENU.register(eventBus);
   }

   static {
      MENU = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "spore");
      CONTAINER = MENU.register("container", () -> IForgeMenuType.create(ContainerMenu::new));
      SURGERY_MENU = MENU.register("surgery_menu", () -> IForgeMenuType.create(SurgeryMenu::new));
      INJECTION_MENU = MENU.register("injection_menu", () -> IForgeMenuType.create(InjectionMenu::new));
      INJECTION_RECIPE_MENU = MENU.register("injection_recipe_menu", () -> IForgeMenuType.create(InjectionRecipeMenu::new));
      ASSIMILATION_MENU = MENU.register("assimilation_menu", () -> IForgeMenuType.create(AssimilationMenu::new));
      INCUBATOR_MENU = MENU.register("incubator_menu", () -> IForgeMenuType.create(IncubatorMenu::new));
      ZOAHOLIC_MENU = MENU.register("zoaholic_menu", () -> IForgeMenuType.create(ZoaholicMenu::new));
      SURGERY_RECIPE_MENU = MENU.register("surgery_recipe_menu", () -> IForgeMenuType.create(SurgeryRecipeMenu::new));
      CDU_MENU = MENU.register("cdu_menu", () -> IForgeMenuType.create(CDUMenu::new));
      CABINET_MENU = MENU.register("cabinet_menu", () -> IForgeMenuType.create(CabinetMenu::new));
      GRAFTING_MENU = MENU.register("grafting_menu", () -> IForgeMenuType.create(GraftingMenu::new));
      GRAFTING_RECIPE_MENU = MENU.register("grafting_recipe_menu", () -> IForgeMenuType.create(GraftingRecipeMenu::new));
   }
}
