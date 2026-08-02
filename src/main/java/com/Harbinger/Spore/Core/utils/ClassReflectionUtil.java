package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeMap;
import com.Harbinger.Spore.Core.utils.unremovableCollections.SporeMapProxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Reflection member access which bypasses {@code Class.ReflectionData} after
 * the trusted lookup has been bootstrapped.
 */
public final class ClassReflectionUtil {
    private static final Class<?>[] NO_PARAMETER_TYPES = new Class<?>[0];
    private static final Object RAW_ACCESS_LOCK = new Object();
    private static final Object CACHE_INIT_LOCK = new Object();
    private static final ThreadLocal<Boolean> BOOTSTRAPPING_RAW_ACCESS = new ThreadLocal<>();

    private static volatile MethodHandle getDeclaredFields0;
    private static volatile MethodHandle getDeclaredMethods0;
    private static volatile boolean rawAccessInitializationAttempted;
    private static volatile boolean rawAccessFailureLogged;

    private static volatile ISporeMap<Class<?>, Field[]> declaredFieldsCache;
    private static volatile ISporeMap<Class<?>, Field[]> declaredPublicFieldsCache;
    private static volatile ISporeMap<Class<?>, Field[]> publicFieldsCache;
    private static volatile ISporeMap<Class<?>, Method[]> declaredMethodsCache;
    private static volatile ISporeMap<Class<?>, Method[]> declaredPublicMethodsCache;
    private static volatile ISporeMap<Class<?>, Method[]> publicMethodsCache;

    public static final Object publicFieldLock=new Object();
    public static final Object publicMethodLock=new Object();
    public static final Object declaredFieldLock=new Object();
    public static final Object declaredPublicFieldLock=new Object();
    public static final Object declaredMethodLock=new Object();
    public static final Object declaredPublicMethodLock=new Object();

    private static MethodHandle reflectionData;
    private static final Class<?> reflectionDataClass;
    private ClassReflectionUtil() {
    }

