package coffeeshop.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import coffeeshop.model.Category;
import coffeeshop.model.InvalidMenuItemDataException;
import coffeeshop.model.Menu;
import coffeeshop.model.Order;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class CsvLoaderTest {

    @Test
    void loadMenuShouldSkipInvalidRowsAndKeepValidRows() throws IOException {
        Path tempCsv = Files.createTempFile("menu-loader", ".csv");
        Files.writeString(
                tempCsv,
                String.join(
                        System.lineSeparator(),
                        "id,name,price,category",
                        "DRINK-001,Americano,2.80,DRINK",
                        "FOOD-001,Croissant,-1.00,FOOD",
                        "SNACK-001,Cookie,1.50,SNACK",
                        "BAD001,InvalidId,1.00,DRINK",
                        "DRINK-001,Duplicate,3.00,DRINK"));

        try {
            Menu menu = CsvLoader.loadMenu(tempCsv);

            assertNotNull(menu.findById("DRINK-001"));
            assertNotNull(menu.findById("SNACK-001"));
            assertNull(menu.findById("FOOD-001"));
            assertEquals(1, menu.listByCategory(Category.DRINK).size());
            assertEquals(1, menu.listByCategory(Category.SNACK).size());
        } finally {
            Files.deleteIfExists(tempCsv);
        }
    }

    @Test
    void loadOrdersShouldGroupLinesWithSameCustomerAndTimestamp()
            throws IOException, InvalidMenuItemDataException {
        Menu menu = buildMenu();
        LocalDateTime timestamp = LocalDateTime.parse("2026-02-01T10:15:30");

        Path tempCsv = Files.createTempFile("orders-loader-group", ".csv");
        Files.writeString(
                tempCsv,
                String.join(
                        System.lineSeparator(),
                        "timestamp,customerId,itemId,quantity,hasOwnCup",
                        timestamp + ",CUST-001,FOOD-001,1,false",
                        timestamp + ",CUST-001,SNACK-001,2,false"));

        try {
            TreeMap<LocalDateTime, Order> orders = CsvLoader.loadOrders(tempCsv, menu);
            assertEquals(1, orders.size());
            assertTrue(orders.containsKey(timestamp));
            assertEquals(6.00, orders.firstEntry().getValue().getTotal(), 1e-9);
        } finally {
            Files.deleteIfExists(tempCsv);
        }
    }

    @Test
    void loadOrdersShouldResolveTimestampCollisionsByNanos()
            throws IOException, InvalidMenuItemDataException {
        Menu menu = buildMenu();
        LocalDateTime timestamp = LocalDateTime.parse("2026-02-01T11:00:00");

        Path tempCsv = Files.createTempFile("orders-loader-collision", ".csv");
        Files.writeString(
                tempCsv,
                String.join(
                        System.lineSeparator(),
                        "timestamp,customerId,itemId,quantity,hasOwnCup",
                        timestamp + ",CUST-001,FOOD-001,1,false",
                        timestamp + ",CUST-002,SNACK-001,1,false"));

        try {
            TreeMap<LocalDateTime, Order> orders = CsvLoader.loadOrders(tempCsv, menu);
            assertEquals(2, orders.size());
            assertTrue(orders.containsKey(timestamp));
            assertTrue(orders.containsKey(timestamp.plusNanos(1)));
        } finally {
            Files.deleteIfExists(tempCsv);
        }
    }

    @Test
    void loadOrdersShouldSkipInvalidRowsAndContinue()
            throws IOException, InvalidMenuItemDataException {
        Menu menu = buildMenu();
        LocalDateTime timestamp = LocalDateTime.parse("2026-02-01T12:00:00");

        Path tempCsv = Files.createTempFile("orders-loader-invalid", ".csv");
        Files.writeString(
                tempCsv,
                String.join(
                        System.lineSeparator(),
                        "timestamp,customerId,itemId,quantity,hasOwnCup",
                        timestamp + ",CUST-001,FOOD-001,1,false",
                        "invalid-time,CUST-002,FOOD-001,1,false",
                        timestamp + ",CUST-003,UNKNOWN-001,1,false",
                        timestamp + ",CUST-004,SNACK-001,0,false",
                        timestamp + ",CUST-005,SNACK-001,1,notabool"));

        try {
            TreeMap<LocalDateTime, Order> orders = CsvLoader.loadOrders(tempCsv, menu);
            assertEquals(1, orders.size());
            assertEquals(3.00, orders.firstEntry().getValue().getTotal(), 1e-9);
        } finally {
            Files.deleteIfExists(tempCsv);
        }
    }

    private static Menu buildMenu() throws InvalidMenuItemDataException {
        Menu menu = new Menu();
        menu.addItem(new coffeeshop.model.MenuItem("FOOD-001", "Sandwich", 3.00, Category.FOOD));
        menu.addItem(new coffeeshop.model.MenuItem("SNACK-001", "Cookie", 1.50, Category.SNACK));
        menu.addItem(new coffeeshop.model.MenuItem("DRINK-001", "Latte", 4.00, Category.DRINK));
        return menu;
    }
}
