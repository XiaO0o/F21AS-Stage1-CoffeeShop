package coffeeshop.simulation.model;

public record QueueOrderViewData(String orderId, int totalItems, QueueType queueType) {
    public QueueOrderViewData {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be null or blank.");
        }
        if (totalItems < 0) {
            throw new IllegalArgumentException("totalItems must not be negative.");
        }
        if (queueType == null) {
            throw new IllegalArgumentException("queueType must not be null.");
        }
    }
}
