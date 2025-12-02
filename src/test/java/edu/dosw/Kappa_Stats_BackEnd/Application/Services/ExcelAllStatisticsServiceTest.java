package edu.dosw.Kappa_Stats_BackEnd.Application.Services;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.*;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.StatsServices.ExcelAllStatisticsService;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelAllStatisticsServiceTest {

    @Mock
    private GenerateDailySalesUseCase dailyUseCase;

    @Mock
    private GenerateWeeklySalesUseCase weeklyUseCase;

    @Mock
    private GenerateMonthlySalesUseCase monthlyUseCase;

    @Mock
    private GenerateSummaryUseCase summaryUseCase;

    @Mock
    private GenerateProductRankingUseCase topProductsUseCase;

    @InjectMocks
    private ExcelAllStatisticsService service;

    private String testStore;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testStore = "STORE-01";
        testDate = LocalDate.now();
    }

    @Test
    void generateAllStatistics_CreatesWorkbookWithAllSheets() throws Exception {
        // Arrange
        DailySalesReport dailyReport = new DailySalesReport(
                testStore, testDate.toString(), 10, 50, new BigDecimal("1000.00")
        );
        WeeklySalesReport weeklyReport = new WeeklySalesReport(
                testStore, testDate, testDate.plusDays(6), 70, 350, new BigDecimal("7000.00")
        );
        MonthlySalesReport monthlyReport = new MonthlySalesReport(
                testStore, 2025, 1, 300, 1500, new BigDecimal("30000.00")
        );
        List<ProductSalesReport> topProducts = Arrays.asList(
                new ProductSalesReport(testStore, "PROD-001", 100, new BigDecimal("1000.00"), "Coffee")
        );
        SummaryReport summaryReport = new SummaryReport(
                testStore, 1000, 5000, new BigDecimal("100000.00"), topProducts
        );

        when(dailyUseCase.generate(any(LocalDate.class), eq(testStore))).thenReturn(dailyReport);
        when(weeklyUseCase.generateWeekly(any(LocalDate.class), eq(testStore))).thenReturn(weeklyReport);
        when(monthlyUseCase.generateMonthlySalesReport(anyInt(), anyInt(), eq(testStore)))
                .thenReturn(monthlyReport);
        when(summaryUseCase.generate(testStore)).thenReturn(summaryReport);
        when(topProductsUseCase.generateTopProducts(testStore)).thenReturn(topProducts);

        // Act
        byte[] result = service.generateAllStatistics(testStore);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify the workbook structure
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertEquals(5, workbook.getNumberOfSheets());

            assertNotNull(workbook.getSheet("Daily Report"));
            assertNotNull(workbook.getSheet("Weekly Report"));
            assertNotNull(workbook.getSheet("Monthly Report"));
            assertNotNull(workbook.getSheet("Summary"));
            assertNotNull(workbook.getSheet("Top Products"));
        }

        verify(dailyUseCase, times(1)).generate(any(LocalDate.class), eq(testStore));
        verify(weeklyUseCase, times(1)).generateWeekly(any(LocalDate.class), eq(testStore));
        verify(monthlyUseCase, times(1)).generateMonthlySalesReport(anyInt(), anyInt(), eq(testStore));
        verify(summaryUseCase, times(1)).generate(testStore);
        verify(topProductsUseCase, times(1)).generateTopProducts(testStore);
    }

    @Test
    void generateAllStatistics_DailySheetHasCorrectStructure() throws Exception {
        // Arrange
        DailySalesReport dailyReport = new DailySalesReport(
                testStore, testDate.toString(), 32, 84, new BigDecimal("250000.50")
        );

        when(dailyUseCase.generate(any(LocalDate.class), eq(testStore))).thenReturn(dailyReport);
        when(weeklyUseCase.generateWeekly(any(), eq(testStore))).thenReturn(
                new WeeklySalesReport(testStore, testDate, testDate, 0, 0, BigDecimal.ZERO)
        );
        when(monthlyUseCase.generateMonthlySalesReport(anyInt(), anyInt(), eq(testStore)))
                .thenReturn(new MonthlySalesReport(testStore, 2025, 1, 0, 0, BigDecimal.ZERO));
        when(summaryUseCase.generate(testStore))
                .thenReturn(new SummaryReport(testStore, 0, 0, BigDecimal.ZERO, List.of()));
        when(topProductsUseCase.generateTopProducts(testStore)).thenReturn(List.of());

        // Act
        byte[] result = service.generateAllStatistics(testStore);

        // Assert
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet dailySheet = workbook.getSheet("Daily Report");
            assertNotNull(dailySheet);

            // Check title row
            assertEquals("Daily Sales Report",
                    dailySheet.getRow(0).getCell(0).getStringCellValue());

            // Check data rows exist
            assertNotNull(dailySheet.getRow(2)); // Store ID row
            assertNotNull(dailySheet.getRow(3)); // Date row
            assertNotNull(dailySheet.getRow(4)); // Total Orders row
            assertNotNull(dailySheet.getRow(5)); // Products Sold row
        }
    }

    @Test
    void generateAllStatistics_TopProductsSheetHasHeader() throws Exception {
        // Arrange
        List<ProductSalesReport> products = Arrays.asList(
                new ProductSalesReport(testStore, "PROD-001", 100, new BigDecimal("1000.00"), "Coffee"),
                new ProductSalesReport(testStore, "PROD-002", 50, new BigDecimal("500.00"), "Tea")
        );

        when(dailyUseCase.generate(any(), eq(testStore)))
                .thenReturn(new DailySalesReport(testStore, testDate.toString(), 0, 0, BigDecimal.ZERO));
        when(weeklyUseCase.generateWeekly(any(), eq(testStore)))
                .thenReturn(new WeeklySalesReport(testStore, testDate, testDate, 0, 0, BigDecimal.ZERO));
        when(monthlyUseCase.generateMonthlySalesReport(anyInt(), anyInt(), eq(testStore)))
                .thenReturn(new MonthlySalesReport(testStore, 2025, 1, 0, 0, BigDecimal.ZERO));
        when(summaryUseCase.generate(testStore))
                .thenReturn(new SummaryReport(testStore, 0, 0, BigDecimal.ZERO, products));
        when(topProductsUseCase.generateTopProducts(testStore)).thenReturn(products);

        // Act
        byte[] result = service.generateAllStatistics(testStore);

        // Assert
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet topProductsSheet = workbook.getSheet("Top Products");
            assertNotNull(topProductsSheet);

            Row headerRow = topProductsSheet.getRow(0);
            assertEquals("Product ID", headerRow.getCell(0).getStringCellValue());
            assertEquals("Product Name", headerRow.getCell(1).getStringCellValue());
            assertEquals("Quantity Sold", headerRow.getCell(2).getStringCellValue());
            assertEquals("Revenue", headerRow.getCell(3).getStringCellValue());

            // Check product data rows
            Row dataRow1 = topProductsSheet.getRow(1);
            assertNotNull(dataRow1);
            assertEquals("PROD-001", dataRow1.getCell(0).getStringCellValue());

            Row dataRow2 = topProductsSheet.getRow(2);
            assertNotNull(dataRow2);
            assertEquals("PROD-002", dataRow2.getCell(0).getStringCellValue());
        }
    }

    @Test
    void generateAllStatistics_WithEmptyData_CreatesValidWorkbook() throws Exception {
        // Arrange
        when(dailyUseCase.generate(any(), eq(testStore)))
                .thenReturn(new DailySalesReport(testStore, testDate.toString(), 0, 0, BigDecimal.ZERO));
        when(weeklyUseCase.generateWeekly(any(), eq(testStore)))
                .thenReturn(new WeeklySalesReport(testStore, testDate, testDate, 0, 0, BigDecimal.ZERO));
        when(monthlyUseCase.generateMonthlySalesReport(anyInt(), anyInt(), eq(testStore)))
                .thenReturn(new MonthlySalesReport(testStore, 2025, 1, 0, 0, BigDecimal.ZERO));
        when(summaryUseCase.generate(testStore))
                .thenReturn(new SummaryReport(testStore, 0, 0, BigDecimal.ZERO, List.of()));
        when(topProductsUseCase.generateTopProducts(testStore)).thenReturn(List.of());

        // Act
        byte[] result = service.generateAllStatistics(testStore);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertEquals(5, workbook.getNumberOfSheets());
        }
    }

    @Test
    void generateAllStatistics_UsesCurrentDate() throws Exception {
        // Arrange
        when(dailyUseCase.generate(any(), eq(testStore)))
                .thenReturn(new DailySalesReport(testStore, testDate.toString(), 0, 0, BigDecimal.ZERO));
        when(weeklyUseCase.generateWeekly(any(), eq(testStore)))
                .thenReturn(new WeeklySalesReport(testStore, testDate, testDate, 0, 0, BigDecimal.ZERO));
        when(monthlyUseCase.generateMonthlySalesReport(anyInt(), anyInt(), eq(testStore)))
                .thenReturn(new MonthlySalesReport(testStore, 2025, 1, 0, 0, BigDecimal.ZERO));
        when(summaryUseCase.generate(testStore))
                .thenReturn(new SummaryReport(testStore, 0, 0, BigDecimal.ZERO, List.of()));
        when(topProductsUseCase.generateTopProducts(testStore)).thenReturn(List.of());

        // Act
        service.generateAllStatistics(testStore);

        // Assert
        verify(dailyUseCase).generate(any(LocalDate.class), eq(testStore));
        verify(monthlyUseCase).generateMonthlySalesReport(
                eq(LocalDate.now().getYear()),
                eq(LocalDate.now().getMonthValue()),
                eq(testStore)
        );
    }
}