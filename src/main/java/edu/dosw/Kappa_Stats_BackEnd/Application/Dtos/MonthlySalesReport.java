package edu.dosw.Kappa_Stats_BackEnd.Application.Dtos;

import java.math.BigDecimal;

public record MonthlySalesReport (
        String store,
        Integer year,
        Integer month,
        Integer totalOrders,
        Integer totalProductsSold,
        BigDecimal totalRevenue

){}
