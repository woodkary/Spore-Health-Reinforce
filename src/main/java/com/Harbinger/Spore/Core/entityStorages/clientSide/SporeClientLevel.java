package com.Harbinger.Spore.Core.entityStorages.clientSide;

import com.Harbinger.Spore.Core.entityStorages.SporeEntityGetter;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import com.Harbinger.Spore.sEvents.SporeEventBus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SporeClientLevel extends ClientLevel {
    public static final Class<? extends ClientLevel> clientLevelClass = (Class<? extends ClientLevel>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeClientLevel.class,
            ClientPacketListener.class,
            ClientLevelData.class,
            ResourceKey.class,
            Holder.class,
            int.class,
            int.class,
            Supplier.class,
            LevelRenderer.class,
            boolean.class,
            long.class
    );
    public SporeClientLevel(ClientPacketListener p_205505_, ClientLevelData p_205506_, ResourceKey<Level> p_205507_, Holder<DimensionType> p_205508_, int p_205509_, int p_205510_, Supplier<ProfilerFiller> p_205511_, LevelRenderer p_205512_, boolean p_205513_, long p_205514_) {
        super(p_205505_, p_205506_, p_205507_, p_205508_, p_205509_, p_205510_, p_205511_, p_205512_, p_205513_, p_205514_);
    }
    @Nullable
    @Override
    public Entity getEntity(int p_104609_) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(p_104609_)){
            return null;
        }
        return this.getEntities().get(p_104609_);
    }
    @Override
    public Iterable<Entity> entitiesForRendering() {
        return this.getEntities().getAll();
    }
    @Override
    public void tickNonPassenger(Entity p_104640_) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(p_104640_)) {
            return;
        }
        super.tickNonPassenger(p_104640_);
    }
    @Override
    public void putNonPlayerEntity(int id, Entity entity) {
        this.addEntity(id, entity);
    }
    public @NotNull LevelEntityGetter<Entity> getEntities() {
        if(this.entityStorage.entityGetter.getClass()!=SporeEntityGetter.entityGetterClass){
            this.entityStorage.entityGetter= SporeEntityGetter.newInstance(this.entityStorage.entityGetter,this.entityStorage.entityStorage,this.entityStorage.sectionStorage);
        }
        return this.entityStorage.getEntityGetter();
    }
    @Override
    public void addEntity(int id, Entity entity) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(id) ||SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) {
            return;
        }
        super.addEntity(id, entity);
    }
    @Override
    public void removeEntity(int p_171643_, Entity.RemovalReason p_171644_) {
        Entity entity = this.getEntities().get(p_171643_);
        if (entity != null) {
            if(entity instanceof IDieWithDiscardEntity){
                SimpleRemoveUtil.INSTANCE.setRemoved(entity, p_171644_);
            }else{
                entity.setRemoved(p_171644_);
            }
            entity.onClientRemoval();
        }

    }
    @Override
    public void tickEntities() {
        ProfilerFiller profilerfiller = this.getProfiler();
        profilerfiller.push("entities");
        this.tickingEntities.forEach(new ClientTickingEntityConsumer(this));
        profilerfiller.pop();
        this.tickBlockEntities();
    }
    @Override
    public void tick(BooleanSupplier p_104727_) {
        SporeEventBus.tick();
        super.tick(p_104727_);
    }

    private record ClientTickingEntityConsumer(ClientLevel level) implements Consumer<Entity> {
        @Override
            public void accept(Entity entity) {
                if (!entity.isRemoved() && !entity.isPassenger() && !SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) {
                    level.guardEntityTick(level::tickNonPassenger, entity);
                }
            }
        }
}
