package com.traffic.simulation;

import com.traffic.controller.TrafficController;
import com.traffic.model.Direction;
import com.traffic.model.Intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Simulation {
    private final Intersection intersection;
    private final TrafficController controller;
    private final List<List<String>> stepResults;

    public Simulation(TrafficController controller) {
        this.intersection = new Intersection();
        this.controller = controller;
        this.stepResults = new ArrayList<>();
    }

    public void addVehicle(String vehicleId, String startRoad, String endRoad) {
        intersection.addVehicle(
                vehicleId,
                Direction.fromString(startRoad),
                Direction.fromString(endRoad)
        );
    }

    public void step() {
        List<String> leftVehicles = controller.step(intersection);
        stepResults.add(leftVehicles);
    }

    public List<List<String>> getStepResults() {
        return Collections.unmodifiableList(this.stepResults);
    }

    public Intersection getIntersection() {
        return this.intersection;
    }
}
