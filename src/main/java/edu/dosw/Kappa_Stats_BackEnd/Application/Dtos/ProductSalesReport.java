package edu.dosw.Kappa_Stats_BackEnd.Application.Dtos;

import java.math.BigDecimal;

public record ProductSalesReport(
        String productId,
        Integer totalSold,
        BigDecimal totalRevenue
) {}
