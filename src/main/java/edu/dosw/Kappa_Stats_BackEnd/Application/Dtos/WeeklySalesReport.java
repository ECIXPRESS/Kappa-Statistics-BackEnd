package edu.dosw.Kappa_Stats_BackEnd.Application.Dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeeklySalesReport(
        String store,
        LocalDate startDate,
        LocalDate endDate,
        Integer totalOrders,
        Integer totalProductsSold,
        BigDecimal totalRevenue
) {}
