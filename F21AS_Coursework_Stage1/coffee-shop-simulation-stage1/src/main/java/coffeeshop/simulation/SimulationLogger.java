package coffeeshop.simulation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class SimulationLogger {

    private static final DateTimeFormatter LOG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static volatile SimulationLogger instance;

    private final List<String> entries = new ArrayList<>();

    private SimulationLogger() {
    }

    public static SimulationLogger getInstance() {
        if (instance == null) {
            synchronized (SimulationLogger.class) {
                if (instance == null) {
                    instance = new SimulationLogger();
                }
            }
        }
        return instance;
    }

    public synchronized void log(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be null or blank.");
        }
        String entry =
                LOG_TIME_FORMAT.format(LocalDateTime.now())
                        + " ["
                        + Thread.currentThread().getName()
                        + "] "
                        + message;
        entries.add(entry);
    }

    public synchronized List<String> getEntriesSnapshot() {
        return new ArrayList<>(entries);
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void writeToFile(Path logFilePath) throws IOException {
        if (logFilePath == null) {
            throw new IllegalArgumentException("logFilePath must not be null.");
        }

        Path parent = logFilePath.toAbsolutePath().normalize().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(logFilePath, entries, StandardCharsets.UTF_8);
    }
}
