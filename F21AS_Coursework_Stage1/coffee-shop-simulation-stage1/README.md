# Coffee Shop Simulation Stage 1

Java 25 Maven project for Coffee Shop Simulation Stage 1.

## Prerequisites

- JDK 17+
- Maven 3.9+

## Data files

- Default menu file: `data/menu.csv`
- Default orders file: `data/orders.csv`
- Both files include valid rows and intentionally invalid rows.
- Invalid rows are skipped and logged to `System.err`.

## Run tests

```bash
mvn test
```

## Run application

For a new device (or after cleaning `target/`), run compile first:

```bash
mvn -DskipTests compile exec:java
```

The app reads `data/menu.csv` and `data/orders.csv` by default.

Example startup output:

```text
CoffeeShopApp started
Loaded menu item count: 8
Loaded order count: 4
Total sales: 44.04
```

## GUI workflow

1. Enter `Customer ID` and click `New Order`.
2. Select one or more items from the left list.
3. Set `Quantity` and `Has Own Cup`, then click `Add Selected Items`.
4. Click `Show Bill` to view line subtotals, subtotal, RuleA, RuleB, and total.
5. Click `Complete Order` to save the order and update statistics.
6. Click `Exit & Generate Report` to write `data/report.txt` and close the app.

## Report output

- Generated file: `data/report.txt`
- Contains:
  - item list and item counts
  - best-selling ranking
  - total order count and total sales

## Run with custom CSV paths

```bash
mvn -DskipTests compile exec:java -Dexec.args="data/menu.csv data/orders.csv"
```
