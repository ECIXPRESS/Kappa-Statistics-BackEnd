package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ApplicationException;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ErrorCodes;
import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateProductRankingUseCase {

    private final OrderRecordRepositoryPort repositoryPort;

    public List<ProductSalesReport> execute(GenerateTopProductsCommand command) {
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

            Map<String, List<OrderRecord>> groupedByProduct = allRecords.stream()
                    .collect(Collectors.groupingBy(OrderRecord::getProductId));

            List<ProductSalesReport> result = new ArrayList<>();

            for (Map.Entry<String, List<OrderRecord>> entry : groupedByProduct.entrySet()) {
                String productId = entry.getKey();
                List<OrderRecord> productRecords = entry.getValue();

                int totalSold = productRecords.stream()
                        .mapToInt(OrderRecord::getQuantity)
                        .sum();

                BigDecimal revenue = productRecords.stream()
                        .map(OrderRecord::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                String productName = productRecords.get(0).getProductName();

                result.add(new ProductSalesReport(
                        store,
                        productId,
                        totalSold,
                        revenue,
                        productName
                ));
            }

            return result.stream()
                    .sorted(Comparator.comparing(ProductSalesReport::totalSold).reversed())
                    .collect(Collectors.toList());

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw ApplicationException.technical(
                    String.format("Failed to generate product ranking for store '%s'",
                            command.storeId()),
                    ErrorCodes.REPORT_GENERATION_FAILED,
                    e
            );
        }
    }

    private void validateCommand(GenerateTopProductsCommand command) {
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
}