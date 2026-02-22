package com.traffic.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionTest {

    private Intersection intersection;

    @BeforeEach
    void setUp() {
        intersection = new Intersection();
    }

    // init values check
    @Test
    void allFourRoadsExist() {
        for (Direction dir : Direction.values()) {
            assertNotNull(intersection.getRoad(dir), "Road missing for direction: " + dir);
        }
    }

    @Test
    void allRoadsStartWithRedLight() {
        for (Direction dir : Direction.values()) {
            assertEquals(LightState.RED, intersection.getRoad(dir).getLightState(),
                    "Road " + dir + " should start RED");
        }
    }

    @Test
    void allRoadsStartWithEmptyQueues() {
        intersection.getQueueSizes().values()
                .forEach(size -> assertEquals(0, size));
    }

    // getting road
    @Test
    void getRoadReturnsSameRoadForSameDirection() {
        Road first = intersection.getRoad(Direction.NORTH);
        Road second = intersection.getRoad(Direction.NORTH);
        assertSame(first, second);
    }

    @Test
    void getRoadReturnsCorrectDirection() {
        for (Direction dir : Direction.values()) {
            assertEquals(dir, intersection.getRoad(dir).getDirection());
        }
    }

    // adding a vehicle
    @Test
    void addVehicleIncreasesQueueOnStartRoad() {
        intersection.addVehicle("v1", Direction.SOUTH, Direction.NORTH);
        assertEquals(1, intersection.getRoad(Direction.SOUTH).getQueueSize());
    }

    @Test
    void addVehicleDoesNotAffectOtherRoads() {
        intersection.addVehicle("v1", Direction.SOUTH, Direction.NORTH);

        assertEquals(0, intersection.getRoad(Direction.NORTH).getQueueSize());
        assertEquals(0, intersection.getRoad(Direction.EAST).getQueueSize());
        assertEquals(0, intersection.getRoad(Direction.WEST).getQueueSize());
    }

    @Test
    void addMultipleVehiclesToSameRoad() {
        intersection.addVehicle("v1", Direction.EAST, Direction.WEST);
        intersection.addVehicle("v2", Direction.EAST, Direction.WEST);
        assertEquals(2, intersection.getRoad(Direction.EAST).getQueueSize());
    }

    @Test
    void addVehiclesToDifferentRoads() {
        intersection.addVehicle("v1", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicle("v2", Direction.EAST, Direction.WEST);

        assertEquals(1, intersection.getRoad(Direction.NORTH).getQueueSize());
        assertEquals(1, intersection.getRoad(Direction.EAST).getQueueSize());
    }

    // check conflicts on green
    @Test
    void noConflictWhenAllRoadsRed() {
        assertFalse(intersection.hasConflictingGreens());
    }

    @Test
    void noConflictWhenOnlyNorthGreen() {
        intersection.getRoad(Direction.NORTH).setLightState(LightState.GREEN);
        assertFalse(intersection.hasConflictingGreens());
    }

    @Test
    void noConflictWhenNorthAndSouthGreen() {
        intersection.getRoad(Direction.NORTH).setLightState(LightState.GREEN);
        intersection.getRoad(Direction.SOUTH).setLightState(LightState.GREEN);
        assertFalse(intersection.hasConflictingGreens());
    }

    @Test
    void noConflictWhenEastAndWestGreen() {
        intersection.getRoad(Direction.EAST).setLightState(LightState.GREEN);
        intersection.getRoad(Direction.WEST).setLightState(LightState.GREEN);
        assertFalse(intersection.hasConflictingGreens());
    }

    @Test
    void conflictWhenNorthAndEastGreen() {
        intersection.getRoad(Direction.NORTH).setLightState(LightState.GREEN);
        intersection.getRoad(Direction.EAST).setLightState(LightState.GREEN);
        assertTrue(intersection.hasConflictingGreens());
    }

    @Test
    void conflictWhenNorthAndWestGreen() {
        intersection.getRoad(Direction.NORTH).setLightState(LightState.GREEN);
        intersection.getRoad(Direction.WEST).setLightState(LightState.GREEN);
        assertTrue(intersection.hasConflictingGreens());
    }

    @Test
    void conflictWhenSouthAndEastGreen() {
        intersection.getRoad(Direction.SOUTH).setLightState(LightState.GREEN);
        intersection.getRoad(Direction.EAST).setLightState(LightState.GREEN);
        assertTrue(intersection.hasConflictingGreens());
    }

    @Test
    void conflictWhenAllFourGreen() {
        for (Direction dir : Direction.values()) {
            intersection.getRoad(dir).setLightState(LightState.GREEN);
        }
        assertTrue(intersection.hasConflictingGreens());
    }

    @Test
    void noConflictWhenNorthYellowAndEastGreen() {
        // YELLOW nie jest GREEN — nie powinno liczyć się jako konflikt
        intersection.getRoad(Direction.NORTH).setLightState(LightState.YELLOW);
        intersection.getRoad(Direction.EAST).setLightState(LightState.GREEN);
        assertFalse(intersection.hasConflictingGreens());
    }

    // getting queue sizes
    @Test
    void getQueueSizesReturnsAllFourDirections() {
        Map<Direction, Integer> sizes = intersection.getQueueSizes();
        assertEquals(4, sizes.size());
        for (Direction dir : Direction.values()) {
            assertTrue(sizes.containsKey(dir));
        }
    }

    @Test
    void getQueueSizesReflectsAddedVehicles() {
        intersection.addVehicle("v1", Direction.WEST, Direction.EAST);
        intersection.addVehicle("v2", Direction.WEST, Direction.EAST);
        intersection.addVehicle("v3", Direction.NORTH, Direction.SOUTH);

        Map<Direction, Integer> sizes = intersection.getQueueSizes();
        assertEquals(2, sizes.get(Direction.WEST));
        assertEquals(1, sizes.get(Direction.NORTH));
        assertEquals(0, sizes.get(Direction.EAST));
        assertEquals(0, sizes.get(Direction.SOUTH));
    }

    @Test
    void applyNsGreenSetsCorrectLights() {
        intersection.applyPhase(Phase.NS_GREEN);
        assertEquals(LightState.GREEN, intersection.getRoad(Direction.NORTH).getLightState());
        assertEquals(LightState.GREEN, intersection.getRoad(Direction.SOUTH).getLightState());
        assertEquals(LightState.RED,   intersection.getRoad(Direction.EAST).getLightState());
        assertEquals(LightState.RED,   intersection.getRoad(Direction.WEST).getLightState());
    }

    @Test
    void applyNsYellowSetsAllYellow() {
        intersection.applyPhase(Phase.NS_YELLOW);
        for (Direction dir : Direction.values()) {
            assertEquals(LightState.YELLOW, intersection.getRoad(dir).getLightState());
        }
    }
}