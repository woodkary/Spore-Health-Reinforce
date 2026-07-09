package com.Harbinger.Spore.Core.utils;

import com.sun.jna.Structure;

import java.util.List;

public final class JvmtiCapabilities extends Structure {
    public int word0;
    public int word1;
    public int word2;
    public int word3;

    protected List<String> getFieldOrder() {
        return List.of("word0", "word1", "word2", "word3");
    }

    private JvmtiCapabilities setBit(int wordIndex, int bitIndex, boolean value) {
        int[] words = new int[]{this.word0, this.word1, this.word2, this.word3};
        if (value) {
            words[wordIndex] |= 1 << bitIndex;
        } else {
            words[wordIndex] &= ~(1 << bitIndex);
        }

        this.word0 = words[0];
        this.word1 = words[1];
        this.word2 = words[2];
        this.word3 = words[3];
        return this;
    }

    private boolean getBit(int wordIndex, int bitIndex) {
        int[] words = new int[]{this.word0, this.word1, this.word2, this.word3};
        return (words[wordIndex] & (1 << bitIndex)) != 0;
    }

    public JvmtiCapabilities setCanRedefineClasses(boolean value) {
        return this.setBit(0, 9, value);
    }

    public boolean canRedefineClasses() {
        return getBit(0, 9);
    }

    public JvmtiCapabilities setCanRedefineAnyClass(boolean value) {
        return this.setBit(0, 21, value);
    }

    public boolean canRedefineAnyClass() {
        return getBit(0, 21);
    }

    public JvmtiCapabilities setCanGenerateAllClassHookEvents(boolean value) {
        return this.setBit(0, 26, value);
    }

    public boolean canGenerateAllClassHookEvents() {
        return getBit(0, 26);
    }

    public JvmtiCapabilities setCanRetransformClasses(boolean value) {
        return this.setBit(1, 5, value);
    }

    public boolean canRetransformClasses() {
        return getBit(1, 5);
    }

    public JvmtiCapabilities setCanRetransformAnyClass(boolean value) {
        return this.setBit(1, 6, value);
    }

    public boolean canRetransformAnyClass() {
        return getBit(1, 6);
    }
}
