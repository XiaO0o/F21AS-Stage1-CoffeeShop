package coffeeshop.simulation.model;

import coffeeshop.model.Order;

public record QueuedOrder(Order order, QueueType queueType) {
    public QueuedOrder {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }
        if (queueType == null) {
            throw new IllegalArgumentException("queueType must not be null.");
        }
    }
}