    public static Class<?> removeCachedReflectionData(Class<?> clazz){
        if(!ensureReflectionDataMethod(clazz)){
            return clazz;
        }
        Object refData=getRfData(clazz);
        if(refData==null){
            return clazz;
        }
        if(reflectionDataClass!=null){
            for (Field field : getDeclaredFields(reflectionDataClass)) {
                clearReflectionData(field, refData);
            }
        }
        for (Field field : getDeclaredFields(refData.getClass())) {
            clearReflectionData(field, refData);
        }
        return clazz;
    }
    public static Class<?> removeSpecialFieldsCache(Class<?> clazz,String... specialFieldNames){
        if(!ensureReflectionDataMethod(clazz)){
            return clazz;
        }
        Object refData=getRfData(clazz);
        if(refData==null){
            return clazz;
        }
        if(reflectionDataClass!=null){
            for (Field field : getDeclaredFields(reflectionDataClass)) {
                removeReflectionDataFields(field, refData,specialFieldNames);
            }
        }
        for (Field field : getDeclaredFields(refData.getClass())) {
            removeReflectionDataFields(field, refData,specialFieldNames);
        }
        return clazz;
    }
    public static Class<?> removeSpecialFieldsCache(Class<?> clazz,String n1,String n2){
        if(!ensureReflectionDataMethod(clazz)){
            return clazz;
        }
        Object refData=getRfData(clazz);
        if(refData==null){
            return clazz;
        }
        if(reflectionDataClass!=null){
            for (Field field : getDeclaredFields(reflectionDataClass)) {
                removeReflectionDataField(field, refData,n1);
                removeReflectionDataField(field, refData,n2);
            }
        }
        for (Field field : getDeclaredFields(refData.getClass())) {
            removeReflectionDataField(field, refData,n1);
            removeReflectionDataField(field, refData,n2);
        }
        return clazz;
    }
    public static Class<?> removeSpecialFieldsCache(Class<?> clazz,String n1,String n2,String n3){
        if(!ensureReflectionDataMethod(clazz)){
            return clazz;
        }
        Object refData=getRfData(clazz);
        if(refData==null){
            return clazz;
        }
        if(reflectionDataClass!=null){
            for (Field field : getDeclaredFields(reflectionDataClass)) {
                removeReflectionDataField(field, refData,n1);
                removeReflectionDataField(field, refData,n2);
                removeReflectionDataField(field, refData,n3);
            }
        }
        for (Field field : getDeclaredFields(refData.getClass())) {
            removeReflectionDataField(field, refData,n1);
            removeReflectionDataField(field, refData,n2);
            removeReflectionDataField(field, refData,n3);
        }
        return clazz;
    }
    public static Class<?> removeSpecialFieldsCache(Class<?> clazz,String n1){
        if(!ensureReflectionDataMethod(clazz)){
            return clazz;
        }
        Object refData=getRfData(clazz);
        if(refData==null){
            return clazz;
        }
        if(reflectionDataClass!=null){
            for (Field field : getDeclaredFields(reflectionDataClass)) {
                removeReflectionDataField(field, refData,n1);
            }
        }
        for (Field field : getDeclaredFields(refData.getClass())) {
            removeReflectionDataField(field, refData,n1);
        }
        return clazz;
    }
    private static Object getRfData(Class<?> clazz){
        if(reflectionData!=null){
            try {
                return reflectionData.invoke(clazz);
            } catch (Throwable e) {
                LogUtil.errorf("failed to get reflectionData() for class %s, %s",clazz.getName(),e.getMessage());
            }
        }
        return null;
    }
    private static boolean ensureReflectionDataMethod(Class<?> clazz){
        if(reflectionDataClass==null){
            return false;
        }
        //先触发ReflectionData缓存
        clazz.getDeclaredFields();
        clazz.getFields();

        clazz.getDeclaredMethods();
        clazz.getMethods();
        //再清空
        if(reflectionData == null){
            try {
                reflectionData = ClassUtil.getLookup().findSpecial(
                        Class.class,
                        "reflectionData",
                        MethodType.methodType(reflectionDataClass),
                        Class.class
                );
            }catch (IllegalAccessException | NoSuchMethodException e) {
                LogUtil.errorf("ReflectionData class not found of class %s",clazz.getName());
                return false;
            }
        }
        return true;
    }
    private static void removeReflectionDataField(Field field, Object refData,String fieldName){
        if(refData==null){
            return;
        }
        String name = field.getName();
        Class<?> type = field.getType();
        if(("declaredFields".equals(name)||
                "publicFields".equals(name)||
                "declaredPublicFields".equals(name))&&Field[].class==type){
            Object temp=ClassUtil.getFieldValue(field,refData);
            if(!(temp instanceof Field[] rfDataFields)){
                return;
            }
            List<Field> newFields=new ArrayList<>();
            for (Field rfDataField : rfDataFields) {
                if(!rfDataField.getName().equals(fieldName)){
                    newFields.add(rfDataField);
                }
            }
            ClassUtil.setFieldValue(field,refData,newFields.toArray(new Field[0]));
        }
    }
    private static void removeReflectionDataFields(Field field, Object refData,String... fieldNames){
        if(refData==null){
            return;
        }
        Set<String> fNames=new HashSet<>(Arrays.asList(fieldNames));
        String name = field.getName();
        Class<?> type = field.getType();
        if(("declaredFields".equals(name)||
                "publicFields".equals(name)||
                "declaredPublicFields".equals(name))&&Field[].class==type){
            Object temp=ClassUtil.getFieldValue(field,refData);
            if(!(temp instanceof Field[] rfDataFields)){
                return;
            }
            List<Field> newFields=new ArrayList<>();
            for (Field rfDataField : rfDataFields) {
                if(!fNames.contains(rfDataField.getName())){
                    newFields.add(rfDataField);
                }
            }
            ClassUtil.setFieldValue(field,refData,newFields.toArray(new Field[0]));
        }
    }
    private static void clearReflectionData(Field field, Object refData){
        if(refData==null){
            return;
        }
        String name = field.getName();
        Class<?> type = field.getType();
        if(("declaredFields".equals(name)||
                "publicFields".equals(name)||
                "declaredPublicFields".equals(name))&&Field[].class==type){
            ClassUtil.setFieldValue(field,refData,new Field[0]);
        }
        if(("declaredMethods".equals(name)||
                "publicMethods".equals(name)||
                "declaredPublicMethods".equals(name))&&Method[].class==type){
            ClassUtil.setFieldValue(field,refData,new Method[0]);
        }
    }

