package com.traffic.command;

public sealed interface Command permits AddVehicleCommand, StepCommand {}
