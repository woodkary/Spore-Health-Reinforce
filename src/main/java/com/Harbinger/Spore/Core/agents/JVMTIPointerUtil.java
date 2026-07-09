package com.Harbinger.Spore.Core.agents;

import com.Harbinger.Spore.Core.agents.transformers.SelfTransformer;
import com.Harbinger.Spore.Core.jvmti.JvmtiMethod;
import com.Harbinger.Spore.Core.utils.JvmtiCapabilities;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.sun.jna.Callback;
import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class JVMTIPointerUtil implements IJVNTIPointer {
    private static final int JVMTI_ENABLE = 1;
    private static final int JVMTI_EVENT_CLASS_FILE_LOAD_HOOK = 54;
    private static final int JVMTI_VERSION_1_2 = 0x30010200;
    private static final int JAVA_VM_GET_ENV_INDEX = 6;
    private static IJVNTIPointer INSTANCE;
    private static volatile boolean nativeBackendUnavailable;
    public static IJVNTIPointer newInstance(){
        if (INSTANCE == null && isNativeBackendAvailable()) {
            INSTANCE = new JVMTIPointerUtil(Pointer.NULL, true);
            return INSTANCE;
        }
        return newInstance(resolveJvmtiEnvPointer());
    }
    public static IJVNTIPointer newInstance(Pointer pointer){
        if(INSTANCE == null){
            Pointer envPointer = normalizeJvmtiEnvPointer(pointer);
            if (envPointer == null || Pointer.nativeValue(envPointer) == 0L) {
                LogUtil.error("JVMTI env pointer is unavailable.");
                return null;
            }
            INSTANCE = new JVMTIPointerUtil(envPointer, false);
        }
        return INSTANCE;
    }
    private final Pointer jvmti;
    private final boolean nativeBackend;
    private final List<SelfTransformer> transformers = new CopyOnWriteArrayList<>();
    private volatile boolean capabilitiesAdded;
    private volatile boolean eventCallbacksSet;
    private volatile boolean classFileLoadHookEnabled;
    private ClassFileLoadHookCallback classFileLoadHookCallback;
    private JvmtiEventCallbacks eventCallbacks;

    public JVMTIPointerUtil(Pointer pointer) {
        this(pointer, false);
    }

    private JVMTIPointerUtil(Pointer pointer, boolean nativeBackend) {
        this.jvmti = pointer;
        this.nativeBackend = nativeBackend;
    }

    @Override
    public IJVNTIPointer addTransformer(SelfTransformer transformer) {
        if (transformer == null) {
            return this;
        }
        if (!transformers.contains(transformer)) {
            transformers.add(transformer);
        }
        addCapabilities();
        setEventCallbacks();
        setEventNotificationMode();
        return this;
    }

    @Override
    public Class<?>[] getAllLoadedClasses() {
        if (nativeBackend) {
            Class<?>[] loaded = getAllLoadedClassesNative();
            if (loaded != null) {
                return loaded;
            }
        }
        return withLoadedClasses((count, classesArray) -> {
            List<Class<?>> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Pointer classPtr = classesArray.getPointer((long) i * Native.POINTER_SIZE);
                String signature = getClassSignature(classPtr);
                if (signature == null) {
                    continue;
                }
                Class<?> resolved = resolveClass(sigToClassName(signature));
                if (resolved != null) {
                    result.add(resolved);
                }
            }
            return result.toArray(new Class<?>[0]);
        }, new Class<?>[0]);
    }

    @Override
    public boolean isModifiableClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        if (nativeBackend) {
            return isModifiableClassNative(clazz);
        }
        return withClassPointer(clazz, classPtr -> {
            Function isModifiableClass = getFunction(JvmtiMethod.IS_MODIFIABLE_CLASS);
            if (isModifiableClass == null) {
                return false;
            }
            ByteByReference result = new ByteByReference();
            int error = isModifiableClass.invokeInt(new Object[]{jvmti, classPtr, result});
            return checkError("IsModifiableClass", error) && result.getValue() != 0;
        }, false);
    }

    @Override
    public boolean isRetransformClassesSupported() {
        addCapabilities();
        if (nativeBackend) {
            return canRetransformClassesNative();
        }
        JvmtiCapabilities capabilities = getCapabilities();
        return capabilities != null && capabilities.canRetransformClasses();
    }

    @Override
    public boolean isTransformerHookInstalled() {
        return eventCallbacksSet && classFileLoadHookEnabled;
    }

    @Override
    public IJVNTIPointer retransformClasses(Class<?>[] classes) {
        if (classes == null || classes.length == 0) {
            return this;
        }
        addCapabilities();
        if (!transformers.isEmpty()) {
            setEventCallbacks();
            setEventNotificationMode();
        }
        if (nativeBackend) {
            if (!retransformClassesNative(classes)) {
                throw new IllegalStateException("JVMTI failed to retransform " + classes.length + " classes");
            }
            return this;
        }
        Function retransformClasses = getFunction(JvmtiMethod.RETRANSFORM_CLASSES);
        if (retransformClasses == null) {
            return this;
        }
        List<Class<?>> failures = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (clazz == null) {
                continue;
            }
            Boolean transformed = withClassPointer(clazz, classPtr -> {
                Memory classArray = new Memory(Native.POINTER_SIZE);
                classArray.setPointer(0L, classPtr);
                int error = retransformClasses.invokeInt(new Object[]{jvmti, 1, classArray});
                return checkError("RetransformClasses " + clazz.getName(), error);
            }, false);
            if (!Boolean.TRUE.equals(transformed)) {
                failures.add(clazz);
            }
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("JVMTI failed to retransform " + failures.size() + " classes");
        }
        return this;
    }

    @Override
    public IJVNTIPointer allocate(){
        Function allocate = getFunction(JvmtiMethod.ALLOCATE);
        if (allocate == null) {
            LogUtil.error("JVMTI Allocate function is unavailable.");
        }
        return this;
    }

    public Pointer allocate(long size) {
        if (size <= 0) {
            return null;
        }
        Function allocate = getFunction(JvmtiMethod.ALLOCATE);
        if (allocate == null) {
            return null;
        }
        PointerByReference memoryRef = new PointerByReference();
        int error = allocate.invokeInt(new Object[]{jvmti, size, memoryRef});
        if (!checkError("Allocate", error)) {
            return null;
        }
        return memoryRef.getValue();
    }

    @Override
    public IJVNTIPointer deallocate(){
        return this;
    }

    public void deallocate(Pointer memory) {
        if (isNull(memory)) {
            return;
        }
        Function deallocate = getFunction(JvmtiMethod.DEALLOCATE);
        if (deallocate == null) {
            return;
        }
        int error = deallocate.invokeInt(new Object[]{jvmti, memory});
        checkError("Deallocate", error);
    }

    @Override
    public IJVNTIPointer addCapabilities(){
        if (capabilitiesAdded) {
            return this;
        }
        if (nativeBackend) {
            capabilitiesAdded = addCapabilitiesNative();
            return this;
        }
        JvmtiCapabilities capabilities = new JvmtiCapabilities()
                .setCanRedefineClasses(true)
                .setCanRedefineAnyClass(true)
                .setCanRetransformClasses(true)
                .setCanRetransformAnyClass(true)
                .setCanGenerateAllClassHookEvents(true);
        capabilities.write();
        if (addCapabilities(capabilities)) {
            capabilitiesAdded = true;
        }
        return this;
    }

    public boolean addCapabilities(JvmtiCapabilities capabilities) {
        if (capabilities == null) {
            return false;
        }
        Function addCapabilities = getFunction(JvmtiMethod.ADD_CAPABILITIES);
        if (addCapabilities == null) {
            return false;
        }
        capabilities.write();
        int error = addCapabilities.invokeInt(new Object[]{jvmti, capabilities.getPointer()});
        return checkError("AddCapabilities", error);
    }

    @Override
    public IJVNTIPointer setEventCallbacks(){
        if (eventCallbacksSet) {
            return this;
        }
        if (nativeBackend) {
            installNativeTransformerHook();
            return this;
        }
        classFileLoadHookCallback = this::onClassFileLoadHook;
        eventCallbacks = new JvmtiEventCallbacks();
        eventCallbacks.ClassFileLoadHook = classFileLoadHookCallback;
        eventCallbacks.write();
        Function setEventCallbacks = getFunction(JvmtiMethod.SET_EVENT_CALLBACKS);
        if (setEventCallbacks == null) {
            return this;
        }
        int error = setEventCallbacks.invokeInt(new Object[]{jvmti, eventCallbacks.getPointer(), eventCallbacks.size()});
        if (checkError("SetEventCallbacks", error)) {
            eventCallbacksSet = true;
        } else {
            eventCallbacks = null;
            classFileLoadHookCallback = null;
        }
        return this;
    }

    @Override
    public IJVNTIPointer setEventNotificationMode(){
        if (classFileLoadHookEnabled) {
            return this;
        }
        if (nativeBackend) {
            installNativeTransformerHook();
            return this;
        }
        setEventCallbacks();
        Function setEventNotificationMode = getFunction(JvmtiMethod.SET_EVENT_NOTIFICATION_MODE);
        if (setEventNotificationMode == null) {
            return this;
        }
        int error = setEventNotificationMode.invokeInt(new Object[]{
                jvmti,
                JVMTI_ENABLE,
                JVMTI_EVENT_CLASS_FILE_LOAD_HOOK,
                Pointer.NULL
        });
        if (checkError("SetEventNotificationMode ClassFileLoadHook", error)) {
            classFileLoadHookEnabled = true;
        }
        return this;
    }

    private void onClassFileLoadHook(Pointer jvmtiEnv,
                                     Pointer jniEnv,
                                     Pointer classBeingRedefined,
                                     Pointer loader,
                                     String name,
                                     Pointer protectionDomain,
                                     int classDataLen,
                                     Pointer classData,
                                     IntByReference newClassDataLen,
                                     PointerByReference newClassData) {
        if (transformers.isEmpty() || classDataLen <= 0 || isNull(classData)) {
            return;
        }
        try {
            String className = name;
            if ((className == null || className.isBlank()) && !isNull(classBeingRedefined)) {
                className = sigToInternalName(getClassSignature(classBeingRedefined));
            }
            if (className == null || className.isBlank()) {
                return;
            }
            byte[] current = classData.getByteArray(0L, classDataLen);
            boolean modified = false;
            for (SelfTransformer transformer : transformers) {
                byte[] transformed = transformer.transformClassByte(null, className, current);
                if (transformed != null && transformed.length > 0) {
                    current = transformed;
                    modified = true;
                }
            }
            if (!modified) {
                return;
            }
            Pointer output = allocate(current.length);
            if (output == null) {
                return;
            }
            output.write(0L, current, 0, current.length);
            newClassDataLen.setValue(current.length);
            newClassData.setValue(output);
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform class by JVMTI ClassFileLoadHook, %s", t.getMessage());
            LogUtil.printStackTrace(t);
        }
    }

    @SuppressWarnings("unused")
    private byte[] transformFromNative(ClassLoader loader, String className, byte[] classfileBuffer) {
        if (transformers.isEmpty() || className == null || className.isBlank()
                || classfileBuffer == null || classfileBuffer.length == 0) {
            return null;
        }
        try {
            byte[] current = classfileBuffer;
            boolean modified = false;
            for (SelfTransformer transformer : transformers) {
                byte[] transformed = transformer.transformClassByte(loader, className, current);
                if (transformed != null && transformed.length > 0) {
                    current = transformed;
                    modified = true;
                }
            }
            return modified ? current : null;
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform class by native JVMTI ClassFileLoadHook, %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }

    private void installNativeTransformerHook() {
        if (!nativeBackend || classFileLoadHookEnabled) {
            return;
        }
        if (installTransformerHookNative(this)) {
            eventCallbacksSet = true;
            classFileLoadHookEnabled = true;
        }
    }

    private static boolean isNativeBackendAvailable() {
        if (nativeBackendUnavailable) {
            return false;
        }
        ensureNativeBridgeLoaded();
        try {
            return isNativeJvmtiAvailable0();
        } catch (Throwable t) {
            nativeBackendUnavailable = true;
            LogUtil.errorf("Native JVMTI backend is unavailable, fallback to JNA: %s", t.getMessage());
            return false;
        }
    }

    private static void ensureNativeBridgeLoaded() {
        try {
            Class.forName(
                    "com.Harbinger.Spore.Core.agents.transformers.SporeClassFileTransformer0",
                    true,
                    JVMTIPointerUtil.class.getClassLoader()
            );
        } catch (Throwable t) {
            LogUtil.errorf("Failed to load transformer native bridge before JVMTI use: %s", t.getMessage());
        }
    }

    private boolean addCapabilitiesNative() {
        try {
            return addCapabilities0();
        } catch (Throwable t) {
            LogUtil.errorf("Native JVMTI AddCapabilities failed: %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return false;
        }
    }

    private boolean canRetransformClassesNative() {
        try {
            return canRetransformClasses0();
        } catch (Throwable t) {
            LogUtil.errorf("Native JVMTI GetCapabilities failed: %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return false;
        }
    }

    private Class<?>[] getAllLoadedClassesNative() {
        try {
            return getAllLoadedClasses0();
        } catch (Throwable t) {
            LogUtil.errorf("Native JVMTI GetLoadedClasses failed: %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }

    private boolean isModifiableClassNative(Class<?> clazz) {
        try {
            return isModifiableClass0(clazz);
        } catch (Throwable t) {
            LogUtil.errorf("Native JVMTI IsModifiableClass failed for %s: %s", clazz.getName(), t.getMessage());
            LogUtil.printStackTrace(t);
            return false;
        }
    }

    private boolean installTransformerHookNative(JVMTIPointerUtil owner) {
        try {
            return installTransformerHook0(owner);
        } catch (Throwable t) {
            LogUtil.errorf("Native JVMTI SetEventCallbacks failed: %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return false;
        }
    }

    private boolean retransformClassesNative(Class<?>[] classes) {
        try {
            return retransformClasses0(classes);
        } catch (Throwable t) {
            LogUtil.errorf("Native JVMTI RetransformClasses failed: %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return false;
        }
    }

    private static native boolean isNativeJvmtiAvailable0();

    private static native boolean addCapabilities0();

    private static native boolean canRetransformClasses0();

    private static native Class<?>[] getAllLoadedClasses0();

    private static native boolean isModifiableClass0(Class<?> clazz);

    private static native boolean installTransformerHook0(JVMTIPointerUtil owner);

    private static native boolean retransformClasses0(Class<?>[] classes);

    private JvmtiCapabilities getCapabilities() {
        Function getCapabilities = getFunction(JvmtiMethod.GET_CAPABILITIES);
        if (getCapabilities == null) {
            return null;
        }
        JvmtiCapabilities capabilities = new JvmtiCapabilities();
        capabilities.write();
        int error = getCapabilities.invokeInt(new Object[]{jvmti, capabilities.getPointer()});
        if (!checkError("GetCapabilities", error)) {
            return null;
        }
        capabilities.read();
        return capabilities;
    }

    private Function getFunction(JvmtiMethod method) {
        if (method == null || isNull(jvmti)) {
            return null;
        }
        try {
            Pointer table = jvmti.getPointer(0L);
            if (isNull(table)) {
                table = jvmti;
            }
            Pointer functionPointer = table.getPointer((long) (method.getIndex() - 1) * Native.POINTER_SIZE);
            if (isNull(functionPointer)) {
                LogUtil.errorf("JVMTI function pointer is null: %s", method);
                return null;
            }
            return Function.getFunction(functionPointer);
        } catch (Throwable t) {
            LogUtil.errorf("failed to resolve JVMTI function %s, %s", method, t.getMessage());
            return null;
        }
    }

    private static Pointer resolveJvmtiEnvPointer() {
        try {
            Pointer javaVM = getJavaVM();
            if (isNullStatic(javaVM)) {
                return null;
            }
            Pointer functions = javaVM.getPointer(0L);
            if (isNullStatic(functions)) {
                return null;
            }
            Pointer getEnvPointer = functions.getPointer((long) JAVA_VM_GET_ENV_INDEX * Native.POINTER_SIZE);
            if (isNullStatic(getEnvPointer)) {
                return null;
            }
            Function getEnv = Function.getFunction(getEnvPointer);
            PointerByReference jvmtiRef = new PointerByReference();
            int error = getEnv.invokeInt(new Object[]{javaVM, jvmtiRef, JVMTI_VERSION_1_2});
            if (error != 0) {
                LogUtil.errorf("JNI GetEnv(JVMTI) failed, error=%d", error);
                return null;
            }
            return jvmtiRef.getValue();
        } catch (Throwable t) {
            LogUtil.errorf("failed to resolve JVMTI env pointer, %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }

    private static Pointer normalizeJvmtiEnvPointer(Pointer pointer) {
        if (isNullStatic(pointer)) {
            return resolveJvmtiEnvPointer();
        }
        try {
            Pointer first = pointer.getPointer(0L);
            if (isNullStatic(first)) {
                Pointer resolved = resolveJvmtiEnvPointer();
                return isNullStatic(resolved) ? pointer : resolved;
            }
        } catch (Throwable ignored) {
        }
        return pointer;
    }

    private static Pointer getJavaVM() {
        Pointer[] vmBuf = new Pointer[1];
        IntByReference nVms = new IntByReference();
        int error = JvmDll.INSTANCE.JNI_GetCreatedJavaVMs(vmBuf, 1, nVms);
        if (error != 0 || nVms.getValue() <= 0 || isNullStatic(vmBuf[0])) {
            LogUtil.errorf("JNI_GetCreatedJavaVMs failed, error=%d, count=%d", error, nVms.getValue());
            return null;
        }
        return vmBuf[0];
    }

    private static String resolveJvmLibraryPath() {
        String javaHome = System.getProperty("java.home");
        if (javaHome != null && !javaHome.isBlank()) {
            File serverJvm = new File(javaHome, "bin/server/jvm.dll");
            if (serverJvm.isFile()) {
                return serverJvm.getAbsolutePath();
            }
            File clientJvm = new File(javaHome, "bin/client/jvm.dll");
            if (clientJvm.isFile()) {
                return clientJvm.getAbsolutePath();
            }
            File macJvm = new File(javaHome, "lib/server/libjvm.dylib");
            if (macJvm.isFile()) {
                return macJvm.getAbsolutePath();
            }
            File linuxJvm = new File(javaHome, "lib/server/libjvm.so");
            if (linuxJvm.isFile()) {
                return linuxJvm.getAbsolutePath();
            }
        }
        return "jvm";
    }

    private String getClassSignature(Pointer classPtr) {
        if (isNull(classPtr)) {
            return null;
        }
        Function getClassSignature = getFunction(JvmtiMethod.GET_CLASS_SIGNATURE);
        if (getClassSignature == null) {
            return null;
        }
        PointerByReference signatureRef = new PointerByReference();
        PointerByReference genericRef = new PointerByReference();
        int error = getClassSignature.invokeInt(new Object[]{jvmti, classPtr, signatureRef, genericRef});
        if (!checkError("GetClassSignature", error)) {
            return null;
        }
        Pointer signaturePtr = signatureRef.getValue();
        Pointer genericPtr = genericRef.getValue();
        try {
            return isNull(signaturePtr) ? null : signaturePtr.getString(0L);
        } finally {
            deallocate(signaturePtr);
            deallocate(genericPtr);
        }
    }

    private <T> T withClassPointer(Class<?> clazz, ClassPointerAction<T> action, T fallback) {
        if (clazz == null || action == null) {
            return fallback;
        }
        return withLoadedClasses((count, classesArray) -> {
            for (int i = 0; i < count; i++) {
                Pointer classPtr = classesArray.getPointer((long) i * Native.POINTER_SIZE);
                String signature = getClassSignature(classPtr);
                if (signature == null) {
                    continue;
                }
                if (matchesClassName(clazz, sigToClassName(signature))) {
                    return action.apply(classPtr);
                }
            }
            LogUtil.errorf("JVMTI loaded class not found: %s", clazz.getName());
            return fallback;
        }, fallback);
    }

    private <T> T withLoadedClasses(LoadedClassesAction<T> action, T fallback) {
        Function getLoadedClasses = getFunction(JvmtiMethod.GET_LOADED_CLASSES);
        if (getLoadedClasses == null || action == null) {
            return fallback;
        }
        IntByReference countRef = new IntByReference();
        PointerByReference classesRef = new PointerByReference();
        int error = getLoadedClasses.invokeInt(new Object[]{jvmti, countRef, classesRef});
        if (!checkError("GetLoadedClasses", error)) {
            return fallback;
        }
        Pointer classesArray = classesRef.getValue();
        try {
            if (isNull(classesArray)) {
                return fallback;
            }
            return action.apply(countRef.getValue(), classesArray);
        } catch (Throwable t) {
            LogUtil.errorf("failed to consume JVMTI loaded classes, %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return fallback;
        } finally {
            deallocate(classesArray);
        }
    }

    private boolean checkError(String action, int error) {
        if (error == 0) {
            return true;
        }
        LogUtil.errorf("JVMTI %s failed, error=%d%s", action, error, getErrorSuffix(error));
        return false;
    }

    private String getErrorSuffix(int error) {
        String errorName = getErrorName(error);
        return errorName == null || errorName.isBlank() ? "" : " (" + errorName + ")";
    }

    private String getErrorName(int error) {
        Function getErrorName = getFunction(JvmtiMethod.GET_ERROR_NAME);
        if (getErrorName == null) {
            return null;
        }
        PointerByReference nameRef = new PointerByReference();
        int result = getErrorName.invokeInt(new Object[]{jvmti, error, nameRef});
        if (result != 0) {
            return null;
        }
        Pointer namePtr = nameRef.getValue();
        try {
            return isNull(namePtr) ? null : namePtr.getString(0L);
        } finally {
            deallocate(namePtr);
        }
    }

    private Class<?> resolveClass(String className) {
        if (className == null || className.isBlank()) {
            return null;
        }
        for (ClassLoader loader : candidateClassLoaders()) {
            try {
                if (loader == null) {
                    return Class.forName(className, false, null);
                }
                return Class.forName(className, false, loader);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private List<ClassLoader> candidateClassLoaders() {
        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Thread.currentThread().getContextClassLoader());
        loaders.add(JVMTIPointerUtil.class.getClassLoader());
        loaders.add(ClassLoader.getSystemClassLoader());
        loaders.add(null);
        loaders.removeAll(Collections.singleton(null));
        loaders.add(null);
        return loaders;
    }

    private boolean matchesClassName(Class<?> clazz, String className) {
        if (clazz == null || className == null) {
            return false;
        }
        String name = clazz.getName();
        return name.equals(className)
                || name.replace('/', '.').equals(className)
                || name.equals(className.replace('.', '/'));
    }

    private String sigToClassName(String signature) {
        if (signature == null) {
            return null;
        }
        if (signature.startsWith("L") && signature.endsWith(";")) {
            return signature.substring(1, signature.length() - 1).replace('/', '.');
        }
        if (signature.startsWith("[")) {
            return signature.replace('/', '.');
        }
        return switch (signature) {
            case "I" -> "int";
            case "J" -> "long";
            case "Z" -> "boolean";
            case "B" -> "byte";
            case "C" -> "char";
            case "S" -> "short";
            case "F" -> "float";
            case "D" -> "double";
            case "V" -> "void";
            default -> signature;
        };
    }

    private String sigToInternalName(String signature) {
        if (signature == null) {
            return null;
        }
        if (signature.startsWith("L") && signature.endsWith(";")) {
            return signature.substring(1, signature.length() - 1);
        }
        return signature.replace('.', '/');
    }

    private boolean isNull(Pointer pointer) {
        return isNullStatic(pointer);
    }

    private static boolean isNullStatic(Pointer pointer) {
        return pointer == null || Pointer.nativeValue(pointer) == 0L;
    }

    private interface ClassPointerAction<T> {
        T apply(Pointer classPtr);
    }

    private interface LoadedClassesAction<T> {
        T apply(int count, Pointer classesArray);
    }

    public interface ClassFileLoadHookCallback extends Callback {
        void callback(Pointer jvmtiEnv,
                      Pointer jniEnv,
                      Pointer classBeingRedefined,
                      Pointer loader,
                      String name,
                      Pointer protectionDomain,
                      int classDataLen,
                      Pointer classData,
                      IntByReference newClassDataLen,
                      PointerByReference newClassData);
    }

    public static final class JvmtiEventCallbacks extends Structure {
        public Pointer VMInit;
        public Pointer VMDeath;
        public Pointer ThreadStart;
        public Pointer ThreadEnd;
        public ClassFileLoadHookCallback ClassFileLoadHook;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("VMInit", "VMDeath", "ThreadStart", "ThreadEnd", "ClassFileLoadHook");
        }
    }

    public interface JvmDll extends StdCallLibrary {
        JvmDll INSTANCE = Native.load(resolveJvmLibraryPath(), JvmDll.class);

        int JNI_GetCreatedJavaVMs(Pointer[] vmBuf, int bufLen, IntByReference nVms);
    }
}
