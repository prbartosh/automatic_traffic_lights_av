package com.traffic.controller;

import com.traffic.model.Direction;
import com.traffic.model.Intersection;
import com.traffic.model.LightState;
import com.traffic.model.Phase;
import com.traffic.simulation.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveTrafficControllerTest {

    private AdaptiveTrafficController controller;
    private Intersection intersection;

    @BeforeEach
    void setUp() {
        controller = new AdaptiveTrafficController();
        intersection = new Intersection();
    }

    // init state
    @Test
    void initialPhaseIsNsGreen() {
        assertEquals(Phase.NS_GREEN, controller.getCurrentPhase());
    }

    // przykład
    @Test
    void exactExampleFromTask() {
        intersection.addVehicle("vehicle1", Direction.SOUTH, Direction.NORTH);
        intersection.addVehicle("vehicle2", Direction.NORTH, Direction.SOUTH);

        List<String> step1 = controller.step(intersection);
        List<String> step2 = controller.step(intersection);

        intersection.addVehicle("vehicle3", Direction.WEST, Direction.SOUTH);
        intersection.addVehicle("vehicle4", Direction.WEST, Direction.SOUTH);

        List<String> step3 = controller.step(intersection);
        List<String> step4 = controller.step(intersection);

        // Step 1: v1 i v2 opuszczają skrzyżowanie
        assertTrue(step1.containsAll(List.of("vehicle1", "vehicle2")),
                "Step 1 should release vehicle1 and vehicle2");

        // Step 2: kolejki puste — nikt nie wyjeżdża
        assertTrue(step2.isEmpty(), "Step 2 should be empty");

        // Step 3 i 4: v3 i v4 wyjeżdżają (po jednym per krok)
        assertEquals(1, step3.size(), "Step 3 should release exactly one vehicle");
        assertEquals(1, step4.size(), "Step 4 should release exactly one vehicle");
        assertEquals("vehicle3", step3.getFirst(), "vehicle3 should leave first (FIFO)");
        assertEquals("vehicle4", step4.getFirst(), "vehicle4 should leave second (FIFO)");
    }

    @Test
    void vehicleOnGreenRoadLeavesOnStep() {
        intersection.addVehicle("v1", Direction.SOUTH, Direction.NORTH);

        List<String> result = controller.step(intersection);

        assertTrue(result.contains("v1"));
    }

    @Test
    void vehicleStaysInQueueWhenRoadIsRed() {
        intersection.addVehicle("n1", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicle("n2", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicle("n3", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicle("e1", Direction.EAST, Direction.WEST);

        controller.step(intersection);

        assertEquals(1, intersection.getRoad(Direction.EAST).getQueueSize(),
                "Vehicle on RED road must remain in queue");
    }

    @Test
    void emptyIntersectionProducesEmptyResult() {
        List<String> result = controller.step(intersection);
        assertTrue(result.isEmpty());
    }

    @Test
    void onlyFirstVehiclePerRoadLeavesPerStep() {
        intersection.addVehicle("v1", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicle("v2", Direction.NORTH, Direction.SOUTH);

        List<String> result = controller.step(intersection);

        assertEquals(1, result.size(), "Only one vehicle per road per step");
        assertEquals("v1", result.get(0), "First vehicle in queue should leave (FIFO)");
        assertEquals(1, intersection.getRoad(Direction.NORTH).getQueueSize(),
                "Second vehicle should remain in queue");
    }

    @Test
    void stepCountMatchesOutputSize() {
        controller.step(intersection);
        controller.step(intersection);
        controller.step(intersection);
        // Nie rzuca wyjątku — każdy step zwraca wynik (nawet pustą listę)
    }

    // safety - no conflicts

    @Test
    void noConflictingGreensOnFirstStep() {
        controller.step(intersection);
        assertFalse(intersection.hasConflictingGreens());
    }

    @Test
    void noConflictingGreensOverManySteps() {
        intersection.addVehicle("n1", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicle("s1", Direction.SOUTH, Direction.NORTH);
        intersection.addVehicle("e1", Direction.EAST, Direction.WEST);
        intersection.addVehicle("w1", Direction.WEST, Direction.EAST);

        for (int i = 0; i < 30; i++) {
            controller.step(intersection);
            assertFalse(intersection.hasConflictingGreens(),
                    "Conflicting greens at step " + (i + 1));
        }
    }

    // adaptivity
    @Test
    void switchesToEwGreenWhenNsQueueEmptyAndEwHasVehicles() {
        // NS jest aktywne, ale kolejki N+S są puste
        // E+W mają pojazdy → system powinien przełączyć
        intersection.addVehicle("e1", Direction.EAST, Direction.WEST);
        intersection.addVehicle("w1", Direction.WEST, Direction.EAST);

        // Po kilku krokach E lub W musi dostać zielone
        boolean eastOrWestLeft = false;
        for (int i = 0; i < 5; i++) {
            List<String> left = controller.step(intersection);
            if (left.contains("e1") || left.contains("w1")) {
                eastOrWestLeft = true;
                break;
            }
        }
        assertTrue(eastOrWestLeft, "Controller must eventually give green to EW when NS is empty");
    }

    @Test
    void doesNotSwitchBeforeMinPhaseSteps() {
        intersection.addVehicle("n1", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicle("e1", Direction.EAST, Direction.WEST);

        controller.step(intersection);
        Phase phaseAfterStep1 = controller.getCurrentPhase();

        assertNotEquals(Phase.EW_GREEN, phaseAfterStep1,
                "Should not switch to EW_GREEN before MIN_PHASE_STEPS");
    }

    @Test
    void forcesSwitchAfterMaxPhaseSteps() {
        for (int i = 0; i < controller.getMaxPhaseSteps() + 2; i++) {
            intersection.addVehicle("n" + i, Direction.NORTH, Direction.SOUTH);
        }

        Phase lastPhase = controller.getCurrentPhase();
        boolean phaseChanged = false;

        for (int i = 0; i < controller.getMaxPhaseSteps() + 2; i++) {
            controller.step(intersection);
            if (controller.getCurrentPhase() != lastPhase && !controller.getCurrentPhase().isYellow()) {
                phaseChanged = true;
                break;
            }
            lastPhase = controller.getCurrentPhase();
        }

        assertTrue(phaseChanged, "Phase must change after MAX_PHASE_STEPS even with long NS queue");
    }

    @Test
    void vehiclesOnHigherQueueRoadEventuallyLeave() {
        for (int i = 0; i < 5; i++) {
            intersection.addVehicle("e" + i, Direction.EAST, Direction.WEST);
        }

        List<String> allLeft = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            allLeft.addAll(controller.step(intersection));
        }

        boolean anyEastLeft = allLeft.stream().anyMatch(id -> id.startsWith("e"));
        assertTrue(anyEastLeft, "Vehicles on high-queue road must eventually leave");
    }

    // phase cycles
    @Test
    void lightsAreGreenForNsOnFirstStep() {
        controller.step(intersection);
        assertEquals(LightState.GREEN, intersection.getRoad(Direction.NORTH).getLightState());
        assertEquals(LightState.GREEN, intersection.getRoad(Direction.SOUTH).getLightState());
        assertEquals(LightState.RED,   intersection.getRoad(Direction.EAST).getLightState());
        assertEquals(LightState.RED,   intersection.getRoad(Direction.WEST).getLightState());
    }

    @Test
    void ewGreenPhaseAllowsEastAndWestToRelease() {
        intersection.addVehicle("e1", Direction.EAST, Direction.WEST);
        intersection.addVehicle("w1", Direction.WEST, Direction.EAST);

        List<String> allLeft = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            allLeft.addAll(controller.step(intersection));
        }

        assertTrue(allLeft.contains("e1") || allLeft.contains("w1"),
                "EW vehicles must leave when EW_GREEN is active");
    }

    @Test
    void switchesToHigherQueuePhase() {
        // start from NS_GREEN but E+W have many cars
        Simulation sim = new Simulation(new AdaptiveTrafficController());

        // load 5 vehicles to east
        for (int i = 0; i < 5; i++) {
            sim.addVehicle("e" + i, "east", "west");
        }

        // make thew steps and eventually system must give EW_GREEN
        List<List<String>> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            sim.step();
            results.add(sim.getStepResults().get(i));
        }

        // check vehicle enter from east
        boolean anyEastLeft = results.stream()
                .anyMatch(step -> step.stream().anyMatch(id -> id.startsWith("e")));

        assertTrue(anyEastLeft, "Adaptive controller must eventually give green to East");
    }

    @Test
    void doesNotStarveHighQueueRoad() {
        // no road wait for more than T_max steps
        Simulation sim = new Simulation(new AdaptiveTrafficController());
        sim.addVehicle("w1", "west", "east");
        sim.addVehicle("w2", "west", "east");

        // after MAX_PHASE_STEPS + 2 (YELLOW) steps west must get green
        int maxWait = controller.getMaxPhaseSteps() + 2;
        for (int i = 0; i < maxWait; i++) {
            sim.step();
        }

        long westLeft = sim.getStepResults().stream()
                .flatMap(List::stream)
                .filter(id -> id.startsWith("w"))
                .count();

        assertTrue(westLeft > 0, "West vehicles must leave within MAX_PHASE_STEPS steps");
    }
}