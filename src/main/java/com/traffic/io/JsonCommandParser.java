package com.traffic.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.command.AddVehicleCommand;
import com.traffic.command.Command;
import com.traffic.command.StepCommand;
import com.traffic.io.dto.CommandDto;
import com.traffic.io.dto.SimulationInputDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JsonCommandParser {
    private final ObjectMapper mapper = new ObjectMapper();
    
    public List<Command> parse(Path inputFile) throws IOException {
        SimulationInputDto dto = mapper.readValue(inputFile.toFile(), SimulationInputDto.class);
        return dto.commands.stream()
                .map(this::toCommand)
                .toList();
    }
    
    private Command toCommand(CommandDto dto) {
        return switch (dto.type) {
            case "addVehicle" -> new AddVehicleCommand(dto.vehicleId, dto.startRoad, dto.endRoad);
            case "step" -> new StepCommand();
            default -> throw new IllegalArgumentException("Unknown command type: " + dto.type);
        };
    }
}
