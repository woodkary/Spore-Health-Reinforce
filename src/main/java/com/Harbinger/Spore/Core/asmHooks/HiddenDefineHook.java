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
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;

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
    // 反射调用lookup.defineHiddenClass或makeHiddenClassDefiner
    // 在所有method.invoke调用时插入，
    // 如果是满足条件的method，收集其第一个参数MethodHandles.Lookup和参数列表中的byte[]参数，
    // 再将byte[]参数替换为lookupDefineHiddenClassHook的结果
    public static boolean isDefineClassOrMakeHiddenClassDefiner(Method method){
        if(method == null || method.getDeclaringClass()!=MethodHandles.Lookup.class){
            return false;
        }
        String methodName = method.getName();
        if(!methodName.equals("defineHiddenClass")&&!methodName.equals("makeHiddenClassDefiner")){
            return false;
        }
        for (Class<?> paraType : method.getParameterTypes()) {
            if(paraType.equals(byte[].class)){
                return true;
            }
        }
        return false;
    }

    public static Object[] reflectiveHiddenClassArgumentsHook(Method method, Object receiver, Object[] arguments) {
        if (!(receiver instanceof MethodHandles.Lookup hostLookup)
                || arguments == null
                || !isDefineClassOrMakeHiddenClassDefiner(method)) {
            return arguments;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        int argumentCount = Math.min(parameterTypes.length, arguments.length);
        Object[] hookedArguments = arguments;
        for (int i = 0; i < argumentCount; i++) {
            if (parameterTypes[i] != byte[].class || !(arguments[i] instanceof byte[] original)) {
                continue;
            }
            byte[] transformed = lookupDefineHiddenClassHook(hostLookup, original);
            if (transformed != original) {
                if (hookedArguments == arguments) {
                    hookedArguments = arguments.clone();
                }
                hookedArguments[i] = transformed;
            }
        }
        return hookedArguments;
    }

    //直接调用lookup.defineHiddenClass(...)的改调用钩子，需要调用者lookup，和参数byte[]
    public static byte[] lookupDefineHiddenClassHook(MethodHandles.Lookup hostLookup,byte[] original){
        if (original == null || original.length == 0) {
            return original;
        }
        Class<?> lookupClass = hostLookup == null ? null : hostLookup.lookupClass();
        String className = resolveClassName(lookupClass, original);
        ClassLoader classLoader = lookupClass == null ? null : lookupClass.getClassLoader();
        byte[] transformed = INSTANCE.transformClassByte(classLoader, className, original);
        return transformed == null || transformed.length == 0 ? original : transformed;
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
        if (original != null && original.length > 0) {
            String className = resolveClassName(lookup, original);
            transformed = INSTANCE.transformClassByte(loader, className, original);
        }
        MethodHandle defineClass0 = ensureRawDefineClass0();
        if (transformed != null && transformed.length > 0) {
            return (Class<?>) defineClass0.invoke(loader,lookup,name,transformed,0,transformed.length,pd,initialize,flags,classData);
        }
        return (Class<?>) defineClass0.invoke(loader,lookup,name,b,off,len,pd,initialize,flags,classData);
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
