package coffeeshop.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu {

    private final Map<String, MenuItem> itemsMap = new HashMap<>();

    public MenuItem findById(String id) {
        return itemsMap.get(id);
    }

    public List<MenuItem> listByCategory(Category cat) {
        if (cat == null) {
            throw new IllegalArgumentException("Category must not be null.");
        }

        List<MenuItem> filteredItems = new ArrayList<>();
        for (MenuItem item : itemsMap.values()) {
            if (item.getCategory() == cat) {
                filteredItems.add(item);
            }
        }

        filteredItems.sort(
                Comparator.comparing(MenuItem::getName).thenComparing(MenuItem::getId));
        return filteredItems;
    }

    public void addItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("MenuItem must not be null.");
        }

        String id = item.getId();
        if (itemsMap.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate id: '" + id + "'.");
        }

        itemsMap.put(id, item);
    }
}
