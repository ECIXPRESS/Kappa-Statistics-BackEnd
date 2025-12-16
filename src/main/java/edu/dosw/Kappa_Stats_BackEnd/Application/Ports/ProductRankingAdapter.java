package edu.dosw.Kappa_Stats_BackEnd.Application.Ports;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRankingAdapter implements ProductRankingPort {

    private final OrderRecordRepositoryPort orderRecordRepository;

    @Override
    public List<ProductSalesReport> generateTopProducts(String store) {
        log.debug("Generando ranking de productos para tienda: {}", store);

        List<OrderRecord> records = orderRecordRepository.findByStore(store);

        if (records.isEmpty()) {
            log.warn("No hay registros para la tienda: {}", store);
            return List.of();
        }

        Map<String, List<OrderRecord>> groupedByProduct = records.stream()
                .collect(Collectors.groupingBy(OrderRecord::getProductId));

        List<ProductSalesReport> result = groupedByProduct.entrySet().stream()
                .map(entry -> {
                    String productId = entry.getKey();
                    List<OrderRecord> productRecords = entry.getValue();

                    int totalSold = productRecords.stream()
                            .mapToInt(OrderRecord::getQuantity)
                            .sum();

                    BigDecimal revenue = productRecords.stream()
                            .map(OrderRecord::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    String productName = productRecords.get(0).getProductName();

                    return new ProductSalesReport(
                            store,
                            productId,
                            totalSold,
                            revenue,
                            productName
                    );
                })
                .sorted(Comparator.comparing(ProductSalesReport::totalSold).reversed())
                .collect(Collectors.toList());

        log.debug("Generados {} productos en el ranking", result.size());
        return result;
    }
}