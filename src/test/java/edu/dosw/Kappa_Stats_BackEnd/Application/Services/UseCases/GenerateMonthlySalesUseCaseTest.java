package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.MonthlySalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateMonthlySalesUseCaseTest {

    @Mock
    private OrderRecordRepository orderRecordRepository;

    @InjectMocks
    private GenerateMonthlySalesUseCase useCase;

    private String testStore;

    @BeforeEach
    void setUp() {
        testStore = "STORE-01";
    }

    @Test
    void generateMonthlySalesReport_WithValidData_ReturnsCorrectReport() {
        // Arrange
        int year = 2025;
        int month = 1;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = LocalDate.of(year, month, 31);

        List<OrderRecord> mockRecords = Arrays.asList(
                createOrderRecord("ORD-001", start, "STORE-01", 5, new BigDecimal("200.00")),
                createOrderRecord("ORD-002", start.plusDays(10), "STORE-01", 3, new BigDecimal("150.00")),
                createOrderRecord("ORD-003", start.plusDays(20), "STORE-01", 2, new BigDecimal("100.00"))
        );

        when(orderRecordRepository.findByDateBetween(start, end)).thenReturn(mockRecords);

        // Act
        MonthlySalesReport report = useCase.generateMonthlySalesReport(year, month, testStore);

        // Assert
        assertNotNull(report);
        assertEquals(testStore, report.store());
        assertEquals(year, report.year());
        assertEquals(month, report.month());
        assertEquals(3, report.totalOrders());
        assertEquals(10, report.totalProductsSold());
        assertEquals(new BigDecimal("450.00"), report.totalRevenue());

        verify(orderRecordRepository, times(1)).findByDateBetween(start, end);
    }

    @Test
    void generateMonthlySalesReport_WithFebruary_HandlesCorrectDays() {
        // Arrange
        int year = 2025;
        int month = 2;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = LocalDate.of(year, month, 28); // 2025 is not a leap year

        when(orderRecordRepository.findByDateBetween(start, end)).thenReturn(Collections.emptyList());

        // Act
        MonthlySalesReport report = useCase.generateMonthlySalesReport(year, month, testStore);

        // Assert
        verify(orderRecordRepository, times(1)).findByDateBetween(start, end);
    }

    @Test
    void generateMonthlySalesReport_WithLeapYear_HandlesFebruary29() {
        // Arrange
        int year = 2024;
        int month = 2;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = LocalDate.of(year, month, 29); // 2024 is a leap year

        when(orderRecordRepository.findByDateBetween(start, end)).thenReturn(Collections.emptyList());

        // Act
        MonthlySalesReport report = useCase.generateMonthlySalesReport(year, month, testStore);

        // Assert
        verify(orderRecordRepository, times(1)).findByDateBetween(start, end);
    }

    @Test
    void generateMonthlySalesReport_WithNoOrders_ReturnsZeroValues() {
        // Arrange
        int year = 2025;
        int month = 1;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = LocalDate.of(year, month, 31);

        when(orderRecordRepository.findByDateBetween(start, end)).thenReturn(Collections.emptyList());

        // Act
        MonthlySalesReport report = useCase.generateMonthlySalesReport(year, month, testStore);

        // Assert
        assertEquals(0, report.totalOrders());
        assertEquals(0, report.totalProductsSold());
        assertEquals(BigDecimal.ZERO, report.totalRevenue());
    }

    @Test
    void generateMonthlySalesReport_WithMultipleStores_FiltersCorrectly() {
        // Arrange
        int year = 2025;
        int month = 1;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = LocalDate.of(year, month, 31);

        List<OrderRecord> mockRecords = Arrays.asList(
                createOrderRecord("ORD-001", start, "STORE-01", 5, new BigDecimal("200.00")),
                createOrderRecord("ORD-002", start, "STORE-02", 3, new BigDecimal("150.00")),
                createOrderRecord("ORD-003", start, "STORE-01", 2, new BigDecimal("100.00"))
        );

        when(orderRecordRepository.findByDateBetween(start, end)).thenReturn(mockRecords);

        // Act
        MonthlySalesReport report = useCase.generateMonthlySalesReport(year, month, testStore);

        // Assert
        assertEquals(2, report.totalOrders());
        assertEquals(7, report.totalProductsSold());
        assertEquals(new BigDecimal("300.00"), report.totalRevenue());
    }

    @Test
    void generateMonthlySalesReport_WithDecember_HandlesEndOfYear() {
        // Arrange
        int year = 2025;
        int month = 12;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = LocalDate.of(year, month, 31);

        List<OrderRecord> mockRecords = Arrays.asList(
                createOrderRecord("ORD-001", start, "STORE-01", 1, new BigDecimal("50.00"))
        );

        when(orderRecordRepository.findByDateBetween(start, end)).thenReturn(mockRecords);

        // Act
        MonthlySalesReport report = useCase.generateMonthlySalesReport(year, month, testStore);

        // Assert
        assertEquals(12, report.month());
        assertEquals(2025, report.year());
    }

    private OrderRecord createOrderRecord(String orderId, LocalDate date, String store,
                                          Integer quantity, BigDecimal totalPrice) {
        return OrderRecord.builder()
                .id("ID-" + orderId)
                .orderId(orderId)
                .productId("PROD-001")
                .quantity(quantity)
                .totalPrice(totalPrice)
                .date(date)
                .store(store)
                .productName("Test Product")
                .build();
    }
}