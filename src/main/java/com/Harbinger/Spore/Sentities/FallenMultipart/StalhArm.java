package com.Harbinger.Spore.Sentities.FallenMultipart;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.BaseEntities.FallenMultipartEntity;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class StalhArm extends FallenMultipartEntity {
   public StalhArm(EntityType type, Level level) {
      super(type, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.sta_blade_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.sta_hp.get() / (double)4.0F * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.sta_armor.get() / (double)4.0F * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }
}
