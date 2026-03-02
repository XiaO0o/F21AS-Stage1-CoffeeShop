package coffeeshop.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class CsvFileReader {

    private CsvFileReader() {
    }

    public static List<String> readAllLines(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        return Files.readAllLines(path);
    }
}
