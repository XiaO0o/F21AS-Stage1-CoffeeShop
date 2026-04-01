package coffeeshop.simulation.model;

import coffeeshop.model.Order;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class OrderQueueManager {

    private final LinkedList<Order> onlineQueue = new LinkedList<>();
    private final LinkedList<Order> regularQueue = new LinkedList<>();
    private boolean producerFinished;

    public synchronized void enqueueOnline(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }
        onlineQueue.addLast(order);
        // Wake all waiting workers when new work arrives.
        notifyAll();
    }

    public synchronized void enqueueRegular(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }
        regularQueue.addLast(order);
        // Wake all waiting workers when new work arrives.
        notifyAll();
    }

    public synchronized QueuedOrder takeNextOrder() throws InterruptedException {
        return takeNextOrder(() -> false);
    }

    public synchronized QueuedOrder takeNextOrder(BooleanSupplier stopWaitingCondition)
            throws InterruptedException {
        if (stopWaitingCondition == null) {
            throw new IllegalArgumentException("stopWaitingCondition must not be null.");
        }

        // Wait without busy looping until there is an order, producer finishes, or worker exits.
        while (onlineQueue.isEmpty()
                && regularQueue.isEmpty()
                && !producerFinished
                && !stopWaitingCondition.getAsBoolean()) {
            wait();
        }

        if (stopWaitingCondition.getAsBoolean()) {
            return null;
        }

        if (!onlineQueue.isEmpty()) {
            return new QueuedOrder(onlineQueue.removeFirst(), QueueType.ONLINE);
        }
        if (!regularQueue.isEmpty()) {
            return new QueuedOrder(regularQueue.removeFirst(), QueueType.REGULAR);
        }
        return null;
    }

    public synchronized void markProducerFinished() {
        producerFinished = true;
        // Wake workers so they can terminate if queue is now empty.
        notifyAll();
    }

    public synchronized void signalWorkers() {
        notifyAll();
    }

    public synchronized boolean isClosedAndEmpty() {
        return producerFinished && onlineQueue.isEmpty() && regularQueue.isEmpty();
    }

    public synchronized List<QueuedOrder> snapshotQueuedOrders() {
        List<QueuedOrder> snapshot = new ArrayList<>(onlineQueue.size() + regularQueue.size());
        for (Order order : onlineQueue) {
            snapshot.add(new QueuedOrder(order, QueueType.ONLINE));
        }
        for (Order order : regularQueue) {
            snapshot.add(new QueuedOrder(order, QueueType.REGULAR));
        }
        return snapshot;
    }
}
