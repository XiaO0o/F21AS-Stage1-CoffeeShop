package coffeeshop.simulation.controller;

import coffeeshop.simulation.SimulationLogger;
import coffeeshop.simulation.model.OrderQueueManager;
import coffeeshop.simulation.model.ProcessingTimeStrategy;
import coffeeshop.simulation.model.QueuedOrder;
import coffeeshop.simulation.model.ServiceStaff;
import coffeeshop.simulation.model.SimulationSpeedMode;
import coffeeshop.simulation.model.SimulationModel;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SimulationEngine {

    public static final int DEFAULT_STAFF_COUNT = 2;
    public static final Path DEFAULT_LOG_PATH = Paths.get("data/simulation.log");
    public static final Path DEFAULT_REPORT_PATH = Paths.get("data/report.txt");

    private final SimulationModel model;
    private final Map<SimulationSpeedMode, ProcessingTimeStrategy> strategies;
    private final AtomicReference<ProcessingTimeStrategy> activeProcessingStrategy;
    private final SimulationLogger logger;
    private final Path logFilePath;
    private final Path reportFilePath;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile SimulationSpeedMode currentSpeedMode = SimulationSpeedMode.ONE_X;

    private Thread producerThread;
    private Thread monitorThread;

    private final Object workerLock = new Object();
    private final List<WorkerHandle> workerHandles = new ArrayList<>();
    private int nextStaffIndex = 1;

    private final Object lifecycleLock = new Object();
    private int activeWorkerCount;
    private boolean producerCompleted;

    public SimulationEngine(
            SimulationModel model,
            Map<SimulationSpeedMode, ProcessingTimeStrategy> strategies,
            SimulationLogger logger,
            Path logFilePath,
            Path reportFilePath) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null.");
        }
        if (strategies == null) {
            throw new IllegalArgumentException("strategies must not be null.");
        }
        if (logger == null) {
            throw new IllegalArgumentException("logger must not be null.");
        }
        if (logFilePath == null) {
            throw new IllegalArgumentException("logFilePath must not be null.");
        }
        if (reportFilePath == null) {
            throw new IllegalArgumentException("reportFilePath must not be null.");
        }

        this.model = model;
        this.strategies = validateAndCopyStrategies(strategies);
        this.logger = logger;
        this.logFilePath = logFilePath;
        this.reportFilePath = reportFilePath;
        this.activeProcessingStrategy = new AtomicReference<>(this.strategies.get(SimulationSpeedMode.ONE_X));
    }

    public synchronized void start(
            List<QueuedOrder> sourceOrders,
            int staffCount,
            long producerDelayMs,
            SimulationSpeedMode initialSpeedMode) {
        if (sourceOrders == null) {
            throw new IllegalArgumentException("sourceOrders must not be null.");
        }
        if (staffCount < 1) {
            throw new IllegalArgumentException("staffCount must be at least 1.");
        }
        if (producerDelayMs < 0) {
            throw new IllegalArgumentException("producerDelayMs must be >= 0.");
        }
        if (initialSpeedMode == null) {
            throw new IllegalArgumentException("initialSpeedMode must not be null.");
        }
        if (running.get()) {
            throw new IllegalStateException("Simulation is already running.");
        }

        synchronized (workerLock) {
            workerHandles.clear();
            nextStaffIndex = 1;
        }
        synchronized (lifecycleLock) {
            activeWorkerCount = 0;
            producerCompleted = false;
        }

        stopRequested.set(false);
        running.set(true);
        logger.clear();
        applySpeedMode(initialSpeedMode);

        List<ServiceStaff> staffMembers = new ArrayList<>(staffCount);
        for (int i = 0; i < staffCount; i++) {
            staffMembers.add(createNewStaff());
        }
        model.startNewSimulation(staffMembers);
        model.refreshQueueSnapshot();

        synchronized (lifecycleLock) {
            activeWorkerCount = staffCount;
        }

        OrderQueueManager queueManager = model.getQueueManager();
        producerThread =
                new Thread(
                        new OrderProducerTask(
                                sourceOrders,
                                queueManager,
                                model,
                                logger,
                                producerDelayMs,
                                stopRequested::get,
                                this::onProducerFinished),
                        "producer-thread");

        for (ServiceStaff staff : staffMembers) {
            startWorkerForStaff(staff);
        }

        producerThread.start();

        monitorThread = new Thread(this::awaitTermination, "simulation-monitor");
        monitorThread.start();
    }

    public boolean addStaff() {
        if (!running.get()) {
            return false;
        }

        ServiceStaff staff = createNewStaff();
        synchronized (lifecycleLock) {
            activeWorkerCount++;
        }
        model.updateStaffState(staff);
        startWorkerForStaff(staff);
        model.appendLog("Staff added: " + staff.getStaffId() + ".");
        return true;
    }

    public boolean requestRemoveOneStaff() {
        if (!running.get()) {
            return false;
        }
        if (countRemovableWorkers() <= 1) {
            model.appendLog("Cannot remove staff because only one active staff remains.");
            return false;
        }

        WorkerHandle handle = findRemovableWorker();
        if (handle == null) {
            model.appendLog("No removable staff is available right now.");
            return false;
        }

        handle.workerTask.requestStopGracefully();
        model.getQueueManager().signalWorkers();
        model.appendLog("Remove requested for " + handle.staff.getStaffId() + ".");
        return true;
    }

    public void requestGracefulStop() {
        if (!running.get()) {
            return;
        }
        stopRequested.set(true);
        model.markStopRequested();

        Thread producer = producerThread;
        if (producer != null) {
            producer.interrupt();
        }
        model.getQueueManager().markProducerFinished();
        model.getQueueManager().signalWorkers();
    }

    public void changeSpeedMode(SimulationSpeedMode speedMode) {
        if (speedMode == null) {
            throw new IllegalArgumentException("speedMode must not be null.");
        }
        applySpeedMode(speedMode);
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean awaitCompletion(long timeoutMs) {
        Thread monitor = monitorThread;
        if (monitor == null) {
            return true;
        }
        try {
            if (timeoutMs <= 0) {
                monitor.join();
            } else {
                monitor.join(timeoutMs);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
        return !monitor.isAlive();
    }

    public SimulationSpeedMode getCurrentSpeedMode() {
        return currentSpeedMode;
    }

    private void awaitTermination() {
        try {
            synchronized (lifecycleLock) {
                while (!producerCompleted || activeWorkerCount > 0) {
                    lifecycleLock.wait();
                }
            }
            model.refreshQueueSnapshot();
            model.markSimulationFinished();
            logEvent("Simulation monitor detected all threads completed.");
            writeLogFile();
            writeReportFile();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logEvent("Simulation monitor interrupted.");
            throw new IllegalStateException("Simulation monitor interrupted.", exception);
        } catch (RuntimeException exception) {
            logEvent("Simulation monitor failed: " + exception.getMessage());
            throw exception;
        } finally {
            running.set(false);
        }
    }

    private void writeLogFile() {
        try {
            logger.writeToFile(logFilePath);
            model.appendLog("Simulation log file: " + logFilePath.toAbsolutePath().normalize());
        } catch (IOException exception) {
            model.appendLog("Failed to write simulation log file: " + exception.getMessage());
            throw new IllegalStateException("Failed to write simulation log file.", exception);
        }
    }

    private void writeReportFile() {
        try {
            Path parent = reportFilePath.toAbsolutePath().normalize().getParent();
            if (parent != null) {
                java.nio.file.Files.createDirectories(parent);
            }
            java.nio.file.Files.writeString(reportFilePath, model.buildReportContent());
            model.appendLog("Simulation report file: " + reportFilePath.toAbsolutePath().normalize());
        } catch (IOException exception) {
            model.appendLog("Failed to write report file: " + exception.getMessage());
            throw new IllegalStateException("Failed to write report file.", exception);
        }
    }

    private void logEvent(String message) {
        logger.log(message);
        model.appendLog(message);
    }

    private void applySpeedMode(SimulationSpeedMode speedMode) {
        ProcessingTimeStrategy strategy = strategies.get(speedMode);
        if (strategy == null) {
            throw new IllegalArgumentException("No processing strategy configured for speed mode " + speedMode);
        }
        activeProcessingStrategy.set(strategy);
        currentSpeedMode = speedMode;
        model.setSpeedMode(speedMode);
    }

    private ServiceStaff createNewStaff() {
        synchronized (workerLock) {
            ServiceStaff staff = new ServiceStaff("STAFF-" + nextStaffIndex);
            nextStaffIndex++;
            return staff;
        }
    }

    private void startWorkerForStaff(ServiceStaff staff) {
        ServiceStaffWorkerTask workerTask =
                new ServiceStaffWorkerTask(
                        staff,
                        model.getQueueManager(),
                        activeProcessingStrategy::get,
                        this::getCurrentSpeedMode,
                        model,
                        logger,
                        this::onWorkerStopped);
        Thread workerThread = new Thread(workerTask, "worker-" + staff.getStaffId());
        synchronized (workerLock) {
            workerHandles.add(new WorkerHandle(staff, workerTask, workerThread));
        }
        workerThread.start();
    }

    private WorkerHandle findRemovableWorker() {
        synchronized (workerLock) {
            for (int i = workerHandles.size() - 1; i >= 0; i--) {
                WorkerHandle handle = workerHandles.get(i);
                if (!handle.thread.isAlive()) {
                    continue;
                }
                if (handle.workerTask.isRemovalRequested()) {
                    continue;
                }
                return handle;
            }
        }
        return null;
    }

    private int countRemovableWorkers() {
        synchronized (workerLock) {
            int count = 0;
            for (WorkerHandle handle : workerHandles) {
                if (handle.thread.isAlive() && !handle.workerTask.isRemovalRequested()) {
                    count++;
                }
            }
            return count;
        }
    }

    private void onWorkerStopped() {
        synchronized (lifecycleLock) {
            activeWorkerCount = Math.max(0, activeWorkerCount - 1);
            lifecycleLock.notifyAll();
        }
    }

    private void onProducerFinished() {
        synchronized (lifecycleLock) {
            producerCompleted = true;
            lifecycleLock.notifyAll();
        }
    }

    private static Map<SimulationSpeedMode, ProcessingTimeStrategy> validateAndCopyStrategies(
            Map<SimulationSpeedMode, ProcessingTimeStrategy> source) {
        EnumMap<SimulationSpeedMode, ProcessingTimeStrategy> copy =
                new EnumMap<>(SimulationSpeedMode.class);
        for (SimulationSpeedMode speedMode : SimulationSpeedMode.values()) {
            ProcessingTimeStrategy strategy = source.get(speedMode);
            if (strategy == null) {
                throw new IllegalArgumentException("Missing strategy for speed mode: " + speedMode);
            }
            copy.put(speedMode, strategy);
        }
        return copy;
    }

    private record WorkerHandle(
            ServiceStaff staff, ServiceStaffWorkerTask workerTask, Thread thread) {}
}
