package com.traffic.command;

public record AddVehicleCommand(
        String vehicleId,
        String startRoad,
        String endRoad
) implements Command {}