    public static Field getDeclaredField(Class<?> targetClass, String name) throws NoSuchFieldException {
        Objects.requireNonNull(targetClass, "targetClass");
        Objects.requireNonNull(name, "name");
        for (Field field : getDeclaredFields(targetClass)) {
            if (name.equals(field.getName())) {
                return field;
            }
        }
        throw new NoSuchFieldException(targetClass.getName() + "." + name);
    }

    public static Method getDeclaredMethod(Class<?> targetClass,
                                           String name,
                                           Class<?>... parameterTypes) throws NoSuchMethodException {
        Objects.requireNonNull(targetClass, "targetClass");
        Objects.requireNonNull(name, "name");
        return findMethod(
                targetClass,
                name,
                normalizeParameterTypes(parameterTypes),
                getDeclaredMethods(targetClass)
        );
    }

    public static Field[] getDeclaredFields(Class<?> targetClass) {
        Objects.requireNonNull(targetClass, "targetClass");
        if (Boolean.TRUE.equals(BOOTSTRAPPING_RAW_ACCESS.get())) {
            return readDeclaredFields(targetClass, false).clone();
        }
        ensureRawAccess();
        ensureCaches();
        return getCachedFields(declaredFieldsCache, targetClass, false).clone();
    }

    public static Method[] getDeclaredMethods(Class<?> targetClass) {
        Objects.requireNonNull(targetClass, "targetClass");
        if (Boolean.TRUE.equals(BOOTSTRAPPING_RAW_ACCESS.get())) {
            return readDeclaredMethods(targetClass, false).clone();
        }
        ensureRawAccess();
        ensureCaches();
        return getCachedMethods(declaredMethodsCache, targetClass, false).clone();
    }

    public static Field getField(Class<?> targetClass, String name) throws NoSuchFieldException {
        Objects.requireNonNull(targetClass, "targetClass");
        Objects.requireNonNull(name, "name");
        for (Field field : getFields(targetClass)) {
            if (name.equals(field.getName())) {
                return field;
            }
        }
        throw new NoSuchFieldException(targetClass.getName() + "." + name);
    }

    public static Method getMethod(Class<?> targetClass,
                                   String name,
                                   Class<?>... parameterTypes) throws NoSuchMethodException {
        Objects.requireNonNull(targetClass, "targetClass");
        Objects.requireNonNull(name, "name");
        return findMethod(
                targetClass,
                name,
                normalizeParameterTypes(parameterTypes),
                getMethods(targetClass)
        );
    }

    public static Field[] getFields(Class<?> targetClass) {
        Objects.requireNonNull(targetClass, "targetClass");
        ensureRawAccess();
        ensureCaches();
        Field[] cached = publicFieldsCache.get(targetClass);
        if (cached != null) {
            return cached.clone();
        }
        synchronized (publicFieldLock) {
            cached = publicFieldsCache.get(targetClass);
            if (cached == null) {
                List<Field> fields = new ArrayList<>();
                collectPublicFields(targetClass, fields, new HashSet<>());
                cached = fields.toArray(new Field[0]);
                publicFieldsCache.actualPut(targetClass, cached);
            }
        }
        return cached.clone();
    }

    public static Method[] getMethods(Class<?> targetClass) {
        Objects.requireNonNull(targetClass, "targetClass");
        ensureRawAccess();
        ensureCaches();
        Method[] cached = publicMethodsCache.get(targetClass);
        if (cached != null) {
            return cached.clone();
        }
        synchronized (publicMethodLock) {
            cached = publicMethodsCache.get(targetClass);
            if (cached == null) {
                List<Method> methods = new ArrayList<>();
                Set<String> signatures = new LinkedHashSet<>();
                collectPublicMethods(targetClass, methods, signatures);
                cached = methods.toArray(new Method[0]);
                publicMethodsCache.actualPut(targetClass, cached);
            }
        }
        return cached.clone();
    }

