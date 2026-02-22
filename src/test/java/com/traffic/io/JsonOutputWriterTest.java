package com.traffic.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonOutputWriterTest {

    private JsonOutputWriter writer;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        writer = new JsonOutputWriter();
        mapper = new ObjectMapper();
    }

    // test format
    @Test
    void outputContainsStepStatusesKey(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");

        writer.write(List.of(List.of()), output);

        JsonNode root = mapper.readTree(output.toFile());
        assertTrue(root.has("stepStatuses"),
                "Root must contain 'stepStatuses' key");
    }

    @Test
    void eachStepStatusContainsLeftVehiclesKey(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");

        writer.write(List.of(List.of("v1")), output);

        JsonNode root = mapper.readTree(output.toFile());
        JsonNode step = root.get("stepStatuses").get(0);
        assertTrue(step.has("leftVehicles"),
                "Each stepStatus must contain 'leftVehicles' key");
    }

    @Test
    void stepStatusesCountMatchesInputSize(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");
        List<List<String>> results = List.of(
                List.of("v1", "v2"),
                List.of(),
                List.of("v3")
        );

        writer.write(results, output);

        JsonNode root = mapper.readTree(output.toFile());
        assertEquals(3, root.get("stepStatuses").size(),
                "stepStatuses count must match number of steps");
    }

    // content of leftVehicles
    @Test
    void vehicleIdsAreWrittenCorrectly(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");

        writer.write(List.of(List.of("vehicle1", "vehicle2")), output);

        JsonNode leftVehicles = mapper.readTree(output.toFile())
                .get("stepStatuses").get(0)
                .get("leftVehicles");

        assertEquals(2, leftVehicles.size());
        assertEquals("vehicle1", leftVehicles.get(0).asText());
        assertEquals("vehicle2", leftVehicles.get(1).asText());
    }

    @Test
    void emptyStepProducesEmptyLeftVehiclesArray(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");

        writer.write(List.of(List.of()), output);

        JsonNode leftVehicles = mapper.readTree(output.toFile())
                .get("stepStatuses").get(0)
                .get("leftVehicles");

        assertTrue(leftVehicles.isArray());
        assertEquals(0, leftVehicles.size());
    }

    // example from example task
    @Test
    void exactOutputFromTask(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");
        List<List<String>> results = List.of(
                List.of("vehicle1", "vehicle2"),
                List.of(),
                List.of("vehicle3"),
                List.of("vehicle4")
        );

        writer.write(results, output);

        JsonNode root = mapper.readTree(output.toFile());
        JsonNode steps = root.get("stepStatuses");

        assertEquals(4, steps.size());
        assertEquals("vehicle1", steps.get(0).get("leftVehicles").get(0).asText());
        assertEquals("vehicle2", steps.get(0).get("leftVehicles").get(1).asText());
        assertEquals(0,          steps.get(1).get("leftVehicles").size());
        assertEquals("vehicle3", steps.get(2).get("leftVehicles").get(0).asText());
        assertEquals("vehicle4", steps.get(3).get("leftVehicles").get(0).asText());
    }

    // output file
    @Test
    void createsOutputFile(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");

        writer.write(List.of(), output);

        assertTrue(Files.exists(output), "Output file must be created");
    }

    @Test
    void outputIsValidJson(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");

        writer.write(List.of(List.of("v1"), List.of()), output);

        // Nie rzuca wyjątku = valid JSON
        assertDoesNotThrow(() -> mapper.readTree(output.toFile()));
    }

    @Test
    void writesEmptyStepsList(@TempDir Path tempDir) throws IOException {
        Path output = tempDir.resolve("output.json");

        writer.write(List.of(), output);

        JsonNode root = mapper.readTree(output.toFile());
        assertTrue(root.get("stepStatuses").isArray());
        assertEquals(0, root.get("stepStatuses").size());
    }
}