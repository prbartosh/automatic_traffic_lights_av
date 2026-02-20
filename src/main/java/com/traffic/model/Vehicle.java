package com.traffic.model;

public record Vehicle(
        String vehicleID,
        Direction startRoad,
        Direction endRoad
) {}
