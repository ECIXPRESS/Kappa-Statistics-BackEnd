package edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Web;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.*;
import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.StatsServices.ExcelAllStatisticsService;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.*;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ApplicationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final GenerateDailySalesUseCase dailyUseCase;
    private final GenerateSummaryUseCase summaryUseCase;
    private final GenerateProductRankingUseCase topProductsUseCase;
    private final GenerateWeeklySalesUseCase weeklyUseCase;
    private final GenerateMonthlySalesUseCase monthlyUseCase;
    private final ExcelAllStatisticsService excelAllService;
    private final OrderRecordRepositoryPort repositoryPort;

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
                                                      "store": "STORE-01",
                                                      "date": "2025-01-10",
                                                      "totalOrders": 32,
                                                      "totalProductsSold": 84,
                                                      "totalRevenue": 250000.50
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Parámetros inválidos",
                            content = @Content(
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "timestamp": "2025-01-10T10:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Store ID cannot be null or empty",
                                                      "errorCode": "STATS_003"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No se encontraron datos",
                            content = @Content(
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "timestamp": "2025-01-10T10:30:00",
                                                      "status": 404,
                                                      "error": "Not Found",
                                                      "message": "No sales data found for store 'STORE-01' on date 2025-01-10",
                                                      "errorCode": "STATS_102"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/daily")
    public ResponseEntity<DailySalesReport> getDaily(
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
        GenerateDailyReportCommand command = new GenerateDailyReportCommand(store, date);
        DailySalesReport report = dailyUseCase.execute(command);
        return ResponseEntity.ok(report);
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
                                                      "store": "STORE-01",
                                                      "startDate": "2025-01-06",
                                                      "endDate": "2025-01-12",
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
    public ResponseEntity<WeeklySalesReport> getWeekly(
            @Parameter(description = "Una fecha dentro de la semana", example = "2025-01-10")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam("store") String store
    ) {
        GenerateWeeklyReportCommand command = new GenerateWeeklyReportCommand(store, date);
        WeeklySalesReport report = weeklyUseCase.execute(command);
        return ResponseEntity.ok(report);
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
                                                      "store": "STORE-01",
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
    public ResponseEntity<MonthlySalesReport> getMonthly(
            @Parameter(description = "Año del reporte", example = "2025")
            @RequestParam("year") int year,

            @Parameter(description = "Mes del reporte", example = "1")
            @RequestParam("month") int month,

            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam("store") String store
    ) {
        GenerateMonthlyReportCommand command = new GenerateMonthlyReportCommand(store, year, month);
        MonthlySalesReport report = monthlyUseCase.execute(command);
        return ResponseEntity.ok(report);
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
                                                      "store": "STORE-01",
                                                      "totalOrders": 8231,
                                                      "totalProductsSold": 21432,
                                                      "totalRevenue": 59000000.00,
                                                      "topProducts": [
                                                        {
                                                          "store": "STORE-01",
                                                          "productId": "CAF-001",
                                                          "totalSold": 240,
                                                          "totalRevenue": 720000.00,
                                                          "productName": "Capuchino"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/summary")
    public ResponseEntity<SummaryReport> getSummary(
            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam("store") String store
    ) {
        GenerateSummaryCommand command = new GenerateSummaryCommand(store);
        SummaryReport report = summaryUseCase.execute(command);
        return ResponseEntity.ok(report);
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
                                                        "store": "STORE-01",
                                                        "productId": "CAF-001",
                                                        "totalSold": 240,
                                                        "totalRevenue": 720000.00,
                                                        "productName": "Capuchino"
                                                      },
                                                      {
                                                        "store": "STORE-01",
                                                        "productId": "CAF-002",
                                                        "totalSold": 180,
                                                        "totalRevenue": 540000.00,
                                                        "productName": "Latte"
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/top-products")
    public ResponseEntity<List<ProductSalesReport>> getTopProducts(
            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam("store") String store
    ) {
        GenerateTopProductsCommand command = new GenerateTopProductsCommand(store);
        List<ProductSalesReport> ranking = topProductsUseCase.execute(command);
        return ResponseEntity.ok(ranking);
    }

    @Operation(
            summary = "Exportar todas las estadísticas en Excel",
            description = "Genera y descarga un archivo Excel con todas las estadísticas de la tienda especificada.",
            parameters = {
                    @Parameter(
                            name = "store",
                            description = "Identificador de la tienda",
                            required = true,
                            example = "STORE-01"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Archivo Excel generado exitosamente",
                            content = @Content(
                                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error al generar el archivo Excel",
                            content = @Content(
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "timestamp": "2025-01-10T10:30:00",
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "message": "Error generating Excel",
                                                      "errorCode": "INTERNAL_ERROR"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAllStatistics(
            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam("store") String store
    ) {
        try {
            byte[] excel = excelAllService.generateAllStatistics(store);

            String fileName = String.format("estadisticas-tienda-%s-%s.xlsx",
                    store, LocalDate.now().toString());

            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .header("Content-Length", String.valueOf(excel.length))
                    .body(excel);

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw ApplicationException.technical(
                    "Error al generar el archivo Excel para la tienda: " + store,
                    "EXCEL_GENERATION_ERROR",
                    e
            );
        }
    }

    @Operation(
            summary = "Verificar estado del servicio",
            description = "Endpoint de salud para verificar que el microservicio de estadísticas está funcionando",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Servicio funcionando correctamente"
                    )
            }
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Statistics Microservice",
                "timestamp", LocalDate.now().toString()
        ));
    }

    @Operation(
            summary = "Endpoint de debug para datos",
            description = "Endpoint para debugging que muestra información detallada sobre los registros almacenados",
            hidden = true
    )
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugData(
            @Parameter(description = "Identificador de la tienda", example = "STORE-01")
            @RequestParam("store") String store,

            @Parameter(description = "Fecha para filtrar (YYYY-MM-DD)", example = "2025-01-10")
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        Map<String, Object> response = new HashMap<>();

        List<OrderRecord> allRecords = repositoryPort.findAll();
        List<OrderRecord> forStore = allRecords.stream()
                .filter(r -> r.getStore().equals(store))
                .collect(Collectors.toList());

        List<OrderRecord> forDate = forStore.stream()
                .filter(r -> r.getDate().equals(date))
                .collect(Collectors.toList());

        List<OrderRecord> viaMethod = repositoryPort.findByStoreAndDateBetween(store, date, date);

        response.put("store", store);
        response.put("date", date);
        response.put("totalRecords", allRecords.size());
        response.put("recordsForStore", forStore.size());
        response.put("recordsForStoreAndDate", forDate.size());
        response.put("viaFindByStoreAndDateBetween", viaMethod.size());

        response.put("sampleRecords", forDate.stream()
                .limit(3)
                .map(r -> Map.of(
                        "id", r.getId(),
                        "product", r.getProductName(),
                        "quantity", r.getQuantity(),
                        "totalPrice", r.getTotalPrice(),
                        "date", r.getDate().toString()
                ))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }
}