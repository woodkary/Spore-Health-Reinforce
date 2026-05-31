package com.Harbinger.Spore.Core.utils.attack;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface IAttack {
    void attack(LivingEntity target, Mob attacker);
    void attack(Player player, Entity target);
    void attack(LivingEntity target, LivingEntity attacker,float baseDamage);
    void dealDamage(LivingEntity target, float damage);
    void dealDamage(LivingEntity target, DamageSource source, float damage);
    float damageReduction(LivingEntity entity, float rawDamage, DamageSource source);
    void playHurtSound(LivingEntity entity, DamageSource source);
    void playSound(Level level, @Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource source, float soundVolume, float voicePitch);
    void playSound(Level level,@Nullable Player p_46543_, double p_46544_, double p_46545_, double p_46546_, SoundEvent p_46547_, SoundSource p_46548_, float p_46549_, float p_46550_);
    Entity getTargetedEntity(Player player, double maxDistance);
    void dropAllDeathLoot(LivingEntity liv, Vec3 position, DamageSource source, Player player);
}

