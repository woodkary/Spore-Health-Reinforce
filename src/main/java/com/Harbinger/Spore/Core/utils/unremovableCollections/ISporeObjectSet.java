package com.Harbinger.Spore.Core.utils.unremovableCollections;

import it.unimi.dsi.fastutil.objects.ObjectSet;

public interface ISporeObjectSet<T> extends ObjectSet<T>, ISporeSet<T>, ISporeObjectCollection<T> {
    @Override
    ISporeObjectIterator<T> iterator();
}
