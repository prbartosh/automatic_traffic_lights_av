package com.traffic.io;

import com.traffic.command.AddVehicleCommand;
import com.traffic.command.Command;
import com.traffic.command.StepCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonCommandParserTest {

    private JsonCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new JsonCommandParser();
    }

    // parsing vehicle

    @Test
    void parsesAddVehicleCommand() throws IOException {
        String json = """
                {
                  "commands": [
                    {"type": "addVehicle", "vehicleId": "v1", "startRoad": "south", "endRoad": "north"}
                  ]
                }
                """;

        List<Command> commands = parser.parseFromString(json);

        assertEquals(1, commands.size());
        assertInstanceOf(AddVehicleCommand.class, commands.get(0));
    }

    @Test
    void addVehicleCommandHasCorrectFields() throws IOException {
        String json = """
                {
                  "commands": [
                    {"type": "addVehicle", "vehicleId": "car42", "startRoad": "east", "endRoad": "west"}
                  ]
                }
                """;

        AddVehicleCommand cmd = (AddVehicleCommand) parser.parseFromString(json).get(0);

        assertEquals("car42", cmd.vehicleId());
        assertEquals("east",  cmd.startRoad());
        assertEquals("west",  cmd.endRoad());
    }

    // parsing step
    @Test
    void parsesStepCommand() throws IOException {
        String json = """
                {"commands": [{"type": "step"}]}
                """;

        List<Command> commands = parser.parseFromString(json);

        assertEquals(1, commands.size());
        assertInstanceOf(StepCommand.class, commands.get(0));
    }

    // order
    @Test
    void preservesCommandOrder() throws IOException {
        String json = """
                {
                  "commands": [
                    {"type": "addVehicle", "vehicleId": "v1", "startRoad": "south", "endRoad": "north"},
                    {"type": "step"},
                    {"type": "addVehicle", "vehicleId": "v2", "startRoad": "west", "endRoad": "east"},
                    {"type": "step"}
                  ]
                }
                """;

        List<Command> commands = parser.parseFromString(json);

        assertEquals(4, commands.size());
        assertInstanceOf(AddVehicleCommand.class, commands.get(0));
        assertInstanceOf(StepCommand.class,       commands.get(1));
        assertInstanceOf(AddVehicleCommand.class, commands.get(2));
        assertInstanceOf(StepCommand.class,       commands.get(3));
    }

    @Test
    void countsStepCommandsCorrectly() throws IOException {
        String json = """
                {
                  "commands": [
                    {"type": "addVehicle", "vehicleId": "v1", "startRoad": "south", "endRoad": "north"},
                    {"type": "addVehicle", "vehicleId": "v2", "startRoad": "north", "endRoad": "south"},
                    {"type": "step"},
                    {"type": "step"},
                    {"type": "addVehicle", "vehicleId": "v3", "startRoad": "west", "endRoad": "south"},
                    {"type": "addVehicle", "vehicleId": "v4", "startRoad": "west", "endRoad": "south"},
                    {"type": "step"},
                    {"type": "step"}
                  ]
                }
                """;

        List<Command> commands = parser.parseFromString(json);

        long stepCount = commands.stream().filter(c -> c instanceof StepCommand).count();
        long addCount  = commands.stream().filter(c -> c instanceof AddVehicleCommand).count();

        assertEquals(8, commands.size());
        assertEquals(4, stepCount);
        assertEquals(4, addCount);
    }

    // example from example task
    @Test
    void parsesExactExampleFromTask() throws IOException {
        String json = """
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

        List<Command> commands = parser.parseFromString(json);

        AddVehicleCommand first = (AddVehicleCommand) commands.get(0);
        assertEquals("vehicle1", first.vehicleId());
        assertEquals("south",    first.startRoad());
        assertEquals("north",    first.endRoad());
    }

    // edge case
    @Test
    void parsesEmptyCommandList() throws IOException {
        String json = """
                {"commands": []}
                """;

        List<Command> commands = parser.parseFromString(json);

        assertTrue(commands.isEmpty());
    }

    @Test
    void parsesFromFile(@TempDir Path tempDir) throws IOException {
        String json = """
                {
                  "commands": [
                    {"type": "step"}
                  ]
                }
                """;
        Path file = tempDir.resolve("input.json");
        Files.writeString(file, json);

        List<Command> commands = parser.parse(file);

        assertEquals(1, commands.size());
        assertInstanceOf(StepCommand.class, commands.get(0));
    }

    @Test
    void throwsOnUnknownCommandType() {
        String json = """
                {"commands": [{"type": "unknownCommand"}]}
                """;

        assertThrows(IllegalArgumentException.class, () -> parser.parseFromString(json));
    }
}