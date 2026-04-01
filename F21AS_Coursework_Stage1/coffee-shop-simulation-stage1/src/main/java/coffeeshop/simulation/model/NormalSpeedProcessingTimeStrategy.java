package coffeeshop.simulation.model;

public class NormalSpeedProcessingTimeStrategy extends ScaledProcessingTimeStrategy {

    public NormalSpeedProcessingTimeStrategy(ProcessingTimeStrategy baseStrategy) {
        super(baseStrategy, 1.0);
    }
}
