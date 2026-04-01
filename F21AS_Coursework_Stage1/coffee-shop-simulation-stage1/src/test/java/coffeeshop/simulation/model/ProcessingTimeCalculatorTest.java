package coffeeshop.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import coffeeshop.model.Category;
import coffeeshop.model.InvalidMenuItemDataException;
import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import java.time.LocalDateTime;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ProcessingTimeCalculatorTest {

    @Test
    void shouldAccumulateMinProcessingTimeByQuantity() throws InvalidMenuItemDataException {
        ProcessingTimeCalculator calculator = new ProcessingTimeCalculator(new AlwaysMinRandom());
        Order order = buildMixedOrder();

        long expected =
                2L * ProcessingTimeCalculator.DRINK_MIN_MS
                        + 1L * ProcessingTimeCalculator.FOOD_MIN_MS
                        + 3L * ProcessingTimeCalculator.SNACK_MIN_MS;

        assertEquals(expected, calculator.calculateProcessingTimeMillis(order));
    }

    @Test
    void shouldAccumulateMaxProcessingTimeByQuantity() throws InvalidMenuItemDataException {
        ProcessingTimeCalculator calculator = new ProcessingTimeCalculator(new AlwaysMaxRandom());
        Order order = buildMixedOrder();

        long expected =
                2L * ProcessingTimeCalculator.DRINK_MAX_MS
                        + 1L * ProcessingTimeCalculator.FOOD_MAX_MS
                        + 3L * ProcessingTimeCalculator.SNACK_MAX_MS;

        assertEquals(expected, calculator.calculateProcessingTimeMillis(order));
    }

    private static Order buildMixedOrder() throws InvalidMenuItemDataException {
        Order order = new Order("ORD-TIME-001", LocalDateTime.parse("2026-03-02T12:00:00"));
        order.addLine(new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK), 2, true);
        order.addLine(new MenuItem("FOOD-001", "Sandwich", 6.50, Category.FOOD), 1, false);
        order.addLine(new MenuItem("SNACK-001", "Cookie", 1.20, Category.SNACK), 3, false);
        return order;
    }

    private static final class AlwaysMinRandom extends Random {
        @Override
        public int nextInt(int bound) {
            return 0;
        }
    }

    private static final class AlwaysMaxRandom extends Random {
        @Override
        public int nextInt(int bound) {
            return bound - 1;
        }
    }
}
