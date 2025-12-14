package edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Web;

import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
@Profile("dev")
@RequiredArgsConstructor
public class TestDataController {

    private final OrderRecordRepositoryPort repositoryPort;

    @PostMapping("/seed-data")
    public ResponseEntity<Map<String, Object>> seedTestData(
            @RequestBody(required = false) Map<String, Object> customData) {
        try {
            System.out.println("=== SEED-DATA ENDPOINT LLAMADO ===");

            List<OrderRecord> existingData = repositoryPort.findAll();
            System.out.println("Registros existentes: " + existingData.size());

            if (!existingData.isEmpty()) {
                existingData.forEach(record -> repositoryPort.deleteById(record.getId()));
                System.out.println("Registros anteriores eliminados");
            }

            List<OrderRecord> testData;

            if (customData != null && !customData.isEmpty()) {
                System.out.println("Creando datos personalizados desde request...");
                testData = createCustomData(customData);
            } else {
                System.out.println("Usando datos de prueba por defecto...");
                testData = createTestData();
            }

            System.out.println("\n=== GUARDANDO DATOS EN MONGODB ===");
            List<OrderRecord> savedRecords = new ArrayList<>();
            testData.forEach(record -> {
                OrderRecord saved = repositoryPort.save(record);
                savedRecords.add(saved);
                System.out.println("Guardado: " + saved.getId() +
                        " | Fecha: " + saved.getDate() +
                        " | Producto: " + saved.getProductName() +
                        " | Cantidad: " + saved.getQuantity());
            });

            System.out.println("Total registros guardados en MongoDB: " + savedRecords.size());

            System.out.println("\n=== VERIFICACIÓN INMEDIATA ===");
            List<OrderRecord> allAfterSave = repositoryPort.findAll();
            System.out.println("Total en MongoDB después de guardar: " + allAfterSave.size());

            analyzeStoreData("STORE-01");

            return ResponseEntity.ok(Map.of(
                    "message", customData != null ?
                            "Datos personalizados insertados correctamente" :
                            "Datos de prueba insertados correctamente",
                    "recordsInserted", testData.size(),
                    "timestamp", LocalDate.now().toString(),
                    "note", "Para eliminar un registro, use DELETE /api/test/clear-data/{id}"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Error al insertar datos de prueba",
                            "message", e.getMessage()
                    ));
        }
    }

    private List<OrderRecord> createCustomData(Map<String, Object> customData) {
        System.out.println("\n=== CREANDO DATOS PERSONALIZADOS ===");

        List<OrderRecord> records = new ArrayList<>();

        try {
            String store = (String) customData.getOrDefault("store", "STORE-01");
            String dateStr = (String) customData.getOrDefault("date", "2025-12-14");
            LocalDate date = LocalDate.parse(dateStr);

            System.out.println("Tienda: " + store);
            System.out.println("Fecha: " + date);

            if (customData.containsKey("products")) {
                List<Map<String, Object>> products = (List<Map<String, Object>>) customData.get("products");

                for (int i = 0; i < products.size(); i++) {
                    Map<String, Object> productData = products.get(i);

                    String orderId = (String) productData.getOrDefault("orderId", "ORD-" + (i + 1));
                    String productId = (String) productData.getOrDefault("productId", "CAF-001");
                    String productName = (String) productData.getOrDefault("productName", "Café Americano");
                    int quantity = ((Number) productData.getOrDefault("quantity", 1)).intValue();
                    double price = ((Number) productData.getOrDefault("price", 4500.00)).doubleValue();

                    records.add(createRecord(store, orderId, productId, productName, quantity, price, date));
                    System.out.println("  Producto " + (i+1) + ": " + productName + " x" + quantity);
                }
            } else {
                records.add(createRecord(store, "ORD-001", "CAF-001", "Café Americano", 2, 9000.00, date));
                System.out.println("  Producto por defecto: Café Americano x2");
            }

        } catch (Exception e) {
            System.out.println("Error creando datos personalizados: " + e.getMessage());
            System.out.println("Creando datos por defecto...");
            records = createTestData();
        }

        System.out.println("Total registros creados: " + records.size());
        System.out.println("=== FIN CREACIÓN DATOS ===\n");
        return records;
    }

