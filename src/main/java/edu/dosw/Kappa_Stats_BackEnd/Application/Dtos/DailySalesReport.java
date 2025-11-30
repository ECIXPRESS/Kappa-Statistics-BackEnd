package edu.dosw.Kappa_Stats_BackEnd.Application.Dtos;

import java.math.BigDecimal;

public record DailySalesReport(
        String store,
        String date,
        Integer totalOrders,
        Integer totalProductsSold,
        BigDecimal totalRevenue
) {}
