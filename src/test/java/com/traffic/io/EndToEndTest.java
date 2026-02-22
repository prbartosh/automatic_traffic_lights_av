package com.traffic.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.Main;
import com.traffic.io.dto.SimulationOutputDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EndToEndTest {
    @Test
    void exactOutputFromTaskExample(@TempDir Path tempDir) throws Exception {
        // input
        String inputJson = """
            {
              "commands": [
                {"type": "addVehicle", "vehicleId": "vehicle1", "startRoad": "south", "endRoad": "north"},
                {"type": "addVehicle", "vehicleId": "vehicle2", "startRoad": "north", "endRoad": "south"},
                {"type": "step"},
                {"type": "step"},
                {"type": "addVehicle", "vehicleId": "vehicle3", "startRoad": "west", "endRoad": "south"},
                {"type": "addVehicle", "vehicleId": "vehicle4", "startRoad": "west", "endRoad": "south"},
                {"type": "step"},
                {"type": "step"}
              ]
            }
            """;

        Path inputFile = tempDir.resolve("input.json");
        Path outputFile = tempDir.resolve("output.json");
        Files.writeString(inputFile, inputJson);

        // starting main like from cmd
        Main.main(new String[]{inputFile.toString(), outputFile.toString()});

        // check output files exists and has stepStatuses
        String outputJson = Files.readString(outputFile);
        assertTrue(outputJson.contains("stepStatuses"), "Output must contain stepStatuses key");
        assertTrue(outputJson.contains("leftVehicles"), "Output must contain leftVehicles key");

        // check exact structure
        ObjectMapper mapper = new ObjectMapper();
        SimulationOutputDto output = mapper.readValue(outputFile.toFile(), SimulationOutputDto.class);
        assertEquals(4, output.stepStatuses.size(), "Must have exactly 4 step statuses");
    }
}
