package edu.dosw.Kappa_Stats_BackEnd.Application.Dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        String store,
        String status,
        LocalDateTime createdAt,
        BigDecimal total,
        String userId,
        List<OrderItemResponse> items
) {}