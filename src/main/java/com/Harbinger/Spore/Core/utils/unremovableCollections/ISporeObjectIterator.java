package com.Harbinger.Spore.Core.utils.unremovableCollections;

import it.unimi.dsi.fastutil.objects.ObjectIterator;

public interface ISporeObjectIterator<T> extends ObjectIterator<T>, ISporeIterator<T> {
    @Override
    void actualRemove();
}
