package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntityAccess;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author karywoodOyo
 */
public final class ProtectedClassInstanceMultiMap<T extends EntityAccess> extends ClassInstanceMultiMap<T> {
    public ProtectedClassInstanceMultiMap(Class<T> clazz) {
        super(clazz);
    }
    public boolean add(T p_13536_) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(p_13536_)) {
            return false;
        }
        boolean $$1 = false;

        for (Map.Entry<Class<?>, List<T>> classListEntry : this.byClass.entrySet()) {
            if ((classListEntry.getKey()).isInstance(p_13536_)) {
                $$1 |= (classListEntry.getValue()).add(p_13536_);
            }
        }

        return $$1;
    }

    public boolean remove(Object p_13543_) {
        boolean $$1 = false;

        for (Map.Entry<Class<?>, List<T>> classListEntry : this.byClass.entrySet()) {
            if ((classListEntry.getKey()).isInstance(p_13543_)) {
                List<T> $$3 = classListEntry.getValue();
                $$1 |= $$3.remove(p_13543_);
            }
        }

        return $$1;
    }

    public boolean contains(Object p_13540_) {
        return this.find(p_13540_.getClass()).contains(p_13540_);
    }

    public <T> Collection<T> find(Class<T> p_13534_) {
        if (!this.baseClass.isAssignableFrom(p_13534_)) {
            throw new IllegalArgumentException("Don't know how to search for " + p_13534_);
        } else {
            List<? extends T> $$1 = (List)this.byClass.computeIfAbsent(p_13534_, (p_13538_) -> {
                Stream var10000 = this.allInstances.stream();
                Objects.requireNonNull(p_13538_);
                return (List)var10000.filter(p_13538_::isInstance).collect(Collectors.toList());
            });
            return Collections.unmodifiableCollection($$1);
        }
    }

    public Iterator<T> iterator() {
        return (this.allInstances.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.allInstances.iterator()));
    }

    public List<T> getAllInstances() {
        return ImmutableList.copyOf(this.allInstances);
    }

    public int size() {
        return this.allInstances.size();
    }
}
