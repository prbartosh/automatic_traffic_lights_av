package com.traffic.model;

public record Vehicle(
        String vehicleId,
        Direction startRoad,
        Direction endRoad
) {}
