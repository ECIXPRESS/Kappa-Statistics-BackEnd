package edu.dosw.Kappa_Stats_BackEnd.Application.Ports;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.ProductSalesReport;
import java.util.List;

public interface ProductRankingPort {
    List<ProductSalesReport> generateTopProducts(String store);
}