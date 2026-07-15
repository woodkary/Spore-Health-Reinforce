package com.Harbinger.Spore.Sitems;

public enum TumorType {
    REGULAR(0),
    SICKEN(1),
    CALCIFIED(2),
    FROZEN(3),
    BILE(4);

    private final int type;

    TumorType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
