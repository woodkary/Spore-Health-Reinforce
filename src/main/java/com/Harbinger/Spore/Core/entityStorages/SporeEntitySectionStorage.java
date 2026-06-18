package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.Visibility;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public final class SporeEntitySectionStorage<T extends EntityAccess> extends EntitySectionStorage<T> implements BiFunction<Long,EntitySection<T>,EntitySection<T>> {
    public static final Class<? extends EntitySectionStorage<? extends EntityAccess>> entitySectionStorageClass = (Class<? extends EntitySectionStorage<? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntitySectionStorage.class,
            Class.class,
            Long2ObjectFunction.class
    );
    public SporeEntitySectionStorage(Class<T> p_156855_, Long2ObjectFunction<Visibility> p_156856_) {
        super(p_156855_, p_156856_);
    }

    @Override
    public EntitySection<T> getOrCreateSection(long p_156894_) {
        return this.sections.compute(p_156894_, this);
    }

    @Override
    public @Nullable EntitySection<T> getSection(long p_156896_) {
        return super.getSection(p_156896_);
    }

    @Override
    public EntitySection<T> createSection(long p_156902_) {
        return new SporeEntitySection<>(super.createSection(p_156902_));
    }

    @Override
    public EntitySection<T> apply(Long sectionKey, EntitySection<T> mapValue) {
        if(mapValue==null){
            return createSection(sectionKey);
        }
        if(mapValue.getClass()!=SporeEntitySection.class){
            return new SporeEntitySection<>(mapValue);
        }
        return mapValue;
    }
}
