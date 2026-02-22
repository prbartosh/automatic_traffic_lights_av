package com.traffic;

import com.traffic.command.AddVehicleCommand;
import com.traffic.command.Command;
import com.traffic.command.StepCommand;
import com.traffic.controller.AdaptiveTrafficController;
import com.traffic.io.JsonCommandParser;
import com.traffic.io.JsonOutputWriter;
import com.traffic.simulation.Simulation;

import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: simulation.jar <input.json> <output.json>");
            System.exit(1);
        }

        Path inputPath = Path.of(args[0]);
        Path outputPath = Path.of(args[1]);
        // parse commands
        List<Command> commands = new JsonCommandParser().parse(inputPath);
        // run simulation
        Simulation simulation = new Simulation(new AdaptiveTrafficController());
        for (Command cmd : commands) {
            switch (cmd) {
                case AddVehicleCommand add -> simulation.addVehicle(
                        add.vehicleId(), add.startRoad(), add.endRoad());
                case StepCommand step -> simulation.step();
            }
        }
        // save output
        new JsonOutputWriter().write(simulation.getStepResults(), outputPath);
        System.out.println("Simulation complete. Output written to: " + outputPath);
    }
}
