package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Set;
import java.util.IdentityHashMap;

public final class UnsafePutHook implements IUnsafePutHook {
    public static final int DIRECT_HANDLE=0;
    public static final int BOUND_HANDLE=1;
    public static final IUnsafePutHook INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IUnsafePutHook.class,
            UnsafePutHook.class
    );
    private final Class<?> directMethodHandleClass;
    private final Class<?> boundMethodHandleClass;
    private final Class<?> memberNameClass;
    private MethodHandle memberName_getDeclaringClass;
    private MethodHandle memberName_getName;
    public UnsafePutHook() {
        Class<?> dmhClass=null;
        try{
            dmhClass=Class.forName("java.lang.invoke.DirectMethodHandle");
        } catch (ClassNotFoundException e) {
            LogUtil.errorf("failed to find class java.lang.invoke.DirectMethodHandle");
        }
        directMethodHandleClass=dmhClass;

        Class<?> bhmClass=null;
        try{
            bhmClass=Class.forName("java.lang.invoke.BoundMethodHandle");
        }catch (ClassNotFoundException e) {
            LogUtil.errorf("failed to find class java.lang.invoke.BoundMethodHandle");
        }
        boundMethodHandleClass=bhmClass;

        Class<?> mnClass=null;
        try{
            mnClass=Class.forName("java.lang.invoke.MemberName");
        }catch (ClassNotFoundException e) {
            LogUtil.errorf("failed to find class java.lang.invoke.MemberName");
        }

        memberNameClass=mnClass;

        memberName_getDeclaringClass=ensureVirtual(memberName_getDeclaringClass,
                memberNameClass,
                "getDeclaringClass",
                MethodType.methodType(Class.class));
    }
    private MethodHandle ensureVirtual(MethodHandle res, Class<?> refc, String name, MethodType type){
        if(res!=null){
            return res;
        }
        if(refc==null){
            return null;
        }
        try {
            res=ClassUtil.getLookup().findVirtual(
                refc, name, type
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            LogUtil.errorf("failed to find virtual method handle for %s",refc);
        }
        return res;
    }
    @Override
    public int isUnsafeRelatedMethodHandle(MethodHandle mh){
        return isUnsafeRelatedMethodHandle(mh, Collections.newSetFromMap(new IdentityHashMap<>()));
    }
    private int isUnsafeRelatedMethodHandle(MethodHandle mh, Set<MethodHandle> seen){
        if (mh == null || !seen.add(mh)) return -1;
        if(directMethodHandleClass!=null&&
                directMethodHandleClass.isInstance(mh)&&
                isUnsafeRelatedDirectMethodHandle(mh)){
            return DIRECT_HANDLE;
        }
        if(boundMethodHandleClass!=null&&
                boundMethodHandleClass.isInstance(mh)&&
                isUnsafeRelatedBoundMethodHandle(mh, seen)){
            return BOUND_HANDLE;
        }
        return -1;
    }
    @Override
    public boolean isSporeModTarget(Object target){
        if (target == null) {
            return false;
        }
        Class<?> targetClass=target instanceof Class<?> clz?clz:target.getClass();
        return targetClass.getName().startsWith("com.Harbinger.Spore.");
    }
    private boolean isUnsafeRelatedBoundMethodHandle(MethodHandle mh, Set<MethodHandle> seen){
        try {
            Class<?> current = mh.getClass();
            while (current != null && current != Object.class) {
                for (java.lang.reflect.Field field : current.getDeclaredFields()) {
                    if (!MethodHandle.class.isAssignableFrom(field.getType())) continue;
                    Object nested = ClassUtil.getFieldValue(field, mh);
                    if (nested instanceof MethodHandle handle && isUnsafeRelatedMethodHandle(handle, seen) >= 0) {
                        return true;
                    }
                }
                current = current.getSuperclass();
            }
        } catch (Throwable e) {
            LogUtil.errorf("failed to inspect bound method handle. %s",e.getMessage());
        }
        return false;
    }
    private boolean isUnsafeRelatedDirectMethodHandle(MethodHandle mh){
        Object member= ClassUtil.getFieldValue(directMethodHandleClass,mh, "member");
        if(member==null){
            return false;
        }
        memberName_getDeclaringClass=ensureVirtual(memberName_getDeclaringClass,memberNameClass,
                "getDeclaringClass",
                MethodType.methodType(Class.class));
        memberName_getName=ensureVirtual(memberName_getName,memberNameClass,
                "getName", MethodType.methodType(String.class));
        if(memberName_getDeclaringClass==null){
            return false;
        }
        try {
            if(memberName_getDeclaringClass.invoke(member) instanceof Class<?> clazz){
                String name=clazz.getName();
                if (!("sun.misc.Unsafe".equals(name)||"jdk.internal.misc.Unsafe".equals(name))) {
                    return false;
                }
                if (memberName_getName != null && memberName_getName.invoke(member) instanceof String methodName) {
                    return methodName.startsWith("put");
                }
                return true;
            }
        } catch (Throwable e) {
            LogUtil.errorf("failed to invoke direct method handle for %s. %s",member,e.getMessage());
        }
        return false;
    }

}
