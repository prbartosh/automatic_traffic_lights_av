package com.traffic.io.dto;

import java.util.List;

public class StepStatusDto {
    public List<String> leftVehicles;

    public StepStatusDto(List<String> leftVehicles) {
        this.leftVehicles = leftVehicles;
    }
}
