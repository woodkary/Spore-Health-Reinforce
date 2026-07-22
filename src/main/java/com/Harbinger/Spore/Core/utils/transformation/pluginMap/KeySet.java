package com.Harbinger.Spore.Core.utils.transformation.pluginMap;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

/**
 * @author karywoodOyo
 */
public final class KeySet extends AbstractSet<String> {
    private final SporePluginHashMap map;
    public KeySet(SporePluginHashMap map) {
        this.map=map;
    }

    @Override
    public Iterator<String> iterator() {
        final LinkedHashMap<String, ILaunchPluginService> snap;
        this.map.rw.readLock().lock();
        try {
            snap = this.map.snapshotUnionView();
        } finally {
            this.map.rw.readLock().unlock();
        }
        return Collections.unmodifiableSet(snap.keySet()).iterator();
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
