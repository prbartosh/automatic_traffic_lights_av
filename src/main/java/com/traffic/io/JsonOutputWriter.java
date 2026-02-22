package com.traffic.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.traffic.io.dto.SimulationOutputDto;
import com.traffic.io.dto.StepStatusDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JsonOutputWriter {

    private final ObjectMapper mapper;

    public JsonOutputWriter() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void write(List<List<String>> stepResults, Path outputPath) throws IOException {
        List<StepStatusDto> statuses = stepResults.stream()
                .map(StepStatusDto::new)
                .toList();

        SimulationOutputDto output = new SimulationOutputDto(statuses);
        mapper.writeValue(outputPath.toFile(), output);
    }
}