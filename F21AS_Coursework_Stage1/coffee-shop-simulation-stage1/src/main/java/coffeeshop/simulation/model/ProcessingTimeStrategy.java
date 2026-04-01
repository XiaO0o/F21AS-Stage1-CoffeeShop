package coffeeshop.simulation.model;

import coffeeshop.model.Order;

public interface ProcessingTimeStrategy {
    long calculateProcessingTimeMillis(Order order);
}
