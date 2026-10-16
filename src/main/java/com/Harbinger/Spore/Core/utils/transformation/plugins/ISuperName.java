package com.Harbinger.Spore.Core.utils.transformation.plugins;

public interface ISuperName {
    String get(String current);

    void put(String className, String superName);
}
