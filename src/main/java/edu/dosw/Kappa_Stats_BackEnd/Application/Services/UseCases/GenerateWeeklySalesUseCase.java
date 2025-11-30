package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.WeeklySalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;


@RequiredArgsConstructor
@Service
public class GenerateWeeklySalesUseCase {
    private final OrderRecordRepository repository;

    public WeeklySalesReport generateWeekly(LocalDate anyDateInWeek, String store) {
        LocalDate start = anyDateInWeek.with(DayOfWeek.MONDAY);
        LocalDate end = anyDateInWeek.with(DayOfWeek.SUNDAY);

        List<OrderRecord> weekRecords = repository.findByDateBetween(start, end).stream().filter(r->r.getStore().equals(store)).toList();;

        List<OrderRecord> records = weekRecords.stream().filter(r->r.getStore().equals(store)).toList();

        int totalOrders = records.size();
        int totalProducts = records.stream().mapToInt(OrderRecord::getQuantity).sum();
        BigDecimal totalRevenue = records.stream()
                .map(OrderRecord::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new WeeklySalesReport(
                store,
                start,
                end,
                totalOrders,
                totalProducts,
                totalRevenue
        );
    }
}
