package com.traffic.model;

import java.util.Set;

public enum Phase {
    NS_GREEN,
    NS_YELLOW,
    EW_GREEN,
    EW_YELLOW;


    public Set<Direction> greenDirections() {
        return switch (this) {
            case NS_GREEN  -> Set.of(Direction.NORTH, Direction.SOUTH);
            case EW_GREEN  -> Set.of(Direction.EAST, Direction.WEST);
            case NS_YELLOW -> Set.of();  // YELLOW = nikt nie jedzie
            case EW_YELLOW -> Set.of();
        };
    }


    public Phase next() {
        return switch (this) {
            case NS_GREEN  -> NS_YELLOW;
            case NS_YELLOW -> EW_GREEN;
            case EW_GREEN  -> EW_YELLOW;
            case EW_YELLOW -> NS_GREEN;
        };
    }

    public boolean isYellow() {
        return this == NS_YELLOW || this == EW_YELLOW;
    }
}
