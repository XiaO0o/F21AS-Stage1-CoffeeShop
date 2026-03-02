package coffeeshop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrderStatisticsTest {

    @Test
    void recordOrderShouldAccumulateCountsByQuantity() throws InvalidMenuItemDataException {
        MenuItem drink = new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK);
        MenuItem snack = new MenuItem("SNACK-001", "Cookie", 1.50, Category.SNACK);
        Order order = new Order("CUST-001", LocalDateTime.parse("2026-02-01T10:15:30"));
        order.addLine(drink, 2, true);
        order.addLine(snack, 3, false);
        order.addLine(drink, 1, false);

        OrderStatistics statistics = new OrderStatistics();
        statistics.recordOrder(order);

        assertEquals(3, statistics.getCount(drink));
        assertEquals(3, statistics.getCount(snack));
    }

    @Test
    void getCountShouldReturnZeroForUnknownItem() throws InvalidMenuItemDataException {
        MenuItem drink = new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK);
        MenuItem food = new MenuItem("FOOD-001", "Sandwich", 4.20, Category.FOOD);
        Order order = new Order("CUST-001", LocalDateTime.parse("2026-02-01T10:15:30"));
        order.addLine(drink, 1, false);

        OrderStatistics statistics = new OrderStatistics();
        statistics.recordOrder(order);

        assertEquals(0, statistics.getCount(food));
    }

    @Test
    void recordOrderShouldRejectNullOrder() {
        OrderStatistics statistics = new OrderStatistics();
        assertThrows(IllegalArgumentException.class, () -> statistics.recordOrder(null));
    }

    @Test
    void getCountShouldRejectNullItem() {
        OrderStatistics statistics = new OrderStatistics();
        assertThrows(IllegalArgumentException.class, () -> statistics.getCount(null));
    }
}
