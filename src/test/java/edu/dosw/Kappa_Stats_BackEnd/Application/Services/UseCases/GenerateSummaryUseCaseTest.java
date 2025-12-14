//package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;
//
//import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
//import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.SummaryReport;
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
//class GenerateSummaryUseCaseTest {
//
//    @Mock
//    private OrderRecordRepository repository;
//
//    @Mock
//    private GenerateProductRankingUseCase rankingUseCase;
//
//    @InjectMocks
//    private GenerateSummaryUseCase useCase;
//
//    private String testStore;
//    private LocalDate testDate;
//
//    @BeforeEach
//    void setUp() {
//        testStore = "STORE-01";
//        testDate = LocalDate.of(2025, 1, 15);
//    }
//
//    @Test
//    void generate_WithValidData_ReturnsCompleteSummary() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", 10, new BigDecimal("100.00"), "STORE-01"),
//                createOrderRecord("ORD-002", 5, new BigDecimal("50.00"), "STORE-01"),
//                createOrderRecord("ORD-003", 8, new BigDecimal("80.00"), "STORE-01")
//        );
//
//        List<ProductSalesReport> mockTopProducts = Arrays.asList(
//                new ProductSalesReport("STORE-01", "PROD-001", 10, new BigDecimal("100.00"), "Coffee"),
//                new ProductSalesReport("STORE-01", "PROD-002", 5, new BigDecimal("50.00"), "Tea")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//        when(rankingUseCase.generateTopProducts(testStore)).thenReturn(mockTopProducts);
//
//        // Act
//        SummaryReport result = useCase.generate(testStore);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(testStore, result.store());
//        assertEquals(3, result.totalOrders());
//        assertEquals(23, result.totalProductsSold());
//        assertEquals(new BigDecimal("230.00"), result.totalRevenue());
//        assertEquals(2, result.topProducts().size());
//
//        verify(repository, times(1)).findAll();
//        verify(rankingUseCase, times(1)).generateTopProducts(testStore);
//    }
//
//    @Test
//    void generate_WithNoOrders_ReturnsZeroValues() {
//        // Arrange
//        when(repository.findAll()).thenReturn(Collections.emptyList());
//        when(rankingUseCase.generateTopProducts(testStore)).thenReturn(Collections.emptyList());
//
//        // Act
//        SummaryReport result = useCase.generate(testStore);
//
//        // Assert
//        assertEquals(0, result.totalOrders());
//        assertEquals(0, result.totalProductsSold());
//        assertEquals(BigDecimal.ZERO, result.totalRevenue());
//        assertTrue(result.topProducts().isEmpty());
//    }
//
//    @Test
//    void generate_WithMultipleStores_FiltersCorrectly() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", 10, new BigDecimal("100.00"), "STORE-01"),
//                createOrderRecord("ORD-002", 5, new BigDecimal("50.00"), "STORE-02"),
//                createOrderRecord("ORD-003", 8, new BigDecimal("80.00"), "STORE-01")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//        when(rankingUseCase.generateTopProducts(testStore)).thenReturn(Collections.emptyList());
//
//        // Act
//        SummaryReport result = useCase.generate(testStore);
//
//        // Assert
//        assertEquals(2, result.totalOrders());
//        assertEquals(18, result.totalProductsSold());
//        assertEquals(new BigDecimal("180.00"), result.totalRevenue());
//    }
//
//    @Test
//    void generate_IncludesTopProducts() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", 10, new BigDecimal("100.00"), "STORE-01")
//        );
//
//        List<ProductSalesReport> mockTopProducts = Arrays.asList(
//                new ProductSalesReport("STORE-01", "PROD-001", 10, new BigDecimal("100.00"), "Coffee"),
//                new ProductSalesReport("STORE-01", "PROD-002", 8, new BigDecimal("80.00"), "Tea"),
//                new ProductSalesReport("STORE-01", "PROD-003", 5, new BigDecimal("50.00"), "Juice")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//        when(rankingUseCase.generateTopProducts(testStore)).thenReturn(mockTopProducts);
//
//        // Act
//        SummaryReport result = useCase.generate(testStore);
//
//        // Assert
//        assertNotNull(result.topProducts());
//        assertEquals(3, result.topProducts().size());
//        assertEquals("PROD-001", result.topProducts().get(0).productId());
//    }
//
//    @Test
//    void generate_CalculatesRevenueSumCorrectly() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", 1, new BigDecimal("99.99"), "STORE-01"),
//                createOrderRecord("ORD-002", 1, new BigDecimal("0.01"), "STORE-01")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//        when(rankingUseCase.generateTopProducts(testStore)).thenReturn(Collections.emptyList());
//
//        // Act
//        SummaryReport result = useCase.generate(testStore);
//
//        // Assert
//        assertEquals(new BigDecimal("100.00"), result.totalRevenue());
//    }
//
//    private OrderRecord createOrderRecord(String orderId, Integer quantity,
//                                          BigDecimal totalPrice, String store) {
//        return OrderRecord.builder()
//                .id("ID-" + orderId)
//                .orderId(orderId)
//                .productId("PROD-001")
//                .productName("Test Product")
//                .quantity(quantity)
//                .totalPrice(totalPrice)
//                .date(testDate)
//                .store(store)
//                .build();
//    }
//}