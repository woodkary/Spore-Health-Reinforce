package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporePickaxeItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class InfectedPickaxe extends SporePickaxeItems {
   public InfectedPickaxe() {
      super((double)(Integer)SConfig.SERVER.inf_pickaxe_damage.get(), (double)2.5F, (double)3.0F, (Integer)SConfig.SERVER.inf_pickaxe_durability.get(), 7, "pickaxe");
   }

   private List getBlockChange() {
      List<StoreDouble> blocks = new ArrayList();

      for(String str : (List<String>)SConfig.DATAGEN.block_infection.get()) {
         String[] string = str.split("\\|");
         Block blockCon1 = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[0]));
         Block blockCon2 = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[1]));
         if (blockCon1 != null && blockCon2 != null) {
            blocks.add(new StoreDouble(blockCon1, blockCon2));
         }
      }

      return blocks;
   }

   public void changeBlock(Level level, BlockPos pos) {
      List<StoreDouble> values = this.getBlockChange();
      if (!values.isEmpty()) {
         BlockState state = level.getBlockState(pos);

         for(StoreDouble storeDouble : values) {
            if (storeDouble.value1.equals(state.getBlock()) && !storeDouble.value1.defaultBlockState().isAir()) {
               level.setBlock(pos, storeDouble.value2.defaultBlockState(), 2);
               level.sendBlockUpdated(pos, storeDouble.value1.defaultBlockState(), storeDouble.value2.defaultBlockState(), 2);
               level.addParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get(), (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)0.0F, (double)0.2F, (double)0.0F);
            }
         }
      }

   }

   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      BlockPos pos = context.getClickedPos();
      Level level = context.getLevel();
      if (player instanceof ServerPlayer serverPlayer) {
         serverPlayer.playNotifySound((SoundEvent)Ssounds.INFECTED_PICKAXE.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
         this.lookForOres(pos, level, serverPlayer);
         this.hurtTool(context.getItemInHand(), serverPlayer, 1);
      }

      return super.useOn(context);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity living, LivingEntity entity) {
      entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1));
      return super.hurtEnemy(stack, living, entity);
   }

   private void lookForOres(BlockPos start, Level level, ServerPlayer player) {
      AABB searchArea = AABB.ofSize(new Vec3((double)start.getX(), (double)start.getY(), (double)start.getZ()), (double)35.0F, (double)35.0F, (double)35.0F);
      BlockPos targetOrePos = null;

      for(BlockPos pos : BlockPos.betweenClosed(Mth.floor(searchArea.minX), Mth.floor(searchArea.minY), Mth.floor(searchArea.minZ), Mth.floor(searchArea.maxX), Mth.floor(searchArea.maxY), Mth.floor(searchArea.maxZ))) {
         if (level.getBlockState(pos).is(TagKey.create(Registries.BLOCK, new ResourceLocation("forge:ores"))) && player.getRandom().nextFloat() < 0.3F) {
            targetOrePos = pos;
            break;
         }
      }

      if (targetOrePos == null) {
         player.displayClientMessage(Component.translatable("inf_pickaxe_no_ores"), true);
      } else {
         Component component = Component.translatable(level.getBlockState(targetOrePos).getBlock().getDescriptionId());
         Component component1 = Component.translatable("inf_pickaxe_found_ores");
         String var10000 = component1.getString();
         String string = var10000 + component.getString();
         player.displayClientMessage(Component.literal(string), true);
         Vec3 startVec = new Vec3((double)start.getX(), (double)start.getY(), (double)start.getZ());
         Vec3 endVec = new Vec3((double)targetOrePos.getX(), (double)targetOrePos.getY(), (double)targetOrePos.getZ());
         Vec3 direction = endVec.subtract(startVec).normalize();
         double distance = startVec.distanceTo(endVec);

         for(int i = 0; (double)i <= distance; ++i) {
            Vec3 current = startVec.add(direction.scale((double)i));
            BlockPos pos = new BlockPos((int)current.x, (int)current.y, (int)current.z);
            this.changeBlock(level, pos);
         }

         player.getCooldowns().addCooldown(this, 40);
      }
   }

   static record StoreDouble(Block value1, Block value2) {
   }
}
