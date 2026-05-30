package com.Harbinger.Spore.Core.agents;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.UnmodifiableClassException;

public interface IInstrumentations {
    IInstrumentations addTransformer(ClassFileTransformer transformer);
    Class<?>[] getAllLoadedClasses();
    IInstrumentations retransformClasses(Class<?>[] classes) throws UnmodifiableClassException;
}
