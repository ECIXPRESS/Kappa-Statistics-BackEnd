package edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Web;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.DailySalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.SummaryReport;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.GenerateDailySalesUseCase;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.GenerateProductRankingUseCase;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.GenerateSummaryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final GenerateDailySalesUseCase daily;
    private final GenerateSummaryUseCase summary;
    private final GenerateProductRankingUseCase topProducts;

    @GetMapping("/daily")
    public DailySalesReport getDaily(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return daily.generate(date);
    }

    @GetMapping("/summary")
    public SummaryReport getSummary() {
        return summary.generate();
    }

    @GetMapping("/top-products")
    public List<ProductSalesReport> getTopProducts() {
        return topProducts.generateTopProducts();
    }
}
