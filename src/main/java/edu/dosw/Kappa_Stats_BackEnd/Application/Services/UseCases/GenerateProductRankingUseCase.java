package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ApplicationException;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ErrorCodes;
import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.ProductRankingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateProductRankingUseCase {

    private final ProductRankingPort productRankingPort;

    public List<ProductSalesReport> execute(GenerateTopProductsCommand command) {
        try {
            validateCommand(command);

            String store = command.storeId();

            List<ProductSalesReport> result = productRankingPort.generateTopProducts(store);

            if (result.isEmpty()) {
                throw ApplicationException.notFound(
                        String.format("No sales data found for store '%s'", store),
                        ErrorCodes.NO_SALES_DATA,
                        Map.of("storeId", store)
                );
            }

            return result;

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