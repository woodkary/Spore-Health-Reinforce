package com.Harbinger.Spore.Fluids;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

public class BileLiquid extends FluidType {
   public static final ResourceLocation FLUID_STILL = new ResourceLocation("spore:block/bile_static");
   public static final ResourceLocation FLUID_FLOWING = new ResourceLocation("spore:block/bile_flow");
   public static final ResourceLocation OVERLAY = new ResourceLocation("spore:textures/extra/bile_overlay.png");

   public BileLiquid(Properties properties) {
      super(properties);
   }

   public void initializeClient(Consumer consumer) {
      consumer.accept(new IClientFluidTypeExtensions() {
         public ResourceLocation getStillTexture() {
            return BileLiquid.FLUID_STILL;
         }

         public ResourceLocation getFlowingTexture() {
            return BileLiquid.FLUID_FLOWING;
         }
      });
   }

   public boolean canSwim(Entity entity) {
      return entity instanceof UtilityEntity || entity instanceof Infected;
   }

   public boolean canExtinguish(Entity entity) {
      return true;
   }

   public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
      if (!(entity instanceof UtilityEntity) && !(entity instanceof Infected) && !(entity instanceof TrueCalamity)) {
         movementVector.scale((double)0.4F);
         if (entity.tickCount % 40 == 0) {
            for(MobEffectInstance instance : bileEffects()) {
               entity.addEffect(instance);
            }

            entity.hurt(entity.damageSources().generic(), 1.0F);
         }
      } else {
         movementVector.scale(1.2);
         entity.setDeltaMovement(entity.getDeltaMovement().add((double)0.0F, 0.01, (double)0.0F));
         entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 0, 40));
         gravity = (double)0.0F;
      }

      return super.move(state, entity, movementVector, gravity);
   }

   public static List<MobEffectInstance> bileEffects() {
      List<MobEffectInstance> values = new ArrayList<>();
      values.add(new MobEffectInstance((MobEffect)Seffects.BILED.get(), 200, 0));
      return values;
   }
}
