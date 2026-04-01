package coffeeshop.simulation.model;

public class SlowMotionProcessingTimeStrategy extends ScaledProcessingTimeStrategy {

    public SlowMotionProcessingTimeStrategy(ProcessingTimeStrategy baseStrategy, double speedMultiplier) {
        super(baseStrategy, toDurationScale(speedMultiplier));
    }

    private static double toDurationScale(double speedMultiplier) {
        if (speedMultiplier <= 0 || speedMultiplier >= 1.0) {
            throw new IllegalArgumentException(
                    "speedMultiplier must be greater than 0 and less than 1.0.");
        }
        return 1.0 / speedMultiplier;
    }
}
