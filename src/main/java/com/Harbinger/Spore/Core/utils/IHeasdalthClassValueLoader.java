package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.utils.wrappedMethod.IWrappedMethod;
import net.minecraft.network.syncher.EntityDataAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

interface IHeasdalthClassValueLoader {
    List<Field> scanAllHealthFields(Class<?> type);

    List<IWrappedMethod> scanAllSetHealthMethods(Class<?> type);

    List<Field> scanAllSubFields(Class<?> type);

    List<IWrappedMethod> scanTickDeathMethods(Class<?> type);

    List<Field> scanTickDeathFields(Class<?> type);

    Map<EntityDataAccessor<?>, String> buildAccessorNameMap(Class<?> type);

    List<IWrappedMethod> scanAllHurtMethods(Class<?> type);

    List<IWrappedMethod> scanDeathMethods(Class<?> type);

    List<Field> scanDeathFields(Class<?> type);

    Optional<MethodHandle> buildStaticMapGetHandle(Class<?> type);

    Optional<MethodHandle> buildStaticMapPutHandle(Class<?> type);
}
