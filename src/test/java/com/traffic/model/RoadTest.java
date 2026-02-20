package com.traffic.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoadTest {

    private Road road;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        road = new Road(Direction.NORTH);
        vehicle = new Vehicle("v1", Direction.SOUTH, Direction.NORTH);
    }

    // inital values test
    @Test
    void initialLightStateIsRed() {
        assertEquals(LightState.RED, road.getLightState());
    }

    @Test
    void initialQueueIsEmpty() {
        assertEquals(0, road.getQueueSize());
    }

    @Test
    void directionIsPreserved() {
        assertEquals(Direction.NORTH, road.getDirection());
    }

    // add vehicle
    @Test
    void addVehicleIncreasesQueueSize() {
        road.addVehicle(vehicle);
        assertEquals(1, road.getQueueSize());
    }

    @Test
    void addMultipleVehiclesAccumulate() {
        road.addVehicle(vehicle);
        road.addVehicle(new Vehicle("v2", Direction.SOUTH, Direction.NORTH));
        road.addVehicle(new Vehicle("v3", Direction.SOUTH, Direction.NORTH));
        assertEquals(3, road.getQueueSize());
    }

    // release vehicle on red
    @Test
    void releaseOnRedReturnsEmpty() {
        road.addVehicle(vehicle);
        // lightState domyślnie RED
        assertTrue(road.releaseFirstVehicle().isEmpty());
    }

    @Test
    void releaseOnRedDoesNotShrinkQueue() {
        road.addVehicle(vehicle);
        road.releaseFirstVehicle();
        assertEquals(1, road.getQueueSize());
    }

    // release vehicle on green
    @Test
    void releaseOnGreenReturnsFrontVehicle() {
        road.addVehicle(vehicle);
        road.setLightState(LightState.GREEN);

        Optional<Vehicle> released = road.releaseFirstVehicle();

        assertTrue(released.isPresent());
        assertEquals("v1", released.get().vehicleId());
    }

    @Test
    void releaseOnGreenDecreasesQueueSize() {
        road.addVehicle(vehicle);
        road.setLightState(LightState.GREEN);

        road.releaseFirstVehicle();

        assertEquals(0, road.getQueueSize());
    }

    @Test
    void releaseRespectsQueueOrder() {
        Vehicle first = new Vehicle("first", Direction.SOUTH, Direction.NORTH);
        Vehicle second = new Vehicle("second", Direction.SOUTH, Direction.NORTH);
        road.addVehicle(first);
        road.addVehicle(second);
        road.setLightState(LightState.GREEN);

        Optional<Vehicle> released = road.releaseFirstVehicle();

        assertEquals("first", released.get().vehicleId());
        assertEquals(1, road.getQueueSize());
    }

    @Test
    void releaseOnGreenWithEmptyQueueReturnsEmpty() {
        road.setLightState(LightState.GREEN);
        assertTrue(road.releaseFirstVehicle().isEmpty());
    }

    // release vehicle on yellow
    @Test
    void releaseOnYellowReturnsEmpty() {
        road.addVehicle(vehicle);
        road.setLightState(LightState.YELLOW);

        assertTrue(road.releaseFirstVehicle().isEmpty());
    }

    // setting LightState
    @Test
    void setLightStateChangesState() {
        road.setLightState(LightState.GREEN);
        assertEquals(LightState.GREEN, road.getLightState());
    }

    @Test
    void setLightStateCanCycleBackToRed() {
        road.setLightState(LightState.GREEN);
        road.setLightState(LightState.YELLOW);
        road.setLightState(LightState.RED);
        assertEquals(LightState.RED, road.getLightState());
    }
}