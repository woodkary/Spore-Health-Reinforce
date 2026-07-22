package com.Harbinger.Spore.Core.utils.transformation.pluginMap;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author karywoodOyo
 */
public final class EntrySet extends AbstractSet<Map.Entry<String, ILaunchPluginService>> {
    final SporePluginHashMap map;
    public EntrySet(SporePluginHashMap map) {
        this.map=map;
    }

    @Override
    public Iterator<Map.Entry<String, ILaunchPluginService>> iterator() {
        final LinkedHashMap<String, ILaunchPluginService> snap;
        this.map.rw.readLock().lock();
        try {
            snap = this.map.snapshotUnionView();
        } finally {
            this.map.rw.readLock().unlock();
        }
        return Collections.unmodifiableSet(snap.entrySet()).iterator();
    }
    @Override
    public boolean removeIf(Predicate<? super Map.Entry<String, ILaunchPluginService>> filter) {
        Objects.requireNonNull(filter);

        this.map.rw.writeLock().lock();
        try {
            boolean modified = false;
            Iterator<Map.Entry<String, ILaunchPluginService>> it =
                    this.map.superEntrySet.get().iterator();

            while (it.hasNext()) {
                Map.Entry<String, ILaunchPluginService> e = it.next();
                String key = e.getKey();
                if (this.map.protectedMap.containsKey(key)) continue;
                if(this.map.minecraftMap.containsKey(key)) continue;
                if (filter.test(e)) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        } finally {
            this.map.rw.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        this.map.rw.readLock().lock();
        try {
            return this.map.snapshotUnionView().size();
        } finally {
            this.map.rw.readLock().unlock();
        }
    }
}
