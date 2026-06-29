package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Recipes.EntityContainer;
import com.Harbinger.Spore.Recipes.WombRecipe;
import com.Harbinger.Spore.Screens.AssimilationMenu;
import com.Harbinger.Spore.Sentities.BaseEntities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Womb extends Organoid implements MenuProvider, IDieWithDiscardEntity {
    private static final EntityDataAccessor<Integer> COUNTER = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BIOMASS = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> LOCATION = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> SPECIAL_DEAD = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.BOOLEAN);
    private int breakCounter;
    private final List<String> attributeIDs = new ArrayList<>();
    private int pendingAdaptationCounts;
    private Vec3 lastLegalPosition = Vec3.ZERO;
    public Womb(EntityType<? extends PathfinderMob> type, Level level, TERRAIN terrain, BlockPos pos) {
        super(type, level);
        this.entityData.set(STATE,terrain.value);
        this.setLocation(pos);
        this.setLegalPosition(Vec3.ZERO);
    }
    public Womb(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.entityData.set(STATE,0);
        this.setLocation(BlockPos.ZERO);
        this.setLegalPosition(Vec3.ZERO);
    }
    private int eatingTicks = 0;

    public List<String> getAttributeIDs() {
        return attributeIDs;
    }

    @Override
    public List<? extends String> getDropList() {
        return SConfig.DATAGEN.womb_loot.get();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(COUNTER, 0);
        this.entityData.define(BIOMASS, 0);
        this.entityData.define(STATE, 0);
        this.entityData.define(LOCATION, BlockPos.ZERO);
        this.entityData.define(SPECIAL_DEAD, false);
    }

    protected SoundEvent getAmbientSound() {
        return Ssounds.WOMB_AMBIENT.get();
    }

    public SoundEvent getDeathSound() {
        return Ssounds.INF_DAMAGE.get();
    }

    @Override
    public void tick() {
        super.tick();
        tickLegalPosition();
        if (this.entityData.get(BIOMASS) >= SConfig.SERVER.reconstructor_biomass.get()){
            this.summon(this,false);
        }
        if (this.entityData.get(COUNTER) < (SConfig.SERVER.recontructor_clock.get() * 20)){
            this.entityData.set(COUNTER , this.entityData.get(COUNTER) + 1);
        }else{
            this.entityData.set(COUNTER,0);
            this.entityData.set(BIOMASS , this.entityData.get(BIOMASS) + 1);
        }
        if (this.random.nextInt(100) == 0){
            this.CallNearbyInfected();
        }
        if (this.random.nextInt(40) == 0){
            this.AssimilateNearbyInfected();
        }
        if (this.random.nextInt(20) == 0 && isEating()){
            this.playSound(SoundEvents.GENERIC_EAT);
        }
        if (this.eatingTicks > 0){
            --eatingTicks;
        }
    }

    public void setBiomass(int biomass){
        entityData.set(BIOMASS,biomass);
    }
    public int getBiomass(){
        return entityData.get(BIOMASS);
    }

    public void setLocation(BlockPos pos){
        entityData.set(LOCATION,pos);
    }
    public BlockPos getLocation(){
        return entityData.get(LOCATION);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Counter", entityData.get(COUNTER));
        tag.putInt("Biomass", entityData.get(BIOMASS));
        tag.putInt("State", entityData.get(STATE));
        tag.putInt("LocationX", this.getLocation().getX());
        tag.putInt("LocationY", this.getLocation().getY());
        tag.putInt("LocationZ", this.getLocation().getZ());
        ListTag teamTag = new ListTag();
        for (String member : attributeIDs) {
            teamTag.add(StringTag.valueOf(member));
        }
        tag.put("mutations", teamTag);
        tag.putInt("adaptationCount", getDamageAdaptationCount());
        addAdditionalLegalPositionData(tag);
    }

    public boolean isEating(){
        return eatingTicks > 0;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(COUNTER, tag.getInt("Counter"));
        entityData.set(BIOMASS, tag.getInt("Biomass"));
        entityData.set(STATE, tag.getInt("State"));
        int i = tag.getInt("LocationX");
        int j = tag.getInt("LocationY");
        int k = tag.getInt("LocationZ");
        this.setLocation(new BlockPos(i, j, k));
        attributeIDs.clear();
        ListTag teamTag = tag.getList("mutations", Tag.TAG_STRING);
        for (int l = 0; l < teamTag.size(); l++) {
            attributeIDs.add(teamTag.getString(l));
        }
        if (tag.contains("adaptationCount")) {
            setAdaptationCount(tag.getInt("adaptationCount"));
        }
        readAdditionalLegalPositionData(tag);
    }
    private void CallNearbyInfected(){
        if (!this.level().isClientSide) {
        AABB hitbox = this.getBoundingBox().inflate(50);
        List<Entity> entities = this.level().getEntities(this, hitbox ,EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        for (Entity en : entities){
            if (en instanceof Infected infected){
                infected.setSearchPos(new BlockPos((int) this.getX(),(int)this.getY(),(int)this.getZ()));
            }
         }
        }
    }
    public Optional<WombRecipe> getCurrentRecipe(Entity entity) {
        EntityContainer container = new EntityContainer(entity);
        return this.level().getRecipeManager().getRecipeFor(WombRecipe.WombRecipeType.INSTANCE, container, level());
    }

    public void addMutation(WombRecipe recipe){
        this.attributeIDs.add(recipe.getAttribute());
    }
    private void AssimilateNearbyInfected(){
        if (!this.level().isClientSide) {
            List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(0.1), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            for (Entity en : entities) {
                if (en instanceof Infected infected) {
                    this.setBiomass(this.getBiomass() + calculateAssimilation(infected) + infected.getKills());
                    Optional<WombRecipe> recipe = getCurrentRecipe(infected);
                    recipe.ifPresent(this::addMutation);
                    infected.discard();
                    if (this.level() instanceof ServerLevel serverLevel) {
                        double x0 = this.getX() - (random.nextFloat() - 0.1) * 0.1D;
                        double y0 = this.getY() + (random.nextFloat() - 0.25) * 0.25D * 5;
                        double z0 = this.getZ() + (random.nextFloat() - 0.1) * 0.1D;
                        serverLevel.sendParticles(Sparticles.BLOOD_PARTICLE.get(), x0, y0, z0, 8, 0, 0, 0, 1);
                    }
                    this.playSound(SoundEvents.GENERIC_EAT);
                    this.eatingTicks = this.eatingTicks + 80;
                    break;
                }
            }
        }
    }

    public int calculateAssimilation(Entity entity){
        int value = SConfig.SERVER.reconstructor_assimilation.get();
        if (entity instanceof Hyper){
            return value * 4;
        }
        if (entity instanceof EvolvedInfected){
            return value * 2;
        }
        return value;
    }

    @Override
    public void actuallyHurt(DamageSource source, float amount) {
        if (!source.is(DamageTypes.FREEZE)) {
            this.pendingAdaptationCounts = Math.min(this.pendingAdaptationCounts + 1, 10);
        }
        super.actuallyHurt(source, amount);
    }


    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        if (BIOMASS.equals(dataAccessor)){
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(dataAccessor);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int age = 1;
        if (this.getBiomass() > (SConfig.SERVER.reconstructor_biomass.get()/4) && this.getBiomass() < (SConfig.SERVER.reconstructor_biomass.get()/2)){
            age = 2;
        }else if (this.getBiomass() > (SConfig.SERVER.reconstructor_biomass.get()/2)){
            age = 3;
        }
        return super.getDimensions(pose).scale(age);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, SConfig.SERVER.reconstructor_hp.get() * SConfig.SERVER.global_health.get())
                .add(Attributes.ARMOR, SConfig.SERVER.reconstructor_armor.get() * SConfig.SERVER.global_armor.get())
                .add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1);

    }
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }


    @Override
    public void aiStep() {
        super.aiStep();
        if (breakCounter < 40){
            breakCounter++;
        }else{
            if (this.getLastDamageSource() == this.damageSources().inWall() && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)){
                AABB aabb = this.getBoundingBox().inflate(0.2,0,0.2);
                boolean flag = false;
                for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
                    BlockState blockstate = this.level().getBlockState(blockpos);
                    if (blockstate.getDestroySpeed(level() ,blockpos) < 10 && blockstate.getDestroySpeed(level() ,blockpos) > 0) {
                        flag =  this.level().destroyBlock(blockpos, true, this) || flag;
                        breakCounter = 0;
                    }
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (getBiomass() > 1){
            int age = 1;
            if (this.getBiomass() > (SConfig.SERVER.reconstructor_biomass.get()/4) && this.getBiomass() < (SConfig.SERVER.reconstructor_biomass.get()/2)){
                age = 2;
            }else if (this.getBiomass() > (SConfig.SERVER.reconstructor_biomass.get()/2)){
                age = 3;
            }
            if (age > 1){
                double maxHealth = SConfig.SERVER.mound_hp.get() * age * SConfig.SERVER.global_health.get();
                AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
                if (health != null) {
                    health.setBaseValue(maxHealth);
                }
                SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(this, (float) maxHealth);
                AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
                if (armor != null) {
                    armor.setBaseValue(SConfig.SERVER.mound_armor.get() * age * SConfig.SERVER.global_armor.get());
                }
            }
        }
    }

    private void summon(Entity entity, boolean value) {
        summon(entity, value, true);
    }

    private void summon(Entity entity, boolean value, boolean discard) {
        if (Math.random() <= 0.3f) {
            this.entityData.set(STATE, this.random.nextInt(TERRAIN.values().length));
        }
        List<? extends String> variantList = this.getVariant().getList();
        if (variantList.isEmpty()) return;

        ResourceLocation entityId = new ResourceLocation(variantList.get(this.random.nextInt(variantList.size())));
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        if (entityType == null) return;

        Mob spawnedEntity = (Mob) entityType.create(level());
        if (spawnedEntity == null) return;
        if (!hasLegalPosition()) {
            this.setPos(this.lastLegalPosition());
        }
        Vec3 origin = new Vec3(this.getLocation().getX(),this.getLocation().getY(),this.getLocation().getZ());
        Vec3 current = this.position();
        double maxDistance = 200.0;
        if (!origin.equals(Vec3.ZERO) && origin.distanceTo(current) > maxDistance) {
            Vec3 newPos = Utilities.generatePositionAway(origin, 100);
            spawnedEntity.teleportRelative(newPos.x, newPos.y, newPos.z);
        }else {
            spawnedEntity.setPos(entity.getX(), entity.getY(), entity.getZ());
        }
        if (spawnedEntity instanceof Calamity calamity) {
            calamity.setSearchArea(this.getLocation());

            for (String attrId : attributeIDs) {
                ResourceLocation attrLocation = new ResourceLocation(attrId);
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attrLocation);
                if (attribute != null) {
                    AttributeInstance instance = calamity.getAttribute(attribute);
                    if (attribute == Attributes.MAX_HEALTH) {
                        double maxHealth = instance != null ? instance.getValue() + 1.0D : SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(calamity) + 1.0D;
                        if (instance != null) {
                            instance.setBaseValue(maxHealth);
                        }
                        SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(calamity, (float) maxHealth);
                    } else if (instance != null){
                        double e = instance.getValue();
                        instance.setBaseValue(e+1);
                    }
                }
            }
            if (this.pendingAdaptationCounts > 0) {
                calamity.setDamageAdaptationCount(this.pendingAdaptationCounts);
            }

            if (value) {
                calamity.setSecondsOnFire(3);
                float maxHealth = SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(calamity);
                SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(calamity, maxHealth);
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            double x0 = this.getX() - (random.nextFloat() - 0.1) * 0.1D;
            double y0 = this.getY() + (random.nextFloat() - 0.25) * 0.15D * 5;
            double z0 = this.getZ() + (random.nextFloat() - 0.1) * 0.1D;
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, 0, 0, 0, 1);
            spawnedEntity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
        }
        level().addFreshEntity(spawnedEntity);
        if (discard) {
            this.discard();
        }
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
        this.syncAtFinalizeSpawn();
        return super.finalizeSpawn(serverLevelAccessor, p_33283_, p_33284_, p_33285_, p_33286_);
    }

    @Override
    public void tickDeath() {
        if (this.getHealth() > 0.0f) {
            return;
        }
        if (this.getBiomass() > (SConfig.SERVER.reconstructor_biomass.get() / 2)) {
            summon(this, true, true);
        }
        super.tickDeath();
    }

    @Override
    public void die(DamageSource p_21014_) {
        if (this.getHealth() > 0.0f) {
            return;
        }
        if (this.getBiomass() > (SConfig.SERVER.reconstructor_biomass.get()/2)){
            summon(this,true);
        }
        super.die(p_21014_);
    }

    public int getDamageAdaptationCount() {
        return pendingAdaptationCounts;
    }

    public void setAdaptationCount(int adaptationCount) {
        pendingAdaptationCounts = adaptationCount;
    }

    @Override
    public LivingEntity self() {
        return this;
    }

    @Override
    public boolean isSpecialDefasd() {
        return this.entityData.get(SPECIAL_DEAD);
    }

    @Override
    public boolean hasLegalPosition() {
        if (Double.isNaN(this.position.x) || Double.isNaN(this.position.y) || Double.isNaN(this.position.z)) {
            return false;
        }
        return this.lastLegalPosition().distanceTo(this.position) <= 5000.0F;
    }

    @Override
    public Vec3 lastLegalPosition() {
        return lastLegalPosition;
    }

    @Override
    public void setLegalPosition(Vec3 position) {
        this.lastLegalPosition = position;
    }

    @Override
    public void specialDie(DamageSource source) {
        this.entityData.set(SPECIAL_DEAD, true);
        if (this.getBiomass() > (SConfig.SERVER.reconstructor_biomass.get() / 2)) {
            this.summon(this, true, false);
        }
    }

    @Override
    public int getEmerge_tick() {
        return 60;
    }

    public TERRAIN getVariant() {
        return TERRAIN.byId(this.entityData.get(STATE) & 255);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new AssimilationMenu(i,inventory);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer  && serverPlayer.getAbilities().instabuild && !level().isClientSide){
            NetworkHooks.openScreen(serverPlayer, this, this.blockPosition());
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public enum TERRAIN{
        GROUND_LEVEL(0,SConfig.SERVER.reconstructor_terrain.get()),
        WATER_LEVEL(1,SConfig.SERVER.reconstructor_water.get()),
        AIR_LEVEL(2,SConfig.SERVER.reconstructor_air.get()),
        UNDERGROUND(3,SConfig.SERVER.reconstructor_underground.get());
        private final int value;
        private final List<? extends String> list;
        TERRAIN(int v, List<? extends String> l){
            this.value = v;
            this.list = l;
        }
        public int getValue(){
            return value;
        }
        public List<? extends String> getList(){
            return list;
        }
        private static final TERRAIN[] BY_ID = Arrays.stream(values()).sorted(Comparator.
                comparingInt(TERRAIN::getValue)).toArray(TERRAIN[]::new);
        public static TERRAIN byId(int id) {
            return BY_ID[id % BY_ID.length];
        }
    }
}
