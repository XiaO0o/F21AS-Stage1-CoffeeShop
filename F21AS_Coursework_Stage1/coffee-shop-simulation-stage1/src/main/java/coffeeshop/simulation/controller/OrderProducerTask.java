package coffeeshop.simulation.controller;

import coffeeshop.simulation.SimulationLogger;
import coffeeshop.simulation.model.OrderQueueManager;
import coffeeshop.simulation.model.QueueType;
import coffeeshop.simulation.model.QueuedOrder;
import coffeeshop.simulation.model.SimulationModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

final class OrderProducerTask implements Runnable {

    private final List<QueuedOrder> sourceOrders;
    private final OrderQueueManager queueManager;
    private final SimulationModel model;
    private final SimulationLogger logger;
    private final long enqueueDelayMs;
    private final BooleanSupplier stopRequestedSupplier;
    private final Runnable onFinishedCallback;

    OrderProducerTask(
            List<QueuedOrder> sourceOrders,
            OrderQueueManager queueManager,
            SimulationModel model,
            SimulationLogger logger,
            long enqueueDelayMs,
            BooleanSupplier stopRequestedSupplier,
            Runnable onFinishedCallback) {
        if (sourceOrders == null) {
            throw new IllegalArgumentException("sourceOrders must not be null.");
        }
        if (queueManager == null) {
            throw new IllegalArgumentException("queueManager must not be null.");
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null.");
        }
        if (logger == null) {
            throw new IllegalArgumentException("logger must not be null.");
        }
        if (enqueueDelayMs < 0) {
            throw new IllegalArgumentException("enqueueDelayMs must be >= 0.");
        }
        if (stopRequestedSupplier == null) {
            throw new IllegalArgumentException("stopRequestedSupplier must not be null.");
        }
        if (onFinishedCallback == null) {
            throw new IllegalArgumentException("onFinishedCallback must not be null.");
        }

        this.sourceOrders = copyOrders(sourceOrders);
        this.queueManager = queueManager;
        this.model = model;
        this.logger = logger;
        this.enqueueDelayMs = enqueueDelayMs;
        this.stopRequestedSupplier = stopRequestedSupplier;
        this.onFinishedCallback = onFinishedCallback;
    }

    @Override
    public void run() {
        logEvent("Producer started. orders=" + sourceOrders.size());
        try {
            for (QueuedOrder queuedOrder : sourceOrders) {
                if (stopRequestedSupplier.getAsBoolean()) {
                    logEvent("Producer observed stop request and will stop enqueueing.");
                    break;
                }

                if (queuedOrder.queueType() == QueueType.ONLINE) {
                    queueManager.enqueueOnline(queuedOrder.order());
                    logEvent("Producer enqueued ONLINE order " + queuedOrder.order().getOrderId() + ".");
                } else {
                    queueManager.enqueueRegular(queuedOrder.order());
                    logEvent("Producer enqueued regular order " + queuedOrder.order().getOrderId() + ".");
                }
                model.refreshQueueSnapshot();

                if (enqueueDelayMs > 0) {
                    Thread.sleep(enqueueDelayMs);
                }
            }
            logEvent("Producer completed enqueueing.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logEvent("Producer interrupted.");
        } catch (RuntimeException exception) {
            logEvent("Producer failed: " + exception.getMessage());
            throw exception;
        } finally {
            queueManager.markProducerFinished();
            model.refreshQueueSnapshot();
            logEvent("Producer marked queue as finished.");
            onFinishedCallback.run();
        }
    }

    private void logEvent(String message) {
        logger.log(message);
        model.appendLog(message);
    }

    private static List<QueuedOrder> copyOrders(List<QueuedOrder> sourceOrders) {
        List<QueuedOrder> copy = new ArrayList<>(sourceOrders.size());
        for (QueuedOrder order : sourceOrders) {
            if (order == null) {
                throw new IllegalArgumentException("sourceOrders must not contain null.");
            }
            copy.add(order);
        }
        return copy;
    }
}
