package edu.dosw.Kappa_Stats_BackEnd;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.*;
import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.StatsServices.ExcelAllStatisticsService;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.*;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Exception.ApplicationException;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Web.StatisticsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private GenerateDailySalesUseCase dailyUseCase;

    @Mock
    private GenerateSummaryUseCase summaryUseCase;

    @Mock
    private GenerateProductRankingUseCase topProductsUseCase;

    @Mock
    private GenerateWeeklySalesUseCase weeklyUseCase;

    @Mock
    private GenerateMonthlySalesUseCase monthlyUseCase;

    @Mock
    private ExcelAllStatisticsService excelAllService;

    @Mock
    private OrderRecordRepositoryPort repositoryPort;

    @InjectMocks
    private StatisticsController statisticsController;

    private final String TEST_STORE = "STORE-01";
    private final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    private final int TEST_YEAR = 2024;
    private final int TEST_MONTH = 1;

    private DailySalesReport dailyReport;
    private WeeklySalesReport weeklyReport;
    private MonthlySalesReport monthlyReport;
    private SummaryReport summaryReport;
    private ProductSalesReport productReport;

    @BeforeEach
    void setUp() {
        // Crear reportes usando records (así es como están definidos)
        dailyReport = new DailySalesReport(
                TEST_STORE,
                TEST_DATE.toString(),
                32,
                84,
                new BigDecimal("250000.50")
        );

        weeklyReport = new WeeklySalesReport(
                TEST_STORE,
                LocalDate.of(2024, 1, 8),
                LocalDate.of(2024, 1, 14),
                240,
                650,
                new BigDecimal("1900000.75")
        );

        monthlyReport = new MonthlySalesReport(
                TEST_STORE,
                TEST_YEAR,
                TEST_MONTH,
                1120,
                3100,
                new BigDecimal("8400000.00")
        );

        productReport = new ProductSalesReport(
                TEST_STORE,
                "CAF-001",
                240,
                new BigDecimal("720000.00"),
                "Capuchino"
        );

        ProductSalesReport productReport2 = new ProductSalesReport(
                TEST_STORE,
                "CAF-002",
                180,
                new BigDecimal("540000.00"),
                "Latte"
        );

        List<ProductSalesReport> topProducts = Arrays.asList(productReport, productReport2);

        summaryReport = new SummaryReport(
                TEST_STORE,
                8231,
                21432,
                new BigDecimal("59000000.00"),
                topProducts
        );
    }

    @Test
    void getDaily_ShouldReturnDailySalesReport() {
        // Arrange
        when(dailyUseCase.execute(any(GenerateDailyReportCommand.class)))
                .thenReturn(dailyReport);

        // Act
        ResponseEntity<DailySalesReport> response = statisticsController.getDaily(TEST_DATE, TEST_STORE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        DailySalesReport result = response.getBody();
        assertEquals(TEST_STORE, result.store());
        assertEquals(TEST_DATE.toString(), result.date());
        assertEquals(32, result.totalOrders());
        assertEquals(84, result.totalProductsSold());
        assertEquals(new BigDecimal("250000.50"), result.totalRevenue());

        verify(dailyUseCase).execute(any(GenerateDailyReportCommand.class));
    }

    @Test
    void getWeekly_ShouldReturnWeeklySalesReport() {
        // Arrange
        when(weeklyUseCase.execute(any(GenerateWeeklyReportCommand.class)))
                .thenReturn(weeklyReport);

        // Act
        ResponseEntity<WeeklySalesReport> response = statisticsController.getWeekly(TEST_DATE, TEST_STORE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        WeeklySalesReport result = response.getBody();
        assertEquals(TEST_STORE, result.store());
        assertEquals(240, result.totalOrders());
        assertEquals(650, result.totalProductsSold());
        assertEquals(new BigDecimal("1900000.75"), result.totalRevenue());

        verify(weeklyUseCase).execute(any(GenerateWeeklyReportCommand.class));
    }

    @Test
    void getMonthly_ShouldReturnMonthlySalesReport() {
        // Arrange
        when(monthlyUseCase.execute(any(GenerateMonthlyReportCommand.class)))
                .thenReturn(monthlyReport);

        // Act
        ResponseEntity<MonthlySalesReport> response =
                statisticsController.getMonthly(TEST_YEAR, TEST_MONTH, TEST_STORE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        MonthlySalesReport result = response.getBody();
        assertEquals(TEST_STORE, result.store());
        assertEquals(TEST_YEAR, result.year());
        assertEquals(TEST_MONTH, result.month());
        assertEquals(1120, result.totalOrders());

        verify(monthlyUseCase).execute(any(GenerateMonthlyReportCommand.class));
    }

    @Test
    void getSummary_ShouldReturnSummaryReport() {
        // Arrange
        when(summaryUseCase.execute(any(GenerateSummaryCommand.class)))
                .thenReturn(summaryReport);

        // Act
        ResponseEntity<SummaryReport> response = statisticsController.getSummary(TEST_STORE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        SummaryReport result = response.getBody();
        assertEquals(TEST_STORE, result.store());
        assertEquals(8231, result.totalOrders());
        assertEquals(21432, result.totalProductsSold());
        assertNotNull(result.topProducts());
        assertEquals(2, result.topProducts().size());

        verify(summaryUseCase).execute(any(GenerateSummaryCommand.class));
    }

    @Test
    void getTopProducts_ShouldReturnProductRanking() {
        // Arrange
        List<ProductSalesReport> expectedRanking = Arrays.asList(productReport);

        when(topProductsUseCase.execute(any(GenerateTopProductsCommand.class)))
                .thenReturn(expectedRanking);

        // Act
        ResponseEntity<List<ProductSalesReport>> response =
                statisticsController.getTopProducts(TEST_STORE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<ProductSalesReport> result = response.getBody();
        assertEquals(1, result.size());
        assertEquals("CAF-001", result.get(0).productId());
        assertEquals("Capuchino", result.get(0).productName());
        assertEquals(240, result.get(0).totalSold());

        verify(topProductsUseCase).execute(any(GenerateTopProductsCommand.class));
    }

    @Test
    void getTopProducts_ShouldReturnEmptyList_WhenNoProducts() {
        // Arrange
        when(topProductsUseCase.execute(any(GenerateTopProductsCommand.class)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ProductSalesReport>> response =
                statisticsController.getTopProducts(TEST_STORE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void exportAllStatistics_ShouldReturnExcelFile() {
        // Arrange
        byte[] mockExcelContent = "Mock Excel Content".getBytes();
        when(excelAllService.generateAllStatistics(TEST_STORE))
                .thenReturn(mockExcelContent);

        // Act
        ResponseEntity<byte[]> response = statisticsController.exportAllStatistics(TEST_STORE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(mockExcelContent, response.getBody());

        HttpHeaders headers = response.getHeaders();
        assertNotNull(headers);
        assertTrue(headers.containsKey(HttpHeaders.CONTENT_TYPE));
        assertTrue(headers.containsKey(HttpHeaders.CONTENT_DISPOSITION));
        assertTrue(headers.containsKey(HttpHeaders.CONTENT_LENGTH));

        verify(excelAllService).generateAllStatistics(TEST_STORE);
    }

    @Test
    void exportAllStatistics_ShouldThrowApplicationException_WhenServiceThrowsApplicationException() {
        // Arrange
        ApplicationException expectedException = new ApplicationException(
                "Error generando Excel",
                "EXCEL_ERROR",
                ApplicationException.ErrorType.TECHNICAL
        );

        when(excelAllService.generateAllStatistics(TEST_STORE))
                .thenThrow(expectedException);

        // Act & Assert
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            statisticsController.exportAllStatistics(TEST_STORE);
        });

        assertEquals("Error generando Excel", exception.getMessage());
        assertEquals("EXCEL_ERROR", exception.getErrorCode());
        assertEquals(ApplicationException.ErrorType.TECHNICAL, exception.getErrorType());
    }

    @Test
    void exportAllStatistics_ShouldWrapGenericException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Unexpected error");

        when(excelAllService.generateAllStatistics(TEST_STORE))
                .thenThrow(expectedException);

        // Act & Assert
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            statisticsController.exportAllStatistics(TEST_STORE);
        });

        assertTrue(exception.getMessage().contains("Error al generar el archivo Excel"));
        assertEquals("EXCEL_GENERATION_ERROR", exception.getErrorCode());
        assertEquals(ApplicationException.ErrorType.TECHNICAL, exception.getErrorType());
        assertSame(expectedException, exception.getCause());
    }

    @Test
    void healthCheck_ShouldReturnStatusUp() {
        // Act
        ResponseEntity<Map<String, String>> response = statisticsController.healthCheck();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, String> body = response.getBody();
        assertEquals("UP", body.get("status"));
        assertEquals("Statistics Microservice", body.get("service"));
        assertNotNull(body.get("timestamp"));

        // Verificar que el timestamp es una fecha válida
        assertDoesNotThrow(() -> LocalDate.parse(body.get("timestamp")));
    }

    @Test
    void debugData_ShouldReturnDebugInformation() {
        // Arrange
        LocalDate debugDate = LocalDate.of(2024, 1, 10);

        OrderRecord record1 = OrderRecord.builder()
                .id("1")
                .store(TEST_STORE)
                .productName("Product 1")
                .quantity(2)
                .totalPrice(new BigDecimal("100.00"))
                .date(debugDate)
                .build();

        OrderRecord record2 = OrderRecord.builder()
                .id("2")
                .store(TEST_STORE)
                .productName("Product 2")
                .quantity(3)
                .totalPrice(new BigDecimal("150.00"))
                .date(debugDate)
                .build();

        List<OrderRecord> allRecords = Arrays.asList(record1, record2);
        List<OrderRecord> dateRecords = Arrays.asList(record1, record2);

        when(repositoryPort.findAll()).thenReturn(allRecords);
        when(repositoryPort.findByStoreAndDateBetween(TEST_STORE, debugDate, debugDate))
                .thenReturn(dateRecords);

        // Act
        ResponseEntity<Map<String, Object>> response =
                statisticsController.debugData(TEST_STORE, debugDate);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(TEST_STORE, body.get("store"));
        assertEquals(debugDate, body.get("date"));
        assertEquals(2, body.get("totalRecords"));
        assertEquals(2, body.get("recordsForStore"));
        assertEquals(2, body.get("recordsForStoreAndDate"));
        assertEquals(2, body.get("viaFindByStoreAndDateBetween"));

        // Verificar que sampleRecords es una lista
        Object sampleRecordsObj = body.get("sampleRecords");
        assertNotNull(sampleRecordsObj);
        assertTrue(sampleRecordsObj instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sampleRecords = (List<Map<String, Object>>) sampleRecordsObj;
        assertEquals(2, sampleRecords.size());
        assertEquals("1", sampleRecords.get(0).get("id"));
        assertEquals("Product 1", sampleRecords.get(0).get("product"));

        verify(repositoryPort).findAll();
        verify(repositoryPort).findByStoreAndDateBetween(TEST_STORE, debugDate, debugDate);
    }

    @Test
    void debugData_ShouldHandleEmptyData() {
        // Arrange
        LocalDate debugDate = LocalDate.of(2024, 1, 10);

        when(repositoryPort.findAll()).thenReturn(Collections.emptyList());
        when(repositoryPort.findByStoreAndDateBetween(TEST_STORE, debugDate, debugDate))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Map<String, Object>> response =
                statisticsController.debugData(TEST_STORE, debugDate);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(TEST_STORE, body.get("store"));
        assertEquals(debugDate, body.get("date"));
        assertEquals(0, body.get("totalRecords"));
        assertEquals(0, body.get("recordsForStore"));
        assertEquals(0, body.get("recordsForStoreAndDate"));
        assertEquals(0, body.get("viaFindByStoreAndDateBetween"));

        // Verificar que sampleRecords es una lista vacía
        Object sampleRecordsObj = body.get("sampleRecords");
        assertNotNull(sampleRecordsObj);
        assertTrue(sampleRecordsObj instanceof List);
        assertTrue(((List<?>) sampleRecordsObj).isEmpty());
    }

    @Test
    void testControllerAnnotations() {
        // Verificar que las anotaciones del controller están presentes
        assertNotNull(statisticsController);

        Class<?> controllerClass = statisticsController.getClass();
        assertNotNull(controllerClass.getAnnotation(org.springframework.web.bind.annotation.RestController.class));
        assertNotNull(controllerClass.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class));
    }

    @Test
    void testConstructorInjection() {
        // Verificar que las dependencias se inyectan correctamente
        assertNotNull(statisticsController);
        assertNotNull(dailyUseCase);
        assertNotNull(summaryUseCase);
        assertNotNull(topProductsUseCase);
        assertNotNull(weeklyUseCase);
        assertNotNull(monthlyUseCase);
        assertNotNull(excelAllService);
        assertNotNull(repositoryPort);
    }

    @Test
    void testAllArgsConstructorAnnotation() {
        // Verificar que el controller tiene @RequiredArgsConstructor
        StatisticsController controller = new StatisticsController(
                dailyUseCase,
                summaryUseCase,
                topProductsUseCase,
                weeklyUseCase,
                monthlyUseCase,
                excelAllService,
                repositoryPort
        );

        assertNotNull(controller);
    }
}