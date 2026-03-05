package coffeeshop.io;

import coffeeshop.model.Category;
import coffeeshop.model.InvalidMenuItemDataException;
import coffeeshop.model.Menu;
import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class to load domain objects (Menu and Orders) from CSV files.
 * Provides robust parsing by skipping malformed rows and logging errors.
 */
public final class CsvLoader {

    private CsvLoader() {
    }

    /**
     * Loads the menu from the specified CSV file.
     * Invalid rows will be skipped, and a valid Menu object will be returned.
     */
    public static Menu loadMenu(Path menuCsvPath) {
        if (menuCsvPath == null) {
            throw new IllegalArgumentException("menuCsvPath must not be null.");
        }

        Menu menu = new Menu();
        List<String> lines;
        try {
            lines = CsvFileReader.readAllLines(menuCsvPath);
        } catch (IOException ioException) {
            System.err.println("Failed to read menu CSV: " + ioException.getMessage());
            return menu;
        }

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line == null || line.isBlank() || (lineNumber == 1 && isMenuHeader(line))) {
                continue; // Skip headers and empty lines
            }

            String[] parts = line.split(",", -1);
            try {
                String id = parts[0].trim();
                String name = parts[1].trim();
                double price = Double.parseDouble(parts[2].trim());
                Category category = Category.fromPrefix(parts[3].trim());
                
                MenuItem menuItem = new MenuItem(id, name, price, category);
                menu.addItem(menuItem);
            } catch (IllegalArgumentException | InvalidMenuItemDataException exception) {
                // Skip this bad line and continue loading the rest.
                System.err.println("Skipping line " + lineNumber + " in '" + menuCsvPath + "': " + exception.getMessage());
            }
        }
        return menu;
    }

    /**
     * Loads orders from the specified CSV file and groups items belonging to the same order.
     * Orders are stored in a TreeMap to maintain chronological ordering.
     */
    public static TreeMap<LocalDateTime, Order> loadOrders(Path ordersCsvPath, Menu menu) {
        if (ordersCsvPath == null || menu == null) {
            throw new IllegalArgumentException("Paths and Menu must not be null.");
        }

        TreeMap<LocalDateTime, Order> orders = new TreeMap<>();
        // Maps a composite key (timestamp|customerId) to the actual resolved timestamp key
        Map<String, LocalDateTime> groupedOrderKeys = new HashMap<>();

        List<String> lines;
        try {
            lines = CsvFileReader.readAllLines(ordersCsvPath);
        } catch (IOException ioException) {
            System.err.println("Failed to read orders CSV: " + ioException.getMessage());
            return orders;
        }

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line == null || line.isBlank() || (lineNumber == 1 && isOrdersHeader(line))) {
                continue;
            }

            String[] parts = line.split(",", -1);
            try {
                LocalDateTime timestamp = LocalDateTime.parse(parts[0].trim());
                String customerId = parts[1].trim();
                String itemId = parts[2].trim();
                
                if (customerId.isBlank() || itemId.isBlank()) {
                    throw new IllegalArgumentException("Customer ID and Item ID must not be blank.");
                }

                MenuItem item = menu.findById(itemId);
                if (item == null) {
                    throw new IllegalArgumentException("Unknown itemId: '" + itemId + "'.");
                }

                int quantity = Integer.parseInt(parts[3].trim());
                if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1.");
                
                boolean hasOwnCup = parseBooleanStrict(parts[4].trim());

                // Group items with the same timestamp and customer ID into a single order
                String groupKey = buildGroupKey(timestamp, customerId);
                LocalDateTime orderKey = groupedOrderKeys.get(groupKey);
                Order order;

                if (orderKey != null) {
                    order = orders.get(orderKey);
                } else {
                    // Handle timestamp collisions by adding 1 nanosecond to ensure unique keys in TreeMap
                    LocalDateTime resolvedKey = resolveUniqueTimestamp(orders, timestamp);
                    order = new Order(customerId, resolvedKey);
                    orders.put(resolvedKey, order);
                    groupedOrderKeys.put(groupKey, resolvedKey);
                }

                order.addLine(item, quantity, hasOwnCup);
            } catch (DateTimeParseException | IllegalArgumentException exception) {
                System.err.println("Skipping line " + lineNumber + " in '" + ordersCsvPath + "': " + exception.getMessage());
            }
        }
        return orders;
    }

    private static String buildGroupKey(LocalDateTime timestamp, String customerId) {
        return timestamp + "|" + customerId;
    }

    /**
     * Resolves key collisions in the TreeMap by incrementing the timestamp by 1 nanosecond.
     */
    private static LocalDateTime resolveUniqueTimestamp(TreeMap<LocalDateTime, Order> orders, LocalDateTime timestamp) {
        LocalDateTime resolved = timestamp;
        while (orders.containsKey(resolved)) {
            resolved = resolved.plusNanos(1);
        }
        return resolved;
    }

    private static boolean parseBooleanStrict(String value) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new IllegalArgumentException("hasOwnCup must be 'true' or 'false'.");
    }

    private static boolean isMenuHeader(String line) {
        return "id,name,price,category".equals(line.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean isOrdersHeader(String line) {
        return "timestamp,customerid,itemid,quantity,hasowncup".equals(line.trim().toLowerCase(Locale.ROOT));
    }
}