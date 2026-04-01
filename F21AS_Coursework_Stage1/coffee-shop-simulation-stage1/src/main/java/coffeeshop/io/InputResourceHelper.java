package coffeeshop.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class InputResourceHelper {

    public static final String CLASSPATH_PREFIX = "classpath:";

    private InputResourceHelper() {
    }

    public static List<String> readAllLines(String source) throws IOException {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be null or blank.");
        }

        String trimmed = source.trim();
        if (trimmed.startsWith(CLASSPATH_PREFIX)) {
            return readClasspathLines(trimmed.substring(CLASSPATH_PREFIX.length()));
        }

        Path filePath = Paths.get(trimmed);
        if (Files.exists(filePath)) {
            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        }

        // Fallback to classpath for default bundled resources.
        return readClasspathLines(trimmed);
    }

    private static List<String> readClasspathLines(String resourcePath) throws IOException {
        String normalized = normalizeResourcePath(resourcePath);
        try (InputStream inputStream = openResource(normalized)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Classpath resource not found: " + normalized);
            }

            List<String> lines = new ArrayList<>();
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return lines;
        }
    }

    private static String normalizeResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("resourcePath must not be null or blank.");
        }
        String normalized = resourcePath.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static InputStream openResource(String normalizedPath) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            InputStream stream = contextLoader.getResourceAsStream(normalizedPath);
            if (stream != null) {
                return stream;
            }
        }
        return InputResourceHelper.class.getClassLoader().getResourceAsStream(normalizedPath);
    }
}
