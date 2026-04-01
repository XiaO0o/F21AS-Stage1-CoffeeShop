package coffeeshop.simulation.controller;

import coffeeshop.model.Order;
import coffeeshop.simulation.SimulationLogger;
import coffeeshop.simulation.model.OrderQueueManager;
import coffeeshop.simulation.model.ProcessingTimeStrategy;
import coffeeshop.simulation.model.QueueType;
import coffeeshop.simulation.model.QueuedOrder;
import coffeeshop.simulation.model.ServiceStaff;
import coffeeshop.simulation.model.ServiceStaffStatus;
import coffeeshop.simulation.model.SimulationSpeedMode;
import coffeeshop.simulation.model.SimulationModel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

final class ServiceStaffWorkerTask implements Runnable {

    private final ServiceStaff staff;
    private final OrderQueueManager queueManager;
    private final Supplier<ProcessingTimeStrategy> processingTimeStrategySupplier;
    private final Supplier<SimulationSpeedMode> speedModeSupplier;
    private final SimulationModel model;
    private final SimulationLogger logger;
    private final Runnable onStoppedCallback;
    private final AtomicBoolean removeRequested = new AtomicBoolean(false);

    ServiceStaffWorkerTask(
            ServiceStaff staff,
            OrderQueueManager queueManager,
            Supplier<ProcessingTimeStrategy> processingTimeStrategySupplier,
            Supplier<SimulationSpeedMode> speedModeSupplier,
            SimulationModel model,
            SimulationLogger logger,
            Runnable onStoppedCallback) {
        if (staff == null) {
            throw new IllegalArgumentException("staff must not be null.");
        }
        if (queueManager == null) {
            throw new IllegalArgumentException("queueManager must not be null.");
        }
        if (processingTimeStrategySupplier == null) {
            throw new IllegalArgumentException("processingTimeStrategySupplier must not be null.");
        }
        if (speedModeSupplier == null) {
            throw new IllegalArgumentException("speedModeSupplier must not be null.");
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null.");
        }
        if (logger == null) {
            throw new IllegalArgumentException("logger must not be null.");
        }
        if (onStoppedCallback == null) {
            throw new IllegalArgumentException("onStoppedCallback must not be null.");
        }

        this.staff = staff;
        this.queueManager = queueManager;
        this.processingTimeStrategySupplier = processingTimeStrategySupplier;
        this.speedModeSupplier = speedModeSupplier;
        this.model = model;
        this.logger = logger;
        this.onStoppedCallback = onStoppedCallback;
    }

    @Override
    public void run() {
        logEvent(staff.getStaffId() + " started.");
        try {
            processOrders();
        } finally {
            staff.setState(ServiceStaffStatus.STOPPED, "-", "Stopped");
            model.updateStaffState(staff);
            logEvent(staff.getStaffId() + " stopped.");
            onStoppedCallback.run();
        }
    }

    private void processOrders() {
        while (true) {
            if (removeRequested.get()) {
                return;
            }

            staff.setState(ServiceStaffStatus.WAITING_FOR_ORDER, "-", "Waiting for order");
            model.updateStaffState(staff);

            QueuedOrder queuedOrder = takeNextOrder();
            model.refreshQueueSnapshot();
            if (queuedOrder == null) {
                if (removeRequested.get()) {
                    logEvent(staff.getStaffId() + " is shutting down after remove request.");
                } else {
                    logEvent(staff.getStaffId() + " detected closed and empty queue.");
                }
                return;
            }

            processSingleOrder(queuedOrder);
            if (removeRequested.get()) {
                staff.setState(ServiceStaffStatus.SHUTTING_DOWN, "-", "Stopping after current order");
                model.updateStaffState(staff);
                return;
            }
        }
    }

    private QueuedOrder takeNextOrder() {
        try {
            return queueManager.takeNextOrder(removeRequested::get);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logEvent(staff.getStaffId() + " interrupted while waiting for order.");
            return null;
        }
    }

    void requestStopGracefully() {
        if (!removeRequested.compareAndSet(false, true)) {
            return;
        }

        ServiceStaffStatus currentStatus = staff.getStatus();
        if (currentStatus == ServiceStaffStatus.PROCESSING_ORDER) {
            staff.setState(
                    ServiceStaffStatus.SHUTTING_DOWN,
                    staff.getCurrentOrderId(),
                    "Finishing current order before stop");
        } else {
            staff.setState(ServiceStaffStatus.SHUTTING_DOWN, "-", "Stopping now");
        }
        model.updateStaffState(staff);
        logEvent(staff.getStaffId() + " removal requested.");
    }

    boolean isRemovalRequested() {
        return removeRequested.get();
    }

    private void processSingleOrder(QueuedOrder queuedOrder) {
        Order order = queuedOrder.order();
        staff.setState(
                ServiceStaffStatus.PROCESSING_ORDER,
                order.getOrderId(),
                "Processing " + queuedOrder.queueType().toDisplayText() + " order " + order.getOrderId());
        model.updateStaffState(staff);

        ProcessingTimeStrategy processingTimeStrategy = processingTimeStrategySupplier.get();
        if (processingTimeStrategy == null) {
            throw new IllegalStateException("processingTimeStrategySupplier returned null.");
        }
        long processMs = processingTimeStrategy.calculateProcessingTimeMillis(order);
        if (processMs < 0) {
            logEvent(staff.getStaffId() + " got a negative processing time.");
            throw new IllegalStateException("Processing time must not be negative.");
        }

        SimulationSpeedMode speedMode = speedModeSupplier.get();
        if (speedMode == null) {
            throw new IllegalStateException("speedModeSupplier returned null.");
        }
        logEvent(
                staff.getStaffId()
                        + " serving "
                        + queuedOrder.queueType().toDisplayText()
                        + " order "
                        + order.getOrderId()
                        + " at speed "
                        + speedMode.displayText()
                        + " for "
                        + processMs
                        + " ms."
                        + (queuedOrder.queueType() == QueueType.ONLINE
                                ? " (online priority applied)"
                                : ""));
        try {
            Thread.sleep(processMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logEvent(staff.getStaffId() + " interrupted while processing order.");
            return;
        }

        staff.setState(ServiceStaffStatus.IDLE, "-", "Completed order " + order.getOrderId());
        model.recordCompletedOrder(order);
        model.updateStaffState(staff);
        logEvent(
                staff.getStaffId()
                        + " completed "
                        + queuedOrder.queueType().toDisplayText()
                        + " order "
                        + order.getOrderId()
                        + ".");
    }

    private void logEvent(String message) {
        logger.log(message);
        model.appendLog(message);
    }
}
