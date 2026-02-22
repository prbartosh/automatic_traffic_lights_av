package com.traffic.controller;

import com.traffic.model.Intersection;
import com.traffic.model.Phase;

import java.util.List;

public class AdaptiveTrafficController implements TrafficController {

    private static final int MIN_PHASE_STEPS = 2;
    private static final int MAX_PHASE_STEPS = 8;

    private Phase currentPhase = Phase.NS_GREEN;
    private int stepsInCurrentPhase = 0;

    @Override
    public List<String> step(Intersection intersection) {
        intersection.applyPhase(this.currentPhase);

        List<String> leftVehicles = intersection.releaseVehicles(this.currentPhase);

        this.stepsInCurrentPhase++;

        if (shouldSwitchPhase(intersection)) {
            this.currentPhase = currentPhase.next();
            this.stepsInCurrentPhase = 0;
        }
        return leftVehicles;
    }

    private Phase oppositePhase(Phase phase) {
        return switch (phase) {
            case NS_GREEN, NS_YELLOW -> Phase.EW_GREEN;
            case EW_GREEN, EW_YELLOW -> Phase.NS_GREEN;
        };
    }


    private boolean shouldSwitchPhase(Intersection intersection) {
        if (this.currentPhase.isYellow()) {
            return true;
        }
        if (this.stepsInCurrentPhase < MIN_PHASE_STEPS) {
            return false;
        }
        if (stepsInCurrentPhase >= MAX_PHASE_STEPS) {
            return true;
        }

        int currentScore = calculateScore(intersection, this.currentPhase);
        int alternativeScore = calculateScore(intersection, oppositePhase(this.currentPhase));
        return alternativeScore > currentScore * 1.5; // if alternative phase has more than 50% cars then switch phase
    }

    private int calculateScore(Intersection intersection, Phase phase) {
        return phase.greenDirections().stream()
                .mapToInt(dir -> intersection.getRoad(dir).getQueueSize())
                .sum();
    }


}
