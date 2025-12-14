//package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;
//
//import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
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
//class GenerateProductRankingUseCaseTest {
//
//    @Mock
//    private OrderRecordRepository repository;
//
//    @InjectMocks
//    private GenerateProductRankingUseCase useCase;
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
//    void generateTopProducts_WithValidData_ReturnsSortedByQuantity() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", "PROD-001", "Coffee", 10, new BigDecimal("100.00"), "STORE-01"),
//                createOrderRecord("ORD-002", "PROD-001", "Coffee", 5, new BigDecimal("50.00"), "STORE-01"),
//                createOrderRecord("ORD-003", "PROD-002", "Tea", 8, new BigDecimal("80.00"), "STORE-01"),
//                createOrderRecord("ORD-004", "PROD-003", "Juice", 20, new BigDecimal("200.00"), "STORE-01")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//
//        // Act
//        List<ProductSalesReport> result = useCase.generateTopProducts(testStore);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(3, result.size());
//
//        // Verify sorted by quantity (descending)
//        assertEquals("PROD-003", result.get(0).productId());
//        assertEquals(20, result.get(0).totalSold());
//        assertEquals("Juice", result.get(0).productName());
//
//        assertEquals("PROD-001", result.get(1).productId());
//        assertEquals(15, result.get(1).totalSold()); // 10 + 5
//        assertEquals("Coffee", result.get(1).productName());
//
//        assertEquals("PROD-002", result.get(2).productId());
//        assertEquals(8, result.get(2).totalSold());
//        assertEquals("Tea", result.get(2).productName());
//
//        verify(repository, times(1)).findAll();
//    }
//
//    @Test
//    void generateTopProducts_AggregatesRevenueCorrectly() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", "PROD-001", "Coffee", 10, new BigDecimal("100.00"), "STORE-01"),
//                createOrderRecord("ORD-002", "PROD-001", "Coffee", 5, new BigDecimal("75.00"), "STORE-01")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//
//        // Act
//        List<ProductSalesReport> result = useCase.generateTopProducts(testStore);
//
//        // Assert
//        assertEquals(1, result.size());
//        assertEquals(new BigDecimal("175.00"), result.get(0).totalRevenue());
//    }
//
//    @Test
//    void generateTopProducts_WithNoOrders_ReturnsEmptyList() {
//        // Arrange
//        when(repository.findAll()).thenReturn(Collections.emptyList());
//
//        // Act
//        List<ProductSalesReport> result = useCase.generateTopProducts(testStore);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void generateTopProducts_WithMultipleStores_FiltersCorrectly() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", "PROD-001", "Coffee", 10, new BigDecimal("100.00"), "STORE-01"),
//                createOrderRecord("ORD-002", "PROD-002", "Tea", 15, new BigDecimal("150.00"), "STORE-02"),
//                createOrderRecord("ORD-003", "PROD-003", "Juice", 5, new BigDecimal("50.00"), "STORE-01")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//
//        // Act
//        List<ProductSalesReport> result = useCase.generateTopProducts(testStore);
//
//        // Assert
//        assertEquals(2, result.size());
//        assertTrue(result.stream().allMatch(p -> p.store().equals(testStore)));
//        assertFalse(result.stream().anyMatch(p -> p.productId().equals("PROD-002")));
//    }
//
//    @Test
//    void generateTopProducts_WithSingleProduct_ReturnsOneItem() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", "PROD-001", "Coffee", 10, new BigDecimal("100.00"), "STORE-01"),
//                createOrderRecord("ORD-002", "PROD-001", "Coffee", 5, new BigDecimal("50.00"), "STORE-01")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//
//        // Act
//        List<ProductSalesReport> result = useCase.generateTopProducts(testStore);
//
//        // Assert
//        assertEquals(1, result.size());
//        assertEquals("PROD-001", result.get(0).productId());
//        assertEquals(15, result.get(0).totalSold());
//    }
//
//    @Test
//    void generateTopProducts_WithEqualQuantities_MaintainsOrder() {
//        // Arrange
//        List<OrderRecord> mockRecords = Arrays.asList(
//                createOrderRecord("ORD-001", "PROD-001", "Coffee", 10, new BigDecimal("100.00"), "STORE-01"),
//                createOrderRecord("ORD-002", "PROD-002", "Tea", 10, new BigDecimal("150.00"), "STORE-01")
//        );
//
//        when(repository.findAll()).thenReturn(mockRecords);
//
//        // Act
//        List<ProductSalesReport> result = useCase.generateTopProducts(testStore);
//
//        // Assert
//        assertEquals(2, result.size());
//        assertEquals(10, result.get(0).totalSold());
//        assertEquals(10, result.get(1).totalSold());
//    }
//
//    private OrderRecord createOrderRecord(String orderId, String productId, String productName,
//                                          Integer quantity, BigDecimal totalPrice, String store) {
//        return OrderRecord.builder()
//                .id("ID-" + orderId)
//                .orderId(orderId)
//                .productId(productId)
//                .productName(productName)
//                .quantity(quantity)
//                .totalPrice(totalPrice)
//                .date(testDate)
//                .store(store)
//                .build();
//    }
//}