package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateProductRankingUseCase {

    private final OrderRecordRepository repository;

    public List<ProductSalesReport> generateTopProducts() {
        List<OrderRecord> all = repository.findAll();

        Map<String, List<OrderRecord>> grouped = all.stream()
                .collect(Collectors.groupingBy(OrderRecord::getProductId));

        List<ProductSalesReport> result = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            int totalSold = entry.getValue().stream().mapToInt(OrderRecord::getQuantity).sum();
            BigDecimal revenue = entry.getValue().stream()
                    .map(OrderRecord::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new ProductSalesReport(
                    entry.getKey(),
                    totalSold,
                    revenue
            ));
        }

        return result.stream()
                .sorted(Comparator.comparing(ProductSalesReport::totalSold).reversed())
                .toList();
    }
}
