package coffeeshop.simulation.model;

public class FastForwardProcessingTimeStrategy extends ScaledProcessingTimeStrategy {

    public FastForwardProcessingTimeStrategy(ProcessingTimeStrategy baseStrategy, double speedMultiplier) {
        super(baseStrategy, toDurationScale(speedMultiplier));
    }

    private static double toDurationScale(double speedMultiplier) {
        if (speedMultiplier <= 1.0) {
            throw new IllegalArgumentException("speedMultiplier must be greater than 1.0.");
        }
        return 1.0 / speedMultiplier;
    }
}
