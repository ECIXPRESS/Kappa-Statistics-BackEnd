package edu.dosw.Kappa_Stats_BackEnd.Application.Services.Clients;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "orders-service", url = "${orders.service.url:http://localhost:8081}")
public interface OrdersClient {

    @GetMapping("/api/orders/store/{storeId}/completed")
    List<OrderResponse> getCompletedOrdersByStore(
            @PathVariable("storeId") String storeId
    );

    @GetMapping("/api/orders/store/{storeId}/date/{date}/completed")
    List<OrderResponse> getCompletedOrdersByStoreAndDate(
            @PathVariable("storeId") String storeId,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    @GetMapping("/api/orders/store/{storeId}/range/completed")
    List<OrderResponse> getCompletedOrdersByStoreAndDateRange(
            @PathVariable("storeId") String storeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}