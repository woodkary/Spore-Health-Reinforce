package com.Harbinger.Spore.Core.utils;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class SporeObjectUtil implements IObjects {
    public static final IObjects INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            IObjects.class,
            SporeObjectUtil.class
    );
    private Unsafe unsafe;
    private Object internalUnsafe;
    private Class<?> internalClass;
    private MethodHandle allocateInstance;
    @Override
    public <T> T clone(T obj){
        //先尝试使用sun.misc.Unsafe
        if(unsafe == null){
            unsafe=ClassUtil.getUnsafe();
        }
        Class<?> objClass = obj.getClass();
        try {
            T res = (T) unsafe.allocateInstance(objClass);
            if(res!=null){
                return deepCopyAllFields(res,obj,objClass);
            }
        } catch (InstantiationException e) {
            LogUtil.errorf("failed to create instance of %s,%s" , objClass,e.getMessage());
        }

        //再尝试使用jdk.internal.misc.Unsafe
        if(internalClass==null){
            try {
                internalClass = Class.forName("jdk.internal.misc.Unsafe");
            }catch (ClassNotFoundException e){
                LogUtil.errorf("failed to find jdk.internal.misc.Unsafe,%s",e.getMessage());
                return obj;
            }
        }
        if(internalUnsafe == null){
            internalUnsafe=ClassUtil.getInternalUnsafe();
        }
        if(internalUnsafe == null){
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
        try{
            T res=(T) allocateInstance.invoke(objClass);
            if(res!=null){
                return deepCopyAllFields(res,obj,objClass);
            }
        }catch (Throwable t) {
            LogUtil.errorf("failed to invoke allocateInstance method %s",t.getMessage());
        }
        return obj;
    }
    private <T> T deepCopyAllFields(T res,T obj,Class<?> objClass){
        for(Class<?> current=objClass;current!=null&&current!=Object.class;current=current.getSuperclass()){
            for (Field field : current.getDeclaredFields()) {
                if(Modifier.isStatic(field.getModifiers())){
                    continue;
                }
                ClassUtil.setFieldValue(field,res,ClassUtil.getFieldValue(field,obj));
            }
        }
        return res;
    }
}
