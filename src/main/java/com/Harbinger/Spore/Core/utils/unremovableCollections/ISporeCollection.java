package com.Harbinger.Spore.Core.utils.unremovableCollections;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Predicate;

public interface ISporeCollection<T> extends Collection<T> {
    boolean actualAdd(T t);
    boolean actualAddAll(@NotNull Collection<? extends T> c);
    boolean actualRemove(Object o);
    boolean actualRemoveAll(@NotNull Collection<?> c);
    boolean actualRetainAll(@NotNull Collection<?> c);
    boolean actualRemoveIf(@NotNull Predicate<? super T> filter);
    void actualClear();
}
