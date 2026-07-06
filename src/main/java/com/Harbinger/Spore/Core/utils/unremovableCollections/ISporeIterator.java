package com.Harbinger.Spore.Core.utils.unremovableCollections;

import java.util.Iterator;

public interface ISporeIterator<T> extends Iterator<T> {
    void actualRemove();
}
