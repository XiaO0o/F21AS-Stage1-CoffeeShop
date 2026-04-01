package coffeeshop.simulation.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public final class ProcessingTimeStrategyFactory {

    private ProcessingTimeStrategyFactory() {
    }

    public static Map<SimulationSpeedMode, ProcessingTimeStrategy> createDefaultStrategies(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("random must not be null.");
        }

        ProcessingTimeStrategy base = new ProcessingTimeCalculator(random);
        EnumMap<SimulationSpeedMode, ProcessingTimeStrategy> strategies =
                new EnumMap<>(SimulationSpeedMode.class);
        strategies.put(SimulationSpeedMode.HALF_X, new SlowMotionProcessingTimeStrategy(base, 0.5));
        strategies.put(SimulationSpeedMode.ONE_X, new NormalSpeedProcessingTimeStrategy(base));
        strategies.put(SimulationSpeedMode.TWO_X, new FastForwardProcessingTimeStrategy(base, 2.0));
        strategies.put(SimulationSpeedMode.FOUR_X, new FastForwardProcessingTimeStrategy(base, 4.0));
        return strategies;
    }
}
