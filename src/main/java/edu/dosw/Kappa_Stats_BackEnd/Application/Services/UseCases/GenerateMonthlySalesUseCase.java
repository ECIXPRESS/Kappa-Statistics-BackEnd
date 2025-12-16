package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;
import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.MonthlySalesReport;
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
public class GenerateMonthlySalesUseCase {

    private final OrderRecordRepositoryPort repositoryPort;

    public MonthlySalesReport execute(GenerateMonthlyReportCommand command) {
        try {
            validateCommand(command);

            String store = command.storeId();
            int year = command.year();
            int month = command.month();

            LocalDate now = LocalDate.now();
            if (year > now.getYear() || (year == now.getYear() && month > now.getMonthValue())) {
                throw ApplicationException.validation(
                        "Cannot generate report for future month",
                        ErrorCodes.FUTURE_DATE,
                        Map.of(
                                "requestedYear", year,
                                "requestedMonth", month,
                                "currentYear", now.getYear(),
                                "currentMonth", now.getMonthValue()
                        )
                );
            }

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<OrderRecord> records = repositoryPort.findByStoreAndDateBetween(store, start, end);

            if (records.isEmpty()) {
                Map<String, Object> details = new HashMap<>();
                details.put("storeId", store);
                details.put("year", year);
                details.put("month", month);
                details.put("period", start + " to " + end);

                throw ApplicationException.notFound(
                        String.format("No sales data found for store '%s' in %d-%02d", store, year, month),
                        ErrorCodes.NO_SALES_DATA,
                        details
                );
            }

            int totalOrders = calculateTotalOrders(records);
            int totalProductsSold = calculateTotalProducts(records);
            BigDecimal totalRevenue = calculateTotalRevenue(records);

            return new MonthlySalesReport(
                    store,
                    year,
                    month,
                    totalOrders,
                    totalProductsSold,
                    totalRevenue
            );

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw ApplicationException.technical(
                    String.format("Failed to generate monthly report for store '%s' in %d-%02d",
                            command.storeId(), command.year(), command.month()),
                    ErrorCodes.REPORT_GENERATION_FAILED,
                    e
            );
        }
    }

    private void validateCommand(GenerateMonthlyReportCommand command) {
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

        if (command.year() < 2023 || command.year() > LocalDate.now().getYear() + 1) {
            throw ApplicationException.validation(
                    String.format("Invalid year: %d. Year must be between 2023 and %d",
                            command.year(), LocalDate.now().getYear() + 1),
                    ErrorCodes.INVALID_YEAR
            );
        }

        if (command.month() < 1 || command.month() > 12) {
            throw ApplicationException.validation(
                    String.format("Invalid month: %d. Month must be between 1 and 12", command.month()),
                    ErrorCodes.INVALID_MONTH
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