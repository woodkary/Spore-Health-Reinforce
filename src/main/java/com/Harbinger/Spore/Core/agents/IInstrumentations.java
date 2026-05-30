package com.Harbinger.Spore.Core.agents;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.UnmodifiableClassException;

public interface IInstrumentations {
    IInstrumentations addTransformer(ClassFileTransformer transformer);
    Class<?>[] getAllLoadedClasses();
    boolean isModifiableClass(Class<?> clazz);
    boolean isRetransformClassesSupported();
    IInstrumentations retransformClasses(Class<?>[] classes) throws UnmodifiableClassException;
}
