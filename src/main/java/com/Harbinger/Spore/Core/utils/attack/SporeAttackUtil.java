package com.Harbinger.Spore.Core.utils.attack;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.*;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.IFakeDataHealthEntity;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Utility.Vanguard;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsBaseItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public final class SporeAttackUtil implements IAttack {
    public static final IAttack INSTANCE = BytecodeUtil.createHiddenSingletonInstance(IAttack.class, SporeAttackUtil.class);
    public void attack(LivingEntity target, Mob attacker){
        float baseDamage=0.0f;
        attack(target,attacker,baseDamage);
    }
    private boolean specialAttack(Player player, Entity entity, Level level, ItemStack stack,float baseDamage,boolean cooldownFlag) {
        // 原版暴击判定
        boolean isCrit = player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() &&
                !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) &&
                !player.isPassenger() && cooldownFlag && !player.isSprinting();

        // 横扫判定
        boolean canSweep = player.onGround() && player.walkDist - player.walkDistO < player.getSpeed() &&
                cooldownFlag && !player.isSprinting();

        // 击退判定
        boolean doKnockback = cooldownFlag&&player.isSprinting();

        // 冲刺击退
        if (doKnockback && entity instanceof LivingEntity victim) {
            victim.knockback(0.5F,
                    Mth.sin(player.getYRot() * ((float) Math.PI / 180F)),
                    -Mth.cos(player.getYRot() * ((float) Math.PI / 180F)));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.setSprinting(false);
        }

        // 暴击特效
        if (isCrit) {
            this.playSound(level,null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.crit(entity);
        }

        // ✅ 横扫特效 + 横扫之刃附魔加成
        if (canSweep && player instanceof ServerPlayer sp) {
            AABB area = player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, entity);
            // 获取横扫附魔倍率
            float sweepingRatio = EnchantmentHelper.getSweepingDamageRatio(player);
            for (Entity near : level.getEntitiesOfClass(Entity.class, area)) {
                if (near.equals(player)) continue;
                if (near instanceof LivingEntity nearby) {
                    if (!nearby.equals(entity) && !player.isAlliedTo(nearby) &&
                            player.distanceToSqr(nearby) < 9.0D) {
                        nearby.knockback(0.4F,
                                Mth.sin(player.getYRot() * ((float) Math.PI / 180F)),
                                -Mth.cos(player.getYRot() * ((float) Math.PI / 180F)));

                        float sweepDamage = baseDamage * sweepingRatio+1.0f;
                        attackWithWeapon(sp,nearby,stack,sweepDamage,false);
                    }
                }
            }

            this.playSound(level,null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
            sp.sweepAttack();
        }

        // 冷却完成攻击音效
        if (player.getAttackStrengthScale(0.5F) > 0.9F && !isCrit) {
            this.playSound(level,null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return isCrit; // ✅ 返回是否暴击
    }
    private void attackWithWeapon(Player player,
                                 LivingEntity target,
                                 ItemStack stack,
                                 float baseDamage,
                                 boolean isCrit) {
        int sharpnessLevel = stack.getEnchantmentLevel(Enchantments.SHARPNESS);
        float bonusDamage = sharpnessLevel > 0 ? 0.5f + 0.5f * sharpnessLevel : 0.0f;
        baseDamage+=bonusDamage;
        int smiteLevel = stack.getEnchantmentLevel(Enchantments.SMITE);
        if(target.getMobType()==MobType.UNDEAD){
            baseDamage+=smiteLevel*2.5f;
        }
        int BOALevel = stack.getEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS);
        if(target.getMobType()==MobType.ARTHROPOD){
            baseDamage+=BOALevel*2.5f;
        }
        if(isCrit){
            baseDamage*=1.5f;
        }
        target.setLastHurtByPlayer(player);
        attack(target,player,baseDamage);
    }
    public void attack(Player player,Entity t) {
        attack(player,t,player.getMainHandItem());
    }
    public void attack(Player player,Entity t,ItemStack stack){
        Item i=stack.getItem();
        t= ParentUtil.INSTANCE.getUltimateParent(t);
        if(!(t instanceof LivingEntity target)||!target.isAlive()){
            return;
        }
        Level level = player.level;
        if (i instanceof SporeToolsBaseItem baseItem) {
            float damage = (float) baseItem.getAttackDamageByDefault(stack);
            float scale = player.getAttackStrengthScale(0.5F);
            damage *= 0.2F + scale * scale * 0.8F;
            boolean isCritical = specialAttack(player, target, level, stack, damage, scale > 0.9f);
            attackWithWeapon(player, target, stack, damage, isCritical);
        }

    }
    public void attack(LivingEntity target, LivingEntity attacker,float baseDamage){
        boolean mobAttackFlag = attacker instanceof Mob;
        // ============ 原来的攻击逻辑 ============
        if (mobAttackFlag&&target instanceof Player) {
            return;
        }
        float damage = baseDamage;
        // 力量效果修正
        MobEffectInstance strength = attacker.getEffect(MobEffects.DAMAGE_BOOST);
        if (strength != null) {
            damage += 2*(strength.getAmplifier() + 1);
        }
        DamageSource damageSource;
        if(attacker instanceof Player player){
            damageSource=attacker.damageSources().playerAttack(player);
        }else if(attacker instanceof Infected inf){
            damageSource=inf.getCustomDamage(inf);
        }else if(attacker instanceof UtilityEntity util){
            damageSource=util.getCustomDamage(util);
        } else {
            damageSource = attacker.damageSources().mobAttack(attacker);
        }
        damage=damageReduction(target,damage,damageSource);
        if(attacker instanceof ArmorPersentageBypass bypass){
            float recalculatedDamage=bypass.amountOfDamage(damage);
            if (recalculatedDamage >= 0.0F && damage < recalculatedDamage) {
                damage = recalculatedDamage;
            }
        }
        damage+=0.0005f*(target.getMaxHealth()+target.getHealth());
        dealDamage(target, attacker, damageSource, damage);
    }

    public void dealDamage(LivingEntity target,float damage){
        dealDamage(target,null,
                target.damageSources().fellOutOfWorld(),
                damage);
    }
    public void dealDamage(LivingEntity target,DamageSource damageSource,float damage){
        dealDamage(target,null,damageSource,damage);
    }
    public void dealDamage(LivingEntity target, LivingEntity attacker,DamageSource damageSource, float damage) {
        if(attacker!=null){
            target.setLastHurtByMob(attacker);
        }
        if(attacker instanceof Player player){
            target.setLastHurtByPlayer(player);
        }
        boolean isSpore = SporeJudge.isSporeEntity(target);
        if(damageSource.is(DamageTypes.FREEZE)&& isSpore){
            damage*=5.0f;
        }
        float targetHealth = target.getHealth();
        boolean willDie = targetHealth - damage <= 0.0f;
        if(willDie){
            target.getPersistentData().putBoolean("SporeDeeaadfd", true);
            EntityHeealuthManager.INSTANCE.setHeealtthDelta(target,Float.NEGATIVE_INFINITY);
        }
        int flag=0;
        if(isSpore){
            flag=1;
            SporeEntityHeeaafastthManager.INSTANCE.hurrt(target,damageSource,damage);
            target.getCombatTracker().recordDamage(damageSource,damage);
        }else{
            EntityHeealuthManager.INSTANCE.hurt(target, damage,damageSource);
        }
        willDie |= target.getHealth() <= 0.0f;
        if(willDie){
            target.getPersistentData().putBoolean("SporeDeeaadfd", true);
            EntityHeealuthManager.INSTANCE.setHeealtthDelta(target,Float.NEGATIVE_INFINITY);
        }
        this.playHurtSound(target, damageSource);
        float v = target.getRandom().nextFloat() - target.getRandom().nextFloat();
        target.animateHurt(v);
        target.hurtDuration = 10;
        target.hurtTime = target.hurtDuration;
        // ⚡ 目标死亡逻辑
        if (willDie) {
            if (attacker!=null) {
                if(!attacker.level.isClientSide) {
                    attacker.killedEntity((ServerLevel) attacker.level(), target);
                }
                double maxHealth = target.attributes.getValue(Attributes.MAX_HEALTH);
                attacker.awardKillScore(target, target.deathScore+(int) (maxHealth*0.5),damageSource);
            }
            addKills(attacker,2);
            if(flag==1){
                SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(target,0.0f);
                if(target instanceof IFakeDataHealthEntity fake){
                    fake.clearDefault0HllealthDelta();
                }
            }
            EntityHeealuthManager.INSTANCE.killEntity(target,damageSource);
        }
    }
    private void addKills(LivingEntity attacker,Integer count) {
        if(attacker instanceof Calamity cal){
            cal.setKills(cal.getKills()+count);
        }else if(attacker instanceof Infected inf){
            inf.setKills(inf.getKills()+count);
        }else if(attacker instanceof Vanguard van){
            van.addKills(count);
        }
    }

    public float damageReduction(LivingEntity entity, float rawDamage, DamageSource source){
        // 原版护甲减伤
        float armor = (float) entity.attributes.getValue(Attributes.ARMOR);
        float toughness = (float) entity.attributes.getValue(Attributes.ARMOR_TOUGHNESS);

        // Vanilla reduction formula
        float armorReduction = 1.0F - Math.min(20.0F, Math.max(armor / 5.0F,
                armor - rawDamage / (2.0F + toughness / 4.0F))) / 25.0F;
        float reducedDamage = rawDamage * armorReduction;

        //抗性减伤
        if (entity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            int reduction = (entity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - reduction;
            float f = reducedDamage * (float)j;
            float d = reducedDamage;
            reducedDamage = Math.max(f / 25.0F, 0.0F);
            float damageLeft = d - reducedDamage;
            if (damageLeft > 0.0F && damageLeft < 3.4028235E37F) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.awardStat(Stats.CUSTOM.get(Stats.DAMAGE_RESISTED), Math.round(damageLeft * 10.0F));
                }
            }
        }

        // 附魔减伤（Protection）
        int protLevel = EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source);
        if (protLevel > 0) {
            reducedDamage *= (1.0F - protLevel * 0.04F);
        }



        return reducedDamage;
    }
    public void playHurtSound(LivingEntity entity, DamageSource source) {
        SoundEvent sound = entity.getHurtSound(source);
        if (sound != null) {
            playSound(entity.level(), null, entity.blockPosition(), sound, entity.getSoundSource(), 1.0f, entity.getVoicePitch());
        }
    }
    public void playSound(Level level, @Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource source, float soundVolume, float voicePitch) {
        playSound(level,player, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, soundEvent, source, soundVolume, voicePitch);
    }
    public void playSound(Level level, @Nullable Player p_46543_, double p_46544_, double p_46545_, double p_46546_, SoundEvent p_46547_, SoundSource p_46548_, float p_46549_, float p_46550_) {
        playSeededSound(level,p_46543_, p_46544_, p_46545_, p_46546_, p_46547_, p_46548_, p_46549_, p_46550_, level.threadSafeRandom.nextLong());
    }
    void playSeededSound(Level level,@Nullable Player p_220363_, double p_220364_, double p_220365_, double p_220366_, SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {
        if(level instanceof ServerLevel serverLevel) {
            playSeededSound(serverLevel,p_220363_, p_220364_, p_220365_, p_220366_, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(p_220367_), p_220368_, p_220369_, p_220370_, p_220371_);
        }else{
            ClientLevel clientLevel = (ClientLevel)level;
            playSeededSound(clientLevel,p_220363_, p_220364_, p_220365_, p_220366_, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(p_220367_), p_220368_, p_220369_, p_220370_, p_220371_);
        }
    }
    void playSeededSound(ServerLevel serverLevel, @Nullable Player p_263330_, double p_263393_, double p_263369_, double p_263354_, Holder<SoundEvent> p_263412_, SoundSource p_263338_, float p_263352_, float p_263390_, long p_263403_) {
        serverLevel.getServer().getPlayerList().broadcast(p_263330_, p_263393_, p_263369_, p_263354_, (p_263412_.value()).getRange(p_263352_), serverLevel.dimension(), new ClientboundSoundPacket(p_263412_, p_263338_, p_263393_, p_263369_, p_263354_, p_263352_, p_263390_, p_263403_));
    }
    void playSeededSound(ClientLevel clientLevel, @Nullable Player p_263381_, double p_263372_, double p_263404_, double p_263365_, Holder<SoundEvent> p_263335_, SoundSource p_263417_, float p_263416_, float p_263349_, long p_263408_) {
        if (p_263381_ == clientLevel.minecraft.player) {
            clientLevel.playSound(p_263372_, p_263404_, p_263365_, p_263335_.value(), p_263417_, p_263416_, p_263349_, false, p_263408_);
        }
    }
    public Entity getTargetedEntity(Player player, double maxDistance) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle();
        Vec3 endPosition = eyePosition.add(lookVector.scale(maxDistance));
        AABB searchBox = (new AABB(eyePosition, endPosition)).inflate(1.0);
        Entity closestEntity = null;
        double closestDistance = maxDistance;

        for(Entity entity : player.level().getEntities(player, searchBox)) {
            if (entity != player) {
                AABB entityBox = entity.getBoundingBox().inflate(0.0);
                Optional<Vec3> hitResult = entityBox.clip(eyePosition, endPosition);
                if (hitResult.isPresent()) {
                    Vec3 hitPoint = hitResult.get();
                    double distance = eyePosition.distanceTo(hitPoint);
                    if (!isLineOfSightBlocked(player, eyePosition, hitPoint) && distance < closestDistance) {
                        closestDistance = distance;
                        closestEntity = entity;
                    }
                }
            }
        }

        return closestEntity;
    }

    private boolean isLineOfSightBlocked(Player player, Vec3 start, Vec3 end) {
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        HitResult result = player.level().clip(context);
        return result.getType() != HitResult.Type.MISS;
    }
    public void dropAllDeathLoot(LivingEntity liv,Vec3 position,DamageSource source,@Nullable Player player) {
        if(liv.level.isClientSide){
            return;
        }
        Entity entity = source.getEntity();
        int i = ForgeHooks.getLootingLevel(liv, entity, source);
        captureDrops(liv);
        boolean flag = true;
        dropFromLootTable(liv,position,source, flag,player);
        liv.dropCustomDeathLoot(source, i, flag);

        liv.dropEquipment();
        dropExperience(liv,position);
        for (ItemEntity e : captureDrops(liv)) {
            liv.level.addFreshEntity(e);
        }

    }
    private Collection<ItemEntity> captureDrops(LivingEntity liv) {
        if(liv.captureDrops==null){
            liv.captureDrops = new ArrayList<>();
        }
        return liv.captureDrops;
    }

    private void dropFromLootTable(LivingEntity liv,Vec3 position,DamageSource source, boolean p_21022_,Player player) {
        ResourceLocation resourcelocation = liv.getLootTable();
        LootTable loottable = liv.level.getServer().getLootData().getLootTable(resourcelocation);
        LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)liv.level)).withParameter(LootContextParams.THIS_ENTITY, liv).withParameter(LootContextParams.ORIGIN, position).withParameter(LootContextParams.DAMAGE_SOURCE, source).withOptionalParameter(LootContextParams.KILLER_ENTITY, source.getEntity()).withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, source.getDirectEntity());
        if (p_21022_ && player != null) {
            lootparams$builder = lootparams$builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());
        }

        LootParams lootparams = lootparams$builder.create(LootContextParamSets.ENTITY);
        loottable.getRandomItems(lootparams, liv.getLootTableSeed(), RandomItemConsumer.newInstance(liv));
    }
    private void dropExperience(LivingEntity liv,Vec3 position) {
        if (liv.level instanceof ServerLevel serverLevel&& !liv.wasExperienceConsumed()) {
            int reward = liv.getExperienceReward();
            ExperienceOrb.award(serverLevel, position, reward);
        }

    }
    private static final class RandomItemConsumer implements Consumer<ItemStack> {
        public static final Class<? extends Consumer<ItemStack>> consumerClass = (Class<? extends Consumer<ItemStack>>) BytecodeUtil.resolveHiddenClassOrSelf(
                RandomItemConsumer.class,
                LivingEntity.class
        );
        private static MethodHandle constructor;
        static {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    consumerClass,
                    RandomItemConsumer.class,
                    LivingEntity.class
            );
        }
        public static Consumer<ItemStack> newInstance(LivingEntity liv){
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    consumerClass,
                    RandomItemConsumer.class,
                    LivingEntity.class
            );

            if(constructor!=null){
                try{
                    return (Consumer<ItemStack>) constructor.invoke(liv);
                } catch (Throwable e) {
                    LogUtil.error("failed to invoke RandomItemConsumer constructor");
                }
            }
            return new RandomItemConsumer(liv);
        }
        private final LivingEntity liv;
        private RandomItemConsumer(LivingEntity liv) {
            this.liv = liv;
        }

        @Override
        public void accept(ItemStack itemStack) {
            liv.spawnAtLocation(itemStack);
        }
    }
}



