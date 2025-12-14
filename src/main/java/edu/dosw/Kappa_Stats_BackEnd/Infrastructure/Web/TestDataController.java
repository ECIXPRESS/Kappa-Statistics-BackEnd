// File: TestDataController.java
package edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Web;

import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Profile("dev")  // Solo disponible en entorno de desarrollo
@RequiredArgsConstructor
public class TestDataController {

    private final OrderRecordRepositoryPort repositoryPort;

    @PostMapping("/seed-data")
    public ResponseEntity<Map<String, Object>> seedTestData() {
        try {
            List<OrderRecord> existingData = repositoryPort.findAll();
            existingData.forEach(record -> repositoryPort.deleteById(record.getId()));

            List<OrderRecord> testData = createTestData();

            testData.forEach(repositoryPort::save);

            return ResponseEntity.ok(Map.of(
                    "message", "Datos de prueba insertados correctamente",
                    "recordsInserted", testData.size(),
                    "timestamp", LocalDate.now().toString(),
                    "stores", List.of("STORE-01", "STORE-02", "CAFE-LEYENDA")
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Error al insertar datos de prueba",
                            "message", e.getMessage()
                    ));
        }
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

            return ResponseEntity.ok(Map.of(
                    "totalRecords", totalRecords,
                    "stores", recordsByStore,
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

    private List<OrderRecord> createTestData() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);
        LocalDate lastMonth = today.minusMonths(1);

        return List.of(
                createRecord("STORE-01", "ORD-001", "CAF-001", "Café Americano", 2, 9000.00, today),
                createRecord("STORE-01", "ORD-001", "SNK-001", "Croissant", 1, 5000.00, today),
                createRecord("STORE-01", "ORD-002", "CAF-002", "Capuchino", 1, 12000.00, today),
                createRecord("STORE-01", "ORD-003", "CAF-001", "Café Americano", 3, 13500.00, yesterday),
                createRecord("STORE-01", "ORD-004", "LCH-001", "Almuerzo Ejecutivo", 1, 25000.00, yesterday),
                createRecord("STORE-01", "ORD-005", "CAF-003", "Latte", 2, 16000.00, lastWeek),
                createRecord("STORE-01", "ORD-006", "SNK-002", "Torta de Chocolate", 1, 8000.00, lastWeek),
                createRecord("STORE-01", "ORD-007", "CAF-001", "Café Americano", 5, 22500.00, lastMonth),

                createRecord("STORE-02", "ORD-101", "PAP-001", "Cuaderno Universitario", 5, 25000.00, today),
                createRecord("STORE-02", "ORD-102", "PAP-002", "Paquete de Hojas", 2, 12000.00, today),
                createRecord("STORE-02", "ORD-103", "IMP-001", "Impresión Color", 10, 5000.00, yesterday),
                createRecord("STORE-02", "ORD-104", "PAP-003", "Lápices HB", 12, 6000.00, lastWeek),
                createRecord("STORE-02", "ORD-105", "PAP-004", "Resaltadores", 3, 9000.00, lastMonth),

                createRecord("CAFE-LEYENDA", "ORD-201", "CAF-001", "Café Americano", 3, 13500.00, today),
                createRecord("CAFE-LEYENDA", "ORD-202", "SNK-003", "Empanada de Carne", 2, 10000.00, today),
                createRecord("CAFE-LEYENDA", "ORD-203", "CAF-004", "Mocaccino", 1, 14000.00, yesterday),
                createRecord("CAFE-LEYENDA", "ORD-204", "SNK-004", "Jugo Natural", 2, 12000.00, lastWeek)
        );
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
}