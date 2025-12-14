//package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;
//
//import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.WeeklySalesReport;
//import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
//import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class GenerateWeeklySalesUseCaseTest {
//
//    @Mock
//    private OrderRecordRepository repository;
//
//    @InjectMocks
//    private GenerateWeeklySalesUseCase useCase;
//
//    private String testStore;
//
//    @BeforeEach
//    void setUp() {
//        testStore = "STORE-01";
//    }
//
//    @Test
//    void generateWeekly_WithValidData_ReturnsCorrectReport() {
//        // Arrange - Wednesday, January 15, 2025
//        LocalDate anyDateInWeek = LocalDate.of(2025, 1, 15);
//        LocalDate weekStart = anyDateInWeek.with(DayOfWeek.MONDAY); // Jan 13
//        LocalDate weekEnd = anyDateInWeek.with(DayOfWeek.SUNDAY);   // Jan 19
//
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", weekStart, "STORE-01", 2, new BigDecimal("100.00")),
//                createOrderRecord("ORD-002", weekStart.plusDays(1), "STORE-01", 3, new BigDecimal("150.00")),
//                createOrderRecord("ORD-003", weekStart.plusDays(2), "STORE-01", 1, new BigDecimal("50.00"))
//        );
//
//        when(repository.findByDateBetween(weekStart, weekEnd)).thenReturn(mockRecords);
//
//        // Act
//        WeeklySalesReport report = useCase.generateWeekly(anyDateInWeek, testStore);
//
//        // Assert
//        assertNotNull(report);
//        assertEquals(testStore, report.store());
//        assertEquals(weekStart, report.startDate());
//        assertEquals(weekEnd, report.endDate());
//        assertEquals(3, report.totalOrders());
//        assertEquals(6, report.totalProductsSold());
//        assertEquals(new BigDecimal("300.00"), report.totalRevenue());
//
//        verify(repository, times(1)).findByDateBetween(weekStart, weekEnd);
//    }
//
//    @Test
//    void generateWeekly_WithMondayDate_CalculatesCorrectWeek() {
//        // Arrange - Monday
//        LocalDate monday = LocalDate.of(2025, 1, 13);
//        LocalDate weekEnd = monday.with(DayOfWeek.SUNDAY);
//
//        when(repository.findByDateBetween(monday, weekEnd)).thenReturn(Collections.emptyList());
//
//        // Act
//        WeeklySalesReport report = useCase.generateWeekly(monday, testStore);
//
//        // Assert
//        assertEquals(monday, report.startDate());
//        assertEquals(weekEnd, report.endDate());
//    }
//
//    @Test
//    void generateWeekly_WithSundayDate_CalculatesCorrectWeek() {
//        // Arrange - Sunday
//        LocalDate sunday = LocalDate.of(2025, 1, 19);
//        LocalDate weekStart = sunday.with(DayOfWeek.MONDAY);
//
//        when(repository.findByDateBetween(weekStart, sunday)).thenReturn(Collections.emptyList());
//
//        // Act
//        WeeklySalesReport report = useCase.generateWeekly(sunday, testStore);
//
//        // Assert
//        assertEquals(weekStart, report.startDate());
//        assertEquals(sunday, report.endDate());
//    }
//
//    @Test
//    void generateWeekly_WithNoOrders_ReturnsZeroValues() {
//        // Arrange
//        LocalDate anyDateInWeek = LocalDate.of(2025, 1, 15);
//        LocalDate weekStart = anyDateInWeek.with(DayOfWeek.MONDAY);
//        LocalDate weekEnd = anyDateInWeek.with(DayOfWeek.SUNDAY);
//
//        when(repository.findByDateBetween(weekStart, weekEnd)).thenReturn(Collections.emptyList());
//
//        // Act
//        WeeklySalesReport report = useCase.generateWeekly(anyDateInWeek, testStore);
//
//        // Assert
//        assertEquals(0, report.totalOrders());
//        assertEquals(0, report.totalProductsSold());
//        assertEquals(BigDecimal.ZERO, report.totalRevenue());
//    }
//
//    @Test
//    void generateWeekly_WithMultipleStores_FiltersCorrectly() {
//        // Arrange
//        LocalDate anyDateInWeek = LocalDate.of(2025, 1, 15);
//        LocalDate weekStart = anyDateInWeek.with(DayOfWeek.MONDAY);
//        LocalDate weekEnd = anyDateInWeek.with(DayOfWeek.SUNDAY);
//
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", weekStart, "STORE-01", 2, new BigDecimal("100.00")),
//                createOrderRecord("ORD-002", weekStart, "STORE-02", 3, new BigDecimal("150.00")),
//                createOrderRecord("ORD-003", weekStart, "STORE-01", 1, new BigDecimal("50.00"))
//        );
//
//        when(repository.findByDateBetween(weekStart, weekEnd)).thenReturn(mockRecords);
//
//        // Act
//        WeeklySalesReport report = useCase.generateWeekly(anyDateInWeek, testStore);
//
//        // Assert
//        assertEquals(2, report.totalOrders());
//        assertEquals(3, report.totalProductsSold());
//        assertEquals(new BigDecimal("150.00"), report.totalRevenue());
//    }
//
//    private OrderRecord createOrderRecord(String orderId, LocalDate date, String store,
//                                          Integer quantity, BigDecimal totalPrice) {
//        return OrderRecord.builder()
//                .id("ID-" + orderId)
//                .orderId(orderId)
//                .productId("PROD-001")
//                .quantity(quantity)
//                .totalPrice(totalPrice)
//                .date(date)
//                .store(store)
//                .productName("Test Product")
//                .build();
//    }
//}