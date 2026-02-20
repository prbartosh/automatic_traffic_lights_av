package com.traffic.model;

import java.security.PublicKey;

public enum LightState {
    RED, YELLOW, GREEN;

    public boolean allowsPassage() {
        return this == GREEN;
    }
}
