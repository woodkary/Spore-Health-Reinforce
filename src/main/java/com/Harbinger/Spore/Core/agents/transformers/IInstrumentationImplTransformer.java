package com.Harbinger.Spore.Core.agents.transformers;

import java.lang.instrument.ClassFileTransformer;

public interface IInstrumentationImplTransformer extends ClassFileTransformer,SelfTransformer {
    void inspectInstrumentationImpl();
}
