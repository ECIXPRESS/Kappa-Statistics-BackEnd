package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.WeeklySalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ApplicationException;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ErrorCodes;
import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateWeeklySalesUseCase {

    private final OrderRecordRepositoryPort repositoryPort;

    public WeeklySalesReport execute(GenerateWeeklyReportCommand command) {
        try {
            validateCommand(command);

            LocalDate anyDateInWeek = command.anyDateInWeek();
            String store = command.storeId();

            if (anyDateInWeek.isAfter(LocalDate.now())) {
                throw ApplicationException.validation(
                        "Cannot generate report for future week",
                        ErrorCodes.FUTURE_DATE,
                        Map.of("requestedDate", anyDateInWeek, "currentDate", LocalDate.now())
                );
            }

            LocalDate start = anyDateInWeek.with(DayOfWeek.MONDAY);
            LocalDate end = anyDateInWeek.with(DayOfWeek.SUNDAY);

            List<OrderRecord> records = repositoryPort.findByStoreAndDateBetween(store, start, end);

            if (records.isEmpty()) {
                Map<String, Object> details = new HashMap<>();
                details.put("storeId", store);
                details.put("weekStart", start);
                details.put("weekEnd", end);
                details.put("requestedDate", anyDateInWeek);

                throw ApplicationException.notFound(
                        String.format("No sales data found for store '%s' in week %s to %s",
                                store, start, end),
                        ErrorCodes.NO_SALES_DATA,
                        details
                );
            }

            int totalOrders = calculateTotalOrders(records);
            int totalProducts = calculateTotalProducts(records);
            BigDecimal totalRevenue = calculateTotalRevenue(records);

            return new WeeklySalesReport(
                    store,
                    start,
                    end,
                    totalOrders,
                    totalProducts,
                    totalRevenue
            );

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw ApplicationException.technical(
                    String.format("Failed to generate weekly report for store '%s' for week containing %s",
                            command.storeId(), command.anyDateInWeek()),
                    ErrorCodes.REPORT_GENERATION_FAILED,
                    e
            );
        }
    }

    private void validateCommand(GenerateWeeklyReportCommand command) {
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

        if (command.anyDateInWeek() == null) {
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