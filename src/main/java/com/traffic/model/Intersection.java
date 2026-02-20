package com.traffic.model;

import java.util.EnumMap;
import java.util.Map;

public class Intersection {
    private final Map<Direction, Road> roads;

    public Intersection() {
        roads = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.values()) {
            roads.put(dir, new Road(dir));
        }
    }

    public Road getRoad(Direction direction) {
        return roads.get(direction);
    }

    public void addVehicle(String vehicleId, Direction startRoad, Direction endRoad) {
        Vehicle vehicle = new Vehicle(vehicleId, startRoad, endRoad);
        roads.get(startRoad).addVehicle(vehicle);
    }

    public boolean hasConflictiongGreens() {
        boolean nsGreen = roads.get(Direction.NORTH).getLightState() == LightState.GREEN
                || roads.get(Direction.SOUTH).getLightState() == LightState.GREEN;
        boolean ewGreen = roads.get(Direction.EAST).getLightState() == LightState.GREEN
                || roads.get(Direction.WEST).getLightState() == LightState.GREEN;
        return nsGreen && ewGreen;
    }

    public void applyPhase(Phase phase) {
        for (Direction dir : Direction.values()) {
            if (phase.greenDirections().contains(dir)) {
                this.roads.get(dir).setLightState(LightState.GREEN);
            } else if (phase.isYellow()) {
                this.roads.get(dir).setLightState(LightState.YELLOW);
            } else {
                this.roads.get(dir).setLightState(LightState.RED);
            }
        }
    }

    public Map<Direction, Integer> getQueueSizes() {
        Map<Direction, Integer> sizes = new EnumMap<>(Direction.class);
        roads.forEach((dir, road) -> sizes.put(dir, road.getQueueSize()));
        return sizes;
    }
}
