package com.traffic.io.dto;

import java.util.List;

public class SimulationOutputDto {
    public List<StepStatusDto> stepStatuses;

    public SimulationOutputDto(List<StepStatusDto> stepStatuses) {
        this.stepStatuses = stepStatuses;
    }
}