    private static Field[] getDeclaredPublicFields(Class<?> targetClass) {
        ensureCaches();
        return getCachedFields(declaredPublicFieldsCache, targetClass, true);
    }

    private static Method[] getDeclaredPublicMethods(Class<?> targetClass) {
        ensureCaches();
        return getCachedMethods(declaredPublicMethodsCache, targetClass, true);
    }

    private static Field[] getCachedFields(ISporeMap<Class<?>, Field[]> cache,
                                           Class<?> targetClass,
                                           boolean publicOnly) {
        Field[] cached = cache.get(targetClass);
        if (cached != null) {
            return cached;
        }
        synchronized (publicOnly?declaredPublicFieldLock:declaredFieldLock) {
            cached = cache.get(targetClass);
            if (cached == null) {
                cached = readDeclaredFields(targetClass, publicOnly);
                cache.actualPut(targetClass, cached);
            }
        }
        return cached;
    }

    private static Method[] getCachedMethods(ISporeMap<Class<?>, Method[]> cache,
                                             Class<?> targetClass,
                                             boolean publicOnly) {
        Method[] cached = cache.get(targetClass);
        if (cached != null) {
            return cached;
        }
        synchronized (publicOnly?declaredPublicMethodLock:declaredMethodLock) {
            cached = cache.get(targetClass);
            if (cached == null) {
                cached = readDeclaredMethods(targetClass, publicOnly);
                cache.actualPut(targetClass, cached);
            }
        }
        return cached;
    }

    private static Field[] readDeclaredFields(Class<?> targetClass, boolean publicOnly) {
        MethodHandle handle = ensureRawDeclaredFieldsHandle();
        if (handle != null) {
            try {
                return (Field[]) handle.invoke(targetClass, publicOnly);
            } catch (Throwable throwable) {
                logRawAccessFailure("fields", targetClass, throwable);
            }
        }
        Field[] fields = targetClass.getDeclaredFields();
        return publicOnly ? filterPublicFields(fields) : fields;
    }

    private static Method[] readDeclaredMethods(Class<?> targetClass, boolean publicOnly) {
        MethodHandle handle = ensureRawDeclaredMethodsHandle();
        if (handle != null) {
            try {
                return (Method[]) handle.invoke(targetClass, publicOnly);
            } catch (Throwable throwable) {
                logRawAccessFailure("methods", targetClass, throwable);
            }
        }
        Method[] methods = targetClass.getDeclaredMethods();
        return publicOnly ? filterPublicMethods(methods) : methods;
    }

    private static MethodHandle ensureRawDeclaredFieldsHandle() {
        ensureRawAccess();
        return getDeclaredFields0;
    }

    private static MethodHandle ensureRawDeclaredMethodsHandle() {
        ensureRawAccess();
        return getDeclaredMethods0;
    }

    private static void ensureRawAccess() {
        if (getDeclaredFields0 != null && getDeclaredMethods0 != null) {
            return;
        }
        if (Boolean.TRUE.equals(BOOTSTRAPPING_RAW_ACCESS.get())) {
            return;
        }
        Throwable initializationFailure = null;
        synchronized (RAW_ACCESS_LOCK) {
            if (getDeclaredFields0 != null && getDeclaredMethods0 != null) {
                return;
            }
            if (rawAccessInitializationAttempted) {
                return;
            }
            rawAccessInitializationAttempted = true;
            BOOTSTRAPPING_RAW_ACCESS.set(Boolean.TRUE);
            try {
                MethodHandles.Lookup lookup = ClassUtil.getLookup();
                MethodHandle fieldsHandle = lookup.findVirtual(
                        Class.class,
                        "getDeclaredFields0",
                        MethodType.methodType(Field[].class, boolean.class)
                );
                MethodHandle methodsHandle = lookup.findVirtual(
                        Class.class,
                        "getDeclaredMethods0",
                        MethodType.methodType(Method[].class, boolean.class)
                );
                getDeclaredFields0 = fieldsHandle;
                getDeclaredMethods0 = methodsHandle;
            } catch (Throwable throwable) {
                initializationFailure = throwable;
            } finally {
                BOOTSTRAPPING_RAW_ACCESS.remove();
            }
        }
        if (initializationFailure != null) {
            logRawAccessFailure("member handles", Class.class, initializationFailure);
        }
    }