    @DeleteMapping("/clear-data")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        try {
            List<OrderRecord> allRecords = repositoryPort.findAll();
            long count = allRecords.size();

            allRecords.forEach(record -> repositoryPort.deleteById(record.getId()));

            return ResponseEntity.ok(Map.of(
                    "message", "Todos los datos han sido eliminados",
                    "recordsDeleted", count
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Error al eliminar datos",
                            "message", e.getMessage()
                    ));
        }
    }

    private void analyzeStoreData(String storeId) {
        List<OrderRecord> storeRecords = repositoryPort.findByStore(storeId);
        System.out.println("\n=== ANÁLISIS TIENDA: " + storeId + " ===");
        System.out.println("Total registros: " + storeRecords.size());

        Map<String, Integer> productQuantities = storeRecords.stream()
                .collect(Collectors.groupingBy(
                        OrderRecord::getProductId,
                        Collectors.summingInt(OrderRecord::getQuantity)
                ));

        System.out.println("Productos vendidos:");
        productQuantities.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> {
                    String productName = storeRecords.stream()
                            .filter(r -> r.getProductId().equals(entry.getKey()))
                            .findFirst()
                            .map(OrderRecord::getProductName)
                            .orElse("Desconocido");
                    System.out.println("  - " + entry.getKey() + " (" + productName + "): " + entry.getValue() + " unidades");
                });
    }

    @GetMapping("/check-data")
    public ResponseEntity<Map<String, Object>> checkData() {
        try {
            long totalRecords = repositoryPort.count();
            List<OrderRecord> allRecords = repositoryPort.findAll();

            Map<String, Long> recordsByStore = allRecords.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            OrderRecord::getStore,
                            java.util.stream.Collectors.counting()
                    ));

            Map<String, List<Map<String, Object>>> rankingByStore = allRecords.stream()
                    .collect(Collectors.groupingBy(OrderRecord::getStore))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> calculateStoreRanking(entry.getValue())
                    ));

            return ResponseEntity.ok(Map.of(
                    "totalRecords", totalRecords,
                    "stores", recordsByStore,
                    "rankingByStore", rankingByStore,
                    "sampleData", allRecords.stream()
                            .limit(5)
                            .map(record -> Map.of(
                                    "id", record.getId(),
                                    "store", record.getStore(),
                                    "product", record.getProductName(),
                                    "quantity", record.getQuantity(),
                                    "date", record.getDate()
                            ))
                            .toList()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Error al verificar datos",
                            "message", e.getMessage()
                    ));
        }
    }

    private List<Map<String, Object>> calculateStoreRanking(List<OrderRecord> storeRecords) {
        Map<String, Map<String, Object>> productData = storeRecords.stream()
                .collect(Collectors.groupingBy(
                        OrderRecord::getProductId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                records -> {
                                    int totalSold = records.stream()
                                            .mapToInt(OrderRecord::getQuantity)
                                            .sum();
                                    BigDecimal revenue = records.stream()
                                            .map(OrderRecord::getTotalPrice)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    String productName = records.get(0).getProductName();

                                    return Map.of(
                                            "productId", records.get(0).getProductId(),
                                            "productName", productName,
                                            "totalSold", totalSold,
                                            "totalRevenue", revenue.doubleValue()
                                    );
                                }
                        )
                ));

        return productData.values().stream()
                .sorted((a, b) -> Integer.compare(
                        (int) b.get("totalSold"),
                        (int) a.get("totalSold")
                ))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<OrderRecord> createTestData() {
        System.out.println("\n=== CREANDO DATOS DE PRUEBA ===");

        LocalDate dateToday = LocalDate.of(2025, 12, 14);
        LocalDate dateYesterday = LocalDate.of(2025, 12, 13);
        LocalDate dateYouQuery = LocalDate.of(2025, 12, 12);
        LocalDate dateLastWeek = LocalDate.of(2025, 12, 7);
        LocalDate dateLastMonth = LocalDate.of(2025, 11, 14);

        System.out.println("Fecha hoy: " + dateToday);
        System.out.println("Fecha ayer: " + dateYesterday);
        System.out.println("Fecha consulta (2025-12-12): " + dateYouQuery);
        System.out.println("Fecha semana pasada: " + dateLastWeek);
        System.out.println("Fecha mes pasado: " + dateLastMonth);

        List<OrderRecord> records = List.of(
                createRecord("STORE-01", "ORD-008", "CAF-001", "Café Americano", 4, 18000.00, dateYouQuery),
                createRecord("STORE-01", "ORD-001", "CAF-001", "Café Americano", 2, 9000.00, dateToday),
                createRecord("STORE-01", "ORD-003", "CAF-001", "Café Americano", 3, 13500.00, dateYesterday),
                createRecord("STORE-01", "ORD-007", "CAF-001", "Café Americano", 5, 22500.00, dateLastMonth),
                createRecord("STORE-01", "ORD-009", "SNK-002", "Torta de Chocolate", 2, 16000.00, dateYouQuery),
                createRecord("STORE-01", "ORD-006", "SNK-002", "Torta de Chocolate", 1, 8000.00, dateLastWeek),
                createRecord("STORE-01", "ORD-005", "CAF-003", "Latte", 2, 16000.00, dateLastWeek),
                createRecord("STORE-01", "ORD-002", "CAF-002", "Capuchino", 1, 12000.00, dateToday),
                createRecord("STORE-01", "ORD-004", "LCH-001", "Almuerzo Ejecutivo", 1, 25000.00, dateYesterday),
                createRecord("STORE-01", "ORD-001", "SNK-001", "Croissant", 1, 5000.00, dateToday),
                createRecord("STORE-02", "ORD-101", "PAP-001", "Cuaderno Universitario", 5, 25000.00, dateToday),
                createRecord("STORE-02", "ORD-104", "PAP-003", "Lápices HB", 12, 6000.00, dateLastWeek),
                createRecord("STORE-02", "ORD-103", "IMP-001", "Impresión Color", 10, 5000.00, dateYesterday),
                createRecord("STORE-02", "ORD-102", "PAP-002", "Paquete de Hojas", 2, 12000.00, dateToday),
                createRecord("STORE-02", "ORD-105", "PAP-004", "Resaltadores", 3, 9000.00, dateLastMonth),
                createRecord("CAFE-LEYENDA", "ORD-201", "CAF-001", "Café Americano", 3, 13500.00, dateToday),
                createRecord("CAFE-LEYENDA", "ORD-202", "SNK-003", "Empanada de Carne", 2, 10000.00, dateToday),
                createRecord("CAFE-LEYENDA", "ORD-204", "SNK-004", "Jugo Natural", 2, 12000.00, dateLastWeek),
                createRecord("CAFE-LEYENDA", "ORD-203", "CAF-004", "Mocaccino", 1, 14000.00, dateYesterday)
        );

        System.out.println("Total registros creados: " + records.size());
        System.out.println("=== FIN CREACIÓN DATOS ===\n");

        return records;
    }

    private OrderRecord createRecord(String store, String orderId, String productId,
                                     String productName, int quantity, double price, LocalDate date) {
        return OrderRecord.builder()
                .store(store)
                .orderId(orderId)
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .totalPrice(BigDecimal.valueOf(price))
                .date(date)
                .build();
    }

    @GetMapping("/test-repository-methods")
    public ResponseEntity<Map<String, Object>> testRepositoryMethods() {
        try {
            LocalDate targetDate = LocalDate.of(2025, 12, 12);
            String store = "STORE-01";

            List<OrderRecord> method1 = null;
            List<OrderRecord> method2 = null;
            List<OrderRecord> method3 = null;

            try {
                method1 = repositoryPort.findByStoreAndDate(store, targetDate);
                System.out.println("findByStoreAndDate: " + method1.size() + " records");
            } catch (Exception e) {
                System.out.println("ERROR en findByStoreAndDate: " + e.getMessage());
            }

            try {
                method2 = repositoryPort.findByStoreAndDateBetween(store, targetDate, targetDate.plusDays(1));
                System.out.println("findByStoreAndDateBetween (con plusDays): " + method2.size() + " records");
            } catch (Exception e) {
                System.out.println("ERROR en findByStoreAndDateBetween: " + e.getMessage());
            }

            try {
                method3 = repositoryPort.findByStore(store);
                System.out.println("findByStore (todos): " + method3.size() + " records");
            } catch (Exception e) {
                System.out.println("ERROR en findByStore: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                    "findByStoreAndDate", Map.of(
                            "count", method1 != null ? method1.size() : 0,
                            "records", method1 != null ? method1.stream()
                                    .map(r -> Map.of("product", r.getProductName(), "date", r.getDate()))
                                    .collect(Collectors.toList()) : List.of()
                    ),
                    "findByStoreAndDateBetween", Map.of(
                            "count", method2 != null ? method2.size() : 0
                    ),
                    "findByStore_all", Map.of(
                            "count", method3 != null ? method3.size() : 0,
                            "dates", method3 != null ? method3.stream()
                                    .map(r -> r.getDate().toString())
                                    .distinct()
                                    .collect(Collectors.toList()) : List.of()
                    )
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test-product-ranking")
    public ResponseEntity<Map<String, Object>> testProductRanking(
            @RequestParam(required = false, defaultValue = "STORE-01") String storeId) {

        try {
            System.out.println("\n=== TEST PRODUCT RANKING ===");
            System.out.println("Tienda: " + storeId);

            List<OrderRecord> allRecords = repositoryPort.findByStore(storeId);

            if (allRecords.isEmpty()) {
                System.out.println("NO HAY DATOS para la tienda: " + storeId);
                return ResponseEntity.ok(Map.of(
                        "store", storeId,
                        "message", "No hay datos para esta tienda",
                        "ranking", List.of()
                ));
            }

            Map<String, Map<String, Object>> productStats = allRecords.stream()
                    .collect(Collectors.groupingBy(
                            OrderRecord::getProductId,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    records -> {
                                        int totalSold = records.stream()
                                                .mapToInt(OrderRecord::getQuantity)
                                                .sum();
                                        BigDecimal revenue = records.stream()
                                                .map(OrderRecord::getTotalPrice)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        String productName = records.get(0).getProductName();

                                        return Map.of(
                                                "productName", productName,
                                                "totalSold", totalSold,
                                                "revenue", revenue.doubleValue(),
                                                "records", records.size()
                                        );
                                    }
                            )
                    ));

            List<Map<String, Object>> ranking = productStats.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(
                            (int) b.getValue().get("totalSold"),
                            (int) a.getValue().get("totalSold")
                    ))
                    .map(entry -> Map.of(
                            "productId", entry.getKey(),
                            "productName", entry.getValue().get("productName"),
                            "totalSold", entry.getValue().get("totalSold"),
                            "revenue", entry.getValue().get("revenue"),
                            "records", entry.getValue().get("records")
                    ))
                    .collect(Collectors.toList());

            System.out.println("Ranking calculado para " + storeId + ":");
            for (int i = 0; i < ranking.size(); i++) {
                Map<String, Object> product = ranking.get(i);
                System.out.println((i + 1) + ". " + product.get("productName") +
                        " - Vendidos: " + product.get("totalSold") +
                        " - Revenue: $" + product.get("revenue"));
            }

            return ResponseEntity.ok(Map.of(
                    "store", storeId,
                    "totalRecords", allRecords.size(),
                    "totalProducts", productStats.size(),
                    "ranking", ranking
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Error al calcular ranking",
                            "message", e.getMessage()
                    ));
        }
    }
}