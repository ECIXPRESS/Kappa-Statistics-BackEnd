package edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.*;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.StatsServices.ExcelAllStatisticsService;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.*;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Web.StatisticsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenerateDailySalesUseCase dailyUseCase;

    @MockitoBean
    private GenerateSummaryUseCase summaryUseCase;

    @MockitoBean
    private GenerateProductRankingUseCase topProductsUseCase;

    @MockitoBean
    private GenerateWeeklySalesUseCase weeklyUseCase;

    @MockitoBean
    private GenerateMonthlySalesUseCase monthlyUseCase;

    @MockitoBean
    private ExcelAllStatisticsService excelAllService;

    @Test
    void getDaily_WithValidParameters_ReturnsReport() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2025, 1, 10);
        String store = "STORE-01";
        DailySalesReport report = new DailySalesReport(
                store,
                testDate.toString(),
                32,
                84,
                new BigDecimal("250000.50")
        );

        when(dailyUseCase.generate(testDate, store)).thenReturn(report);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/daily")
                        .param("date", "2025-01-10")
                        .param("store", store))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.store").value(store))
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.totalOrders").value(32))
                .andExpect(jsonPath("$.totalProductsSold").value(84))
                .andExpect(jsonPath("$.totalRevenue").value(250000.50));
    }

    @Test
    void getWeekly_WithValidParameters_ReturnsReport() throws Exception {
        // Arrange
        LocalDate weekStart = LocalDate.of(2025, 1, 6);
        LocalDate weekEnd = LocalDate.of(2025, 1, 12);
        String store = "STORE-01";
        WeeklySalesReport report = new WeeklySalesReport(
                store,
                weekStart,
                weekEnd,
                240,
                650,
                new BigDecimal("1900000.75")
        );

        when(weeklyUseCase.generateWeekly(any(LocalDate.class), eq(store))).thenReturn(report);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/weekly")
                        .param("date", "2025-01-10")
                        .param("store", store))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.store").value(store))
                .andExpect(jsonPath("$.startDate").value(weekStart.toString()))
                .andExpect(jsonPath("$.endDate").value(weekEnd.toString()))
                .andExpect(jsonPath("$.totalOrders").value(240))
                .andExpect(jsonPath("$.totalProductsSold").value(650))
                .andExpect(jsonPath("$.totalRevenue").value(1900000.75));
    }

    @Test
    void getMonthly_WithValidParameters_ReturnsReport() throws Exception {
        // Arrange
        int year = 2025;
        int month = 1;
        String store = "STORE-01";
        MonthlySalesReport report = new MonthlySalesReport(
                store,
                year,
                month,
                1120,
                3100,
                new BigDecimal("8400000.00")
        );

        when(monthlyUseCase.generateMonthlySalesReport(year, month, store)).thenReturn(report);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/monthly")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .param("store", store))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.store").value(store))
                .andExpect(jsonPath("$.year").value(year))
                .andExpect(jsonPath("$.month").value(month))
                .andExpect(jsonPath("$.totalOrders").value(1120))
                .andExpect(jsonPath("$.totalProductsSold").value(3100))
                .andExpect(jsonPath("$.totalRevenue").value(8400000.00));
    }

    @Test
    void getSummary_WithValidStore_ReturnsReport() throws Exception {
        // Arrange
        String store = "STORE-01";
        List<ProductSalesReport> topProducts = Arrays.asList(
                new ProductSalesReport(store, "CAF-001", 240, new BigDecimal("720000.00"), "Capuchino")
        );
        SummaryReport report = new SummaryReport(
                store,
                8231,
                21432,
                new BigDecimal("59000000.00"),
                topProducts
        );

        when(summaryUseCase.generate(store)).thenReturn(report);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/summary")
                        .param("store", store))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.store").value(store))
                .andExpect(jsonPath("$.totalOrders").value(8231))
                .andExpect(jsonPath("$.totalProductsSold").value(21432))
                .andExpect(jsonPath("$.totalRevenue").value(59000000.00))
                .andExpect(jsonPath("$.topProducts").isArray())
                .andExpect(jsonPath("$.topProducts[0].productId").value("CAF-001"));
    }

    @Test
    void getTopProducts_WithValidStore_ReturnsProductList() throws Exception {
        // Arrange
        String store = "STORE-01";
        List<ProductSalesReport> products = Arrays.asList(
                new ProductSalesReport(store, "CAF-001", 240, new BigDecimal("720000.00"), "Capuchino"),
                new ProductSalesReport(store, "CAF-002", 180, new BigDecimal("540000.00"), "Latte")
        );

        when(topProductsUseCase.generateTopProducts(store)).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/top-products")
                        .param("store", store))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].productId").value("CAF-001"))
                .andExpect(jsonPath("$[0].productName").value("Capuchino"))
                .andExpect(jsonPath("$[0].totalSold").value(240))
                .andExpect(jsonPath("$[1].productId").value("CAF-002"))
                .andExpect(jsonPath("$[1].productName").value("Latte"))
                .andExpect(jsonPath("$[1].totalSold").value(180));
    }

    @Test
    void exportAllStatistics_WithValidStore_ReturnsExcelFile() throws Exception {
        // Arrange
        String store = "STORE-01";
        byte[] mockExcel = new byte[]{1, 2, 3, 4, 5};

        when(excelAllService.generateAllStatistics(store)).thenReturn(mockExcel);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/export")
                        .param("store", store))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=all-statistics.xlsx"))
                .andExpect(content().bytes(mockExcel));
    }

    @Test
    void getDaily_WithInvalidDate_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/statistics/daily")
                        .param("date", "invalid-date")
                        .param("store", "STORE-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMonthly_WithZeroMonth_CallsUseCase() throws Exception {
        // Arrange
        String store = "STORE-01";
        MonthlySalesReport report = new MonthlySalesReport(
                store, 2025, 0, 0, 0, BigDecimal.ZERO
        );

        when(monthlyUseCase.generateMonthlySalesReport(2025, 0, store)).thenReturn(report);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/monthly")
                        .param("year", "2025")
                        .param("month", "0")
                        .param("store", store))
                .andExpect(status().isOk());
    }
}