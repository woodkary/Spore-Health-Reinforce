package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SporeObjectUtil implements IObjects {
    public static final IObjects INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            IObjects.class,
            SporeObjectUtil.class
    );
    private Unsafe unsafe;
    private Object internalUnsafe;
    private Class<?> internalClass;
    private MethodHandle allocateInstance;
    private final ThreadLocal<Integer> noRecur=new ThreadLocal<>();
    private final Set<Class<?>> unInstantiableClasses=new HashSet<>();
    @Override
    public <T> T clone(T obj){
        Integer deep=noRecur.get();
        int recurCount=deep==null?0:deep;
        if(recurCount>4){
            return obj;
        }
        try {
            noRecur.set(recurCount+1);
            //先尝试使用sun.misc.Unsafe
            if (unsafe == null) {
                unsafe = ClassUtil.getUnsafe();
            }
            Class<?> objClass = obj.getClass();
            if(unInstantiableClasses.contains(objClass)){
                return obj;
            }
            boolean unInstantiable = false;
            try {
                T res = (T) unsafe.allocateInstance(objClass);
                if (res != null) {
                    return deepCopyAllFields(res, obj, objClass);
                }
            } catch (InstantiationException e) {
                LogUtil.errorf("%s is uninstantiable,skipping. %s", objClass, e.getMessage());
                unInstantiableClasses.add(objClass);
                unInstantiable = true;
            } catch (Throwable t) {
                LogUtil.errorf("failed to invoke allocateInstance method %s", t.getMessage());
            }
            if (unInstantiable) {
                return obj;
            }

            //再尝试使用jdk.internal.misc.Unsafe
            if (internalClass == null) {
                try {
                    internalClass = Class.forName("jdk.internal.misc.Unsafe");
                } catch (ClassNotFoundException e) {
                    LogUtil.errorf("failed to find jdk.internal.misc.Unsafe,%s", e.getMessage());
                    return obj;
                }
            }
            if (internalUnsafe == null) {
                internalUnsafe = ClassUtil.getInternalUnsafe();
            }
            if (internalUnsafe == null) {
                return obj;
            }
            if (allocateInstance == null) {
                try {
                    allocateInstance = ClassUtil.getLookup().findSpecial(
                            internalClass,
                            "allocateInstance",
                            MethodType.methodType(Object.class, Class.class),
                            internalUnsafe.getClass()
                    );
                    allocateInstance = allocateInstance.bindTo(internalUnsafe);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to find allocateInstance method for jdk.internal.misc.Unsafe. %s", t.getMessage());
                    return obj;
                }
            }
            if (allocateInstance == null) {
                return obj;
            }
            try {
                T res = (T) allocateInstance.invoke(objClass);
                if (res != null) {
                    return deepCopyAllFields(res, obj, objClass);
                }
            } catch (InstantiationException e){
                LogUtil.errorf("%s is uninstantiable,skipping. %s", objClass, e.getMessage());
                unInstantiableClasses.add(objClass);
            } catch (Throwable t) {
                LogUtil.errorf("failed to invoke allocateInstance method %s", t.getMessage());
            }
            return obj;
        }finally {
            if(recurCount>0){
                noRecur.set(recurCount);
            }else{
                noRecur.remove();
            }
        }
    }
    private <T> T deepCopyAllFields(T res,T obj,Class<?> objClass){
        for(Class<?> current=objClass;current!=null&&current!=Object.class;current=current.getSuperclass()){
            for (Field field : current.getDeclaredFields()) {
                if(Modifier.isStatic(field.getModifiers())){
                    continue;
                }
                Class<?> fieldType = field.getType();
                Object fieldValue = ClassUtil.getFieldValue(field, obj);
                if(fieldValue==null){
                    continue;
                }
                if(fieldType.isPrimitive()||
                        Map.class.isAssignableFrom(fieldType)||
                        Collection.class.isAssignableFrom(fieldType)||
                        Number.class.isAssignableFrom(fieldType)||
                        Entity.class.isAssignableFrom(fieldType)||
                        Level.class.isAssignableFrom(fieldType)){
                    ClassUtil.setFieldValue(field, res, fieldValue);
                }else{
                    ClassUtil.setFieldValue(field, res,clone(fieldValue));
                }

            }
        }
        return res;
    }
}
