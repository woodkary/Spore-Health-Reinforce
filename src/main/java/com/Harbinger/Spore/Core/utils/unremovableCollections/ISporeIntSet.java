package com.Harbinger.Spore.Core.utils.unremovableCollections;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntPredicate;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;

public interface ISporeIntSet extends IntSet, ISporeSet<Integer> {
    @Override
    ISporeIntIterator iterator();

    boolean actualAdd(int key);

    boolean actualRemove(int key);

    boolean actualRem(int key);

    boolean actualAddAll(@NotNull IntCollection collection);

    boolean actualRemoveAll(@NotNull IntCollection collection);

    boolean actualRetainAll(@NotNull IntCollection collection);

    boolean actualRemoveIf(@NotNull java.util.function.IntPredicate filter);

    boolean actualRemoveIf(@NotNull IntPredicate filter);
}
