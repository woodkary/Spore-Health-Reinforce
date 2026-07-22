package com.Harbinger.Spore.Core.utils.transformation.pluginMap;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class SporePluginHashMap extends HashMap<String, ILaunchPluginService> {
    private final Predicate<Object> exclude;
    final Map<String, ILaunchPluginService> protectedMap;
    final Supplier<Set<Entry<String, ILaunchPluginService>>> superEntrySet;
    final KeySet keySet=new KeySet(this);
    final EntrySet entrySet=new EntrySet(this);
    final ValuesCollection valuesCollection=new ValuesCollection(this);
    static final Map<String, ILaunchPluginService> minecraftMap=new HashMap<>();
    static final Set<String> minecraftKeys=Set.of(
            "mixin","eventbus","slf4jfixer","object_holder_definalize",
            "runtime_enum_extender","capability_token_subclass",
            "accesstransformer","runtimedistcleaner"
    );
    final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();

    public SporePluginHashMap(Predicate<Object> exclude,
                              Map<String, ILaunchPluginService> protectedMap) {
        this.exclude = Objects.requireNonNull(exclude, "exclude");
        this.protectedMap = Objects.requireNonNull(protectedMap, "protectedMap");
        this.superEntrySet= super::entrySet;
    }

    public SporePluginHashMap(int initialCapacity,
                              Predicate<Object> exclude,
                              Map<String, ILaunchPluginService> protectedMap) {
        super(initialCapacity);
        this.exclude = Objects.requireNonNull(exclude, "exclude");
        this.protectedMap = Objects.requireNonNull(protectedMap, "protectedMap");
        this.superEntrySet= super::entrySet;
    }

    public SporePluginHashMap(int initialCapacity, float loadFactor,
                              Predicate<Object> exclude,
                              Map<String, ILaunchPluginService> protectedMap) {
        super(initialCapacity, loadFactor);
        this.exclude = Objects.requireNonNull(exclude, "exclude");
        this.protectedMap = Objects.requireNonNull(protectedMap, "protectedMap");
        this.superEntrySet= super::entrySet;
    }

    public SporePluginHashMap(Map<? extends String, ? extends ILaunchPluginService> m,
                              Predicate<Object> exclude,
                              Map<String, ILaunchPluginService> protectedMap) {
        super(m);
        this.exclude = Objects.requireNonNull(exclude, "exclude");
        this.protectedMap = Objects.requireNonNull(protectedMap, "protectedMap");
        this.superEntrySet= super::entrySet;
        super.entrySet().removeIf(this::isExcluded);
        m.forEach((s,e)->{
            if(minecraftKeys.contains(s)) {
                minecraftMap.put(s, e);
            }
        });
    }
    public void tryPutMinecraftPlugins(Map<String, ILaunchPluginService> plugins) {
        plugins.forEach((k,v)->{
            if(minecraftKeys.contains(k)) {
                minecraftMap.put(k, v);
            }
        });
    }

    /* ================= 过滤判断（只用于 this） ================= */

    private boolean isExcluded(Entry<String, ILaunchPluginService> e) {
        return exclude.test(e.getKey()) || exclude.test(e.getValue());
    }
    private boolean isExcludedKey(Object key) {
        return exclude.test(key);
    }

    /* ================= 快照视图：protected 优先 + 去重 + 过滤 ================= */

    LinkedHashMap<String, ILaunchPluginService> snapshotUnionView() {
        // 注意：此函数由调用者在 readLock 下调用
        LinkedHashMap<String, ILaunchPluginService> snap = new LinkedHashMap<>();

        // 1) protectedMap 全量放入（强制可见，不过滤）
        snap.putAll(protectedMap);
        snap.putAll(minecraftMap);

        // 2) this 的条目：不覆盖 protected，同 key 跳过；并且要过滤
        for (Entry<String, ILaunchPluginService> e : super.entrySet()) {
            if (snap.containsKey(e.getKey())) continue;
            if (isExcluded(e)) continue;
            snap.put(e.getKey(), e.getValue());
        }

        return snap;
    }

    /* ================= 遍历入口：全部走快照 ================= */

    @Override
    public void forEach(BiConsumer<? super String, ? super ILaunchPluginService> action) {
        Objects.requireNonNull(action);
        final LinkedHashMap<String, ILaunchPluginService> snap;

        rw.readLock().lock();
        try {
            snap = snapshotUnionView();
        } finally {
            rw.readLock().unlock();
        }

        snap.forEach(action);
    }


    @Override
    public Set<Entry<String, ILaunchPluginService>> entrySet() {
        // 返回一个“快照视图”的 Set：每次 iterator()/size() 都基于当时快照
        return entrySet;
    }

    @Override
    public Set<String> keySet() {
        return keySet;
    }

    @Override
    public Collection<ILaunchPluginService> values() {
        return valuesCollection;
    }

    /* ================= 读方法：加读锁 ================= */

    @Override
    public ILaunchPluginService get(Object key) {
        if (isExcludedKey(key)) return null;

        rw.readLock().lock();
        try {
            if (key instanceof String s && protectedMap.containsKey(s)) {
                return protectedMap.get(s);
            }
            ILaunchPluginService value=super.get(key);
            return exclude.test(value) ? null : value;
        } finally {
            rw.readLock().unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        if (isExcludedKey(key)) return false;

        rw.readLock().lock();
        try {
            if (key instanceof String s && protectedMap.containsKey(s)) return true;
            ILaunchPluginService value=super.get(key);
            if(value==null) return false;
            return !exclude.test(value);
        } finally {
            rw.readLock().unlock();
        }
    }
    private boolean isExcludedEntry(String key, ILaunchPluginService value) {
        return exclude.test(key) || exclude.test(value);
    }
    @Override
    public ILaunchPluginService put(String key, ILaunchPluginService value) {
        if (isExcludedEntry(key, value)) {
            return value;
        }

        rw.writeLock().lock();
        try {
            if(minecraftKeys.contains(key)) {
                minecraftMap.put(key, value);
            }
            return super.put(key, value);
        } finally {
            rw.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        rw.readLock().lock();
        try {
            // size 语义按“遍历可见”来：union - exclude
            return snapshotUnionView().size();
        } finally {
            rw.readLock().unlock();
        }
    }

    /* ================= 写方法：加写锁（只写 this，不写 protectedMap） ================= */


    @Override
    public ILaunchPluginService remove(Object key) {
        if (!(key instanceof String s)) return null;
        if (protectedMap.containsKey(s)) return protectedMap.get(key);
        if(minecraftKeys.contains(s)) return  minecraftMap.get(s);

        rw.writeLock().lock();
        try {
            return super.remove(key);
        } finally {
            rw.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        rw.writeLock().lock();
        try {
            for (Entry<String, ILaunchPluginService> entry : super.entrySet()) {
                String key = entry.getKey();
                if(!protectedMap.containsKey(key)&&!minecraftKeys.contains(key)) {
                    super.remove(key);
                }
            }
        } finally {
            rw.writeLock().unlock();
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends ILaunchPluginService> m) {
        rw.writeLock().lock();
        try {
            for (Entry<? extends String, ? extends ILaunchPluginService> e : m.entrySet()) {
                String key = e.getKey();
                ILaunchPluginService value = e.getValue();
                if (isExcludedEntry(key, value)) continue;
                super.put(key, value);
                if(minecraftKeys.contains(key)) {
                    minecraftMap.put(key, value);
                }
            }
        } finally {
            rw.writeLock().unlock();
        }
    }
}

