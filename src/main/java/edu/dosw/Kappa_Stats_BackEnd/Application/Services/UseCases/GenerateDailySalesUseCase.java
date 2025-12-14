package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.DailySalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ApplicationException;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ErrorCodes;
import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateDailySalesUseCase {

    private final OrderRecordRepositoryPort repositoryPort;

    public DailySalesReport execute(GenerateDailyReportCommand command) {
        try {
            validateCommand(command);

            LocalDate date = command.date();
            String store = command.storeId();

            if (date.isAfter(LocalDate.now())) {
                throw ApplicationException.validation(
                        "Cannot generate report for future date",
                        ErrorCodes.FUTURE_DATE,
                        Map.of("requestedDate", date, "currentDate", LocalDate.now())
                );
            }

            List<OrderRecord> allRecordsInRange = repositoryPort.findByStoreAndDateBetween(
                    store,
                    date,
                    date.plusDays(1)  // endDate exclusive
            );

            System.out.println("=== DEBUG: TODOS LOS REGISTROS EN RANGO ===");
            System.out.println("Rango: " + date + " a " + date.plusDays(1));
            System.out.println("Total en rango: " + allRecordsInRange.size());

            allRecordsInRange.forEach(r -> {
                System.out.println("  - Orden: " + r.getOrderId() +
                        " | Producto: " + r.getProductName() +
                        " | Cantidad: " + r.getQuantity() +
                        " | Precio: " + r.getTotalPrice() +
                        " | Fecha: " + r.getDate());
            });

            List<OrderRecord> records = allRecordsInRange.stream()
                    .filter(r -> r.getDate().equals(date))
                    .collect(Collectors.toList());

            System.out.println("=== DEBUG: FILTRADOS POR FECHA EXACTA ===");
            System.out.println("Fecha exacta: " + date);
            System.out.println("Total después de filtrar: " + records.size());

            records.forEach(r -> {
                System.out.println("  - Orden: " + r.getOrderId() +
                        " | Producto: " + r.getProductName() +
                        " | Cantidad: " + r.getQuantity() +
                        " | Precio: " + r.getTotalPrice());
            });

            if (records.isEmpty()) {
                Map<String, Object> details = new HashMap<>();
                details.put("storeId", store);
                details.put("date", date);

                throw ApplicationException.notFound(
                        String.format("No sales data found for store '%s' on date %s", store, date),
                        ErrorCodes.NO_SALES_DATA,
                        details
                );
            }

            int totalOrders = calculateTotalOrders(records);
            int totalProducts = calculateTotalProducts(records);
            BigDecimal totalRevenue = calculateTotalRevenue(records);

            System.out.println("=== DEBUG: CÁLCULOS FINALES ===");
            System.out.println("Órdenes distintas: " + totalOrders);
            System.out.println("Productos totales: " + totalProducts);
            System.out.println("Revenue total: " + totalRevenue);

            return new DailySalesReport(
                    store,
                    date.toString(),
                    totalOrders,
                    totalProducts,
                    totalRevenue
            );

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw ApplicationException.technical(
                    String.format("Failed to generate daily report for store '%s' on date %s",
                            command.storeId(), command.date()),
                    ErrorCodes.REPORT_GENERATION_FAILED,
                    e
            );
        }
    }

    private void validateCommand(GenerateDailyReportCommand command) {
        if (command == null) {
            throw ApplicationException.validation(
                    "Command cannot be null",
                    "COMMAND_NULL"
            );
        }

        if (command.storeId() == null || command.storeId().isBlank()) {
            throw ApplicationException.validation(
                    "Store ID cannot be null or empty",
                    ErrorCodes.INVALID_STORE_ID
            );
        }

        if (command.date() == null) {
            throw ApplicationException.validation(
                    "Date cannot be null",
                    "DATE_NULL"
            );
        }
    }

    private int calculateTotalOrders(List<OrderRecord> records) {
        return (int) records.stream()
                .map(OrderRecord::getOrderId)
                .distinct()
                .count();
    }

    private int calculateTotalProducts(List<OrderRecord> records) {
        return records.stream()
                .mapToInt(OrderRecord::getQuantity)
                .sum();
    }

    private BigDecimal calculateTotalRevenue(List<OrderRecord> records) {
        return records.stream()
                .map(OrderRecord::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}