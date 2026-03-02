package coffeeshop.model;

import java.util.Locale;

public enum Category {
    DRINK,
    FOOD,
    SNACK;

    public static Category fromPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Category prefix must not be null.");
        }

        String normalizedPrefix = prefix.trim();
        if (normalizedPrefix.isEmpty()) {
            throw new IllegalArgumentException("Category prefix must not be blank.");
        }

        try {
            return Category.valueOf(normalizedPrefix.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException cause) {
            throw new IllegalArgumentException(
                    "Invalid category prefix: '" + prefix + "'. Expected DRINK, FOOD, or SNACK.",
                    cause);
        }
    }
}
