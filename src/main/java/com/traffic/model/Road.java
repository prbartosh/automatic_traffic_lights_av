package com.traffic.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class Road {
    private final Direction direction;
    private final Deque<Vehicle> queue;
    private LightState lightState;
    private int totalWaited;

    public Road(Direction direction) {
        this.direction = direction;
        this.queue = new ArrayDeque<>();
        this.lightState = LightState.RED;
        this.totalWaited = 0;
    }

    public void addVehicle(Vehicle vehicle) {
        this.queue.addLast(vehicle);
        this.totalWaited++;
    }

    public Optional<Vehicle> releaseFirstVehicle() {
        if (this.lightState != LightState.GREEN || queue.isEmpty()) {
            return Optional.empty();
        }
        this.totalWaited -= 1;
        return Optional.of(queue.pollFirst());
    }

    public void setLightState(LightState state) {
        this.lightState = state;
    }

    public int getQueueSize() { return this.queue.size(); }
    public LightState getLightState() { return this.lightState; }
    public Direction getDirection() { return direction; }
}
