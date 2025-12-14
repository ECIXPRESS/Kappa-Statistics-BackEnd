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

            List<OrderRecord> records = repositoryPort.findByStoreAndDateBetween(
                    store, date, date
            );

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