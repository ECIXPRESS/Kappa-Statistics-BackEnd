package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.DailySalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateDailySalesUseCase {

    private final OrderRecordRepository repository;

    public DailySalesReport generate(LocalDate date, String store) {
        List<OrderRecord> records = repository.findByDate(date).stream().filter(r->r.getStore().equals(store)).toList();
        int totalOrders = records.size();
        int totalProducts = records.stream().mapToInt(OrderRecord::getQuantity).sum();
        BigDecimal totalRevenue = records.stream()
                .map(OrderRecord::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DailySalesReport(
                store,
                date.toString(),
                totalOrders,
                totalProducts,
                totalRevenue
        );
    }
}