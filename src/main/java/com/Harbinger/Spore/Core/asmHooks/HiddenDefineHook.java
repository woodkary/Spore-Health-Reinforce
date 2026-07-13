package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.IJVNTIPointer;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.agents.JVMTIPointerUtil;
import com.Harbinger.Spore.Core.agents.transformers.SelfTransformer;
import com.Harbinger.Spore.Core.agents.transformers.SporeHiddenDefineHookTransformer;
import com.Harbinger.Spore.Core.agents.transformers.SporeLivingEntityEffectApplicationTransformer;
import com.Harbinger.Spore.Core.agents.transformers.SporeLivingEntityHealthTransformer;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import org.objectweb.asm.ClassReader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.WeakHashMap;

public final class HiddenDefineHook implements SelfTransformer {
    private static final MethodType DEFINE_CLASS0_TYPE = MethodType.methodType(
            Class.class,
            ClassLoader.class,
            Class.class,
            String.class,
            byte[].class, int.class, int.class,
            ProtectionDomain.class,
            boolean.class,
            int.class,
            Object.class
    );
    private static volatile MethodHandle hookDefineClass0;
    private static volatile MethodHandle rawDefineClass0;
    private static volatile MethodHandle findLoadedClass;
    private static volatile MethodHandle findBootstrapClassOrNull;
    private static final ThreadLocal<Boolean> TRANSFORMING_CLASS_BYTES = new ThreadLocal<>();
    public static final SelfTransformer INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            SelfTransformer.class,
            HiddenDefineHook.class
    );
    private final SelfTransformer[] transformers={
            SporeLivingEntityHealthTransformer.newSelfTransformer(),
            SporeLivingEntityEffectApplicationTransformer.newSelfTransformer(),
    };
    @Override
    public byte[] transformClassByte(ClassLoader loader, String className, byte[] classfileBuffer) {
        byte[] res=classfileBuffer;
        boolean modified=false;
        for (SelfTransformer transformer : transformers) {
            byte[] temp=transformer.transformClassByte(loader, className, res);
            if(temp!=null&&temp.length>0) {
                res=temp;
                modified=true;
            }
        }
        return modified ? res : null;
    }
    private static boolean jvmtiInstalled=false;
    private static boolean instInstalled=false;
    private static final Map<Class<?>,String[]> possibleHiddenToOriginalName =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Class<?>,Class<?>> actualHiddenToOriginal =
            Collections.synchronizedMap(new WeakHashMap<>());
    public static void inspectHiddenDefine() {
        //同时安装Instrumentation和jvmti监听InstrumentationImpl加载，但不重转换
        IJVNTIPointer jvmtiUtil= JVMTIPointerUtil.newInstance();
        ClassFileTransformer hiddenTransformer= SporeHiddenDefineHookTransformer.newInstance();
        SelfTransformer hiddenTransformer2=hiddenTransformer instanceof SelfTransformer selfTransformer? selfTransformer : SporeHiddenDefineHookTransformer.newSelfTransformer();
        if(jvmtiUtil!=null && !jvmtiInstalled) {
            jvmtiUtil.addTransformer(hiddenTransformer2);
            jvmtiInstalled=jvmtiUtil.isTransformerHookInstalled();
        }
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        boolean instrumentationReady = instrumentation != null;
        if(instrumentationReady&&!instInstalled) {
            //只安装Transformer，不进行retransform
            instrumentation.addTransformer(hiddenTransformer);
            instInstalled = true;
        }
    }
    //直接调用lookup.defineHiddenClass(...)的改调用钩子，需要调用者lookup，和参数byte[]
    public static byte[] lookupDefineHiddenClassHook(MethodHandles.Lookup hostLookup,byte[] original){
        if (original == null || original.length == 0) {
            return original;
        }
        Class<?> lookupClass = hostLookup == null ? null : hostLookup.lookupClass();
        String className = resolveClassName(lookupClass, original);
        ClassLoader classLoader = lookupClass == null ? null : lookupClass.getClassLoader();
        byte[] transformed = transformClassBytes(classLoader, className, original);
        return transformed == null || transformed.length == 0 ? original : transformed;
    }
    public static MethodHandles.Lookup recordHiddenLookup(MethodHandles.Lookup hiddenLookup){
        if (hiddenLookup != null) {
            recordHidden(hiddenLookup.lookupClass());
        }
        return hiddenLookup;
    }
    //lookup.findStatic的结果重定向到这里，接收lookup.findStatic的结果
    public static MethodHandle lookupFindDefineClass0StaticHook(MethodHandle original){
        if(!isDefineClass0(original)) {
            return original;
        }
        try {
            return ensureHookDefineClass0();
        }catch (Throwable ignored){}
        return original;
    }
    private static boolean isDefineClass0(MethodHandle handle){
        if (handle == null || !DEFINE_CLASS0_TYPE.equals(handle.type())) {
            return false;
        }
        try {
            MethodHandleInfo info = ClassUtil.getLookup().revealDirect(handle);
            return info != null
                    && info.getDeclaringClass() == ClassLoader.class
                    && "defineClass0".equals(info.getName())
                    && info.getReferenceKind() == MethodHandleInfo.REF_invokeStatic;
        } catch (Throwable ignored) {
            return false;
        }
    }
    //改目标的methodHandle为这个
    public static Class<?> myDefineClass0(ClassLoader loader,
                                 Class<?> lookup,
                                 String name,
                                 byte[] b, int off, int len,
                                 ProtectionDomain pd,
                                 boolean initialize,
                                 int flags,
                                 Object classData) throws Throwable {
        byte[] original = sliceClassBytes(b, off, len);
        byte[] transformed = null;
        String className=null;
        if (original != null && original.length > 0) {
            className = resolveClassName(lookup, original);
            transformed = transformClassBytes(loader, className, original);
        }
        String cn=className!=null?className.replace("/","."):name;
        MethodHandle defineClass0 = ensureRawDefineClass0();
        if (transformed != null && transformed.length > 0) {
            return recordHidden((Class<?>) defineClass0.invoke(loader,lookup,name,transformed,0,transformed.length,pd,initialize,flags,classData),cn,name);
        }
        return recordHidden((Class<?>) defineClass0.invoke(loader,lookup,name,b,off,len,pd,initialize,flags,classData),cn,name);
    }
    public static Class<?> recordHidden(Class<?> hidden,String... possibleNames){
        if(hidden==null||!hidden.isHidden()){
            return hidden;
        }
        LinkedHashSet<String> normalizedNames = new LinkedHashSet<>();
        addNormalizedClassName(normalizedNames, hidden.getName());
        if (possibleNames != null) {
            for (String possibleName : possibleNames) {
                addNormalizedClassName(normalizedNames, possibleName);
            }
        }
        if (normalizedNames.isEmpty()) {
            return hidden;
        }
        synchronized (possibleHiddenToOriginalName) {
            String[] oldNames = possibleHiddenToOriginalName.get(hidden);
            if (oldNames != null) {
                normalizedNames.addAll(Arrays.asList(oldNames));
            }
            possibleHiddenToOriginalName.put(hidden, normalizedNames.toArray(String[]::new));
        }
        return hidden;
    }
    public static Class<?> tryGetOriginalClass(Class<?> hidden){
        if(hidden==null||!hidden.isHidden()){
            return hidden;
        }
        Class<?> cached = actualHiddenToOriginal.get(hidden);
        if (cached != null && isCompatibleOriginal(hidden, cached)) {
            return cached;
        }
        if (cached != null) {
            actualHiddenToOriginal.remove(hidden);
        }
        String[] possibleNames;
        synchronized (possibleHiddenToOriginalName) {
            String[] recordedNames = possibleHiddenToOriginalName.get(hidden);
            possibleNames = recordedNames == null ? null : recordedNames.clone();
        }
        if(possibleNames==null){
            String inferredName = normalizeBinaryClassName(hidden.getName());
            possibleNames = inferredName == null ? null : new String[]{inferredName};
        }
        if (possibleNames == null) {
            return hidden;
        }
        for (String possibleName : possibleNames) {
            Class<?> originalClass=findAlreadyLoadedClass(hidden.getClassLoader(), possibleName);
            if (isCompatibleOriginal(hidden, originalClass)) {
                actualHiddenToOriginal.put(hidden,originalClass);
                return originalClass;
            }
        }
        return hidden;
    }

    private static Class<?> findAlreadyLoadedClass(ClassLoader loader, String binaryName) {
        if (binaryName == null) {
            return null;
        }
        try {
            if (loader == null) {
                MethodHandle handle = findBootstrapClassOrNull;
                if (handle == null) {
                    synchronized (HiddenDefineHook.class) {
                        handle = findBootstrapClassOrNull;
                        if (handle == null) {
                            handle = ClassUtil.getLookup().findStatic(
                                    ClassLoader.class,
                                    "findBootstrapClassOrNull",
                                    MethodType.methodType(Class.class, String.class)
                            );
                            findBootstrapClassOrNull = handle;
                        }
                    }
                }
                return (Class<?>) handle.invoke(binaryName);
            }
            MethodHandle handle = findLoadedClass;
            if (handle == null) {
                synchronized (HiddenDefineHook.class) {
                    handle = findLoadedClass;
                    if (handle == null) {
                        handle = ClassUtil.getLookup().findVirtual(
                                ClassLoader.class,
                                "findLoadedClass",
                                MethodType.methodType(Class.class, String.class)
                        );
                        findLoadedClass = handle;
                    }
                }
            }
            return (Class<?>) handle.invoke(loader, binaryName);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void addNormalizedClassName(LinkedHashSet<String> names, String name) {
        String normalized = normalizeBinaryClassName(name);
        if (normalized != null) {
            names.add(normalized);
        }
    }

    private static String normalizeBinaryClassName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        int hiddenSuffix = normalized.indexOf("/0x");
        if (hiddenSuffix < 0) {
            hiddenSuffix = normalized.indexOf("+0x");
        }
        if (hiddenSuffix >= 0) {
            normalized = normalized.substring(0, hiddenSuffix);
        }
        normalized = normalized.replace('/', '.');
        return normalized.isEmpty() || normalized.indexOf('[') >= 0 || normalized.indexOf(';') >= 0
                ? null
                : normalized;
    }

    private static boolean isCompatibleOriginal(Class<?> hidden, Class<?> candidate) {
        if (candidate == null
                || candidate == hidden
                || candidate.isHidden()
                || candidate.getClassLoader() != hidden.getClassLoader()) {
            return false;
        }
        String hiddenBinaryName = normalizeBinaryClassName(hidden.getName());
        if (hiddenBinaryName == null || !hiddenBinaryName.equals(candidate.getName())) {
            return false;
        }
        if (candidate.getSuperclass() != hidden.getSuperclass()
                || !Arrays.equals(candidate.getInterfaces(), hidden.getInterfaces())) {
            return false;
        }
        try {
            Field[] hiddenFields = hidden.getDeclaredFields();
            Field[] candidateFields = candidate.getDeclaredFields();
            if (hiddenFields.length != candidateFields.length) {
                return false;
            }
            for (int i = 0; i < hiddenFields.length; i++) {
                Field hiddenField = hiddenFields[i];
                Field candidateField = candidateFields[i];
                if (!hiddenField.getName().equals(candidateField.getName())
                        || hiddenField.getType() != candidateField.getType()
                        || Modifier.isStatic(hiddenField.getModifiers())
                        != Modifier.isStatic(candidateField.getModifiers())) {
                    return false;
                }
            }
            return true;
        } catch (LinkageError | SecurityException ignored) {
            return false;
        }
    }

    private static byte[] transformClassBytes(ClassLoader loader, String className, byte[] original) {
        if (original == null || original.length == 0 || Boolean.TRUE.equals(TRANSFORMING_CLASS_BYTES.get())) {
            return null;
        }
        TRANSFORMING_CLASS_BYTES.set(Boolean.TRUE);
        try {
            return INSTANCE.transformClassByte(loader, className, original);
        } finally {
            TRANSFORMING_CLASS_BYTES.remove();
        }
    }

    private static MethodHandle ensureHookDefineClass0() throws NoSuchMethodException, IllegalAccessException {
        MethodHandle handle = hookDefineClass0;
        if (handle != null) {
            return handle;
        }
        synchronized (HiddenDefineHook.class) {
            handle = hookDefineClass0;
            if (handle == null) {
                handle = ClassUtil.getLookup().findStatic(
                        HiddenDefineHook.class,
                        "myDefineClass0",
                        DEFINE_CLASS0_TYPE
                );
                hookDefineClass0 = handle;
            }
            return handle;
        }
    }

    private static MethodHandle ensureRawDefineClass0() throws NoSuchMethodException, IllegalAccessException {
        MethodHandle handle = rawDefineClass0;
        if (handle != null) {
            return handle;
        }
        synchronized (HiddenDefineHook.class) {
            handle = rawDefineClass0;
            if (handle == null) {
                handle = ClassUtil.getLookup().findStatic(
                        ClassLoader.class,
                        "defineClass0",
                        DEFINE_CLASS0_TYPE
                );
                rawDefineClass0 = handle;
            }
            return handle;
        }
    }

    private static byte[] sliceClassBytes(byte[] bytes, int off, int len) {
        if (bytes == null || len <= 0 || off < 0 || off > bytes.length || off + len > bytes.length) {
            return null;
        }
        if (off == 0 && len == bytes.length) {
            return bytes;
        }
        return Arrays.copyOfRange(bytes, off, off + len);
    }

    private static String resolveClassName(Class<?> lookupClass, byte[] bytes) {
        try {
            ClassReader reader = new ClassReader(bytes);
            String className = reader.getClassName();
            if (className != null && !className.isBlank()) {
                return className;
            }
        } catch (Throwable ignored) {
        }
        if (lookupClass == null) {
            return null;
        }
        return lookupClass.getName().replace('.', '/');
    }
}
