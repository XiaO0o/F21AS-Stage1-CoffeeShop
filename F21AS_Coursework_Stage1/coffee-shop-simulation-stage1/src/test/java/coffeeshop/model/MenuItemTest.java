package coffeeshop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MenuItemTest {

    @Test
    void shouldCreateValidMenuItem() throws InvalidMenuItemDataException {
        MenuItem menuItem = new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK);

        assertEquals(2.80, menuItem.getPrice());
        assertEquals(Category.DRINK, menuItem.getCategory());
    }

    @Test
    void shouldThrowForInvalidIdPattern() {
        assertThrows(
                InvalidMenuItemDataException.class,
                () -> new MenuItem("DRINK001", "Americano", 2.80, Category.DRINK));
    }

    @Test
    void shouldThrowForInvalidPrice() {
        assertThrows(
                InvalidMenuItemDataException.class,
                () -> new MenuItem("FOOD-001", "Croissant", 0.0, Category.FOOD));
    }

    @Test
    void shouldThrowForInvalidName() {
        assertThrows(
                InvalidMenuItemDataException.class,
                () -> new MenuItem("SNACK-001", "   ", 1.50, Category.SNACK));
    }
}
