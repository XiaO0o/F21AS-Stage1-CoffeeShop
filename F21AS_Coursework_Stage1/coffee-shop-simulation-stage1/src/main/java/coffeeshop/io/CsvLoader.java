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

public final class CsvLoader {

    private CsvLoader() {
    }

    public static Menu loadMenu(Path menuCsvPath) {
        if (menuCsvPath == null) {
            throw new IllegalArgumentException("menuCsvPath must not be null.");
        }

        Menu menu = new Menu();
        List<String> lines;
        try {
            lines = CsvFileReader.readAllLines(menuCsvPath);
        } catch (IOException ioException) {
            System.err.println(
                    "Failed to read menu CSV at '" + menuCsvPath + "': " + ioException.getMessage());
            return menu;
        }

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line == null || line.isBlank()) {
                continue;
            }
            if (lineNumber == 1 && isMenuHeader(line)) {
                continue;
            }

            String[] parts = line.split(",", -1);
            String id = parts[0].trim();
            String name = parts[1].trim();
            String priceText = parts[2].trim();
            String categoryText = parts[3].trim();

            try {
                double price = Double.parseDouble(priceText);
                Category category = Category.fromPrefix(categoryText);
                MenuItem menuItem = new MenuItem(id, name, price, category);
                menu.addItem(menuItem);
            } catch (IllegalArgumentException | InvalidMenuItemDataException exception) {
                // Skip this bad line and continue loading the rest.
                // We print line number + reason to help quick debugging.
                System.err.println(
                        "Skipping line "
                                + lineNumber
                                + " in '"
                                + menuCsvPath
                                + "': "
                                + exception.getMessage());
            }
        }

        return menu;
    }

    public static TreeMap<LocalDateTime, Order> loadOrders(Path ordersCsvPath, Menu menu) {
        if (ordersCsvPath == null) {
            throw new IllegalArgumentException("ordersCsvPath must not be null.");
        }
        if (menu == null) {
            throw new IllegalArgumentException("menu must not be null.");
        }

        TreeMap<LocalDateTime, Order> orders = new TreeMap<>();
        Map<String, LocalDateTime> groupedOrderKeys = new HashMap<>();

        List<String> lines;
        try {
            lines = CsvFileReader.readAllLines(ordersCsvPath);
        } catch (IOException ioException) {
            System.err.println(
                    "Failed to read orders CSV at '" + ordersCsvPath + "': " + ioException.getMessage());
            return orders;
        }

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line == null || line.isBlank()) {
                continue;
            }
            if (lineNumber == 1 && isOrdersHeader(line)) {
                continue;
            }

            String[] parts = line.split(",", -1);
            String timestampText = parts[0].trim();
            String customerId = parts[1].trim();
            String itemId = parts[2].trim();
            String quantityText = parts[3].trim();
            String hasOwnCupText = parts[4].trim();

            try {
                LocalDateTime timestamp = LocalDateTime.parse(timestampText);
                if (customerId.isBlank()) {
                    throw new IllegalArgumentException("customerId must not be blank.");
                }
                if (itemId.isBlank()) {
                    throw new IllegalArgumentException("itemId must not be blank.");
                }

                MenuItem item = menu.findById(itemId);
                if (item == null) {
                    throw new IllegalArgumentException("Unknown itemId: '" + itemId + "'.");
                }

                int quantity = Integer.parseInt(quantityText);
                if (quantity < 1) {
                    throw new IllegalArgumentException("quantity must be at least 1.");
                }

                boolean hasOwnCup = parseBooleanStrict(hasOwnCupText);

                String groupKey = buildGroupKey(timestamp, customerId);
                LocalDateTime orderKey = groupedOrderKeys.get(groupKey);

                Order order;
                if (orderKey != null) {
                    order = orders.get(orderKey);
                    if (order == null) {
                        throw new IllegalStateException(
                                "Inconsistent order grouping state for key '" + groupKey + "'.");
                    }
                } else {
                    // Keep TreeMap key unique by adding 1ns on collisions.
                    LocalDateTime resolvedKey = resolveUniqueTimestamp(orders, timestamp);
                    order = new Order(customerId, resolvedKey);
                    orders.put(resolvedKey, order);
                    groupedOrderKeys.put(groupKey, resolvedKey);
                }

                order.addLine(item, quantity, hasOwnCup);
            } catch (DateTimeParseException | IllegalArgumentException exception) {
                // Skip this bad line and continue loading the rest.
                // We print line number + reason to help quick debugging.
                System.err.println(
                        "Skipping line "
                                + lineNumber
                                + " in '"
                                + ordersCsvPath
                                + "': "
                                + exception.getMessage());
            }
        }

        return orders;
    }

    private static String buildGroupKey(LocalDateTime timestamp, String customerId) {
        return timestamp + "|" + customerId;
    }

    private static LocalDateTime resolveUniqueTimestamp(
            TreeMap<LocalDateTime, Order> orders, LocalDateTime timestamp) {
        LocalDateTime resolved = timestamp;
        while (orders.containsKey(resolved)) {
            resolved = resolved.plusNanos(1);
        }
        return resolved;
    }

    private static boolean parseBooleanStrict(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException(
                "hasOwnCup must be 'true' or 'false' but was '" + value + "'.");
    }

    private static boolean isMenuHeader(String line) {
        return "id,name,price,category".equals(line.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean isOrdersHeader(String line) {
        return "timestamp,customerid,itemid,quantity,hasowncup"
                .equals(line.trim().toLowerCase(Locale.ROOT));
    }
}
