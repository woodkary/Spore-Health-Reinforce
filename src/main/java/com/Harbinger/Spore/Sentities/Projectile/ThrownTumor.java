package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Fluids.BileLiquid;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PlayMessages;

import java.util.List;

public class ThrownTumor extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(ThrownTumor.class, EntityDataSerializers.INT);
    public ThrownTumor(EntityType<? extends ThrownTumor> type, Level level) {
        super(type, level);
    }
    public MobEffect mobEffect;
    public int duration = 600;
    public int potion_level = 1;
    public Level.ExplosionInteraction explode = Level.ExplosionInteraction.NONE;

    public ThrownTumor(Level level, LivingEntity entity) {
        super(Sentities.THROWN_TUMOR.get(), entity, level);
    }

    public ThrownTumor(PlayMessages.SpawnEntity spawnEntity, Level level) {
        super(Sentities.THROWN_TUMOR.get(), level);
    }

    @Override
    protected Item getDefaultItem() {
        return switch (entityData.get(TYPE)) {
            case 1 -> Sitems.SICKEN_TUMOR.get();
            case 2 -> Sitems.CALCIFIED_TUMOR.get();
            case 3 -> Sitems.FROZEN_TUMOR.get();
            case 4 -> Sitems.BILE_TUMOR.get();
            default -> Sitems.TUMOR.get();
        };
    }

    public void handleEntityEvent(byte value) {
        if (value == 3) {
            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - 0.5D) * 0.08D, ((double)this.random.nextFloat() - 0.5D) * 0.08D, ((double)this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }

    }

    public void setMobEffect(MobEffect effect){
        this.mobEffect = effect;
    }
    public void setExplode(Level.ExplosionInteraction value){
        this.explode = value;
    }

    private double getCalamityBallisticMutation(Calamity calamity) {
        AttributeInstance ballistic = calamity.getAttribute(SAttributes.BALLISTIC.get());
        return ballistic != null ? ballistic.getValue() : 0.0;
    }

    private void explodeExtraHurt(float explodeRadius) {
        if (!(this.getOwner() instanceof Calamity calamity)) {
            return;
        }
        double ballistic = getCalamityBallisticMutation(calamity);
        if (ballistic <= 0.0) {
            return;
        }
        this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(explodeRadius),
                living -> living.isAlive() && !SporeJudge.isSporeEntity(living) && !(living instanceof Player)
        ).forEach(living -> SporeAttackUtil.INSTANCE.dealDamage(
                living,
                calamity,
                living.damageSources().explosion(this, calamity),
                (float) ballistic
        ));
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide){
            float explodeRadius = (float) SConfig.SERVER.tumor_explosion.get();
            explodeExtraHurt(explodeRadius);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), explodeRadius, explode);
            AABB aabb = this.getBoundingBox().inflate(3);
            List<Entity> entities = level().getEntities(this,aabb);
            switch (entityData.get(TYPE)){
                case 0 : summonPotionEffects();
                break;
                case 1 : poisonTargets(entities);
                break;
                case 2 : damageTargets(entities);
                break;
                case 3 : freezeTargets(entities);
                break;
                case 4 : bileTargets(entities);
                break;
            }
            this.discard();
        }
    }

    public void summonPotionEffects(){
        AreaEffectCloud cloud = EntityType.AREA_EFFECT_CLOUD.create(level());
        if (mobEffect != null && cloud != null){
            cloud.addEffect(new MobEffectInstance(mobEffect,duration,potion_level));
            cloud.setDuration(160);
            cloud.setRadius(2f);
            cloud.moveTo(this.getX(), this.getY(), this.getZ());
            level().addFreshEntity(cloud);
        }
    }
    public void poisonTargets(List<Entity> entityList){
        for (Entity entity : entityList){
            if (entity instanceof LivingEntity livingEntity){
                livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON,200,1));
                livingEntity.addEffect(new MobEffectInstance(Seffects.CORROSION.get(),200,1));
            }
        }
    }
    public void bileTargets(List<Entity> entityList){
        for (Entity entity : entityList){
            if (entity instanceof LivingEntity livingEntity){
                for (MobEffectInstance instance : BileLiquid.bileEffects()){
                    livingEntity.addEffect(instance);
                }
            }
        }
    }
    public void freezeTargets(List<Entity> entityList){
        for (Entity entity : entityList){
            if (entity instanceof LivingEntity target){
                MobEffectInstance instance = target.getEffect(Seffects.FROSTBITE.get());
                int intensity = instance == null ? 0 : instance.getAmplifier()+1;
                target.addEffect(new MobEffectInstance(Seffects.FROSTBITE.get(),600,intensity));
                if (SporeJudge.isSporeEntity(target)) {
                    SporeAttackUtil.INSTANCE.dealDamage(target, this.getOwner() instanceof LivingEntity living ? living : null, target.damageSources().freeze(), 5.0f);
                }
            }
        }
    }
    public void damageTargets(List<Entity> entityList){
        for (Entity entity : entityList){
            if (entity instanceof LivingEntity target){
                Entity ownerEntity = this.getOwner();
                boolean isTargetOwner = target.equals(ownerEntity);
                if (ownerEntity instanceof LivingEntity owner){
                    DamageSource source = this.level().damageSources().mobProjectile(this, owner);
                    if (isTargetOwner) {
                        target.hurt(source, 10.0F);
                    } else {
                        target.hurtTime = 0;
                        target.invulnerableTime = 0;
                        SporeAttackUtil.INSTANCE.dealDamage(target, owner, source, 10.0F);
                    }
                }
            }
        }
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TYPE,0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(TYPE,tag.getInt("type"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("type",this.entityData.get(TYPE));
    }
    public void setType(int value){
        entityData.set(TYPE,value);
    }
}
