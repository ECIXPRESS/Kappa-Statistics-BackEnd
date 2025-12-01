package edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Web;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.*;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.StatsServices.ExcelAllStatisticsService;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor

public class StatisticsController {

    private final GenerateDailySalesUseCase daily;
    private final GenerateSummaryUseCase summary;
    private final GenerateProductRankingUseCase topProducts;
    private final GenerateWeeklySalesUseCase weekly;
    private final GenerateMonthlySalesUseCase monthly;
    private final ExcelAllStatisticsService excelAllService;


    @Operation(
            summary = "Obtener ventas diarias",
            description = "Genera un reporte de ventas para una fecha específica y una tienda dada.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte diario generado",
                            content = @Content(
                                    schema = @Schema(implementation = DailySalesReport.class),
                                    examples = @ExampleObject(
                                            name = "Ejemplo",
                                            summary = "Ejemplo de reporte diario",
                                            value = """
                                                    {
                                                      "storeId": "STORE-01",
                                                      "date": "2025-01-10",
                                                      "totalOrders": 32,
                                                      "totalProductsSold": 84,
                                                      "totalRevenue": 250000.50
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/daily")
    public DailySalesReport getDaily(
            @Parameter(
                    description = "Fecha del reporte (YYYY-MM-DD)",
                    example = "2025-01-10"
            )
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @Parameter(
                    description = "Identificador de la tienda",
                    example = "STORE-01"
            )
            @RequestParam("store")
            String store
    ) {
        return daily.generate(date, store);
    }

    @Operation(
            summary = "Obtener ventas semanales",
            description = "Genera un reporte semanal basado en cualquier fecha dentro de la semana.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte semanal generado",
                            content = @Content(
                                    schema = @Schema(implementation = WeeklySalesReport.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "storeId": "STORE-01",
                                                      "weekStart": "2025-01-06",
                                                      "weekEnd": "2025-01-12",
                                                      "totalOrders": 240,
                                                      "totalProductsSold": 650,
                                                      "totalRevenue": 1900000.75
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/weekly")
    public WeeklySalesReport getWeekly(
            @Parameter(description = "Una fecha dentro de la semana", example = "2025-01-10")
            @RequestParam String date,

            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam String store
    ) {
        LocalDate parsedDate = LocalDate.parse(date);
        return weekly.generateWeekly(parsedDate, store);
    }


    @Operation(
            summary = "Obtener ventas mensuales",
            description = "Genera un reporte mensual para un año y mes específicos.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte mensual generado",
                            content = @Content(
                                    schema = @Schema(implementation = MonthlySalesReport.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "storeId": "STORE-01",
                                                      "year": 2025,
                                                      "month": 1,
                                                      "totalOrders": 1120,
                                                      "totalProductsSold": 3100,
                                                      "totalRevenue": 8400000.00
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/monthly")
    public MonthlySalesReport getMonthly(
            @Parameter(description = "Año del reporte", example = "2025")
            @RequestParam int year,

            @Parameter(description = "Mes del reporte", example = "1")
            @RequestParam int month,

            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam String store
    ) {
        return monthly.generateMonthlySalesReport(year, month, store);
    }

    @Operation(
            summary = "Obtener resumen general",
            description = "Genera un resumen estadístico general de una tienda.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Resumen generado",
                            content = @Content(
                                    schema = @Schema(implementation = SummaryReport.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "storeId": "STORE-01",
                                                      "totalOrders": 8231,
                                                      "totalProductsSold": 21432,
                                                      "totalRevenue": 59000000.00
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/summary")
    public SummaryReport getSummary(
            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam String store
    ) {
        return summary.generate(store);
    }


    @Operation(
            summary = "Obtener ranking de productos",
            description = "Devuelve una lista de productos con mayor cantidad de ventas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ranking generado",
                            content = @Content(
                                    schema = @Schema(implementation = ProductSalesReport.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    [
                                                      {
                                                        "productId": "CAF-001",
                                                        "productName": "Capuchino",
                                                        "quantitySold": 240,
                                                        "totalRevenue": 720000.00
                                                      },
                                                      {
                                                        "productId": "CAF-002",
                                                        "productName": "Latte",
                                                        "quantitySold": 180,
                                                        "totalRevenue": 540000.00
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    )
            }
    )

    @GetMapping("/top-products")
    public List<ProductSalesReport> getTopProducts(
            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam String store
    ) {
        return topProducts.generateTopProducts(store);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAllStatistics(@RequestParam String store) {
        byte [] excel = excelAllService.generateAllStatistics(store);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=all-statistics.xlsx")
                .body(excel);

    }


}
