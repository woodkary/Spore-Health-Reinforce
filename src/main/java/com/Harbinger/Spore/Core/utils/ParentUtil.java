package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author karywoodOyo
 */
public final class ParentUtil implements IParents,Function<Class<?>, List<MethodHandle>> {
    public static final IParents INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            IParents.class,
            ParentUtil.class
    );
    // 缓存每个类中可能的 getParent 方法
    private final Map<Class<?>, List<MethodHandle>> GET_PARENT_METHODS = new ConcurrentHashMap<>();

    /**
     * 获取缓存的 getParent 方法
     */
    private List<MethodHandle> getParentMethods(Class<?> clazz) {
        return GET_PARENT_METHODS.computeIfAbsent(clazz, this);
    }

    @Override
    public List<MethodHandle> apply(Class<?> c) {
        List<MethodHandle> methods = new ArrayList<>();
        traverseAndFindMethods(c, methods);
        return methods;
    }

    /**
     * 递归遍历类和接口，查找符合条件的方法
     */
    private void traverseAndFindMethods(Class<?> current, List<MethodHandle> methods) {
        if (current == null || current == Object.class) {
            return;
        }

        // 1. 查找当前类声明的方法
        for (Method m : current.getDeclaredMethods()) {
            if (m.getParameterCount() == 0 &&
                    !Modifier.isStatic(m.getModifiers()) && // 排除静态方法
                    !m.isBridge() &&                        // 排除编译器生成的桥接方法，避免协变返回类型导致的重复
                    m.getName().equalsIgnoreCase("getParent") &&
                    Entity.class.isAssignableFrom(m.getReturnType())) {
                try {
                    // 使用 TRUSTED Lookup，无需 setAccessible
                    methods.add(ClassUtil.getLookup().findVirtual(
                            current,
                            m.getName(),
                            MethodType.methodType(m.getReturnType(), m.getParameterTypes())
                    ));
                } catch (Throwable t) {
                    LogUtil.errorf("failed to traverse method %s", m.getName());
                }
            }
        }

        // 2. 继续查找父类
        traverseAndFindMethods(current.getSuperclass(), methods);

        // 3. 查找实现的接口 (处理接口中的 default 方法或抽象契约)
        for (Class<?> iface : current.getInterfaces()) {
            traverseAndFindMethods(iface, methods);
        }
    }

    /**
     * 递归获取实体的最终 parent
     */
    public Entity getUltimateParent(Entity entity) {
        if (entity == null) return null;

        List<MethodHandle> methods = getParentMethods(entity.getClass());
        for (MethodHandle m : methods) {
            try {
                // 修复 Bug：直接 invoke(entity)，不要 bindTo(entity).invoke(entity)
                Object result = m.invoke(entity);
                if (result instanceof Entity parent) {
                    // 如果 parent 还有 getParent 方法，递归调用
                    // 注意：这里的递归逻辑会以 parent 的实际运行类型重新查找方法
                    return getUltimateParent(parent);
                }
            } catch (Throwable t) {
                LogUtil.errorf("failed to invoke MethodHandle %s", m.toString());
            }
        }
        return entity;
    }
}
