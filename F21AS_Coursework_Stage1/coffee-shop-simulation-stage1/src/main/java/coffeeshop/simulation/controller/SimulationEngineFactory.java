package coffeeshop.simulation.controller;

import coffeeshop.simulation.model.SimulationModel;

public interface SimulationEngineFactory {
    SimulationEngine create(SimulationModel model);
}
