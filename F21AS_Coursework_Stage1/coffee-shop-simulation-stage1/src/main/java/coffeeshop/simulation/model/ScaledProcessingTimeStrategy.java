package coffeeshop.simulation.model;

import coffeeshop.model.Order;

abstract class ScaledProcessingTimeStrategy implements ProcessingTimeStrategy {

    private final ProcessingTimeStrategy baseStrategy;
    private final double durationScale;

    ScaledProcessingTimeStrategy(ProcessingTimeStrategy baseStrategy, double durationScale) {
        if (baseStrategy == null) {
            throw new IllegalArgumentException("baseStrategy must not be null.");
        }
        if (durationScale <= 0) {
            throw new IllegalArgumentException("durationScale must be greater than 0.");
        }
        this.baseStrategy = baseStrategy;
        this.durationScale = durationScale;
    }

    @Override
    public long calculateProcessingTimeMillis(Order order) {
        long base = baseStrategy.calculateProcessingTimeMillis(order);
        if (base < 0) {
            throw new IllegalStateException("Base processing time must not be negative.");
        }
        return Math.max(1L, Math.round(base * durationScale));
    }
}
