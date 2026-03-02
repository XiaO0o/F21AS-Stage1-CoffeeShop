package coffeeshop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class MenuTest {

    @Test
    void addItemAndFindByIdShouldWork() throws InvalidMenuItemDataException {
        Menu menu = new Menu();
        MenuItem item = new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK);

        menu.addItem(item);

        assertNotNull(menu.findById("DRINK-001"));
    }

    @Test
    void addItemShouldRejectNull() {
        Menu menu = new Menu();
        assertThrows(IllegalArgumentException.class, () -> menu.addItem(null));
    }

    @Test
    void addItemShouldRejectDuplicateId() throws InvalidMenuItemDataException {
        Menu menu = new Menu();
        menu.addItem(new MenuItem("SNACK-001", "Cookie", 1.20, Category.SNACK));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> menu.addItem(new MenuItem("SNACK-001", "Cracker", 1.50, Category.SNACK)));
        assertEquals("Duplicate id: 'SNACK-001'.", exception.getMessage());
    }

    @Test
    void listByCategoryShouldSortByNameThenId() throws InvalidMenuItemDataException {
        Menu menu = new Menu();
        menu.addItem(new MenuItem("DRINK-003", "Latte", 3.60, Category.DRINK));
        menu.addItem(new MenuItem("DRINK-002", "Americano", 2.90, Category.DRINK));
        menu.addItem(new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK));
        menu.addItem(new MenuItem("FOOD-001", "Croissant", 3.20, Category.FOOD));

        List<MenuItem> drinkItems = menu.listByCategory(Category.DRINK);

        assertEquals(3, drinkItems.size());
        assertEquals("DRINK-001", drinkItems.get(0).getId());
        assertEquals("DRINK-002", drinkItems.get(1).getId());
        assertEquals("DRINK-003", drinkItems.get(2).getId());
    }

    @Test
    void listByCategoryShouldRejectNullCategory() {
        Menu menu = new Menu();
        assertThrows(IllegalArgumentException.class, () -> menu.listByCategory(null));
    }
}
