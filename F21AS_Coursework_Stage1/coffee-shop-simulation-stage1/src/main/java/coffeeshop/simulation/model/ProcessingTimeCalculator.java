package coffeeshop.simulation.model;

import coffeeshop.model.Category;
import coffeeshop.model.Order;
import coffeeshop.model.OrderLine;
import java.util.Random;

public class ProcessingTimeCalculator implements ProcessingTimeStrategy {

    public static final int DRINK_MIN_MS = 2_000;
    public static final int DRINK_MAX_MS = 4_000;
    public static final int FOOD_MIN_MS = 6_000;
    public static final int FOOD_MAX_MS = 10_000;
    // Stage 1 did not define snack prep time, so we use a reasonable middle range.
    public static final int SNACK_MIN_MS = 3_000;
    public static final int SNACK_MAX_MS = 5_000;

    private final Random random;

    public ProcessingTimeCalculator(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("random must not be null.");
        }
        this.random = random;
    }

    @Override
    public long calculateProcessingTimeMillis(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }

        long totalMs = 0L;
        for (OrderLine line : order.getLinesView()) {
            Category category = line.getItem().getCategory();
            for (int i = 0; i < line.getQuantity(); i++) {
                totalMs += randomDurationByCategory(category);
            }
        }
        return totalMs;
    }

    private int randomDurationByCategory(Category category) {
        return switch (category) {
            case DRINK -> randomInRange(DRINK_MIN_MS, DRINK_MAX_MS);
            case FOOD -> randomInRange(FOOD_MIN_MS, FOOD_MAX_MS);
            case SNACK -> randomInRange(SNACK_MIN_MS, SNACK_MAX_MS);
        };
    }

    private int randomInRange(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
