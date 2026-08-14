package com.Harbinger.Spore.Core.utils.unremovableCollections;

import it.unimi.dsi.fastutil.objects.ObjectCollection;

public interface ISporeObjectCollection<T> extends ObjectCollection<T>, ISporeCollection<T> {
    @Override
    ISporeObjectIterator<T> iterator();
}
