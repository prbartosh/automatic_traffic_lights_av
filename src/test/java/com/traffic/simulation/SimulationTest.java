package com.traffic.simulation;

import com.traffic.controller.AdaptiveTrafficController;
import com.traffic.controller.TrafficController;
import com.traffic.model.Intersection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationTest {

    @Mock
    private TrafficController mockController;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(mockController);
    }

    @Test
    void testInitialStateHasEmptyStepResults() {
        assertTrue(simulation.getStepResults().isEmpty(), "Step results should be empty");
    }

    @Test
    void testAddVehicleDoesNotThrowException() {
        assertDoesNotThrow(() -> {
            simulation.addVehicle("v1", "NORTH", "SOUTH");
        });
    }

    @Test
    void stepStepRecordsControllerResults() {
        List<String> step1LeftVehicles = Arrays.asList("v1", "v2");
        when(mockController.step(any(Intersection.class))).thenReturn(step1LeftVehicles);

        // act
        simulation.step();

        List<List<String>> results = simulation.getStepResults();
        assertEquals(1, results.size(), "Should record exactly one step result");
        assertEquals(step1LeftVehicles, results.getFirst(), "The recorded list should match the controller's output");
        verify(mockController, times(1)).step(any(Intersection.class));
    }

    @Test
    void testMultipleStepsAccumulateResults() {
        List<String> step1LeftVehicles = List.of("v1");
        List<String> step2LeftVehicles = List.of("v2", "v3");
        List<String> step3LeftVehicles = Collections.emptyList();

        when(mockController.step(any(Intersection.class)))
                .thenReturn(step1LeftVehicles)
                .thenReturn(step2LeftVehicles)
                .thenReturn(step3LeftVehicles);

        simulation.step();
        simulation.step();
        simulation.step();

        List<List<String>> results = simulation.getStepResults();
        assertEquals(3, results.size());
        assertEquals(step1LeftVehicles, results.getFirst());
        assertEquals(step2LeftVehicles, results.get(1));
        assertEquals(step3LeftVehicles, results.get(2));
    }

    @Test
    void testGetStepResultsIsUnmodifiable() {
        when(mockController.step(any(Intersection.class))).thenReturn(Arrays.asList("v1"));
        simulation.step();

        List<List<String>> results = simulation.getStepResults();

        assertThrows(UnsupportedOperationException.class, () -> {
            results.add(Arrays.asList("v2"));
        }, "The returned list should be unmodifiable");
    }

    @Test
    void testStepPassesIntersectionToController() {
        when(mockController.step(any(Intersection.class))).thenReturn(Collections.emptyList());

        simulation.step();

        ArgumentCaptor<Intersection> intersectionCaptor = ArgumentCaptor.forClass(Intersection.class);
        verify(mockController).step(intersectionCaptor.capture());

        assertNotNull(intersectionCaptor.getValue(), "The simulation should pass a non-null Intersection to the controller");
    }

    @Test
    void neverHasConflictingGreensOverManySteps() {
        Simulation sim = new Simulation(new AdaptiveTrafficController());

        sim.addVehicle("n1", "north", "south");
        sim.addVehicle("s1", "south", "north");
        sim.addVehicle("e1", "east", "west");
        sim.addVehicle("w1", "west", "east");

        Intersection intersection = sim.getIntersection();

        for (int i = 0; i < 20; i++) {
            sim.step();
            assertFalse(
                    intersection.hasConflictingGreens(),
                    "Conflicting greens detected at step " + (i + 1)
            );
        }
    }

}