    private static void ensureCaches() {
        if (declaredFieldsCache != null) {
            return;
        }
        synchronized (CACHE_INIT_LOCK) {
            if (declaredFieldsCache != null) {
                return;
            }
            ISporeMap<Class<?>, Field[]> newDeclaredFieldsCache = newProtectedWeakCache();
            ISporeMap<Class<?>, Field[]> newDeclaredPublicFieldsCache = newProtectedWeakCache();
            ISporeMap<Class<?>, Field[]> newPublicFieldsCache = newProtectedWeakCache();
            ISporeMap<Class<?>, Method[]> newDeclaredMethodsCache = newProtectedWeakCache();
            ISporeMap<Class<?>, Method[]> newDeclaredPublicMethodsCache = newProtectedWeakCache();
            ISporeMap<Class<?>, Method[]> newPublicMethodsCache = newProtectedWeakCache();

            declaredPublicFieldsCache = newDeclaredPublicFieldsCache;
            publicFieldsCache = newPublicFieldsCache;
            declaredMethodsCache = newDeclaredMethodsCache;
            declaredPublicMethodsCache = newDeclaredPublicMethodsCache;
            publicMethodsCache = newPublicMethodsCache;
            // This volatile write publishes the other five caches as one initialized group.
            declaredFieldsCache = newDeclaredFieldsCache;
        }
    }

    private static <T> ISporeMap<Class<?>, T> newProtectedWeakCache() {
        return SporeMapProxy.newInstance(Collections.synchronizedMap(new WeakHashMap<>()));
    }

    private static void collectPublicFields(Class<?> targetClass,
                                            List<Field> fields,
                                            Set<Class<?>> visitedInterfaces) {
        fields.addAll(Arrays.asList(getDeclaredPublicFields(targetClass)));
        for (Class<?> interfaceClass : targetClass.getInterfaces()) {
            collectPublicInterfaceFields(interfaceClass, fields, visitedInterfaces);
        }
        Class<?> superClass = targetClass.getSuperclass();
        if (superClass != null) {
            collectPublicFields(superClass, fields, visitedInterfaces);
        }
    }

    private static void collectPublicInterfaceFields(Class<?> interfaceClass,
                                                     List<Field> fields,
                                                     Set<Class<?>> visitedInterfaces) {
        if (!visitedInterfaces.add(interfaceClass)) {
            return;
        }
        fields.addAll(Arrays.asList(getDeclaredPublicFields(interfaceClass)));
        for (Class<?> parentInterface : interfaceClass.getInterfaces()) {
            collectPublicInterfaceFields(parentInterface, fields, visitedInterfaces);
        }
    }

    private static void collectPublicMethods(Class<?> targetClass,
                                             List<Method> methods,
                                             Set<String> signatures) {
        if (targetClass.isInterface()) {
            collectPublicInterfaceMethods(
                    targetClass,
                    targetClass,
                    methods,
                    signatures,
                    new HashSet<>()
            );
            return;
        }
        for (Class<?> current = targetClass; current != null; current = current.getSuperclass()) {
            addPublicMethods(targetClass, current, methods, signatures);
        }
        Set<Class<?>> visitedInterfaces = new HashSet<>();
        for (Class<?> current = targetClass; current != null; current = current.getSuperclass()) {
            for (Class<?> interfaceClass : current.getInterfaces()) {
                collectPublicInterfaceMethods(
                        targetClass,
                        interfaceClass,
                        methods,
                        signatures,
                        visitedInterfaces
                );
            }
        }
    }

