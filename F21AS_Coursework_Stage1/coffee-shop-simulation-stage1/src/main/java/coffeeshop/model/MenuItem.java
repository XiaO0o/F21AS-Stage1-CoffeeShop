package coffeeshop.model;

import java.util.Objects;
import java.util.regex.Pattern;

public class MenuItem {

    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z]+-\\d{3}$");

    private final String id;
    private final String name;
    private final double price;
    private final Category category;

    public MenuItem(String id, String name, double price, Category category)
            throws InvalidMenuItemDataException {
        // We validate early so bad CSV or UI input fails fast.
        if (id == null || id.isBlank()) {
            throw new InvalidMenuItemDataException("Invalid id: id must not be null or blank.");
        }
        // ID must look like CATEGORY-001.
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new InvalidMenuItemDataException(
                    "Invalid id: '" + id + "' does not match pattern <CATEGORY>-XXX.");
        }
        // Name and price checks keep menu data usable.
        if (name == null || name.isBlank()) {
            throw new InvalidMenuItemDataException("Invalid name: name must not be blank.");
        }
        if (price <= 0) {
            throw new InvalidMenuItemDataException("Invalid price: price must be greater than 0.");
        }
        if (category == null) {
            throw new InvalidMenuItemDataException("Invalid category: category must not be null.");
        }

        String categoryPrefix = id.substring(0, id.indexOf('-'));
        Category categoryFromId;
        try {
            categoryFromId = Category.fromPrefix(categoryPrefix);
        } catch (IllegalArgumentException cause) {
            // Wrap parsing errors in our checked exception for callers.
            throw new InvalidMenuItemDataException(
                    "Invalid id category prefix: '" + categoryPrefix + "'.",
                    cause);
        }

        if (categoryFromId != category) {
            throw new InvalidMenuItemDataException(
                    "Invalid category mismatch: id prefix '"
                            + categoryPrefix
                            + "' maps to "
                            + categoryFromId
                            + ", but constructor category is "
                            + category
                            + ".");
        }

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MenuItem menuItem)) {
            return false;
        }
        return id.equals(menuItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " - " + name + " (£" + price + ", " + category + ")";
    }
}
