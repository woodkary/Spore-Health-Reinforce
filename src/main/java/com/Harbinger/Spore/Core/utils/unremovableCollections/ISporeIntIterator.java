package com.Harbinger.Spore.Core.utils.unremovableCollections;

import it.unimi.dsi.fastutil.ints.IntIterator;

public interface ISporeIntIterator extends IntIterator, ISporeIterator<Integer> {
    @Override
    void actualRemove();
}
