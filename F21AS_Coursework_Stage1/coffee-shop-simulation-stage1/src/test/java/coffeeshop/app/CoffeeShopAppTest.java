package coffeeshop.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CoffeeShopAppTest {

    @Test
    void mainShouldLoadMenuOrdersAndPrintSummary() throws IOException {
        String originalHeadless = System.getProperty("java.awt.headless");
        System.setProperty("java.awt.headless", "true");

        Path tempMenuCsv = Files.createTempFile("menu", ".csv");
        Files.writeString(
                tempMenuCsv,
                String.join(
                        System.lineSeparator(),
                        "id,name,price,category",
                        "DRINK-001,Americano,2.80,DRINK",
                        "FOOD-001,Croissant,3.20,FOOD",
                        "SNACK-001,Cookie,1.50,SNACK"));
        Path tempOrdersCsv = Files.createTempFile("orders", ".csv");
        Files.writeString(
                tempOrdersCsv,
                String.join(
                        System.lineSeparator(),
                        "timestamp,customerId,itemId,quantity,hasOwnCup",
                        "2026-02-01T10:15:30,CUST-001,FOOD-001,1,false"));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
            CoffeeShopApp.main(new String[] {tempMenuCsv.toString(), tempOrdersCsv.toString()});
        } finally {
            System.setOut(originalOut);
            if (originalHeadless == null) {
                System.clearProperty("java.awt.headless");
            } else {
                System.setProperty("java.awt.headless", originalHeadless);
            }
            Files.deleteIfExists(tempMenuCsv);
            Files.deleteIfExists(tempOrdersCsv);
        }

        String output = capturedOutput.toString(StandardCharsets.UTF_8);
        Path expectedReportPath =
                tempMenuCsv.toAbsolutePath().normalize().getParent().resolve("report.txt");
        assertTrue(output.contains("CoffeeShopApp started"));
        assertTrue(output.contains("Loaded menu item count: 3"));
        assertTrue(output.contains("Loaded order count: 1"));
        assertTrue(output.contains("Total sales: 3.20"));
        assertTrue(output.contains("Report written to: " + expectedReportPath.toString()));
    }
}
