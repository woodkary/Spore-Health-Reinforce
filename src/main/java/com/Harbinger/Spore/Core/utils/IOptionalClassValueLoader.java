package com.Harbinger.Spore.Core.utils;

import java.util.Optional;

interface IOptionalClassValueLoader {
    Optional<Class<?>> loadClassValue(Class<?> type);
}
