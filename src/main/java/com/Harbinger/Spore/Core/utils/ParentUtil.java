package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.Entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author karywoodOyo
 */
public class ParentUtil implements IParents,Function<Class<?>, List<Method>> {
    public static final IParents INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            IParents.class,
            ParentUtil.class
    );
    // 缓存每个类中可能的 getParent 方法
    private final Map<Class<?>, List<Method>> GET_PARENT_METHODS = new ConcurrentHashMap<>();
    /**
     * 获取缓存的 getParent 方法
     */
    private List<Method> getParentMethods(Class<?> clazz) {
        return GET_PARENT_METHODS.computeIfAbsent(clazz,this);
    }
    @Override
    public List<Method> apply(Class<?> c) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> current = c;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Method m : current.getDeclaredMethods()) {
                if (m.getParameterCount() == 0 &&
                        m.getName().equalsIgnoreCase("getParent") &&
                        Entity.class.isAssignableFrom(m.getReturnType())) {
                    m.setAccessible(true);
                    methods.add(m);
                }
            }
        }
        return methods;
    }

    /**
     * 递归获取实体的最终 parent
     */
    public Entity getUltimateParent(Entity entity) {
        if (entity == null) return null;

        List<Method> methods = getParentMethods(entity.getClass());
        for (Method m : methods) {
            try {
                Object result = m.invoke(entity);
                if (result instanceof Entity parent) {
                    // 如果 parent 还有 getParent 方法，递归调用
                    return getUltimateParent(parent);
                }
            } catch (Exception ignored) {
            }
        }
        return entity;
    }
}
