package coffeeshop.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OrderStatistics {

    private final Map<MenuItem, Integer> countMap = new HashMap<>();

    public void recordOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }

        for (OrderLine line : order.getLinesView()) {
            countMap.merge(line.getItem(), line.getQuantity(), Integer::sum);
        }
    }

    public int getCount(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null.");
        }
        return countMap.getOrDefault(item, 0);
    }

    Map<MenuItem, Integer> getCountMapView() {
        return Collections.unmodifiableMap(countMap);
    }
}
