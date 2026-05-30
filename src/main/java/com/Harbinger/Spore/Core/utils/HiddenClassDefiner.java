package com.Harbinger.Spore.Core.utils;

import org.objectweb.asm.ClassReader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HiddenClassDefiner {
    private static volatile MethodHandle makeHiddenClassDefiner;
    private static volatile Class<?> classDefinerClass;
    private static volatile MethodHandle defineClassAsLookup;
    private static volatile MethodHandle defineClass0;
    private static final Object DEFINE_CLASS0_LOCK = new Object();
    private static final Object LOOKUP_CLASS_DEFINER_LOCK = new Object();
    private static final int FLAG_NESTMATE = 1;
    private static final int FLAG_HIDDEN_CLASS = 2;
    private static final int FLAG_STRONG = 4;
    private static final int FLAG_ACCESS_VM_ANNOTATIONS = 8;
    private static final Map<MethodHandles.Lookup.ClassOption,Integer> optionAndFlags=Map.of(
            MethodHandles.Lookup.ClassOption.NESTMATE,FLAG_NESTMATE,
            MethodHandles.Lookup.ClassOption.STRONG,FLAG_STRONG
    );
    private static final ConcurrentHashMap<HiddenDefineKey,CachedClassRef> lookupToHidden = new ConcurrentHashMap<>();
    private static final ReferenceQueue<Class<?>> hiddenClassRefQueue = new ReferenceQueue<>();
    private static final ThreadLocal<HashSet<HiddenDefineKey>> inProgress = ThreadLocal.withInitial(HashSet::new);
    public static Class<?> defaneClazz0(Class<?> lookupClass,byte[] bytes,boolean initialize,MethodHandles.Lookup.ClassOption... options) throws Throwable{
        if (lookupClass == null || bytes == null || bytes.length == 0) {
            return null;
        }
        cleanupStaleHiddenClassCacheEntries();
        Set<MethodHandles.Lookup.ClassOption> optionSet = toOptionSet(options);
        ClassLoader loader = lookupClass.getClassLoader();
        ProtectionDomain pd = (loader != null) ? lookupClass.getProtectionDomain() : null;
        int flags=getFlags(optionSet, loader);
        MethodHandle handle = ensureDefineClass0();

        HiddenDefineKey key = HiddenDefineKey.of(lookupClass, bytes, initialize, flags);
        Class<?> cached = getCachedHiddenClass(key);
        if (cached != null) {
            return cached;
        }
        HashSet<HiddenDefineKey> active = inProgress.get();
        if (active.contains(key)) {
            return doDefineClass0(handle, loader, lookupClass, bytes, pd, initialize, flags);
        }

        synchronized (DEFINE_CLASS0_LOCK) {
            cached = getCachedHiddenClass(key);
            if (cached != null) {
                return cached;
            }
            active.add(key);
            try {
                Class<?> defined = doDefineClass0(handle, loader, lookupClass, bytes, pd, initialize, flags);
                if (defined != null) {
                    lookupToHidden.put(key, new CachedClassRef(key, defined, hiddenClassRefQueue));
                }
                return defined;
            } finally {
                active.remove(key);
                if (active.isEmpty()) {
                    inProgress.remove();
                }
            }
        }
    }
    private static MethodHandle ensureDefineClass0() throws NoSuchMethodException, IllegalAccessException {
        MethodHandle handle = defineClass0;
        if (handle != null) {
            return handle;
        }
        synchronized (DEFINE_CLASS0_LOCK) {
            handle = defineClass0;
            if (handle == null) {
                handle = ClassUtil.getLookup().findStatic(
                        ClassLoader.class,
                        "defineClass0",
                        MethodType.methodType(Class.class,
                                ClassLoader.class,
                                Class.class,
                                String.class,
                                byte[].class, int.class, int.class,
                                ProtectionDomain.class,
                                boolean.class,
                                int.class,
                                Object.class)
                );
                defineClass0 = handle;
            }
            return handle;
        }
    }
    private static Class<?> getCachedHiddenClass(HiddenDefineKey key) {
        CachedClassRef ref = lookupToHidden.get(key);
        if (ref == null) {
            return null;
        }
        Class<?> cached = ref.get();
        if (cached == null) {
            lookupToHidden.remove(key, ref);
        }
        return cached;
    }
    private static void cleanupStaleHiddenClassCacheEntries() {
        CachedClassRef stale;
        while ((stale = (CachedClassRef) hiddenClassRefQueue.poll()) != null) {
            lookupToHidden.remove(stale.key, stale);
        }
    }
    private static Class<?> doDefineClass0(MethodHandle handle,
                                           ClassLoader loader,
                                           Class<?> lookupClass,
                                           byte[] bytes,
                                           ProtectionDomain pd,
                                           boolean initialize,
                                           int flags) throws Throwable {
        String targetName = extractBinaryClassName(bytes);
        if (targetName == null || targetName.isBlank()) {
            targetName = lookupClass.getName();
        }
        return (Class<?>) handle.invoke(
                loader,
                lookupClass,
                targetName,
                bytes,0,bytes.length,
                pd,
                initialize,
                flags,
                null
        );
    }
    private static String extractBinaryClassName(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            ClassReader reader = new ClassReader(bytes);
            String internal = reader.getClassName();
            if (internal == null || internal.isBlank()) {
                return null;
            }
            return internal.replace('/', '.');
        } catch (Throwable t) {
            return null;
        }
    }
    private static int getFlags(Set<MethodHandles.Lookup.ClassOption> options, ClassLoader loader){
        int flags = FLAG_HIDDEN_CLASS | optionsToFlag(options);
        // jdk.internal.vm.annotations are permitted for classes
        // defined to boot loader and platform loader
        if (loader == null || loader == ClassLoader.getPlatformClassLoader()) {
            flags |= FLAG_ACCESS_VM_ANNOTATIONS;
        }
        return flags;
    }
    private static Set<MethodHandles.Lookup.ClassOption> toOptionSet(MethodHandles.Lookup.ClassOption... options) {
        if (options == null || options.length == 0) {
            return Set.of();
        }
        HashSet<MethodHandles.Lookup.ClassOption> result = new HashSet<>(options.length);
        for (MethodHandles.Lookup.ClassOption option : options) {
            if (option != null) {
                result.add(option);
            }
        }
        if (result.isEmpty()) {
            return Set.of();
        }
        return result;
    }
    private static int optionsToFlag(Set<MethodHandles.Lookup.ClassOption> options) {
        if (options == null || options.isEmpty()) {
            return 0;
        }
        int flags = 0;
        for (MethodHandles.Lookup.ClassOption cp : options) {
            flags |= optionAndFlags.getOrDefault(cp,0);
        }
        return flags;
    }
    public static MethodHandles.Lookup defaneHeddenClazz(MethodHandles.Lookup hostLookup,byte[] bytes, boolean initialize, MethodHandles.Lookup.ClassOption... options) throws IllegalAccessException {
        if (hostLookup == null || bytes == null || bytes.length == 0) {
            return hostLookup;
        }
        if (classDefinerClass == null || makeHiddenClassDefiner == null || defineClassAsLookup == null) {
            synchronized (LOOKUP_CLASS_DEFINER_LOCK) {
                if (classDefinerClass == null) {
                    try {
                        classDefinerClass = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassDefiner");
                    } catch (ClassNotFoundException e) {
                        LogUtil.error("cannot find class java.lang.invoke.MethodHandles$Lookup$ClassDefiner");
                        return fallbackDafine(hostLookup, bytes, initialize, options);
                    }
                }
                if (makeHiddenClassDefiner == null) {
                    try {
                        makeHiddenClassDefiner = ClassUtil.getLookup().findVirtual(
                                MethodHandles.Lookup.class,
                                "makeHiddenClassDefiner",
                                MethodType.methodType(classDefinerClass,byte[].class,Set.class,boolean.class));
                    } catch (NoSuchMethodException e) {
                        LogUtil.error("cannot find makeHiddenClassDefiner");
                        return fallbackDafine(hostLookup, bytes, initialize, options);
                    }
                }
                if (defineClassAsLookup ==null){
                    try {
                        defineClassAsLookup = ClassUtil.getLookup().findVirtual(
                                classDefinerClass,
                                "defineClassAsLookup",
                                MethodType.methodType(MethodHandles.Lookup.class, boolean.class)
                        );
                    }catch (NoSuchMethodException e){
                        LogUtil.error("cannot find defineClassAsLookup");
                        return fallbackDafine(hostLookup, bytes, initialize, options);
                    }
                }
            }
        }
        Set<MethodHandles.Lookup.ClassOption> optionSet = toOptionSet(options);
        MethodHandle MHCD=makeHiddenClassDefiner.bindTo(hostLookup);
        Object classDefiner=null;
        try {
            classDefiner = MHCD.invoke(bytes.clone(), optionSet, false);
        }catch (Throwable e){
            LogUtil.error("failed to invoke makeHiddenClassDefiner");
            return fallbackDafine(hostLookup, bytes, initialize, options);
        }
        if(classDefiner!=null) {
            MethodHandle DCAL = defineClassAsLookup.bindTo(classDefiner);
            try {
                return (MethodHandles.Lookup) DCAL.invoke(initialize);
            } catch (Throwable e) {
                LogUtil.error("failed to invoke defineClassAsLookup");
            }
        }
        return fallbackDafine(hostLookup, bytes, initialize, options);
    }
    private static final class HiddenDefineKey {
        private final Class<?> lookupClass;
        private final int bytecodeHash;
        private final int bytecodeLength;
        private final boolean initialize;
        private final int flags;
        private final int hash;

        private HiddenDefineKey(Class<?> lookupClass, int bytecodeHash, int bytecodeLength, boolean initialize, int flags) {
            this.lookupClass = lookupClass;
            this.bytecodeHash = bytecodeHash;
            this.bytecodeLength = bytecodeLength;
            this.initialize = initialize;
            this.flags = flags;
            this.hash = computeHash();
        }

        static HiddenDefineKey of(Class<?> lookupClass, byte[] bytes, boolean initialize, int flags) {
            return new HiddenDefineKey(
                    lookupClass,
                    Arrays.hashCode(bytes),
                    bytes.length,
                    initialize,
                    flags
            );
        }

        private int computeHash() {
            int result = System.identityHashCode(lookupClass);
            result = 31 * result + bytecodeHash;
            result = 31 * result + bytecodeLength;
            result = 31 * result + (initialize ? 1 : 0);
            result = 31 * result + flags;
            return result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof HiddenDefineKey other)) {
                return false;
            }
            return lookupClass == other.lookupClass
                    && bytecodeHash == other.bytecodeHash
                    && bytecodeLength == other.bytecodeLength
                    && initialize == other.initialize
                    && flags == other.flags;
        }
    }
    private static final class CachedClassRef extends WeakReference<Class<?>> {
        private final HiddenDefineKey key;

        private CachedClassRef(HiddenDefineKey key, Class<?> referent, ReferenceQueue<Class<?>> q) {
            super(referent, q);
            this.key = key;
        }
    }
    private static MethodHandles.Lookup fallbackDafine(MethodHandles.Lookup hostLookup,byte[] bytes, boolean initialize, MethodHandles.Lookup.ClassOption... options) throws IllegalAccessException {
        return hostLookup.defineHiddenClass(bytes, initialize, options);
    }
    public static void tryLoadSelfClass(){
        try{
            new HiddenClassDefiner();
            return;
        }catch (Throwable e){}
        try{
            Class.forName("com.Harbinger.Spore.Core.utils.HiddenClassDefiner");
        }catch (Throwable e){}
    }
}
