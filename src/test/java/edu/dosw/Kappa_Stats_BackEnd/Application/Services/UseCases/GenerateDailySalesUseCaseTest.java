//package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;
//
//import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.DailySalesReport;
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
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class GenerateDailySalesUseCaseTest {
//
//    @Mock
//    private OrderRecordRepository repository;
//
//    @InjectMocks
//    private GenerateDailySalesUseCase useCase;
//
//    private LocalDate testDate;
//    private String testStore;
//
//    @BeforeEach
//    void setUp() {
//        testDate = LocalDate.of(2025, 1, 15);
//        testStore = "STORE-01";
//    }
//
//    @Test
//    void generate_WithValidData_ReturnsCorrectReport() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", "PROD-001", 2, new BigDecimal("50.00"), "STORE-01"),
//                createOrderRecord("ORD-002", "PROD-002", 3, new BigDecimal("75.00"), "STORE-01"),
//                createOrderRecord("ORD-003", "PROD-001", 1, new BigDecimal("25.00"), "STORE-01")
//        );
//
//        when(repository.findByDate(testDate)).thenReturn(mockRecords);
//
//        // Act
//        DailySalesReport report = useCase.generate(testDate, testStore);
//
//        // Assert
//        assertNotNull(report);
//        assertEquals(testStore, report.store());
//        assertEquals(testDate.toString(), report.date());
//        assertEquals(3, report.totalOrders());
//        assertEquals(6, report.totalProductsSold());
//        assertEquals(new BigDecimal("150.00"), report.totalRevenue());
//
//        verify(repository, times(1)).findByDate(testDate);
//    }
//
//    @Test
//    void generate_WithNoOrders_ReturnsZeroValues() {
//        // Arrange
//        when(repository.findByDate(testDate)).thenReturn(Collections.emptyList());
//
//        // Act
//        DailySalesReport report = useCase.generate(testDate, testStore);
//
//        // Assert
//        assertNotNull(report);
//        assertEquals(testStore, report.store());
//        assertEquals(0, report.totalOrders());
//        assertEquals(0, report.totalProductsSold());
//        assertEquals(BigDecimal.ZERO, report.totalRevenue());
//    }
//
//    @Test
//    void generate_WithMultipleStores_FiltersCorrectly() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", "PROD-001", 2, new BigDecimal("50.00"), "STORE-01"),
//                createOrderRecord("ORD-002", "PROD-002", 3, new BigDecimal("75.00"), "STORE-02"),
//                createOrderRecord("ORD-003", "PROD-001", 1, new BigDecimal("25.00"), "STORE-01")
//        );
//
//        when(repository.findByDate(testDate)).thenReturn(mockRecords);
//
//        // Act
//        DailySalesReport report = useCase.generate(testDate, testStore);
//
//        // Assert
//        assertEquals(2, report.totalOrders());
//        assertEquals(3, report.totalProductsSold());
//        assertEquals(new BigDecimal("75.00"), report.totalRevenue());
//    }
//
//    private OrderRecord createOrderRecord(String orderId, String productId, Integer quantity,
//                                          BigDecimal totalPrice, String store) {
//        return OrderRecord.builder()
//                .id("ID-" + orderId)
//                .orderId(orderId)
//                .productId(productId)
//                .quantity(quantity)
//                .totalPrice(totalPrice)
//                .date(testDate)
//                .store(store)
//                .productName("Product " + productId)
//                .build();
//    }
//}