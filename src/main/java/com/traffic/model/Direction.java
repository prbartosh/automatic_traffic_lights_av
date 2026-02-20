package com.traffic.model;

public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public static Direction fromString(String value) {
        return valueOf(value.toUpperCase());
    }
}