    private static void collectPublicInterfaceMethods(Class<?> rootClass,
                                                      Class<?> interfaceClass,
                                                      List<Method> methods,
                                                      Set<String> signatures,
                                                      Set<Class<?>> visitedInterfaces) {
        if (!visitedInterfaces.add(interfaceClass)) {
            return;
        }
        addPublicMethods(rootClass, interfaceClass, methods, signatures);
        for (Class<?> parentInterface : interfaceClass.getInterfaces()) {
            collectPublicInterfaceMethods(rootClass, parentInterface, methods, signatures, visitedInterfaces);
        }
    }

    private static void addPublicMethods(Class<?> rootClass,
                                         Class<?> declaringClass,
                                         List<Method> methods,
                                         Set<String> signatures) {
        for (Method method : getDeclaredPublicMethods(declaringClass)) {
            if (declaringClass.isInterface()
                    && declaringClass != rootClass
                    && Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (signatures.add(methodSignature(method))) {
                methods.add(method);
            }
        }
    }

    private static String methodSignature(Method method) {
        StringBuilder signature = new StringBuilder(method.getName()).append('(');
        for (Class<?> parameterType : method.getParameterTypes()) {
            signature.append(parameterType.getName()).append(';');
        }
        return signature.append(')').append(method.getReturnType().getName()).toString();
    }

    private static Method findMethod(Class<?> targetClass,
                                     String name,
                                     Class<?>[] parameterTypes,
                                     Method[] methods) throws NoSuchMethodException {
        Method selected = null;
        for (Method method : methods) {
            if (!name.equals(method.getName())
                    || !Arrays.equals(parameterTypes, method.getParameterTypes())) {
                continue;
            }
            if (selected == null
                    || selected.getReturnType().isAssignableFrom(method.getReturnType())) {
                selected = method;
            }
        }
        if (selected != null) {
            return selected;
        }
        throw new NoSuchMethodException(methodDescription(targetClass, name, parameterTypes));
    }

    private static String methodDescription(Class<?> targetClass,
                                            String name,
                                            Class<?>[] parameterTypes) {
        StringBuilder description = new StringBuilder(targetClass.getName())
                .append('.')
                .append(name)
                .append('(');
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                description.append(',');
            }
            description.append(parameterTypes[i] == null ? "null" : parameterTypes[i].getName());
        }
        return description.append(')').toString();
    }

    private static Class<?>[] normalizeParameterTypes(Class<?>[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return NO_PARAMETER_TYPES;
        }
        Class<?>[] normalized = parameterTypes.clone();
        for (Class<?> parameterType : normalized) {
            Objects.requireNonNull(parameterType, "parameterType");
        }
        return normalized;
    }

    private static Field[] filterPublicFields(Field[] fields) {
        List<Field> publicFields = new ArrayList<>(fields.length);
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())) {
                publicFields.add(field);
            }
        }
        return publicFields.toArray(new Field[0]);
    }

    private static Method[] filterPublicMethods(Method[] methods) {
        List<Method> publicMethods = new ArrayList<>(methods.length);
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                publicMethods.add(method);
            }
        }
        return publicMethods.toArray(new Method[0]);
    }

    private static void logRawAccessFailure(String memberKind,
                                            Class<?> targetClass,
                                            Throwable throwable) {
        if (rawAccessFailureLogged) {
            return;
        }
        synchronized (RAW_ACCESS_LOCK) {
            if (rawAccessFailureLogged) {
                return;
            }
            rawAccessFailureLogged = true;
            LogUtil.errorf(
                    "failed to access raw declared %s for %s: %s",
                    memberKind,
                    targetClass.getName(),
                    throwable.getMessage()
            );
        }
    }


    static{
        Class<?> rd=null;
        try {
            rd = Class.forName("java.lang.Class$ReflectionData");
        }catch (ClassNotFoundException e) {
            LogUtil.errorf("ReflectionData class not found");
        }
        reflectionDataClass = rd;
    }

}
