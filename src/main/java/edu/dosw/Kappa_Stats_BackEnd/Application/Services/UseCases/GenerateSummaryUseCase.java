package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.SummaryReport;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateSummaryUseCase {

    private final OrderRecordRepository repository;
    private final GenerateProductRankingUseCase rankingUseCase;

    public SummaryReport generate() {

        List<OrderRecord> all = repository.findAll();

        int totalOrders = all.size();
        int totalProducts = all.stream().mapToInt(OrderRecord::getQuantity).sum();

        BigDecimal totalRevenue = all.stream()
                .map(OrderRecord::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ProductSalesReport> topProducts = rankingUseCase.generateTopProducts();

        return new SummaryReport(
                totalOrders,
                totalProducts,
                totalRevenue,
                topProducts
        );
    }
}
