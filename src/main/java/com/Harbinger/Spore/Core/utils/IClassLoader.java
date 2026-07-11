package com.Harbinger.Spore.Core.utils;

import org.objectweb.asm.tree.ClassNode;

public interface IClassLoader {
    Class<?> tryAvoidHiddenClass(Class<?> clazz);

    Class<?> getOriginalClass(Class<?> clazz);

    Class<?> deffineneClazz(ClassNode node, Class<?> initialClass);
    Class<?> creeateveWrapperHidden(Class<?> callback);
    Class<?> deffineneHiddenClazz(ClassNode node, Class<?> initialClass);
}
