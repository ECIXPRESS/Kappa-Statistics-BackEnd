package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.MonthlySalesReport;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateMonthlySalesUseCase {

    private final OrderRecordRepository orderRecordRepository;

    public MonthlySalesReport generateMonthlySalesReport(int year, int month, String store){

        LocalDate start = LocalDate.of(year,month,1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());


        List<OrderRecord> records = orderRecordRepository.findByDateBetween(start, end).stream().filter(r->r.getStore().equals(store)).toList();

        int totalOrders = records.size();
        int totalProductsSold = records.stream().mapToInt(OrderRecord::getQuantity).sum();

        BigDecimal totalRevenue = records.stream().map(OrderRecord::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlySalesReport(
                store,
                year,
                month,
                totalOrders,
                totalProductsSold,
                totalRevenue
        );

    }

}
