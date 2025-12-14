package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.SummaryReport;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ApplicationException;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ErrorCodes;
import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateSummaryUseCase {

    private final OrderRecordRepositoryPort repositoryPort;
    private final GenerateProductRankingUseCase rankingUseCase;

    public SummaryReport execute(GenerateSummaryCommand command) {
        try {
            validateCommand(command);

            String store = command.storeId();
            List<OrderRecord> allRecords = repositoryPort.findByStore(store);

            if (allRecords.isEmpty()) {
                throw ApplicationException.notFound(
                        String.format("No sales data found for store '%s'", store),
                        ErrorCodes.NO_SALES_DATA,
                        Map.of("storeId", store)
                );
            }

            int totalOrders = calculateTotalOrders(allRecords);
            int totalProducts = calculateTotalProducts(allRecords);
            BigDecimal totalRevenue = calculateTotalRevenue(allRecords);

            GenerateTopProductsCommand topProductsCommand = new GenerateTopProductsCommand(store);
            List<ProductSalesReport> topProducts = rankingUseCase.execute(topProductsCommand);

            List<ProductSalesReport> limitedTopProducts = topProducts.stream()
                    .limit(10)
                    .toList();

            return new SummaryReport(
                    store,
                    totalOrders,
                    totalProducts,
                    totalRevenue,
                    limitedTopProducts
            );

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw ApplicationException.technical(
                    String.format("Failed to generate summary report for store '%s'",
                            command.storeId()),
                    ErrorCodes.REPORT_GENERATION_FAILED,
                    e
            );
        }
    }

    private void validateCommand(GenerateSummaryCommand command) {
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