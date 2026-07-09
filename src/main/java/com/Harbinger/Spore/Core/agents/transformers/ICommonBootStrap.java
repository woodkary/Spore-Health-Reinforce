package com.Harbinger.Spore.Core.agents.transformers;

public interface ICommonBootStrap {
    void tryRetransformHiddenClasses(Class<?>... classes);

    SporeLivingEntityHealthTransformerBootstrap.KlassAndAccessFlags modifyClassesAccessFlags(Class<?> clazz);

    void resetToHidden(Class<?> clazz, SporeLivingEntityHealthTransformerBootstrap.KlassAndAccessFlags klassAndAccessFlags);

    void installAndRetransform();
}
