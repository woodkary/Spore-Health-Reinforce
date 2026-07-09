package com.Harbinger.Spore.Core.agents;

import com.Harbinger.Spore.Core.agents.transformers.SelfTransformer;

public interface IJVNTIPointer {
    IJVNTIPointer addTransformer(SelfTransformer transformer);
    Class<?>[] getAllLoadedClasses();
    boolean isModifiableClass(Class<?> clazz);
    boolean isRetransformClassesSupported();
    IJVNTIPointer retransformClasses(Class<?>[] classes);

    IJVNTIPointer allocate();

    IJVNTIPointer deallocate();

    IJVNTIPointer addCapabilities();

    IJVNTIPointer setEventCallbacks();

    IJVNTIPointer setEventNotificationMode();
}
