package edu.dosw.Kappa_Stats_BackEnd.Application.Dtos;

import java.math.BigDecimal;
import java.util.List;

public record SummaryReport(
        Integer totalOrders,
        Integer totalProductsSold,
        BigDecimal totalRevenue,
        List<ProductSalesReport> topProducts
) {}
