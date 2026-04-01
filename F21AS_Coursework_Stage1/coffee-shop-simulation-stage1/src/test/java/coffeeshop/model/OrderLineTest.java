package coffeeshop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class OrderLineTest {

    @Test
    void getSubtotalShouldBeCorrectWhenQuantityIsTwo() throws InvalidMenuItemDataException {
        MenuItem item = new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK);
        OrderLine orderLine = new OrderLine(item, 2, true);

        assertEquals(5.60, orderLine.getSubtotal(), 1e-9);
    }

    @Test
    void constructorShouldThrowWhenQuantityIsLessThanOne() throws InvalidMenuItemDataException {
        MenuItem item = new MenuItem("FOOD-001", "Croissant", 3.20, Category.FOOD);

        assertThrows(IllegalArgumentException.class, () -> new OrderLine(item, 0, false));
    }
}
