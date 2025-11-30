package edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence;

import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface OrderRecordRepository extends MongoRepository<OrderRecord, String> {

    List<OrderRecord> findByDate(LocalDate date);
    List<OrderRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
