package edu.dosw.Kappa_Stats_BackEnd.Application.Ports;

import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.Clients.OrdersClient;
import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.*;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRecordRepositoryAdapter implements OrderRecordRepositoryPort {

    private final OrderRecordRepository orderRecordRepository;
    private final OrdersClient ordersClient;

    @Override
    public List<OrderRecord> findByStoreAndDateBetween(String store, LocalDate startDate, LocalDate endDate) {
        log.info("Consultando órdenes del microservicio Orders para tienda {} entre {} y {}",
                store, startDate, endDate);

        try {
            List<OrderResponse> orders = ordersClient.getCompletedOrdersByStoreAndDateRange(store, startDate, endDate);

            return convertOrdersToOrderRecords(orders, store);

        } catch (Exception e) {
            log.error("Error al obtener órdenes de Orders service: {}", e.getMessage());
            log.info("Usando datos locales como fallback");
            return orderRecordRepository.findByStoreAndDateBetween(store, startDate, endDate);
        }
    }

    @Override
    public List<OrderRecord> findByStoreAndDate(String store, LocalDate date) {
        log.info("Consultando órdenes del microservicio Orders para tienda {} en fecha {}", store, date);

        try {
            List<OrderResponse> orders = ordersClient.getCompletedOrdersByStoreAndDate(store, date);
            return convertOrdersToOrderRecords(orders, store);

        } catch (Exception e) {
            log.error("Error al obtener órdenes de Orders service: {}", e.getMessage());
            log.info("Usando datos locales como fallback");
            return orderRecordRepository.findByStoreAndDate(store, date);
        }
    }

    @Override
    public List<OrderRecord> findByStore(String store) {
        log.info("Consultando todas las órdenes del microservicio Orders para tienda {}", store);

        try {
            List<OrderResponse> orders = ordersClient.getCompletedOrdersByStore(store);
            return convertOrdersToOrderRecords(orders, store);

        } catch (Exception e) {
            log.error("Error al obtener órdenes de Orders service: {}", e.getMessage());
            log.info("Usando datos locales como fallback");
            return orderRecordRepository.findByStore(store);
        }
    }

    private List<OrderRecord> convertOrdersToOrderRecords(List<OrderResponse> orders, String store) {
        List<OrderRecord> orderRecords = new ArrayList<>();

        if (orders == null || orders.isEmpty()) {
            log.info("No se encontraron órdenes para la tienda {}", store);
            return orderRecords;
        }

        log.info("Convirtiendo {} órdenes a OrderRecords", orders.size());

        for (OrderResponse order : orders) {
            if (!"COMPLETED".equalsIgnoreCase(order.status()) &&
                    !"DELIVERED".equalsIgnoreCase(order.status()) &&
                    !"ENTREGADO".equalsIgnoreCase(order.status())) {
                continue;
            }

            if (order.items() != null) {
                for (OrderItemResponse item : order.items()) {
                    OrderRecord record = OrderRecord.builder()
                            .store(order.store())
                            .orderId(order.id())
                            .productId(item.productId())
                            .productName(item.productName())
                            .quantity(item.quantity())
                            .totalPrice(item.subtotal() != null ? item.subtotal() : BigDecimal.ZERO)
                            .date(order.createdAt().toLocalDate())
                            .build();

                    orderRecords.add(record);
                }
            }
        }

        log.info("Convertidas {} órdenes a {} OrderRecords", orders.size(), orderRecords.size());
        return orderRecords;
    }

    @Override
    public OrderRecord save(OrderRecord orderRecord) {
        return orderRecordRepository.save(orderRecord);
    }

    @Override
    public List<OrderRecord> findAll() {
        return orderRecordRepository.findAll();
    }

    @Override
    public Optional<OrderRecord> findById(String id) {
        return orderRecordRepository.findById(id);
    }

    @Override
    public List<OrderRecord> findByDate(LocalDate date) {
        return orderRecordRepository.findByDate(date);
    }

    @Override
    public List<OrderRecord> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return orderRecordRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    public long count() {
        return orderRecordRepository.count();
    }

    @Override
    public void deleteById(String id) {
        orderRecordRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return orderRecordRepository.existsById(id);
    }
}